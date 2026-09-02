package teacommontea.veritesauver.invsee;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;


public final class InvSeeCommand implements CommandExecutor, TabCompleter {

    private final boolean ender;

    public InvSeeCommand(boolean ender) {
        this.ender = ender;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        InvSeeFeature feature = InvSeeFeature.get();
        if (feature == null) {
            return true;
        }
        if (ender) {
            feature.endersee(sender, args);
        } else {
            feature.invsee(sender, args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        InvSeeFeature feature = InvSeeFeature.get();
        return feature == null ? List.of() : feature.tab(sender, args);
    }
}
