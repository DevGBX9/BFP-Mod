package org.gbxteam.betterview.sys.diagnostics;

import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.motors.Driver;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
//? if >= 1.21.11 {
import org.jspecify.annotations.Nullable;
//?} else {
/*import javax.annotation.Nullable;
*///?}

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DebugEntryBlockEntityAnimators implements DebugScreenEntry {
    private static final ResourceLocation BFP_DEBUG_GROUP = ResourceLocation.fromNamespaceAndPath(BFPMain.MOD_ID, "bfp");

    @Override
    public void display(DebugScreenDisplayer output, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        output.addToGroup(BFP_DEBUG_GROUP, "Block Entity Joint Animators currently evaluating:");
        
        var activeAnimators = JointAnimatorDispatcher.getInstance().getCurrentlyEvaluatingBlockEntityJointAnimators();
        activeAnimators.forEach((pos, id) -> {
            output.addToGroup(BFP_DEBUG_GROUP, pos.toShortString() + ": " + id.toString());
        });
    }
}
