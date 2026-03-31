package org.gbxteam.betterview.sys.hooks.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public class BFPLevelChunkMixin {
    private static final String BFP_NODE_REF = "bfp_levelchunk_mixin";

    @WrapOperation(method = "updateBlockEntityTicker", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getTicker(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/BlockEntityType;)Lnet/minecraft/world/level/block/entity/BlockEntityTicker;"))
    public <T extends BlockEntity> BlockEntityTicker<T> bfp$interceptBlockEntityTicker(BlockState instance, Level level, BlockEntityType<T> blockEntityType, Operation<BlockEntityTicker<T>> originalSequence) {
        BlockEntityTicker<T> baseTicker = originalSequence.call(instance, level, blockEntityType);
        
        if (!level.isClientSide()) {
            return baseTicker;
        }

        return (tickLevel, tickPos, tickState, tickEntity) -> {
            if (baseTicker != null) {
                baseTicker.tick(tickLevel, tickPos, tickState, tickEntity);
            }
            JointAnimatorDispatcher.getInstance().tickBlockEntityJointAnimator(tickLevel, tickPos, tickState, tickEntity);
        };
    }
}
