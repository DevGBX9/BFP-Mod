package org.gbxteam.betterview.sys.assets.json;

import com.google.gson.*;
import org.gbxteam.betterview.core.skeleton.JointChannel;
import org.gbxteam.betterview.core.skeleton.rig.RigSystem;
import org.gbxteam.betterview.sys.assets.FormatVersion;
import net.minecraft.client.model.geom.PartPose;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RigSystemDeserializer implements JsonDeserializer<RigSystem> {

    /** BFP Joint Skeleton JSON Parser */
    private static final String ROOT_KEY = "root_joint";
    private static final String JOINTS_KEY = "joints";
    private static final String CUSTOM_ATTR_KEY = "custom_attributes";
    private static final String ATTR_TYPE_KEY = "type";
    private static final String ATTR_DEFAULT_KEY = "default_value";

    private static final List<String> REQUIRED_SKELETON_KEYS = List.of(ROOT_KEY, JOINTS_KEY);

    private static final String CHILDREN_KEY = "children";
    private static final String MIRROR_KEY = "mirror_joint";
    private static final String MODEL_PART_ID_KEY = "model_part_identifier";
    private static final String REF_POSE_KEY = "reference_pose";
    private static final String MODEL_OFFSET_KEY = "model_part_offset";
    private static final String MODEL_SPACE_PARENT_KEY = "model_part_space_parent";

    private static final List<String> REQUIRED_JOINT_KEYS = List.of(CHILDREN_KEY, REF_POSE_KEY);

    @Override
    public RigSystem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject skelJson = jsonElement.getAsJsonObject();

        FormatVersion ver = FormatVersion.ofAssetJsonObject(skelJson);
        if (ver.isIncompatible()) {
            throw new JsonParseException("Animation sequence version is out of date for deserializer.");
        }
        for (String key : REQUIRED_SKELETON_KEYS) {
            if (!skelJson.has(key)) {
                throw new JsonParseException("Required key " + key + " not found in skeleton data.");
            }
        }

        String rootJoint = skelJson.get(ROOT_KEY).getAsString();
        RigSystem.Builder skelBuilder = RigSystem.of(rootJoint);
        Map<String, JsonElement> jointsMap = skelJson.get(JOINTS_KEY).getAsJsonObject().asMap();

        bfp$parseJointTree(rootJoint, null, jointsMap, ctx, skelBuilder);

        if (skelJson.has(CUSTOM_ATTR_KEY)) {
            skelJson.get(CUSTOM_ATTR_KEY).getAsJsonObject().asMap().forEach((attrName, attrJson) -> {
                boolean isFloat = Objects.equals(attrJson.getAsJsonObject().get(ATTR_TYPE_KEY).getAsString(), "float");
                if (isFloat) {
                    float defaultVal = attrJson.getAsJsonObject().get(ATTR_DEFAULT_KEY).getAsFloat();
                    skelBuilder.defineCustomAttribute(attrName, defaultVal);
                }
            });
        }

        return skelBuilder.build();
    }

    private void bfp$parseJointTree(
            String joint,
            @Nullable String parent,
            Map<String, JsonElement> jointsMap,
            JsonDeserializationContext ctx,
            RigSystem.Builder skelBuilder
    ) {
        if (!jointsMap.containsKey(joint)) {
            throw new JsonParseException("Joint \"" + joint + "\" being defined is not present in the skeleton.");
        }

        RigSystem.JointConfiguration.Builder cfgBuilder = RigSystem.JointConfiguration.builder();
        JsonObject jointJson = jointsMap.get(joint).getAsJsonObject();

        for (String key : REQUIRED_JOINT_KEYS) {
            if (!jointJson.has(key)) {
                throw new JsonParseException("Required key " + key + " not found in joint data.");
            }
        }

        if (!jointsMap.containsKey(parent) && parent != null) {
            throw new JsonParseException("Joint \"" + joint + "\" being defined has parent \"" + parent + "\" that is not present in the skeleton.");
        }

        for (JsonElement childEl : jointJson.get(CHILDREN_KEY).getAsJsonArray()) {
            String child = childEl.getAsString();
            if (!jointsMap.containsKey(child)) {
                throw new JsonParseException("Joint \"" + joint + "\" in skeleton has child \"" + joint + "\" that is not present in the skeleton.");
            }
            cfgBuilder.addChild(child);
            bfp$parseJointTree(child, joint, jointsMap, ctx, skelBuilder);
        }

        cfgBuilder.setParent(parent);
        cfgBuilder.setReferencePose(GsonConfiguration.deserializeWithFallback(ctx, jointJson, REF_POSE_KEY, JointChannel.class, JointChannel.ZERO));
        cfgBuilder.setMirrorJoint(GsonConfiguration.deserializeWithFallback(ctx, jointJson, MIRROR_KEY, String.class, null));
        cfgBuilder.setModelPartIdentifier(GsonConfiguration.deserializeWithFallback(ctx, jointJson, MODEL_PART_ID_KEY, String.class, null));
        cfgBuilder.setModelPartOffset(GsonConfiguration.deserializeWithFallback(ctx, jointJson, MODEL_OFFSET_KEY, PartPose.class, PartPose.ZERO));

        String spaceParent = GsonConfiguration.deserializeWithFallback(ctx, jointJson, MODEL_SPACE_PARENT_KEY, String.class, null);
        if (jointsMap.containsKey(spaceParent)) {
            cfgBuilder.setModelPartSpaceParent(spaceParent);
        }
        skelBuilder.defineJoint(joint, cfgBuilder.build());
    }
}
