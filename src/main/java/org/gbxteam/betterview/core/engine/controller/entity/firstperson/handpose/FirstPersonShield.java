package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.*;
import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.skeleton.rig.BlendMask;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.*;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.montage.MontageSlotFunction;
import org.gbxteam.betterview.core.logic.poses.statemachine.*;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.world.InteractionHand;

import java.util.Set;
import java.util.function.Predicate;

public class FirstPersonShield {

    public static String SHIELD_MAIN_HAND_CACHE = "shield_main_hand";
    public static String SHIELD_OFF_HAND_CACHE = "shield_off_hand";

    public static String getShieldCacheIdentifier(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? SHIELD_MAIN_HAND_CACHE : SHIELD_OFF_HAND_CACHE;
    }

    public static boolean isUsingShield(StateTransitionContext context, InteractionHand hand) {
        boolean isUsing = context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
        boolean handPoseIsShield = context.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand)) == FirstPersonHandPoses.SHIELD;
        return isUsing && handPoseIsShield;
    }

    public static boolean hasShieldEnteredCooldown(StateTransitionContext context, InteractionHand hand) {
        boolean isHandOnCooldown = context.getDriverValue(FirstPersonDrivers.getItemOnCooldownDriver(hand));
        boolean wasUsingShield = context.getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue();
        return isHandOnCooldown && wasUsingShield;
    }

    public static boolean isShieldNotOnCooldown(StateTransitionContext context, InteractionHand hand) {
        boolean isHandOnCooldown = context.getDriverValue(FirstPersonDrivers.getItemOnCooldownDriver(hand));
        return !isHandOnCooldown;
    }

    public static PoseFunction<LocalSpacePose> getShieldCachedPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand
    ) {
        String shieldCacheIdentifier = getShieldCacheIdentifier(hand);
        return cachedPoseContainer.getOrThrow(shieldCacheIdentifier);
    }

    public static PoseFunction<LocalSpacePose> constructShieldPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPosefunction
    ) {
        PoseFunction<LocalSpacePose> shieldStateMachine = constructShieldStateMachine(cachedPoseContainer, hand, miningPosefunction);

        String shieldCacheIdentifier = getShieldCacheIdentifier(hand);
        cachedPoseContainer.register(shieldCacheIdentifier, shieldStateMachine, true);
        return getShieldCachedPoseFunction(cachedPoseContainer, hand);
    }

    public static PoseFunction<LocalSpacePose> constructWithHandsOffsetByShield(
            CachedPoseContainer cachedPoseContainer,
            PoseFunction<LocalSpacePose> inputPose
    ) {
        PoseFunction<LocalSpacePose> pose = inputPose;
        for (InteractionHand hand : InteractionHand.values()) {
            BlendMask mask = FirstPersonJointAnimator.LEFT_SIDE_MASK;
            pose = constructWithHandOffsetByShield(cachedPoseContainer, pose, hand, mask);
        }
        return pose;
    }

    public static PoseFunction<LocalSpacePose> constructWithHandOffsetByShield(
            CachedPoseContainer cachedPoseContainer,
            PoseFunction<LocalSpacePose> inputPose,
            InteractionHand hand,
            BlendMask handMask
    ) {
        PoseFunction<LocalSpacePose> shieldStateMachine = getShieldCachedPoseFunction(cachedPoseContainer, hand);
        PoseFunction<LocalSpacePose> baseShieldPose;
        baseShieldPose = SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_POSE).build();

        PoseFunction<LocalSpacePose> additiveShieldStateMachine;
        additiveShieldStateMachine = MakeDynamicAdditiveFunction.of(shieldStateMachine, baseShieldPose);
        additiveShieldStateMachine = BlendPosesFunction.builder(EmptyPoseFunction.of(false))
                .addBlendInput(additiveShieldStateMachine, context -> 1f, handMask)
                .build();

        if (hand == InteractionHand.OFF_HAND) {
            additiveShieldStateMachine = MirrorFunction.of(additiveShieldStateMachine);
        }

        PoseFunction<LocalSpacePose> inputWithAdditivePose;
        inputWithAdditivePose = ApplyAdditiveFunction.of(inputPose, additiveShieldStateMachine);
        return inputWithAdditivePose;
    }


    public static PoseFunction<LocalSpacePose> constructStaticShieldMiningStateMachine(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand
    ) {
        PoseFunction<LocalSpacePose> shieldPose = SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_POSE).build();
        PoseFunction<LocalSpacePose> pose;
        if (hand == InteractionHand.MAIN_HAND) {
            PoseFunction<LocalSpacePose> baseMiningPose = SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_TOOL_POSE).build();
            pose = FirstPersonMining.constructPickaxeMiningPoseFunction();
            pose = MakeDynamicAdditiveFunction.of(pose, baseMiningPose);
            pose = ApplyAdditiveFunction.of(shieldPose, pose);
        } else {
            pose = shieldPose;
        }
        return pose;
    }

    private static PoseFunction<LocalSpacePose> constructShieldStateMachine(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPosefunction
    ) {
        Predicate<StateTransitionContext> isUsingShieldPredicate = context -> isUsingShield(context, hand);
        Predicate<StateTransitionContext> isNotUsingShieldPredicate = isUsingShieldPredicate.negate();

        PoseFunction<LocalSpacePose> shieldStateMachine;
        shieldStateMachine = PoseManager.builder(FirstPersonShield::getShieldEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(SHIELD_LOWERED_STATE, miningPosefunction)
                        .build())
                .defineState(StateDefinition.builder(SHIELD_BLOCKING_IN_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_BLOCK_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SHIELD_BLOCKING_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(5)).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SHIELD_BLOCKING_STATE, MontageSlotFunction.of(SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_BLOCK_OUT).build(), FirstPersonMontages.SHIELD_BLOCK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(SHIELD_BLOCKING_OUT_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_BLOCK_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SHIELD_LOWERED_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(15)).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SHIELD_DISABLED_IN_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_DISABLE_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SHIELD_DISABLED_STATE)
                                .isTakenOnAnimationFinished(0)
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SHIELD_DISABLED_STATE, SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SHIELD_DISABLED_OUT_STATE)
                                .isTakenIfTrue(context -> isShieldNotOnCooldown(context, hand))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SHIELD_DISABLED_OUT_STATE, SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SHIELD_LOWERED_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        SHIELD_BLOCKING_IN_STATE,
                                        SHIELD_BLOCKING_STATE,
                                        SHIELD_BLOCKING_OUT_STATE,
                                        SHIELD_LOWERED_STATE,
                                        SHIELD_DISABLED_OUT_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(SHIELD_DISABLED_IN_STATE)
                                .isTakenIfTrue(context -> hasShieldEnteredCooldown(context, hand))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        SHIELD_BLOCKING_IN_STATE,
                                        SHIELD_BLOCKING_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(SHIELD_BLOCKING_OUT_STATE)
                                .isTakenIfTrue(isNotUsingShieldPredicate
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED)
                                )
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).build())
                                .setPriority(50)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        SHIELD_BLOCKING_IN_STATE,
                                        SHIELD_BLOCKING_STATE,
                                        SHIELD_BLOCKING_OUT_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(SHIELD_LOWERED_STATE)
                                .isTakenIfTrue(isNotUsingShieldPredicate
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED)
                                        .and(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING))
                                )
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).build())
                                .setPriority(60)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        SHIELD_BLOCKING_OUT_STATE,
                                        SHIELD_DISABLED_OUT_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(SHIELD_BLOCKING_IN_STATE)
                                .isTakenIfTrue(isUsingShieldPredicate
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .bindToOnTransitionTaken(FirstPersonAttackAnimations::cancelAttackOffHandOffset)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        SHIELD_LOWERED_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(SHIELD_BLOCKING_IN_STATE)
                                .isTakenIfTrue(isUsingShieldPredicate)
                                .bindToOnTransitionTaken(FirstPersonAttackAnimations::cancelAttackOffHandOffset)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();

        return shieldStateMachine;
    }

    public static final String SHIELD_LOWERED_STATE = "lowered";
    public static final String SHIELD_BLOCKING_IN_STATE = "blocking_in";
    public static final String SHIELD_BLOCKING_STATE = "blocking";
    public static final String SHIELD_BLOCKING_OUT_STATE = "blocking_out";
    public static final String SHIELD_DISABLED_IN_STATE = "disabled_in";
    public static final String SHIELD_DISABLED_STATE = "disabled";
    public static final String SHIELD_DISABLED_OUT_STATE = "disabled_out";

    private static String getShieldEntryState(DriverGetter driverGetter) {
        return SHIELD_LOWERED_STATE;
    }

    enum ShieldStates {
        LOWERED,
        BLOCKING_IN,
        BLOCKING,
        BLOCKING_OUT,
        DISABLED_IN,
        DISABLED,
        DISABLED_OUT
    }
}