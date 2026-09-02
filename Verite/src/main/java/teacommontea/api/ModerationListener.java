package teacommontea.api;


public interface ModerationListener {

    default void entryAdded(Punishment punishment) {}

    default void entryRemoved(Punishment punishment) {}

    default void broadcastSent(String message, String target) {}
}
