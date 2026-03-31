package org.gbxteam.betterview.sys.hooks.game;

import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonUseAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class BFPMinecraftMixin {
    private static final String BFP_NODE_REF = "bfp_minecraft_core_mixin";

    @Shadow @Nullable public ClientLevel level;

    @Shadow private volatile boolean pause;

    @Shadow protected abstract boolean isLevelRunningNormally();

    @Shadow public abstract CompletableFuture<Void> delayTextureReload();

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Shadow public abstract BlockEntityRenderDispatcher getBlockEntityRenderDispatcher();

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    public void bfp$trackItemDropEvent(CallbackInfo bfp_trace) {
        JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer()
            .ifPresent(container -> container.getDriver(FirstPersonDrivers.HAS_DROPPED_ITEM).trigger());
    }

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getConnection()Lnet/minecraft/client/multiplayer/ClientPacketListener;", ordinal = 0))
    public void bfp$trackItemSwapEvent(CallbackInfo bfp_trace) {
        JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer()
            .ifPresent(container -> container.getDriver(FirstPersonDrivers.HAS_SWAPPED_ITEMS).trigger());
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    public void bfp$trackMissedAttackEvent(CallbackInfoReturnable<Boolean> bfp_ret) {
        JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(container -> {
            if (!container.getDriverValue(FirstPersonDrivers.IS_MINING)) {
                container.getDriver(FirstPersonDrivers.HAS_ATTACKED).trigger();
            }
        });
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    public void bfp$trackStartUseEvent(CallbackInfo bfp_trace) {
        JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(FirstPersonUseAnimations::updateUseAnimationHitResults);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;animateTick(III)V"))
    private void bfp$tickCoreAnimators(CallbackInfo bfp_trace) {
        if (this.level != null) {
            JointAnimatorDispatcher.getInstance().tick(this.level.entitiesForRendering());
        }
    }
}
