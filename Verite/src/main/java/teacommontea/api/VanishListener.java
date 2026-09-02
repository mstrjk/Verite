package teacommontea.api;

import java.util.UUID;


public interface VanishListener {

    default void vanished(UUID player) {}

    default void unvanished(UUID player) {}
}
