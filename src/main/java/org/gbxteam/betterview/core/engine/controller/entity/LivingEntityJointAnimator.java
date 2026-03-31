package org.gbxteam.betterview.core.engine.controller.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityJointAnimator<T extends LivingEntity, S extends LivingEntityRenderState> extends EntityJointAnimator<T, S> {
}
