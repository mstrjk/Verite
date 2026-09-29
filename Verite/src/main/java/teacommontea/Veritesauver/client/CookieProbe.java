package teacommontea.veritesauver.client;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

final class CookieProbe {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TIMEOUT_MS = 4000L;

    private final NamespacedKey key;

    CookieProbe(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "probe");
    }

    void run(Player p, Consumer<List<Signal>> onDone) {
        byte[] nonce = new byte[16];
        RANDOM.nextBytes(nonce);
        String expected = encode(nonce);

        try {
            p.storeCookie(key, nonce);
        } catch (Throwable t) {
            onDone.accept(List.of(Signal.of(Signal.Source.COOKIE, "Cookie storage refused", "store failed", 45)));
            return;
        }

        teacommontea.util.sched.Sched.executeAsync(() -> verify(p, expected, onDone), 500L);
    }

    private void verify(Player p, String expected, Consumer<List<Signal>> onDone) {
        if (!p.isOnline()) {
            onDone.accept(List.of());
            return;
        }
        CompletableFuture<byte[]> future;
        try {
            future = p.retrieveCookie(key);
        } catch (Throwable t) {
            onDone.accept(List.of(Signal.of(Signal.Source.COOKIE, "Cookie retrieval refused", "request failed", 45)));
            return;
        }
        future.orTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).whenComplete((value, error) -> {
            if (error != null) {
                onDone.accept(List.of(Signal.of(Signal.Source.COOKIE, "Cookie ignored", "no response", 50)));
                return;
            }
            if (value == null || value.length == 0) {
                onDone.accept(List.of(Signal.of(Signal.Source.COOKIE, "Cookie discarded", "empty", 50)));
                return;
            }
            String got = encode(value);
            if (!got.equals(expected)) {
                onDone.accept(List.of(Signal.of(Signal.Source.COOKIE, "Cookie mutated", "altered", 55)));
                return;
            }
            onDone.accept(List.of());
        });
    }

    void clear(Player p) {
        try {
            p.storeCookie(key, new byte[0]);
        } catch (Throwable ignored) {
        }
    }

    private static String encode(byte[] raw) {
        return new String(java.util.Base64.getEncoder().encode(raw), StandardCharsets.US_ASCII);
    }
}
