package org.gbxteam.betterview.core.logic.poses;

import org.gbxteam.betterview.core.helpers.TimeSpan;
import net.minecraft.util.Tuple;

public interface AnimationPlayer {

    /**
     * Returns the remaining time in the sequence player at the previous tick and the current tick.
     * Meant to be called in contexts just prior to this pose function updating
     * @implNote    Tuple should be (remainingTime - playRate, remainingTime)
     */
    Tuple<TimeSpan, TimeSpan> getRemainingTime();

    /**
     * Returns the length of the animation currently being played.
     */
    TimeSpan getAnimationLength();
}
