package teacommontea.veritechasse.Vanilla;

import org.bukkit.Bukkit;

public final class Protocol {

    private final int major;
    private final int minor;
    private final int patch;

    private Protocol(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static Protocol of(int major, int minor, int patch) {
        return new Protocol(major, minor, patch);
    }

    public static Protocol current() {
        return parse(Bukkit.getBukkitVersion());
    }

    public static Protocol parse(String raw) {
        if (raw == null) {
            return new Protocol(0, 0, 0);
        }

        int cut = raw.indexOf('-');
        String trimmed = cut < 0 ? raw : raw.substring(0, cut);

        String[] parts = trimmed.split("\\.");
        int major = numberAt(parts, 0);
        int minor = numberAt(parts, 1);
        int patch = numberAt(parts, 2);
        return new Protocol(major, minor, patch);
    }

    private static int numberAt(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        try {
            return Integer.parseInt(parts[index].trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public int major() {
        return this.major;
    }

    public int minor() {
        return this.minor;
    }

    public int patch() {
        return this.patch;
    }

    public boolean atLeast(int otherMajor, int otherMinor, int otherPatch) {
        if (this.major != otherMajor) {
            return this.major > otherMajor;
        }
        if (this.minor != otherMinor) {
            return this.minor > otherMinor;
        }
        return this.patch >= otherPatch;
    }

    public boolean below(int otherMajor, int otherMinor, int otherPatch) {
        return !this.atLeast(otherMajor, otherMinor, otherPatch);
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + "." + this.patch;
    }
}
