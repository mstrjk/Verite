package teacommontea.veritechasse.reality;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerClimb.ClimbMotion;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerClimb.ClimbReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerClimb.ClimbableBlocks;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerCrawl.CrawlReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerCrawl.CrawlState;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.Bounce;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.ExternalY;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.FallDamage;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.VerticalReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.VerticalTick;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerGlide.GlideGate;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerGlide.GlideReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerJump.JumpGate;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerJump.JumpPower;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerJump.JumpReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerLiquid.FluidState;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerLiquid.LiquidReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerMisc.CreativeFlight;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerMisc.MiscReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerSneak.SneakPose;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerSneak.SneakReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ.HorizontalReality;
import teacommontea.veritechasse.vanilla.PlayerMovement.Support.GroundState;
import teacommontea.veritechasse.vanilla.Protocol;

public final class RealityCheck {

    public static final double DEFAULT_TOLERANCE = 1.0E-6D;

    private final Protocol protocol;
    private final Era era;

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

    public static final long MAXIMUM_EVALUATED_GAP = 4L;

    public static final double RESTING_EPSILON = 1.0E-4D;

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

        boolean derivedGround = derivedGround(previous, current);
        boolean airborne = !derivedGround;

        addContext(observations, previous, current, derivedGround, tickGap);
        checkHorizontal(observations, previous, current, derivedGround);
        checkVertical(observations, previous, current, derivedGround, airborne);
        checkPose(observations, current);
        checkFlight(observations, current);
        checkGlide(observations, current, derivedGround);
        checkFall(observations, previous, current, derivedGround);
        return observations;
    }

    private long elapsedSteps(PlayerSnapshot previous, PlayerSnapshot current) {
        long gap = current.tick() - previous.tick();
        return gap < 1L ? 1L : gap;
    }

    private boolean derivedGround(PlayerSnapshot previous, PlayerSnapshot current) {
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
        double expected = -VerticalTick.next(-fell, current.effects(), this.protocol);
        return GroundState.onGround(-expected, -fell);
    }

    private void addContext(
            List<Observation> observations,
            PlayerSnapshot previous,
            PlayerSnapshot current,
            boolean derivedGround,
            long tickGap) {
        StringBuilder state = new StringBuilder();
        state.append("h=").append(format(current.observedHorizontal()));
        state.append(" dy=").append(format(current.observedVertical()));
        state.append(" dt=").append(tickGap);
        state.append(" pkt=").append(current.packetIndex());
        state.append(" ground=").append(derivedGround);
        state.append(" support=").append(current.supported());
        if (!current.takesFallDamage()) {
            state.append(" nodamage");
        }
        state.append(" pose=").append(current.pose());
        if (current.sprinting()) {
            state.append(" sprint");
        }
        if (current.sneaking()) {
            state.append(" sneak");
        }
        if (current.swimming()) {
            state.append(" swim");
        }
        if (current.gliding()) {
            state.append(" glide");
        }
        if (current.flying()) {
            state.append(" fly");
        }
        if (current.inWater()) {
            state.append(" water");
        }
        if (current.inLava()) {
            state.append(" lava");
        }
        state.append(" on=").append(current.blockBelow());
        state.append(" in=").append(current.blockHere());
        observations.add(Observation.context(state.toString()));
    }

    private void checkHorizontal(
            List<Observation> observations,
            PlayerSnapshot previous,
            PlayerSnapshot current,
            boolean derivedGround) {
        double observed = current.observedHorizontal();
        if (current.flying()) {
            double bound = MiscReality.maximumFlightHorizontal(
                CreativeFlight.DEFAULT_FLYING_SPEED, current.sprinting());
            compare(observations, "fly-xz", observed, bound);
            return;
        }
        if (current.gliding()) {
            double bound = GlideReality.maximumHorizontal(previous.horizontal(), true);
            compare(observations, "glide-xz", observed, bound);
            return;
        }
        if (current.inLava()) {
            double bound = LiquidReality.maximumLavaSpeed(true, current.ultraWarm());
            compare(observations, "lava-xz", observed, bound);
            return;
        }
        if (current.inWater()) {
            double bound = LiquidReality.maximumWaterSpeedWithCurrent(
                current.effects(), current.boots(), current.sprinting(), derivedGround, this.era);
            compare(observations, "water-xz", observed, bound);
            return;
        }
        if (ClimbableBlocks.contains(current.blockHere())) {
            compare(observations, "climb-xz", observed, ClimbMotion.CLAMP);
            return;
        }

        boolean slow = SneakPose.isCrouching(current.pose())
            || CrawlState.isCrawling(current.pose(), current.inWater());
        boolean sprinting = current.sprinting() || previous.sprinting();
        double steady;
        if (slow) {
            steady = SneakReality.maximumTerminalSpeed(
                current.effects(), current.blockBelow(), current.blockHere(),
                SneakPose.isCrouching(current.pose()),
                CrawlState.isCrawling(current.pose(), current.inWater()),
                current.leggings(), this.era);
        } else {
            steady = GroundSpeed.terminalSpeed(
                current.effects(), sprinting,
                current.blockBelow(), current.blockHere());
        }

        double carried = previous.observedHorizontal();

        long steps = elapsedSteps(previous, current);

        double acceleration = GroundSpeed.frictionInfluencedSpeed(
            current.effects(), sprinting, current.blockBelow());
        boolean leftGround = previous.supported() && !current.supported();

        if (!derivedGround) {
            double decayed = carried;
            if (leftGround) {
                decayed = JumpReality.maximumTakeoffHorizontal(
                    carried, acceleration, GroundSpeed.AIR_DRAG, sprinting);
            }
            double reachable = 0.0D;
            for (long step = 0L; step < steps; step++) {
                if (step > 0L || !leftGround) {
                    decayed = decayed * GroundSpeed.AIR_DRAG
                        + GroundSpeed.airborneSpeed(sprinting, false);
                }
                reachable = reachable + Math.max(steady, decayed);
            }
            compare(observations, "air-xz", observed, reachable,
                " carried=" + format(carried) + " takeoff=" + leftGround
                    + " steps=" + steps + " pkt=" + current.packetIndex());
            return;
        }

        double takeoff = JumpReality.maximumTakeoffHorizontal(
            carried, acceleration, GroundSpeed.AIR_DRAG, sprinting);
        double perStep = Math.max(steady, takeoff);
        compare(observations, "ground-xz", observed, perStep * (double) steps,
            " carried=" + format(carried) + " steady=" + format(steady)
                + " takeoff=" + format(takeoff) + " steps=" + steps
                + " on=" + current.blockBelow());
    }

    private void checkVertical(
            List<Observation> observations,
            PlayerSnapshot previous,
            PlayerSnapshot current,
            boolean derivedGround,
            boolean airborne) {
        double observed = current.observedVertical();
        if (current.flying() || current.gliding() || current.inWater() || current.inLava()) {
            return;
        }
        if (ClimbableBlocks.contains(current.blockHere())) {
            if (!ClimbReality.permitsAscent(observed, true, true)) {
                observations.add(Observation.suspect("climb-y",
                    "rose " + format(observed) + " on a climbable, bound "
                        + format(ClimbMotion.maximumAscent(true, true))));
            }
            return;
        }
        if (!airborne || observed <= 0.0D) {
            return;
        }

        long steps = elapsedSteps(previous, current);
        double carried = previous.observedVertical();
        double jump = JumpPower.of(
            current.blockHere(), current.blockBelow(), current.effects(), this.protocol);
        double bounce = Bounce.maximumRebound(
            carried, current.blockBelow(), true, this.protocol);
        double velocity = Math.max(Math.max(carried, bounce), jump);
        double bound = 0.0D;
        for (long step = 0L; step < steps; step++) {
            velocity = Math.max(
                VerticalTick.next(velocity, current.effects(), this.protocol), jump);
            bound = bound + velocity;
        }
        if (observed > bound + DEFAULT_TOLERANCE) {
            observations.add(Observation.suspect("delta-y",
                "rose " + format(observed) + " airborne, bound " + format(bound)
                    + " (carried " + format(carried) + ", bounce " + format(bounce)
                    + ", jump " + format(jump) + ", steps=" + steps + ")"));
        }
    }

    private void checkPose(List<Observation> observations, PlayerSnapshot current) {
        if (SneakReality.poseContradictsShiftKey(current.pose(), current.sneaking())) {
            observations.add(Observation.suspect("pose",
                "sneak key held but pose is " + current.pose()));
        }
    }

    private void checkFlight(List<Observation> observations, PlayerSnapshot current) {
        if (MiscReality.fliesWithoutPermission(current.flying(), current.mayFly())) {
            observations.add(Observation.suspect("flight",
                "flying without permission"));
        }
    }

    private void checkGlide(
            List<Observation> observations,
            PlayerSnapshot current,
            boolean derivedGround) {
        if (!current.gliding()) {
            return;
        }
        boolean hasGlider = hasUsableGlider(current);
        if (GlideReality.glidingWithoutGlider(
                true, derivedGround, false, current.effects(), hasGlider)) {
            observations.add(Observation.suspect("glide",
                "gliding with no usable glider"
                    + " chest=" + itemName(current.chestplate())
                    + " ground=" + derivedGround));
        }
    }

    private boolean hasUsableGlider(PlayerSnapshot current) {
        ItemStack chest = current.chestplate();
        if (chest == null) {
            return false;
        }
        String key = itemName(chest);
        int maxDamage = chest.getType().getMaxDurability();
        return GlideGate.legacyElytraIsUsable(key, damageOf(chest), maxDamage);
    }

    private static String itemName(ItemStack stack) {
        if (stack == null) {
            return "";
        }
        return stack.getType().name().toLowerCase(Locale.ROOT);
    }

    private static int damageOf(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof Damageable) {
            return ((Damageable) meta).getDamage();
        }
        return 0;
    }

    private void checkFall(
            List<Observation> observations,
            PlayerSnapshot previous,
            PlayerSnapshot current,
            boolean derivedGround) {
        if (!derivedGround || previous.fallDistance() <= 0.0D) {
            return;
        }
        if (!previous.takesFallDamage() || !current.takesFallDamage()) {
            return;
        }
        boolean expected = FallDamage.expectsDamageOn(
            current.blockBelow(), previous.fallDistance(),
            current.mayFly(), current.effects(), this.protocol);
        boolean observed = current.health() < previous.health();
        if (expected && !observed) {
            observations.add(Observation.note("fall",
                "landed from " + format(previous.fallDistance())
                    + " expecting damage, none observed"));
        }
    }

    private void compare(
            List<Observation> observations,
            String label,
            double observed,
            double bound) {
        compare(observations, label, observed, bound, "");
    }

    private void compare(
            List<Observation> observations,
            String label,
            double observed,
            double bound,
            String extra) {
        if (observed > bound + DEFAULT_TOLERANCE) {
            observations.add(Observation.suspect(label,
                "moved " + format(observed) + ", bound " + format(bound)
                    + " (excess " + format(observed - bound) + ")" + extra));
        }
    }

    private static String format(double value) {
        return String.format("%.5f", value);
    }

    public String describeCapabilities() {
        StringBuilder out = new StringBuilder();
        out.append("client input available: ")
            .append(teacommontea.veritechasse.vanilla.PlayerMovement.Support.ClientInput
                .availableIn(this.protocol));
        out.append(", horizontal collision: ")
            .append(teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets
                .MovePlayerPacket.hasHorizontalCollision(this.protocol));
        out.append(", sneak source: ")
            .append(teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets
                .SneakSource.of(this.protocol));
        out.append(", jump attribute: ").append(JumpPower.attributeDriven(this.protocol));
        out.append(", fluid split: ").append(FluidState.travelMethodsAreSplit(this.protocol));
        out.append(", jump cooldown: ").append(JumpGate.COOLDOWN_TICKS).append(" ticks");
        out.append(", stuck-vertical wired: ").append(ExternalY.stuckMultiplier("cobweb", null));
        return out.toString();
    }
}
