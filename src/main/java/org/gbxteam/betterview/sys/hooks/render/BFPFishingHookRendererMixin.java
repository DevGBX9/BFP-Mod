package org.gbxteam.betterview.sys.hooks.render;

import com.mojang.blaze3d.Blaze3D;
import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonJointAnimator;
import org.gbxteam.betterview.sys.settings.BFPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHookRenderer.class)
public abstract class BFPFishingHookRendererMixin {
    private static final String BFP_NODE_REF = "bfp_fishing_mixin";

    @Shadow public static HumanoidArm getHoldingArm(Player player) { return null; }

    @Inject(method = "getPlayerHandPos", at = @At("HEAD"), cancellable = true)
    private void bfp$dynamicallyBindFishingHookTransform(Player player, float handAngle, float partialTick, CallbackInfoReturnable<Vec3> bfp_ret) {
        if (!BFPMain.CONFIG.data().firstPersonPlayer.enableRenderer) return;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        if (dispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            
            JointAnimatorDispatcher jointAnimDispatcher = JointAnimatorDispatcher.getInstance();
            jointAnimDispatcher.getFirstPersonPlayerDataContainer()
                .flatMap(container -> jointAnimDispatcher.getInterpolatedFirstPersonPlayerPose())
                .ifPresent(pose -> {
                    
                    float fovScaleFactor = dispatcher.options.fov().get();
                    fovScaleFactor = ((1 / 70f) / 70f) * (fovScaleFactor * fovScaleFactor);

                    HumanoidArm activeArm = getHoldingArm(player);
                    String jointId = activeArm == HumanoidArm.LEFT ? FirstPersonJointAnimator.LEFT_ITEM_JOINT : FirstPersonJointAnimator.RIGHT_ITEM_JOINT;
                    
                    Matrix4f itemTransMat = new Matrix4f()
                            .scale(1f/16f)
                            .scale(1, -1, -1)
                            .mul(pose.getJointChannel(jointId).getTransform())
                            .translate(0, 10, 5);

                    float xRotFilter = player.getXRot(partialTick) * Mth.DEG_TO_RAD;
                    float yRotFilter = player.getYRot(partialTick) * -Mth.DEG_TO_RAD;
                    Quaternionf activeRotationSet = new Quaternionf().rotateY(yRotFilter).rotateX(xRotFilter);
                    
                    Matrix4f playerTransMat = new Matrix4f()
                            .translate(dispatcher.camera.position().toVector3f())
                            .rotate(activeRotationSet);

                    Matrix4f camCenterTransMat = new Matrix4f()
                            .scale(1f/16f)
                            .scale(1, -1, -1)
                            .mul(pose.getJointChannel(FirstPersonJointAnimator.CAMERA_JOINT).getTransform())
                            .translate(0, 0, -5);

                    Matrix4f fovLerpedTransform = camCenterTransMat.lerp(itemTransMat, fovScaleFactor);

                    Vector3f lineAttachPoint = playerTransMat
                            .translate(fovLerpedTransform.getTranslation(new Vector3f()))
                            .rotate(fovLerpedTransform.getNormalizedRotation(new Quaternionf()))
                            .getTranslation(new Vector3f());

                    bfp_ret.setReturnValue(new Vec3(lineAttachPoint));
            });
        }
    }
}
