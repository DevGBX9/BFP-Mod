package org.gbxteam.betterview.core.visuals;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

public enum ItemRenderType {
    THIRD_PERSON_ITEM,
    MIRRORED_THIRD_PERSON_ITEM,
    MAP,
    ON_SHELF;

    /** Checks if this render type requires a mirrored transform. */
    public boolean isMirrored() {
        return this == MIRRORED_THIRD_PERSON_ITEM;
    }

    /** Resolves the correct ItemDisplayContext based on arm side. */
    public ItemDisplayContext getItemDisplayContext(HumanoidArm side) {
        if (this == ON_SHELF) {
            return ItemDisplayContext.ON_SHELF;
        }
        boolean isRightSide = (side == HumanoidArm.RIGHT);
        return isRightSide ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
