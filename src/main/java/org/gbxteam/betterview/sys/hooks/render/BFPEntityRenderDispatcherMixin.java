package org.gbxteam.betterview.sys.hooks.render;

import com.llamalad7.mixinextras.sugar.Local;
import org.gbxteam.betterview.sys.bridge.FirstPersonPlayerRendererGetter;
import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(EntityRenderDispatcher.class)
public class BFPEntityRenderDispatcherMixin implements FirstPersonPlayerRendererGetter {
    private static final String BFP_NODE_REF = "bfp_entityrend_mixin";

    @Unique
    private FirstPersonPlayerRenderer bfp$activePlayerRenderer;

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void bfp$initPlayerRendererCore(ResourceManager resourceManager, CallbackInfo bfp_trace, @Local EntityRendererProvider.Context context){
        this.bfp$activePlayerRenderer = new FirstPersonPlayerRenderer(context);
    }

    @Override
    public Optional<FirstPersonPlayerRenderer> bfp$getFirstPersonPlayerRenderer() {
        return Optional.ofNullable(this.bfp$activePlayerRenderer);
    }
}
