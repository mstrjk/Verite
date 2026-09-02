package teacommontea.api;

import java.util.UUID;


public record MojangProfile(UUID uuid, String name, MojangStatus status) {}
