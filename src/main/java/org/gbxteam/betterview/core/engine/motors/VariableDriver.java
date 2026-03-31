package org.gbxteam.betterview.core.engine.motors;

import org.gbxteam.betterview.core.helpers.Interpolator;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ==========================================================================================
 * BFP ANIMATION ENGINE - VARIABLE DRIVER
 * Copyright (c) 2024 gbxteam. All Rights Reserved.
 * ==========================================================================================
 * 
 * A standard fluid data container for the animation engine. 
 * Allows continuous tick updates with native cross-tick interpolation capabilities.
 * 
 * @param <D> The underlying data structure managed by this driver.
 */
public class VariableDriver<D> implements Driver<D> {

    // Core Properties
    protected final Supplier<D> baseValueProvider;
    protected final Interpolator<D> blendFunction;

    // Runtime State
    protected D liveTickValue;
    protected D lastTickValue;

    protected VariableDriver(Supplier<D> baseValueProvider, Interpolator<D> blendFunction) {
        this.baseValueProvider = baseValueProvider;
        this.blendFunction = blendFunction;
        this.liveTickValue = baseValueProvider.get();
        this.lastTickValue = baseValueProvider.get();
    }

    /**
     * Initializes a standard variable driver with sub-tick blending capabilities.
     * 
     * @param defaultProvider Supplier producing the fallback state.
     * @param blendFunction The mathematical formula for interpolating between ticks.
     */
    public static <D> VariableDriver<D> ofInterpolatable(Supplier<D> defaultProvider, Interpolator<D> blendFunction) {
        return new VariableDriver<>(defaultProvider, blendFunction);
    }

    /**
     * Initializes a discrete driver that strictly snaps to the nearest tick (no blending).
     */
    public static <D> VariableDriver<D> ofConstant(Supplier<D> defaultProvider) {
        return ofInterpolatable(defaultProvider, Interpolator.constantBlend());
    }

    /**
     * Convenience factory for discrete boolean variables.
     */
    public static VariableDriver<Boolean> ofBoolean(Supplier<Boolean> defaultProvider) {
        return ofInterpolatable(defaultProvider, Interpolator.BOOLEAN_BLEND);
    }

    /**
     * Convenience factory for smoothly interpolated floating point variables.
     */
    public static VariableDriver<Float> ofFloat(@Nullable Supplier<Float> defaultProvider) {
        return ofInterpolatable(defaultProvider, Interpolator.FLOAT);
    }

    /**
     * Convenience factory for discrete integer variables.
     */
    public static VariableDriver<Integer> ofInteger(Supplier<Integer> defaultProvider) {
        return ofConstant(defaultProvider);
    }

    /**
     * Convenience factory for smoothly interpolated 3D transformation vectors.
     */
    public static VariableDriver<Vector3f> ofVector(Supplier<Vector3f> defaultProvider) {
        return ofInterpolatable(defaultProvider, Interpolator.VECTOR_FLOAT);
    }

    /**
     * Overwrites the active tick state. Fallbacks to default if the input is null.
     * 
     * @implNote Always pass copied/immutable object references here to prevent external mutations!
     */
    public void setValue(D updatedValue) {
        this.liveTickValue = (updatedValue != null) ? updatedValue : this.baseValueProvider.get();
    }

    /**
     * Safely mutates the live tick state through a transformation mapping.
     */
    public void modifyValue(Function<D, D> mutator) {
        this.liveTickValue = mutator.apply(this.liveTickValue);
    }

    /**
     * Reverts the live state back to the original fallback configuration.
     */
    public void reset() {
        this.setValue(this.baseValueProvider.get());
    }

    /**
     * Flushes both the live and historical states completely, snapping the entire var to fallback.
     */
    public void hardReset() {
        this.reset();
        this.pushCurrentToPrevious();
    }

    /**
     * Checks if a mutation occurred over the duration of the current tick cycle.
     */
    public boolean hasValueChanged() {
        return !this.liveTickValue.equals(this.lastTickValue);
    }

    public D getPreviousValue() {
        return this.lastTickValue;
    }

    @Override
    public D getCurrentValue() {
        return this.liveTickValue;
    }

    @Override
    public D getInterpolatedValue(float deltaTick) {
        return blendFunction.interpolate(this.lastTickValue, this.liveTickValue, deltaTick);
    }

    @Override
    public void pushCurrentToPrevious() {
        this.lastTickValue = this.liveTickValue;
    }

    @Override
    public void tick() {
        // Variable drivers do not calculate their own ticks inherently.
    }

    @Override
    public void postTick() {
        // Blank implementation for basic variables.
    }

    @Override
    public String toString() {
        if (this.liveTickValue instanceof Float val) {
            return String.format("%.2f", val);
        } else if (this.liveTickValue instanceof Vector3f vec) {
            return String.format("(%.2f %.2f %.2f)", vec.x, vec.y, vec.z);
        }
        return this.liveTickValue.toString();
    }

    @Override
    public String getChatFormattedString() {
        return this.toString();
    }
}