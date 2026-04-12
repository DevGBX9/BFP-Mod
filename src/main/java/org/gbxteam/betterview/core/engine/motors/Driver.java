package org.gbxteam.betterview.core.engine.motors;

/**
 * ==========================================================================================
 * BFP ANIMATION ENGINE - CORE DRIVER COMPONENT
 * Copyright (c) 2024 gbxteam. All Rights Reserved.
 * ==========================================================================================
 *
 * The Driver interface provides the foundational contract for data variables that orchestrate
 * the runtime pose calculations. Drivers encapsulate changing data over time (such as vectors,
 * floats, or triggers) and process their interpolation natively.
 * 
 * @param <D> The underlying data structure managed by this driver.
 */
public interface Driver<D> {

    /**
     * Internal processor called once per active game tick. 
     * This prepares the driver's state before any pose mathematics are evaluated.
     */
    void tick();

    /**
     * Executes the final cleanup logic for the data driver after the pose graph 
     * has fully processed the tick.
     */
    void postTick();

    /**
     * Propagates the stored active value back to the historical (previous) value,
     * ensuring that subsequent ticks have valid reference points for interpolation mapping.
     */
    void pushCurrentToPrevious();

    /**
     * Retrieves the snapshot of the active data value presently held by the driver.
     * 
     * @return The active discrete value.
     */
    D getCurrentValue();

    /**
     * Computes the blended (interpolated) result between the historical value and the currently active one.
     * 
     * @param fractionalTick The normalized time threshold between [0.0 - 1.0].
     * @return The seamlessly computed output value for rendering.
     */
    D getInterpolatedValue(float fractionalTick);

    /**
     * Helper string conversion for rendering driver diagnostics on the chat overlay or debug tools.
     * 
     * @return A chat-formatted human-readable diagnostic snapshot.
     */
    String getChatFormattedString();
}
