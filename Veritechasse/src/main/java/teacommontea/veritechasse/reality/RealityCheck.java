package teacommontea.veritechasse.Reality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.ExternalY;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerJump.JumpGate;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerJump.JumpPower;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.FluidState;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class RealityCheck {

    public static final long MAXIMUM_EVALUATED_GAP = 4L;

    private final Protocol protocol;
    private final Era era;

    private final Map<UUID, PlayerState> states = new HashMap<>();

    public RealityCheck(Protocol protocol) {
        this.protocol = protocol;
        this.era = Era.enchantments(protocol);
    }

    public Protocol protocol() {
        return this.protocol;
    }

    public Era era() {
        return this.era;
    }

    public PlayerState stateOf(UUID id) {
        PlayerState state = this.states.get(id);
        if (state == null) {
            state = new PlayerState();
            this.states.put(id, state);
        }
        return state;
    }

    public void forget(UUID id) {
        this.states.remove(id);
    }

    public void authoriseImpulse(UUID id, double horizontal, double vertical, long tick) {
        stateOf(id).authoriseImpulse(horizontal, vertical, tick);
    }

    public List<Observation> evaluate(PlayerSnapshot previous, PlayerSnapshot current) {
        List<Observation> observations = new ArrayList<>();
        if (previous == null) {
            return observations;
        }
        if (current.changedWorld(previous)) {
            observations.add(Observation.note("world", "changed world, baseline reset"));
            return observations;
        }

        long tickGap = current.tick() - previous.tick();
        if (tickGap < 0L) {
            return observations;
        }
        if (tickGap > MAXIMUM_EVALUATED_GAP) {
            current.recordObservedHorizontal(0.0D);
            current.recordObservedVertical(0.0D);
            observations.add(Observation.note("gap",
                tickGap + " ticks since last movement, baseline reset"));
            return observations;
        }

        current.recordObservedHorizontal(current.horizontalDistanceTo(previous));
        current.recordObservedVertical(current.verticalDistanceTo(previous));

        PlayerState state = stateOf(current.id());
        CheckContext context = new CheckContext(
            previous, current, state, this.protocol, this.era);

        for (Check check : CheckRegistry.all()) {
            check.evaluate(context, observations);
        }

        state.decayImpulse(current.tick(), GroundSpeed.AIR_DRAG);
        return observations;
    }

    public String describeCapabilities() {
        StringBuilder out = new StringBuilder();
        out.append("checks: ").append(CheckRegistry.count());
        out.append(" ").append(CheckRegistry.keys());
        out.append(", client input available: ")
            .append(teacommontea.veritechasse.Vanilla.PlayerMovement.Support.ClientInput
                .availableIn(this.protocol));
        out.append(", horizontal collision: ")
            .append(teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets
                .MovePlayerPacket.hasHorizontalCollision(this.protocol));
        out.append(", sneak source: ")
            .append(teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets
                .SneakSource.of(this.protocol));
        out.append(", jump attribute: ").append(JumpPower.attributeDriven(this.protocol));
        out.append(", fluid split: ").append(FluidState.travelMethodsAreSplit(this.protocol));
        out.append(", jump cooldown: ").append(JumpGate.COOLDOWN_TICKS).append(" ticks");
        out.append(", stuck-vertical wired: ").append(ExternalY.stuckMultiplier("cobweb", null));
        return out.toString();
    }
}
