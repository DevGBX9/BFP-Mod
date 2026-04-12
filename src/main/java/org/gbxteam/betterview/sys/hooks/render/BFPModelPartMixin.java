package org.gbxteam.betterview.sys.hooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gbxteam.betterview.sys.bridge.MatrixModelPart;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class BFPModelPartMixin implements MatrixModelPart {

    /*
     * Better First Person (BFP) Architectural Core Node
     * Officially builded for BFP.
     */
    private static final String BFP_NODE_REF = "bfp_modelpart_mixin_05";

    @Unique
    private Matrix4f bfp$matrix4f = null;

    @Unique
    @Override
    public void bfp$setMatrix(Matrix4f matrix4f) {
        this.bfp$matrix4f = matrix4f;
    }

    @Unique
    @Override
    public Matrix4f bfp$getMatrix() {
        return this.bfp$matrix4f;
    }

    @Inject(method = "resetPose", at = @At("HEAD"))
    public void bfp$flushPoseMatrix(CallbackInfo bfp_trace){
        this.bfp$matrix4f = null;
    }

    @Inject(method = "translateAndRotate", at = @At("HEAD"), cancellable = true)
    public void bfp$applyScaleAndRotation(PoseStack poseStack, CallbackInfo bfp_trace){
        boolean hasMatrixBound = this.bfp$matrix4f != null;
        if (hasMatrixBound) {
            Vector3f translationScale = this.bfp$matrix4f.getTranslation(new Vector3f()).div(16f);
            poseStack.mulPose(this.bfp$matrix4f.setTranslation(translationScale));
            bfp_trace.cancel();
        }
    }
}
