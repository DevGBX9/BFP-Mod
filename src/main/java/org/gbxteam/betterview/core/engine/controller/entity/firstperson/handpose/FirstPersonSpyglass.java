package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMovementFlows;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonJointAnimator;
import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.context.PoseCalculationContext;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.skeleton.JointChannel;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.*;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateDefinition;
import org.gbxteam.betterview.core.logic.poses.statemachine.PoseManager;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateTransition;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateTransitionContext;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemUseAnimation;
import org.joml.Vector3f;

public class FirstPersonSpyglass {

    public static final String SPYGLASS_IDLE_STATE = "idle";
    public static final String SPYGLASS_LOOKING_STATE = "looking";
    public static final String SPYGLASS_LOOKING_EXIT_STATE = "looking_exit";

    private static String getSpyglassEntryState(DriverGetter driverGetter) {
        return SPYGLASS_IDLE_STATE;
    }

    private static boolean isUsingItem(StateTransitionContext context, InteractionHand hand) {
        return context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
    }

    public static PoseFunction<LocalSpacePose> handSpyglassPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {
        PoseFunction<LocalSpacePose> lookingPoseFunction = SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_SPYGLASS_LOOKING_EXIT).build();
        PoseFunction<LocalSpacePose> lookingExitPoseFunction = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPYGLASS_LOOKING_EXIT)
                .setPlayRate(1)
                .setLooping(false)
                .build();

        return PoseManager.builder(FirstPersonSpyglass::getSpyglassEntryState)
                .defineState(StateDefinition.builder(SPYGLASS_IDLE_STATE, miningPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_LOOKING_STATE)
                                .isTakenIfTrue(context -> isUsingItem(context, hand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SPYGLASS_LOOKING_STATE, lookingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_LOOKING_EXIT_STATE)
                                .isTakenIfTrue(context -> !isUsingItem(context, hand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SPYGLASS_LOOKING_EXIT_STATE, lookingExitPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_LOOKING_STATE)
                                .isTakenIfTrue(context -> isUsingItem(context, hand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .build();
    }

    /**
     * If the item the player is currently using is a spyglass, return a vector with 0. Otherwise return 1.
     */
    private static Vector3f getHiddenScale(PoseCalculationContext context) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand))) {
                ItemUseAnimation itemUseAnimation = context.getDriverValue(FirstPersonDrivers.getItemDriver(hand)).getUseAnimation();
                if (itemUseAnimation == ItemUseAnimation.SPYGLASS) {
                    return new Vector3f(0);
                }
            }
        }
        return new Vector3f(1);
    }

    public static PoseFunction<LocalSpacePose> getHiddenArmsSpyglassPose(PoseFunction<LocalSpacePose> inputPoseFunction) {
        return JointTransformerFunction.localOrParentSpaceBuilder(inputPoseFunction, FirstPersonJointAnimator.ARM_BUFFER_JOINT)
                .setScale(FirstPersonSpyglass::getHiddenScale, JointChannel.TransformType.ADD, JointChannel.TransformSpace.LOCAL)
                .build();
    }
}
