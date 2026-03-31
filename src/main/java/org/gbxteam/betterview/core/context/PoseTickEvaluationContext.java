package org.gbxteam.betterview.core.context;

import org.gbxteam.betterview.core.engine.motors.Driver;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.engine.motors.VariableDriver;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;

import java.util.function.Function;

public record PoseTickEvaluationContext(
        DriverGetter driverGetter,
        MontageManager montageManager,
        boolean resetting,
        long currentTick
) implements DriverGetter {

    /**
     * Creates a copy of the evaluation state that is marked for a hard reset.
     *
     * <p>A hard reset is an animation reset that immediately resets with no blending.</p>
     */
    public PoseTickEvaluationContext markedForReset() {
        return new PoseTickEvaluationContext(this.driverGetter, this.montageManager, true, this.currentTick);
    }

    public PoseTickEvaluationContext cleared() {
        return new PoseTickEvaluationContext(this.driverGetter, this.montageManager, false, this.currentTick);
    }

    /**
     * Runs the provided function if this evaluation state is marked for hard reset.
     */
    public void ifMarkedForReset(Runnable runnable) {
        if (this.resetting) {
            runnable.run();
        }
    }

    @Override
    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey) {
        return this.driverGetter.getDriver(driverKey);
    }

    public <D, R extends VariableDriver<D>> void setVariableDriverValue(DriverKey<R> driverKey, D newValue) {
        this.getDriver(driverKey).setValue(newValue);
    }
}
