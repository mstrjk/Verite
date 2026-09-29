package teacommontea.veritesauver.client;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientSignatures {

    private ClientSignatures() {}

    public record Probe(String key, String token, String owner, int weight, boolean keybind) {}

    public static final List<Probe> KEYBINDS = List.of(
            new Probe("optifine", "of.key.zoom", "OptiFine", 40, true),
            new Probe("sodium", "key.sodium.options", "Sodium", 35, true),
            new Probe("iris", "key.iris.reload", "Iris Shaders", 35, true),
            new Probe("meteor", "key.meteor-client.open-gui", "Meteor Client", 80, true),
            new Probe("wurst", "key.wurst.zoom", "Wurst", 80, true),
            new Probe("impact", "key.impact.clickgui", "Impact", 80, true),
            new Probe("baritone", "key.baritone.toggle", "Baritone", 60, true),
            new Probe("replaymod", "key.replaymod.keyframe_next", "ReplayMod", 30, true),
            new Probe("xaero.minimap", "key.xaero_zoom_in", "Xaero's Minimap", 30, true),
            new Probe("journeymap", "key.journeymap.zoom_in", "JourneyMap", 30, true),
            new Probe("litematica", "litematica.hotkey.name.toggle_all_rendering", "Litematica", 35, true),
            new Probe("tweakeroo", "tweakeroo.hotkey.name.toggle_fly", "Tweakeroo", 45, true),
            new Probe("freecam", "key.freecam.toggle", "Freecam", 55, true),
            new Probe("wynntils", "key.wynntils.open_wynntils_menu", "Wynntils", 35, true),
            new Probe("essential", "key.essential.open_friends", "Essential", 30, true)
    );

    public static final List<Probe> TRANSLATIONS = List.of(
            new Probe("sodium.opts", "sodium.options.pages.quality", "Sodium", 35, false),
            new Probe("iris.opts", "options.iris.shaderPackSelection", "Iris Shaders", 35, false),
            new Probe("meteor.name", "meteor-client.name", "Meteor Client", 80, false),
            new Probe("fabric.api", "fabric-api.resource.reload", "Fabric API", 25, false),
            new Probe("modmenu", "modmenu.title", "Mod Menu", 30, false),
            new Probe("lunar", "lunar.title", "Lunar Client", 45, false),
            new Probe("feather", "feather.title", "Feather Client", 45, false),
            new Probe("badlion", "badlion.title", "Badlion Client", 45, false),
            new Probe("forge", "fml.menu.mods", "Forge or NeoForge", 30, false),
            new Probe("neoforge", "neoforge.menu.mods", "NeoForge", 35, false)
    );

    public static final List<Probe> KNOWN_PACKS = List.of(
            new Probe("fabric.api.pack", "fabric:fabric-api", "Fabric API", 45, false),
            new Probe("fabric.resource.pack", "fabric-resource-loader-v0:fabric-resource-loader-v0", "Fabric Resource Loader", 45, false),
            new Probe("sodium.pack", "sodium:sodium", "Sodium", 40, false),
            new Probe("iris.pack", "iris:iris", "Iris Shaders", 40, false),
            new Probe("meteor.pack", "meteor-client:meteor-client", "Meteor Client", 85, false),
            new Probe("baritone.pack", "baritone:baritone", "Baritone", 65, false),
            new Probe("litematica.pack", "litematica:litematica", "Litematica", 40, false),
            new Probe("wurst.pack", "wurst:wurst", "Wurst", 85, false),
            new Probe("essential.pack", "essential:essential", "Essential", 35, false),
            new Probe("modmenu.pack", "modmenu:modmenu", "Mod Menu", 35, false)
    );

    private static final Map<String, String> CHANNEL_OWNERS = Map.ofEntries(
            Map.entry("lunar:apollo", "Lunar Client"),
            Map.entry("labymod3:main", "LabyMod"),
            Map.entry("labymod:neo", "LabyMod"),
            Map.entry("feather:client", "Feather Client"),
            Map.entry("badlion:mods", "Badlion Client"),
            Map.entry("cosmetica:cosmetica", "Cosmetica"),
            Map.entry("wdl:init", "World Downloader"),
            Map.entry("xaerominimap:main", "Xaero's Minimap"),
            Map.entry("worldinfo:world_info", "Xaero's Minimap"),
            Map.entry("journeymap:client", "JourneyMap"),
            Map.entry("essential:essential", "Essential"),
            Map.entry("wynntils:version", "Wynntils"),
            Map.entry("replaymod:settings", "ReplayMod"),
            Map.entry("bobby:hello", "Bobby"),
            Map.entry("syncmatica:syncmatica", "Syncmatica")
    );

    private static final Map<String, String> LOADER_PREFIXES = Map.ofEntries(
            Map.entry("fabric:", "Fabric"),
            Map.entry("fabric-", "Fabric"),
            Map.entry("fml:", "Forge"),
            Map.entry("forge:", "Forge"),
            Map.entry("neoforge:", "NeoForge"),
            Map.entry("quilt:", "Quilt"),
            Map.entry("c:", "Common mod API")
    );

    public static String brandOwner(String brand) {
        if (brand == null || brand.isBlank()) {
            return null;
        }
        String key = brand.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> e : BRAND_OWNERS.entrySet()) {
            if (key.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return teacommontea.util.text.Text.capitalise(brand);
    }

    private static final Map<String, String> BRAND_OWNERS = Map.ofEntries(
            Map.entry("fabric", "Fabric"),
            Map.entry("quilt", "Quilt"),
            Map.entry("neoforge", "NeoForge"),
            Map.entry("forge", "Forge"),
            Map.entry("lunar", "Lunar Client"),
            Map.entry("feather", "Feather Client"),
            Map.entry("labymod", "LabyMod"),
            Map.entry("badlion", "Badlion Client"),
            Map.entry("pojav", "PojavLauncher")
    );

    public static String channelOwner(String channel) {
        if (channel == null) {
            return null;
        }
        String key = channel.toLowerCase(Locale.ROOT);
        String direct = CHANNEL_OWNERS.get(key);
        if (direct != null) {
            return direct;
        }
        for (Map.Entry<String, String> e : LOADER_PREFIXES.entrySet()) {
            if (key.startsWith(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }

    public static int channelWeight(String channel) {
        if (channel == null) {
            return 0;
        }
        String key = channel.toLowerCase(Locale.ROOT);
        if (CHANNEL_OWNERS.containsKey(key)) {
            return 70;
        }
        return 25;
    }

    private static final java.util.Set<String> INFRASTRUCTURE = java.util.Set.of(
            "bungeecord", "bungeecord:main", "velocity:main", "floodgate", "geyser",
            "queueup", "vv", "viaversion", "papi", "placeholderapi", "luckperms",
            "worldedit", "plan", "tab", "packetevents", "skinsrestorer", "libertybans");

    public static boolean vanillaChannel(String channel) {
        if (channel == null) {
            return true;
        }
        String key = channel.toLowerCase(Locale.ROOT);
        if (key.equals("minecraft:register") || key.equals("minecraft:unregister")
                || key.equals("minecraft:brand")) {
            return true;
        }
        String namespace = key.indexOf(':') > 0 ? key.substring(0, key.indexOf(':')) : key;
        return INFRASTRUCTURE.contains(key) || INFRASTRUCTURE.contains(namespace);
    }
}
