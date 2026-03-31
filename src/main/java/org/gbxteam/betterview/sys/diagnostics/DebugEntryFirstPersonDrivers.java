package org.gbxteam.betterview.sys.diagnostics;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.motors.Driver;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DebugEntryFirstPersonDrivers implements DebugScreenEntry {
    private static final Identifier BFP_DEBUG_GROUP = Identifier.fromNamespaceAndPath(BFPMain.MOD_ID, "bfp");

    @Override
    public void display(DebugScreenDisplayer output, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        output.addToGroup(BFP_DEBUG_GROUP, "First Person Drivers");

        JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(container -> {
            List<Map.Entry<DriverKey<?>, Driver<?>>> orderedDrivers = container.getAllDrivers()
                    .entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getIdentifier()))
                    .toList();

            for (Map.Entry<DriverKey<?>, Driver<?>> driverEntry : orderedDrivers) {
                String label = driverEntry.getKey().getIdentifier();
                String value = driverEntry.getValue().getChatFormattedString();
                output.addToGroup(BFP_DEBUG_GROUP, label + ":    " + value);
            }
        });
    }
}
