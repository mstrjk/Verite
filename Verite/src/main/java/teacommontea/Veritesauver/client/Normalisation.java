package teacommontea.veritesauver.client;

import java.util.ArrayList;
import java.util.List;

final class Normalisation {

    private Normalisation() {}

    private static final int SUSPICIOUS = 2;

    static List<Signal> evaluate(ClientProfile profile, boolean signsRan, boolean cookiesRan) {
        String reported = profile.reportedBrand();
        boolean claimsVanilla = reported == null || reported.isBlank()
                || reported.equalsIgnoreCase("vanilla");

        List<String> scrubbed = new ArrayList<>();
        if (claimsVanilla && !profile.has(Signal.Source.CHANNEL)
                && !profile.has(Signal.Source.UNKNOWN_CHANNEL)) {
            scrubbed.add("no mod channels");
        }
        if (signsRan && !profile.has(Signal.Source.SIGN)) {
            scrubbed.add("no translation or keybind leaks");
        }
        if (cookiesRan && profile.has(Signal.Source.COOKIE)) {
            scrubbed.add("non-vanilla cookie handling");
        }

        boolean cookieAnomaly = profile.has(Signal.Source.COOKIE);
        if (scrubbed.size() < SUSPICIOUS || !cookieAnomaly) {
            return List.of();
        }
        return List.of(Signal.of(Signal.Source.NORMALISED, "Privacy or spoofing client",
                String.join(", ", scrubbed), 60));
    }
}
