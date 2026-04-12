package org.gbxteam.betterview.sys.assets.json;

import com.google.gson.*;
import org.gbxteam.betterview.core.logic.timelines.MovementFlow;
import org.gbxteam.betterview.sys.assets.FormatVersion;
import org.gbxteam.betterview.core.helpers.Interpolator;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Timeline;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class MovementFlowDeserializer implements JsonDeserializer<MovementFlow> {

    /** BFP Animation Sequence JSON Parser */
    private static final String LENGTH_KEY = "length";
    private static final String JOINT_SKELETON_KEY = "joint_skeleton";
    private static final String JOINT_CHANNELS_KEY = "joint_channels";
    private static final String CUSTOM_ATTRIBUTES_KEY = "custom_attributes";
    private static final String TIME_MARKERS_KEY = "time_markers";

    private static final List<String> REQUIRED_KEYS = List.of(LENGTH_KEY, JOINT_CHANNELS_KEY);

    @Override
    public MovementFlow deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject seqJson = jsonElement.getAsJsonObject();

        FormatVersion ver = FormatVersion.ofAssetJsonObject(seqJson);
        if (ver.isIncompatible()) {
            throw new JsonParseException("Animation sequence version is out of date for deserializer.");
        }

        for (String requiredKey : REQUIRED_KEYS) {
            if (!seqJson.has(requiredKey)) {
                throw new JsonParseException("Required key " + requiredKey + " not found in data.");
            }
        }

        var parsedId = Identifier.read(seqJson.get(JOINT_SKELETON_KEY).getAsString()).result();
        Identifier skeletonRef;
        if (parsedId.isPresent()) {
            skeletonRef = parsedId.get().withPath(path -> "skeletons/" + path + ".json");
        } else {
            throw new JsonParseException("Joint skeleton resource location " + seqJson.get(JOINT_SKELETON_KEY).getAsString() + " is invalid.");
        }

        float duration = seqJson.get(LENGTH_KEY).getAsFloat();
        MovementFlow.Builder seqBuilder = MovementFlow.builder(TimeSpan.ofSeconds(duration), skeletonRef);

        JsonObject channelsJson = seqJson.getAsJsonObject(JOINT_CHANNELS_KEY);
        channelsJson.asMap().forEach((jointName, jointEl) -> {
            JsonObject jointJson = jointEl.getAsJsonObject();
            Timeline<Vector3f> translationTl = bfp$parseTimeline(ctx, jointJson, "translation", Vector3f.class, Interpolator.VECTOR_FLOAT, duration);
            Timeline<Quaternionf> rotationTl = bfp$parseTimeline(ctx, jointJson, "rotation", Quaternionf.class, Interpolator.QUATERNION, duration);
            Timeline<Vector3f> scaleTl = bfp$parseTimeline(ctx, jointJson, "scale", Vector3f.class, Interpolator.VECTOR_FLOAT, duration);
            Timeline<Boolean> visibilityTl = bfp$parseTimeline(ctx, jointJson, "visibility", Boolean.class, Interpolator.BOOLEAN_KEYFRAME, duration);

            seqBuilder.putJointTranslationTimeline(jointName, translationTl);
            seqBuilder.putJointRotationTimeline(jointName, rotationTl);
            seqBuilder.putJointScaleTimeline(jointName, scaleTl);
            seqBuilder.putJointVisibilityTimeline(jointName, visibilityTl);
        });

        JsonObject attrsJson = seqJson.getAsJsonObject(CUSTOM_ATTRIBUTES_KEY);
        attrsJson.asMap().forEach((attrName, attrEl) -> {
            seqBuilder.putCustomAttributeTimeline(attrName, bfp$parseTimeline(ctx, attrsJson, attrName, Float.class, Interpolator.FLOAT, duration));
        });

        if (seqJson.has(TIME_MARKERS_KEY)) {
            Map<String, JsonElement> markers = seqJson.getAsJsonObject(TIME_MARKERS_KEY).asMap();
            markers.forEach((markerId, markerEl) -> {
                JsonArray times = markerEl.getAsJsonArray();
                times.forEach(t -> seqBuilder.putTimeMarker(markerId, TimeSpan.ofSeconds(t.getAsFloat())));
            });
        }
        return seqBuilder.build();
    }

    private static <X> Timeline<X> bfp$parseTimeline(JsonDeserializationContext ctx, JsonObject json, String key, Class<X> type, Interpolator<X> interp, float totalLength) {
        Timeline<X> tl = Timeline.of(interp, totalLength);
        json.getAsJsonObject(key).asMap().forEach((kfStr, kfVal) -> {
            float kf = Float.parseFloat(kfStr);
            tl.addKeyframe(kf, ctx.deserialize(kfVal, type));
        });
        return tl;
    }
}
