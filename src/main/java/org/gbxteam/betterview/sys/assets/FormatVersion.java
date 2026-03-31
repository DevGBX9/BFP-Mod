package org.gbxteam.betterview.sys.assets;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record FormatVersion(int version) {

    /** BFP Resource Format Versioning */
    private static final Logger BFP_FV_LOGGER = LogManager.getLogger("BFP/Resources/FormatVersion");
    private static final String FORMAT_VERSION_KEY = "format_version";

    public static FormatVersion of(int version) {
        return new FormatVersion(version);
    }

    public static FormatVersion ofDefault() {
        return FormatVersion.of(1);
    }

    /** Returns true if the format version is below the minimum supported version. */
    public boolean isIncompatible() {
        return this.version < 5;
    }

    public static JsonDeserializer<FormatVersion> getDeserializer() {
        return (jsonElement, type, context) -> FormatVersion.of(jsonElement.getAsInt());
    }

    public static FormatVersion ofAssetJsonObject(JsonObject assetJson) {
        boolean hasVersionField = assetJson.has(FORMAT_VERSION_KEY);
        if (hasVersionField) {
            return FormatVersion.of(assetJson.get(FORMAT_VERSION_KEY).getAsInt());
        }
        throw new JsonParseException("Asset does not contain valid format version field in JSON data.");
    }
}
