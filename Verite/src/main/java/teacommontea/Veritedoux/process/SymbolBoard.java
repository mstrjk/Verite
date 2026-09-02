package teacommontea.veritedoux.process;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public final class SymbolBoard {

    private final teacommontea.veritedoux.util.Eve board;

    private SymbolBoard(teacommontea.veritedoux.util.Eve board) {
        this.board = board;
    }

    public static SymbolBoard parse(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        if (!teacommontea.veritedoux.util.Eve.nativeAvailable()) {
            return null;
        }
        try {
            teacommontea.veritedoux.util.Eve parsed = teacommontea.veritedoux.util.Eve.parse(source);
            if (parsed.ruleCount() == 0) {
                parsed.close();
                return null;
            }
            return new SymbolBoard(parsed);
        } catch (Exception e) {
            return null;
        }
    }

    public int ruleCount() {
        return board.ruleCount();
    }

    private static List<String> glyphTokens(String message) {
        Set<String> tokens = new LinkedHashSet<>();
        StringBuilder run = new StringBuilder();
        int n = message.length();
        for (int i = 0; i < n; ) {
            int cp = message.codePointAt(i);
            i += Character.charCount(cp);
            if (cp > 0x7F) {
                String glyph = new String(Character.toChars(cp));
                tokens.add(glyph);
                run.append(glyph);
            } else if (run.length() > 0) {
                tokens.add(run.toString());
                run.setLength(0);
            }
        }
        if (run.length() > 0) {
            tokens.add(run.toString());
        }
        return new ArrayList<>(tokens);
    }

    public boolean hits(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        for (String token : glyphTokens(message)) {
            if (!board.scan(token, token).isEmpty()) {
                return true;
            }
        }
        return !board.sequenceHits(message).isEmpty();
    }
}
