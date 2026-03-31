package org.gbxteam.betterview.sys.diagnostics;

import org.gbxteam.betterview.BFPMain;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public class BFPDebugScreenEntries {

    /** Registers BFP debug overlay panels into the F3 menu framework. */
    public static void register(BiConsumer<Identifier, DebugScreenEntry> registrar) {
        registrar.accept(Identifier.fromNamespaceAndPath(BFPMain.MOD_ID, "first_person_drivers"), new DebugEntryFirstPersonDrivers());
        registrar.accept(Identifier.fromNamespaceAndPath(BFPMain.MOD_ID, "currently_evaluating_block_entity_animators"), new DebugEntryBlockEntityAnimators());
    }
}
