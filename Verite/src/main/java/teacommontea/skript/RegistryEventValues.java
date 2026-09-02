package teacommontea.skript;

import java.util.function.Function;

import org.bukkit.event.Event;

import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;


public final class RegistryEventValues implements EventValueBridge {

    private SkriptAddon addon;

    @Override
    public void init(Object addon) {
        this.addon = (SkriptAddon) addon;
    }

    @Override
    public <E extends Event, V> void register(Class<E> event, Class<V> value, Function<E, V> getter, int time) {
        EventValueRegistry registry = addon.registry(EventValueRegistry.class);
        registry.register(EventValue.builder(event, value)
            .getter(getter::apply)
            .time(EventValue.Time.of(time))
            .build());
    }
}
