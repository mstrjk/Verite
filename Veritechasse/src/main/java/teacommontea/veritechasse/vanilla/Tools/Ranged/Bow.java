package teacommontea.veritechasse.vanilla.Tools.Ranged;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.Support.DamageModifiers;
import teacommontea.veritechasse.vanilla.Enchantments.Support.KnockbackModifiers;
import teacommontea.veritechasse.vanilla.Enchantments.Support.PostAttackEffects;
import teacommontea.veritechasse.vanilla.Enchantments.Support.RangedModifiers;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Ranged.Support.ProjectileSpread;

public final class Bow {

    public static final String KEY = "bow";

    public static final int MAX_DAMAGE = 384;
    public static final int ENCHANTMENT_VALUE = 1;
    public static final int DAMAGE_PER_SHOT = 1;

    public static final int MAX_DRAW_DURATION_TICKS = 20;
    public static final int USE_DURATION_TICKS = 72000;
    public static final int DEFAULT_PROJECTILE_RANGE = 15;

    public static final float VELOCITY_MULTIPLIER = 3.0F;
    public static final float MINIMUM_POWER = 0.1F;
    public static final float FULL_POWER = 1.0F;
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

    private Bow() {
    }

    public static float powerForTime(int timeHeld) {
        float pow = (float) timeHeld / (float) MAX_DRAW_DURATION_TICKS;
        pow = (pow * pow + pow * 2.0F) / 3.0F;
        return pow > FULL_POWER ? FULL_POWER : pow;
    }

    public static boolean releases(int timeHeld) {
        return powerForTime(timeHeld) >= MINIMUM_POWER;
    }

    public static boolean isCritical(int timeHeld) {
        return powerForTime(timeHeld) == FULL_POWER;
    }

    public static float velocity(int timeHeld) {
        return powerForTime(timeHeld) * VELOCITY_MULTIPLIER;
    }

    public static float maxVelocity() {
        return VELOCITY_MULTIPLIER;
    }

    public static int ticksForFullDraw() {
        return MAX_DRAW_DURATION_TICKS;
    }

    public static int minimumDrawTicks() {
        int ticks = 0;
        while (ticks <= MAX_DRAW_DURATION_TICKS && powerForTime(ticks) < MINIMUM_POWER) {
            ticks = ticks + 1;
        }
        return ticks;
    }

    public static float[] shotAngles(int projectileCount, float spreadDegrees) {
        return ProjectileSpread.angles(projectileCount, spreadDegrees);
    }

    public static float arrowDamage(ItemStack bow, EntityType target, float baseArrowDamage, Era era) {
        return DamageModifiers.modifyProjectileDamage(bow, baseArrowDamage, era);
    }

    public static float arrowKnockback(ItemStack bow, float baseKnockback, Era era) {
        return KnockbackModifiers.modifyProjectileKnockback(bow, baseKnockback, era);
    }

    public static int igniteTicks(ItemStack bow) {
        return PostAttackEffects.projectileIgniteTicks(bow);
    }

    public static boolean consumesArrow(ItemStack bow, String ammoItem) {
        return RangedModifiers.consumesAmmo(bow, ammoItem);
    }

    public static int piercingCount(ItemStack bow, Era era) {
        return RangedModifiers.piercingCount(bow, era);
    }

    public static Reality damageFrom(ItemStack bow, EntityType target, float baseArrowDamage, int timeHeld, Era era) {
        float scaled = arrowDamage(bow, target, baseArrowDamage, era) * powerForTime(timeHeld);
        return Reality.of(scaled);
    }

    public static Reality velocityFrom(int timeHeld) {
        return Reality.of(velocity(timeHeld));
    }

    public static Reality maxVelocityReality() {
        return Reality.of(VELOCITY_MULTIPLIER);
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
