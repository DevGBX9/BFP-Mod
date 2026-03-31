package org.gbxteam.betterview.sys.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import org.gbxteam.betterview.BFPMain;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return BFPMain.CONFIG.getConfigScreen(FabricLoader.getInstance()::isModLoaded)::apply;
    }
}
