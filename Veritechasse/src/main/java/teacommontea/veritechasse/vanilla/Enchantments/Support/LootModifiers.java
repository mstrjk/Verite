package teacommontea.veritechasse.vanilla.Enchantments.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Fortune;
import teacommontea.veritechasse.vanilla.Enchantments.Looting;
import teacommontea.veritechasse.vanilla.Enchantments.Lunge;
import teacommontea.veritechasse.vanilla.Enchantments.SweepingEdge;
import teacommontea.veritechasse.vanilla.Era;

public final class LootModifiers {

    private LootModifiers() {
    }

    public static int fortuneLevel(ItemStack tool) {
        return tool == null ? 0 : Fortune.levelOn(tool);
    }

    public static int oreDropsMax(ItemStack tool, int baseCount) {
        return Fortune.oreDropsMax(baseCount, fortuneLevel(tool));
    }

    public static double oreDropsExpected(ItemStack tool, int baseCount) {
        return Fortune.oreDropsExpected(baseCount, fortuneLevel(tool));
    }

    public static int uniformBonusMax(ItemStack tool, int baseCount, int bonusMultiplier) {
        return Fortune.uniformBonusMax(baseCount, fortuneLevel(tool), bonusMultiplier);
    }

    public static double uniformBonusExpected(ItemStack tool, int baseCount, int bonusMultiplier) {
        return Fortune.uniformBonusExpected(baseCount, fortuneLevel(tool), bonusMultiplier);
    }

    public static int lootingLevel(ItemStack weapon) {
        return weapon == null ? 0 : Looting.levelOn(weapon);
    }

    public static int lootBonusMax(ItemStack weapon) {
        return Looting.lootBonusMax(lootingLevel(weapon));
    }

    public static float equipmentDropChanceBonus(ItemStack weapon, Era era) {
        return Looting.equipmentDropChanceBonus(lootingLevel(weapon), era);
    }

    public static int sweepingEdgeLevel(ItemStack weapon) {
        return weapon == null ? 0 : SweepingEdge.levelOn(weapon);
    }

    public static float sweepingDamageRatio(ItemStack weapon, Era era) {
        return SweepingEdge.sweepingDamageRatio(sweepingEdgeLevel(weapon), era);
    }

    public static float sweepDamage(ItemStack weapon, float attackDamage, Era era) {
        return SweepingEdge.sweepDamage(attackDamage, sweepingEdgeLevel(weapon), era);
    }

    public static int lungeLevel(ItemStack weapon) {
        return weapon == null ? 0 : Lunge.levelOn(weapon);
    }

    public static float lungeExhaustion(ItemStack weapon, Era era) {
        return Lunge.exhaustion(lungeLevel(weapon), era);
    }

    public static float lungeImpulseMagnitude(ItemStack weapon, Era era) {
        return Lunge.impulseMagnitude(lungeLevel(weapon), era);
    }

    public static double lungeHorizontalImpulse(ItemStack weapon, Era era) {
        return Lunge.horizontalImpulse(lungeLevel(weapon), era);
    }

    public static boolean lungeApplies(ItemStack weapon, boolean riding) {
        return Lunge.applies(lungeLevel(weapon), riding);
    }
}
