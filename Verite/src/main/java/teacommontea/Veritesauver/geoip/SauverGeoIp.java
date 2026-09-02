package teacommontea.veritesauver.geoip;

import java.io.File;
import java.net.InetAddress;
import teacommontea.veritesauver.Sauver;

public final class SauverGeoIp {

    private static volatile Object reader;
    private static volatile boolean attempted;

    private SauverGeoIp() {}

    private static final String MIRROR_URL =
        "https://raw.githubusercontent.com/P3TERX/GeoLite.mmdb/download/GeoLite2-Country.mmdb";

    public static void load(File dataFolder) {
        attempted = true;
        reader = null;
        File db = new File(dataFolder, "GeoLite2-Country.mmdb");
        if (db.exists()) {
            bind(db);
            return;
        }
        Sauver s = Sauver.instance();
        if (s == null || s.plugin() == null) {
            return;
        }

        teacommontea.util.sched.Sched.executeAsync(() -> {
            if (download(db)) {
                bind(db);
                Sauver cur = Sauver.instance();
                if (cur != null && cur.plugin() != null) {
                    cur.plugin().getLogger().info("GeoLite2 country database downloaded; geoip is now active.");
                }
            }
        });
    }

    private static boolean download(File db) {
        try {
            File parent = db.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            java.net.URL url = java.net.URI.create(MIRROR_URL).toURL();
            java.net.URLConnection conn = url.openConnection();
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(60_000);
            File tmp = new File(db.getAbsolutePath() + ".part");
            try (java.io.InputStream in = conn.getInputStream()) {
                java.nio.file.Files.copy(in, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            java.nio.file.Files.move(tmp.toPath(), db.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Throwable t) {
            Sauver s = Sauver.instance();
            if (s != null && s.plugin() != null) {
                s.plugin().getLogger().warning("Could not download GeoLite2 database (geoip stays off): " + t.getMessage());
            }
            return false;
        }
    }

    private static void bind(File db) {
        try {
            Class<?> readerClass = Class.forName("com.maxmind.db.Reader");
            reader = readerClass.getConstructor(File.class).newInstance(db);
        } catch (Throwable t) {
            reader = null;
        }
    }

    public static String country(String ip) {
        Object r = reader;
        if (r == null) {
            return null;
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            Object record = r.getClass()
                    .getMethod("get", InetAddress.class, Class.class)
                    .invoke(r, addr, java.util.Map.class);
            if (!(record instanceof java.util.Map<?, ?> top)) {
                return null;
            }
            Object countryObj = top.get("country");
            if (!(countryObj instanceof java.util.Map<?, ?> countryMap)) {
                return null;
            }
            Object namesObj = countryMap.get("names");
            if (namesObj instanceof java.util.Map<?, ?> names) {
                Object en = names.get("en");
                if (en != null) {
                    return en.toString();
                }
            }
            Object iso = countryMap.get("iso_code");
            return iso == null ? null : iso.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean available() {
        return reader != null;
    }

    public static boolean isPrivate(String ip) {
        if (ip == null || ip.isBlank()) {
            return true;
        }
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isLoopbackAddress() || addr.isAnyLocalAddress()
                    || addr.isLinkLocalAddress() || addr.isSiteLocalAddress();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean attempted() {
        return attempted;
    }
}
