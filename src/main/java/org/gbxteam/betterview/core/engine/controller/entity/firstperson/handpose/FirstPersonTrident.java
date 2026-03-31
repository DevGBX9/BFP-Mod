package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMovementFlows;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.engine.motors.VariableDriver;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.poses.SequencePlayerFunction;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.statemachine.*;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.world.InteractionHand;

import java.util.Set;

public class FirstPersonTrident {

    public static final String TRIDENT_IDLE_STATE = "idle";
    public static final String TRIDENT_CHARGE_THROW_STATE = "charge_throw";
    public static final String TRIDENT_RIPTIDE_STATE = "riptide";
    public static final String TRIDENT_RIPTIDE_END_STATE = "riptide_end";

    private static String getTridentEntryState(DriverGetter driverGetter) {
        return TRIDENT_IDLE_STATE;
    }

    public static PoseFunction<LocalSpacePose> handTridentPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {
        DriverKey<VariableDriver<Boolean>> usingItemDriverKey = FirstPersonDrivers.getUsingItemDriver(hand);

        PoseFunction<LocalSpacePose> tridentStateMachine;
        tridentStateMachine = PoseManager.builder(FirstPersonTrident::getTridentEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(TRIDENT_IDLE_STATE, miningPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(TRIDENT_CHARGE_THROW_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_TRIDENT_CHARGE_THROW).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TRIDENT_RIPTIDE_END_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(usingItemDriverKey).negate())
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(15)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(TRIDENT_RIPTIDE_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_TRIDENT_RIPTIDE).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TRIDENT_RIPTIDE_END_STATE)
                                .isTakenOnAnimationFinished(1)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_IN_RIPTIDE).negate())
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(TRIDENT_RIPTIDE_END_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_TRIDENT_RIPTIDE_END).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TRIDENT_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                TRIDENT_IDLE_STATE,
                                TRIDENT_RIPTIDE_END_STATE,
                                TRIDENT_RIPTIDE_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(TRIDENT_CHARGE_THROW_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(usingItemDriverKey))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                TRIDENT_IDLE_STATE,
                                TRIDENT_RIPTIDE_END_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(TRIDENT_RIPTIDE_STATE)
                                .isTakenIfTrue(context -> shouldPlayRiptideAnimation(context, hand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        .build())
                .build();

        return tridentStateMachine;
    }

    private static boolean shouldPlayRiptideAnimation(StateTransitionContext context, InteractionHand hand) {
        boolean isInRiptide = context.getDriverValue(FirstPersonDrivers.IS_IN_RIPTIDE);
        InteractionHand lastUsedHand = context.getDriverValue(FirstPersonDrivers.LAST_USED_HAND);
        return isInRiptide && lastUsedHand == hand;
    }
}
