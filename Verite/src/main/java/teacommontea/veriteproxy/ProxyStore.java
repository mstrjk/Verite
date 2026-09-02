package teacommontea.veriteproxy;

import teacommontea.util.KvStore;
import teacommontea.util.VeriteH2;
import teacommontea.veritesauver.core.SauverDAO;

import java.io.File;

public final class ProxyStore {

    private final VeriteH2 db;
    private final KvStore store;
    private final SauverDAO dao;

    private ProxyStore(VeriteH2 db, KvStore store, SauverDAO dao) {
        this.db = db;
        this.store = store;
        this.dao = dao;
    }

    public static ProxyStore open(ProxyConfig cfg) throws Exception {
        VeriteH2.Mode mode;
        switch (cfg.mode()) {
            case "remote":
                mode = new VeriteH2.Mode(cfg.dataFolder(), false, false, 0,
                        cfg.remoteHost(), cfg.remotePort()).readOnly();
                break;
            case "embedded":
            default:
                mode = new VeriteH2.Mode(cfg.dataFolder(), cfg.autoServer(), false,
                        cfg.autoServer() ? cfg.port() : 0, null, 0).readOnly();
                break;
        }
        VeriteH2 db = VeriteH2.open(mode);
        KvStore store = KvStore.open("sauver", true, true);
        SauverDAO dao = new SauverDAO(store);
        return new ProxyStore(db, store, dao);
    }

    public SauverDAO dao() {
        return dao;
    }

    public void close() {
        store.shutdown();
        db.close();
    }
}
