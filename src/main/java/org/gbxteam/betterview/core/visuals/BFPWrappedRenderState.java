package org.gbxteam.betterview.core.visuals;

import org.gbxteam.betterview.core.context.AnimationDataContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BFPWrappedRenderState<D> {

    /** BFP Render State Wrapper - encapsulates animation data with vanilla render state. */
    private final D bfp$wrappedValue;
    private final AnimationDataContainer bfp$animDataContainer;

    private BFPWrappedRenderState(D wrapped, @Nullable AnimationDataContainer animData) {
        this.bfp$wrappedValue = wrapped;
        this.bfp$animDataContainer = animData;
    }

    public static <D> BFPWrappedRenderState<D> of(D innerValue, @Nullable AnimationDataContainer dataContainer) {
        return new BFPWrappedRenderState<>(innerValue, dataContainer);
    }

    public D getInnerValue() {
        return this.bfp$wrappedValue;
    }

    public Optional<AnimationDataContainer> getDataContainer() {
        return Optional.ofNullable(this.bfp$animDataContainer);
    }
}
