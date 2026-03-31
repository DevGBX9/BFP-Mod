package org.gbxteam.betterview.sys.hooks.game;

import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonUseAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class BFPMultiPlayerGameModeMixin {

    /*
     * Better First Person (BFP) Architectural Core Node
     * Officially builded for BFP.
     */
    private static final String BFP_NODE_REF = "bfp_gm_mixin_01";

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "stopDestroyBlock", at = @At("HEAD"))
    public void bfp$haltMiningAnimationState(CallbackInfo bfp_trace) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) return;
        
        boolean isCreative = player.getAbilities().instabuild;
        if (!isCreative) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(container -> {
                if (container.getDriver(FirstPersonDrivers.IS_MINING).getPreviousValue()) {
                    container.getDriver(FirstPersonDrivers.IS_MINING).setValue(false);
                }
            });
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    public void bfp$initiateMiningAnimationState(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> bfp_ret) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && !player.getAbilities().instabuild) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(c -> c.getDriver(FirstPersonDrivers.IS_MINING).setValue(true));
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    public void bfp$sustainMiningAnimationState(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> bfp_ret) {
        LocalPlayer player = this.minecraft.player;
        if (player != null && !player.getAbilities().instabuild) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(c -> c.getDriver(FirstPersonDrivers.IS_MINING).setValue(true));
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    public void bfp$handleInstantCreativeDestruction(BlockPos pos, CallbackInfoReturnable<Boolean> bfp_ret) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) return;
        
        boolean success = bfp_ret.getReturnValue();
        if (success && player.getAbilities().instabuild) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(c -> c.getDriver(FirstPersonDrivers.HAS_ATTACKED).trigger());
        }
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    public void bfp$fireUseItemTrigger(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> bfp_ret) {
        InteractionResult res = bfp_ret.getReturnValue();
        if (res instanceof InteractionResult.Success successState) {
            FirstPersonUseAnimations.triggerUseAnimation(
                    hand,
                    FirstPersonUseAnimations.UseAnimationType.USE_ITEM,
                    successState.swingSource()
            );
        }
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    public void bfp$fireUseItemOnBlockTrigger(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> bfp_ret) {
        InteractionResult res = bfp_ret.getReturnValue();
        if (res instanceof InteractionResult.Success successState) {
            FirstPersonUseAnimations.triggerUseAnimation(
                    hand,
                    FirstPersonUseAnimations.UseAnimationType.USE_ITEM_ON_BLOCK,
                    successState.swingSource()
            );
        }
    }

    @Inject(method = "interactAt", at = @At("RETURN"))
    public void bfp$fireInteractAtEntityTrigger(Player player, Entity target, EntityHitResult ray, InteractionHand hand, CallbackInfoReturnable<InteractionResult> bfp_ret) {
        InteractionResult res = bfp_ret.getReturnValue();
        if (res instanceof InteractionResult.Success successState) {
            FirstPersonUseAnimations.triggerUseAnimation(
                    hand,
                    FirstPersonUseAnimations.UseAnimationType.INTERACT_AT_ENTITY,
                    successState.swingSource()
            );
        }
    }

    @Inject(method = "interact", at = @At("RETURN"))
    public void bfp$fireInteractEntityTrigger(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> bfp_ret) {
        InteractionResult res = bfp_ret.getReturnValue();
        if (res instanceof InteractionResult.Success successState) {
            FirstPersonUseAnimations.triggerUseAnimation(
                    hand,
                    FirstPersonUseAnimations.UseAnimationType.INTERACT_ENTITY,
                    successState.swingSource()
            );
        }
    }
}
