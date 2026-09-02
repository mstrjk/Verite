package teacommontea;

import teacommontea.util.Colours;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import teacommontea.veritesauver.captcha.CaptchaCommand;
import teacommontea.veritesauver.captcha.CaptchaManager;
import teacommontea.util.Messages;

import teacommontea.veritedoux.EveEntry;
import teacommontea.veritedoux.EveCommands;
import teacommontea.veritedoux.postprocess.EveStore;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.command.SauverCommands;

import teacommontea.metrics.bstats.bukkit.Metrics;

public final class VeritePlugin extends JavaPlugin
        implements CommandExecutor, TabCompleter {

    private static final String[] SIEVE_CONFIGS = {
            "filter/English/main_en.eve",
            "filter/Spanish/main_es.eve",
            "filter/French/main_fr.eve",
            "filter/Italian/main_it.eve",
            "filter/Portuguese/main_pt.eve",
            "filter/German/main_de.eve",
            "filter/English/profanity_en.eve",
            "filter/German/profanity_de.eve",
            "filter/Italian/profanity_it.eve",
            "filter/Spanish/profanity_es.eve",
            "filter/Spanish/profanity_es_AR.eve", "filter/Spanish/profanity_es_CL.eve",
            "filter/Spanish/profanity_es_CO.eve", "filter/Spanish/profanity_es_CU.eve",
            "filter/Spanish/profanity_es_DO.eve", "filter/Spanish/profanity_es_ES.eve",
            "filter/Spanish/profanity_es_MX.eve", "filter/Spanish/profanity_es_PA.eve",
            "filter/Spanish/profanity_es_PE.eve", "filter/Spanish/profanity_es_UY.eve",
            "filter/Spanish/profanity_es_VE.eve",
            "filter/French/profanity_fr_FR.eve", "filter/French/profanity_fr_CA.eve",
            "filter/Portuguese/profanity_pt_PT.eve", "filter/Portuguese/profanity_pt_BR.eve"};

    private boolean douxLoaded;
    private boolean sauverLoaded;
    private boolean vanishLoaded;

    private teacommontea.veritedoux.intercept.ChatIntercept chatIntercept;

    private teacommontea.veritevoiler.Vanish vanish;

    private Sauver sauver;
    private SauverCommands sauverCommands;

    private CaptchaManager captcha;

    private teacommontea.skript.VeriteSkript skript;

    private static final int BSTATS_PLUGIN_ID = 32815;
    private Metrics metrics;

    private static final String[] SAUVER_COMMANDS = {
            "ban", "tempban", "mute", "tempmute", "ipban", "ipmute", "kick",
            "warn", "unwarn", "warnings", "checkwarn", "warnlist",
            "unban", "unmute", "checkban", "checkmute",
            "dupeip", "iphistory", "namehistory", "lastuuid",
            "banlist", "mutelist", "history", "staffhistory", "staffrollback", "prunehistory",
            "lockdown", "geoip", "whois", "seen", "punish",
            "bc", "chatclear", "chatmute", "slowmode"};

    private final Messages messages = new Messages();

    private void msg(CommandSender to, String miniMessage) {
        to.spigot().sendMessage(messages.prefixed(miniMessage));
    }

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        teacommontea.util.sched.Sched.install(this);

        extractResource("config.yml", "config.yml");

        try {
            teacommontea.util.VeriteH2.open(databaseMode());
        } catch (Throwable t) {
            getLogger().severe("the H2 database could not be opened; disabling. "
                    + "Verite stores all data in H2 and will not run without it: " + t.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        teacommontea.util.SelfHeal.migrate(this);

        for (String legacy : new String[] {"config/sieve_settings.yml",
                "config/doux_settings.yml", "config/doux_settings_advanced.yml",
                "config/sauver_settings.yml"}) {
            File old = new File(getDataFolder(), legacy);
            if (old.isFile()) {
                old.delete();
            }
        }

        resolveChannel();

        if (!ensureFilter()) {
            getLogger().severe("filter boards unavailable; disabling. "
                    + "Verite will retry the download on next start.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!ensureNative()) {
            getLogger().severe("native library unavailable; disabling. "
                    + "Verite will retry the download on next start.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        teacommontea.veritedoux.util.Eve.configureNativeDir(new File(filterDir(), ".native"));

        if (!ensureTokenizers()) {
            getLogger().severe("tokenizer bundle unavailable; disabling. "
                    + "Verite will retry the download on next start.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        teacommontea.util.SelfHeal.validate(this);
        teacommontea.util.SelfHeal.healSettings(this);
        readComponentToggles();

        bringUpFilter();
        bringUpModeration();
        bringUpVanish();

        registerCommand("verite", this);

        teacommontea.util.FakeConnectCommand fakeConnect = new teacommontea.util.FakeConnectCommand(this);
        registerCommand("fakejoin", fakeConnect);
        registerCommand("fakeleave", fakeConnect);

        if (douxLoaded) {
            registerCommand("veriteflag", new teacommontea.util.FlagCommand(this, this::reloadAll));
        }

        if (sauverLoaded && sauverCommands != null) {
            for (String name : SAUVER_COMMANDS) {
                registerCommand(name, sauverCommands);
            }
            if (captcha != null) {
                registerCommand("captcha", new CaptchaCommand(captcha, messages));
            }
        }
        if (sauverLoaded && sauver != null) {
            teacommontea.veritesauver.markers.ShowMarkers showBarriers = new teacommontea.veritesauver.markers.ShowMarkers(
                    this, sauver.store(), org.bukkit.Material.BARRIER, "barriers", "showbarriers", "veritedoux.showbarriers");
            registerCommand("showbarriers", showBarriers);
            Bukkit.getPluginManager().registerEvents(showBarriers, this);
            showBarriers.resumeAll();

            teacommontea.veritesauver.markers.ShowMarkers showLight = new teacommontea.veritesauver.markers.ShowMarkers(
                    this, sauver.store(), org.bukkit.Material.LIGHT, "light blocks", "showlight", "veritedoux.showlight");
            registerCommand("showlight", showLight);
            Bukkit.getPluginManager().registerEvents(showLight, this);
            showLight.resumeAll();

            teacommontea.veritesauver.invsee.InvSeeFeature.enable(this);
            registerCommand("invsee", new teacommontea.veritesauver.invsee.InvSeeCommand(false));
            registerCommand("endersee", new teacommontea.veritesauver.invsee.InvSeeCommand(true));
        }
        if (vanishLoaded && vanish != null) {
            registerCommand("vanish", new teacommontea.veritevoiler.VanishCommand(vanish));
        }

        bringUpSkript();

        teacommontea.util.chat.ChatRouter.install(this);

        this.metrics = new Metrics(this, BSTATS_PLUGIN_ID);
    }

    private void bringUpSkript() {
        if (getServer().getPluginManager().getPlugin("Skript") == null) {
            return;
        }
        try {
            this.skript = teacommontea.skript.VeriteSkript.enable(this);
        } catch (Throwable t) {
            getLogger().warning("Skript integration failed to start (Skript syntax off): " + t.getMessage());
            this.skript = null;
        }
    }

    private void bringUpVanish() {
        try {
            this.vanish = teacommontea.veritevoiler.Vanish.enable(this);
            this.vanishLoaded = true;
        } catch (Exception e) {
            getLogger().warning("Veritevoiler failed to start (vanish off): " + e.getMessage());
            this.vanish = null;
            this.vanishLoaded = false;
        }
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        if (chatIntercept != null) {
            chatIntercept.shutdown();
            chatIntercept = null;
        }
        if (douxLoaded) {
            EveEntry.shutdown();
        }
        if (captcha != null) {
            captcha.shutdown();
            captcha = null;
        }
        if (sauver != null) {
            sauver.disable();
            sauver = null;
        }
        if (vanish != null) {
            vanish.disable();
            vanish = null;
        }
        teacommontea.util.VeriteH2 h2 = teacommontea.util.VeriteH2.active();
        if (h2 != null) {
            h2.close();
        }
    }

    private teacommontea.util.VeriteH2.Mode databaseMode() {
        File f = new File(getDataFolder(), "config.yml");
        String mode = "embedded";
        int port = 9092;
        String hostAddr = "127.0.0.1:9092";
        boolean autoServer = false;
        if (f.isFile()) {
            YamlConfiguration y = teacommontea.util.Yaml.loadYaml(f);
            mode = y.getString("database.mode", mode).trim().toLowerCase(java.util.Locale.ROOT);
            port = y.getInt("database.port", port);
            hostAddr = y.getString("database.host.address", hostAddr).trim();
            autoServer = y.getBoolean("database.auto.server", autoServer);
        }
        switch (mode) {
            case "host":
                return new teacommontea.util.VeriteH2.Mode(getDataFolder(), false, true, port, null, 0);
            case "remote": {
                String host = hostAddr;
                int rport = port;
                int colon = hostAddr.lastIndexOf(':');
                if (colon > 0) {
                    host = hostAddr.substring(0, colon);
                    try {
                        rport = Integer.parseInt(hostAddr.substring(colon + 1).trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                return new teacommontea.util.VeriteH2.Mode(getDataFolder(), false, false, 0, host, rport);
            }
            default:
                return new teacommontea.util.VeriteH2.Mode(getDataFolder(), autoServer, false,
                        autoServer ? port : 0, null, 0);
        }
    }

    private void readComponentToggles() {
        File f = new File(getDataFolder(), "config.yml");
        if (f.isFile()) {
            YamlConfiguration y = teacommontea.util.Yaml.loadYaml(f);
            Messages.setPrefix(y.getString("general.prefix", Messages.DEFAULT_PREFIX));
        }
    }

    public void reloadAll() {
        readComponentToggles();
        if (douxLoaded) {
            EveCommands.loadAll(this);
        }
        if (sauverLoaded && sauver != null) {
            sauver.reload();
        }
        if (vanishLoaded && vanish != null) {
            vanish.reload();
        }
    }

    private boolean gear(String path, boolean def) {
        File f = new File(getDataFolder(), "config.yml");
        if (!f.isFile()) return def;
        return teacommontea.util.Yaml.loadYaml(f).getBoolean(path, def);
    }

    private boolean filterGear()     { return gear("chat.filter.enabled", true); }
    private boolean moderationGear() { return gear("moderation.enabled", true); }
    private boolean vanishGear()     { return gear("vanish.enabled", true); }

    private static final String RELEASE_API_LIST =
            "https://api.github.com/repos/mstrjk/Verite_Public/releases?per_page=100";
    private static final String FILTER_ASSET = "filter.zip";
    private static final String NATIVE_ASSET = "default.native.zip";
    private static final String TOKENIZER_ASSET = "default.tokenizers.zip";
    private static final long CONFIG_DOWNLOAD_TIMEOUT_MS = 60_000;
    private static final long CONFIG_MAX_BYTES = 512L * 1024 * 1024;

    private teacommontea.util.ReleaseChannel channel;

    private File filterDir() {
        return new File(getDataFolder(), "filter");
    }

    private boolean ensureFilter() {
        File dir = filterDir();
        boolean present = dir.isDirectory() && new File(dir, "English").isDirectory();

        String latest = channelDigest(FILTER_ASSET);
        if (latest == null) {
            if (present) {
                getLogger().info("no supported release resolved; using the filter boards on disk.");
                return true;
            }
            getLogger().warning("filter boards missing and no supported release is available.");
            return false;
        }

        if (present && bundleSeen("filter", latest)) {
            return true;
        }

        File tmp = new File(getDataFolder(), "filter.tmp");
        try {
            getLogger().info(present
                    ? "a newer filter release is available; updating boards..."
                    : "filter boards not found; downloading...");
            downloadTo(channelUrl(FILTER_ASSET), tmp);
            if (present) {
                backupEveBoards();
            }
            unzipInto(tmp, getDataFolder());
            if (!new File(dir, "English").isDirectory()) {
                throw new java.io.IOException("filter bundle did not contain filter/English");
            }
            if (present) {
                teacommontea.util.SelfHeal.reconcileEve(this, SIEVE_CONFIGS);
            }
            recordBundle("filter", latest);
            getLogger().info("filter boards installed.");
            return true;
        } catch (Exception e) {
            getLogger().warning("filter download/extract failed: " + e.getMessage());
            return present;
        } finally {
            tmp.delete();
        }
    }

    private void backupEveBoards() {
        for (String config : SIEVE_CONFIGS) {
            File deployed = new File(getDataFolder(), config);
            if (!deployed.isFile()) {
                continue;
            }
            File backup = new File(getDataFolder(), config + ".bak");
            try {
                java.nio.file.Files.copy(deployed.toPath(), backup.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                getLogger().warning("could not back up " + config + " before update: " + e.getMessage());
            }
        }
    }

    private void resolveChannel() {
        try {
            String json = fetchText(RELEASE_API_LIST);
            String version = getDescription().getVersion();
            channel = teacommontea.util.ReleaseChannel.resolve(json, version, this::fetchText);
            if (channel != null) {
                getLogger().info("using release " + channel.tag()
                        + " (supported by build " + version + ").");
            } else {
                getLogger().warning("no published release supports build "
                        + version + "; using bundles on disk if present.");
            }
        } catch (Exception e) {
            channel = null;
            getLogger().warning("release listing failed: " + e.getMessage());
        }
    }

    private String channelDigest(String assetName) {
        return channel == null ? null : channel.digestOf(assetName);
    }

    private String channelUrl(String assetName) {
        return channel == null ? null : channel.urlOf(assetName);
    }

    private String fetchText(String url) throws Exception {
        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        c.setConnectTimeout((int) CONFIG_DOWNLOAD_TIMEOUT_MS);
        c.setReadTimeout((int) CONFIG_DOWNLOAD_TIMEOUT_MS);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "Verite/" + getDescription().getVersion());
        c.setRequestProperty("Accept", "application/vnd.github+json");
        int code = c.getResponseCode();
        if (code != 200) {
            throw new java.io.IOException("HTTP " + code + " from release API");
        }
        try (java.io.InputStream in = c.getInputStream()) {
            byte[] all = in.readAllBytes();
            return new String(all, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private boolean ensureNative() {
        File dir = filterDir();
        File nativeDir = new File(dir, ".native");
        boolean present = nativeDir.isDirectory();

        String latest = channelDigest(NATIVE_ASSET);
        if (latest == null) {
            if (present) {
                getLogger().info("no supported release resolved; using the native library on disk.");
                return true;
            }
            getLogger().warning("native library missing and no supported release is available.");
            return false;
        }

        if (present && bundleSeen("native", latest)) {
            return true;
        }
        if (nativeDir.exists()) {
            deleteRecursive(nativeDir);
        }
        File tmp = new File(getDataFolder(), "native.tmp");
        try {
            getLogger().info(present
                    ? "a newer native library is available; updating..."
                    : "native library not found; downloading...");
            downloadTo(channelUrl(NATIVE_ASSET), tmp);
            dir.mkdirs();
            unzipInto(tmp, dir);
            if (!nativeDir.isDirectory()) {
                throw new java.io.IOException("native bundle did not contain .native");
            }
            recordBundle("native", latest);
            getLogger().info("native library installed.");
            return true;
        } catch (Exception e) {
            getLogger().warning("native download/extract failed: " + e.getMessage());
            deleteRecursive(nativeDir);
            return false;
        } finally {
            tmp.delete();
        }
    }

    private boolean ensureTokenizers() {
        File tokDir = new File(filterDir(), ".tokenizers");
        File lex = new File(tokDir, "tokenizer.vlex9");
        boolean present = lex.isFile() && lexiconIsCurrent(lex);

        String latest = channelDigest(TOKENIZER_ASSET);
        if (latest == null) {
            if (present) {
                getLogger().info("no supported release resolved; using the tokenizers on disk.");
                return true;
            }
            getLogger().warning("tokenizers missing and no supported release is available.");
            return false;
        }

        if (present && bundleSeen("tokenizers", latest)) {
            return true;
        }
        if (tokDir.exists()) {
            getLogger().info("tokenizers are outdated or incomplete; refreshing...");
            deleteRecursive(tokDir);
        }
        File tmp = new File(getDataFolder(), "tokenizers.tmp");
        try {
            getLogger().info(present
                    ? "a newer tokenizer bundle is available; updating..."
                    : "tokenizers not found; downloading...");
            downloadTo(channelUrl(TOKENIZER_ASSET), tmp);
            filterDir().mkdirs();
            unzipInto(tmp, filterDir());
            if (!lex.isFile()) {
                throw new java.io.IOException("bundle did not contain .tokenizers/tokenizer.vlex9");
            }
            recordBundle("tokenizers", latest);
            getLogger().info("tokenizers installed.");
            return true;
        } catch (Exception e) {
            getLogger().warning("tokenizer download/extract failed: " + e.getMessage());
            deleteRecursive(tokDir);
            return false;
        } finally {
            tmp.delete();
        }
    }

    private boolean lexiconIsCurrent(File lex) {
        try (java.io.InputStream raw = new java.io.FileInputStream(lex)) {
            byte[] head = new byte[8];
            int off = 0;
            while (off < head.length) {
                int n = raw.read(head, off, head.length - off);
                if (n < 0) break;
                off += n;
            }
            byte[] magic = {'V', 'L', 'E', 'X', '9', 'J', '1', 0};
            return off == 8 && java.util.Arrays.equals(head, magic);
        } catch (Exception e) {
            return false;
        }
    }

    private void extractResource(String resource, String targetName) {
        File out = new File(getDataFolder(), targetName);
        if (out.exists()) return;
        File parent = out.getParentFile();
        if (parent != null) parent.mkdirs();
        try (java.io.InputStream in = getResource(resource)) {
            if (in != null) java.nio.file.Files.copy(in, out.toPath());
        } catch (Exception e) {
            getLogger().warning("could not extract " + targetName + ": " + e.getMessage());
        }
    }

    private void downloadTo(String url, File dest) throws Exception {
        java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        c.setConnectTimeout((int) CONFIG_DOWNLOAD_TIMEOUT_MS);
        c.setReadTimeout((int) CONFIG_DOWNLOAD_TIMEOUT_MS);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "Verite/" + getDescription().getVersion());
        int code = c.getResponseCode();
        if (code != 200) throw new java.io.IOException("HTTP " + code + " from bundle host");
        long total = 0;
        try (java.io.InputStream in = c.getInputStream();
             java.io.OutputStream out = new java.io.FileOutputStream(dest)) {
            byte[] buf = new byte[1 << 16];
            int n;
            while ((n = in.read(buf)) > 0) {
                total += n;
                if (total > CONFIG_MAX_BYTES) throw new java.io.IOException("bundle exceeds size cap");
                out.write(buf, 0, n);
            }
        }
    }

    private boolean bundleSeen(String bundle, String token) {
        if (token == null) {
            return false;
        }
        teacommontea.util.VeriteH2 h2 = teacommontea.util.VeriteH2.active();
        if (h2 == null) {
            return false;
        }
        byte[] stored = h2.read("bundle:" + bundle);
        if (stored == null) {
            return false;
        }
        String got = new String(stored, java.nio.charset.StandardCharsets.UTF_8).trim();
        return got.equalsIgnoreCase(token);
    }

    private void recordBundle(String bundle, String token) {
        teacommontea.util.VeriteH2 h2 = teacommontea.util.VeriteH2.active();
        if (h2 == null || token == null) {
            return;
        }
        h2.write("bundle:" + bundle, token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private void unzipInto(File zip, File targetDir) throws Exception {
        String root = targetDir.getCanonicalPath() + File.separator;
        long total = 0;
        try (java.util.zip.ZipInputStream zin =
                     new java.util.zip.ZipInputStream(new java.io.FileInputStream(zip))) {
            java.util.zip.ZipEntry e;
            byte[] buf = new byte[1 << 16];
            while ((e = zin.getNextEntry()) != null) {
                File out = new File(targetDir, e.getName());
                if (!out.getCanonicalPath().startsWith(root)) {
                    throw new java.io.IOException("zip entry escapes target: " + e.getName());
                }
                if (e.isDirectory()) {
                    out.mkdirs();
                } else {
                    File parent = out.getParentFile();
                    if (parent != null) parent.mkdirs();
                    try (java.io.OutputStream fo = new java.io.FileOutputStream(out)) {
                        int n;
                        while ((n = zin.read(buf)) > 0) {
                            total += n;
                            if (total > CONFIG_MAX_BYTES) throw new java.io.IOException("bundle inflation exceeds cap");
                            fo.write(buf, 0, n);
                        }
                    }
                }
                zin.closeEntry();
            }
        }
    }

    private void deleteRecursive(File f) {
        if (f == null || !f.exists()) return;
        File[] kids = f.listFiles();
        if (kids != null) for (File k : kids) deleteRecursive(k);
        f.delete();
    }

    private static final long FILTER_LOAD_BUDGET_MS = 30_000;

    private void bringUpFilter() {
        if (!loadFilterBounded()) {

            douxLoaded = false;
            return;
        }

        try {
            EveEntry.enableStore(EveStore.open(this));
        } catch (Exception e) {
            getLogger().warning("EVE store off (count() will return 0): " + e.getMessage());
        }

        new ChatEventGuard().register();

        douxLoaded = true;
    }

    private boolean loadFilterBounded() {
        final Throwable[] failure = new Throwable[1];
        Thread worker = new Thread(() -> {
            try {

                EveCommands.loadAll(this);
            } catch (Throwable t) {
                failure[0] = t;
            }
        }, "Verite-FilterLoad");
        worker.setDaemon(true);
        worker.start();
        try {
            worker.join(FILTER_LOAD_BUDGET_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            getLogger().severe("filter load interrupted during boot:");
            ie.printStackTrace();
            return false;
        }

        if (worker.isAlive()) {

            StringBuilder where = new StringBuilder("chat-filter load exceeded "
                    + FILTER_LOAD_BUDGET_MS + "ms budget and STALLED. Worker stack at timeout:\n");
            for (StackTraceElement el : worker.getStackTrace()) {
                where.append("    at ").append(el).append('\n');
            }
            getLogger().severe(where.toString());
            worker.interrupt();
            return false;
        }
        if (failure[0] != null) {
            getLogger().severe("chat-filter load FAILED with an exception:");
            failure[0].printStackTrace();
            return false;
        }
        return true;
    }

    private static final class ChatEventGuard {
        private final ThreadLocal<Boolean> accepted = ThreadLocal.withInitial(() -> Boolean.FALSE);

        void register() {
            teacommontea.util.chat.ChatRouter.register(
                    org.bukkit.event.EventPriority.LOWEST, true, event -> {
                accepted.set(Boolean.FALSE);
                String text = event.message();
                if (text == null || text.isEmpty()) {
                    return;
                }
                java.util.UUID id = event.sender().getUniqueId();
                EveEntry.Result r = EveEntry.check(id, text);
                if (r == EveEntry.Result.CLEAN) {
                    accepted.set(Boolean.TRUE);
                    return;
                }
                event.setCancelled(true);
                event.sender().spigot().sendMessage(EveEntry.blockNotice(r, text));
            });
            teacommontea.util.chat.ChatRouter.register(
                    org.bukkit.event.EventPriority.MONITOR, false, event -> {
                if (!accepted.get() || event.isCancelled()) {
                    accepted.set(Boolean.FALSE);
                    return;
                }
                accepted.set(Boolean.FALSE);
                String text = event.message();
                if (text == null || text.isEmpty()) {
                    return;
                }
                EveEntry.recordSend(event.sender().getUniqueId(), text);
            });
        }
    }

    private void bringUpModeration() {
        try {
            this.sauver = Sauver.enable(this);
            this.sauverCommands = new SauverCommands(sauver);

            this.captcha = new CaptchaManager(this, messages);
            getServer().getPluginManager().registerEvents(captcha.standardListener(), this);
            getServer().getPluginManager().registerEvents(captcha.detailedListener(), this);
            this.sauverLoaded = true;
        } catch (Exception e) {
            getLogger().warning("Veritesauver failed to start (moderation off): " + e.getMessage());
            this.sauverLoaded = false;
        }
    }

    private void registerCommand(String name, Object handler) {
        org.bukkit.command.PluginCommand pc = getCommand(name);
        if (pc != null) {
            pc.setExecutor((CommandExecutor) handler);
            pc.setTabCompleter((TabCompleter) handler);
            return;
        }

        getLogger().warning("/" + name + " was not in the command map; registering dynamically.");
        try {
            java.lang.reflect.Constructor<org.bukkit.command.PluginCommand> ctor =
                    org.bukkit.command.PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            ctor.setAccessible(true);
            org.bukkit.command.PluginCommand cmd = ctor.newInstance(name, this);
            cmd.setExecutor((CommandExecutor) handler);
            cmd.setTabCompleter((TabCompleter) handler);

            java.lang.reflect.Method getMap = Bukkit.getServer().getClass().getMethod("getCommandMap");
            org.bukkit.command.CommandMap map = (org.bukkit.command.CommandMap) getMap.invoke(Bukkit.getServer());
            map.register(getName().toLowerCase(java.util.Locale.ROOT), cmd);
        } catch (Exception e) {
            getLogger().warning("Dynamic registration of /" + name + " failed: " + e);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            String on = Colours.BRAND + "on", off = Colours.WARNING + "off";
            msg(sender, Colours.BRAND_ACCENT_SECONDARY + "Veritédoux " + Colours.BRAND_ACCENT_SECONDARY + "(chat filter)" + Colours.BRAND_ACCENT_SECONDARY + ": " + (filterGear() ? on : off));
            msg(sender, Colours.BRAND_ACCENT_SECONDARY + "Veritésauver " + Colours.BRAND_ACCENT_SECONDARY + "(moderation)" + Colours.BRAND_ACCENT_SECONDARY + ": " + (moderationGear() ? on : off));
            msg(sender, Colours.BRAND_ACCENT_SECONDARY + "Veritévoiler " + Colours.BRAND_ACCENT_SECONDARY + "(vanish)" + Colours.BRAND_ACCENT_SECONDARY + ": " + (vanishGear() ? on : off));
            return true;
        }
        if (!sender.hasPermission("verite.admin")) {
            msg(sender, Colours.WARNING + "You don't have permission to do that.");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                reloadAll();
                msg(sender, Colours.BRAND_ACCENT_SECONDARY + "Reloaded " + Colours.BRAND + "Verité" + Colours.BRAND_ACCENT_SECONDARY + ": chat filter, moderation, vanish, and config.");
            }
            case "count" -> {
                if (!douxLoaded) { msg(sender, Colours.WARNING + "The chat filter is unavailable."); return true; }
                if (args.length < 2) { msg(sender, Colours.BRAND_ACCENT_SECONDARY + "Usage" + Colours.BRAND_ACCENT_SECONDARY + ": " + Colours.BRAND_ACCENT_SECONDARY + "/verite count " + Colours.BRAND_ACCENT_SECONDARY + "<" + Colours.BRAND_ACCENT_SECONDARY + "player" + Colours.BRAND_ACCENT_SECONDARY + ">"); return true; }
                org.bukkit.OfflinePlayer t = org.bukkit.Bukkit.getOfflinePlayer(args[1]);
                msg(sender, Colours.BRAND_ACCENT_SECONDARY + args[1] + " " + Colours.BRAND_ACCENT_SECONDARY + "has " + Colours.WARNING
                        + teacommontea.veritedoux.EveEntry.count(t.getUniqueId()) + " " + Colours.BRAND_ACCENT_SECONDARY + "flags.");
            }
            default -> msg(sender, Colours.BRAND_ACCENT_SECONDARY + "/verite " + Colours.BRAND_ACCENT_SECONDARY + "status " + Colours.BRAND_ACCENT_SECONDARY + "| " + Colours.BRAND_ACCENT_SECONDARY + "reload " + Colours.BRAND_ACCENT_SECONDARY + "| " + Colours.BRAND_ACCENT_SECONDARY + "count " + Colours.BRAND_ACCENT_SECONDARY + "<" + Colours.BRAND_ACCENT_SECONDARY + "player" + Colours.BRAND_ACCENT_SECONDARY + ">");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();
        if (cmd.equals("verite")) {
            if (args.length == 1) {
                return teacommontea.util.Complete.prefix(List.of("status", "reload", "count"), args[0]);
            } else if (args.length == 2 && args[0].equalsIgnoreCase("count")) {
                return teacommontea.util.Complete.onlineNames(args[1]);
            }
            return new ArrayList<>();
        }
        return List.of();
    }
}
