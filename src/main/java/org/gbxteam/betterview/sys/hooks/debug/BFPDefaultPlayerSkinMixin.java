package org.gbxteam.betterview.sys.hooks.debug;

import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(DefaultPlayerSkin.class)
public abstract class BFPDefaultPlayerSkinMixin {
    private static final String BFP_NODE_REF = "bfp_defaultskin_mixin";

    // Forces Steve skin rather than randomizing on dev builds locally.

    @Shadow @Final private static PlayerSkin[] DEFAULT_SKINS;

    @Shadow protected static PlayerSkin create(String name, PlayerModelType modelType) { return null; }

    @Inject(method = "get(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/PlayerSkin;", at = @At("HEAD"), cancellable = true)
    private static void bfp$forceSteveSkinDefault(UUID uuid, CallbackInfoReturnable<PlayerSkin> bfp_ret) {
        bfp_ret.setReturnValue(create("entity/player/wide/steve", PlayerModelType.WIDE));
    }
}
