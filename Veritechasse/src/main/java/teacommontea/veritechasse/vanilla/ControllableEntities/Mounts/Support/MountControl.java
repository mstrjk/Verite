package teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Camel;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.HappyGhast;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Horses;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Llamas;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Pig;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Strider;
import teacommontea.veritechasse.vanilla.Reality;

public final class MountControl {

    private MountControl() {
    }

    public static ControlGate gateFor(String entityName) {
        if (entityName == null) {
            return ControlGate.NONE;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (name.equals(Pig.KEY)) {
            return Pig.controlGate();
        }
        if (name.equals(Strider.KEY)) {
            return Strider.controlGate();
        }
        if (name.equals(HappyGhast.KEY)) {
            return HappyGhast.controlGate();
        }
        if (name.equals(Camel.KEY)) {
            return Camel.controlGate();
        }
        if (Horses.contains(name)) {
            return Horses.controlGate();
        }
        if (Llamas.contains(name)) {
            return Llamas.controlGate();
        }
        return ControlGate.NONE;
    }

    public static MovementEnvelope envelopeFor(String entityName) {
        if (entityName == null) {
            return null;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (name.equals(Pig.KEY)) {
            return Pig.ENVELOPE;
        }
        if (name.equals(Strider.KEY)) {
            return Strider.ENVELOPE;
        }
        if (name.equals(HappyGhast.KEY)) {
            return HappyGhast.ENVELOPE;
        }
        if (name.equals(Camel.KEY)) {
            return Camel.ENVELOPE;
        }
        if (Horses.contains(name)) {
            return Horses.ENVELOPE;
        }
        if (Llamas.contains(name)) {
            return Llamas.ENVELOPE;
        }
        return null;
    }

    public static boolean grantsControl(
            String entityName,
            boolean equipped,
            String heldItemName,
            boolean stillTimeout) {
        return gateFor(entityName).satisfiedBy(equipped, heldItemName, stillTimeout);
    }

    public static boolean isSteerable(String entityName) {
        MovementEnvelope envelope = envelopeFor(entityName);
        return envelope != null && envelope.steerable();
    }

    public static boolean canJump(String entityName) {
        MovementEnvelope envelope = envelopeFor(entityName);
        return envelope != null && envelope.canJump();
    }

    public static boolean canFly(String entityName) {
        MovementEnvelope envelope = envelopeFor(entityName);
        return envelope != null && envelope.canFly();
    }

    public static boolean isControllable(String entityName) {
        return gateFor(entityName) != ControlGate.NONE;
    }

    public static Reality steeringInfluence(String entityName) {
        if (!isSteerable(entityName)) {
            return Reality.impossible();
        }
        MovementEnvelope envelope = envelopeFor(entityName);
        return Reality.of(envelope.speedMultiplier());
    }
}
