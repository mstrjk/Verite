package teacommontea.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public final class ReleaseChannel {

    public static final class Asset {
        private final String digest;
        private final String url;

        public Asset(String digest, String url) {
            this.digest = digest;
            this.url = url;
        }

        public String digest() {
            return digest;
        }

        public String url() {
            return url;
        }
    }

    private final String tag;
    private final Map<String, Asset> assets;

    private ReleaseChannel(String tag, Map<String, Asset> assets) {
        this.tag = tag;
        this.assets = assets;
    }

    public String tag() {
        return tag;
    }

    public Asset asset(String name) {
        return assets.get(name);
    }

    public String digestOf(String name) {
        Asset a = assets.get(name);
        return a == null ? null : a.digest();
    }

    public String urlOf(String name) {
        Asset a = assets.get(name);
        return a == null ? null : a.url();
    }

    public static int compareVersions(String a, String b) {
        String[] pa = a == null ? new String[0] : a.split("\\.");
        String[] pb = b == null ? new String[0] : b.split("\\.");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            int va = i < pa.length ? segment(pa[i]) : 0;
            int vb = i < pb.length ? segment(pb[i]) : 0;
            if (va != vb) {
                return va < vb ? -1 : 1;
            }
        }
        return 0;
    }

    private static int segment(String s) {
        int v = 0;
        boolean any = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                v = v * 10 + (c - '0');
                any = true;
            } else if (any) {
                break;
            }
        }
        return v;
    }

    public static String minimumSupportVersion(String propertiesBody) {
        if (propertiesBody == null) {
            return null;
        }
        for (String raw : propertiesBody.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            if (key.equals("minimum-support-version")) {
                return line.substring(eq + 1).trim();
            }
        }
        return null;
    }

    static List<String> sliceReleases(String json) {
        List<String> out = new ArrayList<>();
        if (json == null) {
            return out;
        }
        int depth = 0;
        int start = -1;
        boolean inStr = false;
        boolean esc = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inStr) {
                if (esc) {
                    esc = false;
                } else if (c == '\\') {
                    esc = true;
                } else if (c == '"') {
                    inStr = false;
                }
                continue;
            }
            if (c == '"') {
                inStr = true;
            } else if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    out.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return out;
    }

    private static int afterColon(String json, String key) {
        String needle = "\"" + key + "\"";
        int from = 0;
        while (true) {
            int at = json.indexOf(needle, from);
            if (at < 0) {
                return -1;
            }
            int i = at + needle.length();
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                i++;
            }
            if (i < json.length() && json.charAt(i) == ':') {
                i++;
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }
                return i;
            }
            from = at + needle.length();
        }
    }

    private static String stringField(String json, String key) {
        int i = afterColon(json, key);
        if (i < 0 || i >= json.length() || json.charAt(i) != '"') {
            return null;
        }
        int start = i + 1;
        int end = start;
        boolean esc = false;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (esc) {
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else if (c == '"') {
                break;
            }
            end++;
        }
        return json.substring(start, end);
    }

    static Map<String, Asset> parseAssets(String releaseJson) {
        Map<String, Asset> out = new LinkedHashMap<>();
        int open = afterColon(releaseJson, "assets");
        if (open < 0 || open >= releaseJson.length() || releaseJson.charAt(open) != '[') {
            return out;
        }
        int close = matchArray(releaseJson, open);
        if (close < 0) {
            return out;
        }
        String array = releaseJson.substring(open + 1, close);
        for (String obj : sliceReleases(array)) {
            String name = stringField(obj, "name");
            if (name == null || name.isEmpty()) {
                continue;
            }
            String digest = stringField(obj, "digest");
            if (digest != null) {
                int colon = digest.indexOf(':');
                if (colon >= 0) {
                    digest = digest.substring(colon + 1);
                }
            }
            String url = stringField(obj, "browser_download_url");
            out.put(name, new Asset(digest, url));
        }
        return out;
    }

    private static int matchArray(String json, int openBracket) {
        int depth = 0;
        boolean inStr = false;
        boolean esc = false;
        for (int i = openBracket; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inStr) {
                if (esc) {
                    esc = false;
                } else if (c == '\\') {
                    esc = true;
                } else if (c == '"') {
                    inStr = false;
                }
                continue;
            }
            if (c == '"') {
                inStr = true;
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static ReleaseChannel resolve(String releasesJson, String buildVersion, TextReader reader) {
        for (String release : sliceReleases(releasesJson)) {
            Map<String, Asset> assets = parseAssets(release);
            Asset props = assets.get("default.properties");
            if (props == null || props.url() == null) {
                continue;
            }
            String body;
            try {
                body = reader.read(props.url());
            } catch (Exception e) {
                continue;
            }
            String min = minimumSupportVersion(body);
            if (min == null) {
                continue;
            }
            if (compareVersions(min, buildVersion) <= 0) {
                String tag = stringField(release, "tag_name");
                return new ReleaseChannel(tag, assets);
            }
        }
        return null;
    }

    public interface TextReader {
        String read(String url) throws Exception;
    }
}
