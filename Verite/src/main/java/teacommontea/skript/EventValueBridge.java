package teacommontea.skript;

import java.util.function.Function;

import org.bukkit.event.Event;


public interface EventValueBridge {

    int TIME_PAST = -1;
    int TIME_NOW = 0;
    int TIME_FUTURE = 1;

    default void init(Object addon) {}

    <E extends Event, V> void register(Class<E> event, Class<V> value, Function<E, V> getter, int time);
}
