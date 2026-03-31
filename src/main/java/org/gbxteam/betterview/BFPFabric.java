package org.gbxteam.betterview;

import org.gbxteam.betterview.sys.diagnostics.DebugEntryFirstPersonDrivers;
import org.gbxteam.betterview.sys.diagnostics.BFPDebugScreenEntries;
import org.gbxteam.betterview.sys.assets.BFPResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public class BFPFabric implements ClientModInitializer {

    /** BFP Fabric Client Entrypoint - GBXTeam */

    @Override
    public void onInitializeClient() {
        BFPMain.initialize();
        bfp$initResourcePipeline();
        bfp$initDebugOverlays();
    }

    private static void bfp$initResourcePipeline() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(BFPResources.RELOADER_IDENTIFIER, new BFPResources());
    }

    private static void bfp$initDebugOverlays() {
        BFPDebugScreenEntries.register(DebugScreenEntries::register);
    }
}