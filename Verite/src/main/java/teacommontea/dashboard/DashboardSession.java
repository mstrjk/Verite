package teacommontea.dashboard;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

import org.bukkit.plugin.Plugin;

import teacommontea.util.ConsoleColours;
import teacommontea.util.Trace;

public final class DashboardSession {

    private static final String TRUST_PERMISSION = "verite.admin";
    private static final int TIMEOUT_MS = 20_000;
    private static final long APPROVAL_MS = 5L * 60L * 1000L;
    private static final long SESSION_MS = 60L * 60L * 1000L;
    private static final int MAX_BYTES = 8 * 1024 * 1024;

    private final Plugin plugin;
    private final DashboardKeys keys;
    private final String apiBase;
    private final UUID owner;
    private final String ownerName;

    private String sessionKey;
    private String nonce;
    private long nonceExpiresAt;
    private long sessionExpiresAt;
    private String browserKey;
    private RealtimeChannel channel;
    private volatile boolean trusted;

    public DashboardSession(Plugin plugin, DashboardKeys keys, String apiBase, UUID owner, String ownerName) {
        this.plugin = plugin;
        this.keys = keys;
        this.apiBase = apiBase.endsWith("/") ? apiBase.substring(0, apiBase.length() - 1) : apiBase;
        this.owner = owner;
        this.ownerName = ownerName;
    }

    public String key() {
        return sessionKey;
    }

    public String nonce() {
        return nonce;
    }

    public String ownerName() {
        return ownerName;
    }

    public boolean approvalExpired() {
        return System.currentTimeMillis() > nonceExpiresAt;
    }

    public boolean expired() {
        return System.currentTimeMillis() > sessionExpiresAt;
    }

    public boolean trusted() {
        return trusted;
    }

    public UUID owner() {
        return owner;
    }

    public String start(String serverName, String snapshot) throws Exception {
        String body = Json.object(
                "payload", Json.raw(snapshot),
                "pluginKey", keys.publicKey(),
                "serverName", serverName
        );

        String reply = post(apiBase + "/api/dashboard/session", body);
        Map<String, String> fields = Json.flat(reply);

        sessionKey = fields.get("key");
        String realtimeUrl = fields.get("realtimeUrl");
        String realtimeKey = fields.get("realtimeKey");
        String channelName = fields.get("channel");

        if (sessionKey == null) {
            throw new IllegalStateException("The dashboard did not return a session key.");
        }
        if (realtimeUrl == null || realtimeKey == null) {
            throw new IllegalStateException("The dashboard did not return its relay details.");
        }

        this.nonce = newNonce();
        this.nonceExpiresAt = System.currentTimeMillis() + APPROVAL_MS;
        this.sessionExpiresAt = System.currentTimeMillis() + SESSION_MS;

        channel = new RealtimeChannel(realtimeUrl, realtimeKey, channelName,
                this::onFrame,
                () -> {},
                problem -> plugin.getLogger().warning(
                        ConsoleColours.bad("Dashboard channel problem: " + problem)));
        channel.open();

        return sessionKey;
    }

    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    public void approve(String approvedBy) {
        trusted = true;
        plugin.getLogger().info(ConsoleColours.ok("Dashboard approved by ")
                + ConsoleColours.value(approvedBy)
                + ConsoleColours.ok(" for ")
                + ConsoleColours.value(ownerName)
                + ConsoleColours.ok(". The browser now has control."));
        send(Json.object("type", "connected", "sessionKey", sessionKey));
    }

    public void pushState(String players, String state, String punishments) {
        if (!trusted || channel == null || !channel.isOpen()) {
            return;
        }
        send(Json.object(
                "type", "state",
                "online", Json.raw(players),
                "state", Json.raw(state),
                "punishments", punishments == null ? null : Json.raw(punishments)
        ));
    }

    private void onFrame(Map<String, String> frame) {
        String msg = frame.get("msg");
        String signature = frame.get("signature");
        if (msg == null || signature == null) {
            return;
        }

        Map<String, String> body = Json.flat(msg);
        String type = body.get("type");
        if (type == null) {
            return;
        }

        if ("hello".equals(type)) {
            String offered = body.get("browserKey");
            if (offered == null || !DashboardKeys.verify(offered, msg, signature)) {
                plugin.getLogger().warning(ConsoleColours.bad(
                        "A dashboard browser failed its signature check and was refused."));
                return;
            }
            browserKey = offered;
            teacommontea.util.sched.Sched.executeAsync(() -> {
                if (!trusted && remembered(offered)) {
                    trusted = true;
                    plugin.getLogger().info(ConsoleColours.ok(
                            "A previously trusted browser reconnected for ")
                            + ConsoleColours.value(ownerName)
                            + ConsoleColours.ok(". Approval was not needed."));
                }
                send(Json.object(
                        "type", "hello-reply",
                        "trusted", trusted,
                        "nonce", trusted ? null : nonce,
                        "serverId", keys.publicKey(),
                        "adminUuid", owner.toString(),
                        "server", plugin.getServer().getName()
                ));
                if (!trusted) {
                    promptOwner();
                }
            });
            return;
        }

        if (browserKey == null || !DashboardKeys.verify(browserKey, msg, signature)) {
            plugin.getLogger().warning(ConsoleColours.bad(
                    "A dashboard message failed its signature check and was ignored."));
            return;
        }

        switch (type) {
            case "ping" -> send(Json.object("type", "pong"));
            case "connected" -> { }
            case "change-request" -> handleChange(body);
            case "query" -> handleQuery(body);
            default -> { }
        }
    }

    private void handleChange(Map<String, String> body) {
        if (!trusted) {
            plugin.getLogger().warning(ConsoleColours.bad(
                    "An unapproved dashboard tried to change something. It was refused."));
            reply(body, false, "This connection has not been approved yet.");
            return;
        }
        String changeKey = body.get("changeKey");
        if (changeKey == null) {
            reply(body, false, "That change was missing its key.");
            return;
        }
        teacommontea.util.sched.Sched.executeAsync(() -> {
            try {
                String payload = get(apiBase + "/api/dashboard/change/" + changeKey);
                DashboardChanges.apply(plugin, payload, ownerName, owner.toString());
                post(apiBase + "/api/dashboard/change/" + changeKey, "{}");
                reply(body, true, null);
                Dashboard.changed(plugin);
            } catch (IllegalArgumentException e) {
                reply(body, false, e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().warning(ConsoleColours.bad(
                        "Failed to apply a dashboard change.") + Trace.of(e));
                reply(body, false, "That change could not be applied.");
            }
        });
    }

    private void handleQuery(Map<String, String> body) {
        if (!trusted) {
            reply(body, false, "This connection has not been approved yet.");
            return;
        }
        Runnable lookup = () -> {
            try {
                String result = DashboardQueries.run(plugin, body);
                send(Json.object(
                        "type", "query-response",
                        "requestId", body.get("requestId"),
                        "ok", true,
                        "result", Json.raw(result)
                ));
            } catch (Exception e) {
                reply(body, false, "That lookup failed.");
            }
        };

        if (DashboardQueries.readsServer(body.get("what"))) {
            teacommontea.util.sched.Sched.executeGlobal(lookup);
        } else {
            teacommontea.util.sched.Sched.executeAsync(lookup);
        }
    }

    private void reply(Map<String, String> body, boolean ok, String error) {
        send(Json.object(
                "type", body.get("type") != null && body.get("type").startsWith("query")
                        ? "query-response" : "change-response",
                "requestId", body.get("requestId"),
                "ok", ok,
                "error", error
        ));
    }

    private void send(String message) {
        try {
            if (channel != null && channel.isOpen()) {
                channel.broadcast(message, keys.sign(message));
            }
        } catch (Exception e) {
            plugin.getLogger().warning(ConsoleColours.bad(
                    "Failed to deliver information to the dashboard.") + Trace.of(e));
        }
    }

    private boolean remembered(String offeredKey) {
        try {
            String query = "?server=" + url(keys.publicKey())
                    + "&admin=" + url(owner.toString())
                    + "&key=" + url(offeredKey);
            String reply = get(apiBase + "/api/dashboard/trust" + query);
            return "true".equals(Json.flat(reply).get("trusted"));
        } catch (Exception e) {
            return false;
        }
    }

    private static String url(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void promptOwner() {
        teacommontea.util.sched.Sched.executeGlobal(() -> {
            String line = teacommontea.util.Colours.BRAND_ACCENT_SECONDARY
                    + "A dashboard window has connected. If it was you, "
                    + "<click:run_command:'/verite dashboard trust " + nonce + "'>"
                    + "<hover:show_text:'"
                    + teacommontea.util.Colours.BRAND_ACCENT_SECONDARY
                    + "Only approve this if it was you.'>"
                    + teacommontea.util.Colours.BRAND_ACCENT + "["
                    + teacommontea.util.Colours.BRAND + "click here"
                    + teacommontea.util.Colours.BRAND_ACCENT + "]</hover></click>"
                    + teacommontea.util.Colours.BRAND_ACCENT_SECONDARY
                    + " to trust the session.";

            String prefixed = teacommontea.util.Messages.prefix() + " " + line;
            for (org.bukkit.entity.Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getUniqueId().equals(owner) || p.hasPermission(TRUST_PERMISSION)) {
                    teacommontea.util.text.Text.send(p, prefixed);
                }
            }
        });
    }

    private static String newNonce() {
        byte[] bytes = new byte[6];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String post(String url, String body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(TIMEOUT_MS);
        c.setReadTimeout(TIMEOUT_MS);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("User-Agent", "Verite/" + plugin.getDescription().getVersion());
        try (OutputStream out = c.getOutputStream()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }
        int code = c.getResponseCode();
        if (code < 200 || code > 299) {
            throw new IllegalStateException("The dashboard returned HTTP " + code + ".");
        }
        return read(c.getInputStream());
    }

    private String get(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(TIMEOUT_MS);
        c.setReadTimeout(TIMEOUT_MS);
        c.setRequestProperty("User-Agent", "Verite/" + plugin.getDescription().getVersion());
        int code = c.getResponseCode();
        if (code < 200 || code > 299) {
            throw new IllegalStateException("The dashboard returned HTTP " + code + ".");
        }
        return read(c.getInputStream());
    }

    private static String read(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        int total = 0;
        while ((n = in.read(buf)) > 0) {
            total += n;
            if (total > MAX_BYTES) {
                throw new IllegalStateException("The dashboard reply was too large.");
            }
            out.write(buf, 0, n);
        }
        in.close();
        return out.toString(StandardCharsets.UTF_8);
    }
}
