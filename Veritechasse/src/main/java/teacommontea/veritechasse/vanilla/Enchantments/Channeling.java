package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableTrident;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetRiptide;

public final class Channeling {

    public static final String KEY = "channeling";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableTrident.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetRiptide.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(25.0F, 0.0F),
        new LinearValue(50.0F, 0.0F),
        8);

    public static final String REQUIRED_BLOCK_TAG = "lightning_rods";

    private Channeling() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static boolean strikesOnBlockHit(int level, boolean thundering, boolean hitLightningRod, boolean canSeeSky) {
        return level > 0 && thundering && hitLightningRod && canSeeSky;
    }

    public static boolean strikesOnEntityHit(int level, boolean thundering, boolean victimCanSeeSky) {
        return level > 0 && thundering && victimCanSeeSky;
    }
}
