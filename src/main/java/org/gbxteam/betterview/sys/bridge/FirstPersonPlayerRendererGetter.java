package org.gbxteam.betterview.sys.bridge;

import org.gbxteam.betterview.core.visuals.FirstPersonPlayerRenderer;

import java.util.Optional;

public interface FirstPersonPlayerRendererGetter {
    Optional<FirstPersonPlayerRenderer> bfp$getFirstPersonPlayerRenderer();
    
    //? if mc >= 12110 {
    default <M> M bfp$getModel() {
        return null;
    }
    //?}
}
