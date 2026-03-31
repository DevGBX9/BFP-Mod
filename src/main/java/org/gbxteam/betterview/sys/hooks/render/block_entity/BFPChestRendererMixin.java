package org.gbxteam.betterview.sys.hooks.render.block_entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.context.AnimationDataContainer;
import org.gbxteam.betterview.core.visuals.BFPWrappedRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ChestRenderer.class)
public class BFPChestRendererMixin {
    private static final String BFP_NODE_REF = "bfp_chest_render_mixin";

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    )
    public void bfp$interceptChestModelSubmission(
            SubmitNodeCollector instance, Model<?> model, Object o, PoseStack poseStack, RenderType renderType, int i, int j, int k, TextureAtlasSprite textureAtlasSprite, int f, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, Operation<Void> original, @Local(argsOnly = true) ChestRenderState renderState
    ) {
        Optional<AnimationDataContainer> dataBox = JointAnimatorDispatcher.getInstance().getBlockEntityAnimationDataContainer(renderState.blockPos, renderState.blockEntityType);
        if (dataBox.isPresent()) {
            BFPWrappedRenderState<?> bfpWrapped = BFPWrappedRenderState.of(o, dataBox.get());
            original.call(instance, model, bfpWrapped, poseStack, renderType, i, j, k, textureAtlasSprite, f, crumblingOverlay);
        } else {
            original.call(instance, model, o, poseStack, renderType, i, j, k, textureAtlasSprite, f, crumblingOverlay);
        }
    }
}
