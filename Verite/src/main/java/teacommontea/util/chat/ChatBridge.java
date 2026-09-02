package teacommontea.util.chat;

import org.bukkit.entity.Player;

import java.util.Set;


public final class ChatBridge {

    private ChatBridge() {}

    public interface Event {
        Player sender();
        String message();
        void setMessage(String message);
        Set<Player> recipients();
        boolean isCancelled();
        void setCancelled(boolean cancelled);
    }

    public interface Handler {
        void onChat(Event event);
    }
}
