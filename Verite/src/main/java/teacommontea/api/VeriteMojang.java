package teacommontea.api;

import teacommontea.api.internal.ApiBridge;

import teacommontea.veritesauver.mojang.SauverMojang;

public final class VeriteMojang {

    private VeriteMojang() {}

    public static MojangProfile lookup(String name) {
        return ApiBridge.toMojang(SauverMojang.lookup(name));
    }
}
