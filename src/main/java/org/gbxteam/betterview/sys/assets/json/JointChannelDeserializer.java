package org.gbxteam.betterview.sys.assets.json;

import com.google.gson.*;
import com.mojang.math.Axis;
import org.gbxteam.betterview.core.skeleton.JointChannel;
import net.minecraft.util.GsonHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class JointChannelDeserializer implements JsonDeserializer<JointChannel> {

    /** BFP Joint Channel JSON Parser */
    private static final String BFP_TRANSLATION = "translation";
    private static final String BFP_ROTATION = "rotation";
    private static final String BFP_SCALE = "scale";
    private static final String BFP_VISIBILITY = "visibility";

    private static final Vector3f ZERO_VEC = new Vector3f(0, 0, 0);
    private static final Quaternionf IDENTITY_QUAT = Axis.XP.rotation(0);
    private static final Vector3f UNIT_SCALE = new Vector3f(1, 1, 1);
    private static final boolean DEFAULT_VIS = true;

    @Override
    public JointChannel deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject channelJson = json.getAsJsonObject();
        return JointChannel.ofTranslationRotationScaleQuaternion(
                GsonConfiguration.deserializeWithFallback(ctx, channelJson, BFP_TRANSLATION, Vector3f.class, ZERO_VEC),
                GsonConfiguration.deserializeWithFallback(ctx, channelJson, BFP_ROTATION, Quaternionf.class, IDENTITY_QUAT),
                GsonConfiguration.deserializeWithFallback(ctx, channelJson, BFP_SCALE, Vector3f.class, UNIT_SCALE),
                Boolean.TRUE.equals(GsonConfiguration.deserializeWithFallback(ctx, channelJson, BFP_VISIBILITY, Boolean.class, DEFAULT_VIS))
        );
    }
}
