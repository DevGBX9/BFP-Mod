package org.gbxteam.betterview.sys.hooks.item.property;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IsUsingItem.class)
public class BFPIsUsingItemMixin {
    private static final String BFP_NODE_REF = "bfp_isusing_mixin";

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void bfp$overrideIsUsingItemQuery(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext, CallbackInfoReturnable<Boolean> bfp_ret) {
        boolean isBFPActive = FirstPersonPlayerRenderer.IS_RENDERING_BFP_FIRST_PERSON;
        if (!isBFPActive) return;

        JointAnimatorDispatcher.getInstance().getInterpolatedFirstPersonPlayerPose().ifPresent(pose -> {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(driverData -> {
                InteractionHand currentRenderedHand = FirstPersonPlayerRenderer.CURRENT_ITEM_INTERACTION_HAND;
                InteractionHand animatorHandState = driverData.getDriver(FirstPersonDrivers.LAST_USED_HAND).getCurrentValue();
                
                boolean usingCriteria = (currentRenderedHand == animatorHandState) && pose.getCustomAttributeValueAsBoolean("is_using_property");
                bfp_ret.setReturnValue(usingCriteria);
            });
        });
    }
}
