package org.gbxteam.betterview.core.engine.controller.block_entity;

import org.gbxteam.betterview.core.context.DriverGetter;
import org.gbxteam.betterview.core.context.PoseTickEvaluationContext;
import org.gbxteam.betterview.core.engine.motors.DriverKey;
import org.gbxteam.betterview.core.engine.motors.VariableDriver;
import org.gbxteam.betterview.core.logic.states.LocalSpacePose;
import org.gbxteam.betterview.core.logic.poses.PoseFunction;
import org.gbxteam.betterview.core.logic.poses.SequenceEvaluatorFunction;
import org.gbxteam.betterview.core.logic.poses.SequencePlayerFunction;
import org.gbxteam.betterview.core.logic.cache.CachedPoseContainer;
import org.gbxteam.betterview.core.logic.poses.montage.MontageManager;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateDefinition;
import org.gbxteam.betterview.core.logic.poses.statemachine.PoseManager;
import org.gbxteam.betterview.core.logic.poses.statemachine.StateTransition;
import org.gbxteam.betterview.core.helpers.TimeSpan;
import org.gbxteam.betterview.core.helpers.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface TwoStateContainerJointAnimator<B extends BlockEntity> extends BlockEntityJointAnimator<B> {

    DriverKey<VariableDriver<Float>> CONTAINER_OPENNESS = DriverKey.of("container_openness", () -> VariableDriver.ofFloat(() -> 0f));
    DriverKey<VariableDriver<Boolean>> CONTAINER_IS_OPEN = DriverKey.of("container_is_open", () -> VariableDriver.ofBoolean(() -> false));

    float getOpenProgress(B blockEntity);

    Identifier getOpenMovementFlow();

    Identifier getCloseMovementFlow();

    @Override
    default void extractAnimationData(B blockEntity, DriverGetter dataContainer, MontageManager montageManager) {
        this.extractContainerOpennessData(blockEntity, dataContainer);
    }

    default void extractContainerOpennessData(B blockEntity, DriverGetter dataContainer) {
        dataContainer.getDriver(CONTAINER_OPENNESS).setValue(this.getOpenProgress(blockEntity));

        float currentShulkerBoxOpenness = dataContainer.getDriver(CONTAINER_OPENNESS).getCurrentValue();
        float previousShulkerBoxOpenness = dataContainer.getDriver(CONTAINER_OPENNESS).getPreviousValue();

        boolean shulkerBoxIsOpen = false;
        if (currentShulkerBoxOpenness >= 1f) {
            shulkerBoxIsOpen = true;
        } else if (currentShulkerBoxOpenness != previousShulkerBoxOpenness) {
            if (currentShulkerBoxOpenness > previousShulkerBoxOpenness) {
                shulkerBoxIsOpen = true;
            }
        }
        dataContainer.getDriver(CONTAINER_IS_OPEN).setValue(shulkerBoxIsOpen);
    }

    @Override
    default PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {
        return this.makeContainerOpenClosePoseFunction();
    }

    String CONTAINER_CLOSED_STATE = "closed";
    String CONTAINER_OPENING_STATE = "opening";
    String CONTAINER_OPEN_STATE = "open";
    String CONTAINER_CLOSING_STATE = "closing";

    private static String getInitialContainerState(DriverGetter driverGetter) {
        return driverGetter.getDriverValue(CONTAINER_IS_OPEN) ? CONTAINER_OPEN_STATE : CONTAINER_CLOSED_STATE;
    }

    default PoseFunction<LocalSpacePose> makeContainerOpenClosePoseFunction() {


        PoseFunction<LocalSpacePose> chestClosedPoseFunction = SequenceEvaluatorFunction.builder(this.getOpenMovementFlow()).build();
        PoseFunction<LocalSpacePose> chestOpeningPoseFunction = SequencePlayerFunction.builder(this.getOpenMovementFlow()).build();
        PoseFunction<LocalSpacePose> chestOpenPoseFunction = SequenceEvaluatorFunction.builder(this.getCloseMovementFlow()).build();
        PoseFunction<LocalSpacePose> chestClosingPoseFunction = SequencePlayerFunction.builder(this.getCloseMovementFlow()).build();

        PoseFunction<LocalSpacePose> shulkerBoxStateMachine;
        shulkerBoxStateMachine = PoseManager.builder(TwoStateContainerJointAnimator::getInitialContainerState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(CONTAINER_CLOSED_STATE, chestClosedPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_OPENING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CONTAINER_OPENING_STATE, chestOpeningPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_OPEN_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(CONTAINER_CLOSING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN).negate())
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CONTAINER_OPEN_STATE, chestOpenPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_CLOSING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN).negate())
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CONTAINER_CLOSING_STATE, chestClosingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_CLOSED_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(CONTAINER_OPENING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .build();
        return shulkerBoxStateMachine;
    }
}
