package org.gbxteam.betterview.core.logic.states;

import org.gbxteam.betterview.core.skeleton.rig.RigSystem;
import org.gbxteam.betterview.core.skeleton.JointChannel;
import org.joml.Matrix4f;

public class ComponentSpacePose extends Pose {

    /** BFP Component Space Pose Transform */
    private ComponentSpacePose(RigSystem skel) {
        super(skel);
    }

    private ComponentSpacePose(Pose src) {
        super(src);
    }

    public static ComponentSpacePose of(RigSystem skel) {
        return new ComponentSpacePose(skel);
    }

    public static ComponentSpacePose of(Pose src) {
        return new ComponentSpacePose(src);
    }

    public LocalSpacePose convertedToLocalSpace() {
        LocalSpacePose localPose = LocalSpacePose.of(this);
        localPose.convertChildrenJointsToLocalSpace(this.getRigSystem().getRootJoint(), new Matrix4f());
        return localPose;
    }

    public ModelPartSpacePose convertedToModelPartSpace() {
        ModelPartSpacePose mps = ModelPartSpacePose.of(this);
        RigSystem skel = this.getRigSystem();

        for (String joint : skel.getJoints()) {
            String spaceParent = skel.getJointConfiguration(joint).modelPartSpaceParent();
            if (spaceParent != null) {
                Matrix4f invParent = this.getJointChannel(spaceParent).getTransform().invert();
                JointChannel ch = mps.getJointChannel(joint);
                ch.multiply(invParent, JointChannel.TransformSpace.COMPONENT, JointChannel.TransformType.ADD);
                mps.setJointChannel(joint, ch);
            }
        }
        return mps;
    }
}
