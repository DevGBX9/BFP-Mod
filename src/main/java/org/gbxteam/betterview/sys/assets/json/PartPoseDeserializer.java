package org.gbxteam.betterview.sys.assets.json;

import com.google.gson.*;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.PartPose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class PartPoseDeserializer implements JsonDeserializer<PartPose> {

    /** BFP PartPose JSON Parser */
    private static final String BFP_TRANSLATION = "translation";
    private static final String BFP_ROTATION = "rotation";
    private static final String BFP_SCALE = "scale";

    private static final Vector3f ZERO_VEC = new Vector3f(0, 0, 0);
    private static final Vector3f ZERO_ROT = new Vector3f(0, 0, 0);
    private static final Vector3f UNIT_SCALE = new Vector3f(1, 1, 1);

    @Override
    public PartPose deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject poseJson = json.getAsJsonObject();
        Vector3f trans = GsonConfiguration.deserializeWithFallback(ctx, poseJson, BFP_TRANSLATION, Vector3f.class, ZERO_VEC);
        Vector3f rot = GsonConfiguration.deserializeWithFallback(ctx, poseJson, BFP_ROTATION, Vector3f.class, ZERO_ROT);
        Vector3f scl = GsonConfiguration.deserializeWithFallback(ctx, poseJson, BFP_SCALE, Vector3f.class, UNIT_SCALE);
        return new PartPose(
                trans.x(), trans.y(), trans.z(),
                rot.x(), rot.y(), rot.z(),
                scl.x(), scl.y(), scl.z()
        );
    }
}
