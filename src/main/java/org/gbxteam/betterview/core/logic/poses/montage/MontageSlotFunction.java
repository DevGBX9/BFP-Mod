package org.gbxteam.betterview.core.logic.poses.montage;

import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.context.PoseCalculationContext;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public record MontageSlotFunction(PoseFunction<LocalSpacePose> input, String slot) implements PoseFunction<LocalSpacePose> {

    public static MontageSlotFunction of(PoseFunction<LocalSpacePose> inputPose, String slot) {
        return new MontageSlotFunction(inputPose, slot);
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        return context.montageManager().getLayeredSlotPose(this.input.compute(context), this.slot, context.jointSkeleton(), context.partialTicks());
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        if (!context.montageManager().areAnyMontagesInSlotFullyOverriding(this.slot)) {
            this.input.tick(context);
        }
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return MontageSlotFunction.of(this.input.wrapUnique(), this.slot);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : this.input.searchDownChainForMostRelevant(findCondition);
    }
}
