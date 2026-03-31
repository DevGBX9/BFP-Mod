package org.gbxteam.betterview;

import org.gbxteam.betterview.core.engine.controller.JointAnimatorRegistry;
import org.gbxteam.betterview.core.engine.controller.block_entity.ChestJointAnimator;
import org.gbxteam.betterview.core.engine.controller.block_entity.ShulkerBoxJointAnimator;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonJointAnimator;
import org.gbxteam.betterview.sys.settings.BFPConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BFPMain {

	/** BFP Core Infrastructure - GBXTeam */
	public static final Logger DEBUG_LOGGER = LogManager.getLogger("BFP/DEBUG");
	public static final String MOD_ID = "bfp";
	public static final BFPConfig CONFIG = new BFPConfig();

	public static void initialize() {
		CONFIG.load();
		bfp$registerCoreAnimators();
	}

	public static Identifier makeIdentifier(String location) {
		return Identifier.fromNamespaceAndPath(MOD_ID, location);
	}

	private static void bfp$registerCoreAnimators() {
		JointAnimatorRegistry.registerFirstPersonPlayerJointAnimator(new FirstPersonJointAnimator());

		JointAnimatorRegistry.registerBlockEntityJointAnimator(BlockEntityType.CHEST, new ChestJointAnimator<>());
		JointAnimatorRegistry.registerBlockEntityJointAnimator(BlockEntityType.ENDER_CHEST, new ChestJointAnimator<>());
		JointAnimatorRegistry.registerBlockEntityJointAnimator(BlockEntityType.TRAPPED_CHEST, new ChestJointAnimator<>());
		JointAnimatorRegistry.registerBlockEntityJointAnimator(BlockEntityType.SHULKER_BOX, new ShulkerBoxJointAnimator());
	}
}