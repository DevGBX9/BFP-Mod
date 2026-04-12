package org.gbxteam.betterview.sys.hooks.item.property;

import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.UseCycle;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UseCycle.class)
public class BFPUseCycleMixin {
    private static final String BFP_NODE_REF = "bfp_usecycle_mixin";

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void bfp$overrideUseCycle(ItemStack stack, ClientLevel level, ItemOwner owner, int seed, CallbackInfoReturnable<Float> bfp_ret) {
        boolean isFirstPersonRendererActive = FirstPersonPlayerRenderer.IS_RENDERING_BFP_FIRST_PERSON;
        if (isFirstPersonRendererActive) {
            bfp_ret.setReturnValue(0f);
        }
    }
}
