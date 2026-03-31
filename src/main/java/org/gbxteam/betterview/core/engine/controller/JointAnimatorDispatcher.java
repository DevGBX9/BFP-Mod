package org.gbxteam.betterview.core.engine.controller;

import com.google.common.collect.Maps;
import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.sys.bridge.MatrixModelPart;
import org.gbxteam.betterview.core.engine.controller.block_entity.BlockEntityJointAnimator;
import org.gbxteam.betterview.core.context.AnimationDataContainer;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.engine.motors.VariableDriver;
import org.gbxteam.betterview.core.logic.states.ModelPartSpacePose;
import org.gbxteam.betterview.core.logic.states.Pose;
import org.gbxteam.betterview.core.skeleton.rig.RigSystem;
import org.gbxteam.betterview.core.logic.states.ComponentSpacePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Function;

public class JointAnimatorDispatcher {
    /** BFP Animation Dispatcher - GBXTeam */
    private static final JointAnimatorDispatcher INSTANCE = new JointAnimatorDispatcher();

    private final WeakHashMap<UUID, AnimationDataContainer> bfp$entityStorage;
    private final HashMap<Long, AnimationDataContainer> bfp$blockEntityStorage;
    private AnimationDataContainer bfp$fpPlayerData;
    private ModelPartSpacePose bfp$interpolatedFPPose;

    private static final DriverKey<VariableDriver<Identifier>> BFP_BLOCK_TYPE_DK = DriverKey.of("block_entity_type", () -> VariableDriver.ofConstant(() -> Identifier.withDefaultNamespace("none")));
    private static final DriverKey<VariableDriver<Identifier>> BFP_ENTITY_TYPE_DK = DriverKey.of("entity_type", () -> VariableDriver.ofConstant(() -> Identifier.withDefaultNamespace("none")));

    public JointAnimatorDispatcher() {
        this.bfp$entityStorage = new WeakHashMap<>();
        this.bfp$blockEntityStorage = Maps.newHashMap();
    }

    public static JointAnimatorDispatcher getInstance() {
        return INSTANCE;
    }

    public void reInitializeData() {
        this.bfp$fpPlayerData = null;
        this.bfp$entityStorage.clear();
        this.bfp$blockEntityStorage.clear();
    }

    public <T extends Entity, B extends BlockEntity> void tick(Iterable<T> entitiesForRendering) {
        this.bfp$tickFirstPersonAnimator();
        this.bfp$flushBlockEntitiesOutOfRange();
    }

    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> void tickBlockEntityJointAnimator(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        Optional<BlockEntityJointAnimator<T>> animatorOpt = JointAnimatorRegistry.getBlockEntityJointAnimator((BlockEntityType<T>) blockEntity.getType());
        if (animatorOpt.isEmpty()) return;

        BlockEntityJointAnimator<T> animator = animatorOpt.get();
        Optional<AnimationDataContainer> dataOpt = this.getBlockEntityAnimationDataContainer(blockPos, blockEntity.getType());
        if (dataOpt.isEmpty()) return;

        bfp$tickAnimator(animator, blockEntity, dataOpt.get());
    }

    public void bfp$tickFirstPersonAnimator() {
        if (BFPMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                    animator -> this.getFirstPersonPlayerDataContainer().ifPresent(
                            data -> bfp$tickAnimator(animator, Minecraft.getInstance().player, data)
                    )
            );
        }
    }

    private <T> void bfp$tickAnimator(JointAnimator<T> animator, T ref, AnimationDataContainer data) {
        data.preTick();
        animator.extractAnimationData(ref, data, data.getMontageManager());
        data.tick();
        if (animator.getPoseCalulationFrequency() == JointAnimator.PoseCalculationFrequency.CALCULATE_ONCE_PER_TICK) {
            data.getDriver(data.getPerTickCalculatedPoseDriverKey()).setValue(data.computePose(1));
        }
        data.postTick();
    }

    private static <T extends BlockEntity> Optional<AnimationDataContainer> bfp$tryBuildBlockEntityData(BlockEntityType<T> type) {
        Optional<BlockEntityJointAnimator<T>> animOpt = JointAnimatorRegistry.getBlockEntityJointAnimator(type);
        if (animOpt.isEmpty()) return Optional.empty();

        AnimationDataContainer data = AnimationDataContainer.of(animOpt.get());
        data.getDriver(BFP_BLOCK_TYPE_DK).setValue(BlockEntityType.getKey(type));
        return Optional.of(data);
    }

    public Map<BlockPos, Identifier> getCurrentlyEvaluatingBlockEntityJointAnimators() {
        Map<BlockPos, Identifier> result = Maps.newHashMap();
        this.bfp$blockEntityStorage.forEach((packed, data) -> {
            result.put(BlockPos.of(packed), data.getDriverValue(BFP_BLOCK_TYPE_DK));
        });
        return result;
    }

    private static boolean bfp$isWithinCameraRange(BlockPos pos) {
        BlockPos camPos = Objects.requireNonNull(Minecraft.getInstance().getCameraEntity()).blockPosition();
        int range = BFPMain.CONFIG.data().blockEntities.evaluationDistance;
        return camPos.distChessboard(pos) < range;
    }

    private static boolean bfp$isBlockEntityEnabled(BlockEntityType<?> type) {
        Identifier typeId = BlockEntityType.getKey(type);
        assert typeId != null;
        return BFPMain.CONFIG.data().blockEntities.enabledBlockEntities.getOrDefault(typeId.toString(), true);
    }

    public void bfp$flushBlockEntitiesOutOfRange() {
        List<Long> toRemove = new ArrayList<>();
        for (long packed : this.bfp$blockEntityStorage.keySet()) {
            if (!bfp$isWithinCameraRange(BlockPos.of(packed))) {
                toRemove.add(packed);
            }
        }
        toRemove.forEach(this.bfp$blockEntityStorage::remove);
    }

    public <T extends BlockEntity> Optional<AnimationDataContainer> getBlockEntityAnimationDataContainer(BlockPos pos, BlockEntityType<T> type) {
        long packed = pos.asLong();

        if (bfp$isWithinCameraRange(pos) && bfp$isBlockEntityEnabled(type)) {
            if (this.bfp$blockEntityStorage.containsKey(packed)) {
                AnimationDataContainer existing = this.bfp$blockEntityStorage.get(packed);
                if (existing.getDriverValue(BFP_BLOCK_TYPE_DK) == BlockEntityType.getKey(type)) {
                    return Optional.of(existing);
                }
                this.bfp$blockEntityStorage.remove(packed);
            }

            if (!this.bfp$blockEntityStorage.containsKey(packed)) {
                bfp$tryBuildBlockEntityData(type).ifPresent(data -> this.bfp$blockEntityStorage.put(packed, data));
            }

            return Optional.ofNullable(this.bfp$blockEntityStorage.getOrDefault(packed, null));
        } else {
            this.bfp$blockEntityStorage.remove(packed);
            return Optional.empty();
        }
    }

    public Optional<AnimationDataContainer> getFirstPersonPlayerDataContainer() {
        if (this.bfp$fpPlayerData == null) {
            JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                    animator -> this.bfp$fpPlayerData = AnimationDataContainer.of(animator)
            );
        }
        return Optional.ofNullable(this.bfp$fpPlayerData);
    }

    public Optional<ModelPartSpacePose> getInterpolatedFirstPersonPlayerPose() {
        return Optional.ofNullable(this.bfp$interpolatedFPPose);
    }

    public void calculateInterpolatedFirstPersonPlayerPose(AnimationDataContainer data, float partialTicks) {
        this.bfp$interpolatedFPPose = data.getInterpolatedAnimationPose(partialTicks);
    }
}
