package teacommontea.skript;

import java.util.function.Function;

import org.bukkit.event.Event;

import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;


public final class GetterEventValues implements EventValueBridge {

    @Override
    public <E extends Event, V> void register(Class<E> event, Class<V> value, Function<E, V> getter, int time) {
        EventValues.registerEventValue(event, value, new Getter<V, E>() {
            @Override
            public V get(E e) {
                return getter.apply(e);
            }
        }, time);
    }
}
