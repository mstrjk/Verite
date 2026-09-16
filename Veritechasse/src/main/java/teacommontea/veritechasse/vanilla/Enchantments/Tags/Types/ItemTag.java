package teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types;

import java.util.Locale;
import java.util.Set;

import org.bukkit.Material;

public final class ItemTag {

    private final String key;
    private final Set<String> members;

    public ItemTag(String key, Set<String> members) {
        this.key = key;
        this.members = Set.copyOf(members);
    }

    public static ItemTag of(String key, String... members) {
        return new ItemTag(key, Set.of(members));
    }


    public String key() {
        return this.key;
    }

    public Set<String> members() {
        return this.members;
    }

    public boolean contains(String materialName) {
        return materialName != null && this.members.contains(materialName.toLowerCase(Locale.ROOT));
    }

    public boolean contains(Material material) {
        return material != null && contains(material.name());
    }
}
