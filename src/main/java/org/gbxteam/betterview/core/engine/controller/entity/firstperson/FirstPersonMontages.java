package org.gbxteam.betterview.core.engine.controller.entity.firstperson;

import org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose.FirstPersonGenericItems;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose.FirstPersonHandPoses;
import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.logic.poses.SequenceReferencePoint;
import org.gbxteam.betterview.core.logic.poses.montage.MontageConfiguration;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;


public class FirstPersonMontages {

    /** BFP Montage Configuration Registry - GBXTeam */
    public static final String MAIN_HAND_ATTACK_SLOT = "main_hand_attack";
    public static final String OFF_HAND_ATTACK_SLOT = "off_hand_attack";

    public static final String SHIELD_BLOCK_SLOT = "shield_block";
    public static final String SPEAR_CHARGE_SLOT = "spear_charge";

    public static String getAttackSlot(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? MAIN_HAND_ATTACK_SLOT : OFF_HAND_ATTACK_SLOT;
    }

    public static Identifier getCurrentHandPose(DriverGetter driverContainer, InteractionHand hand) {
        Identifier handPose = driverContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        return FirstPersonHandPoses.getOrThrowFromIdentifier(handPose).currentBasePoseSupplier().apply(driverContainer, hand);
    }

    public static Identifier getCurrentMainHandPose(DriverGetter driverContainer) {
        return getCurrentHandPose(driverContainer, InteractionHand.MAIN_HAND);
    }

    public static Identifier getCurrentOffHandPose(DriverGetter driverContainer) {
        return getCurrentHandPose(driverContainer, InteractionHand.OFF_HAND);
    }



    public static final MontageConfiguration HAND_EMPTY_ATTACK_MONTAGE = MontageConfiguration.builder("hand_empty_attack", FirstPersonMovementFlows.HAND_EMPTY_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
            .build();
    public static final MontageConfiguration HAND_TOOL_ATTACK_PICKAXE_MONTAGE = MontageConfiguration.builder("hand_tool_attack_pickaxe", FirstPersonMovementFlows.HAND_TOOL_PICKAXE_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(40)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(FirstPersonMontages::getCurrentMainHandPose, SequenceReferencePoint.END)
            .build();
    public static final MontageConfiguration HAND_TOOL_ATTACK_AXE_MONTAGE = MontageConfiguration.builder("hand_tool_attack_axe", FirstPersonMovementFlows.HAND_TOOL_AXE_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(15)).setEasement(Easing.SINE_IN_OUT).build())
            .build();
    public static final MontageConfiguration HAND_MACE_ATTACK_MONTAGE = MontageConfiguration.builder("hand_mace_attack", FirstPersonMovementFlows.HAND_MACE_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
            .build();
    public static final MontageConfiguration HAND_TRIDENT_JAB_MONTAGE = MontageConfiguration.builder("hand_trident_jab", FirstPersonMovementFlows.HAND_TRIDENT_JAB)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
            .build();
    public static final MontageConfiguration HAND_SPEAR_JAB_MONTAGE = MontageConfiguration.builder("hand_spear_jab", FirstPersonMovementFlows.HAND_SPEAR_JAB)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(20)).setEasement(Easing.SINE_IN_OUT).build())
            .build();


    public static final MontageConfiguration USE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("hand_use_main_hand", FirstPersonMovementFlows.HAND_TOOL_USE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(16)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(FirstPersonMontages::getCurrentMainHandPose, SequenceReferencePoint.END)
            .build();

    public static final MontageConfiguration USE_OFF_HAND_MONTAGE = USE_MAIN_HAND_MONTAGE.makeBuilderCopy("hand_use_off_hand", USE_MAIN_HAND_MONTAGE.animationSequence())
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .makeAdditive(FirstPersonMontages::getCurrentOffHandPose, SequenceReferencePoint.END)
            .build();

    public static final MontageConfiguration PLACE_BLOCK_MAIN_HAND_MONTAGE = MontageConfiguration.builder("place_block_main_hand", FirstPersonMovementFlows.HAND_GENERIC_ITEM_USE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(16)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(FirstPersonMontages::getCurrentMainHandPose, SequenceReferencePoint.END)
            .build();

    public static final MontageConfiguration PLACE_BLOCK_OFF_HAND_MONTAGE = USE_MAIN_HAND_MONTAGE.makeBuilderCopy("place_block_off_hand", PLACE_BLOCK_MAIN_HAND_MONTAGE.animationSequence())
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .makeAdditive(FirstPersonMontages::getCurrentOffHandPose, SequenceReferencePoint.END)
            .build();




    public static final MontageConfiguration SHIELD_BLOCK_IMPACT_MONTAGE = MontageConfiguration.builder("shield_block_impact", FirstPersonMovementFlows.HAND_SHIELD_IMPACT)
            .playsInSlot(SHIELD_BLOCK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration AXE_SCRAPE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("axe_scrape_main_hand", FirstPersonMovementFlows.HAND_TOOL_AXE_SCRAPE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration AXE_SCRAPE_OFF_HAND_MONTAGE = AXE_SCRAPE_MAIN_HAND_MONTAGE.makeBuilderCopy("axe_scrape_off_hand", FirstPersonMovementFlows.HAND_TOOL_AXE_SCRAPE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static final MontageConfiguration HOE_TILL_MAIN_HAND_MONTAGE = MontageConfiguration.builder("hoe_till_main_hand", FirstPersonMovementFlows.HAND_TOOL_HOE_TILL)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration HOE_TILL_OFF_HAND_MONTAGE = HOE_TILL_MAIN_HAND_MONTAGE.makeBuilderCopy("hoe_till_off_hand", FirstPersonMovementFlows.HAND_TOOL_HOE_TILL)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static final MontageConfiguration SHOVEL_FLATTEN_MAIN_HAND_MONTAGE = MontageConfiguration.builder("shovel_flatten_main_hand", FirstPersonMovementFlows.HAND_TOOL_SHOVEL_FLATTEN)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration SHOVEL_FLATTEN_OFF_HAND_MONTAGE = SHOVEL_FLATTEN_MAIN_HAND_MONTAGE.makeBuilderCopy("shoven_flatten_off_hand", FirstPersonMovementFlows.HAND_TOOL_SHOVEL_FLATTEN)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static final MontageConfiguration SHEARS_USE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("shears_use_main_hand", FirstPersonMovementFlows.HAND_GENERIC_ITEM_SHEARS_USE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration SHEARS_USE_OFF_HAND_MONTAGE = SHEARS_USE_MAIN_HAND_MONTAGE.makeBuilderCopy("shears_use_off_hand", FirstPersonMovementFlows.HAND_GENERIC_ITEM_SHEARS_USE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();


    public static final MontageConfiguration BUCKET_COLLECT_MAIN_HAND_MONTAGE = MontageConfiguration.builder("bucket_collect_main_hand", FirstPersonMovementFlows.HAND_GENERIC_ITEM_BUCKET_COLLECT)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(FirstPersonMontages::getCurrentMainHandPose, SequenceReferencePoint.END)
            .build();

    public static final MontageConfiguration BUCKET_COLLECT_OFF_HAND_MONTAGE = BUCKET_COLLECT_MAIN_HAND_MONTAGE.makeBuilderCopy("bucket_collect_off_hand", FirstPersonMovementFlows.HAND_GENERIC_ITEM_BUCKET_COLLECT)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .makeAdditive(FirstPersonMontages::getCurrentOffHandPose, SequenceReferencePoint.END)
            .build();

    public static final MontageConfiguration BUCKET_EMPTY_MAIN_HAND_MONTAGE = MontageConfiguration.builder("bucket_empty_main_hand", FirstPersonMovementFlows.HAND_GENERIC_ITEM_BUCKET_EMPTY)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(FirstPersonMontages::getCurrentMainHandPose, SequenceReferencePoint.END)
            .build();

    public static final MontageConfiguration BUCKET_EMPTY_OFF_HAND_MONTAGE = BUCKET_EMPTY_MAIN_HAND_MONTAGE.makeBuilderCopy("bucket_empty_off_hand", FirstPersonMovementFlows.HAND_GENERIC_ITEM_BUCKET_EMPTY)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .makeAdditive(FirstPersonMontages::getCurrentOffHandPose, SequenceReferencePoint.END)
            .build();

    public static final MontageConfiguration CROSSBOW_FIRE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("crossbow_fire_main_hand", FirstPersonMovementFlows.HAND_CROSSBOW_FIRE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(20)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration CROSSBOW_FIRE_OFF_HAND_MONTAGE = CROSSBOW_FIRE_MAIN_HAND_MONTAGE.makeBuilderCopy("crossbow_fire_off_hand", FirstPersonMovementFlows.HAND_CROSSBOW_FIRE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();



    public static final MontageConfiguration SPEAR_CHARGE_IMPACT_MONTAGE = MontageConfiguration.builder("spear_charge_impact", FirstPersonMovementFlows.HAND_SPEAR_CHARGE_IMPACT)
            .playsInSlot(SPEAR_CHARGE_SLOT)
            .setTransitionIn(Transition.INSTANT)
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
            .build();



    public static MontageConfiguration getCrossbowFireMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> CROSSBOW_FIRE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> CROSSBOW_FIRE_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getUseAnimationMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> USE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> USE_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getPlaceBlockAnimationMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> PLACE_BLOCK_MAIN_HAND_MONTAGE;
            case OFF_HAND -> PLACE_BLOCK_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getAxeScrapeMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> AXE_SCRAPE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> AXE_SCRAPE_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getHoeTillMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> HOE_TILL_MAIN_HAND_MONTAGE;
            case OFF_HAND -> HOE_TILL_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getShovelFlattenMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> SHOVEL_FLATTEN_MAIN_HAND_MONTAGE;
            case OFF_HAND -> SHOVEL_FLATTEN_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getShearsUseMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> SHEARS_USE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> SHEARS_USE_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getBucketCollectMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> BUCKET_COLLECT_MAIN_HAND_MONTAGE;
            case OFF_HAND -> BUCKET_COLLECT_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getBucketEmptyMontage(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> BUCKET_EMPTY_MAIN_HAND_MONTAGE;
            case OFF_HAND -> BUCKET_EMPTY_OFF_HAND_MONTAGE;
        };
    }

    public static void playAttackMontage(DriverGetter driverContainer, MontageManager montageManager) {
        // Reserved for future attack montage dispatch
    }
}