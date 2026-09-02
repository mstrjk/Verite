package teacommontea.veritevoiler;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import teacommontea.util.Scope;

final class VanishEffectStore {

    private static final String SCOPE = "vanish_effects";

    private final Scope scope;

    VanishEffectStore(teacommontea.util.KvStore store) {
        this.scope = store.scope(SCOPE);
    }

    void snapshot(Player p, PotionEffectType[] managed) {
        StringBuilder sb = new StringBuilder();
        for (PotionEffectType type : managed) {
            if (type == null) continue;
            PotionEffect e = p.getPotionEffect(type);
            if (e == null) continue;
            if (sb.length() > 0) sb.append(';');
            sb.append(type.getName()).append('=')
              .append(e.getAmplifier()).append(',')
              .append(e.getDuration()).append(',')
              .append(e.isAmbient() ? 1 : 0).append(',')
              .append(e.hasParticles() ? 1 : 0).append(',')
              .append(e.hasIcon() ? 1 : 0);
        }
        scope.set(p.getUniqueId().toString(), sb.toString());
    }

    boolean restore(Player p, PotionEffectType[] managed) {
        String blob = scope.getString(p.getUniqueId().toString(), null);
        scope.delete(p.getUniqueId().toString());
        if (blob == null) return false;
        for (Saved s : parse(blob)) {
            PotionEffectType type = PotionEffectType.getByName(s.type);
            if (type == null) continue;

            int duration = s.duration;
            if (duration == 0) continue;
            p.addPotionEffect(new PotionEffect(type, duration, s.amplifier, s.ambient, s.particles, s.icon));
        }
        return true;
    }

    void clear(Player p) {
        scope.delete(p.getUniqueId().toString());
    }

    boolean has(UUID id) {
        return scope.getString(id.toString(), null) != null;
    }

    private static List<Saved> parse(String blob) {
        List<Saved> out = new ArrayList<>();
        if (blob.isEmpty()) return out;
        for (String seg : blob.split(";")) {
            int eq = seg.indexOf('=');
            if (eq <= 0) continue;
            String name = seg.substring(0, eq);
            String[] parts = seg.substring(eq + 1).split(",");
            if (parts.length < 5) continue;
            try {
                out.add(new Saved(name,
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        parts[2].equals("1"),
                        parts[3].equals("1"),
                        parts[4].equals("1")));
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    private static final class Saved {
        final String type;
        final int amplifier;
        final int duration;
        final boolean ambient;
        final boolean particles;
        final boolean icon;
        Saved(String type, int amplifier, int duration, boolean ambient, boolean particles, boolean icon) {
            this.type = type;
            this.amplifier = amplifier;
            this.duration = duration;
            this.ambient = ambient;
            this.particles = particles;
            this.icon = icon;
        }
    }
}
