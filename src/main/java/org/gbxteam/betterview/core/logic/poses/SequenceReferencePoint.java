package org.gbxteam.betterview.core.logic.poses;

public enum SequenceReferencePoint {
    BEGINNING(0),
    END(1f);

    private final float progressThroughSequence;

    SequenceReferencePoint(float progressThroughSequence) {
        this.progressThroughSequence = progressThroughSequence;
    }

    public float getProgressThroughSequence() {
        return this.progressThroughSequence;
    }
}
