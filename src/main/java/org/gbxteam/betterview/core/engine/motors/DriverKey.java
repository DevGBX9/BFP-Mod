package org.gbxteam.betterview.core.engine.motors;

import org.gbxteam.betterview.core.context.AnimationDataContainer;

import java.util.function.Supplier;

/**
 * Key for animation drivers that are stored once per data container.
 * <p>
 * These keys are used for storing the default value of a driver object, to initialize every time a
 * driver is instanced from a new data container.
 * <p>
 *
 * @see AnimationDataContainer
 * @author gbxteam
 */
public class DriverKey<R extends Driver<?>> {

    private final String identifier;
    private final Supplier<R> defaultValue;

    protected DriverKey(String identifier, Supplier<R> defaultValue){
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    public static <R extends Driver<?>> DriverKey<R> of(String identifier, Supplier<R> defaultValue){
        return new DriverKey<>(identifier, defaultValue);
    }

    /**
     * Returns the string identifier for this animation data key.
     */
    public String getIdentifier(){
        return this.identifier;
    }

    /**
     * Creates a new instance from the data key's default supplier.
     */
    public R createInstance(){
        return this.defaultValue.get();
    }
}
