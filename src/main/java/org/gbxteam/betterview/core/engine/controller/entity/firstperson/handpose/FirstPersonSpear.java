package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMovementFlows;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMontages;
import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.*;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;
import org.gbxteam.betterview.core.logic.poses.montage.MontageSlotFunction;
import org.gbxteam.betterview.core.logic.poses.statemachine.*;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.gbxteam.betterview.core.utils.BFPMultiVersionWrappers;

import java.util.Set;

public class FirstPersonSpear {

    public static PoseFunction<LocalSpacePose> constructSpearPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {
        PoseFunction<LocalSpacePose> pose = miningPoseFunction;

        pose = constructChargePoseFunction(cachedPoseContainer, hand, pose);
        pose = constructWithSpearImpact(pose);

        return pose;
    }

    private static PoseFunction<LocalSpacePose> constructWithSpearImpact(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> basePose = SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_POSE_1).build();
        PoseFunction<LocalSpacePose> pose = MontageSlotFunction.of(basePose, FirstPersonMontages.SPEAR_CHARGE_SLOT);
        pose = MakeDynamicAdditiveFunction.of(pose, basePose);
        pose = ApplyAdditiveFunction.of(inputPose, pose);
        return pose;
    }

    public static String CHARGE_IDLE_STATE = "idle";
    public static String CHARGE_ENTER_STATE = "enter";
    public static String CHARGE_STAGE_1_STATE = "stage_1";
    public static String CHARGE_STAGE_1_TO_2_STATE = "stage_1_to_2";
    public static String CHARGE_STAGE_2_STATE = "stage_2";
    public static String CHARGE_STAGE_2_TO_3_STATE = "stage_2_to_3";
    public static String CHARGE_STAGE_3_STATE = "stage_3";
    public static String CHARGE_EXIT_STATE = "exit";

    private static PoseFunction<LocalSpacePose> constructChargePoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {

        PoseFunction<LocalSpacePose> chargeEnterPose = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_ENTER).build();
        PoseFunction<LocalSpacePose> chargeExitPose = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_EXIT).build();
        PoseFunction<LocalSpacePose> chargeStage1Pose = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_POSE_1).build();
        PoseFunction<LocalSpacePose> chargeStage2Pose = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_POSE_2).build();
        PoseFunction<LocalSpacePose> chargeStage3Pose = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_POSE_3).build();
        PoseFunction<LocalSpacePose> chargeStage1To2Pose = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_WEAKEN_1).build();
        PoseFunction<LocalSpacePose> chargeStage2To3Pose = SequencePlayerFunction.builder(FirstPersonMovementFlows.HAND_SPEAR_CHARGE_WEAKEN_2).build();

        PoseFunction<LocalSpacePose> chargeStateMachinePose;
        chargeStateMachinePose = PoseManager.builder(context -> CHARGE_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(CHARGE_IDLE_STATE, miningPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(CHARGE_ENTER_STATE, chargeEnterPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_1_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_1_STATE, chargeStage1Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_1_TO_2_STATE)
                                .isTakenIfTrue(FirstPersonSpear::spearCanNoLongerDismount)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_1_TO_2_STATE, chargeStage1To2Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_2_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_2_STATE, chargeStage2Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_2_TO_3_STATE)
                                .isTakenIfTrue(FirstPersonSpear::spearCanNoLongerKnockback)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_2_TO_3_STATE, chargeStage2To3Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_3_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_3_STATE, chargeStage3Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(CHARGE_EXIT_STATE, chargeExitPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_IDLE_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                CHARGE_STAGE_1_STATE,
                                CHARGE_STAGE_2_STATE,
                                CHARGE_STAGE_3_STATE,
                                CHARGE_STAGE_1_TO_2_STATE,
                                CHARGE_STAGE_2_TO_3_STATE,
                                CHARGE_ENTER_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(CHARGE_EXIT_STATE)
                                .isTakenIfTrue(FirstPersonSpear::spearCanNoLongerDamage)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                CHARGE_EXIT_STATE,
                                CHARGE_IDLE_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(CHARGE_ENTER_STATE)
                                .isTakenIfTrue(context -> isUsingSpear(context, hand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(60)
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .build();
        return chargeStateMachinePose;
    }

    public static boolean isUsingSpear(StateTransitionContext context, InteractionHand hand) {
        boolean isUsing = context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
        boolean handPoseIsSpear = context.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand)) == FirstPersonHandPoses.SPEAR;
        boolean spearCanDamage = context.getDriverValue(FirstPersonDrivers.SPEAR_CAN_DAMAGE);
        return isUsing && handPoseIsSpear && spearCanDamage;
    }

    public static boolean spearCanNoLongerDismount(StateTransitionContext context) {
        boolean spearCanDismount = context.getDriverValue(FirstPersonDrivers.SPEAR_CAN_DISMOUNT);
        return !spearCanDismount;
    }

    public static boolean spearCanNoLongerKnockback(StateTransitionContext context) {
        boolean spearCanKnockback = context.getDriverValue(FirstPersonDrivers.SPEAR_CAN_KNOCKBACK);
        return !spearCanKnockback;
    }

    public static boolean spearCanNoLongerDamage(StateTransitionContext context) {
        boolean spearCanDamage = context.getDriverValue(FirstPersonDrivers.SPEAR_CAN_DAMAGE);
        return !spearCanDamage;
    }

    public static void extractSpearData(LocalPlayer player, DriverGetter driverContainer, MontageManager montageManager) {
        BFPMultiVersionWrappers.bfp$updateSpearDrivers(player, driverContainer);
        for (InteractionHand hand : InteractionHand.values()) {
            //? if >= 1.21.11 {
            int ticksSinceLastSpearImpact = (int) player.getTicksSinceLastKineticHitFeedback(0);
            if (ticksSinceLastSpearImpact == 1) {
                montageManager.playMontage(FirstPersonMontages.SPEAR_CHARGE_IMPACT_MONTAGE);
            }
            //?}
        }
    }

}
