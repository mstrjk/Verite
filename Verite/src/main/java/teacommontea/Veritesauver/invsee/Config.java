package teacommontea.veritesauver.invsee;

import org.bukkit.configuration.file.FileConfiguration;

import teacommontea.util.Colours;

public final class Config {

    public boolean offlineSupport = true;
    public boolean unknownSupport = true;
    public boolean tabCompleteOffline = true;

    private static final String INVENTORY_TITLE = Colours.INVENTORY_NAME + "<player>'s inventory";
    private static final String ENDER_TITLE = Colours.INVENTORY_NAME + "<player>'s enderchest";

    public static Config from(FileConfiguration cfg) {
        Config c = new Config();
        if (cfg == null) {
            return c;
        }
        c.offlineSupport = cfg.getBoolean("invsee.enable-offline-player-support", c.offlineSupport);
        c.unknownSupport = cfg.getBoolean("invsee.enable-unknown-player-support", c.unknownSupport);
        c.tabCompleteOffline = cfg.getBoolean("invsee.tabcomplete-offline-players", c.tabCompleteOffline);
        return c;
    }

    public String titleFor(boolean ender, String targetName) {
        String raw = ender ? ENDER_TITLE : INVENTORY_TITLE;
        return raw.replace("<player>", targetName == null ? "?" : targetName);
    }

    public Mirror mainMirror() {
        return Mirror.defaultMain();
    }

    public Mirror enderMirror(int size) {
        return Mirror.defaultEnder(size);
    }

    public Palette palette() {
        return Palette.glass();
    }
}
