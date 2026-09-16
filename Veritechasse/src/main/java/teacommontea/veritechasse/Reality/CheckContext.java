package teacommontea.veritechasse.Reality;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.ExternalY;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.VerticalTick;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.ExternalXZ;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.RiptideImpulseXZ;
import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.GroundState;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class CheckContext {

    public static final double RESTING_EPSILON = 1.0E-4D;

    public static final String AIR = "air";

    public static final String LAVA = "lava";

    private final PlayerSnapshot previous;
    private final PlayerSnapshot current;
    private final PlayerState state;
    private final Protocol protocol;
    private final Era era;

    private final long tickGap;
    private final long steps;
    private final boolean derivedGround;
    private final double externalHorizontal;

    public CheckContext(
            PlayerSnapshot previous,
            PlayerSnapshot current,
            PlayerState state,
            Protocol protocol,
            Era era) {
        this.previous = previous;
        this.current = current;
        this.state = state;
        this.protocol = protocol;
        this.era = era;
        this.tickGap = current.tick() - previous.tick();
        this.steps = this.tickGap < 1L ? 1L : this.tickGap;
        this.derivedGround = deriveGround(previous, current, protocol);
        this.externalHorizontal = deriveExternalHorizontal(current, era)
            + state.impulseHorizontal(current.tick());
    }

    public PlayerSnapshot previous() {
        return this.previous;
    }

    public PlayerSnapshot current() {
        return this.current;
    }

    public PlayerState state() {
        return this.state;
    }

    public Protocol protocol() {
        return this.protocol;
    }

    public Era era() {
        return this.era;
    }

    public long tickGap() {
        return this.tickGap;
    }

    public long steps() {
        return this.steps;
    }

    public boolean derivedGround() {
        return this.derivedGround;
    }

    public boolean airborne() {
        return !this.derivedGround;
    }

    public double externalHorizontal() {
        return this.externalHorizontal;
    }

    public double impulseVertical() {
        return this.state.impulseVertical(this.current.tick());
    }

    public boolean impulseIsLive() {
        return this.state.impulseIsLive(this.current.tick());
    }

    public boolean inFluidEitherTick() {
        return this.current.swimPhysics() || this.previous.swimPhysics();
    }

    public boolean touchingWaterEitherTick() {
        return this.current.touchingWater() || this.previous.touchingWater();
    }

    public boolean nearBlockBelow() {
        if (!isPassableName(this.current.blockBelow())) {
            return true;
        }
        return !isPassableName(this.current.blockHere());
    }

    public static boolean isFluidName(String blockName) {
        return PlayerSnapshot.WATER.equals(blockName)
            || PlayerSnapshot.BUBBLE_COLUMN.equals(blockName)
            || LAVA.equals(blockName);
    }

    private static boolean isPassableName(String blockName) {
        return AIR.equals(blockName) || isFluidName(blockName);
    }

    public boolean inBubbleColumn() {
        return ExternalY.inBubbleColumn(this.current.blockHere());
    }

    private static boolean deriveGround(
            PlayerSnapshot previous, PlayerSnapshot current, Protocol protocol) {
        if (!current.supported()) {
            return false;
        }
        if (current.observedVertical() > RESTING_EPSILON) {
            return false;
        }
        double fell = previous.y() - current.y();
        if (fell <= RESTING_EPSILON) {
            return true;
        }
        double expected = -VerticalTick.next(-fell, current.effects(), protocol);
        return GroundState.onGround(-expected, -fell);
    }

    private static double deriveExternalHorizontal(PlayerSnapshot current, Era era) {
        double total = ExternalXZ.additiveImpulse(
            null,
            0.0D,
            0.0F,
            current.inWater(),
            current.inLava(),
            current.ultraWarm(),
            false,
            false,
            true);
        if (RiptideImpulseXZ.canLaunch(current.heldItem(), current.inWater(), false)) {
            total = total + RiptideImpulseXZ.horizontalImpulse(
                current.heldItem(), current.pitch(), era);
        }
        return total;
    }
}
