package teacommontea.veriteproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public final class ProxyConfig {

    private final File dataFolder;
    private final String mode;
    private final int port;
    private final String remoteHost;
    private final int remotePort;
    private final boolean autoServer;
    private final boolean banAlts;
    private final boolean enforceMuteCommands;

    private ProxyConfig(File dataFolder, String mode, int port, String remoteHost, int remotePort,
                        boolean autoServer, boolean banAlts, boolean enforceMuteCommands) {
        this.dataFolder = dataFolder;
        this.mode = mode;
        this.port = port;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.autoServer = autoServer;
        this.banAlts = banAlts;
        this.enforceMuteCommands = enforceMuteCommands;
    }

    public static ProxyConfig load(File dataFolder) {
        dataFolder.mkdirs();
        File file = new File(dataFolder, "verite-proxy.properties");
        Properties p = new Properties();
        if (file.isFile()) {
            try (InputStream in = new FileInputStream(file)) {
                p.load(in);
            } catch (Exception ignored) {
            }
        } else {
            p.setProperty("database.mode", "remote");
            p.setProperty("database.port", "9092");
            p.setProperty("database.host.address", "127.0.0.1:9092");
            p.setProperty("database.auto.server", "false");
            p.setProperty("moderation.ban.alts", "false");
            p.setProperty("moderation.enforce.mute.commands", "true");
            try (OutputStream out = new FileOutputStream(file)) {
                p.store(out, "Verite proxy: read-only shared punishment enforcement.");
            } catch (Exception ignored) {
            }
        }

        String mode = p.getProperty("database.mode", "remote").trim().toLowerCase(java.util.Locale.ROOT);
        int port = parseInt(p.getProperty("database.port", "9092"), 9092);
        String hostAddr = p.getProperty("database.host.address", "127.0.0.1:9092").trim();
        boolean autoServer = Boolean.parseBoolean(p.getProperty("database.auto.server", "false").trim());
        boolean banAlts = Boolean.parseBoolean(p.getProperty("moderation.ban.alts", "false").trim());
        boolean enforceMute = Boolean.parseBoolean(
                p.getProperty("moderation.enforce.mute.commands", "true").trim());

        String host = hostAddr;
        int rport = port;
        int colon = hostAddr.lastIndexOf(':');
        if (colon > 0) {
            host = hostAddr.substring(0, colon);
            rport = parseInt(hostAddr.substring(colon + 1).trim(), port);
        }

        return new ProxyConfig(dataFolder, mode, port, host, rport, autoServer, banAlts, enforceMute);
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public File dataFolder() {
        return dataFolder;
    }

    public String mode() {
        return mode;
    }

    public int port() {
        return port;
    }

    public String remoteHost() {
        return remoteHost;
    }

    public int remotePort() {
        return remotePort;
    }

    public boolean autoServer() {
        return autoServer;
    }

    public boolean banAlts() {
        return banAlts;
    }

    public boolean enforceMuteCommands() {
        return enforceMuteCommands;
    }

    public String forwardedIp(String hostname, String fallback) {
        if (hostname != null && hostname.contains("///")) {
            String[] parts = hostname.split("///");
            if (parts.length >= 2) {
                String candidate = parts[1].trim();
                int c = candidate.lastIndexOf(':');
                if (c > 0 && candidate.indexOf(':') == c) {
                    candidate = candidate.substring(0, c);
                }
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
        }
        return fallback;
    }
}
