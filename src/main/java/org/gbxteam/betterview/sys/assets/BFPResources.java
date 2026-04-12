package org.gbxteam.betterview.sys.assets;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.skeleton.rig.RigSystem;
import org.gbxteam.betterview.core.logic.timelines.MovementFlow;
import org.gbxteam.betterview.sys.assets.json.GsonConfiguration;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BFPResources implements PreparableReloadListener {

    /** BFP Core Resource Loader - GBXTeam */
    private static final Logger BFP_RES_LOGGER = LogManager.getLogger("BFP/Resources");

    public static final Identifier RELOADER_IDENTIFIER = Identifier.fromNamespaceAndPath(BFPMain.MOD_ID, "bfp_asset_loader");
    private static final String JOINT_SKELETON_PATH = "skeletons";
    private static final String ANIMATION_SEQUENCE_PATH = "sequences";
    private static final Map<Identifier, MovementFlow> ANIMATION_SEQUENCES = Maps.newHashMap();
    private static final Map<Identifier, RigSystem> JOINT_SKELETONS = Maps.newHashMap();

    public static Map<Identifier, RigSystem> getRigSystems() {
        return JOINT_SKELETONS;
    }

    public static Map<Identifier, MovementFlow> getMovementFlows() {
        return ANIMATION_SEQUENCES;
    }

    public static RigSystem getOrThrowRigSystem(Identifier skeletonId) {
        if (JOINT_SKELETONS.containsKey(skeletonId)) {
            return JOINT_SKELETONS.get(skeletonId);
        }
        throw new IllegalArgumentException("Tried to access joint skeleton from resource location " + skeletonId + ", but it was not found in the loaded data: " + JOINT_SKELETONS.keySet());
    }

    public static MovementFlow getOrThrowMovementFlow(Identifier sequenceId) {
        if (ANIMATION_SEQUENCES.containsKey(sequenceId)) {
            return ANIMATION_SEQUENCES.get(sequenceId);
        }
        throw new IllegalArgumentException("Tried to access animation sequence from resource location " + sequenceId + ", but it was not found in the loaded data.");
    }

    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor bgExecutor, PreparationBarrier barrier, Executor applyExecutor) {
        CompletableFuture<Map<Identifier, RigSystem>> skeletonsFuture = bfp$loadRigSystems(sharedState.resourceManager(), bgExecutor);
        CompletableFuture<Map<Identifier, MovementFlow>> sequencesFuture = bfp$loadMovementFlows(sharedState.resourceManager(), bgExecutor);

        return CompletableFuture.allOf(skeletonsFuture, sequencesFuture)
                .thenCompose(barrier::wait)
                .thenCompose(voided -> CompletableFuture.runAsync(() -> {
                    JOINT_SKELETONS.clear();
                    JOINT_SKELETONS.putAll(skeletonsFuture.join());
                    ANIMATION_SEQUENCES.clear();
                    ANIMATION_SEQUENCES.putAll(sequencesFuture.join());
                    ANIMATION_SEQUENCES.replaceAll((id, seq) -> seq.getBaked());
                    BFP_RES_LOGGER.info("Cleared and replaced BFP resource data.");
                }));
    }

    private static CompletableFuture<Map<Identifier, MovementFlow>> bfp$loadMovementFlows(ResourceManager manager, Executor bgExecutor) {
        return bfp$loadJsonResources(
                manager, bgExecutor, MovementFlow.class, ANIMATION_SEQUENCE_PATH,
                id -> BFP_RES_LOGGER.info("Successfully loaded animation sequence {}", id)
        );
    }

    private static CompletableFuture<Map<Identifier, RigSystem>> bfp$loadRigSystems(ResourceManager manager, Executor bgExecutor) {
        return bfp$loadJsonResources(
                manager, bgExecutor, RigSystem.class, JOINT_SKELETON_PATH,
                id -> BFP_RES_LOGGER.info("Successfully loaded joint skeleton {}", id)
        );
    }

    private static <D> CompletableFuture<Map<Identifier, D>> bfp$loadJsonResources(ResourceManager manager, Executor bgExecutor, Class<D> type, String assetPath, Consumer<Identifier> onLoaded) {
        return CompletableFuture.supplyAsync(() -> {
            Predicate<Identifier> isJsonAsset = id -> id.getPath().endsWith(".json");
            Map<Identifier, Resource> foundAssets = manager.listResources(assetPath, isJsonAsset);

            Map<Identifier, D> parsedAssets = Maps.newHashMap();
            foundAssets.forEach((assetId, resource) -> {
                try {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement jsonData = GsonHelper.fromJson(GsonConfiguration.getInstance(), reader, JsonElement.class);
                        D parsedObject = GsonConfiguration.getInstance().fromJson(jsonData, type);
                        parsedAssets.put(assetId, parsedObject);
                        onLoaded.accept(assetId);
                    } catch (JsonParseException parseEx) {
                        BFP_RES_LOGGER.warn("Skipping loading of JSON asset {} of type {} due to a JSON parsing error:", assetId, type.getSimpleName());
                        BFP_RES_LOGGER.warn("--- {}", parseEx.getMessage());
                    }
                } catch (IOException ioEx) {
                    BFP_RES_LOGGER.error("Encountered error while reading asset {} of type {}:", assetId, type.getSimpleName());
                    BFP_RES_LOGGER.error("--- {}", ioEx.getMessage());
                    throw new RuntimeException(ioEx);
                }
            });
            return parsedAssets;
        }, bgExecutor);
    }
}
