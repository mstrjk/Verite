package teacommontea.veritechasse.Vanilla.PlayerArmour.Shields.Support;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class ShieldDisable {

    public static final float LEGACY_BASE_CHANCE = 0.25F;
    public static final float LEGACY_EFFICIENCY_SCALE = 0.05F;
    public static final float LEGACY_SPRINT_BONUS = 0.75F;
    public static final int LEGACY_COOLDOWN_TICKS = 100;

    public static final float AXE_DISABLES_BLOCKING_FOR_SECONDS = 5.0F;
    public static final int GUARANTEED_COOLDOWN_TICKS = 100;

    public static final int GUARANTEED_PROTOCOL_MAJOR = 1;
    public static final int GUARANTEED_PROTOCOL_MINOR = 20;
    public static final int GUARANTEED_PROTOCOL_PATCH = 6;

    public static final int WEAPON_COMPONENT_PROTOCOL_MAJOR = 1;
    public static final int WEAPON_COMPONENT_PROTOCOL_MINOR = 21;
    public static final int WEAPON_COMPONENT_PROTOCOL_PATCH = 5;

    private ShieldDisable() {
    }

    public static boolean isGuaranteed(Protocol protocol) {
        return protocol.atLeast(
            GUARANTEED_PROTOCOL_MAJOR,
            GUARANTEED_PROTOCOL_MINOR,
            GUARANTEED_PROTOCOL_PATCH);
    }

    public static boolean usesWeaponComponent(Protocol protocol) {
        return protocol.atLeast(
            WEAPON_COMPONENT_PROTOCOL_MAJOR,
            WEAPON_COMPONENT_PROTOCOL_MINOR,
            WEAPON_COMPONENT_PROTOCOL_PATCH);
    }

    public static float legacyChance(int efficiencyLevel, boolean sprinting) {
        float chance = LEGACY_BASE_CHANCE + efficiencyLevel * LEGACY_EFFICIENCY_SCALE;
        if (sprinting) {
            chance = chance + LEGACY_SPRINT_BONUS;
        }
        return chance > 1.0F ? 1.0F : chance;
    }

    public static float chance(int efficiencyLevel, boolean sprinting, Protocol protocol) {
        if (isGuaranteed(protocol)) {
            return 1.0F;
        }
        return legacyChance(efficiencyLevel, sprinting);
    }

    public static int cooldownTicks(Protocol protocol) {
        if (usesWeaponComponent(protocol)) {
            return (int) (AXE_DISABLES_BLOCKING_FOR_SECONDS * 20.0F);
        }
        return LEGACY_COOLDOWN_TICKS;
    }

    public static int cooldownTicks(float disableBlockingForSeconds) {
        return (int) (disableBlockingForSeconds * 20.0F);
    }

    public static boolean disablesBlocking(float disableBlockingForSeconds) {
        return disableBlockingForSeconds > 0.0F;
    }

    public static Reality cooldownFrom(Protocol protocol) {
        return Reality.of(cooldownTicks(protocol));
    }

    public static Reality chanceFrom(int efficiencyLevel, boolean sprinting, Protocol protocol) {
        return Reality.of(chance(efficiencyLevel, sprinting, protocol));
    }
}
