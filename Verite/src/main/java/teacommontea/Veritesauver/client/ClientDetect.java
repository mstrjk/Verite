package teacommontea.veritesauver.client;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class ClientDetect implements org.bukkit.event.Listener {

    private static final long BRAND_POLL_TICKS = 20L;
    private static final int BRAND_POLL_MAX_ATTEMPTS = 15;

    private final SignProbe signProbe;
    private final CookieProbe cookieProbe;
    private final ClientInfoProbe clientInfoProbe;

    private final Map<UUID, ClientProfile> profiles = new ConcurrentHashMap<>();

    private ClientDetect(SignProbe signProbe, CookieProbe cookieProbe,
                         ClientInfoProbe clientInfoProbe) {
        this.signProbe = signProbe;
        this.cookieProbe = cookieProbe;
        this.clientInfoProbe = clientInfoProbe;
    }

    public static ClientDetect install(Plugin plugin) {
        if (!ClientConfig.enabled()) {
            return null;
        }
        SignProbe sign = null;
        if (ClientConfig.signProbe()) {
            try {
                sign = new SignProbe(ProbeNms.resolve());
            } catch (ProbeNms.Unsupported u) {
                plugin.getLogger().warning(teacommontea.util.ConsoleColours.bad(
                        teacommontea.util.Lang.of("client.probe.signs.unavailable")) + u.getMessage());
            }
        }
        CookieProbe cookies = ClientConfig.cookieProbe() ? new CookieProbe(plugin) : null;
        ClientDetect detect = new ClientDetect(sign, cookies, ClientInfoProbe.resolve());
        org.bukkit.Bukkit.getPluginManager().registerEvents(detect, plugin);
        return detect;
    }

    public ClientProfile profile(UUID player) {
        return profiles.get(player);
    }

    public void forget(UUID player) {
        profiles.remove(player);
    }

    public void shutdown() {
        profiles.clear();
    }

    public void inspect(Player p, Consumer<ClientProfile> onDone) {
        ClientProfile profile = new ClientProfile(p.getUniqueId());
        profiles.put(p.getUniqueId(), profile);

        pollBrand(p, profile, 1, () -> runProbes(p, profile, onDone));
    }

    private void pollBrand(Player p, ClientProfile profile, int attempt, Runnable next) {
        teacommontea.util.sched.Sched.executeFor(p, () -> {
            if (!p.isOnline()) {
                return;
            }
            String brand = brandOf(p);
            boolean known = brand != null && !brand.isBlank();
            if (!known && attempt < BRAND_POLL_MAX_ATTEMPTS) {
                pollBrand(p, profile, attempt + 1, next);
                return;
            }
            String resolved = known ? brand : "vanilla";
            profile.reportedBrand(resolved);
            if (known && !resolved.equalsIgnoreCase("vanilla")) {
                String owner = ClientSignatures.brandOwner(resolved);
                if (owner != null) {
                    profile.add(Signal.of(Signal.Source.BRAND, owner, resolved, 30));
                }
            }
            next.run();
        }, BRAND_POLL_TICKS);
    }

    private void runProbes(Player p, ClientProfile profile, Consumer<ClientProfile> onDone) {
        profile.addAll(ChannelProbe.read(p));

        if (clientInfoProbe != null) {
            Map<String, String> info = clientInfoProbe.read(p);
            profile.addAll(ClientInfoProbe.evaluate(p, info));
            String print = ClientInfoProbe.fingerprint(info);
            if (print != null) {
                profile.fact("clientinfo", print);
            }
        }

        boolean signsRan = signProbe != null;
        boolean cookiesRan = cookieProbe != null;

        AtomicInteger outstanding = new AtomicInteger(1);
        Runnable settle = () -> {
            if (outstanding.decrementAndGet() == 0) {
                profile.addAll(Normalisation.evaluate(profile, signsRan, cookiesRan));
                ClientVerdict.decide(profile);
                onDone.accept(profile);
            }
        };

        if (cookieProbe != null) {
            outstanding.incrementAndGet();
            cookieProbe.run(p, found -> {
                profile.addAll(found);
                cookieProbe.clear(p);
                settle.run();
            });
        }
        if (signProbe != null) {
            outstanding.incrementAndGet();
            teacommontea.util.sched.Sched.executeFor(p, () -> signProbe.start(p, found -> {
                profile.addAll(found);
                settle.run();
            }), ClientConfig.probeDelayTicks());
        }
        settle.run();
    }

    public void cancel(Player p) {
        if (signProbe != null) {
            signProbe.cancel(p.getUniqueId());
        }
    }

    public static String brandOf(Player p) {
        try {
            Object v = Player.class.getMethod("getClientBrandName").invoke(p);
            return v == null ? null : String.valueOf(v);
        } catch (Throwable t) {
            return null;
        }
    }

    public List<Signal> signalsOf(UUID player) {
        ClientProfile profile = profiles.get(player);
        return profile == null ? List.of() : profile.signals();
    }
}
