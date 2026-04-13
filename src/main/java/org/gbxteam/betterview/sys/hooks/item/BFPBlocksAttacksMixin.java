package org.gbxteam.betterview.sys.hooks.item;

//? if mc >= 12105 {

import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlocksAttacks.class)
public class BFPBlocksAttacksMixin {
    private static final String BFP_NODE_REF = "bfp_blocks_attack_mixin";

    @Inject(method = "onBlocked", at = @At("HEAD"))
    public void bfp$trackShieldImpactVisuals(ServerLevel serverLevel, LivingEntity livingEntity, CallbackInfo bfp_trace) {
        boolean isLocal = Minecraft.getInstance().isLocalPlayer(livingEntity.getUUID());
        if (isLocal) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer()
                .ifPresent(container -> container.getDriver(FirstPersonDrivers.HAS_BLOCKED_ATTACK).trigger());
        }
    }
}

//?} else {

/*import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class BFPBlocksAttacksMixin {

}

*///?}