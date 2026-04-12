package org.gbxteam.betterview.sys.hooks.item.property;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.CrossbowPull;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowPull.class)
public class BFPCrossbowPullMixin {
    private static final String BFP_NODE_REF = "bfp_crossbow_mixin";

    /**
     * Modifies the "Crossbow Pull" item model property to sync up with BFP's first person animations rather than how it's calculated in vanilla.
     */
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void bfp$overrideCrossbowPullCalculation(ItemStack stack, ClientLevel level, ItemOwner owner, int seed, CallbackInfoReturnable<Float> bfp_ret) {
        boolean isBFPActive = FirstPersonPlayerRenderer.IS_RENDERING_BFP_FIRST_PERSON;
        if (!isBFPActive) return;

        JointAnimatorDispatcher.getInstance().getInterpolatedFirstPersonPlayerPose().ifPresent(pose -> {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(driverData -> {
                InteractionHand currentHand = FirstPersonPlayerRenderer.CURRENT_ITEM_INTERACTION_HAND;
                InteractionHand driverHand = driverData.getDriver(FirstPersonDrivers.LAST_USED_HAND).getCurrentValue();
                
                float pullValue = (currentHand == driverHand) ? pose.getCustomAttributeValue("crossbow_pull_property") : 0f;
                bfp_ret.setReturnValue(pullValue);
            });
        });
    }
}
