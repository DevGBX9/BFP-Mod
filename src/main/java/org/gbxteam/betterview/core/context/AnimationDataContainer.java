package org.gbxteam.betterview.core.context;

import com.google.common.collect.Maps;
import org.gbxteam.betterview.sys.bridge.MatrixModelPart;
import org.gbxteam.betterview.core.engine.controller.JointAnimator;
import org.gbxteam.betterview.core.engine.motors.Driver;
import org.gbxteam.betterview.core.engine.motors.VariableDriver;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.skeleton.rig.RigSystem;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.states.ModelPartSpacePose;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;
import org.gbxteam.betterview.sys.assets.BFPResources;
import org.gbxteam.betterview.core.helpers.Interpolator;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

import java.util.Map;
import java.util.function.Function;

public class AnimationDataContainer implements DriverGetter {

    /** BFP Animation Data Pipeline */
    private final Map<DriverKey<? extends Driver<?>>, Driver<?>> bfp$drivers;
    private final CachedPoseContainer bfp$cachedPoses;
    private final PoseFunction<LocalSpacePose> bfp$poseFunc;
    private final MontageManager bfp$montages;

    private final RigSystem bfp$skeleton;
    private final DriverKey<VariableDriver<LocalSpacePose>> bfp$perTickPoseDK;
    private final DriverKey<VariableDriver<Long>> bfp$gameTimeDK;
    private final JointAnimator.PoseCalculationFrequency bfp$calcFreq;

    private AnimationDataContainer(JointAnimator<?> animator) {
        this.bfp$drivers = Maps.newHashMap();
        this.bfp$cachedPoses = CachedPoseContainer.of();
        this.bfp$poseFunc = animator.constructPoseFunction(bfp$cachedPoses).wrapUnique();
        this.bfp$montages = MontageManager.of(this);

        this.bfp$skeleton = BFPResources.getOrThrowRigSystem(animator.getRigSystem());
        this.bfp$perTickPoseDK = DriverKey.of("per_tick_calculated_pose", () -> VariableDriver.ofInterpolatable(() -> LocalSpacePose.of(bfp$skeleton), Interpolator.LOCAL_SPACE_POSE));
        this.bfp$gameTimeDK = DriverKey.of("game_time", () -> VariableDriver.ofConstant(() -> 0L));
        this.bfp$calcFreq = animator.getPoseCalulationFrequency();

        this.tick();
    }

    public static AnimationDataContainer of(JointAnimator<?> animator) {
        return new AnimationDataContainer(animator);
    }

    public void preTick() {
        this.bfp$drivers.values().forEach(Driver::pushCurrentToPrevious);
    }

    public void tick() {
        this.bfp$montages.tick();
        this.bfp$drivers.values().forEach(Driver::tick);
        this.getDriver(this.bfp$gameTimeDK).setValue(this.getDriver(this.bfp$gameTimeDK).getCurrentValue() + 1);
        this.bfp$poseFunc.tick(new PoseTickEvaluationContext(this, this.bfp$montages, false, this.getDriverValue(this.bfp$gameTimeDK)));
    }

    public void postTick() {
        this.bfp$drivers.values().forEach(Driver::postTick);
    }

    public LocalSpacePose computePose(float partialTicks) {
        this.bfp$cachedPoses.clearCaches();
        return this.bfp$poseFunc.compute(new PoseCalculationContext(
                this, this.bfp$skeleton, this.bfp$montages, partialTicks,
                TimeSpan.ofTicks(this.getInterpolatedDriverValue(bfp$gameTimeDK, 1) + partialTicks)
        ));
    }

    public DriverKey<VariableDriver<LocalSpacePose>> getPerTickCalculatedPoseDriverKey() {
        return this.bfp$perTickPoseDK;
    }

    public MontageManager getMontageManager() {
        return this.bfp$montages;
    }

    public Map<DriverKey<? extends Driver<?>>, Driver<?>> getAllDrivers() {
        return this.bfp$drivers;
    }

    public <D, R extends Driver<D>> D getInterpolatedDriverValue(DriverKey<R> dk, float partialTicks) {
        return this.getDriver(dk).getInterpolatedValue(partialTicks);
    }

    @Override
    public <D, R extends Driver<D>> D getDriverValue(DriverKey<R> dk) {
        return this.getInterpolatedDriverValue(dk, 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, R extends Driver<D>> R getDriver(DriverKey<R> dk) {
        return (R) this.bfp$drivers.computeIfAbsent(dk, DriverKey::createInstance);
    }

    private RigSystem bfp$getSkeleton() {
        return this.bfp$skeleton;
    }

    public ModelPartSpacePose getInterpolatedAnimationPose(float partialTicks) {
        LocalSpacePose localPose = switch (this.bfp$calcFreq) {
            case CALCULATE_EVERY_FRAME -> this.computePose(partialTicks);
            case CALCULATE_ONCE_PER_TICK -> this.getInterpolatedDriverValue(this.getPerTickCalculatedPoseDriverKey(), partialTicks);
        };
        return localPose.convertedToComponentSpace().convertedToModelPartSpace();
    }

    public <S> void setupAnimWithAnimationPose(Model<S> mdl, float partialTicks) {
        mdl.resetPose();
        ModelPartSpacePose animPose = this.getInterpolatedAnimationPose(partialTicks);
        RigSystem skel = this.bfp$getSkeleton();

        Function<String, ModelPart> partLookup = mdl.root().createPartLookup();
        skel.getJoints().forEach(joint -> {
            String partId = skel.getJointConfiguration(joint).modelPartIdentifier();
            if (partId != null) {
                ModelPart part = partLookup.apply(partId);
                if (part != null) {
                    ((MatrixModelPart)(Object) part).bfp$setMatrix(animPose.getJointChannel(joint).getTransform());
                }
            }
        });
    }
}
