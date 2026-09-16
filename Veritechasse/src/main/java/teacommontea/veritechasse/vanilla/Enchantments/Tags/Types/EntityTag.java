package teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types;

import java.util.Locale;
import java.util.Set;

import org.bukkit.entity.EntityType;

public final class EntityTag {

    private final String key;
    private final Set<String> members;

    public EntityTag(String key, Set<String> members) {
        this.key = key;
        this.members = Set.copyOf(members);
    }

    public static EntityTag of(String key, String... members) {
        return new EntityTag(key, Set.of(members));
    }


    public String key() {
        return this.key;
    }

    public Set<String> members() {
        return this.members;
    }

    public boolean contains(String typeName) {
        return typeName != null && this.members.contains(typeName.toLowerCase(Locale.ROOT));
    }

    public boolean contains(EntityType type) {
        return type != null && contains(type.name());
    }
}
