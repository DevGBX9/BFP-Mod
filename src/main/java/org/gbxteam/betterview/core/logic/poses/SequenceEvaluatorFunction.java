package org.gbxteam.betterview.core.logic.poses;

import org.gbxteam.betterview.core.context.PoseCalculationContext;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.timelines.MovementFlow;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SequenceEvaluatorFunction implements PoseFunction<LocalSpacePose> {

    private final Function<PoseCalculationContext, Identifier> animationSequenceFunction;
    private final Function<PoseCalculationContext, TimeSpan> sequenceTimeFunction;

    private SequenceEvaluatorFunction(Function<PoseCalculationContext, Identifier> animationSequenceFunction, Function<PoseCalculationContext, TimeSpan> sequenceTimeFunction) {
        this.animationSequenceFunction = animationSequenceFunction;
        this.sequenceTimeFunction = sequenceTimeFunction;
    }

    public static Builder builder(Function<PoseCalculationContext, Identifier> animationSequenceFunction) {
        return new Builder(animationSequenceFunction);
    }

    public static Builder builder(Identifier animationSequence) {
        return builder(context -> animationSequence);
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        TimeSpan time = this.sequenceTimeFunction.apply(context);
        Identifier sequence = this.animationSequenceFunction.apply(context);
        return MovementFlow.samplePose(
                context.jointSkeleton(),
                sequence,
                time,
                true
        );
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {

    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return new SequenceEvaluatorFunction(this.animationSequenceFunction, this.sequenceTimeFunction);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : Optional.empty();
    }

    public static class Builder {
        private final Function<PoseCalculationContext, Identifier> animationSequenceFunction;
        private Function<PoseCalculationContext, TimeSpan> sequenceTimeFunction;

        public Builder(Function<PoseCalculationContext, Identifier> animationSequenceFunction) {
            this.animationSequenceFunction = animationSequenceFunction;
            this.sequenceTimeFunction = context -> TimeSpan.ZERO;
        }

        public Builder evaluatesPoseAt(Function<PoseCalculationContext, TimeSpan> sequenceTimeFunction) {
            this.sequenceTimeFunction = sequenceTimeFunction;
            return this;
        }

        public Builder evaluatesPoseAt(TimeSpan sequenceTime) {
            this.sequenceTimeFunction = context -> sequenceTime;
            return this;
        }

        public SequenceEvaluatorFunction build() {
            return new SequenceEvaluatorFunction(this.animationSequenceFunction, this.sequenceTimeFunction);
        }
    }
}
