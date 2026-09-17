package teacommontea.veritechasse.Reality;

public final class Violation {

    public static final String FAMILY_SEPARATOR = "_";

    public static final String ENGINE = "engine";
    public static final String HANDWRITTEN = "handwritten";
    public static final String AGREED = "agreed";

    public static final double ENGINE_WEIGHT = 1.0D;
    public static final double HANDWRITTEN_WEIGHT = 2.0D;
    public static final double AGREEMENT_MULTIPLIER = 2.0D;

    private final String key;
    private final String title;
    private final String description;
    private final String origin;
    private final String detail;
    private final double weight;

    private Violation(
            String key,
            String title,
            String description,
            String origin,
            String detail,
            double weight) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.origin = origin;
        this.detail = detail;
        this.weight = weight;
    }

    public static Violation engine(Check check, String detail) {
        return new Violation(
            check.key(),
            check.title(),
            check.description(),
            ENGINE,
            detail,
            ENGINE_WEIGHT);
    }

    public static Violation handwritten(Check check, String detail) {
        return new Violation(
            check.key(),
            check.title(),
            check.description(),
            HANDWRITTEN,
            detail,
            HANDWRITTEN_WEIGHT);
    }

    public Violation agreedWith(Violation other) {
        double combined = (this.weight + other.weight) * AGREEMENT_MULTIPLIER;
        return new Violation(
            this.key,
            this.title,
            this.description,
            AGREED,
            this.detail + "; " + other.detail,
            combined);
    }

    public String family() {
        int separator = this.key.indexOf(FAMILY_SEPARATOR);
        return separator < 0 ? this.key : this.key.substring(0, separator);
    }

    public String key() {
        return this.key;
    }

    public String title() {
        return this.title;
    }

    public String description() {
        return this.description;
    }

    public String origin() {
        return this.origin;
    }

    public String detail() {
        return this.detail;
    }

    public double weight() {
        return this.weight;
    }

    public boolean isEngine() {
        return ENGINE.equals(this.origin);
    }

    public boolean isHandwritten() {
        return HANDWRITTEN.equals(this.origin);
    }

    public boolean isAgreed() {
        return AGREED.equals(this.origin);
    }

    @Override
    public String toString() {
        return this.title + " (" + this.origin + " +" + this.weight + ") " + this.detail;
    }
}
