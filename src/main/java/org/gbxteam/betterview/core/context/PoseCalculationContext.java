package org.gbxteam.betterview.core.context;

import org.gbxteam.betterview.core.engine.motors.Driver;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.engine.motors.VariableDriver;
import org.gbxteam.betterview.core.skeleton.rig.RigSystem;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;
import org.gbxteam.betterview.core.helpers.TimeSpan;

import java.util.function.Function;

public record PoseCalculationContext (
        DriverGetter driverGetter,
        RigSystem jointSkeleton,
        MontageManager montageManager,
        float partialTicks,
        TimeSpan gameTime
) implements DriverGetter {

    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey) {
        return this.driverGetter.getDriver(driverKey);
    }

    @Override
    public <D, R extends Driver<D>> D getDriverValue(DriverKey<R> driverKey) {
        return this.getDriver(driverKey).getInterpolatedValue(this.partialTicks);
    }
}
