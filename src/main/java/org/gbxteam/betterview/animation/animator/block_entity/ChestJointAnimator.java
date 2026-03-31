package org.gbxteam.betterview.core.engine.controller.block_entity;

import org.gbxteam.betterview.BFPMain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;

public class ChestJointAnimator<B extends BlockEntity & LidBlockEntity> implements TwoStateContainerJointAnimator<B> {

    public static final Identifier CHEST_SKELETON = BFPMain.makeIdentifier("skeletons/block_entity/chest.json");

    public static final Identifier CHEST_OPEN_SEQUENCE = BFPMain.makeIdentifier("sequences/block_entity/chest/open.json");
    public static final Identifier CHEST_CLOSE_SEQUENCE = BFPMain.makeIdentifier("sequences/block_entity/chest/close.json");

    @Override
    public Identifier getRigSystem() {
        return CHEST_SKELETON;
    }

    @Override
    public float getOpenProgress(B blockEntity) {
        return blockEntity.getOpenNess(0);
    }

    @Override
    public Identifier getOpenMovementFlow() {
        return CHEST_OPEN_SEQUENCE;
    }

    @Override
    public Identifier getCloseMovementFlow() {
        return CHEST_CLOSE_SEQUENCE;
    }
}
