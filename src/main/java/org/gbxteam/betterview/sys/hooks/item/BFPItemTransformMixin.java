package org.gbxteam.betterview.sys.hooks.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTransform.class)
public class BFPItemTransformMixin {
    private static final String BFP_NODE_REF = "bfp_item_transform_mixin";

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;scale(FFF)V"))
    public void bfp$dynamicallyFlipItemModel(boolean bl, PoseStack.Pose pose, CallbackInfo bfp_trace) {
        boolean canFlip = FirstPersonPlayerRenderer.SHOULD_FLIP_ITEM_TRANSFORM;
        if (canFlip && FirstPersonPlayerRenderer.IS_RENDERING_BFP_FIRST_PERSON) {
            pose.rotate(Axis.YP.rotation(Mth.PI));
        }
    }
}
