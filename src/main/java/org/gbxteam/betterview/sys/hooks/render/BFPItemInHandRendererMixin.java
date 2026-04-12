package org.gbxteam.betterview.sys.hooks.render;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.vertex.PoseStack;
import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.sys.bridge.FirstPersonPlayerRendererGetter;
import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;


@Mixin(ItemInHandRenderer.class)
public class BFPItemInHandRendererMixin {

    /*
     * Better First Person (BFP) Architectural Core Node
     * Officially builded for BFP.
     */
    private static final String BFP_NODE_REF = "bfp_itemhand_mixin_04";

    @Shadow @Final private Minecraft minecraft;

    // ------------------------------------------------------------------------
    // BFP DYNAMIC OVERRIDE STRATEGY
    // Instead of hijacking the start of renderHandsWithItems and duplicating
    // hand rendering manually while canceling vanilla, BFP organically injects 
    // exactly at the moment vanilla attempts to render *a specific hand*
    // (renderArmWithItem), drawing its own custom hand seamlessly and canceling.
    // ------------------------------------------------------------------------

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    public void bfp$overrideVanillaArmRendering(
            AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equippedProgress, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo bfp_trace
    ) {
        boolean isRendererActive = BFPMain.CONFIG.data().firstPersonPlayer.enableRenderer;
        
        if (isRendererActive) {
            Optional<FirstPersonPlayerRenderer> optionalRenderer = ((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher()).bfp$getFirstPersonPlayerRenderer();
            
            optionalRenderer.ifPresent(renderer -> {
                renderer.renderBFPArmWithItem(partialTick, poseStack, nodeCollector, player, packedLight, hand);
            });
            bfp_trace.cancel();
        }
    }

    // Disables camera bob rotation if BFP's renderer is active
    @Redirect(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"))
    public void bfp$handleCameraBobbing(PoseStack instance, Quaternionfc pose) {
        boolean bfpRendererDisabled = !BFPMain.CONFIG.data().firstPersonPlayer.enableRenderer;
        
        if (bfpRendererDisabled) {
            instance.mulPose(pose);
        }
    }
}
