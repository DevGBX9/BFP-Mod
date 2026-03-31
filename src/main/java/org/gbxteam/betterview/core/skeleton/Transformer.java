package org.gbxteam.betterview.core.skeleton;

@FunctionalInterface
public interface Transformer<X> {
    void transform(JointChannel jointChannel, X value, JointChannel.TransformSpace transformSpace, JointChannel.TransformType transformType);
}
