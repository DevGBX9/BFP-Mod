package org.gbxteam.betterview.sys.hooks.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gbxteam.betterview.sys.bridge.LivingEntityRenderStateAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WingsLayer.class)
public abstract class BFPElytraLayerMixin<T extends LivingEntity, S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private static final String BFP_NODE_REF = "bfp_elytra_layer_mixin";

    public BFPElytraLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    private boolean bfp$canApplyElytraTransform(LivingEntityRenderState state) {
        LivingEntityRenderStateAccess access = (LivingEntityRenderStateAccess) state;
        return access.bfp$getInterpolatedAnimationPose().isPresent();
    }
}
