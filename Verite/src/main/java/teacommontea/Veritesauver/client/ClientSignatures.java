package teacommontea.veritesauver.client;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientSignatures {

    private ClientSignatures() {}

    public record Probe(String key, String token, String owner, int weight, boolean keybind) {}

    public static final List<Probe> KEYBINDS = List.of(
            new Probe("optifine.zoom", "of.key.zoom", "OptiFine", 40, true),
            new Probe("sodium.options", "key.sodium.options", "Sodium", 35, true),
            new Probe("iris.reload", "key.iris.reload", "Iris Shaders", 35, true),
            new Probe("meteor.gui", "key.meteor-client.open-gui", "Meteor Client", 85, true),
            new Probe("meteor.commands", "key.meteor-client.open-commands", "Meteor Client", 85, true),
            new Probe("wurst.zoom", "key.wurst.zoom", "Wurst", 85, true),
            new Probe("impact.clickgui", "key.impact.clickgui", "Impact", 80, true),
            new Probe("baritone.toggle", "key.baritone.toggle", "Baritone", 65, true),
            new Probe("baritone.cancel", "key.baritone.cancel", "Baritone", 65, true),
            new Probe("baritone.path", "key.baritone.pathToggle", "Baritone", 65, true),
            new Probe("replaymod.next", "key.replaymod.keyframe_next", "ReplayMod", 30, true),
            new Probe("xaero.minimap.zoom", "key.xaero_zoom_in", "Xaero's Minimap", 35, true),
            new Probe("xaero.worldmap.toggle", "key.xaero.toggle_world_map", "Xaero's World Map", 35, true),
            new Probe("journeymap.map", "journeymap.key.map", "JourneyMap", 35, true),
            new Probe("journeymap.zoom", "key.journeymap.zoom_in", "JourneyMap", 30, true),
            new Probe("litematica.render", "litematica.hotkey.name.toggle_all_rendering", "Litematica", 40, true),
            new Probe("tweakeroo.fly", "tweakeroo.hotkey.name.toggle_fly", "Tweakeroo", 45, true),
            new Probe("tweakeroo.freecamera", "tweakeroo.feature_toggle.name.tweakfreecamera", "Tweakeroo", 45, true),
            new Probe("freecam.toggle", "key.freecam.toggle", "Freecam", 65, true),
            new Probe("zergatul.freecam", "key.zergatul.freecam.toggle", "Zergatul Freecam", 70, true),
            new Probe("wynntils.menu", "key.wynntils.open_wynntils_menu", "Wynntils", 35, true),
            new Probe("essential.friends", "key.essential.open_friends", "Essential", 30, true),
            new Probe("killaura.fabric", "key.killaura", "KillAura Fabric", 85, true),
            new Probe("chestesp.toggle", "key.chestesp.toggle", "ChestESP", 80, true),
            new Probe("autofish.gui", "key.autofish.open_gui", "AutoFish", 55, true),
            new Probe("lumina.gui", "key.lumina.open_click_gui", "Lumina", 85, true),
            new Probe("autoswitch.toggle", "key.autoswitch.toggle", "AutoSwitch", 60, true),
            new Probe("xray.toggle", "xray.config.toggle", "Advanced XRay", 80, true),
            new Probe("xray.open", "xray.config.open", "Advanced XRay", 80, true),
            new Probe("xray.fabric.enable", "keybinding.enable_xray", "Advanced XRay Fabric", 80, true),
            new Probe("xray.fabric.gui", "keybinding.open_gui", "Advanced XRay Fabric", 75, true),
            new Probe("doabarrelroll.toggle", "key.do_a_barrel_roll.toggle_enabled", "Do a Barrel Roll", 35, true),
            new Probe("voxelmap.menu", "key.minimap.voxelmapmenu", "VoxelMap", 35, true),
            new Probe("healthindicators.render", "key.healthindicators.renderingEnabled", "Health Indicators", 40, true),
            new Probe("quickhotkeys.totem", "key.glitchifyed.quick_hotkeys.equip_totem", "Quick Hotkeys", 55, true),
            new Probe("climbladdersfast.toggle", "key.climbladdersfast.toggle", "Climb Ladders Fast", 50, true),
            new Probe("autoattack.afk", "key.autoattack.afkAttack", "AutoAttack", 60, true),
            new Probe("invutils.autotool", "key.p1x3lc0w.invutil.autoTool", "InvUtils", 50, true),
            new Probe("craftingtweaks.rotate", "key.craftingtweaks.rotate", "Crafting Tweaks", 35, true),
            new Probe("inventorytabs.next", "inventorytabs.key.next_tab", "Inventory Tabs", 35, true),
            new Probe("stepup.toggle", "key.stepup.toggle", "StepUp", 50, true),
            new Probe("preciseplacing.toggle", "key.preciseblockplacing.toggle_enabled", "Precise Block Placing", 50, true),
            new Probe("quickturn.turn", "key.vekquickturn.quickturn", "Quick Turn", 40, true),
            new Probe("slotswap.swap", "key.slotswap.swap", "Slot Swap", 45, true),
            new Probe("playerhighlighter.toggle", "key.playerhighlighter.toggle", "Player Highlighter", 50, true),
            new Probe("easierchests.sort", "key.easierchests.sortchest", "Easier Chests", 35, true),
            new Probe("vivecraft.leftclick", "vivecraft.key.guiLeftClick", "Vivecraft", 30, true),
            new Probe("itemswapper.switcher", "key.itemswapper.itemswitcher", "ItemSwapper", 40, true),
            new Probe("autotools.tool", "key.autotools.get_tool", "AutoTools", 55, true),
            new Probe("ecs.swap", "key.ecs.swap", "Elytra Chestplate Swapper", 45, true),
            new Probe("autoclicker.rinf", "key.autoclicker-rinf.toggleacf", "AutoClicker RINF", 75, true),
            new Probe("autoclicker.p1k0chu", "key.auto-clicker_.toggle", "Auto Clicker", 75, true),
            new Probe("flighthelper.lock", "key.flighthelper.lockup", "Flight Helper", 55, true),
            new Probe("hypnotic.gui", "key.hypnotic-client.open-click-gui", "Hypnotic Client", 85, true),
            new Probe("entityoutliner.selector", "key.entity-outliner.selector", "Entity Outliner", 50, true),
            new Probe("mousewheelie.config", "key.mousewheelie.open_config_screen", "Mouse Wheelie", 35, true),
            new Probe("healthbarplus.options", "key.healthbarplus.options", "HealthBar Plus", 40, true),
            new Probe("antighost.reveal", "key.antighost.reveal", "AntiGhost", 45, true),
            new Probe("invmove.toggle", "keybind.invmove.toggleMove", "InvMove", 55, true),
            new Probe("flymod.toggle", "key.flymod.toggle", "Fly Mod", 65, true),
            new Probe("autototem.toggle", "key.autototem.toggle", "AutoTotem", 70, true),
            new Probe("smartoffhand.totem", "key.smartoffhand.totem_swap", "SmartOffhand", 65, true),
            new Probe("antiafk.toggle", "key.antiafk.toggle", "AntiAFK", 55, true),
            new Probe("glazed.activate", "key.glazed.activate-key", "Glazed", 70, true),
            new Probe("nochatreports.toggle", "nochatreports.key.toggle", "No Chat Reports", 35, true),
            new Probe("nochatreports.command", "key.nochatreports.cmd", "No Chat Reports", 35, true),
            new Probe("opsec.toggle", "key.opsec.toggle", "OpSec", 55, true),
            new Probe("freelook.menu", "freelook.key.menu", "FreeLook", 45, true),
            new Probe("neat.toggle", "neat.keybind.toggle", "Neat", 35, true),
            new Probe("crawl.toggle", "key.crawl", "Crawl", 40, true),
            new Probe("inventoryessentials.bulkdrop", "key.inventoryessentials.bulk_drop", "Inventory Essentials", 40, true),
            new Probe("quickswap.swap", "key.SwapInventory.QAQ", "QuickSwap", 45, true),
            new Probe("noteblockyoinker.record", "key.record.desc", "Noteblock Music Yoinker", 30, true)
    );

    public static final List<Probe> TRANSLATIONS = List.of(
            new Probe("sodium.quality", "sodium.options.pages.quality", "Sodium", 35, false),
            new Probe("sodium.impact", "sodium.option_impact.low", "Sodium", 35, false),
            new Probe("iris.options", "options.iris.shaderPackSelection", "Iris Shaders", 35, false),
            new Probe("meteor.name", "meteor-client.name", "Meteor Client", 85, false),
            new Probe("fabric.api", "fabric-api.resource.reload", "Fabric API", 25, false),
            new Probe("modmenu.title", "modmenu.title", "Mod Menu", 30, false),
            new Probe("lunar.title", "lunar.title", "Lunar Client", 45, false),
            new Probe("feather.title", "feather.title", "Feather Client", 45, false),
            new Probe("badlion.title", "badlion.title", "Badlion Client", 45, false),
            new Probe("forge.mods", "fml.menu.mods", "Forge or NeoForge", 30, false),
            new Probe("neoforge.mods", "neoforge.menu.mods", "NeoForge", 35, false),
            new Probe("liquidbounce.killaura", "liquidbounce.module.killaura.name", "LiquidBounce", 90, false),
            new Probe("liquidbounce.enabled", "liquidbounce.generic.enabled", "LiquidBounce", 90, false),
            new Probe("bleachhack.killaura", "bleachhack.module.killaura", "BleachHack", 90, false),
            new Probe("aristois.killaura", "emc.module.killaura.name", "Aristois", 90, false),
            new Probe("coffee.killaura", "coffee.module.killaura.name", "Coffee Client", 90, false),
            new Probe("doomsday.killaura", "doomsday.module.killaura.name", "Doomsday Client", 90, false),
            new Probe("impact.killaura", "impact.module.killaura.name", "Impact", 85, false),
            new Probe("inertia.killaura", "inertia.module.killaura.name", "Inertia", 85, false),
            new Probe("kamiblue.killaura", "kami.module.killaura.name", "Kami Blue", 85, false),
            new Probe("rusherhack.killaura", "rusherhack.module.killaura.name", "RusherHack", 90, false),
            new Probe("lambda.killaura", "lambda.module.killaura.name", "Lambda", 85, false),
            new Probe("worlddownloader.startstop", "key.wdl.startStop", "World Downloader", 70, false),
            new Probe("autoclicker.fabric.holding", "autoclicker-fabric.hud.holding", "AutoClicker Fabric", 75, false),
            new Probe("tweakeroo.tab", "tweakeroo.config.tab.tweaks", "Tweakeroo", 45, false),
            new Probe("tweakeroo.snapaim", "tweakeroo.config.comment.snapaimyawstep", "Tweakeroo", 45, false),
            new Probe("tweakermore.category", "tweakermore.hotkeys.category.main", "TweakerMore", 45, false),
            new Probe("itemscroller.generic", "itemscroller.config.tab.generic", "Item Scroller", 40, false),
            new Probe("itemscroller.gui", "itemscroller.gui.button.config_gui.generic", "Item Scroller", 40, false),
            new Probe("malilib.on", "malilib.on", "MaLiLib", 30, false),
            new Probe("litematica.config", "litematica.gui.button.config", "Litematica", 40, false),
            new Probe("litematica.mainmenu", "litematica.gui.button.change_menu.to_main_menu", "Litematica", 40, false),
            new Probe("inventoryprofiles.title", "inventoryprofiles.gui.config.title", "Inventory Profiles Next", 40, false),
            new Probe("inventoryprofiles.tweaks", "inventoryprofiles.gui.config.Tweaks", "Inventory Profiles", 40, false),
            new Probe("viafabric.protocol", "gui.protocol_version_field.name", "ViaFabric", 30, false),
            new Probe("viafabricplus.on", "base.viafabricplus.on", "ViaFabricPlus", 30, false),
            new Probe("seedcracker.enabled", "cracker.enabled", "SeedCrackerX", 80, false),
            new Probe("nametagtweaks.settings", "nametagtweaks.key.open_settings", "NameTagTweaks", 35, false),
            new Probe("nametagtweaks.name", "nametag-tweaks.nametag-tweaks", "NameTag Tweaks", 35, false),
            new Probe("healthindicators.category", "key.categories.healthindicators", "Health Indicators", 40, false),
            new Probe("betternamevisibility.toggle", "key.name_visibility.toggleNames", "Better Name Visibility", 40, false),
            new Probe("journeymap.automap", "jm.advanced.automappoll", "JourneyMap", 35, false),
            new Probe("voxelmap.north", "minimap.ui.north", "VoxelMap", 35, false),
            new Probe("accurateplacement.category", "key.category.accurateblockplacement.category", "Accurate Block Placement", 55, false),
            new Probe("accurateplacement.toggle", "net.clayborn.accurateblockplacement.togglevanillaplacement", "Accurate Block Placement", 55, false),
            new Probe("jade.modmenu", "modmenu.nameTranslation.jade", "Jade", 35, false),
            new Probe("invmove.config", "config.invmove.title", "InvMove", 50, false),
            new Probe("autoclicker.category", "key.category.autoclicker.keybinding-title", "AutoClicker", 70, false),
            new Probe("xray.name", "xray.mod_name", "Advanced XRay", 80, false),
            new Probe("xray.ate47", "x13.mod.xray", "X-Ray by ATE47", 75, false),
            new Probe("cheatutils.category", "key.category.cheatutils.common", "CheatUtils", 80, false),
            new Probe("bridgingmod.config", "config.bridgingmod.title", "Bridging Mod", 45, false),
            new Probe("shouldersurfing.camera", "shouldersurfing.configuration.camera", "Shoulder Surfing", 35, false),
            new Probe("seethroughlava.title", "text.autoconfig.seethroughlava.title", "See Through Lava", 45, false),
            new Probe("torohealth.red", "config.torohealth.red", "ToroHealth", 35, false),
            new Probe("antitoolbreak.category", "category.antitoolbreak", "Anti Tool Break", 45, false),
            new Probe("inventorymanagement.dark", "inventorymanagement.resourcepack.dark", "Inventory Management", 35, false),
            new Probe("moremousetweaks.matching", "option.moremousetweaks.matching", "More Mouse Tweaks", 35, false),
            new Probe("jex.name", "jex.name", "Jex Client", 80, false),
            new Probe("steppy.title", "text.autoconfig.steppy.title", "Steppy", 45, false),
            new Probe("thunderhack.gui", "descriptions.client.thundergui", "ThunderHack", 85, false),
            new Probe("inventoryessentials.title", "config.inventoryessentials.title", "Inventory Essentials", 40, false),
            new Probe("elytrapitch.title", "text.autoconfig.elytrapitch.title", "ElytraPitch", 40, false),
            new Probe("doabarrelroll.config", "config.do_a_barrel_roll.title", "Do a Barrel Roll", 35, false),
            new Probe("hitrange.name", "hitrange.name", "HitRange", 60, false),
            new Probe("wynnspell.category", "key.category.wynncraft-spell-caster.spell", "Wynncraft Spell Caster", 45, false),
            new Probe("yesstevemodel.category", "key.category.yes_steve_model", "Yes Steve Model", 30, false),
            new Probe("pvputils.settings", "key.pvp_utils.open_settings", "PVP Utils", 55, false),
            new Probe("bettermaceswap.category", "key.category.bettermaceswap.general", "Better Mace Swap", 60, false),
            new Probe("pearlwind.category", "key.category.pearlwind_chargeanalysis.category", "Pearl/Wind Charge Analysis", 55, false),
            new Probe("carpet.rule", "carpet.rule.carpets.name", "Carpet", 30, false),
            new Probe("reachdisplay.title", "reach_display.midnightconfig.title", "Reach Display", 45, false),
            new Probe("skyboxify.title", "options.skyboxify.title", "Skyboxify", 30, false),
            new Probe("shulkerboxtooltip.contains", "container.shulkerbox.contains", "Shulker Box Tooltip", 30, false),
            new Probe("customcrosshair.category", "key.category.custom_crosshair_mod.key_bindings", "Custom Crosshair Mod", 35, false),
            new Probe("gammautils.category", "key.category.gammautils.gamma", "Gamma Utils", 35, false),
            new Probe("inventoryhud.category", "key.inventoryhud.category", "Inventory HUD", 30, false),
            new Probe("macrokeybinds.title", "text.globalmacros.title", "Macro/Global Macros", 55, false),
            new Probe("jsmacros.close", "jsmacrosce.close", "JsMacros", 60, false)
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
            Map.entry("syncmatica:syncmatica", "Syncmatica"),
            Map.entry("servux:litematics", "Litematica"),
            Map.entry("servux:tweaks", "Tweakeroo"),
            Map.entry("servux:structures", "MiniHUD / Servux"),
            Map.entry("servux:hud_metadata", "MiniHUD / Servux"),
            Map.entry("minecraft:explosiveenhancement", "Explosive Enhancement"),
            Map.entry("minecraft:fancymenu_packet_bridge", "FancyMenu")
    );

    private static final Map<String, String> CHANNEL_PREFIX_OWNERS = Map.ofEntries(
            Map.entry("xaeroworldmap:", "Xaero's World Map"),
            Map.entry("xaerominimap:", "Xaero's Minimap"),
            Map.entry("jade:", "Jade"),
            Map.entry("journeymap:", "JourneyMap"),
            Map.entry("voicechat:", "Simple Voice Chat"),
            Map.entry("noxesium-v2:", "Noxesium"),
            Map.entry("flashback:", "Flashback"),
            Map.entry("appleskin:", "AppleSkin"),
            Map.entry("bindcmd:", "BindCmd"),
            Map.entry("showmeyourskin:", "Show Me Your Skin"),
            Map.entry("fabric-screen-handler-api-v1:", "Fabric Screen Handler API"),
            Map.entry("cardinal-components:", "Cardinal Components API"),
            Map.entry("architectury:", "Architectury API"),
            Map.entry("forgeconfigapiport:", "Forge Config API Port"),
            Map.entry("clientsort:", "Client Sort"),
            Map.entry("roughlyenoughitems:", "Roughly Enough Items"),
            Map.entry("craftingtweaks:", "Crafting Tweaks"),
            Map.entry("do_a_barrel_roll:", "Do a Barrel Roll"),
            Map.entry("controlify:", "Controlify"),
            Map.entry("distant_horizons:", "Distant Horizons"),
            Map.entry("creativecore:", "CreativeCore"),
            Map.entry("fzzy_config:", "Fzzy Config"),
            Map.entry("plasmo:", "Plasmo Voice"),
            Map.entry("replaymod:", "ReplayMod"),
            Map.entry("replayvoicechat:", "Replay Voice Chat"),
            Map.entry("vivecraft:", "Vivecraft"),
            Map.entry("axiom:", "Axiom"),
            Map.entry("puzzleslib:", "Puzzles Lib"),
            Map.entry("pvaddonflashback:", "PV Addon Flashback"),
            Map.entry("pickupnotifier:", "Pick Up Notifier"),
            Map.entry("owo:", "owo-lib"),
            Map.entry("xaerolib:", "XaeroLib"),
            Map.entry("midnightcontrols:", "MidnightControls"),
            Map.entry("horseexpert:", "Horse Expert"),
            Map.entry("waystones:", "Waystones"),
            Map.entry("balm:", "Balm"),
            Map.entry("geckolib:", "GeckoLib"),
            Map.entry("subtle_effects:", "Subtle Effects"),
            Map.entry("cpm_net:", "Custom Player Models"),
            Map.entry("animatium:", "Animatium"),
            Map.entry("shulkerboxtooltip:", "Shulker Box Tooltip"),
            Map.entry("skinshuffle:", "Skin Shuffle"),
            Map.entry("hotbarapi:", "Hotbar API"),
            Map.entry("betterstats:", "Better Statistics Screen"),
            Map.entry("inventoryessentials:", "Inventory Essentials"),
            Map.entry("wildfire_gender:", "Wildfire's Female Gender Mod"),
            Map.entry("statuemenus:", "Statue Menus"),
            Map.entry("strawstatues:", "Straw Statues"),
            Map.entry("libjf-config-network-v0:", "LibJF Config"),
            Map.entry("libgui:", "LibGui"),
            Map.entry("patpat:", "PatPat"),
            Map.entry("improvedmapcolors:", "Improved Map Colors"),
            Map.entry("redstonetools:", "Redstone Tools"),
            Map.entry("tcdcommons:", "TCDCommons"),
            Map.entry("effectinsights:", "Effect Insights"),
            Map.entry("tooltipinsights:", "Tooltip Insights"),
            Map.entry("mocap:", "Mocap"),
            Map.entry("chiseled-bookshelf-visualizer:", "Chiseled Bookshelf Visualizer"),
            Map.entry("bookshelfinspector:", "Chiseled Bookshelf Visualizer"),
            Map.entry("cr-compass-ribbon:", "Compass Ribbon"),
            Map.entry("lunarclient:", "Lunar Client"),
            Map.entry("lunar:", "Lunar Client"),
            Map.entry("apollo:", "Lunar Client"),
            Map.entry("badlion:", "Badlion Client"),
            Map.entry("feather:", "Feather Client"),
            Map.entry("essential:", "Essential"),
            Map.entry("norisk:", "NoRisk Client"),
            Map.entry("minimapsync:", "Minimap Sync"),
            Map.entry("freecam:", "Freecam"),
            Map.entry("voxelmap:", "VoxelMap"),
            Map.entry("replay:", "Replay / recording mod"),
            Map.entry("baritone:", "Baritone"),
            Map.entry("autototem:", "AutoTotem"),
            Map.entry("tweakeroo:", "Tweakeroo")
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
        for (Map.Entry<String, String> e : CHANNEL_PREFIX_OWNERS.entrySet()) {
            if (key.startsWith(e.getKey())) {
                return e.getValue();
            }
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
        for (Map.Entry<String, String> e : CHANNEL_PREFIX_OWNERS.entrySet()) {
            if (key.startsWith(e.getKey())) {
                return 45;
            }
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
