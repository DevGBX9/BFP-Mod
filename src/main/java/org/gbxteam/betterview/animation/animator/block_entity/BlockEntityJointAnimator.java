package org.gbxteam.betterview.core.engine.controller.block_entity;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimator;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityJointAnimator<B extends BlockEntity> extends JointAnimator<B> {

    @Override
    default PoseCalculationFrequency getPoseCalulationFrequency() {
        return BFPMain.CONFIG.data().blockEntities.poseCalculationFrequency;
    }
}
