package org.gbxteam.betterview.sys.bridge;

import org.gbxteam.betterview.core.engine.controller.entity.EntityJointAnimator;
import org.gbxteam.betterview.core.logic.states.Pose;

import java.util.Optional;

public interface LivingEntityRenderStateAccess {
    void bfp$setInterpolatedAnimationPose(Pose interpolatedPose);
    Optional<Pose> bfp$getInterpolatedAnimationPose();

    void bfp$setEntityJointAnimator(EntityJointAnimator<?, ?> livingEntityJointAnimator);
    Optional<EntityJointAnimator<?, ?>> bfp$getEntityJointAnimator();
}
