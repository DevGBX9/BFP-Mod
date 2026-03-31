package org.gbxteam.betterview.core.engine.controller;

import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;
import net.minecraft.resources.Identifier;

/**
 * Uses a data reference and a joint skeleton to calculate a pose once per tick.
 * @param <T> Object used for data reference
 */
public interface JointAnimator<T> {

    /**
     * Gets the resource location for the joint skeleton being used.
     * @return                              Joint skeleton resource location
     */
    Identifier getRigSystem();

    /**
     * Uses an object for data reference and updates the animation data container. Called once per tick, prior to pose samplers updating and pose calculation.
     * @param dataReference                 Object used as reference for updating the animation data container
     * @param dataContainer                 Data container for getting and setting drivers
     * @param montageManager                Controls data used for getting and playing animation montages.
     */
    void extractAnimationData(T dataReference, DriverGetter dataContainer, MontageManager montageManager);

    /**
     * Creates the pose function that will return an animation pose for the joint animator.
     * @param cachedPoseContainer           Container for registering and retrieving saved cached poses.
     * @return                              Pose function that returns a pose in local space.
     */
    PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer);

    default PoseCalculationFrequency getPoseCalulationFrequency(){
        return PoseCalculationFrequency.CALCULATE_ONCE_PER_TICK;
    }

    enum PoseCalculationFrequency {
        CALCULATE_EVERY_FRAME,
        CALCULATE_ONCE_PER_TICK
    }
}
