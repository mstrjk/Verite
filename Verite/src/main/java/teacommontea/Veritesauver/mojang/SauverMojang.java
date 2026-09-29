package teacommontea.veritesauver.mojang;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SauverMojang {

    public enum Status { FOUND, NOT_FOUND, UNKNOWN, RATE_LIMITED }

    public record Profile(UUID uuid, String name, Status status) {
        static Profile found(UUID u, String n) { return new Profile(u, n, Status.FOUND); }
        static final Profile NOT_FOUND = new Profile(null, null, Status.NOT_FOUND);
        static final Profile UNKNOWN   = new Profile(null, null, Status.UNKNOWN);
        static final Profile RATE_LIMITED = new Profile(null, null, Status.RATE_LIMITED);
    }

    private static final String ENDPOINT = "https://api.mojang.com/users/profiles/minecraft/";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private static final int MAX_CACHE = 2048;
    private static final long NEGATIVE_TTL_MS = 10L * 60L * 1000L;
    private static final long BACKOFF_MS = 60L * 1000L;

    private record Cached(Profile profile, long expires) {}

    private static final ConcurrentHashMap<String, Cached> CACHE = new ConcurrentHashMap<>();
    private static volatile long blockedUntil;

    private SauverMojang() {}

    public static Profile lookup(String name) {
        if (name == null || name.isBlank()) {
            return Profile.NOT_FOUND;
        }
        if (!valid(name)) {
            return Profile.NOT_FOUND;
        }
        String key = name.toLowerCase(Locale.ROOT);
        long now = System.currentTimeMillis();

        Cached cached = CACHE.get(key);
        if (cached != null) {
            if (cached.expires() == 0L || cached.expires() > now) {
                return cached.profile();
            }
            CACHE.remove(key);
        }
        if (now < blockedUntil) {
            return Profile.RATE_LIMITED;
        }

        Profile result = query(name);
        if (result.status() == Status.RATE_LIMITED) {
            blockedUntil = now + BACKOFF_MS;
            return result;
        }
        if (result.status() == Status.FOUND) {
            store(key, new Cached(result, 0L));
        } else if (result.status() == Status.NOT_FOUND) {
            store(key, new Cached(result, now + NEGATIVE_TTL_MS));
        }
        return result;
    }

    private static void store(String key, Cached value) {
        if (CACHE.size() >= MAX_CACHE) {
            CACHE.clear();
        }
        CACHE.put(key, value);
    }

    static boolean valid(String name) {
        if (name.length() < 3 || name.length() > 16) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '_';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private static Profile query(String name) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT + java.net.URLEncoder.encode(name,
                            java.nio.charset.StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();
            HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code == 404 || code == 204) {
                return Profile.NOT_FOUND;
            }
            if (code == 429) {
                return Profile.RATE_LIMITED;
            }
            if (code != 200) {
                return Profile.UNKNOWN;
            }
            String body = resp.body();
            String id = extract(body, "id");
            String canonical = extract(body, "name");
            if (id == null) {
                return Profile.UNKNOWN;
            }
            return Profile.found(dashed(id), canonical == null ? name : canonical);
        } catch (Throwable t) {
            return Profile.UNKNOWN;
        }
    }

    private static String extract(String json, String field) {
        if (json == null) {
            return null;
        }
        String needle = "\"" + field + "\"";
        int k = json.indexOf(needle);
        if (k < 0) {
            return null;
        }
        int colon = json.indexOf(':', k + needle.length());
        if (colon < 0) {
            return null;
        }
        int open = json.indexOf('"', colon + 1);
        if (open < 0) {
            return null;
        }
        int close = json.indexOf('"', open + 1);
        if (close < 0) {
            return null;
        }
        return json.substring(open + 1, close);
    }

    private static UUID dashed(String undashed) {
        if (undashed.length() != 32) {
            return UUID.fromString(undashed);
        }
        String d = undashed.substring(0, 8) + "-" + undashed.substring(8, 12) + "-"
                + undashed.substring(12, 16) + "-" + undashed.substring(16, 20) + "-"
                + undashed.substring(20);
        return UUID.fromString(d);
    }
}
