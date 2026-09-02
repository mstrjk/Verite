package teacommontea.veritedoux.process;

import java.util.ArrayList;
import java.util.List;

import teacommontea.veritedoux.util.EveDebug;
import teacommontea.veritedoux.util.EveText;
import teacommontea.veritedoux.preprocess.EveLang;
import teacommontea.veritedoux.preprocess.EveSegment;
import teacommontea.veritedoux.EveEntry;
import teacommontea.veritedoux.util.EveSettings;

public final class EveMatcher {

    private static final java.util.regex.Pattern WS = java.util.regex.Pattern.compile("\\s+");

    private static final int FP_MIN_LEN = 6;
    private static final int SEG_MIN_LEN = 5;
    private static final int MAX_SCAN_WORDS = 80;
    private static final int SCAN_STEP = 64;

    private static final java.util.Map<String, EveEntry.Result> FINGERPRINTS = new java.util.HashMap<>();
    private static final java.util.Map<String, String> FP_SOURCE = new java.util.HashMap<>();
    private static final java.util.Map<String, Boolean> FP_COLLISION_CHECKED = new java.util.concurrent.ConcurrentHashMap<>();

    private static EveSettings settings = null;

    private EveMatcher() {}

    public static void configure(EveSettings s) {
        settings = s;
    }

    public static int fingerprintCount() {
        return FINGERPRINTS.size();
    }

    public static int buildFingerprints(teacommontea.veritedoux.util.Eve eve) {
        FINGERPRINTS.clear();
        FP_COLLISION_CHECKED.clear();
        FP_SOURCE.clear();
        int wordCount = 0;
        if (eve != null) {
            for (teacommontea.veritedoux.util.Eve.ConcreteWord cw : eve.concreteWords()) {
                String w = cw.word();
                wordCount++;
                if (w.length() < FP_MIN_LEN) {
                    continue;
                }
                EveEntry.Result cat = cw.flag("sh", false) ? EveEntry.Result.SELF_HARM
                        : cw.flag("ea", false) ? EveEntry.Result.ABUSE
                        : cw.flag("pf", false) ? EveEntry.Result.PROFANITY : EveEntry.Result.BLOCK;

                String fp = EveText.fingerprint(w);
                FINGERPRINTS.merge(fp, cat, EveMatcher::worse);
                FP_SOURCE.putIfAbsent(fp, w);
            }
        }
        return wordCount;
    }

    private static boolean fpTrusted(String fp) {
        return FP_COLLISION_CHECKED.computeIfAbsent(fp, k -> {
            String source = FP_SOURCE.get(k);
            return source != null && !EveSegment.fingerprintCollides(k, source);
        });
    }

    static EveEntry.Result worse(EveEntry.Result a, EveEntry.Result b) {
        if (a == EveEntry.Result.SELF_HARM || b == EveEntry.Result.SELF_HARM) return EveEntry.Result.SELF_HARM;
        if (a == EveEntry.Result.ABUSE || b == EveEntry.Result.ABUSE) return EveEntry.Result.ABUSE;
        if (a == EveEntry.Result.PROFANITY && b == EveEntry.Result.PROFANITY) return EveEntry.Result.PROFANITY;
        return EveEntry.Result.BLOCK;
    }

    private static EveEntry.Result escalate(EveEntry.Result worst, EveEntry.Result r) {
        if (r == EveEntry.Result.BLOCK) {
            return EveEntry.Result.BLOCK;
        }
        if (r == EveEntry.Result.PROFANITY && worst == EveEntry.Result.CLEAN) {
            return EveEntry.Result.PROFANITY;
        }
        return worst;
    }

    static final class CheckWork {
        final java.util.HashMap<String, String> segmentTokens = new java.util.HashMap<>();
        final java.util.HashMap<String, java.util.Map<String, Double>> segmentPositions = new java.util.HashMap<>();
        final java.util.HashMap<String, float[]> languagePriors = new java.util.HashMap<>();
        final java.util.IdentityHashMap<teacommontea.veritedoux.util.Eve,
                java.util.HashMap<String, java.util.List<teacommontea.veritedoux.util.Eve.Match>>> contextualScans =
                new java.util.IdentityHashMap<>();
    }

    private static final class ScanScope {
        final String vetoLine;
        final teacommontea.veritedoux.util.Eve[] boards;
        final String[] boardLangs;
        final boolean[] shortCircuit;
        final String[] gateKeys;
        final CheckWork work;
        private final java.util.HashMap<String, EveEntry.Result> chunks = new java.util.HashMap<>();
        final java.util.HashSet<String> doneLines = new java.util.HashSet<>();

        private record Entry(teacommontea.veritedoux.util.Eve board, String lang, boolean shortCircuit) {}

        ScanScope(String vetoLine, ScanCtx ctx, CheckWork work) {
            this.vetoLine = vetoLine;
            this.work = work;
            java.util.ArrayList<Entry> es = new java.util.ArrayList<>();
            for (String lang : ctx.langs()) {
                teacommontea.veritedoux.util.Eve main = EveBoards.mainBoards().get(lang);
                if (main != null) es.add(new Entry(main, lang, true));
                teacommontea.veritedoux.util.Eve prof = EveBoards.generalProfanity().get(lang);
                if (prof != null) es.add(new Entry(prof, lang, false));
            }
            for (int i = 0; i < ctx.dialectBoards().size(); i++) {
                es.add(new Entry(ctx.dialectBoards().get(i), ctx.dialectLangs().get(i), true));
            }
            int n = es.size();
            boards = new teacommontea.veritedoux.util.Eve[n];
            boardLangs = new String[n];
            shortCircuit = new boolean[n];
            gateKeys = new String[n];
            for (int i = 0; i < n; i++) {
                Entry e = es.get(i);
                boards[i] = e.board();
                boardLangs[i] = e.lang();
                shortCircuit[i] = e.shortCircuit();
                gateKeys[i] = e.board().gateKey(vetoLine);
            }
        }

        java.util.List<teacommontea.veritedoux.util.Eve.Match> scan(int boardIndex, String chunk) {
            teacommontea.veritedoux.util.Eve board = boards[boardIndex];
            String gateKey = gateKeys[boardIndex];
            String key = (gateKey == null ? vetoLine : gateKey) + (char) 0 + chunk;
            return work.contextualScans.computeIfAbsent(board, b -> new java.util.HashMap<>())
                       .computeIfAbsent(key, c -> {
                           long t = EveDebug.start();
                           java.util.List<teacommontea.veritedoux.util.Eve.Match> r = board.scan(chunk, vetoLine);
                           EveDebug.end("native board.scan", t);
                           return r;
                       });
        }

        EveEntry.Result classify(String chunk) {
            EveEntry.Result cached = chunks.get(chunk);
            if (cached != null) return cached;
            EveEntry.Result worst = EveEntry.Result.CLEAN;
            for (int b = 0; b < boards.length; b++) {
                EveEntry.Result r = scanBoard(this, b, chunk, boardLangs[b]);
                if (shortCircuit[b] && (r == EveEntry.Result.SELF_HARM || r == EveEntry.Result.ABUSE)) {
                    chunks.put(chunk, r);
                    return r;
                }
                worst = escalate(worst, r);
            }
            chunks.put(chunk, worst);
            return worst;
        }
    }

    private record ScanCtx(java.util.Set<String> langs,
                           java.util.List<teacommontea.veritedoux.util.Eve> dialectBoards,
                           java.util.List<String> dialectLangs) {}

    static EveEntry.Result scan(String lower, String rawVeto, boolean scanAll, CheckWork work) {
        String[] allWords = WS.split(lower);
        if (allWords.length > MAX_SCAN_WORDS) {
            EveEntry.Result worst = EveEntry.Result.CLEAN;
            for (int start = 0; start < allWords.length; start += SCAN_STEP) {
                int end = Math.min(start + MAX_SCAN_WORDS, allWords.length);
                StringBuilder w = new StringBuilder();
                for (int i = start; i < end; i++) { if (i > start) w.append(' '); w.append(allWords[i]); }
                EveEntry.Result r = checkWindow(w.toString(), rawVeto, scanAll, work);
                if (r == EveEntry.Result.SELF_HARM || r == EveEntry.Result.ABUSE) return r;
                if (r != EveEntry.Result.CLEAN) worst = worst == EveEntry.Result.CLEAN ? r : worse(worst, r);
                if (end == allWords.length) break;
            }
            return worst;
        }
        return checkWindow(lower, rawVeto, scanAll, work);
    }

    private static ScanCtx resolveScope(String lower, boolean scanAll, List<double[]> tokenLangs) {
        java.util.Set<String> langs;
        if (scanAll) {
            langs = EveBoards.mainBoards().keySet();
        } else {
            langs = EveLang.presentLanguages(lower, tokenLangs);
            if (langs.isEmpty()) {
                String dom = EveLang.confidence(tokenLangs).lang();
                langs = (dom != null && !dom.isEmpty())
                        ? java.util.Set.of(dom) : EveBoards.mainBoards().keySet();
            }
        }
        java.util.List<teacommontea.veritedoux.util.Eve> dialectBoards = new java.util.ArrayList<>();
        java.util.List<String> dialectLangs = new java.util.ArrayList<>();
        if (!EveBoards.dialectBoards().isEmpty()) {
            for (java.util.Map.Entry<String, teacommontea.veritedoux.util.Eve> e : EveBoards.dialectBoards().entrySet()) {
                String boardLang = EveBoards.dialectToLang().getOrDefault(e.getKey(), "");
                if (langs.contains(boardLang)) {
                    dialectBoards.add(e.getValue());
                    dialectLangs.add(boardLang);
                }
            }
        }
        return new ScanCtx(langs, dialectBoards, dialectLangs);
    }

    private static EveEntry.Result checkWindow(String lower, String rawVeto, boolean scanAll, CheckWork work) {
        String vetoLine = lower + "\n" + rawVeto;
        long s = EveDebug.start();
        List<double[]> tokenLangs = EveLang.label(lower, work.languagePriors);
        EveDebug.end("EveLang.label", s);
        s = EveDebug.start();
        ScanCtx ctx = resolveScope(lower, scanAll, tokenLangs);
        EveDebug.end("resolveScope", s);

        ScanScope sc = new ScanScope(vetoLine, ctx, work);

        s = EveDebug.start();
        EveEntry.Result direct = matchLine(lower, 1, sc);
        EveDebug.end("matchLine(direct)", s);
        if (direct != EveEntry.Result.CLEAN) {
            return direct;
        }

        if (EveText.hasRepeat(lower)) {
            String capped = EveText.reduceRuns(lower, 2);
            if (!capped.equals(lower)) {
                EveEntry.Result r = matchLine(capped, 1, sc);
                if (r != EveEntry.Result.CLEAN) return r;
            }
            String single = EveText.reduceRuns(lower, 1);
            if (!single.equals(lower) && !single.equals(capped)) {
                EveEntry.Result r = matchLine(single, 1, sc);
                if (r != EveEntry.Result.CLEAN) return r;
            }
        }

        if ((settings == null || settings.fingerprint) && !FINGERPRINTS.isEmpty()) {
            long sf = EveDebug.start();
            String[] fpWords = WS.split(lower);
            for (int wi = 0; wi < fpWords.length; wi++) {
                String w = fpWords[wi];
                if (w.length() >= FP_MIN_LEN && w.chars().allMatch(Character::isLetter)) {
                    String key = EveText.fingerprint(w);
                    EveEntry.Result fp = FINGERPRINTS.get(key);
                    if (fp != null && fpTrusted(key) && !langAllows(EveLang.tokenLang(tokenLangs, wi), w)) {
                        EveDebug.end("fingerprint", sf);
                        return fp;
                    }
                }
            }
            EveDebug.end("fingerprint", sf);
        }
        if (EveSegment.ready()) {
            List<String> candidates = new ArrayList<>();
            candidates.add(lower);
            for (String tok : WS.split(lower)) {
                candidates.add(tok);
            }
            if (settings == null || settings.deobfuscate) {
                long sd = EveDebug.start();
                java.util.HashSet<String> seenCand = new java.util.HashSet<>();
                for (String cand : candidates) {
                    if (!seenCand.add(cand)) continue;
                    String recovered = EveSegment.deobfuscate(cand);
                    if (recovered != null) {
                        EveEntry.Result r = matchLine(recovered, 1, sc);
                        if (r != EveEntry.Result.CLEAN) {
                            EveDebug.end("deobfuscate+match", sd);
                            return r;
                        }
                    }
                }

                String dense = teacommontea.veritedoux.util.Eve.denseStrip(lower);
                if (dense != null) {
                    EveEntry.Result r = matchLine(dense, 1, sc);
                    if (r != EveEntry.Result.CLEAN) {
                        EveDebug.end("deobfuscate+match", sd);
                        return r;
                    }
                }
                EveDebug.end("deobfuscate+match", sd);
            }

            if (settings == null || settings.segmentation) {
                long ss = EveDebug.start();
                long sg = EveDebug.start();
                List<String> segmented = EveSegment.segmentLines(lower, work.segmentTokens, work.segmentPositions);
                EveDebug.end("segmentLines", sg);
                for (String segged : segmented) {
                    EveEntry.Result r = matchLine(segged, SEG_MIN_LEN, sc);
                    if (r != EveEntry.Result.CLEAN) {
                        EveDebug.end("segment+match", ss);
                        return r;
                    }
                }
                EveDebug.end("segment+match", ss);
            }
        }
        return EveEntry.Result.CLEAN;
    }

    public static FingerprintReport fingerprintReport(String word, String lang) {
        boolean eligible = word != null && word.length() >= FP_MIN_LEN && word.chars().allMatch(Character::isLetter);
        String key = word == null ? "" : EveText.fingerprint(word);
        EveEntry.Result collidesWith = eligible ? FINGERPRINTS.get(key) : null;
        String source = collidesWith != null ? FP_SOURCE.get(key) : null;
        boolean trusted = collidesWith != null && fpTrusted(key);
        boolean allowed = langAllows(lang, word);
        boolean wouldBlock = eligible && collidesWith != null && trusted && !allowed;
        return new FingerprintReport(word, eligible, key, collidesWith, source, trusted, allowed, wouldBlock);
    }

    public record FingerprintReport(String word, boolean eligible, String fingerprint,
            EveEntry.Result collidesWith, String collisionSource, boolean trusted,
            boolean languageAllows, boolean wouldBlock) {}

    private static boolean langAllows(String lang, String word) {
        return lang != null && !lang.isEmpty()
                && EveSegment.knownIn(lang, word) && !blockedByLang(lang, word);
    }

    private static boolean blockedByLang(String lang, String word) {
        teacommontea.veritedoux.util.Eve eve = EveBoards.eve();
        if (eve == null) return false;
        return !eve.scan(word, word).isEmpty();
    }

    private static EveEntry.Result matchLine(String lower, int minSingleLen, ScanScope sc) {
        if (EveBoards.eve() == null) {
            return EveEntry.Result.CLEAN;
        }
        if (!sc.doneLines.add(minSingleLen + "|" + lower)) {
            return EveEntry.Result.CLEAN;
        }

        String[] words = WS.split(lower);
        for (int wi = 0; wi < words.length; wi++) {
            words[wi] = EveText.trimEdges(words[wi]);
        }

        int maxWords = EveBoards.maxWords();
        EveEntry.Result worst = EveEntry.Result.CLEAN;
        for (int i = 0; i < words.length; i++) {
            StringBuilder chunk = new StringBuilder(words[i]);
            for (int n = 0; n < maxWords && i + n < words.length; n++) {
                if (n > 0) chunk.append(' ').append(words[i + n]);
                if (n == 0 && chunk.length() < minSingleLen) {
                    continue;
                }
                EveEntry.Result r = sc.classify(chunk.toString());
                if (r == EveEntry.Result.SELF_HARM || r == EveEntry.Result.ABUSE) return r;
                worst = escalate(worst, r);
            }
        }
        return worst;
    }

    public record MatchTrace(String chunk, String lang, int rule, String ruleName, String realm, EveEntry.Result category) {}

    private static final ThreadLocal<java.util.List<MatchTrace>> TRACE = new ThreadLocal<>();

    public static void traceBegin() {
        TRACE.set(new java.util.ArrayList<>());
    }

    public static java.util.List<MatchTrace> traceEnd() {
        java.util.List<MatchTrace> t = TRACE.get();
        TRACE.remove();
        return t == null ? java.util.List.of() : t;
    }

    private static EveEntry.Result scanBoard(ScanScope sc, int boardIndex, String c, String lang) {
        EveEntry.Result worst = EveEntry.Result.CLEAN;
        java.util.List<teacommontea.veritedoux.util.Eve.Match> matches = sc.scan(boardIndex, c);
        for (teacommontea.veritedoux.util.Eve.Match m : matches) {
            EveEntry.Result r = categoryOf(m, lang);
            java.util.List<MatchTrace> tr = TRACE.get();
            if (tr != null && r != EveEntry.Result.CLEAN) {
                tr.add(new MatchTrace(c, lang, m.rule(), m.name(), m.realm(), r));
            }
            if (r == EveEntry.Result.SELF_HARM || r == EveEntry.Result.ABUSE) {
                return r;
            }
            worst = escalate(worst, r);
        }
        return worst;
    }

    private static EveEntry.Result categoryOf(teacommontea.veritedoux.util.Eve.Match m, String lang) {
        if (m.flag("sh", false)) {
            return (settings == null || settings.selfHarmEnabled(lang)) ? EveEntry.Result.SELF_HARM : EveEntry.Result.CLEAN;
        }
        if (m.flag("ea", false)) {
            return (settings == null || settings.abuseEnabled(lang)) ? EveEntry.Result.ABUSE : EveEntry.Result.CLEAN;
        }
        if (m.flag("pf", false)) {
            return EveEntry.Result.PROFANITY;
        }
        return (settings == null || settings.slurEnabled(lang)) ? EveEntry.Result.BLOCK : EveEntry.Result.CLEAN;
    }
}
