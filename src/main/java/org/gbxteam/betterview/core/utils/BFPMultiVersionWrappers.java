package org.gbxteam.betterview.core.utils;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
//? if mc >= 12111 {
import net.minecraft.world.item.component.KineticWeapon;
//?}


public class BFPMultiVersionWrappers {

    /** Resolves the correct trident animation type across MC versions. */
    public static ItemUseAnimation bfp$resolveTridentAnimation() {
        //? if mc >= 12111 {
        return ItemUseAnimation.TRIDENT;
        //?} else {
        /*return ItemUseAnimation.SPEAR;
        *///?}
    }


    /** Resolves the new spear animation type (1.21.11+). */
    public static ItemUseAnimation bfp$resolveSpearAnimation() {
        //? if mc >= 12111 {
        return ItemUseAnimation.SPEAR;
        //?} else {
        /*throw new RuntimeException("1.21.11 feature attempted to be used in older version");
         *///?}
    }


    public static void bfp$updateSpearDrivers(net.minecraft.client.player.LocalPlayer player, org.gbxteam.betterview.core.context.DriverGetter driverContainer) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(hand);
            //? if mc >= 12111 {
            net.minecraft.world.item.component.KineticWeapon kineticWeapon = itemStack.get(net.minecraft.core.component.DataComponents.KINETIC_WEAPON);
            if (kineticWeapon == null) {
                continue;
            }
            int spearUseDuration = itemStack.getUseDuration(player) - (player.getUseItemRemainingTicks() + 1);
            int delayTicks = kineticWeapon.delayTicks();
            driverContainer.getDriver(org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers.SPEAR_CAN_DISMOUNT).setValue(spearUseDuration < kineticWeapon.dismountConditions().map(net.minecraft.world.item.component.KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
            driverContainer.getDriver(org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers.SPEAR_CAN_KNOCKBACK).setValue(spearUseDuration < kineticWeapon.knockbackConditions().map(net.minecraft.world.item.component.KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
            driverContainer.getDriver(org.gbxteam.betterview.core.engine.controller.entity.firstperson.FirstPersonDrivers.SPEAR_CAN_DAMAGE).setValue(spearUseDuration < kineticWeapon.damageConditions().map(net.minecraft.world.item.component.KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
            //?}

        }
    }
}
