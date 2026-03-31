package org.gbxteam.betterview.sys.hooks.game;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.BFPCameraOverhaulSystem;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.BFPScreenShakes;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from Camera Overhaul's LocalPlayerMixin.
 * Adds subtle cinematic screen shake when the player swings their hand (attacking, mining, etc).
 */
@Mixin(LocalPlayer.class)
public abstract class BFPHandSwingMixin {

    @Unique private static long bfp$shakeHandle;

    @Shadow public abstract boolean isLocalPlayer();

    @Inject(method = "swing", at = @At("RETURN"))
    private void bfp$onHandSwing(InteractionHand interactionHand, CallbackInfo ci) {
        if (!isLocalPlayer()) return;

        bfp$shakeHandle = BFPScreenShakes.recreate(bfp$shakeHandle);
        var shake = BFPScreenShakes.get(bfp$shakeHandle);
        shake.trauma = 0.06f;      // Balanced: not too subtle, not too aggressive
        shake.frequency = 0.7f;
        shake.lengthInSeconds = 0.35f;

        BFPCameraOverhaulSystem.getInstance().notifyOfPlayerAction();
    }
}
