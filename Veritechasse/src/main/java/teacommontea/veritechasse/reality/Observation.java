package teacommontea.veritechasse.Reality;

public final class Observation {

    public static final String CONTEXT = "context";
    public static final String NOTE = "note";
    public static final String SUSPECT = "suspect";

    private final String severity;
    private final String label;
    private final String detail;

    private Observation(String severity, String label, String detail) {
        this.severity = severity;
        this.label = label;
        this.detail = detail;
    }

    public static Observation context(String detail) {
        return new Observation(CONTEXT, "state", detail);
    }

    public static Observation note(String label, String detail) {
        return new Observation(NOTE, label, detail);
    }

    public static Observation suspect(String label, String detail) {
        return new Observation(SUSPECT, label, detail);
    }

    public String severity() {
        return this.severity;
    }

    public String label() {
        return this.label;
    }

    public String detail() {
        return this.detail;
    }

    public boolean isContext() {
        return CONTEXT.equals(this.severity);
    }

    public boolean isSuspect() {
        return SUSPECT.equals(this.severity);
    }

    public boolean isNote() {
        return NOTE.equals(this.severity);
    }

    @Override
    public String toString() {
        return "[" + this.label + "] " + this.detail;
    }
}
