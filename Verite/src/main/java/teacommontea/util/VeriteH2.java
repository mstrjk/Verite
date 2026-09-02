package teacommontea.util;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;


public final class VeriteH2 {

    private static volatile VeriteH2 active;

    private final Connection conn;
    private final boolean readOnly;
    private final Object tcpServer;

    private VeriteH2(Connection conn, boolean readOnly, Object tcpServer) {
        this.conn = conn;
        this.readOnly = readOnly;
        this.tcpServer = tcpServer;
    }

    public static synchronized VeriteH2 open(File dataFolder) throws Exception {
        return open(new Mode(dataFolder, false, false, 0, null, 0));
    }

    public static synchronized VeriteH2 open(Mode mode) throws Exception {
        if (active != null) {
            return active;
        }
        Driver driver = (Driver) Class.forName("org.h2.Driver").getDeclaredConstructor().newInstance();

        Object tcpServer = null;
        String url;
        Properties props = new Properties();
        props.setProperty("user", "verite");
        props.setProperty("password", "");

        if (mode.remoteHost != null) {
            url = "jdbc:h2:tcp://" + mode.remoteHost + ":" + mode.remotePort + "/"
                    + localPath(mode.dataFolder)
                    + ";MODE=REGULAR;IFEXISTS=TRUE";
            if (mode.readOnly) {
                url = url + ";ACCESS_MODE_DATA=r";
            }
        } else {
            mode.dataFolder.mkdirs();
            if (mode.hostTcp) {
                tcpServer = startTcpServer(mode.tcpPort, driver);
            }
            url = "jdbc:h2:" + localPath(mode.dataFolder)
                    + ";MODE=REGULAR;DB_CLOSE_ON_EXIT=FALSE";
            if (mode.autoServer) {
                url = url + ";AUTO_SERVER=TRUE";
                if (mode.tcpPort > 0) {
                    url = url + ";AUTO_SERVER_PORT=" + mode.tcpPort;
                }
            }
            if (mode.readOnly) {
                url = url + ";ACCESS_MODE_DATA=r";
            }
        }

        Connection c = driver.connect(url, props);
        if (c == null) {
            throw new java.sql.SQLException("H2 driver did not accept the connection URL");
        }
        if (!mode.readOnly) {
            try (Statement st = c.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS store "
                        + "(name VARCHAR(64) PRIMARY KEY, data BLOB)");
            }
        }
        VeriteH2 h2 = new VeriteH2(c, mode.readOnly, tcpServer);
        active = h2;
        return h2;
    }

    private static String localPath(File dataFolder) {
        return new File(dataFolder, "verite").getAbsolutePath().replace('\\', '/');
    }

    private static Object startTcpServer(int port, Driver driver) throws Exception {
        Class<?> serverClass = Class.forName("org.h2.tools.Server");
        String[] args = port > 0
                ? new String[] {"-tcpAllowOthers", "-tcpPort", String.valueOf(port), "-ifNotExists"}
                : new String[] {"-tcpAllowOthers", "-ifNotExists"};
        Object server = serverClass.getMethod("createTcpServer", String[].class)
                .invoke(null, (Object) args);
        serverClass.getMethod("start").invoke(server);
        return server;
    }

    public static VeriteH2 active() {
        return active;
    }

    public static boolean isActive() {
        return active != null;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public synchronized byte[] read(String name) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT data FROM store WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes(1);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public synchronized void write(String name, byte[] bytes) {
        if (readOnly) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO store (name, data) KEY (name) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setBytes(2, bytes);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public synchronized boolean writeIf(String name, byte[] expected, byte[] next) {
        if (readOnly) {
            return false;
        }
        try {
            boolean prevAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                boolean applied;
                if (expected == null) {
                    applied = insertAbsent(name, next);
                } else {
                    applied = updateIfEquals(name, expected, next);
                }
                if (applied) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
                return applied;
            } catch (Exception e) {
                conn.rollback();
                return false;
            } finally {
                conn.setAutoCommit(prevAuto);
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean insertAbsent(String name, byte[] next) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store (name, data) SELECT ?, ? "
                        + "WHERE NOT EXISTS (SELECT 1 FROM store WHERE name = ?)")) {
            ps.setString(1, name);
            ps.setBytes(2, next);
            ps.setString(3, name);
            return ps.executeUpdate() == 1;
        }
    }

    private boolean updateIfEquals(String name, byte[] expected, byte[] next) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE store SET data = ? WHERE name = ? AND data = ?")) {
            ps.setBytes(1, next);
            ps.setString(2, name);
            ps.setBytes(3, expected);
            return ps.executeUpdate() == 1;
        }
    }

    public synchronized boolean remove(String name) {
        if (readOnly) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM store WHERE name = ?")) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public synchronized void close() {
        try {
            conn.close();
        } catch (Exception ignored) {
        }
        if (tcpServer != null) {
            try {
                tcpServer.getClass().getMethod("stop").invoke(tcpServer);
            } catch (Exception ignored) {
            }
        }
        if (active == this) {
            active = null;
        }
    }

    public static final class Mode {
        final File dataFolder;
        final boolean autoServer;
        final boolean hostTcp;
        final int tcpPort;
        final String remoteHost;
        final int remotePort;
        boolean readOnly;

        public Mode(File dataFolder, boolean autoServer, boolean hostTcp, int tcpPort,
                    String remoteHost, int remotePort) {
            this.dataFolder = dataFolder;
            this.autoServer = autoServer;
            this.hostTcp = hostTcp;
            this.tcpPort = tcpPort;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        public Mode readOnly() {
            this.readOnly = true;
            return this;
        }
    }
}
