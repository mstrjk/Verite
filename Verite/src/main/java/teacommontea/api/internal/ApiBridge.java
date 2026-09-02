package teacommontea.api.internal;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import teacommontea.api.ActivePunishment;
import teacommontea.api.DurationCheck;
import teacommontea.api.MojangProfile;
import teacommontea.api.MojangStatus;
import teacommontea.api.ModerationListener;
import teacommontea.api.PlayerProfile;
import teacommontea.api.Punishment;
import teacommontea.api.PunishmentResult;
import teacommontea.api.PunishmentType;
import teacommontea.api.VanishListener;

import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverDAO;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.punish.SauverLimits;
import teacommontea.veritesauver.mojang.SauverMojang;
import teacommontea.veritedoux.postprocess.EveStore;

public final class ApiBridge {

    private ApiBridge() {}

    private static final List<ModerationListener> MODERATION = new CopyOnWriteArrayList<>();
    private static final List<VanishListener> VANISH = new CopyOnWriteArrayList<>();

    public static Punishment toPunishment(Entry e) {
        if (e == null) {
            return null;
        }
        return new Punishment(
                e.id(), e.randomId(), toType(e.type()), e.uuid(), e.ip(), e.reason(),
                e.executorUuid(), e.executorName(), e.removedByUuid(), e.removedByName(),
                e.removalReason(), e.dateStart(), e.dateEnd(), e.serverScope(),
                e.serverOrigin(), e.template(), e.silent(), e.ipban(), e.active());
    }

    public static List<Punishment> toPunishments(List<Entry> entries) {
        List<Punishment> out = new java.util.ArrayList<>(entries.size());
        for (Entry e : entries) {
            out.add(toPunishment(e));
        }
        return out;
    }

    public static Entry fromPunishment(Punishment p) {
        if (p == null) {
            return null;
        }
        return new Entry(p.id(), p.randomId(), fromType(p.type()), p.uuid(), p.ip(), p.reason(),
                p.executorUuid(), p.executorName(), p.removedByUuid(), p.removedByName(), p.removalReason(),
                p.dateStart(), p.dateEnd(), p.serverScope(), p.serverOrigin(), p.template(), p.silent(),
                p.ipban(), p.active());
    }

    public static PunishmentResult toResult(SauverEngine.Result r) {
        if (r == null) {
            return new PunishmentResult(false, null, "no result");
        }
        return new PunishmentResult(r.ok(), toPunishment(r.entry()), r.error());
    }

    public static PlayerProfile toProfile(SauverDAO.Profile p) {
        if (p == null) {
            return null;
        }
        return new PlayerProfile(
                p.uuid(), p.name(), p.names(), p.ips(), p.lastIp(), p.clients(), p.lastClient(),
                p.protocol(), p.referrer(), p.firstJoin(), p.lastSeen(), p.joinCount(),
                p.playtimeMs(), p.online(), p.punishments());
    }

    public static ActivePunishment toActive(SauverDAO.ActivePointer ptr) {
        if (ptr == null) {
            return null;
        }
        return new ActivePunishment(toType(ptr.type()), ptr.uuid(), ptr.entryId());
    }

    public static List<ActivePunishment> toActives(List<SauverDAO.ActivePointer> ptrs) {
        List<ActivePunishment> out = new java.util.ArrayList<>(ptrs.size());
        for (SauverDAO.ActivePointer p : ptrs) {
            out.add(toActive(p));
        }
        return out;
    }

    public static DurationCheck toCheck(SauverLimits.Check c) {
        if (c == null) {
            return null;
        }
        return new DurationCheck(c.ok(), c.durationMillis(), c.error());
    }

    public static MojangProfile toMojang(SauverMojang.Profile p) {
        if (p == null) {
            return null;
        }
        return new MojangProfile(p.uuid(), p.name(), toMojangStatus(p.status()));
    }

    public static MojangStatus toMojangStatus(SauverMojang.Status s) {
        if (s == null) {
            return null;
        }
        switch (s) {
            case FOUND:     return MojangStatus.FOUND;
            case NOT_FOUND: return MojangStatus.NOT_FOUND;
            case UNKNOWN:   return MojangStatus.UNKNOWN;
            default:        return null;
        }
    }

    public static PunishmentType toType(Entry.Type type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case BAN:     return PunishmentType.BAN;
            case MUTE:    return PunishmentType.MUTE;
            case WARNING: return PunishmentType.WARNING;
            case KICK:    return PunishmentType.KICK;
            default:      return null;
        }
    }

    public static Entry.Type fromType(PunishmentType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case BAN:     return Entry.Type.BAN;
            case MUTE:    return Entry.Type.MUTE;
            case WARNING: return Entry.Type.WARNING;
            case KICK:    return Entry.Type.KICK;
            default:      return null;
        }
    }

    public static void registerModeration(ModerationListener l) {
        if (l != null && !MODERATION.contains(l)) {
            MODERATION.add(l);
        }
    }

    public static void unregisterModeration(ModerationListener l) {
        MODERATION.remove(l);
    }

    public static void registerVanish(VanishListener l) {
        if (l != null && !VANISH.contains(l)) {
            VANISH.add(l);
        }
    }

    public static void unregisterVanish(VanishListener l) {
        VANISH.remove(l);
    }

    public static void firePunishmentAdded(Entry e) {
        Punishment p = toPunishment(e);
        for (ModerationListener l : MODERATION) {
            try {
                l.entryAdded(p);
            } catch (Throwable t) {
                warn("entryAdded", t);
            }
        }
    }

    public static void firePunishmentRemoved(Entry e) {
        Punishment p = toPunishment(e);
        for (ModerationListener l : MODERATION) {
            try {
                l.entryRemoved(p);
            } catch (Throwable t) {
                warn("entryRemoved", t);
            }
        }
    }

    public static void fireBroadcast(String message, String target) {
        for (ModerationListener l : MODERATION) {
            try {
                l.broadcastSent(message, target);
            } catch (Throwable t) {
                warn("broadcastSent", t);
            }
        }
    }

    public static void fireVanished(UUID player) {
        for (VanishListener l : VANISH) {
            try {
                l.vanished(player);
            } catch (Throwable t) {
                warn("vanished", t);
            }
        }
    }

    public static void fireUnvanished(UUID player) {
        for (VanishListener l : VANISH) {
            try {
                l.unvanished(player);
            } catch (Throwable t) {
                warn("unvanished", t);
            }
        }
    }

    private static void warn(String which, Throwable t) {
        org.bukkit.Bukkit.getLogger().warning(
                "[Verite] an API " + which + " listener threw: " + t);
    }
}
