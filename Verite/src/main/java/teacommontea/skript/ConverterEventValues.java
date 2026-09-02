package teacommontea.skript;

import java.util.function.Function;

import org.bukkit.event.Event;

import ch.njol.skript.registrations.EventValues;
import org.skriptlang.skript.lang.converter.Converter;


public final class ConverterEventValues implements EventValueBridge {

    @Override
    public <E extends Event, V> void register(Class<E> event, Class<V> value, Function<E, V> getter, int time) {
        Converter<E, V> converter = getter::apply;
        EventValues.registerEventValue(event, value, converter, time);
    }
}
