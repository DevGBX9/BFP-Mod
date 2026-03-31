package org.gbxteam.betterview.sys.hooks.item.property;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UseDuration.class)
public class BFPUseDurationMixin {
    private static final String BFP_NODE_REF = "bfp_useduration_mixin";

    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    public void bfp$overrideUseDuration(ItemStack stack, ClientLevel level, ItemOwner owner, int seed, CallbackInfoReturnable<Float> bfp_ret) {
        boolean flagFirstPerson = FirstPersonPlayerRenderer.IS_RENDERING_BFP_FIRST_PERSON;
        if (!flagFirstPerson) return;
        
        JointAnimatorDispatcher dispatcher = JointAnimatorDispatcher.getInstance();
        dispatcher.getInterpolatedFirstPersonPlayerPose().ifPresent(animPose -> {
            dispatcher.getFirstPersonPlayerDataContainer().ifPresent(container -> {
                InteractionHand currentHand = FirstPersonPlayerRenderer.CURRENT_ITEM_INTERACTION_HAND;
                InteractionHand driverHand = container.getDriver(FirstPersonDrivers.LAST_USED_HAND).getCurrentValue();
                
                float durationProp = (currentHand == driverHand) ? animPose.getCustomAttributeValue("use_duration_property") : 0f;
                bfp_ret.setReturnValue(durationProp);
            });
        });
    }
}
