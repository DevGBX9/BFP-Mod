package org.gbxteam.betterview.sys.hooks.render;

import org.gbxteam.betterview.sys.bridge.LivingEntityRenderStateAccess;
import org.gbxteam.betterview.core.engine.controller.entity.EntityJointAnimator;
import org.gbxteam.betterview.core.logic.states.Pose;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(LivingEntityRenderState.class)
public class BFPLivingEntityRenderStateMixin implements LivingEntityRenderStateAccess {
    private static final String BFP_NODE_REF = "bfp_living_state_mixin";

    @Unique
    private Pose bfp$trackedInterpolatedPose;

    @Unique
    private EntityJointAnimator<?, ?> bfp$activeEntityAnimator;

    @Unique
    @Override
    public void bfp$setInterpolatedAnimationPose(Pose interpolatedPose) {
        this.bfp$trackedInterpolatedPose = interpolatedPose;
    }

    @Override
    public Optional<Pose> bfp$getInterpolatedAnimationPose() {
        return Optional.ofNullable(this.bfp$trackedInterpolatedPose);
    }

    @Override
    public void bfp$setEntityJointAnimator(EntityJointAnimator<?, ?> entityJointAnimator) {
        this.bfp$activeEntityAnimator = entityJointAnimator;
    }

    @Override
    public Optional<EntityJointAnimator<?, ?>> bfp$getEntityJointAnimator() {
        return Optional.ofNullable(this.bfp$activeEntityAnimator);
    }
}
