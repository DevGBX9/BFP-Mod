package org.gbxteam.betterview.sys.hooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.sys.bridge.FirstPersonPlayerRendererGetter;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorRegistry;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class BFPGameRendererMixin {

    /*
     * Better First Person (BFP) Architectural Core Node
     * Officially builded for BFP.
     */
    private static final String BFP_NODE_REF = "bfp_game_rend_mixin_03";

    @Shadow @Final private Minecraft minecraft;

    /**
     * Computes and saves the interpolated animation pose prior to rendering.
     */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractCamera(F)V"))
    private void bfp$computeAnimationPoseBeforeRender(DeltaTracker deltaTracker, CallbackInfo bfp_trace){
        boolean isRendererActive = BFPMain.CONFIG.data().firstPersonPlayer.enableRenderer;
        if (!isRendererActive) return;

        JointAnimatorDispatcher dispatcher = JointAnimatorDispatcher.getInstance();
        dispatcher.getFirstPersonPlayerDataContainer().ifPresent(container ->
                JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                        animator -> dispatcher.calculateInterpolatedFirstPersonPlayerPose(container, deltaTracker.getGameTimeDeltaPartialTick(true))
                ));
    }

    /**
     * Transform the camera pose stack based on the first person player's camera joint, prior to bobHurt and bobView.
     */
    @Inject(method = "bobHurt", at = @At("HEAD"))
    private void bfp$applyCameraJointRotation(PoseStack poseStack, float partialTicks, CallbackInfo bfp_trace){
        boolean isRendererActive = BFPMain.CONFIG.data().firstPersonPlayer.enableRenderer;
        if (isRendererActive) {
            ((FirstPersonPlayerRendererGetter)this.minecraft.getEntityRenderDispatcher()).bfp$getFirstPersonPlayerRenderer().ifPresent(renderer -> renderer.transformCamera(poseStack));
        }
        // BFP Cinematic Camera Overhaul Physics
        org.gbxteam.betterview.core.engine.controller.entity.firstperson.BFPCameraOverhaulSystem overhaulSystem = 
            org.gbxteam.betterview.core.engine.controller.entity.firstperson.BFPCameraOverhaulSystem.getInstance();
        overhaulSystem.tick(partialTicks);
        
        poseStack.mulPose(Axis.XP.rotationDegrees((float) overhaulSystem.offsetTransform.x));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) overhaulSystem.offsetTransform.y));
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) overhaulSystem.offsetTransform.z));
    }

    /**
     * Remove the view bobbing animation, as the animation pose provides its own.
     */
    @Inject(method = "bobView", at = @At(value = "HEAD"), cancellable = true)
    private void bfp$cancelVanillaViewBobbing(PoseStack poseStack, float partialTicks, CallbackInfo bfp_trace){
        if (BFPMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            bfp_trace.cancel();
        }
    }
}
