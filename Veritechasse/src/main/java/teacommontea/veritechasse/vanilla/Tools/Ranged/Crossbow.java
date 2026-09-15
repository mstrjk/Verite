package teacommontea.veritechasse.vanilla.Tools.Ranged;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.Support.RangedModifiers;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Ranged.Support.ProjectileSpread;

public final class Crossbow {

    public static final String KEY = "crossbow";

    public static final int MAX_DAMAGE = 465;
    public static final int ENCHANTMENT_VALUE = 1;
    public static final int DAMAGE_PER_ARROW_SHOT = 1;
    public static final int DAMAGE_PER_FIREWORK_SHOT = 3;

    public static final float MAX_CHARGE_SECONDS = 1.25F;
    public static final int MAX_CHARGE_TICKS = 25;
    public static final int DEFAULT_PROJECTILE_RANGE = 8;

    public static final float ARROW_POWER = 3.15F;
    public static final float FIREWORK_POWER = 1.6F;
    public static final float MOB_ARROW_POWER = 1.6F;

    public static final float START_SOUND_PERCENT = 0.2F;
    public static final float MID_SOUND_PERCENT = 0.5F;

    public static final float FULL_CHARGE = 1.0F;
    public static final float BASE_UNCERTAINTY = 1.0F;

    public static final boolean ENCHANTABLE = true;
    public static final boolean FIRE_RESISTANT = false;

    public static final boolean USE_PERMITS_SPRINTING = false;
    public static final float USE_SPEED_MULTIPLIER = 0.2F;

    public static boolean slowsWhileUsed() {
        return USE_SPEED_MULTIPLIER < 1.0F;
    }

    public static float useSpeedMultiplier() {
        return USE_SPEED_MULTIPLIER;
    }

    public static boolean permitsSprintingWhileUsed() {
        return USE_PERMITS_SPRINTING;
    }

    private Crossbow() {
    }

    public static int chargeDurationTicks(float chargeTimeSeconds) {
        return (int) Math.floor((double) (chargeTimeSeconds * 20.0F));
    }

    public static int chargeDurationTicks() {
        return chargeDurationTicks(MAX_CHARGE_SECONDS);
    }

    public static float powerForTime(int timeHeld, int chargeDurationTicks) {
        if (chargeDurationTicks <= 0) {
            return FULL_CHARGE;
        }
        float pow = (float) timeHeld / (float) chargeDurationTicks;
        return pow > FULL_CHARGE ? FULL_CHARGE : pow;
    }

    public static boolean fullyCharged(int timeHeld, int chargeDurationTicks) {
        return powerForTime(timeHeld, chargeDurationTicks) >= FULL_CHARGE;
    }

    public static boolean loads(int timeHeld, int chargeDurationTicks, boolean alreadyCharged) {
        return !alreadyCharged && fullyCharged(timeHeld, chargeDurationTicks);
    }

    public static float shootingPower(boolean containsFirework) {
        return containsFirework ? FIREWORK_POWER : ARROW_POWER;
    }

    public static int damagePerShot(boolean containsFirework) {
        return containsFirework ? DAMAGE_PER_FIREWORK_SHOT : DAMAGE_PER_ARROW_SHOT;
    }

    public static int projectileCount(int multishotLevel) {
        if (multishotLevel <= 0) {
            return 1;
        }
        return 2 * multishotLevel + 1;
    }

    public static float spreadDegrees(int multishotLevel) {
        if (multishotLevel <= 0) {
            return 0.0F;
        }
        return 10.0F * (float) multishotLevel;
    }

    public static float[] shotAngles(int multishotLevel) {
        return ProjectileSpread.angles(projectileCount(multishotLevel), spreadDegrees(multishotLevel));
    }

    public static float widestAngle(int multishotLevel) {
        return ProjectileSpread.widestAngle(projectileCount(multishotLevel), spreadDegrees(multishotLevel));
    }

    public static int resolvedProjectileCount(ItemStack crossbow, Era era) {
        return RangedModifiers.projectileCount(crossbow, era);
    }

    public static float resolvedSpread(ItemStack crossbow, Era era) {
        return RangedModifiers.projectileSpread(crossbow, era);
    }

    public static float[] resolvedShotAngles(ItemStack crossbow, Era era) {
        return ProjectileSpread.angles(resolvedProjectileCount(crossbow, era), resolvedSpread(crossbow, era));
    }

    public static int resolvedChargeDurationTicks(ItemStack crossbow, Era era) {
        return RangedModifiers.chargeDurationTicks(crossbow, MAX_CHARGE_SECONDS, era);
    }

    public static int resolvedPiercingCount(ItemStack crossbow, Era era) {
        return RangedModifiers.piercingCount(crossbow, era);
    }

    public static int resolvedDurabilityCost(ItemStack crossbow, boolean containsFirework, Era era) {
        return damagePerShot(containsFirework) * resolvedProjectileCount(crossbow, era);
    }

    public static Reality velocityFrom(boolean containsFirework) {
        return Reality.of(shootingPower(containsFirework));
    }

    public static int remaining(int damage) {
        int left = MAX_DAMAGE - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(int damage) {
        return damage >= MAX_DAMAGE;
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(remaining(damage));
    }
}
