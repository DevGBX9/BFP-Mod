package org.gbxteam.betterview.core.logic.poses;

import org.gbxteam.betterview.core.context.PoseCalculationContext;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class EmptyPoseFunction implements PoseFunction<LocalSpacePose> {

    private final boolean useReferencePose;

    public EmptyPoseFunction(boolean useReferencePose) {
        this.useReferencePose = useReferencePose;
    }

    public static EmptyPoseFunction of() {
        return EmptyPoseFunction.of(true);
    }

    public static EmptyPoseFunction of(boolean useReferencePose) {
        return new EmptyPoseFunction(useReferencePose);
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        LocalSpacePose pose = LocalSpacePose.of(context.jointSkeleton());
        if (!this.useReferencePose) {
            pose.setIdentity();
        }
        return pose;
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {

    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return EmptyPoseFunction.of(this.useReferencePose);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : Optional.empty();
    }
}
