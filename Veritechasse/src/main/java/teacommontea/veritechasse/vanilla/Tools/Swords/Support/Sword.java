package teacommontea.veritechasse.vanilla.Tools.Swords.Support;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Support.Durability;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.PlayerBase;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.vanilla.Tools.Tags.SwordEfficient;
import teacommontea.veritechasse.vanilla.Tools.Tags.SwordInstantlyMines;

public final class Sword {

    public static final float ATTACK_DAMAGE_BASELINE = 3.0F;
    public static final float ATTACK_SPEED_BASELINE = -2.4F;

    public static final float COBWEB_SPEED = 15.0F;
    public static final float EFFICIENT_SPEED = 1.5F;
    public static final float DEFAULT_SPEED = 1.0F;
    public static final float INSTANT_SPEED = Float.MAX_VALUE;

    public static final int DAMAGE_PER_ATTACK = 1;
    public static final int DAMAGE_PER_BLOCK = 2;

    public static final String COBWEB = "cobweb";

    public static final boolean CAN_DESTROY_BLOCKS_IN_CREATIVE = false;

    private Sword() {
    }

    public static boolean miningIsTagDriven(Protocol protocol) {
        return protocol.atLeast(1, 21, 3);
    }

    public static double attackDamage(Material material) {
        return PlayerBase.ATTACK_DAMAGE + (double) ATTACK_DAMAGE_BASELINE + (double) material.attackDamageBonus();
    }

    public static double attackSpeed() {
        return PlayerBase.ATTACK_SPEED + (double) ATTACK_SPEED_BASELINE;
    }

    public static double attackCooldownTicks() {
        return 20.0D / attackSpeed();
    }

    public static float destroySpeed(String blockName, boolean isPlantLike, ToolEra era) {
        if (blockName == null) {
            return DEFAULT_SPEED;
        }
        if (COBWEB.equalsIgnoreCase(blockName)) {
            return COBWEB_SPEED;
        }
        if (SwordInstantlyMines.contains(blockName, era)) {
            return INSTANT_SPEED;
        }
        if (isPlantLike || SwordEfficient.contains(blockName, era)) {
            return EFFICIENT_SPEED;
        }
        return DEFAULT_SPEED;
    }

    public static boolean correctForDrops(String blockName) {
        return COBWEB.equalsIgnoreCase(blockName);
    }

    public static int maxDamage(Material material) {
        return Durability.maxDamage(material);
    }

    public static Reality damageFrom(Material material) {
        return Reality.of(attackDamage(material));
    }
}
