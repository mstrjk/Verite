package teacommontea.veritesauver.client;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

final class ChannelProbe {

    private ChannelProbe() {}

    static List<Signal> read(Player p) {
        Set<String> channels;
        try {
            channels = new TreeSet<>(p.getListeningPluginChannels());
        } catch (Throwable t) {
            return List.of();
        }
        List<Signal> out = new ArrayList<>();
        for (String channel : channels) {
            if (ClientSignatures.vanillaChannel(channel)) {
                continue;
            }
            String owner = ClientSignatures.channelOwner(channel);
            if (owner == null) {
                out.add(Signal.of(Signal.Source.UNKNOWN_CHANNEL, channel, channel, 0));
                continue;
            }
            out.add(Signal.of(Signal.Source.CHANNEL, owner, channel, ClientSignatures.channelWeight(channel)));
        }
        return out;
    }
}
