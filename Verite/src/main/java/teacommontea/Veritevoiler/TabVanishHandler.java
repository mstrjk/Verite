package teacommontea.veritevoiler;


public interface TabVanishHandler {

    boolean isVanished(java.util.UUID player);

    boolean canSee(java.util.UUID viewer, java.util.UUID target);
}
