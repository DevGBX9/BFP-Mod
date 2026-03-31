package org.gbxteam.betterview.sys.bridge;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface MatrixModelPart {
    void bfp$setMatrix(Matrix4f matrix4f);
    Matrix4f bfp$getMatrix();
}
