package teacommontea.veritechasse.Vanilla.Potions.Tags;

import org.bukkit.entity.EntityType;

import teacommontea.veritechasse.Vanilla.Potions.Support.EffectEra;

public final class IgnoresPoisonAndRegen {

    public static final String KEY = "ignores_poison_and_regen";

    private IgnoresPoisonAndRegen() {
    }

    public static boolean contains(EntityType type, EffectEra era) {
        return Undead.contains(type, era);
    }

    public static boolean contains(String typeName, EffectEra era) {
        return Undead.contains(typeName, era);
    }
}
