package org.gbxteam.betterview.core.logic.poses;

import org.gbxteam.betterview.core.context.PoseCalculationContext;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.logic.states.ComponentSpacePose;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.states.Pose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Converts a pose from component space to local space or local space to component space
 * @param input             Input pose function
 * @param converter         Conversion function (handled by static factory)
 * @param <I>               Input  pose type
 * @param <O>               Output pose type
 */
public record PoseConversionFunction<I extends Pose, O extends Pose>(PoseFunction<I> input, Function<I, O> converter) implements PoseFunction<O> {
    @Override
    public @NotNull O compute(PoseCalculationContext context) {
        return this.converter.apply(this.input.compute(context));
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        this.input.tick(context);
    }

    @Override
    public PoseFunction<O> wrapUnique() {
        return new PoseConversionFunction<>(this.input.wrapUnique(), this.converter);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : this.input.searchDownChainForMostRelevant(findCondition);
    }

    /**
     * Creates a pose conversion function that converts the local space pose to a component space pose.
     * @param input             Local space pose function
     */
    public static PoseConversionFunction<LocalSpacePose, ComponentSpacePose> localToComponentOf(PoseFunction<LocalSpacePose> input){
        return new PoseConversionFunction<>(input, LocalSpacePose::convertedToComponentSpace);
    }

    /**
     * Creates a pose conversion function that converts the component space pose to a local space pose.
     * @param input             Component space pose function
     */
    public static PoseConversionFunction<ComponentSpacePose, LocalSpacePose> componentToLocalOf(PoseFunction<ComponentSpacePose> input){
        return new PoseConversionFunction<>(input, ComponentSpacePose::convertedToLocalSpace);
    }
}