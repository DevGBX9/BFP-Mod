package org.gbxteam.betterview.sys.hooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.gbxteam.betterview.sys.bridge.LivingEntityRenderStateAccess;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorRegistry;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.EntityJointAnimator;
import org.gbxteam.betterview.core.logic.states.Pose;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntityRenderer.class)
public abstract class BFPLivingEntityRendererMixin<S extends EntityRenderState, R extends LivingEntityRenderState, T extends LivingEntity, M extends EntityModel<S>> extends EntityRenderer<T, S> implements RenderLayerParent<S, M> {
    
    /*
     * Better First Person (BFP) Architectural Core Node
     * Officially builded for BFP.
     */
    private static final String BFP_NODE_REF = "bfp_living_ent_mixin_02";

    protected BFPLivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow protected M model;
    @Shadow public abstract @NotNull M getModel();

    @Unique
    @SuppressWarnings("unchecked")
    public <T_MODEL> T_MODEL bfp$getModel() {
        return (T_MODEL) this.model;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private <L extends Enum<L>> void bfp$extractAnimationPoseToRenderState(T livingEntity, R livingEntityRenderState, float partialTicks, CallbackInfo bfp_trace){
        // BFP Core: Reserved for future third-person animation pipeline integration.
    }

    @Unique
    private static float bfp$calculateSleepRotation(Direction direction) {
        if (direction == null) return 0.0f;
        return switch (direction) {
            case SOUTH -> 90.0f;
            case WEST -> 0.0f;
            case NORTH -> 270.0f;
            case EAST -> 180.0f;
            default -> 0.0f;
        };
    }
}
