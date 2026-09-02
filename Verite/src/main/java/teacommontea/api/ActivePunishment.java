package teacommontea.api;

import java.util.UUID;


public record ActivePunishment(PunishmentType type, UUID uuid, long entryId) {}
