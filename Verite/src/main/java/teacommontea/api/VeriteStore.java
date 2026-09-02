package teacommontea.api;


import teacommontea.veritesauver.Sauver;
import teacommontea.util.KvStore;

public final class VeriteStore {

    private VeriteStore() {}

    private static KvStore store() {
        Sauver s = Sauver.instance();
        return s == null ? null : s.store();
    }

    public static boolean available() {
        return store() != null;
    }

    public static VeriteScope scope(String name) {
        KvStore st = store();
        return st == null ? null : new VeriteScope(st.scope(name));
    }
}
