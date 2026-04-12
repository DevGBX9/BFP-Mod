package org.gbxteam.betterview.core.engine.controller.entity.firstperson;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.sys.utils.BFPMathUtils;
import org.gbxteam.betterview.sys.utils.BFPVectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.SimplexNoise;

public final class BFPCameraOverhaulSystem {

    private static final BFPCameraOverhaulSystem INSTANCE = new BFPCameraOverhaulSystem();

    private final Vector3d prevCameraEulerRot = new Vector3d();
    private final Vector3d prevEntityVelocity = new Vector3d();
    private double lastActionTime;
    
    // Real-time delta tracking (ported from Camera Overhaul's TimeSystem)
    private long prevNanoTime;
    private long currentNanoTime;
    private double deltaTime;
    private double accumulatedTime;
    
    // Extracted Modifiers from CameraOverhaul
    public final Vector3d offsetTransform = new Vector3d();

    public static BFPCameraOverhaulSystem getInstance() {
        return INSTANCE;
    }

    public double getAccumulatedTime() {
        return accumulatedTime;
    }

    private void updateTime() {
        prevNanoTime = currentNanoTime;
        currentNanoTime = System.nanoTime();

        if (prevNanoTime <= 0) {
            prevNanoTime = currentNanoTime - 1;
        }

        deltaTime = (currentNanoTime - prevNanoTime) / 1_000_000_000d;
        accumulatedTime += deltaTime;
    }

    public void notifyOfPlayerAction() {
        this.lastActionTime = accumulatedTime;
    }

    public void tick(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.isPaused() || !BFPMain.CONFIG.data().firstPersonPlayer.enableCameraOverhaul) {
            this.offsetTransform.set(0, 0, 0);
            return;
        }

        updateTime();

        // Reset the offset transform
        this.offsetTransform.set(0, 0, 0);

        Vector3d currentVelocity = BFPVectorUtils.toJoml(player.getDeltaMovement());
        // Since we are applying on bobHurt, camera rotation is roughly player rotation
        Vector3d currentCameraEulerRot = new Vector3d(player.getXRot(), player.getYRot(), 0);

        if (!currentVelocity.equals(prevEntityVelocity) || !currentCameraEulerRot.equals(prevCameraEulerRot)) {
            notifyOfPlayerAction();
        }

        // Use real frame-accurate delta time (not hardcoded)

        // Execute Authentic Camera Overhaul Physics
        verticalVelocityPitchOffset(currentVelocity, deltaTime);
        forwardVelocityPitchOffset(currentVelocity, currentCameraEulerRot, deltaTime);
        turningRollOffset(currentCameraEulerRot, deltaTime);
        strafingRollOffset(currentVelocity, currentCameraEulerRot, deltaTime);
        noiseOffset(accumulatedTime, deltaTime);

        // Screen Shakes (hand swing, etc.)
        Vector3d cameraPos = BFPVectorUtils.toJoml(player.getEyePosition());
        BFPScreenShakes.onCameraUpdate(cameraPos, 2.5); // screenShakesMaxIntensity = 2.5
        BFPScreenShakes.modifyCameraTransform(this.offsetTransform);

        this.prevEntityVelocity.set(currentVelocity);
        this.prevCameraEulerRot.set(currentCameraEulerRot);
    }

    // 1. Vertical Velocity Pitch
    private static final double BASE_VERTICAL_PITCH_SMOOTHING = 0.00004;
    private double prevVerticalVelocityPitchOffset;
    private void verticalVelocityPitchOffset(Vector3d velocity, double deltaTime) {
        double multiplier = BFPMain.CONFIG.data().firstPersonPlayer.coVerticalVelocityPitchFactor;
        double smoothing = BASE_VERTICAL_PITCH_SMOOTHING * BFPMain.CONFIG.data().firstPersonPlayer.coVerticalVelocitySmoothing;

        double targetOffset = velocity.y * multiplier;
        double currentOffset = BFPMathUtils.damp(prevVerticalVelocityPitchOffset, targetOffset, smoothing, deltaTime);
        
        this.offsetTransform.x += currentOffset;
        this.prevVerticalVelocityPitchOffset = currentOffset;
    }

    // 2. Forward Velocity Pitch
    private static final double BASE_FORWARD_PITCH_SMOOTHING = 0.008;
    private double prevForwardVelocityPitchOffset;
    private void forwardVelocityPitchOffset(Vector3d velocity, Vector3d cameraRot, double deltaTime) {
        double multiplier = BFPMain.CONFIG.data().firstPersonPlayer.coForwardVelocityPitchFactor;
        double smoothing = BASE_FORWARD_PITCH_SMOOTHING * BFPMain.CONFIG.data().firstPersonPlayer.coHorizontalVelocitySmoothing;

        Vector3d relativeVel = getForwardRelativeVelocity(velocity, cameraRot);
        double targetOffset = relativeVel.z * multiplier;
        // In Camera Overhaul, moving forward limits Z vel. relativeVel.z is used for forward speed.
        double currentOffset = BFPMathUtils.damp(prevForwardVelocityPitchOffset, targetOffset, smoothing, deltaTime);
        
        this.offsetTransform.x += currentOffset;
        this.prevForwardVelocityPitchOffset = currentOffset;
    }

    // 3. Turning Roll
    private static final double BASE_TURNING_ROLL_ACCUMULATION = 0.0048;
    private static final double BASE_TURNING_ROLL_INTENSITY = 1.25;
    private static final double BASE_TURNING_ROLL_SMOOTHING = 0.0825;
    private double turningRollTargetOffset;
    private void turningRollOffset(Vector3d cameraRot, double deltaTime) {
        double decaySmoothing = BASE_TURNING_ROLL_SMOOTHING * BFPMain.CONFIG.data().firstPersonPlayer.coTurningRollSmoothing;
        double intensity = BASE_TURNING_ROLL_INTENSITY * BFPMain.CONFIG.data().firstPersonPlayer.coTurningRollIntensity;
        double accumulation = BASE_TURNING_ROLL_ACCUMULATION * 1.0; // Hardcoded default

        double yawDelta = prevCameraEulerRot.y - cameraRot.y;

        // Decay
        turningRollTargetOffset = BFPMathUtils.damp(turningRollTargetOffset, 0, decaySmoothing, deltaTime);
        // Accumulation
        turningRollTargetOffset = BFPMathUtils.clamp(turningRollTargetOffset + (yawDelta * accumulation), -1.0, 1.0);
        // Apply
        double turningRollOffset = BFPMathUtils.clamp01(turningEasing(Math.abs(turningRollTargetOffset))) * intensity * Math.signum(turningRollTargetOffset);
        
        this.offsetTransform.z += turningRollOffset;
    }
    private static double turningEasing(double x) {
        return x < 0.5 ? (4 * x * x * x) : (1 - Math.pow(-2 * x + 2, 3) / 2);
    }

    // 4. Strafing Roll
    private static final double BASE_STRAFING_ROLL_SMOOTHING = 0.008;
    private double prevStrafingRollOffset;
    private void strafingRollOffset(Vector3d velocity, Vector3d cameraRot, double deltaTime) {
        double multiplier = BFPMain.CONFIG.data().firstPersonPlayer.coStrafingRollFactor;
        double smoothing = BASE_STRAFING_ROLL_SMOOTHING * BFPMain.CONFIG.data().firstPersonPlayer.coHorizontalVelocitySmoothing;

        Vector3d relativeVel = getForwardRelativeVelocity(velocity, cameraRot);
        double target = -relativeVel.x * multiplier;
        double offset = BFPMathUtils.damp(prevStrafingRollOffset, target, smoothing, deltaTime);

        this.offsetTransform.z += offset;
        this.prevStrafingRollOffset = offset;
    }

    // 5. Dynamic Sway Noise
    private static final double CAMERASWAY_FADING_SMOOTHNESS = 3.0;
    private double cameraSwayFactor;
    private double cameraSwayFactorTarget;
    private void noiseOffset(double time, double deltaTime) {
        float noiseX = (float)(time * 0.16); // cfg.general.cameraSwayFrequency

        if ((time - lastActionTime) < 0.15) { // cfg.general.cameraSwayFadeInDelay
            cameraSwayFactorTarget = 0;
        } else if (cameraSwayFactor == cameraSwayFactorTarget) {
            cameraSwayFactorTarget = 1;
        }

        double fadeLength = cameraSwayFactorTarget > 0 ? 5.0 : 0.75;
        double fadeStep = fadeLength > 0.0 ? deltaTime / fadeLength : 1.0;
        cameraSwayFactor = BFPMathUtils.stepTowards(cameraSwayFactor, cameraSwayFactorTarget, fadeStep);

        double scaledIntensity = 0.60 * Math.pow(cameraSwayFactor, CAMERASWAY_FADING_SMOOTHNESS); // cfg.general.cameraSwayIntensity
        Vector3d target = new Vector3d(scaledIntensity, scaledIntensity, 0.0);
        Vector3d noise = new Vector3d(
            SimplexNoise.noise(noiseX, 420),
            SimplexNoise.noise(noiseX, 1337),
            SimplexNoise.noise(noiseX, 6969)
        );

        this.offsetTransform.add(noise.mul(target));
    }

    // Helper Context
    private Vector3d getForwardRelativeVelocity(Vector3d velocity, Vector3d cameraRot) {
        Vector2d temp = BFPVectorUtils.rotate(new Vector2d(velocity.x, velocity.z), 360d - cameraRot.y);
        return new Vector3d(temp.x, velocity.y, temp.y);
    }
}
