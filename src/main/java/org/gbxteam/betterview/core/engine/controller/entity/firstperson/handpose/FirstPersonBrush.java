package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMovementFlows;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.poses.SequencePlayerFunction;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateDefinition;
import org.gbxteam.betterview.core.logic.poses.statemachine.PoseManager;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateTransition;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateTransitionContext;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.world.InteractionHand;

public class FirstPersonBrush {

    private static boolean isUsingItem(StateTransitionContext context, InteractionHand hand) {
        return context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
    }

    public static final String BRUSH_IDLE_STATE = "idle";
    public static final String BRUSH_SIFTING_STATE = "sifting";

    public static PoseFunction<LocalSpacePose> constructBrushPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {
        PoseFunction<LocalSpacePose> siftingPoseFunction = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_BRUSH_SIFT_LOOP)
                .setPlayRate(1)
                .setLooping(true)
                .build();

        return PoseManager.builder(functioncontext -> BRUSH_IDLE_STATE)
                .defineState(StateDefinition.builder(BRUSH_IDLE_STATE, miningPoseFunction)
                        .addOutboundTransition(StateTransition.builder(BRUSH_SIFTING_STATE)
                                .isTakenIfTrue(context -> isUsingItem(context, hand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(true)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(BRUSH_SIFTING_STATE, siftingPoseFunction)
                        .addOutboundTransition(StateTransition.builder(BRUSH_IDLE_STATE)
                                .isTakenIfTrue(context -> !isUsingItem(context, hand))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.EXPONENTIAL_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .build();
    }
}
