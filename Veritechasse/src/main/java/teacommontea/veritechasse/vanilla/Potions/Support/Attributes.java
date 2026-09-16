package teacommontea.veritechasse.Vanilla.Potions.Support;

public final class Attributes {

    public static final Ranged MOVEMENT_SPEED = new Ranged(0.7D, 0.0D, 1024.0D);
    public static final Ranged ATTACK_SPEED = new Ranged(4.0D, 0.0D, 1024.0D);
    public static final Ranged ATTACK_DAMAGE = new Ranged(2.0D, 0.0D, 2048.0D);
    public static final Ranged MAX_HEALTH = new Ranged(20.0D, 1.0D, 1024.0D);
    public static final Ranged LUCK = new Ranged(0.0D, -1024.0D, 1024.0D);

    public static final Ranged MAX_ABSORPTION = new Ranged(0.0D, 0.0D, 2048.0D);
    public static final Ranged SAFE_FALL_DISTANCE = new Ranged(3.0D, -1024.0D, 1024.0D);
    public static final Ranged FALL_DAMAGE_MULTIPLIER = new Ranged(1.0D, 0.0D, 100.0D);
    public static final Ranged GRAVITY = new Ranged(0.08D, -1.0D, 1.0D);
    public static final Ranged WAYPOINT_TRANSMIT_RANGE = new Ranged(0.0D, 0.0D, 6.0E7D);

    public static final Ranged JUMP_STRENGTH_GENERIC = new Ranged(0.42D, 0.0D, 32.0D);
    public static final Ranged JUMP_STRENGTH_HORSE = new Ranged(0.7D, 0.0D, 2.0D);

    private Attributes() {
    }
}
