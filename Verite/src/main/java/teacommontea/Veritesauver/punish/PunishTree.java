package teacommontea.veritesauver.punish;

import teacommontea.util.Colours;
import teacommontea.veritesauver.util.SauverFormat;
import teacommontea.veritesauver.util.SauverMessages;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.util.SauverConfig;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverEngine;

public final class PunishTree implements Listener, InventoryHolder {

    private static final int SIZE = 27;
    private static final int CONTENT_START = 9;
    private static final int CONTENT_SLOTS = 9;
    private static final int BACK_SLOT = 18;
    private static final int NEXT_SLOT = 26;

    private static final String STORE_SCOPE = "punishtree";

    private final Sauver sauver;

    public PunishTree(Sauver sauver) {
        this.sauver = sauver;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    private SauverMessages msg() {
        return sauver.messages();
    }

    private enum Page { PLAYERS, CATEGORIES, CONFIRM }

    private final class View implements InventoryHolder {
        Inventory inventory;
        Page page;
        int pageIndex;
        UUID target;
        String targetName;
        String category;
        boolean fromPlayerPage;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public void open(Player staff, UUID target, String targetName) {
        SauverTree tree = SauverTree.load();
        if (!tree.enabled()) {
            msg().err(staff, Colours.WARNING + "The punishment tree is disabled.");
            return;
        }
        View v = new View();
        if (target != null) {
            v.target = target;
            v.targetName = targetName;
            v.fromPlayerPage = false;
            openCategories(staff, v, 0, tree);
        } else {
            openPlayers(staff, v, 0);
        }
    }

    private void openPlayers(Player staff, View v, int pageIndex) {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.removeIf(p -> p.getUniqueId().equals(staff.getUniqueId()));
        online.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        int pages = pageCount(online.size());
        pageIndex = Math.max(0, Math.min(pageIndex, pages - 1));

        Inventory inv = Bukkit.createInventory(v, SIZE, title("Select a player"));
        v.inventory = inv;
        v.page = Page.PLAYERS;
        v.pageIndex = pageIndex;

        frame(inv);
        int from = pageFrom(pageIndex, online.size());
        int to = Math.min(from + CONTENT_SLOTS, online.size());
        int slot = CONTENT_START;
        for (int i = from; i < to; i++) {
            inv.setItem(slot++, playerHead(online.get(i)));
        }
        nav(inv, pageIndex, pages, false);
        staff.openInventory(inv);
    }

    private void openCategories(Player staff, View v, int pageIndex, SauverTree tree) {
        List<SauverTree.Category> allowed = allowedCategories(staff, tree);
        if (allowed.isEmpty()) {
            msg().err(staff, Colours.WARNING + "You have no punishment categories available.");
            staff.closeInventory();
            return;
        }
        int pages = pageCount(allowed.size());
        pageIndex = Math.max(0, Math.min(pageIndex, pages - 1));

        Inventory inv = Bukkit.createInventory(v, SIZE, title("Punish " + v.targetName));
        v.inventory = inv;
        v.page = Page.CATEGORIES;
        v.pageIndex = pageIndex;

        frame(inv);
        int from = pageFrom(pageIndex, allowed.size());
        int to = Math.min(from + CONTENT_SLOTS, allowed.size());
        int slot = CONTENT_START;
        for (int i = from; i < to; i++) {
            inv.setItem(slot++, categoryIcon(v.target, allowed.get(i), tree));
        }
        boolean canBack = v.fromPlayerPage;
        nav(inv, pageIndex, pages, canBack);
        staff.openInventory(inv);
    }

    private void openConfirm(Player staff, View v, SauverTree tree) {
        SauverTree.Category cat = tree.category(v.category);
        if (cat == null) {
            openCategories(staff, v, 0, tree);
            return;
        }
        SauverTree.Step step = cat.stepFor(offences(v.target, v.category));

        Inventory inv = Bukkit.createInventory(v, SIZE, title("Punish " + v.targetName));
        v.inventory = inv;
        v.page = Page.CONFIRM;

        frame(inv);
        inv.setItem(11, named(Material.RED_CONCRETE, Colours.WARNING + "Cancel",
                List.of(Colours.BRAND_ACCENT + "Close without punishing")));
        inv.setItem(13, named(Material.LIME_CONCRETE, Colours.SUCCESS + "Confirm",
                List.of(Colours.BRAND_ACCENT + "Apply " + Colours.WARNING + describe(step) + " " + Colours.BRAND_ACCENT + "to " + Colours.BRAND_ACCENT_SECONDARY + v.targetName,
                        Colours.BRAND_ACCENT + "Category: " + Colours.BRAND_ACCENT_SECONDARY + pretty(v.category))));
        inv.setItem(15, named(Material.LIGHT_BLUE_CONCRETE, Colours.BRAND + "Manual",
                List.of(Colours.BRAND_ACCENT + "Choose the punishment yourself")));
        nav(inv, 0, 1, true);
        staff.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player staff)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof View v)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() != v.inventory) {
            return;
        }
        int slot = event.getRawSlot();
        SauverTree tree = SauverTree.load();

        switch (v.page) {
            case PLAYERS -> handlePlayersClick(staff, v, slot, tree);
            case CATEGORIES -> handleCategoriesClick(staff, v, slot, tree);
            case CONFIRM -> handleConfirmClick(staff, v, slot, tree);
        }
    }

    private void handlePlayersClick(Player staff, View v, int slot, SauverTree tree) {
        if (slot == BACK_SLOT) {
            staff.closeInventory();
            return;
        }
        if (slot == NEXT_SLOT) {
            if (v.pageIndex + 1 < pageCount(playerCount(staff))) {
                openPlayers(staff, v, v.pageIndex + 1);
            }
            return;
        }
        int index = contentIndex(v, slot, playerCount(staff));
        if (index < 0) {
            return;
        }
        ItemStack it = v.inventory.getItem(slot);
        if (it == null || it.getType() != Material.PLAYER_HEAD) {
            return;
        }
        if (!(it.getItemMeta() instanceof SkullMeta sm) || sm.getOwningPlayer() == null) {
            return;
        }
        OfflinePlayer owner = sm.getOwningPlayer();
        v.target = owner.getUniqueId();
        v.targetName = owner.getName() != null ? owner.getName() : "player";
        v.fromPlayerPage = true;
        openCategories(staff, v, 0, tree);
    }

    private void handleCategoriesClick(Player staff, View v, int slot, SauverTree tree) {
        List<SauverTree.Category> allowed = allowedCategories(staff, tree);
        if (slot == BACK_SLOT) {
            if (v.pageIndex > 0) {
                openCategories(staff, v, v.pageIndex - 1, tree);
            } else if (v.fromPlayerPage) {
                openPlayers(staff, v, 0);
            } else {
                staff.closeInventory();
            }
            return;
        }
        if (slot == NEXT_SLOT) {
            if (v.pageIndex + 1 < pageCount(allowed.size())) {
                openCategories(staff, v, v.pageIndex + 1, tree);
            }
            return;
        }
        int index = contentIndex(v, slot, allowed.size());
        if (index < 0 || index >= allowed.size()) {
            return;
        }
        v.category = allowed.get(index).name();
        openConfirm(staff, v, tree);
    }

    private void handleConfirmClick(Player staff, View v, int slot, SauverTree tree) {
        switch (slot) {
            case 11 -> staff.closeInventory();
            case 13 -> approve(staff, v, tree);
            case 15 -> manual(staff, v, tree);
            case BACK_SLOT -> openCategories(staff, v, 0, tree);
            default -> { }
        }
    }

    private int contentIndex(View v, int slot, int total) {
        if (slot < CONTENT_START || slot >= CONTENT_START + CONTENT_SLOTS) {
            return -1;
        }
        return pageFrom(v.pageIndex, total) + (slot - CONTENT_START);
    }

    private int playerCount(Player staff) {
        int n = Bukkit.getOnlinePlayers().size();
        return Bukkit.getOnlinePlayers().contains(staff) ? n - 1 : n;
    }

    private static int pageCount(int items) {
        return Math.max(1, (int) Math.ceil(items / (double) CONTENT_SLOTS));
    }

    private static int pageFrom(int pageIndex, int total) {
        int from = pageIndex * CONTENT_SLOTS;
        if (from + CONTENT_SLOTS > total) {
            from = Math.max(0, total - CONTENT_SLOTS);
        }
        return from;
    }

    private void approve(Player staff, View v, SauverTree tree) {
        SauverTree.Category cat = tree.category(v.category);
        if (cat == null) {
            staff.closeInventory();
            return;
        }
        int prior = offences(v.target, v.category);
        SauverTree.Step step = cat.stepFor(prior);
        String reason = pretty(v.category);
        String targetName = v.targetName;
        UUID target = v.target;

        String block = SauverExempt.blockReason(staff, target, entryType(step.type()));
        if (block != null) {
            msg().err(staff, Colours.WARNING + block);
            staff.closeInventory();
            return;
        }

        SauverEngine.Result r = apply(staff, step, target, targetName, reason);
        if (r == null) {
            msg().err(staff, Colours.WARNING + "That punishment could not be applied.");
            staff.closeInventory();
            return;
        }
        if (!r.ok()) {
            msg().err(staff, Colours.WARNING + r.error());
            staff.closeInventory();
            return;
        }
        setOffences(target, v.category, prior + 1);
        msg().send(staff, Colours.BRAND + "Punished " + Colours.BRAND_ACCENT_SECONDARY + targetName + " " + Colours.BRAND + "for " + Colours.BRAND_ACCENT_SECONDARY
                + pretty(v.category) + " " + Colours.BRAND_ACCENT + "(" + Colours.WARNING + describe(step) + Colours.BRAND_ACCENT + ")");
        staff.closeInventory();
    }

    private void manual(Player staff, View v, SauverTree tree) {
        if (SauverConfig.treeUseFields() && atLeast_1_21_7() && openFields(staff, v)) {
            return;
        }
        manualChat(staff, v);
    }

    private void manualChat(Player staff, View v) {
        staff.closeInventory();
        String t = v.targetName;
        String prompt = Colours.BRAND + "Choose the punishment for " + Colours.BRAND_ACCENT_SECONDARY + t + Colours.BRAND + ":<newline>"
                + manualButton(Colours.WARN + "Warn", "run_command", "/warn " + t,
                        Colours.BRAND_ACCENT_SECONDARY + "Warn " + Colours.WARN + t + " " + Colours.BRAND_ACCENT_SECONDARY + "now") + " "
                + manualButton(Colours.MUTE + "Mute", "suggest_command", "/mute " + t + " [<days>] [<reason>]",
                        Colours.BRAND_ACCENT_SECONDARY + "Prefill a mute for " + Colours.MUTE + t + Colours.BRAND_ACCENT_SECONDARY + ". " + Colours.BRAND_ACCENT + "Leave days blank for permanent.") + " "
                + manualButton(Colours.WARNING + "Kick", "run_command", "/kick " + t,
                        Colours.BRAND_ACCENT_SECONDARY + "Kick " + Colours.WARNING + t + " " + Colours.BRAND_ACCENT_SECONDARY + "now") + " "
                + manualButton(Colours.DANGER + "Ban", "suggest_command", "/ban " + t + " [<days>] [<reason>]",
                        Colours.BRAND_ACCENT_SECONDARY + "Prefill a ban for " + Colours.DANGER + t + Colours.BRAND_ACCENT_SECONDARY + ". " + Colours.BRAND_ACCENT + "Leave days blank for permanent.");
        msg().send(staff, prompt);
    }

    private String manualButton(String label, String action, String command, String hover) {
        return Colours.BRAND_ACCENT + "[<hover:show_text:'" + hover + "'><click:" + action + ":'" + command + "'>"
                + label + "</click></hover>" + Colours.BRAND_ACCENT + "]";
    }

    private boolean openFields(Player staff, View v) {
        staff.closeInventory();
        return new PunishDialog(sauver).open(staff, v.target, v.targetName, v.category);
    }

    private static boolean atLeast_1_21_7() {
        try {
            String raw = Bukkit.getMinecraftVersion();
            String[] parts = raw.split("[^0-9]+");
            int major = parts.length > 0 && !parts[0].isEmpty() ? Integer.parseInt(parts[0]) : 0;
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            if (major != 1) {
                return major > 1;
            }
            if (minor != 21) {
                return minor > 21;
            }
            return patch >= 7;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Entry.Type entryType(SauverTree.StepType type) {
        return switch (type) {
            case WARN -> Entry.Type.WARNING;
            case KICK -> Entry.Type.KICK;
            case MUTE -> Entry.Type.MUTE;
            case BAN -> Entry.Type.BAN;
        };
    }

    private SauverEngine.Result apply(Player staff, SauverTree.Step step, UUID target,
                                      String targetName, String reason) {
        UUID execUuid = staff.getUniqueId();
        String execName = staff.getName();
        return switch (step.type()) {
            case WARN -> SauverEngine.warn(target, targetName, reason, execUuid, execName, false);
            case KICK -> SauverEngine.kick(target, targetName, reason, execUuid, execName, false);
            case MUTE -> SauverEngine.issue(Entry.Type.MUTE, target, null, targetName, reason,
                    execUuid, execName, step.durationMillis(), false, false);
            case BAN -> SauverEngine.issue(Entry.Type.BAN, target, null, targetName, reason,
                    execUuid, execName, step.durationMillis(), false, false);
        };
    }

    private int offences(UUID target, String category) {
        return sauver.store().scope(STORE_SCOPE).getInt(offKey(target, category), 0);
    }

    private void setOffences(UUID target, String category, int value) {
        sauver.store().scope(STORE_SCOPE).set(offKey(target, category), value);
    }

    private static String offKey(UUID target, String category) {
        return "off." + target + "." + category;
    }

    public boolean canOpen(Player staff) {
        if (staff.hasPermission("veritesauver.tree")
                || staff.hasPermission("veritesauver.tree.all")
                || staff.hasPermission("veritesauver.admin")) {
            return true;
        }
        return !allowedCategories(staff, SauverTree.load()).isEmpty();
    }

    private List<SauverTree.Category> allowedCategories(Player staff, SauverTree tree) {
        List<SauverTree.Category> out = new ArrayList<>();
        if (staff.hasPermission("veritesauver.tree.all") || staff.hasPermission("veritesauver.admin")) {
            out.addAll(tree.categories().values());
            return out;
        }
        String group = primaryGroup(staff);
        List<String> names = group == null ? List.of() : tree.permissions().getOrDefault(group, List.of());
        for (String name : names) {
            SauverTree.Category c = tree.category(name);
            if (c != null && !out.contains(c)) {
                out.add(c);
            }
        }
        return out;
    }

    private static String primaryGroup(Player p) {
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = provider.getMethod("get").invoke(null);
            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, p.getUniqueId());
            if (user == null) {
                return null;
            }
            String primary = (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
            return primary == null ? null : primary.toLowerCase(Locale.ROOT);
        } catch (Throwable t) {
            return null;
        }
    }

    private ItemStack playerHead(Player p) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta sm) {
            sm.setOwningPlayer(p);
            teacommontea.util.text.Text.itemName(sm, Colours.BRAND_ACCENT_SECONDARY + p.getName());
            teacommontea.util.text.Text.itemLore(sm, List.of(Colours.BRAND_ACCENT + "Click to punish this player"));
            sm.addItemFlags(org.bukkit.inventory.ItemFlag.values());
            head.setItemMeta(sm);
        }
        return head;
    }

    private ItemStack categoryIcon(UUID target, SauverTree.Category cat, SauverTree tree) {
        SauverTree.Step next = cat.stepFor(offences(target, cat.name()));
        List<String> lore = new ArrayList<>();
        lore.add(Colours.BRAND_ACCENT + "Next: " + Colours.WARNING + describe(next));
        lore.add(Colours.BRAND_ACCENT + "Offences so far: " + Colours.BRAND_ACCENT_SECONDARY + offences(target, cat.name()));
        lore.add(Colours.BRAND_ACCENT + "Ladder:");
        for (SauverTree.Step s : cat.ladder()) {
            lore.add(Colours.BRAND_ACCENT + " - " + Colours.BRAND_ACCENT_SECONDARY + describe(s));
        }
        return named(cat.icon(), Colours.BRAND_ACCENT_SECONDARY + pretty(cat.name()), lore);
    }

    private void frame(Inventory inv) {
        ItemStack glass = named(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < CONTENT_START; i++) {
            inv.setItem(i, glass);
        }
        for (int i = CONTENT_START + CONTENT_SLOTS; i < SIZE; i++) {
            inv.setItem(i, glass);
        }
    }

    private void nav(Inventory inv, int pageIndex, int pages, boolean canBack) {
        if (pageIndex > 0 || canBack) {
            inv.setItem(BACK_SLOT, turtleArrow(Colours.BRAND + "Back"));
        } else {
            inv.setItem(BACK_SLOT, named(Material.BARRIER, Colours.WARNING + "<bold>CLOSE", List.of()));
        }
        if (pageIndex + 1 < pages) {
            inv.setItem(NEXT_SLOT, turtleArrow(Colours.BRAND + "Next"));
        }
    }

    private ItemStack turtleArrow(String name) {
        ItemStack it = new ItemStack(Material.TIPPED_ARROW);
        if (it.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta pm) {
            pm.setBasePotionData(new org.bukkit.potion.PotionData(org.bukkit.potion.PotionType.TURTLE_MASTER));
            teacommontea.util.text.Text.itemName(pm, name);
            pm.addItemFlags(org.bukkit.inventory.ItemFlag.values());
            it.setItemMeta(pm);
        }
        return it;
    }

    private String title(String body) {
        return teacommontea.util.text.Text.toLegacy(Colours.INVENTORY_NAME + body);
    }

    private ItemStack named(Material material, String name, List<String> lore) {
        ItemStack it = new ItemStack(material);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            teacommontea.util.text.Text.itemName(meta, name);
            if (!lore.isEmpty()) {
                teacommontea.util.text.Text.itemLore(meta, lore);
            }
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.values());
            it.setItemMeta(meta);
        }
        return it;
    }

    private static String pretty(String category) {
        String base = category.replace('.', ' ').replace('_', ' ');
        String[] words = base.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    private static String describe(SauverTree.Step step) {
        if (step == null) {
            return "nothing";
        }
        return switch (step.type()) {
            case WARN -> "Warn";
            case KICK -> "Kick";
            case MUTE -> step.permanent() ? "Permanent mute" : "Mute " + SauverFormat.fancyTime(step.durationMillis());
            case BAN -> step.permanent() ? "Permanent ban" : "Ban " + SauverFormat.fancyTime(step.durationMillis());
        };
    }
}
