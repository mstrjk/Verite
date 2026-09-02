package teacommontea.veritesauver.invsee;


public final class SpectateResult {

    public enum Reason {
        TARGET_DOES_NOT_EXIST,
        UNKNOWN_TARGET,
        TARGET_EXEMPT,
        OFFLINE_SUPPORT_DISABLED,
        OPEN_CANCELLED,
        UNKNOWN
    }

    private final Object inventory;
    private final Reason reason;

    private SpectateResult(Object inventory, Reason reason) {
        this.inventory = inventory;
        this.reason = reason;
    }

    public static SpectateResult success(Object inventory) {
        return new SpectateResult(inventory, null);
    }

    public static SpectateResult fail(Reason reason) {
        return new SpectateResult(null, reason);
    }

    public boolean isSuccess() {
        return inventory != null;
    }

    public Object inventory() {
        return inventory;
    }

    public Reason reason() {
        return reason;
    }
}
