package teacommontea.veritesauver.invsee;

import java.lang.reflect.Constructor;
import java.util.UUID;


final class InvSeeProfiles {

    private static volatile Constructor<?> ctor;

    private InvSeeProfiles() {}

    static Object newProfile(UUID uuid, String name) throws Throwable {
        Constructor<?> c = ctor;
        if (c == null) {
            Class<?> profile = InvSeeAccess.firstExisting("com.mojang.authlib.GameProfile");
            if (profile == null) {
                throw new InvSeeAccess.Unsupported("no com.mojang.authlib.GameProfile on this server");
            }
            c = profile.getConstructor(UUID.class, String.class);
            ctor = c;
        }
        return c.newInstance(uuid, name);
    }
}
