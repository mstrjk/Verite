package teacommontea.api;


import teacommontea.veritesauver.geoip.SauverGeoIp;

public final class VeriteGeoIp {

    private VeriteGeoIp() {}

    public static String country(String ip) {
        return SauverGeoIp.country(ip);
    }

    public static boolean available() {
        return SauverGeoIp.available();
    }

    public static boolean isPrivate(String ip) {
        return SauverGeoIp.isPrivate(ip);
    }

    public static boolean attempted() {
        return SauverGeoIp.attempted();
    }
}
