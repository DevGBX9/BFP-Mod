package org.gbxteam.betterview.core.engine.controller.entity.firstperson;

import org.gbxteam.betterview.sys.utils.BFPMathUtils;
import org.joml.SimplexNoise;
import org.joml.Vector3d;
import org.joml.Math;

/**
 * Ported from Camera Overhaul's ScreenShakes system.
 * Handles procedural screen shakes triggered by hand swings, explosions, etc.
 */
public final class BFPScreenShakes {

    public static final class Slot {
        public float trauma;
        public float radius;
        public float frequency;
        public float lengthInSeconds;
        public final Vector3d position = new Vector3d(Double.POSITIVE_INFINITY);
        double startTime;
        short version = 1;

        public boolean hasPosition() { return position.isFinite(); }

        private Slot() { }

        public void setDefaults() {
            trauma = 0.5f;
            radius = 10.0f;
            frequency = 1.0f;
            lengthInSeconds = 2.0f;
            position.set(Double.POSITIVE_INFINITY);
            startTime = BFPCameraOverhaulSystem.getInstance().getAccumulatedTime();
        }
    }

    private static final Slot dummyInstance = new Slot();
    private static final Vector3d calculatedOffset = new Vector3d();
    private static final Slot[] instances = new Slot[64];
    private static long instanceMask;

    private BFPScreenShakes() {}

    public static void onCameraUpdate(Vector3d cameraPosition, double maxIntensity) {
        getNoiseAtPosition(cameraPosition, calculatedOffset);
        calculatedOffset.mul(maxIntensity);
    }

    public static void modifyCameraTransform(Vector3d eulerRot) {
        eulerRot.add(calculatedOffset);
    }

    private static int extractIndex(long handle) { return (int)handle; }
    private static int extractVersion(long handle) { return (int)(handle >> 32); }
    private static long constructHandle(int index, int version) { return ((long)index) | ((long)version << 32); }
    private static boolean isHandleValid(long handle) { return handle != 0L && instances[extractIndex(handle)].version == extractVersion(handle); }

    public static Slot get(long handle) { return isHandleValid(handle) ? instances[extractIndex(handle)] : dummyInstance; }
    public static long create() {
        if (instanceMask == Long.MAX_VALUE) return 0L;

        int index = Long.numberOfTrailingZeros(~instanceMask);
        if (instances[index] == null) instances[index] = new Slot();
        int version = instances[index].version;

        instanceMask |= 1L << index;
        instances[index].setDefaults();

        return constructHandle(index, version);
    }
    public static long recreate(long handle) {
        if (!isHandleValid(handle)) return create();
        get(handle).setDefaults();
        return handle;
    }

    public static void getNoiseAtPosition(Vector3d position, Vector3d noise) {
        var mask = instanceMask;
        double time = BFPCameraOverhaulSystem.getInstance().getAccumulatedTime();
        float sampleBase = (float)(time * 6.0); // screenShakesMaxFrequency = 6.0

        float total = 0f;
        noise.set(0.0);

        while (mask != 0L) {
            int index = Long.numberOfTrailingZeros(mask);
            mask &= ~(1L << index);

            var ss = instances[index];
            float progress = ss.lengthInSeconds > 0.0 ? (float)Math.clamp((time - ss.startTime) / ss.lengthInSeconds, 0.0, 1.0) : 1f;
            if (progress >= 1f) {
                instanceMask &= ~(1L << index);
                ss.version++;
                continue;
            }

            float decay = 1f - progress;
            float intensity = Math.clamp(ss.trauma, 0f, 1f) * (decay * decay);

            if (ss.hasPosition()) {
                float distance = (float)position.distance(ss.position);
                float distanceFactor = 1f - Math.min(1f, distance / ss.radius);
                intensity *= (distanceFactor * distanceFactor);
            }

            if (intensity <= 0f || !Float.isFinite(intensity)) continue;

            float sampleStep = sampleBase * ss.frequency;
            noise.add(
                SimplexNoise.noise(sampleStep, -69) * intensity,
                SimplexNoise.noise(sampleStep, -420) * intensity,
                SimplexNoise.noise(sampleStep, -1337) * intensity
            );
            total += intensity;
        }

        if (total > 1.0) noise.div(total);
    }
}
