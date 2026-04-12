package org.gbxteam.betterview.core.skeleton;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gbxteam.betterview.core.helpers.Interpolator;
import net.minecraft.client.model.geom.PartPose;
import org.joml.*;

public final class JointChannel {

    /** BFP Joint Transform Channel */
    private final Matrix4f transform;
    private boolean visibility;

    public static final JointChannel ZERO = JointChannel.ofPartPose(PartPose.ZERO);

    private JointChannel(Matrix4f transform, boolean visibility) {
        this.transform = transform;
        this.visibility = visibility;
    }

    public static JointChannel of(Matrix4f transform, boolean visibility){
        return new JointChannel(new Matrix4f(transform), visibility);
    }

    public static JointChannel of(JointChannel jointChannel){
        return JointChannel.of(jointChannel.getTransform(), jointChannel.visibility);
    }

    public static JointChannel ofPartPose(PartPose partPose){
        return ofTranslationRotationScaleEuler(new Vector3f(partPose.x(), partPose.y(), partPose.z()), new Vector3f(partPose.xRot(), partPose.yRot(), partPose.zRot()), new Vector3f(partPose.xScale(), partPose.yScale(), partPose.zScale()), true);
    }

    public static JointChannel ofTranslationRotationScaleEuler(Vector3f translation, Vector3f rotationEuler, Vector3f scale, boolean visibility){
        return ofTranslationRotationScaleQuaternion(translation, new Quaternionf().rotationZYX(rotationEuler.z(), rotationEuler.y(), rotationEuler.x()), scale, visibility);
    }

    public static JointChannel ofTranslationRotationScaleQuaternion(Vector3f translation, Quaternionf rotation, Vector3f scale, boolean visibility){
        return of(new Matrix4f().translationRotateScale(translation, rotation, scale), visibility);
    }

    public Matrix4f getTransform() {
        return new Matrix4f(this.transform);
    }

    public boolean getVisibility() {
        return this.visibility;
    }

    public Vector3f getTranslation() {
        return this.transform.getTranslation(new Vector3f());
    }

    public Quaternionf getRotation() {
        return this.transform.getNormalizedRotation(new Quaternionf());
    }

    public Vector3f getEulerRotationZYX() {
        return this.transform.getEulerAnglesZYX(new Vector3f());
    }

    public Vector3f getScale() {
        return this.transform.getScale(new Vector3f());
    }

    public PartPose asPartPose(){
        Vector3f rotation = this.getEulerRotationZYX();
        Vector3f translation = this.getTranslation();
        return PartPose
                .offsetAndRotation(
                        translation.x(),
                        translation.y(),
                        translation.z(),
                        rotation.x(),
                        rotation.y(),
                        rotation.z()
                );
    }

    public void setIdentity() {
        this.transform.identity();
    }

    public void translate(Vector3f translation, TransformSpace transformSpace, TransformType transformType) {
        switch (transformType){
            case ADD -> {
                if(translation.x() != 0 || translation.y() != 0 || translation.z() != 0){
                    switch (transformSpace){
                        case LOCAL -> this.transform.translate(translation);
                        case COMPONENT, PARENT -> this.transform.translateLocal(translation);
                    }
                }
            }
            case REPLACE -> this.transform.setTranslation(translation);
        }
    }

    public void rotate(Quaternionf rotation, TransformSpace transformSpace, TransformType transformType) {
        switch (transformType){
            case ADD -> {
                switch (transformSpace){
                    //case COMPONENT, PARENT -> this.transform.rotation(this.transform.getNormalizedRotation(new Quaternionf()).premul(rotation));
                    case LOCAL -> this.transform.rotate(rotation);
                    case COMPONENT, PARENT -> {
                        Quaternionf currentRotation = this.transform.getUnnormalizedRotation(new Quaternionf());
                        rotation.mul(currentRotation, currentRotation);
                        this.transform.translationRotateScale(this.getTranslation(), currentRotation, this.getScale());
                    }
                }
            }
            case REPLACE -> this.transform.translationRotateScale(this.getTranslation(), rotation, this.getScale());
        }
    }

    public void scale(Vector3f scale, TransformSpace transformSpace, TransformType transformType) {
        switch (transformType){
            case ADD -> {
                switch (transformSpace){
                    //case COMPONENT, PARENT -> this.transform.rotation(this.transform.getNormalizedRotation(new Quaternionf()).premul(rotation));
                    case LOCAL -> this.transform.scale(scale);
                    case COMPONENT, PARENT -> this.transform.scaleLocal(scale.x, scale.y, scale.z);

                }
            }
            case REPLACE -> {
                Matrix4f scaled = new Matrix4f().identity();
                scaled.translationRotateScale(this.getTranslation(), this.getRotation(), scale);
                this.transform.set(scaled);
            }
        }
    }

    public void rotate(Vector3f rotationEuler, TransformSpace transformSpace, TransformType transformType) {
        this.rotate(new Quaternionf().rotationXYZ(rotationEuler.x(), rotationEuler.y(), rotationEuler.z()), transformSpace, transformType);
    }

    public void multiply(JointChannel other, TransformSpace transformSpace, TransformType transformType) {
        this.multiply(other.transform, transformSpace, transformType);
    }

    public void multiply(Matrix4f xform, TransformSpace transformSpace, TransformType transformType) {
        switch (transformType) {
            case ADD -> {
                switch (transformSpace) {
                    case COMPONENT, PARENT -> this.transform.mul(xform);
                    case LOCAL -> xform.mul(this.transform, this.transform);
                }
            }
            case REPLACE -> this.transform.set(xform);
        }
    }

    public void invert() {
        this.transform.invert();
    }

    private static final Quaternionf BFP_QUAT_CACHE = new Quaternionf();
    private static final Vector3f BFP_VEC_CACHE = new Vector3f();

    public JointChannel mirrored() {
        Vector3f mirroredTrans = this.getTranslation().mul(-1, 1, 1);
        Vector3f mirroredRot = this.transform.getUnnormalizedRotation(BFP_QUAT_CACHE).getEulerAnglesZYX(BFP_VEC_CACHE).mul(1, -1, -1);
        return JointChannel.ofTranslationRotationScaleEuler(mirroredTrans, mirroredRot, this.getScale(), this.visibility);
    }

    /**
     * Returns a joint channel interpolated between this pose and the provided pose.
     * @param other             Joint channel to interpolate to
     * @param weight            Weight value, 0 is the original channel and 1 is the other channel.
     * @param destination       Channel to save interpolated channel onto.
     */
    public JointChannel interpolate(JointChannel other, float weight, JointChannel destination) {
        Vector3f translation = this.transform.getTranslation(new Vector3f());
        Quaternionf rotation = this.transform.getUnnormalizedRotation(new Quaternionf());
        Vector3f scale = this.transform.getScale(new Vector3f());

        Vector3f otherTranslation = other.transform.getTranslation(new Vector3f());
        Quaternionf otherRotation = other.transform.getUnnormalizedRotation(new Quaternionf());
        Vector3f otherScale = other.transform.getScale(new Vector3f());

        translation.lerp(otherTranslation, weight);
        rotation.slerp(otherRotation, weight);
        scale.lerp(otherScale, weight);
        boolean visibility = Interpolator.BOOLEAN_BLEND.interpolate(this.visibility, other.visibility, weight);

        destination.transform.translationRotateScale(translation, rotation, scale);
        destination.visibility = visibility;
        return destination;
    }

    /**
     * Returns this joint channel interpolated between this pose and the provided pose.
     * @param other             Joint channel to interpolate to
     * @param weight            Weight value, 0 is the original channel and 1 is the other channel.
     */
    public JointChannel interpolate(JointChannel other, float weight) {
        return this.interpolate(other, weight, this);
    }

    public void transformPoseStack(PoseStack poseStack, float scale) {
        Matrix4f mtx = new Matrix4f(this.transform);
        poseStack.mulPose(mtx.setTranslation(this.getTranslation().div(new Vector3f(scale))));
    }

    public void transformPoseStack(PoseStack poseStack) {
        this.transformPoseStack(poseStack, 1F);
    }

    public enum TransformSpace {
        COMPONENT,
        PARENT,
        LOCAL
    }

    public enum TransformType {
        IGNORE,
        REPLACE,
        ADD
    }
}
