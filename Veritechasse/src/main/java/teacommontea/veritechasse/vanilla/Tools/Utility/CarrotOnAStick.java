package teacommontea.veritechasse.Vanilla.Tools.Utility;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Utility.Support.Steering;

public final class CarrotOnAStick {

    public static final String KEY = "carrot_on_a_stick";
    public static final String STEERS = "pig";

    public static final int MAX_DAMAGE = 25;
    public static final int DAMAGE_PER_BOOST = 7;

    public static final boolean ENCHANTABLE = false;
    public static final boolean FIRE_RESISTANT = false;

    private CarrotOnAStick() {
    }

    public static boolean steers(String entityName) {
        return STEERS.equalsIgnoreCase(entityName);
    }

    public static boolean canBoost(String vehicleName, boolean riding, boolean alreadyBoosting) {
        return Steering.canBoost(riding, steers(vehicleName), alreadyBoosting);
    }

    public static int boostDuration(int roll) {
        return Steering.boostDuration(roll);
    }

    public static int maxBoosts() {
        return MAX_DAMAGE / DAMAGE_PER_BOOST;
    }

    public static int remaining(int damage) {
        int left = MAX_DAMAGE - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(int damage) {
        return damage >= MAX_DAMAGE;
    }

    public static boolean convertsOnBreak(Protocol protocol) {
        return Steering.convertsOnBreak(protocol);
    }

    public static String convertsTo() {
        return Steering.CONVERTS_TO;
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(remaining(damage));
    }
}
