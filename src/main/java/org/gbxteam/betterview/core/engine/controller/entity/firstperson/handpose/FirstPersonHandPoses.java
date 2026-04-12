package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMovementFlows;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMining;
import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.context.PoseCalculationContext;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.poses.SequenceEvaluatorFunction;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.visuals.ItemRenderType;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.utils.BFPMultiVersionWrappers;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FirstPersonHandPoses {

    private static final Map<Identifier, HandPoseDefinition> HAND_POSES_BY_IDENTIFIER = new HashMap<>();

    public static Identifier register(Identifier identifier, HandPoseDefinition configuration) {
        HAND_POSES_BY_IDENTIFIER.put(identifier, configuration);
        return identifier;
    }

    public static final Identifier EMPTY_MAIN_HAND = register(BFPMain.makeIdentifier("empty_main_hand"), HandPoseDefinition.builder(
            "empty_main_hand",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_EMPTY_POSE,
            ItemStack::isEmpty,
            10)
            .setHandsToUsePoseIn(InteractionHand.MAIN_HAND)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_EMPTY_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_EMPTY_LOWER)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructEmptyHandMiningPoseFunction)
            .build());

    public static final Identifier EMPTY_OFF_HAND = register(BFPMain.makeIdentifier("empty_off_hand"), HandPoseDefinition.builder(
            "empty_off_hand",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_EMPTY_LOWERED,
            ItemStack::isEmpty,
            10)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_EMPTY_LOWERED)
            .setLowerSequence(FirstPersonMovementFlows.HAND_EMPTY_LOWERED)
            .setHandsToUsePoseIn(InteractionHand.OFF_HAND)
            .build());
    public static final Identifier GENERIC_ITEM = register(BFPMain.makeIdentifier("generic_item"), HandPoseDefinition.builder(
            "generic_item",
            FirstPersonGenericItems::constructPoseFunction,
            FirstPersonGenericItems::getCurrentBasePose,
            itemStack -> true,
            0)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructEmptyHandMiningPoseFunction)
            .build());
    public static final Identifier PICKAXE = register(BFPMain.makeIdentifier("pickaxe"), HandPoseDefinition.builder(
            "pickaxe",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.PICKAXES),
            60)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier AXE = register(BFPMain.makeIdentifier("axe"), HandPoseDefinition.builder(
            "axe",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.AXES),
            50)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructAxeMiningPoseFunction)
            .build());
    public static final Identifier SHOVEL = register(BFPMain.makeIdentifier("shovel"), HandPoseDefinition.builder(
            "shovel",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.SHOVELS),
            40)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .setMiningPoseFunctionSuppler(FirstPersonMining::constructShovelMiningPoseFunction)
            .build());
    public static final Identifier HOE = register(BFPMain.makeIdentifier("hoe"), HandPoseDefinition.builder(
            "hoe",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.HOES),
            40)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier SWORD = register(BFPMain.makeIdentifier("sword"), HandPoseDefinition.builder(
            "sword",
            FirstPersonSword::handSwordPoseFunction,
            FirstPersonMovementFlows.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.SWORDS),
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_SWORD_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier SHIELD = register(BFPMain.makeIdentifier("shield"), HandPoseDefinition.builder(
            "shield",
            FirstPersonShield::constructShieldPoseFunction,
            FirstPersonMovementFlows.HAND_SHIELD_POSE,
            itemStack -> itemStack.getItem() instanceof ShieldItem,
            90)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .setMiningPoseFunctionSuppler(() -> FirstPersonMining.constructPickaxeMiningPoseFunction(SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_POSE).build()))
            .build());
    public static final Identifier BOW = register(BFPMain.makeIdentifier("bow"), HandPoseDefinition.builder(
            "bow",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_BOW_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.BOW,
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier CROSSBOW = register(BFPMain.makeIdentifier("crossbow"), HandPoseDefinition.builder(
            "crossbow",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_CROSSBOW_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.CROSSBOW,
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_CROSSBOW_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier TRIDENT = register(BFPMain.makeIdentifier("trident"), HandPoseDefinition.builder(
            "trident",
            FirstPersonTrident::handTridentPoseFunction,
            FirstPersonMovementFlows.HAND_TRIDENT_POSE,
            itemStack -> itemStack.getUseAnimation() == BFPMultiVersionWrappers.bfp$resolveTridentAnimation(),
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_SPEAR_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_SPEAR_LOWER)
            .build());
    public static final Identifier BRUSH = register(BFPMain.makeIdentifier("brush"), HandPoseDefinition.builder(
            "brush",
            FirstPersonBrush::constructBrushPoseFunction,
            FirstPersonMovementFlows.HAND_BRUSH_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.BRUSH,
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier MACE = register(BFPMain.makeIdentifier("mace"), HandPoseDefinition.builder(
            "mace",
            FirstPersonMace::handMacePoseFunction,
            FirstPersonMovementFlows.HAND_MACE_POSE,
            itemStack -> itemStack.is(ItemTags.MACE_ENCHANTABLE),
            110)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier SPYGLASS = register(BFPMain.makeIdentifier("spyglass"), HandPoseDefinition.builder(
            "spyglass",
            FirstPersonSpyglass::handSpyglassPoseFunction,
            FirstPersonMovementFlows.HAND_SPYGLASS_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.SPYGLASS,
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .build());
    public static final Identifier MAP = register(BFPMain.makeIdentifier("map"), HandPoseDefinition.builder(
            "map",
            HandPoseFunctionSupplier::constructOnlyWithMiningAnimation,
            FirstPersonMovementFlows.HAND_MAP_SINGLE_HAND_POSE,
            itemStack -> itemStack.has(DataComponents.MAP_ID),
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_TOOL_LOWER)
            .setItemRenderType(ItemRenderType.MAP)
            .build());
    //? if >= 1.21.11 {
    public static final Identifier SPEAR = register(BFPMain.makeIdentifier("spear"), HandPoseDefinition.builder(
            "spear",
            FirstPersonSpear::constructSpearPoseFunction,
            FirstPersonMovementFlows.HAND_SPEAR_POSE,
            itemStack -> itemStack.getUseAnimation() == BFPMultiVersionWrappers.bfp$resolveSpearAnimation(),
            100)
            .setRaiseSequence(FirstPersonMovementFlows.HAND_SPEAR_RAISE)
            .setLowerSequence(FirstPersonMovementFlows.HAND_SPEAR_LOWER)
            .build());
    //? }

    public static Identifier getFallback() {
        return GENERIC_ITEM;
    }

    public static Identifier getEmptyMainHand() {
        return EMPTY_MAIN_HAND;
    }

    public static Identifier getEmptyOffHand() {
        return EMPTY_OFF_HAND;
    }

    public static Identifier getEmptyHandPose(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> getEmptyMainHand();
            case OFF_HAND -> getEmptyOffHand();
        };
    }

    public record HandPoseDefinition(
            String stateIdentifier,
            Predicate<ItemStack> choosePoseIfTrue,
            int evaluationPriority,
            HandPoseFunctionSupplier poseFunctionSupplier,
            Supplier<PoseFunction<LocalSpacePose>> miningPoseFunctionSupplier,
            BiFunction<DriverGetter, InteractionHand, Identifier> currentBasePoseSupplier,
            Identifier raiseSequence,
            Identifier lowerSequence,
            Transition raiseToPoseTransition,
            Transition poseToLowerTransition,
            ItemRenderType itemRenderType,
            InteractionHand[] handsToUsePoseIn
    ) {

        public String getRaiseStateIdentifier() {
            return this.stateIdentifier + "_raise";
        }

        public String getLowerStateIdentifier() {
            return this.stateIdentifier + "_lower";
        }

        public PoseFunction<LocalSpacePose> constructBasePoseFunction(InteractionHand hand) {
            return SequenceEvaluatorFunction.builder(context -> this.currentBasePoseSupplier().apply(context, hand)).build();
        }

        public static Builder builder(
                String stateIdentifier,
                HandPoseFunctionSupplier poseFunctionSupplier,
                BiFunction<DriverGetter, InteractionHand, Identifier> basePoseSequence,
                Predicate<ItemStack> choosePoseIfTrue,
                int chooseEvaluationPriority
        ) {
            return new Builder(stateIdentifier, poseFunctionSupplier, basePoseSequence, choosePoseIfTrue, chooseEvaluationPriority);
        }

        public static Builder builder(
                String stateIdentifier,
                HandPoseFunctionSupplier poseFunctionSupplier,
                Identifier basePoseSequence,
                Predicate<ItemStack> choosePoseIfTrue,
                int chooseEvaluationPriority
        ) {
            return new Builder(stateIdentifier, poseFunctionSupplier, (context, hand) -> basePoseSequence, choosePoseIfTrue, chooseEvaluationPriority);
        }

        public static class Builder {
            private final String stateIdentifier;
            private final Predicate<ItemStack> choosePoseIfTrue;
            private final int evaluationPriority;
            private final HandPoseFunctionSupplier poseFunctionSupplier;
            private final BiFunction<DriverGetter, InteractionHand, Identifier> basePoseSequenceSupplier;

            private Supplier<PoseFunction<LocalSpacePose>> miningPoseFunctionSupplier;
            private Identifier raiseSequence;
            private Identifier lowerSequence;
            private Transition raiseToPoseTransition;
            private Transition poseToLowerTransition;
            private ItemRenderType itemRenderType;
            private InteractionHand[] handsToUsePoseIn;

            private Builder(
                    String stateIdentifier,
                    HandPoseFunctionSupplier poseFunctionSupplier,
                    BiFunction<DriverGetter, InteractionHand, Identifier> basePoseSequenceSupplier,
                    Predicate<ItemStack> choosePoseIfTrue,
                    int evaluationPriority
            ) {
                this.stateIdentifier = stateIdentifier;
                this.poseFunctionSupplier = poseFunctionSupplier;
                this.choosePoseIfTrue = choosePoseIfTrue;
                this.evaluationPriority = evaluationPriority;
                this.basePoseSequenceSupplier = basePoseSequenceSupplier;

                this.miningPoseFunctionSupplier = FirstPersonMining::constructPickaxeMiningPoseFunction;
                this.raiseSequence = FirstPersonMovementFlows.HAND_GENERIC_ITEM_RAISE;
                this.lowerSequence = FirstPersonMovementFlows.HAND_GENERIC_ITEM_LOWER;
                this.raiseToPoseTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.poseToLowerTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.itemRenderType = ItemRenderType.THIRD_PERSON_ITEM;
                this.handsToUsePoseIn = InteractionHand.values();
            }

            public Builder setRaiseSequence(Identifier sequence) {
                this.raiseSequence = sequence;
                return this;
            }

            public Builder setLowerSequence(Identifier sequence) {
                this.lowerSequence = sequence;
                return this;
            }

            public Builder setRaiseToPoseTransition(Transition transition) {
                this.raiseToPoseTransition = transition;
                return this;
            }

            public Builder setPoseToLowerTransition(Transition transition) {
                this.poseToLowerTransition = transition;
                return this;
            }

            public Builder setItemRenderType(ItemRenderType renderType) {
                this.itemRenderType = renderType;
                return this;
            }

            public Builder setHandsToUsePoseIn(InteractionHand... hands) {
                this.handsToUsePoseIn = hands;
                return this;
            }

            public Builder setMiningPoseFunctionSuppler(Supplier<PoseFunction<LocalSpacePose>> miningPoseFunctionSuppler) {
                this.miningPoseFunctionSupplier = miningPoseFunctionSuppler;
                return this;
            }

            public HandPoseDefinition build() {
                return new HandPoseDefinition(
                        this.stateIdentifier,
                        this.choosePoseIfTrue,
                        this.evaluationPriority,
                        this.poseFunctionSupplier,
                        this.miningPoseFunctionSupplier,
                        this.basePoseSequenceSupplier,
                        this.raiseSequence,
                        this.lowerSequence,
                        this.raiseToPoseTransition,
                        this.poseToLowerTransition,
                        this.itemRenderType,
                        this.handsToUsePoseIn
                );
            }
        }
    }

    @FunctionalInterface
    public interface HandPoseFunctionSupplier {
        PoseFunction<LocalSpacePose> constructHandPose(
                CachedPoseContainer cachedPoseContainer,
                InteractionHand hand,
                PoseFunction<LocalSpacePose> miningPoseFunction
        );

        static PoseFunction<LocalSpacePose> constructOnlyWithMiningAnimation(
                CachedPoseContainer cachedPoseContainer,
                InteractionHand hand,
                PoseFunction<LocalSpacePose> miningPoseFunction
        ) {
            return miningPoseFunction;
        }
    }

    public static HandPoseDefinition getOrThrowFromIdentifier(Identifier identifier) {
        HandPoseDefinition definition = HAND_POSES_BY_IDENTIFIER.get(identifier);
        if (definition == null) {
            throw new RuntimeException("Identifier " + identifier + " is not a registered hand pose.");
        }
        return definition;
    }

    public static Set<Identifier> getRegisteredHandPoseDefinitions() {
        return HAND_POSES_BY_IDENTIFIER.keySet();
    }

    public static Identifier testForNextHandPose(ItemStack itemStack, InteractionHand hand) {

        Map<Identifier, HandPoseDefinition> handPosesSortedByPriority = HAND_POSES_BY_IDENTIFIER.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        for (Identifier key : handPosesSortedByPriority.keySet()) {
            HandPoseDefinition definition = HAND_POSES_BY_IDENTIFIER.get(key);
            boolean poseHasBeenChosen = definition.choosePoseIfTrue().test(itemStack);
            boolean poseCanPlayInCurrentHand = Arrays.asList(definition.handsToUsePoseIn()).contains(hand);
            if (poseHasBeenChosen && poseCanPlayInCurrentHand) {
                return key;
            }
        }
        return getFallback();
    }
}
