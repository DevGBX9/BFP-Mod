package org.gbxteam.betterview.sys.assets.json;

import com.google.gson.*;
import org.gbxteam.betterview.core.skeleton.JointChannel;
import org.gbxteam.betterview.core.skeleton.rig.RigSystem;
import org.gbxteam.betterview.core.logic.timelines.MovementFlow;
import org.gbxteam.betterview.sys.assets.FormatVersion;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GsonConfiguration {

    /** BFP Gson Serialization Engine */
    private static final Gson BFP_GSON_INSTANCE = bfp$buildGsonInstance();

    private static Gson bfp$buildGsonInstance() {
        return new GsonBuilder()
                .setStrictness(Strictness.STRICT)
                .setPrettyPrinting()
                .registerTypeAdapter(Vector3f.class, bfp$vec3fDeserializer())
                .registerTypeAdapter(Quaternionf.class, bfp$quaternionDeserializer())
                .registerTypeAdapter(MovementFlow.class, new MovementFlowDeserializer())
                .registerTypeAdapter(RigSystem.class, new RigSystemDeserializer())
                .registerTypeAdapter(FormatVersion.class, FormatVersion.getDeserializer())
                .registerTypeAdapter(JointChannel.class, new JointChannelDeserializer())
                .registerTypeAdapter(PartPose.class, new PartPoseDeserializer())
                .registerTypeAdapter(ModelPart.class, new ModelPartSerializer())
                .create();
    }

    public static Gson getInstance() {
        return BFP_GSON_INSTANCE;
    }

    public static <D> D deserializeWithFallback(JsonDeserializationContext ctx, JsonObject json, String key, Class<D> type, D fallback) {
        if (!json.has(key)) {
            return fallback;
        }
        if (json.get(key).isJsonNull()) {
            return null;
        }
        return ctx.deserialize(json.get(key), type);
    }

    private static JsonDeserializer<Vector3f> bfp$vec3fDeserializer() {
        return (el, type, ctx) -> {
            JsonArray arr = el.getAsJsonArray();
            return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
        };
    }

    private static JsonDeserializer<Quaternionf> bfp$quaternionDeserializer() {
        return (el, type, ctx) -> {
            JsonArray arr = el.getAsJsonArray();
            float zRad = arr.get(2).getAsFloat() * Mth.DEG_TO_RAD;
            float yRad = arr.get(1).getAsFloat() * Mth.DEG_TO_RAD;
            float xRad = arr.get(0).getAsFloat() * Mth.DEG_TO_RAD;
            return new Quaternionf().rotationZYX(zRad, yRad, xRad);
        };
    }
}
