package teacommontea.veritedoux.process;

import java.util.List;
import java.util.UUID;

import teacommontea.veritedoux.util.EveDebug;
import teacommontea.veritedoux.util.EveText;
import teacommontea.veritedoux.process.EveRepeat;
import teacommontea.veritedoux.process.SymbolBoard;
import teacommontea.veritedoux.EveEntry;
import teacommontea.veritedoux.util.EveSettings;
import teacommontea.veritedoux.postprocess.EveStore;

public final class EveRoute {

    private static final long DEDUP_WINDOW_MS = 750;
    private static final java.util.Map<String, long[]> DEDUP_STAMP = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, EveEntry.Result> DEDUP_VERDICT = new java.util.concurrent.ConcurrentHashMap<>();

    private static volatile EveSettings settings = null;
    private static volatile EveStore store;

    private EveRoute() {}

    public static void configure(EveSettings s) {
        settings = s;
    }

    public static void enableStore(EveStore s) {
        store = s;
    }

    public static void shutdown() {
        if (store != null) {
            store.close();
            store = null;
        }
    }

    public static EveEntry.Result route(String message) {
        if (message == null || message.isEmpty() || EveBoards.eve() == null) {
            return EveEntry.Result.CLEAN;
        }
        if (settings != null && !settings.componentEnabled) {
            return EveEntry.Result.CLEAN;
        }

        long s = EveDebug.start();
        boolean gate = EveGate.languageBlocks(message);
        EveDebug.end("languageGateBlocks", s);
        if (gate) {
            return EveEntry.Result.BLOCK;
        }

        if (EveGate.spamBlocks(message)) {
            return EveEntry.Result.BLOCK;
        }

        if (EveGate.unreadableBlocks(message)) {
            return EveEntry.Result.BLOCK;
        }

        SymbolBoard symbols = EveBoards.symbols();
        if (symbols != null && symbols.hits(message)) {
            return EveEntry.Result.BLOCK;
        }

        boolean scanAll = true;

        String rawVeto = EveText.foldAccents(message.toLowerCase());
        s = EveDebug.start();
        List<String> cands = candidates(message);
        EveDebug.end("candidates(gen)", s);

        s = EveDebug.start();
        boolean anywhere = EveGate.anywhereBlocks(rawVeto, cands);
        EveDebug.end("anywhereBlocks", s);
        if (anywhere) {
            return EveEntry.Result.BLOCK;
        }
        EveEntry.Result worst = EveEntry.Result.CLEAN;
        EveMatcher.CheckWork work = new EveMatcher.CheckWork();
        for (String cand : cands) {
            long sc = EveDebug.start();
            EveEntry.Result r = EveMatcher.scan(cand, rawVeto, scanAll, work);
            EveDebug.end("checkAll(per-candidate)", sc);
            if (r == EveEntry.Result.SELF_HARM || r == EveEntry.Result.ABUSE) {
                return r;
            }
            if (r != EveEntry.Result.CLEAN) {
                worst = (worst == EveEntry.Result.CLEAN) ? r : EveMatcher.worse(worst, r);
            }
        }
        return worst;
    }

    public static Explain explain(String message) {
        if (message == null || message.isEmpty() || EveBoards.eve() == null) {
            return new Explain("engine-off", EveEntry.Result.CLEAN);
        }
        if (settings != null && !settings.componentEnabled) {
            return new Explain("component-disabled", EveEntry.Result.CLEAN);
        }
        if (EveGate.languageBlocks(message)) {
            return new Explain("language-gate", EveEntry.Result.BLOCK);
        }
        if (EveGate.spamBlocks(message)) {
            return new Explain("spam-gate", EveEntry.Result.BLOCK);
        }
        if (EveGate.unreadableBlocks(message)) {
            return new Explain("unreadable-gate", EveEntry.Result.BLOCK);
        }
        SymbolBoard symbols = EveBoards.symbols();
        if (symbols != null && symbols.hits(message)) {
            return new Explain("symbol-board", EveEntry.Result.BLOCK);
        }
        String rawVeto = EveText.foldAccents(message.toLowerCase());
        List<String> cands = candidates(message);
        java.util.List<CandidateResult> candResults = new java.util.ArrayList<>();
        if (EveGate.anywhereBlocks(rawVeto, cands)) {
            return new Explain("anywhere-gate", EveEntry.Result.BLOCK, cands, candResults);
        }
        EveEntry.Result worst = EveEntry.Result.CLEAN;
        String worstCand = null;
        EveMatcher.CheckWork work = new EveMatcher.CheckWork();
        for (String cand : cands) {
            EveEntry.Result r = EveMatcher.scan(cand, rawVeto, scanAll(), work);
            candResults.add(new CandidateResult(cand, r));
            if (r == EveEntry.Result.SELF_HARM || r == EveEntry.Result.ABUSE) {
                return new Explain("matcher:" + cand, r, cands, candResults);
            }
            if (r != EveEntry.Result.CLEAN) {
                if (worst == EveEntry.Result.CLEAN) {
                    worst = r;
                    worstCand = cand;
                } else {
                    EveEntry.Result merged = EveMatcher.worse(worst, r);
                    if (merged != worst) {
                        worst = merged;
                        worstCand = cand;
                    }
                }
            }
        }
        if (worst == EveEntry.Result.CLEAN) {
            return new Explain("clean", EveEntry.Result.CLEAN, cands, candResults);
        }
        return new Explain("matcher:" + worstCand, worst, cands, candResults);
    }

    private static boolean scanAll() {
        return true;
    }

    public record CandidateResult(String candidate, EveEntry.Result verdict) {}

    public record Explain(String stage, EveEntry.Result verdict,
                          java.util.List<String> candidates, java.util.List<CandidateResult> candidateResults) {
        public Explain(String stage, EveEntry.Result verdict) {
            this(stage, verdict, java.util.List.of(), java.util.List.of());
        }
    }

    private static String dedupKey(UUID player, String message) {
        return player + "\0" + message;
    }

    public static EveEntry.Result route(UUID player, String message) {
        boolean anon = player == null || player.equals(teacommontea.api.VeriteFilter.ANONYMOUS);
        if (!anon && (settings == null || settings.componentEnabled) && EveRepeat.blocks(player, message)) {
            return EveEntry.Result.REPEAT;
        }
        String key = dedupKey(player, message);
        long now = System.currentTimeMillis();
        long[] prev = DEDUP_STAMP.get(key);
        if (prev != null && now - prev[0] < DEDUP_WINDOW_MS) {
            EveEntry.Result cached = DEDUP_VERDICT.get(key);
            if (cached != null) {
                return cached;
            }
        }

        EveEntry.Result r = route(message);
        if (!anon && r != EveEntry.Result.CLEAN && store != null) {
            store.record(player, r, message);
        }

        DEDUP_STAMP.put(key, new long[]{now});
        DEDUP_VERDICT.put(key, r);
        if (DEDUP_STAMP.size() > 512) {
            DEDUP_STAMP.entrySet().removeIf(e -> now - e.getValue()[0] > DEDUP_WINDOW_MS);
            DEDUP_VERDICT.keySet().retainAll(DEDUP_STAMP.keySet());
        }
        return r;
    }

    public static void recordSend(UUID player, String message) {
        boolean anon = player == null || player.equals(teacommontea.api.VeriteFilter.ANONYMOUS);
        if (!anon && (settings == null || settings.componentEnabled)) {
            EveRepeat.record(player, message);
        }
    }

    public static int count(UUID player) {
        return store == null ? 0 : store.count(player);
    }

    private static List<String> candidates(String message) {
        boolean entity = settings == null || settings.entityStrip;
        boolean homo = settings == null || settings.homoglyphFold;
        return EveText.candidates(message, entity, homo);
    }
}
