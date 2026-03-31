package org.gbxteam.betterview.core.logic.poses.statemachine;

import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.engine.motors.Driver;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.AnimationPlayer;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.helpers.TimeSpan;

import java.util.Optional;

public record StateTransitionContext(
        DriverGetter driverGetter,
        TimeSpan timeElapsedInCurrentState,
        float currentStateWeight,
        float previousStateWeight,
        PoseFunction<LocalSpacePose> currentStateInput,
        TimeSpan transitionDuration
) implements DriverGetter {

    public Optional<AnimationPlayer> findMostRelevantAnimationPlayer() {
        Optional<PoseFunction<?>> foundPoseFunction = this.currentStateInput.searchDownChainForMostRelevant(poseFunction -> poseFunction instanceof AnimationPlayer);
        return foundPoseFunction.map(poseFunction -> (AnimationPlayer) poseFunction);
    }

    @Override
    @SuppressWarnings("deprecated")
    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey) {
        return this.driverGetter.getDriver(driverKey);
    }
}
