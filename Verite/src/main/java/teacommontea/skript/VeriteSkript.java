package teacommontea.skript;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Version;

import teacommontea.api.ModerationListener;
import teacommontea.api.Punishment;
import teacommontea.api.VeriteModeration;

public final class VeriteSkript {

    private static final String ERA_GETTER = "teacommontea.skript.GetterEventValues";
    private static final String ERA_CONVERTER = "teacommontea.skript.ConverterEventValues";
    private static final String ERA_REGISTRY = "teacommontea.skript.RegistryEventValues";

    private final SkriptAddon addon;
    private final EventValueBridge eventValues;

    private VeriteSkript(SkriptAddon addon, EventValueBridge eventValues) {
        this.addon = addon;
        this.eventValues = eventValues;
    }

    public static VeriteSkript enable(JavaPlugin plugin) {
        SkriptAddon addon;
        try {
            addon = Skript.registerAddon(plugin);
        } catch (Throwable t) {
            plugin.getLogger().warning("Skript present but addon registration was refused (Skript syntax off): "
                    + t.getMessage());
            return null;
        }

        EventValueBridge bridge = selectBridge(plugin);
        if (bridge == null) {
            return null;
        }
        bridge.init(addon);

        VeriteSkript instance = new VeriteSkript(addon, bridge);
        try {
            instance.loadSyntax("teacommontea.skript", "elements");
            instance.registerPunishment(plugin);
        } catch (Throwable t) {
            plugin.getLogger().warning("Skript syntax failed to load (Skript syntax off): " + t.getMessage());
            return null;
        }
        return instance;
    }

    private void registerPunishment(JavaPlugin plugin) {
        try {
            Classes.registerClass(new ClassInfo<>(Punishment.class, "veritepunishment")
                    .user("(verite ?)?punishments?")
                    .name("Verite Punishment"));
        } catch (Throwable t) {
            plugin.getLogger().warning("Skript: failed to register Punishment type ("
                    + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
        }

        Skript.registerEvent("Verite Punishment", SimpleEvent.class, VeritePunishmentEvent.class,
                "verite punishment");

        try {
            eventValues.register(VeritePunishmentEvent.class, Punishment.class,
                    VeritePunishmentEvent::punishment, EventValueBridge.TIME_NOW);
        } catch (Throwable t) {
            plugin.getLogger().warning("Skript: failed to register event-punishment value ("
                    + t.getClass().getSimpleName() + ": " + t.getMessage() + ")");
        }

        VeriteModeration.registerListener(new ModerationListener() {
            @Override
            public void entryAdded(Punishment punishment) {
                fire(plugin, punishment, false);
            }

            @Override
            public void entryRemoved(Punishment punishment) {
                fire(plugin, punishment, true);
            }
        });
    }

    private static void fire(JavaPlugin plugin, Punishment punishment, boolean removed) {
        if (punishment == null) {
            return;
        }
        VeritePunishmentEvent event = new VeritePunishmentEvent(punishment, removed);
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getPluginManager().callEvent(event);
        } else {
            teacommontea.util.sched.Sched.executeGlobal(() -> Bukkit.getPluginManager().callEvent(event));
        }
    }

    private static EventValueBridge selectBridge(JavaPlugin plugin) {
        Version version = Skript.getVersion();
        String era;
        if (version.compareTo(2, 10, 0) < 0) {
            era = ERA_GETTER;
        } else if (version.compareTo(2, 15, 0) < 0) {
            era = ERA_CONVERTER;
        } else {
            era = ERA_REGISTRY;
        }
        try {
            return (EventValueBridge) Class.forName(era).getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            plugin.getLogger().warning("could not initialise the Skript event value bridge for version "
                    + version + " (Skript syntax off): " + t.getMessage());
            return null;
        }
    }

    public void loadSyntax(String basePackage, String... subPackages) {
        try {
            addon.loadClasses(basePackage, subPackages);
        } catch (IOException e) {
            throw new RuntimeException("[Verite] failed to load Skript syntax classes from " + basePackage, e);
        }
    }

    public SkriptAddon addon() {
        return addon;
    }

    public EventValueBridge eventValues() {
        return eventValues;
    }
}
