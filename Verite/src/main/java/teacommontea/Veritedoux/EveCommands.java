package teacommontea.veritedoux;

import org.bukkit.plugin.Plugin;
import teacommontea.veritedoux.preprocess.EveLexicon;
import teacommontea.veritedoux.preprocess.EveLang;
import teacommontea.veritedoux.preprocess.EveSegment;
import teacommontea.veritedoux.preprocess.EveRegister;
import teacommontea.veritedoux.preprocess.EveScam;

public final class EveCommands {

    private EveCommands() {}

    public static void loadAll(Plugin plugin) {
        EveLexicon.load(plugin);
        EveSegment.load(plugin);
        EveLang.load(plugin);
        EveRegister.load(plugin);
        EveScam.load(plugin);
        EveEntry.load(plugin);
    }
}
