package org.gbxteam.betterview.core.engine.motors;

import net.minecraft.ChatFormatting;

/**
 * ==========================================================================================
 * BFP ANIMATION ENGINE - TRIGGER DRIVER
 * Copyright (c) 2024 gbxteam. All Rights Reserved.
 * ==========================================================================================
 * 
 * Manages one-shot pulse signals within the animation pipeline.
 * Emits a boolean trigger that is automatically consumed and reset after the pose evaluation cycle.
 */
public class TriggerDriver implements Driver<Boolean> {

    // Configuration
    private final int pulseLength;

    // Runtime State
    private int activeTicksRemaining;
    private boolean isConsumed;

    private TriggerDriver(int pulseLength) {
        this.pulseLength = pulseLength;
        this.activeTicksRemaining = 0;
        this.isConsumed = false;
    }

    /**
     * Initializes a standard single-tick pulse driver.
     */
    public static TriggerDriver of() {
        return new TriggerDriver(1);
    }

    /**
     * Initializes a pulse driver that stays active for a specified duration of ticks.
     */
    public static TriggerDriver of(int customPulseLength) {
        return new TriggerDriver(Math.max(1, customPulseLength));
    }

    /**
     * Fires the pulse signal, resetting any consumption state.
     */
    public void trigger() {
        this.activeTicksRemaining = this.pulseLength;
        this.isConsumed = false;
    }

    /**
     * Unconditionally consumes the current pulse signal to prevent duplicate evaluation.
     */
    public void consume() {
        this.isConsumed = true;
    }

    /**
     * Checks if a pulse is currently active (regardless of consumption state).
     */
    public boolean hasBeenTriggered() {
        return this.activeTicksRemaining > 0;
    }

    /**
     * Checks whether the current pulse signal has already been captured by the evaluator.
     */
    public boolean hasBeenConsumed() {
        return this.isConsumed;
    }

    /**
     * Evaluates if a fresh pulse signal is available for processing.
     */
    public boolean hasBeenTriggeredAndNotConsumed() {
        return hasBeenTriggered() && !hasBeenConsumed();
    }

    /**
     * Conditionally executes a runnable logic block if the driver has an unconsumed pulse.
     * Consumes the pulse immediately afterwards.
     * 
     * @param executor Logic to execute if active.
     */
    public void runAndConsumeIfTriggered(Runnable executor) {
        if (this.hasBeenTriggeredAndNotConsumed()) {
            executor.run();
            this.consume();
        }
    }

    @Override
    public Boolean getCurrentValue() {
        return this.hasBeenTriggered();
    }

    @Override
    public Boolean getInterpolatedValue(float deltaTick) {
        return this.getCurrentValue(); // Triggers do not support sub-tick float interpolation
    }

    @Override
    public void tick() {
        // Handled via explicit triggering
    }

    @Override
    public void postTick() {
        // Cooldown processing
        if (this.isConsumed && this.activeTicksRemaining > 0) {
            this.activeTicksRemaining--;
        }
    }

    @Override
    public void pushCurrentToPrevious() {
        // State is managed by the cooldown mechanism
    }

    @Override
    public String toString() {
        return this.hasBeenTriggered() ? "PULSE ACTIVE" : "Idling";
    }

    @Override
    public String getChatFormattedString() {
        var formatting = this.hasBeenTriggered() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY;
        return formatting + this.toString();
    }
}
