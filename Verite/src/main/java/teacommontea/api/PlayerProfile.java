package teacommontea.api;

import java.util.List;
import java.util.UUID;


public record PlayerProfile(
        UUID uuid,
        String name,
        List<String> names,
        List<String> ips,
        String lastIp,
        List<String> clients,
        String lastClient,
        int protocol,
        String referrer,
        long firstJoin,
        long lastSeen,
        long joinCount,
        long playtimeMs,
        boolean online,
        long punishments) {}
