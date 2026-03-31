package org.gbxteam.betterview.sys.hooks.render;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.sys.assets.json.GsonConfiguration;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.*;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Mixin(LayerDefinitions.class)
public class BFPLayerDefinitionsMixin {
    private static final String BFP_NODE_REF = "bfp_layers_mixin";

    @Inject(method = "createRoots", at = @At("RETURN"))
    private static void bfp$trackModelDefinitions(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> bfp_ret) {
        // Disabled active dump to external json, used internally for BFP model generation pipeline.
        if (true) return;
        
        Logger log = BFPMain.DEBUG_LOGGER;
        Map<ModelLayerLocation, LayerDefinition> locMap = bfp_ret.getReturnValue();
        if (locMap != null && !locMap.isEmpty()) {
            log.info("BFP: Intercepted " + locMap.size() + " layer definitions.");
        }
    }
}
