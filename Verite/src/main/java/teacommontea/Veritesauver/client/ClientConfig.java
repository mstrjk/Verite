package teacommontea.veritesauver.client;

public final class ClientConfig {

    private ClientConfig() {}

    public static boolean enabled() {
        return bool("client.detect.enabled", true);
    }

    public static boolean signProbe() {
        return bool("client.detect.probe.signs", true);
    }

    public static boolean cookieProbe() {
        return bool("client.detect.probe.cookies", true);
    }

    public static int probeDelayTicks() {
        return teacommontea.veritesauver.util.SauverConfig.clientInt("client.detect.probe.delay.ticks", 60);
    }

    public static boolean notifyVanilla() {
        return bool("client.detect.notify.vanilla", false);
    }

    private static boolean bool(String path, boolean def) {
        return teacommontea.veritesauver.util.SauverConfig.clientBool(path, def);
    }
}
