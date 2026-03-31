package org.gbxteam.betterview.core.context;

import org.gbxteam.betterview.core.engine.motors.Driver;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.engine.motors.VariableDriver;

import java.util.function.Function;

public interface DriverGetter {

    default <D, R extends Driver<D>> D getDriverValue(DriverKey<R> driverKey) {
        return this.getDriver(driverKey).getCurrentValue();
    }

    <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey);
}
