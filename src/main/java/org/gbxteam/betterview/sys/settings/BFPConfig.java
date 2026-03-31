package org.gbxteam.betterview.sys.settings;

import com.google.common.collect.Maps;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimator;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class BFPConfig {

    private static final Logger LOGGER = LogManager.getLogger("BFP/Config");

    //private static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve(BFPMain.MOD_ID + ".json");
    private static final Path CONFIG_FILE_PATH = Path.of("config").resolve( BFPMain.MOD_ID + ".json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    private Data configData;

    public BFPConfig() {
        this.configData = new Data();
    }

    public Data data() {
        return configData;
    }

    public void load() {
        if (Files.exists(CONFIG_FILE_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_FILE_PATH.toFile())) {
                this.configData = GSON.fromJson(reader, BFPConfig.Data.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load bfp config file", e);
            }
        } else {
            configData = new BFPConfig.Data();
        }
        save();

    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE_PATH)) {
            writer.write(GSON.toJson(this.configData));
//            BFPMain.LOGGER.info("Saved config to path {}", CONFIG_FILE_PATH);
        } catch (Exception e) {
            LOGGER.error("Failed to write config to path {}", CONFIG_FILE_PATH.toAbsolutePath());
        }
        JointAnimatorDispatcher.getInstance().reInitializeData();
    }


    public static class Data {

        public final FirstPersonPlayer firstPersonPlayer = new FirstPersonPlayer();
        public final BlockEntities blockEntities = new BlockEntities();
        public final CombatAndWeapons combatAndWeapons = new CombatAndWeapons();

        public static class FirstPersonPlayer {

            public boolean enableRenderer = true;

            public boolean enableCameraRotationDamping = true;
            public float cameraRotationStiffnessFactor = 0.3f;
            public float cameraRotationDampingFactor = 0.65f;

            public float cameraShakeMasterIntensity = 1f;
            public float cameraShakeMovementIntensity = 1f;
            public float cameraShakeItemInteractionIntensity = 0f;

            public float miningAnimationSpeedMultiplier = 1f;
            public float mapMovementAnimationIntensity = 0.2f;
            public float runningArmSwingIntensity = 1f;

            public float armOffsetX = 0f;
            public float armOffsetY = 0f;
            public float armOffsetZ = 0f;

            // BFP Camera Overhaul Physics
            public boolean enableCameraOverhaul = true;
            public double coStrafingRollFactor = 10.0;
            public double coForwardVelocityPitchFactor = 7.0;
            public double coVerticalVelocityPitchFactor = 2.5;
            public double coHorizontalVelocitySmoothing = 1.0;
            public double coVerticalVelocitySmoothing = 1.0;
            public double coTurningRollIntensity = 1.25;
            public double coTurningRollSmoothing = 1.0;

        }

        public static class CombatAndWeapons {
            public boolean cinematicCombatMode = false;
            public boolean dynamicWeaponWeight = true;
            public float combatSwayMultiplier = 1.0f;
            public float heavyWeaponSwayPenalty = 1.5f;
            public float lightWeaponAgilityBonus = 0.8f;
        }

        public static class BlockEntities {

            public JointAnimator.PoseCalculationFrequency poseCalculationFrequency = JointAnimator.PoseCalculationFrequency.CALCULATE_EVERY_FRAME;
            public int evaluationDistance = 32;
            public Map<String, Boolean> enabledBlockEntities = Maps.newHashMap();

        }
    }

    public Function<Screen, Screen> getConfigScreen(Predicate<String> ifModLoaded) {

        if (ifModLoaded.test("yet_another_config_lib_v3")) {
            return BFPConfigScreen::createConfigScreen;
        } else {
            return parent -> new AlertScreen(
                    () -> Minecraft.getInstance().setScreen(parent),
                    Component.translatable("bfp.config.yacl_not_found.header"),
                    Component.translatable("bfp.config.yacl_not_found.description"),
                    Component.translatable("bfp.config.yacl_not_found.close"),
                    true
            );
        }
    }
}
