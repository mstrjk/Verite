package teacommontea.veritesauver.invsee;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public final class ItemDelta {

    private record Kind(Material material, ItemMeta meta) {
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Kind k)) {
                return false;
            }
            return material == k.material && Objects.equals(meta, k.meta);
        }

        @Override
        public int hashCode() {
            return Objects.hash(material, meta);
        }
    }

    private final Map<Kind, Integer> net = new LinkedHashMap<>();

    private static Kind kindOf(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || stack.getAmount() == 0) {
            return null;
        }
        return new Kind(stack.getType(), stack.hasItemMeta() ? stack.getItemMeta() : null);
    }

    private void add(Kind kind, int amount) {
        if (kind == null || amount == 0) {
            return;
        }
        int sum = net.getOrDefault(kind, 0) + amount;
        if (sum == 0) {
            net.remove(kind);
        } else {
            net.put(kind, sum);
        }
    }

    public boolean isEmpty() {
        return net.isEmpty();
    }

    public void mergeFrom(ItemDelta other) {
        for (Map.Entry<Kind, Integer> e : other.net.entrySet()) {
            add(e.getKey(), e.getValue());
        }
    }

    public static ItemDelta between(List<ItemStack> before, List<ItemStack> after) {
        if (before.size() != after.size()) {
            throw new IllegalArgumentException("mismatched snapshot sizes");
        }
        ItemDelta delta = new ItemDelta();
        for (int i = 0; i < before.size(); i++) {
            ItemStack b = before.get(i);
            ItemStack a = after.get(i);
            if (Objects.equals(b, a)) {
                continue;
            }
            Kind ak = kindOf(a);
            Kind bk = kindOf(b);
            if (ak != null) {
                delta.add(ak, a.getAmount());
            }
            if (bk != null) {
                delta.add(bk, -b.getAmount());
            }
        }
        return delta;
    }

    public String changes() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Kind, Integer> e : net.entrySet()) {
            int v = e.getValue();
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(v < 0 ? "Removed " : "Added ")
              .append(describe(e.getKey()))
              .append(" (").append(Math.abs(v)).append(")");
        }
        return sb.toString();
    }

    private static String describe(Kind kind) {
        String name = titleCase(kind.material().name());
        return kind.meta() == null ? name : name + " & " + kind.meta();
    }

    private static String titleCase(String materialName) {
        String[] parts = materialName.toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
