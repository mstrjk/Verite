package teacommontea.veritechasse.Reality;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.ExternalY;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.VerticalTick;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.Gravity;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerJump.JumpGate;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.FluidState;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.LavaMotion;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.SinkableSurface;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.WaterVertical;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerMisc.PowderSnowSupport;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.ExternalXZ;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.RiptideImpulseXZ;
import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.GroundState;
import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.SurfaceExemption;
import teacommontea.veritechasse.Vanilla.Potions.Levitation;
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

    public boolean levitating() {
        return this.current.effects() != null
            && this.current.effects().has(Levitation.KEY);
    }

    public boolean sinkExpectationIsValid() {
        return SurfaceExemption.sinkExpectationIsValid(
            this.current.passenger(),
            this.current.flying(),
            this.current.mayFly(),
            this.current.gameMode(),
            this.current.gliding(),
            this.current.rocketActive(),
            levitating(),
            this.current.riptiding(),
            inBubbleColumn(),
            this.current.supported());
    }

    public boolean inTransition() {
        return SurfaceExemption.inTransition(
            SurfaceExemption.surfaceChanged(
                this.previous.blockHere(), this.current.blockHere()),
            SurfaceExemption.fluidContactChanged(
                this.previous.touchingWater(), this.current.touchingWater()),
            SurfaceExemption.poseChanged(this.previous.pose(), this.current.pose()),
            SurfaceExemption.supportChanged(
                this.previous.supported(), this.current.supported()),
            SurfaceExemption.vehicleChanged(
                this.previous.passenger(),
                this.previous.vehicleType(),
                this.current.passenger(),
                this.current.vehicleType()),
            SurfaceExemption.abilitiesChanged(
                this.previous.flying(),
                this.previous.mayFly(),
                this.current.flying(),
                this.current.mayFly()),
            SurfaceExemption.footwearChanged(
                PowderSnowSupport.walksOnPowderSnow(this.previous.boots()),
                PowderSnowSupport.walksOnPowderSnow(this.current.boots())),
            SurfaceExemption.blockBelowChanged(
                this.previous.blockBelow(), this.current.blockBelow()),
            this.tickGap > 1L,
            this.current.changedWorld(this.previous),
            impulseIsLive());
    }

    public boolean walksOnPowderSnow() {
        return PowderSnowSupport.walksOnPowderSnow(this.current.boots());
    }

    public String sinkableSurface() {
        return SinkableSurface.surfaceOf(
            this.current.blockHere(), this.current.blockBelow(), this.current.boots());
    }

    public boolean overSinkable() {
        return sinkableSurface() != null;
    }

    public boolean overSinkableUnsupported() {
        return overSinkable() && !this.current.supported();
    }

    public boolean swimmingPose() {
        return this.current.swimming() || this.current.swimPhysics();
    }

    public boolean submergedDeep() {
        return this.current.submerged();
    }

    public boolean touchingFluid() {
        return this.current.inWater() || this.current.inLava();
    }

    public double deltaY() {
        return this.current.deltaY();
    }

    public double observedVertical() {
        return this.current.observedVertical();
    }

    public double horizontal() {
        return this.current.observedHorizontal();
    }

    public double baseGravity() {
        return Gravity.effective(0.0D, this.current.effects(), this.protocol);
    }

    public boolean sprintingInFluid() {
        return FluidState.swimmingImpliesSprinting(
            this.current.swimming(), this.current.sprinting());
    }

    public double carriedVertical() {
        return this.previous.deltaY();
    }

    public double expectedVerticalCeiling() {
        double carried = carriedVertical();
        String surface = sinkableSurface();
        if (SinkableSurface.LAVA.equals(surface)) {
            return LavaMotion.nextVertical(
                carried, baseGravity(), this.current.shallowLava())
                + JumpGate.LIQUID_JUMP_IMPULSE;
        }
        if (SinkableSurface.POWDER_SNOW.equals(surface)) {
            return PowderSnowSupport.enteringDelta(carried);
        }
        if (this.current.swimPhysics() || this.current.touchingWater()) {
            return WaterVertical.maximumRise(
                carried, baseGravity(), this.current.sprinting());
        }
        return VerticalTick.next(carried, this.current.effects(), this.protocol);
    }

    public double expectedVerticalFloor() {
        double carried = carriedVertical();
        String surface = sinkableSurface();
        if (SinkableSurface.LAVA.equals(surface)) {
            return LavaMotion.nextVertical(
                carried, baseGravity(), this.current.shallowLava())
                + JumpGate.SINK_IMPULSE;
        }
        if (SinkableSurface.POWDER_SNOW.equals(surface)) {
            return PowderSnowSupport.enteringDelta(carried);
        }
        if (this.current.swimPhysics() || this.current.touchingWater()) {
            return WaterVertical.minimumFall(
                carried, baseGravity(), this.current.sprinting());
        }
        return VerticalTick.next(carried, this.current.effects(), this.protocol);
    }

    public double expectedVertical() {
        return expectedVerticalCeiling();
    }

    public double verticalExcess() {
        return this.current.deltaY() - expectedVerticalCeiling();
    }

    public boolean verticalContradictsEngine() {
        return verticalExcess() > VERTICAL_TOLERANCE;
    }

    public boolean sankLessThanVanillaDemands() {
        return this.current.deltaY() > expectedVerticalFloor() + VERTICAL_TOLERANCE
            && verticalContradictsEngine();
    }

    public static final double VERTICAL_TOLERANCE = 1.0E-3D;

    private java.util.List<String> trace;

    public void trace(String checkKey, String branch, String reason) {
        if (this.trace == null) {
            this.trace = new java.util.ArrayList<>();
        }
        this.trace.add(checkKey + "/" + branch + ": " + reason);
    }

    public java.util.List<String> traceLines() {
        return this.trace == null ? java.util.List.of() : this.trace;
    }

    public String transitionReason() {
        if (SurfaceExemption.surfaceChanged(
                this.previous.blockHere(), this.current.blockHere())) {
            return "surface " + this.previous.blockHere()
                + "->" + this.current.blockHere();
        }
        if (SurfaceExemption.fluidContactChanged(
                this.previous.touchingWater(), this.current.touchingWater())) {
            return "fluidContact";
        }
        if (SurfaceExemption.poseChanged(this.previous.pose(), this.current.pose())) {
            return "pose " + this.previous.pose() + "->" + this.current.pose();
        }
        if (SurfaceExemption.supportChanged(
                this.previous.supported(), this.current.supported())) {
            return "support";
        }
        if (SurfaceExemption.vehicleChanged(
                this.previous.passenger(), this.previous.vehicleType(),
                this.current.passenger(), this.current.vehicleType())) {
            return "vehicle";
        }
        if (SurfaceExemption.abilitiesChanged(
                this.previous.flying(), this.previous.mayFly(),
                this.current.flying(), this.current.mayFly())) {
            return "abilities";
        }
        if (SurfaceExemption.footwearChanged(
                PowderSnowSupport.walksOnPowderSnow(this.previous.boots()),
                PowderSnowSupport.walksOnPowderSnow(this.current.boots()))) {
            return "footwear";
        }
        if (SurfaceExemption.blockBelowChanged(
                this.previous.blockBelow(), this.current.blockBelow())) {
            return "blockBelow " + this.previous.blockBelow()
                + "->" + this.current.blockBelow();
        }
        if (this.tickGap > 1L) {
            return "tickGap " + this.tickGap;
        }
        if (this.current.changedWorld(this.previous)) {
            return "world";
        }
        if (impulseIsLive()) {
            return "impulse";
        }
        return null;
    }

    public String evaluableReason() {
        if (!overSinkable()) {
            return "notOverSinkable here=" + this.current.blockHere()
                + " below=" + this.current.blockBelow();
        }
        if (!sinkExpectationIsValid()) {
            return "sinkExpectationInvalid passenger=" + this.current.passenger()
                + " flying=" + this.current.flying()
                + " mayFly=" + this.current.mayFly()
                + " mode=" + this.current.gameMode()
                + " gliding=" + this.current.gliding()
                + " rocket=" + this.current.rocketActive()
                + " levitating=" + levitating()
                + " riptide=" + this.current.riptiding()
                + " bubble=" + inBubbleColumn()
                + " supported=" + this.current.supported();
        }
        String transition = transitionReason();
        if (transition != null) {
            return "transition " + transition;
        }
        return null;
    }

    public boolean evaluable() {
        if (!overSinkable()) {
            return false;
        }
        if (!sinkExpectationIsValid()) {
            return false;
        }
        return !inTransition();
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
