package teacommontea.veritechasse.reality;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak.BlockKnowledge;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak.BlockReach;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak.BreakProgress;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerAggressor.AggressorReality;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerAggressor.AttackReach;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerAggressor.AttackResolution;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak.BreakReality;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerConsume.ConsumeGate;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerConsume.ConsumeReality;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerConsume.ConsumeState;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerVictim.ExchangeReality;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerVictim.HurtWindow;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerVictim.VictimReality;
import teacommontea.veritechasse.vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger.HungerReality;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger.HungerTick;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerPlace.BlockFace;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerPlace.PlaceReality;
import teacommontea.veritechasse.vanilla.PlayerInteraction.Tags.FoodValues;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets.BlockHitPacket;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets.InteractionPackets;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets.UseItemPacket;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Tools.Support.DestroySpeed;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class InteractionCheck {

    public static final float PROGRESS_TOLERANCE = 1.0E-4F;

    private final Protocol protocol;
    private final Era era;
    private final ToolEra toolEra;

    public InteractionCheck(Protocol protocol) {
        this.protocol = protocol;
        this.era = Era.enchantments(protocol);
        this.toolEra = ToolEra.of(protocol);
    }

    public Protocol protocol() {
        return this.protocol;
    }

    public float perTickProgress(
            float baseToolSpeed,
            float blockHardness,
            boolean preferredTool,
            ItemStack tool,
            ItemStack helmet,
            ActiveEffects effects,
            boolean eyeInWater,
            boolean onGround) {
        float playerSpeed = DestroySpeed.resolveFor(
            baseToolSpeed,
            tool,
            helmet,
            effects.amplifierOf("haste"),
            effects.amplifierOf("conduit_power"),
            effects.amplifierOf("mining_fatigue"),
            DestroySpeed.DEFAULT_BLOCK_BREAK_SPEED,
            eyeInWater,
            onGround,
            this.toolEra,
            this.era);
        return BreakProgress.perTickProgress(playerSpeed, blockHardness, preferredTool);
    }

    public List<Observation> evaluateBreakStart(
            PlayerSnapshot snapshot,
            int blockX,
            int blockY,
            int blockZ,
            String blockName,
            double blockInteractionRange,
            boolean creative) {
        List<Observation> observations = new ArrayList<>();
        checkReach(observations, "break-reach",
            snapshot, blockX, blockY, blockZ, blockInteractionRange, creative);
        checkFacing(observations, "break-facing", snapshot, blockX, blockY, blockZ);
        if (BreakReality.targetWasAlreadyAir(blockName)) {
            observations.add(Observation.suspect("break-air",
                "started breaking " + blockName + " at "
                    + blockX + "," + blockY + "," + blockZ));
        }
        return observations;
    }

    public List<Observation> evaluateBreakFinish(
            PlayerSnapshot snapshot,
            BreakSession session,
            int blockX,
            int blockY,
            int blockZ,
            String blockName,
            double blockInteractionRange,
            boolean creative) {
        List<Observation> observations = new ArrayList<>();
        checkReach(observations, "break-reach",
            snapshot, blockX, blockY, blockZ, blockInteractionRange, creative);
        checkFacing(observations, "break-facing", snapshot, blockX, blockY, blockZ);

        if (creative) {
            return observations;
        }
        if (session == null) {
            observations.add(Observation.suspect("break-unstarted",
                "broke " + blockName + " at " + blockX + "," + blockY + "," + blockZ
                    + " with no observed start"));
            return observations;
        }
        if (!session.matches(blockX, blockY, blockZ)) {
            observations.add(Observation.suspect("break-mismatch",
                "broke " + blockX + "," + blockY + "," + blockZ
                    + " while started on "
                    + session.blockX() + "," + session.blockY() + "," + session.blockZ()));
            return observations;
        }
        if (session.instaBreak()) {
            return observations;
        }

        int elapsed = session.elapsedTicks(snapshot.tick());
        float perTick = session.perTickProgress();
        if (perTick <= 0.0F) {
            observations.add(Observation.suspect("break-unbreakable",
                "broke " + blockName + " with zero progress rate"));
            return observations;
        }
        if (BreakReality.brokeTooFast(perTick, elapsed)) {
            int minimum = BreakReality.minimumTicks(perTick);
            observations.add(Observation.suspect("break-speed",
                "broke " + blockName + " in " + elapsed + " ticks, minimum " + minimum
                    + " (progress " + format(BreakProgress.serverProgress(perTick, elapsed))
                    + ", server needs "
                    + format(BreakProgress.SERVER_COMPLETION_THRESHOLD) + ")"));
        }
        return observations;
    }

    public List<Observation> evaluatePlace(
            PlayerSnapshot snapshot,
            int placedX,
            int placedY,
            int placedZ,
            int againstX,
            int againstY,
            int againstZ,
            String replacedBlockName,
            boolean canBuild,
            double blockInteractionRange,
            boolean creative,
            int minimumY,
            int maximumY) {
        List<Observation> observations = new ArrayList<>();
        checkReach(observations, "place-reach",
            snapshot, againstX, againstY, againstZ, blockInteractionRange, creative);
        checkFacing(observations, "place-facing", snapshot, againstX, againstY, againstZ);

        if (!BlockKnowledge.withinWorldHeight(placedY, minimumY, maximumY)) {
            observations.add(Observation.suspect("place-height",
                "placed at y=" + placedY + " outside [" + minimumY + "," + maximumY + "]"));
        }
        if (!canBuild) {
            observations.add(Observation.suspect("place-denied",
                "placed at " + placedX + "," + placedY + "," + placedZ
                    + " where building is not permitted"));
        }
        if (PlaceReality.placingIntoOccupiedSpace(replacedBlockName, true)) {
            observations.add(Observation.suspect("place-occupied",
                "placed into " + replacedBlockName));
        }

        String face = faceBetween(againstX, againstY, againstZ, placedX, placedY, placedZ);
        if (face == null) {
            boolean replacedInPlace = placedX == againstX
                && placedY == againstY && placedZ == againstZ;
            if (!replacedInPlace) {
                observations.add(Observation.suspect("place-face",
                    "placed at " + placedX + "," + placedY + "," + placedZ
                        + " which is not adjacent to "
                        + againstX + "," + againstY + "," + againstZ));
            }
        } else {
            int expectedX = PlaceReality.placementX(againstX, face, false);
            int expectedY = PlaceReality.placementY(againstY, face, false);
            int expectedZ = PlaceReality.placementZ(againstZ, face, false);
            if (expectedX != placedX || expectedY != placedY || expectedZ != placedZ) {
                observations.add(Observation.suspect("place-offset",
                    "face " + face + " from " + againstX + "," + againstY + "," + againstZ
                        + " should place at " + expectedX + "," + expectedY + "," + expectedZ));
            }
        }
        return observations;
    }

    public List<Observation> evaluatePlaceHit(
            double hitX,
            double hitY,
            double hitZ,
            int blockX,
            int blockY,
            int blockZ,
            String face,
            int previousSequence,
            int sequence) {
        List<Observation> observations = new ArrayList<>();
        if (PlaceReality.hitLocationIsImpossible(
                hitX, hitY, hitZ, blockX, blockY, blockZ)) {
            observations.add(Observation.suspect("place-hit",
                "hit " + format(hitX) + "," + format(hitY) + "," + format(hitZ)
                    + " is outside " + format(PlaceReality.hitLocationFrom().bound())
                    + " of block centre"));
        }
        if (face != null && PlaceReality.faceDoesNotMatchHit(
                hitX, hitY, hitZ, blockX, blockY, blockZ, face, HIT_FACE_TOLERANCE)) {
            observations.add(Observation.suspect("place-hit-face",
                "hit location does not lie on the claimed " + face + " face"));
        }
        if (!InteractionPackets.sequenceIsWellFormed(sequence)) {
            observations.add(Observation.suspect("place-sequence-malformed",
                "negative sequence " + sequence
                    + " which the server rejects outright"));
        } else if (PlaceReality.sequenceIsOutOfOrder(previousSequence, sequence)) {
            observations.add(Observation.note("place-sequence",
                "sequence " + sequence + " after " + previousSequence
                    + " (the server absorbs this with Math.max, it does not reject)"));
        }
        if (!BlockHitPacket.serverAcceptsHit(
                hitX, hitY, hitZ, blockX, blockY, blockZ)) {
            observations.add(Observation.suspect("place-hit-rejected",
                "hit location would be rejected by the server bound"));
        }
        float relativeX = BlockHitPacket.relativeOf(hitX, blockX);
        float relativeY = BlockHitPacket.relativeOf(hitY, blockY);
        float relativeZ = BlockHitPacket.relativeOf(hitZ, blockZ);
        if (!BlockHitPacket.isWellFormed(relativeX, relativeY, relativeZ)) {
            observations.add(Observation.suspect("place-hit-malformed",
                "hit location contains NaN or infinity"));
        } else if (!BlockHitPacket.relativesAreWithinBlock(
                relativeX, relativeY, relativeZ)) {
            observations.add(Observation.note("place-hit-outside",
                "hit lies outside the clicked block volume"
                    + " (vanilla still accepts this, its bound is from block centre)"));
        }
        return observations;
    }

    public List<Observation> evaluateBlockKnowledge(
            boolean chunkLoaded,
            boolean chunkSentToClient,
            boolean clientLoaded,
            int blockY,
            int minimumY,
            int maximumY) {
        List<Observation> observations = new ArrayList<>();
        if (!BlockKnowledge.serverKnowsBlock(
                chunkLoaded, BlockKnowledge.withinWorldHeight(blockY, minimumY, maximumY))) {
            observations.add(Observation.suspect("knowledge-server",
                "acted on a block the server does not know"));
        }
        if (BlockKnowledge.actionPrecedesKnowledge(chunkSentToClient, clientLoaded)) {
            observations.add(Observation.suspect("knowledge-client",
                "acted before the client could know the block"));
        }
        if (BlockKnowledge.aboveBuildLimit(blockY, maximumY)) {
            observations.add(Observation.suspect("knowledge-above",
                "y=" + blockY + " is above the build limit " + maximumY));
        }
        if (BlockKnowledge.belowBuildLimit(blockY, minimumY)) {
            observations.add(Observation.suspect("knowledge-below",
                "y=" + blockY + " is below the world floor " + minimumY));
        }
        return observations;
    }

    public List<Observation> evaluateUseTick(
            String itemName,
            int useDuration,
            int remainingBefore,
            boolean useOnRelease,
            String heldBefore,
            String heldNow) {
        List<Observation> observations = new ArrayList<>();
        if (ConsumeState.interruptedByItemChange(heldBefore, heldNow)) {
            observations.add(Observation.note("use-interrupted",
                "held item changed from " + heldBefore + " to " + heldNow
                    + " mid-use"));
        }
        if (UseItemPacket.completesOnTick(remainingBefore, useOnRelease)
                && !ConsumeState.completesOnTick(remainingBefore, useOnRelease)) {
            observations.add(Observation.suspect("use-completion",
                "completion disagrees with the vanilla countdown for " + itemName));
        }
        int elapsed = UseItemPacket.elapsedTicks(useDuration, remainingBefore);
        if (UseItemPacket.releaseArrivedTooEarly(
                useDuration, elapsed, ConsumeReality.minimumTicks(itemName))) {
            observations.add(Observation.suspect("use-early-release",
                itemName + " released after " + elapsed + " ticks, minimum "
                    + ConsumeReality.minimumTicks(itemName)));
        }
        return observations;
    }

    public List<Observation> evaluateKnockback(
            double observedHorizontal,
            double horizontalBefore,
            double power,
            double knockbackResistance) {
        List<Observation> observations = new ArrayList<>();
        double expected = VictimReality.expectedKnockback(
            horizontalBefore, power, knockbackResistance);
        if (VictimReality.resistedTooMuchKnockback(observedHorizontal, expected)) {
            observations.add(Observation.suspect("knockback-resist",
                "moved " + format(observedHorizontal)
                    + ", expected " + format(expected)));
        }
        if (VictimReality.tookTooMuchKnockback(observedHorizontal, expected)) {
            observations.add(Observation.suspect("knockback-excess",
                "moved " + format(observedHorizontal)
                    + ", expected " + format(expected)));
        }
        return observations;
    }

    public static final double HIT_FACE_TOLERANCE = 1.0E-4D;

    public List<Observation> evaluateConsumeStart(
            String itemName,
            int foodLevel,
            boolean invulnerable) {
        List<Observation> observations = new ArrayList<>();
        if (itemName == null) {
            return observations;
        }
        if (!ConsumeGate.canStart(itemName, foodLevel, invulnerable, this.protocol)) {
            observations.add(Observation.suspect("consume-gate",
                "started eating " + itemName + " at food " + foodLevel
                    + " where vanilla forbids it"));
        }
        return observations;
    }

    public List<Observation> evaluateConsume(
            PlayerSnapshot snapshot,
            String itemName,
            long startTick,
            int foodBefore,
            int foodAfter,
            float saturationBefore,
            float saturationAfter) {
        List<Observation> observations = new ArrayList<>();
        int elapsed = (int) (snapshot.tick() - startTick);
        if (startTick >= 0L && ConsumeState.completedTooEarly(
                itemName, elapsed, this.protocol)) {
            observations.add(Observation.suspect("consume-early",
                itemName + " completed after " + elapsed
                    + " ticks of use animation"));
        }
        if (startTick >= 0L && ConsumeReality.completedTooFast(itemName, elapsed)) {
            observations.add(Observation.suspect("consume-speed",
                "consumed " + itemName + " in " + elapsed + " ticks, minimum "
                    + ConsumeReality.minimumTicks(itemName)));
        }
        if (ConsumeReality.gainedTooMuchFood(itemName, foodBefore, foodAfter, this.protocol)) {
            observations.add(Observation.suspect("consume-food",
                itemName + " took food " + foodBefore + " to " + foodAfter
                    + ", expected at most "
                    + ConsumeReality.expectedFoodAfter(itemName, foodBefore, this.protocol)));
        }
        if (ConsumeReality.gainedTooMuchSaturation(
                itemName, saturationBefore, saturationAfter, foodAfter, this.protocol)) {
            observations.add(Observation.suspect("consume-saturation",
                itemName + " took saturation " + format(saturationBefore)
                    + " to " + format(saturationAfter)));
        }
        if (!FoodValues.isEdible(itemName) && foodAfter > foodBefore) {
            observations.add(Observation.suspect("consume-inedible",
                itemName + " is not edible but food rose "
                    + foodBefore + " to " + foodAfter));
        }
        return observations;
    }

    public List<Observation> evaluateFoodChange(
            int foodBefore,
            int foodAfter,
            float saturation,
            float exhaustion,
            float health,
            boolean consumed,
            boolean saturationEffect,
            String difficulty,
            boolean naturalRegeneration,
            int elapsedTicks) {
        List<Observation> observations = new ArrayList<>();
        if (HungerReality.foodExceedsCap(foodAfter)) {
            observations.add(Observation.suspect("hunger-cap",
                "food level " + foodAfter + " exceeds the vanilla maximum"));
        }
        if (HungerReality.saturationExceedsFood(saturation, foodAfter)) {
            observations.add(Observation.suspect("hunger-saturation",
                "saturation " + format(saturation) + " exceeds food " + foodAfter));
        }
        if (HungerReality.foodRoseWithoutCause(
                foodBefore, foodAfter, consumed, saturationEffect,
                difficulty, naturalRegeneration, elapsedTicks)) {
            observations.add(Observation.suspect("hunger-gain",
                "food rose " + foodBefore + " to " + foodAfter + " with no cause"));
        }
        if (HungerReality.exhaustionExceedsCap(exhaustion)) {
            observations.add(Observation.suspect("hunger-exhaustion",
                "exhaustion " + format(exhaustion) + " exceeds the vanilla cap"));
        }
        if (HungerTick.starves(foodAfter)
                && !HungerTick.starvationDamages(health, difficulty)) {
            observations.add(Observation.note("hunger-starve",
                "at food " + foodAfter + " starvation does not damage on " + difficulty));
        }
        return observations;
    }

    public List<Observation> evaluateSaturationChange(
            float saturationBefore,
            float saturationAfter,
            boolean consumed,
            boolean saturationEffect,
            String difficulty,
            boolean naturalRegeneration,
            int elapsedTicks) {
        List<Observation> observations = new ArrayList<>();
        if (HungerReality.saturationRoseWithoutCause(
                saturationBefore, saturationAfter, consumed, saturationEffect,
                difficulty, naturalRegeneration, elapsedTicks, this.protocol)) {
            observations.add(Observation.suspect("hunger-saturation-gain",
                "saturation rose " + format(saturationBefore)
                    + " to " + format(saturationAfter) + " with no cause"));
        }
        return observations;
    }

    public List<Observation> evaluateRegen(
            float healthBefore,
            float healthAfter,
            boolean naturalRegeneration,
            int foodLevel,
            float saturation,
            String difficulty) {
        List<Observation> observations = new ArrayList<>();
        if (HungerReality.healedWithoutFood(
                healthBefore, healthAfter, naturalRegeneration,
                foodLevel, saturation, difficulty)) {
            observations.add(Observation.suspect("regen-unexplained",
                "healed " + format(healthBefore) + " to " + format(healthAfter)
                    + " at food " + foodLevel
                    + " saturation " + format(saturation)));
        }
        return observations;
    }

    public List<Observation> evaluateSprintGate(
            boolean sprinting,
            int foodLevel,
            boolean mayFly) {
        List<Observation> observations = new ArrayList<>();
        if (HungerReality.sprintedWhileExhausted(sprinting, foodLevel, mayFly)) {
            observations.add(Observation.suspect("hunger-sprint",
                "sprinting at food " + foodLevel + " where vanilla forbids it"));
        }
        return observations;
    }

    public List<Observation> evaluateDamage(
            float incomingDamage,
            float observedFinalDamage,
            float totalArmour,
            float armourToughness,
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            ActiveEffects victimEffects,
            String damageType,
            boolean damagesHelmet,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown) {
        List<Observation> observations = new ArrayList<>();

        float protection = VictimReality.totalProtection(
            helmet, chestplate, leggings, boots,
            isFire(damageType), isExplosion(damageType),
            isProjectile(damageType), isFall(damageType), this.era);

        float expected = VictimReality.expectedDamage(
            incomingDamage,
            totalArmour,
            armourToughness,
            protection,
            victimEffects,
            damageType,
            EffectEra.of(this.protocol),
            damagesHelmet,
            helmet != null,
            lastHurt,
            invulnerableTime,
            bypassesCooldown,
            this.protocol);

        if (VictimReality.tookTooLittleDamage(observedFinalDamage, expected)) {
            observations.add(Observation.suspect("damage-low",
                damageType + " dealt " + format(observedFinalDamage)
                    + ", expected " + format(expected)));
        }
        if (VictimReality.tookTooMuchDamage(observedFinalDamage, expected)) {
            observations.add(Observation.note("damage-high",
                damageType + " dealt " + format(observedFinalDamage)
                    + ", expected " + format(expected)));
        }
        if (HurtWindow.absorbedEntirely(
                incomingDamage, lastHurt, invulnerableTime,
                bypassesCooldown, this.protocol)
                && observedFinalDamage > VictimReality.TOLERANCE) {
            observations.add(Observation.suspect("damage-window",
                "damage " + format(observedFinalDamage)
                    + " landed inside the invulnerability window"));
        }
        return observations;
    }

    public float protectionOf(
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            String damageType) {
        return VictimReality.totalProtection(
            helmet, chestplate, leggings, boots,
            isFire(damageType), isExplosion(damageType),
            isProjectile(damageType), isFall(damageType), this.era);
    }

    public List<Observation> evaluateThorns(
            float attackerHealthLost,
            ItemStack victimChestplate) {
        List<Observation> observations = new ArrayList<>();
        if (attackerHealthLost <= 0.0F) {
            if (ExchangeReality.thornsIsPossible(victimChestplate)) {
                observations.add(Observation.context(
                    "victim wears thorns, attacker took no damage"));
            }
            return observations;
        }
        if (ExchangeReality.thornsDamageIsImpossible(
                attackerHealthLost, victimChestplate, this.era)) {
            observations.add(Observation.suspect("thorns",
                "attacker lost " + format(attackerHealthLost)
                    + " which thorns cannot explain (maximum "
                    + format(ExchangeReality.maximumThornsToAttacker(
                        victimChestplate, this.era)) + ")"));
        }
        if (ExchangeReality.attackerLostUnexplainedHealth(
                attackerHealthLost, victimChestplate, this.era)) {
            observations.add(Observation.suspect("thorns-unexplained",
                "attacker lost " + format(attackerHealthLost)
                    + " with no thorns source on the victim"));
        }
        return observations;
    }

    public List<Observation> evaluateExchange(
            float attackerOutgoingDamage,
            float observedVictimHealthLost,
            boolean claimedBlocking,
            double blockingAngleRadians,
            boolean blockBypassed,
            int shieldTicksHeld,
            float totalArmour,
            float armourToughness,
            float totalProtection,
            ActiveEffects victimEffects,
            String damageType,
            boolean damagesHelmet,
            boolean wearingHelmet,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown) {
        List<Observation> observations = new ArrayList<>();

        float expected = ExchangeReality.dealtToVictim(
            attackerOutgoingDamage,
            claimedBlocking,
            blockingAngleRadians,
            blockBypassed,
            totalArmour,
            armourToughness,
            totalProtection,
            victimEffects,
            damageType,
            EffectEra.of(this.protocol),
            damagesHelmet,
            wearingHelmet,
            lastHurt,
            invulnerableTime,
            bypassesCooldown,
            this.protocol);

        if (ExchangeReality.victimDamageDisagreesWithAttacker(
                observedVictimHealthLost, expected)) {
            observations.add(Observation.suspect("exchange-damage",
                "victim lost " + format(observedVictimHealthLost)
                    + ", attacker side predicts " + format(expected)));
        }
        if (ExchangeReality.blockedWithoutShieldReady(
                claimedBlocking, shieldTicksHeld, this.protocol)) {
            observations.add(Observation.suspect("exchange-block-early",
                "blocked after only " + shieldTicksHeld + " ticks of holding"));
        }
        if (ExchangeReality.blockedOutsideAngle(claimedBlocking, blockingAngleRadians)) {
            observations.add(Observation.suspect("exchange-block-angle",
                "blocked a hit arriving at "
                    + format(Math.toDegrees(blockingAngleRadians)) + " degrees"));
        }
        return observations;
    }

    public float attackerOutgoingDamage(
            PlayerSnapshot attacker,
            ItemStack weapon,
            EntityType target,
            float baseAttackDamage,
            float attackStrengthScale,
            float weaponBonus,
            boolean targetIsLiving) {
        return AggressorReality.outgoingDamage(
            weapon,
            target,
            baseAttackDamage,
            attackStrengthScale,
            weaponBonus,
            attacker.fallDistance(),
            attacker.supported(),
            false,
            attacker.inWater(),
            attacker.effects(),
            false,
            targetIsLiving,
            attacker.sprinting(),
            this.era);
    }

    public List<Observation> evaluateClaimedCrit(
            PlayerSnapshot attacker,
            boolean claimedCritical,
            float attackStrengthScale,
            boolean targetIsLiving) {
        List<Observation> observations = new ArrayList<>();
        if (AttackResolution.claimedCriticalIsImpossible(
                claimedCritical,
                attackStrengthScale,
                attacker.fallDistance(),
                attacker.supported(),
                false,
                attacker.inWater(),
                attacker.effects(),
                false,
                targetIsLiving,
                attacker.sprinting())) {
            observations.add(Observation.suspect("attack-crit",
                "claimed a critical while fallDistance "
                    + format(attacker.fallDistance())
                    + " onGround " + attacker.supported()
                    + " sprinting " + attacker.sprinting()));
        }
        return observations;
    }

    public List<Observation> evaluateAttack(
            PlayerSnapshot attacker,
            double distanceSquaredToTarget,
            boolean creative,
            boolean sprintingBefore,
            boolean sprintingAfter,
            float attackStrengthScale,
            int ticksSinceLastAttack,
            double attackSpeed) {
        List<Observation> observations = new ArrayList<>();
        if (AggressorReality.claimedStrengthIsImpossible(
                attackStrengthScale, ticksSinceLastAttack, attackSpeed, 0.0F)) {
            observations.add(Observation.suspect("attack-strength",
                "claimed strength " + format(attackStrengthScale)
                    + " after " + ticksSinceLastAttack + " ticks at speed "
                    + format(attackSpeed)));
        }
        if (AggressorReality.attackedTooFast(
                ticksSinceLastAttack, attackSpeed, 0.0F)) {
            observations.add(Observation.note("attack-rate",
                "attacked after " + ticksSinceLastAttack
                    + " ticks at speed " + format(attackSpeed)));
        }
        if (AggressorReality.attackedOutOfReach(
                distanceSquaredToTarget, creative, this.protocol)) {
            observations.add(Observation.suspect("attack-reach",
                "attacked at " + format(Math.sqrt(distanceSquaredToTarget))
                    + ", bound "
                    + format(AttackReach.interactionRange(creative, this.protocol))));
        }
        boolean knockback = AggressorReality.isKnockbackAttack(
            sprintingBefore, attackStrengthScale);
        if (AggressorReality.keptSprintingAfterKnockback(
                sprintingBefore, sprintingAfter, knockback)) {
            observations.add(Observation.suspect("attack-sprint",
                "kept sprinting through a knockback attack"));
        }
        return observations;
    }

    private static boolean isFire(String damageType) {
        return damageType != null
            && (damageType.contains("fire") || damageType.contains("lava")
                || damageType.contains("hot_floor"));
    }

    private static boolean isExplosion(String damageType) {
        return damageType != null && damageType.contains("explosion");
    }

    private static boolean isProjectile(String damageType) {
        return damageType != null
            && (damageType.contains("projectile") || damageType.contains("arrow")
                || damageType.contains("trident"));
    }

    private static boolean isFall(String damageType) {
        return damageType != null && damageType.contains("fall");
    }

    private void checkReach(
            List<Observation> observations,
            String label,
            PlayerSnapshot snapshot,
            int blockX,
            int blockY,
            int blockZ,
            double blockInteractionRange,
            boolean creative) {
        double eyeY = snapshot.y() + eyeHeight(snapshot);
        if (BreakReality.outOfReach(
                snapshot.x(), eyeY, snapshot.z(),
                blockX, blockY, blockZ,
                blockInteractionRange, creative, this.protocol)) {
            double distance = Math.sqrt(BlockReach.distanceSquared(
                snapshot.x(), eyeY, snapshot.z(), blockX, blockY, blockZ, this.protocol));
            double bound = BlockReach.interactionRange(
                blockInteractionRange, creative, this.protocol);
            observations.add(Observation.suspect(label,
                "distance " + format(distance) + ", bound " + format(bound)));
        }
    }

    private void checkFacing(
            List<Observation> observations,
            String label,
            PlayerSnapshot snapshot,
            int blockX,
            int blockY,
            int blockZ) {
        double eyeY = snapshot.y() + eyeHeight(snapshot);
        if (BreakReality.facingAwayFromTarget(
                snapshot.x(), eyeY, snapshot.z(),
                blockX, blockY, blockZ,
                snapshot.pitch(), snapshot.yaw())) {
            observations.add(Observation.note(label,
                "target at " + blockX + "," + blockY + "," + blockZ
                    + " is outside the look cone (server does not enforce facing)"));
        }
    }

    private static double eyeHeight(PlayerSnapshot snapshot) {
        if (snapshot.sneaking()) {
            return 1.27D;
        }
        if (snapshot.swimming() || snapshot.gliding()) {
            return 0.4D;
        }
        return 1.62D;
    }

    public static String faceBetween(
            int againstX,
            int againstY,
            int againstZ,
            int placedX,
            int placedY,
            int placedZ) {
        int dx = placedX - againstX;
        int dy = placedY - againstY;
        int dz = placedZ - againstZ;
        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) {
            return null;
        }
        if (dy == 1) {
            return BlockFace.UP;
        }
        if (dy == -1) {
            return BlockFace.DOWN;
        }
        if (dz == 1) {
            return BlockFace.SOUTH;
        }
        if (dz == -1) {
            return BlockFace.NORTH;
        }
        return dx == 1 ? BlockFace.EAST : BlockFace.WEST;
    }

    public static String normalise(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT);
    }

    private static String format(double value) {
        return String.format("%.5f", value);
    }
}
