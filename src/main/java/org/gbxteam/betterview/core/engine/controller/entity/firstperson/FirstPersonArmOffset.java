package org.gbxteam.betterview.core.engine.controller.entity.firstperson;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.skeleton.JointChannel;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.JointTransformerFunction;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.joml.Vector3f;

public class FirstPersonArmOffset {

    public static PoseFunction<LocalSpacePose> constructWithArmXOffset(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> pose = inputPose;

        pose = JointTransformerFunction.localOrParentSpaceBuilder(pose, FirstPersonJointAnimator.RIGHT_ARM_BUFFER_JOINT)
                .setTranslation(
                        context -> new Vector3f(BFPMain.CONFIG.data().firstPersonPlayer.armOffsetX, 0, 0),
                        JointChannel.TransformType.ADD,
                        JointChannel.TransformSpace.PARENT
                ).build();

        pose = JointTransformerFunction.localOrParentSpaceBuilder(pose, FirstPersonJointAnimator.LEFT_ARM_BUFFER_JOINT)
                .setTranslation(
                        context -> new Vector3f(-BFPMain.CONFIG.data().firstPersonPlayer.armOffsetX, 0, 0),
                        JointChannel.TransformType.ADD,
                        JointChannel.TransformSpace.PARENT
                ).build();

        return pose;
    }

    public static PoseFunction<LocalSpacePose> constructWithArmYZOffset(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> pose = inputPose;

        pose = JointTransformerFunction.localOrParentSpaceBuilder(pose, FirstPersonJointAnimator.ARM_BUFFER_JOINT)
                .setTranslation(
                        context -> new Vector3f(
                                0,
                                -BFPMain.CONFIG.data().firstPersonPlayer.armOffsetY,
                                BFPMain.CONFIG.data().firstPersonPlayer.armOffsetZ
                        ),
                        JointChannel.TransformType.ADD,
                        JointChannel.TransformSpace.PARENT
                ).build();

        return pose;
    }

}
