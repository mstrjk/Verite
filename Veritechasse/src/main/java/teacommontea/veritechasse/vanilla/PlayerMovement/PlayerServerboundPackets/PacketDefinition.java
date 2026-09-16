package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets;

import java.util.List;
import java.util.Locale;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class PacketDefinition {

    private final String stableName;
    private final int[] addedAt;
    private final int[] removedAt;
    private final List<PacketPhase> phases;
    private final List<String> classNames;

    public PacketDefinition(
            String stableName,
            int[] addedAt,
            int[] removedAt,
            List<PacketPhase> phases,
            List<String> classNames) {
        this.stableName = stableName;
        this.addedAt = addedAt.clone();
        this.removedAt = removedAt == null ? null : removedAt.clone();
        this.phases = List.copyOf(phases);
        this.classNames = List.copyOf(classNames);
    }

    public String stableName() {
        return this.stableName;
    }

    public List<PacketPhase> phases() {
        return this.phases;
    }

    public List<String> classNames() {
        return this.classNames;
    }

    public boolean wasRemoved() {
        return this.removedAt != null;
    }

    public boolean addedIn(Protocol protocol) {
        return protocol.atLeast(this.addedAt[0], this.addedAt[1], this.addedAt[2]);
    }

    public boolean removedIn(Protocol protocol) {
        if (this.removedAt == null) {
            return false;
        }
        return protocol.atLeast(this.removedAt[0], this.removedAt[1], this.removedAt[2]);
    }

    public boolean existsIn(Protocol protocol) {
        return addedIn(protocol) && !removedIn(protocol);
    }

    public boolean wasRenamed() {
        return this.classNames.size() > 1;
    }

    public boolean movedPhase() {
        return this.phases.size() > 1;
    }

    public boolean hasPhase(PacketPhase phase) {
        return this.phases.contains(phase);
    }

    public boolean matchesClass(String className) {
        if (className == null) {
            return false;
        }
        String simple = className;
        int dot = simple.lastIndexOf('.');
        if (dot >= 0) {
            simple = simple.substring(dot + 1);
        }
        for (String candidate : this.classNames) {
            if (candidate.equals(simple)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesStableName(String name) {
        if (name == null) {
            return false;
        }
        return this.stableName.equals(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return "PacketDefinition[" + this.stableName + "]";
    }
}
