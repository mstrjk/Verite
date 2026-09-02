package teacommontea.veritedoux.process;

import java.util.List;

import teacommontea.veritedoux.util.EveText;
import teacommontea.veritedoux.preprocess.EveLang;
import teacommontea.veritedoux.preprocess.EveRegister;
import teacommontea.veritedoux.preprocess.EveScam;
import teacommontea.veritedoux.EveEntry;
import teacommontea.veritedoux.util.EveSettings;

public final class EveGate {

    private static final java.util.regex.Pattern WS = java.util.regex.Pattern.compile("\\s+");

    private static EveSettings settings = null;

    private EveGate() {}

    public static void configure(EveSettings s) {
        settings = s;
    }

    static boolean languageBlocks(String message) {
        if (settings == null || !settings.languageGateActive()) {
            return false;
        }
        java.util.Set<String> present = EveLang.presentLanguages(message);
        if (present.isEmpty()) {
            return !settings.languageAllowed("undeterminable");
        }
        return present.stream().anyMatch(lang -> !settings.languageAllowed(lang));
    }

    static boolean spamBlocks(String message) {
        if (settings == null || !settings.blockSpam) return false;
        if (EveScam.ready()) return EveScam.looksLikeScam(message);
        return EveRegister.ready() && EveRegister.looksLikeAd(message);
    }

    static boolean unreadableBlocks(String message) {
        if (settings == null || !settings.keepChatReadable) {
            return false;
        }
        int letters = 0, fancy = 0;
        int len = message.length();
        for (int i = 0; i < len; ) {
            int cp = message.codePointAt(i);
            i += Character.charCount(cp);
            if (EveText.isFancyLetter(cp)) {
                fancy++;
                letters++;
            } else if (Character.isLetter(cp)) {
                letters++;
            }
        }
        return letters > 0 && fancy * 2 >= letters;
    }

    static boolean anywhereBlocks(String rawVeto, List<String> cands) {
        if (!teacommontea.veritedoux.util.Eve.nativeAvailable()) {
            return false;
        }
        int maxWords = EveBoards.maxWords();
        java.util.List<teacommontea.veritedoux.util.Eve> boards = new java.util.ArrayList<>();
        boards.addAll(EveBoards.mainBoards().values());
        boards.addAll(EveBoards.dialectBoards().values());
        for (teacommontea.veritedoux.util.Eve board : boards) {
            if (board == null) {
                continue;
            }
            java.util.Map<Integer, java.util.BitSet> covered = new java.util.HashMap<>();
            for (String cand : cands) {
                String veto = cand + "\n" + rawVeto;
                String[] words = WS.split(cand);
                for (int i = 0; i < words.length; i++) {
                    words[i] = EveText.trimEdges(words[i]);
                }
                for (int i = 0; i < words.length; i++) {
                    StringBuilder chunk = new StringBuilder();
                    for (int n = 0; n < maxWords && i + n < words.length; n++) {
                        if (n > 0) {
                            chunk.append(' ');
                        }
                        chunk.append(words[i + n]);
                        java.util.List<teacommontea.veritedoux.util.Eve.AnywhereHit> hits =
                                board.anywhereHits(chunk.toString(), veto);
                        for (teacommontea.veritedoux.util.Eve.AnywhereHit hit : hits) {
                            int need = board.anywhereGroupCount(hit.rule());
                            if (need <= 0) {
                                continue;
                            }
                            java.util.BitSet seen =
                                    covered.computeIfAbsent(hit.rule(), k -> new java.util.BitSet());
                            for (int g : hit.groups()) {
                                seen.set(g);
                            }
                            if (seen.cardinality() >= need) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
