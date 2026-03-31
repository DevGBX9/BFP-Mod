package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMovementFlows;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.ApplyAdditiveFunction;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.poses.SequencePlayerFunction;
import org.gbxteam.betterview.core.logic.poses.SequenceReferencePoint;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.statemachine.*;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.Objects;
import java.util.Set;

public class FirstPersonDrinking {

    public static final String DRINKING_IDLE_STATE = "idle";
    public static final String DRINKING_BEGIN_STATE = "drinking_begin";
    public static final String DRINKING_LOOP_STATE = "drinking_loop";
    public static final String DRINKING_FINISHED_STATE = "drinking_finished";

    public static PoseFunction<LocalSpacePose> constructWithDrinkingStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand hand, PoseFunction<LocalSpacePose> idlePoseFunction) {
        PoseFunction<LocalSpacePose> drinkingLoopPoseFunction = ApplyAdditiveFunction.of(
                SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_GENERIC_ITEM_DRINK_PROGRESS)
                        .setPlayRate(context -> context.getDriverValue(FirstPersonDrivers.ITEM_CONSUMPTION_SPEED))
                        .build(),
                SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_GENERIC_ITEM_DRINK_LOOP)
                        .setLooping(true)
                        .setPlayRate(1f)
                        .isAdditive(true, SequenceReferencePoint.BEGINNING)
                        .build()
        );

        // Eating pose functions
        PoseFunction<LocalSpacePose> eatingLoopPoseFunction = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_GENERIC_ITEM_EAT_LOOP)
                .setPlayRate(1.5f)
                .setLooping(true)
                .build();
        PoseFunction<LocalSpacePose> eatingBeginPoseFunction = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_GENERIC_ITEM_EAT_BEGIN).build();

        return PoseManager.builder(context -> DRINKING_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(DRINKING_IDLE_STATE, idlePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                // Drinking
                .defineState(StateDefinition.builder(DRINKING_BEGIN_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_GENERIC_ITEM_DRINK_BEGIN)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(DRINKING_LOOP_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(DRINKING_LOOP_STATE, drinkingLoopPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(DRINKING_FINISHED_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_GENERIC_ITEM_DRINK_FINISH)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(DRINKING_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        DRINKING_BEGIN_STATE,
                                        DRINKING_LOOP_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(DRINKING_FINISHED_STATE)
                                .isTakenIfTrue(context -> !isDrinking(context, hand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(context -> FirstPersonDrivers.updateRenderedItem(context, hand))
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        DRINKING_FINISHED_STATE,
                                        DRINKING_IDLE_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(DRINKING_BEGIN_STATE)
                                .isTakenIfTrue(context -> isDrinking(context, hand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(context -> updateConsumptionSpeed(context, hand))
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        DRINKING_BEGIN_STATE,
                                        DRINKING_LOOP_STATE,
                                        DRINKING_FINISHED_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(DRINKING_IDLE_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING))
                                .setCanInterruptOtherTransitions(true)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(context -> updateConsumptionSpeed(context, hand))
                                .build())
                        .build())
                .build();
    }

    public static void updateConsumptionSpeed(PoseTickEvaluationContext context, InteractionHand hand) {
        ItemStack item = context.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        if (!item.has(DataComponents.CONSUMABLE)) {
            return;
        }
        float speed = Objects.requireNonNull(item.get(DataComponents.CONSUMABLE)).consumeSeconds();
        speed = 1f / Math.max(speed, 0.1f);
        context.getDriver(FirstPersonDrivers.ITEM_CONSUMPTION_SPEED).setValue(speed);
    }

    private static boolean isDrinking(StateTransitionContext context, InteractionHand hand) {
        if (!context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand))) {
            return false;
        }
        return context.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand)).getUseAnimation() == ItemUseAnimation.DRINK;
    }
}
