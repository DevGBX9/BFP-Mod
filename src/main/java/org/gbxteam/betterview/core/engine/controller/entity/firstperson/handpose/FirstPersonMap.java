package org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonMovementFlows;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonJointAnimator;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.skeleton.rig.BlendMask;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.BlendPosesFunction;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.poses.SequenceEvaluatorFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public class FirstPersonMap {

    public static PoseFunction<LocalSpacePose> blendAdditiveMovementIfHoldingMap(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> pose = inputPose;

        for (InteractionHand hand : InteractionHand.values()) {
            pose = blendAdditiveMovementIfHoldingMapInHand(pose, hand);
        }

        return pose;
    }

    public static PoseFunction<LocalSpacePose> blendAdditiveMovementIfHoldingMapInHand(
            PoseFunction<LocalSpacePose> inputPose,
            InteractionHand hand
    ) {
        BlendMask blendMask = BlendMask.builder()
                .defineForMultipleJoints(switch(hand) {
                    case MAIN_HAND -> FirstPersonJointAnimator.RIGHT_SIDE_JOINTS;
                    case OFF_HAND -> FirstPersonJointAnimator.LEFT_SIDE_JOINTS;
                }, 1f)
                .build();

        return BlendPosesFunction.builder(inputPose)
                .addBlendInput(
                        SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.GROUND_MOVEMENT_POSE).build(),
                        context -> getMapMovementAnimationWeight(context, hand),
                        blendMask)
                .build();
    }

    public static float getMapMovementAnimationWeight(PoseTickEvaluationContext context, InteractionHand hand) {
        Identifier handPose = context.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        if (handPose == FirstPersonHandPoses.MAP) {
            return 1 - BFPMain.CONFIG.data().firstPersonPlayer.mapMovementAnimationIntensity;
        }
        return 0;
    }

}
