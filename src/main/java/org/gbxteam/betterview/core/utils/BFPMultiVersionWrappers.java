package org.gbxteam.betterview.core.utils;

import net.minecraft.world.item.ItemUseAnimation;

public class BFPMultiVersionWrappers {

    /** Resolves the correct trident animation type across MC versions. */
    public static ItemUseAnimation bfp$resolveTridentAnimation() {
        //? if >= 1.21.11 {
        return ItemUseAnimation.TRIDENT;
        //?} else {
        /*return ItemUseAnimation.SPEAR;
        *///?}
    }

    /** Resolves the new spear animation type (1.21.11+). */
    public static ItemUseAnimation bfp$resolveSpearAnimation() {
        //? if >= 1.21.11 {
        return ItemUseAnimation.SPEAR;
        //?} else {
        /*throw new RuntimeException("1.21.11 feature attempted to be used in older version");
         *///?}
    }
}
