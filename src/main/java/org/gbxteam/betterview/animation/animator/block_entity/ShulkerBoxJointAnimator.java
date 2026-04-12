package org.gbxteam.betterview.core.engine.controller.block_entity;

import org.gbxteam.betterview.BFPMain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.jetbrains.annotations.NotNull;

public class ShulkerBoxJointAnimator implements TwoStateContainerJointAnimator<@org.jetbrains.annotations.NotNull ShulkerBoxBlockEntity> {

    public static final Identifier SHULKER_BOX_SKELETON = BFPMain.makeIdentifier("skeletons/block_entity/shulker_box.json");

    public static final Identifier SHULKER_BOX_OPEN_SEQUENCE = BFPMain.makeIdentifier("sequences/block_entity/shulker_box/open.json");
    public static final Identifier SHULKER_BOX_CLOSE_SEQUENCE = BFPMain.makeIdentifier("sequences/block_entity/shulker_box/close.json");

    @Override
    public Identifier getRigSystem() {
        return SHULKER_BOX_SKELETON;
    }

    @Override
    public float getOpenProgress(@NotNull ShulkerBoxBlockEntity blockEntity) {
        return blockEntity.getProgress(0);
    }

    @Override
    public Identifier getOpenMovementFlow() {
        return SHULKER_BOX_OPEN_SEQUENCE;
    }

    @Override
    public Identifier getCloseMovementFlow() {
        return SHULKER_BOX_CLOSE_SEQUENCE;
    }
}
