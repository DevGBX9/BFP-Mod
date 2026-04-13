package org.gbxteam.betterview.core.engine.controller.entity.firstperson;

import com.google.common.collect.Maps;
import org.gbxteam.betterview.BFPMain;
import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.*;
import org.gbxteam.betterview.core.logic.poses.montage.MontageConfiguration;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;
import org.gbxteam.betterview.core.logic.poses.montage.MontageSlotFunction;
import org.gbxteam.betterview.core.helpers.Easing;
import org.gbxteam.betterview.core.utils.BFPMultiVersionWrappers;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPersonAttackAnimations {

    private static final Map<Identifier, AttackAnimationRule> ATTACK_ANIMATION_RULES_BY_IDENTIFIER = Maps.newHashMap();

    public static Identifier register(Identifier identifier, AttackAnimationRule rule) {
        ATTACK_ANIMATION_RULES_BY_IDENTIFIER.put(identifier, rule);
        return identifier;
    }

    public static final Identifier DEFAULT = register(BFPMain.makeIdentifier("default"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE,
            context -> true,
            0)
            .build());
    public static final Identifier EMPTY_HAND_PUNCH = register(BFPMain.makeIdentifier("empty_hand_punch"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_EMPTY_ATTACK_MONTAGE,
            context -> context.item().isEmpty(),
            20)
            .build());
    public static final Identifier TRIDENT = register(BFPMain.makeIdentifier("trident_jab"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_TRIDENT_JAB_MONTAGE,
            context -> context.item().getUseAnimation() == BFPMultiVersionWrappers.bfp$resolveTridentAnimation(),
            30)
            .build());
    public static final Identifier AXE_ACROSS = register(BFPMain.makeIdentifier("axe_across"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_TOOL_ATTACK_AXE_MONTAGE,
            context -> context.item().is(ItemTags.AXES),
            30)
            .setDoesAnimationOffsetOffHand(true)
            .build());
    public static final Identifier MACE_SLAM = register(BFPMain.makeIdentifier("mace_slam"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_MACE_ATTACK_MONTAGE,
            context -> context.item().is(ItemTags.MACE_ENCHANTABLE),
            30)
            .build());
    public static final Identifier SWORD_MAIN = register(BFPMain.makeIdentifier("sword_main"), AttackAnimationRule.builder(
            MontageConfiguration.builder("hand_tool_sword_attack", FirstPersonMovementFlows.HAND_TOOL_SWORD_ATTACK)
                    .playsInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT)
                    .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
                    .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
                    .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(1)).setEasement(Easing.SINE_IN_OUT).build())
                    .build(),
            context -> context.item().is(ItemTags.SWORDS),
            60)
            .setDoesAnimationOffsetOffHand(true)
            .build());
    public static final Identifier SWORD_CRITICAL = register(BFPMain.makeIdentifier("sword_critical"), AttackAnimationRule.builder(
                    MontageConfiguration.builder("hand_tool_sword_attack_critical", FirstPersonMovementFlows.HAND_TOOL_SWORD_ATTACK_CRITICAL)
                            .playsInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT)
                            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
                            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
                            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
                            .build(),
                    context -> (context.item().is(ItemTags.SWORDS) || context.item().is(ItemTags.AXES)) && context.isCriticalAttack(),
                    70)
            .setDoesAnimationOffsetOffHand(true)
            .build());
    //? if mc >= 12111 {
    public static final Identifier SPEAR_JAB = register(BFPMain.makeIdentifier("spear_jab"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_SPEAR_JAB_MONTAGE,
            context -> context.item().getUseAnimation() == BFPMultiVersionWrappers.bfp$resolveSpearAnimation(),
            30
    )
            .build());
    //? }

    public record AttackAnimationRule(
            MontageConfiguration montageToPlay,
            Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
            int evaluationPriority,
            boolean doesAnimationOffsetOffHand
    ) {
        public static Builder builder(
                MontageConfiguration montageToPlay,
                Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
                int evaluationPriority
        ) {
            return new Builder(montageToPlay, shouldChooseAttackAnimation, evaluationPriority);
        }

        public static class Builder {

            private final MontageConfiguration montageToPlay;
            private final Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation;
            private final int evaluationPriority;
            private boolean doesAnimationOffsetOffHand;

            private Builder(
                MontageConfiguration montageToPlay,
                Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
                int evaluationPriority
            ) {
                this.montageToPlay = montageToPlay;
                this.shouldChooseAttackAnimation = shouldChooseAttackAnimation;
                this.evaluationPriority = evaluationPriority;
                this.doesAnimationOffsetOffHand = false;
            }

            public Builder setDoesAnimationOffsetOffHand(boolean doesAnimationOffsetOffHand) {
                this.doesAnimationOffsetOffHand = doesAnimationOffsetOffHand;
                return this;
            }

            public AttackAnimationRule build() {
                return new AttackAnimationRule(
                        this.montageToPlay,
                        this.shouldChooseAttackAnimation,
                        this.evaluationPriority,
                        this.doesAnimationOffsetOffHand
                );
            }
        }
    }

    public record AttackAnimationConditionContext(
            ItemStack item,
            boolean isCriticalAttack,
            boolean isSprintAttack
    ) {

    }

    public static void tryPlayingAttackAnimation(DriverGetter dataContainer, MontageManager montageManager) {

        AttackAnimationConditionContext attackAnimationContext = new AttackAnimationConditionContext(
                dataContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM),
                dataContainer.getDriverValue(FirstPersonDrivers.MEETS_CRITICAL_ATTACK_CONDITIONS),
                dataContainer.getDriverValue(FirstPersonDrivers.MEETS_SPRINT_ATTACK_CONDITIONS)
        );

        Map<Identifier, AttackAnimationRule> sortedAttackAnimationRules = ATTACK_ANIMATION_RULES_BY_IDENTIFIER.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        for (Identifier ruleIdentifier : sortedAttackAnimationRules.keySet()) {
            AttackAnimationRule rule = sortedAttackAnimationRules.get(ruleIdentifier);
            if (rule.shouldChooseAttackAnimation().test(attackAnimationContext)) {
                BFPMain.DEBUG_LOGGER.info("Playing use animation \"{}\"", ruleIdentifier);

                MontageConfiguration montage = rule.montageToPlay();
                if (montage == null) {
                    return;
                }

                playAttackAnimation(montageManager, rule);
                return;
            }
        }
    }

    public static String ATTACK_OFF_HAND_OFFSET_SLOT = "attack_off_hand_offset_slot";

    private static void playAttackAnimation(MontageManager montageManager, AttackAnimationRule rule) {
        MontageConfiguration attackMontage = rule.montageToPlay();
        montageManager.playMontage(attackMontage);

        if (rule.doesAnimationOffsetOffHand()) {
            MontageConfiguration offHandOffsetMontage;
            offHandOffsetMontage = attackMontage.makeBuilderCopy(
                    attackMontage.identifier() + "_offhand_offset",
                            attackMontage.animationSequence())
                    .playsInSlot(ATTACK_OFF_HAND_OFFSET_SLOT)
                    .build();
            montageManager.playMontage(offHandOffsetMontage);
        }
    }

    public static void cancelAttackOffHandOffset(PoseTickEvaluationContext context) {
        Transition outTransition = Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.CUBIC_OUT).build();
        context.montageManager().interruptMontagesInSlot(ATTACK_OFF_HAND_OFFSET_SLOT, outTransition);
    }

    public static PoseFunction<LocalSpacePose> constructWithOffsetOffHandAttack(PoseFunction<LocalSpacePose> inputOffHandPose) {
        PoseFunction<LocalSpacePose> offsetBasePose;
        offsetBasePose = SequenceEvaluatorFunction.builder(FirstPersonMovementFlows.HAND_TOOL_SWORD_ATTACK).build();

        PoseFunction<LocalSpacePose> offsetAdditivePose;
        offsetAdditivePose = MontageSlotFunction.of(offsetBasePose, ATTACK_OFF_HAND_OFFSET_SLOT);
        offsetAdditivePose = MakeDynamicAdditiveFunction.of(offsetAdditivePose, offsetBasePose);
        offsetAdditivePose = BlendPosesFunction.builder(EmptyPoseFunction.of(false))
                .addBlendInput(offsetAdditivePose, context -> 1f, FirstPersonJointAnimator.LEFT_SIDE_MASK)
                .build();

        PoseFunction<LocalSpacePose> pose;
        pose = ApplyAdditiveFunction.of(inputOffHandPose, offsetAdditivePose);
        return pose;
    }
}
