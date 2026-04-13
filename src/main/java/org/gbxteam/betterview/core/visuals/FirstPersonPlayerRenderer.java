package org.gbxteam.betterview.core.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.gbxteam.betterview.sys.bridge.MatrixModelPart;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.*;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose.FirstPersonGenericItems;
import org.gbxteam.betterview.core.engine.controller.entity.firstperson.handpose.FirstPersonHandPoses;
import org.gbxteam.betterview.core.context.AnimationDataContainer;
import org.gbxteam.betterview.core.skeleton.JointChannel;
import org.gbxteam.betterview.core.logic.states.ModelPartSpacePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//? if mc >= 12111 {
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
//?} else if mc >= 12110 {
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.entity.RenderLayerParent;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
*///?}

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
//? if mc >= 12110 {
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceLocation; // Fallback for some contexts
//?} else {
import net.minecraft.resources.ResourceLocation;
//?}
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
//? if mc >= 12111 {
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
//?} else if mc >= 12110 {
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
//?}

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


import java.util.Objects;

//? if mc >= 12110 {
public class FirstPersonPlayerRenderer implements RenderLayerParent<AvatarRenderState, PlayerModel> {
//?} else {
/*public class FirstPersonPlayerRenderer implements RenderLayerParent<AbstractClientPlayer, PlayerModel> {
*///?}


    /** BFP First Person Rendering Pipeline - GBXTeam */
    private final Minecraft bfp$mc;
    private final EntityRenderDispatcher bfp$renderDispatcher;
    private final ItemRenderer bfp$itemRenderer;
    private final BlockRenderDispatcher bfp$blockRenderer;
    //? if mc >= 12111 {
    private final ItemModelResolver bfp$itemModelResolver;
    //?}

    private final JointAnimatorDispatcher bfp$animDispatcher;

    public static boolean IS_RENDERING_BFP_FIRST_PERSON = false;
    public static boolean SHOULD_FLIP_ITEM_TRANSFORM = false;
    public static InteractionHand CURRENT_ITEM_INTERACTION_HAND = InteractionHand.MAIN_HAND;
    public static float CURRENT_PARTIAL_TICKS = 0;

    public FirstPersonPlayerRenderer(EntityRendererProvider.Context ctx) {
        this.bfp$mc = Minecraft.getInstance();
        this.bfp$renderDispatcher = ctx.getEntityRenderDispatcher();
        this.bfp$itemRenderer = bfp$mc.getItemRenderer();
        this.bfp$blockRenderer = ctx.getBlockRenderDispatcher();
        //? if mc >= 12111 {
        this.bfp$itemModelResolver = ctx.getItemModelResolver();
        //?}

        this.bfp$animDispatcher = JointAnimatorDispatcher.getInstance();
    }

    public void renderBFPArmWithItem(float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, AbstractClientPlayer player, int combinedLight, InteractionHand hand) {
        CURRENT_PARTIAL_TICKS = partialTick;
        JointAnimatorDispatcher dispatcher = JointAnimatorDispatcher.getInstance();

        if (dispatcher.getFirstPersonPlayerDataContainer().isEmpty()) return;
        if (dispatcher.getInterpolatedFirstPersonPlayerPose().isEmpty()) return;

        boolean isLeftHanded = this.bfp$mc.options.mainHand().get() == HumanoidArm.LEFT;
        HumanoidArm armSide = hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (isLeftHanded) {
            armSide = armSide.getOpposite();
        }

        AnimationDataContainer animData = dispatcher.getFirstPersonPlayerDataContainer().get();
        ModelPartSpacePose animPose = dispatcher.getInterpolatedFirstPersonPlayerPose().get();

        JointChannel armChannel = animPose.getJointChannel(FirstPersonJointAnimator.getArmJoint(armSide));
        JointChannel itemChannel = animPose.getJointChannel(FirstPersonJointAnimator.getItemJoint(armSide));

        //? if mc >= 12110 {
        AvatarRenderer<@NotNull AbstractClientPlayer> playerRenderer = this.bfp$renderDispatcher.getPlayerRenderer(player);
        //?} else {
        /*PlayerRenderer playerRenderer = (PlayerRenderer)this.bfp$renderDispatcher.getRenderer(player);
        *///?}

        PlayerModel mdl = playerRenderer.getModel();
        //? if mc >= 12111 {
        ModelPart armPart = mdl.getArm(armSide);
        //?} else if mc >= 12110 {
        /*ModelPart armPart = ((org.gbxteam.betterview.sys.bridge.FirstPersonPlayerRendererGetter)playerRenderer).bfp$getModel();
        *///?} else {
        /*ModelPart armPart = mdl.bfp$getArm(armSide);
        *///?}

        ((MatrixModelPart)(Object)armPart).bfp$setMatrix(armChannel.getTransform());

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        this.bfp$renderArmGeometry(player, mdl, armSide, poseStack, nodeCollector, combinedLight);

        ResourceLocation genericPoseId = animData.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(hand));
        FirstPersonGenericItems.GenericItemPoseDefinition genericDef = FirstPersonGenericItems.getOrThrowFromIdentifier(genericPoseId);
        ResourceLocation handPoseId = animData.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        FirstPersonHandPoses.HandPoseDefinition handDef = FirstPersonHandPoses.getOrThrowFromIdentifier(handPoseId);
        ItemRenderType renderType = handPoseId == FirstPersonHandPoses.GENERIC_ITEM ? genericDef.itemRenderType() : handDef.itemRenderType();

        ItemStack heldItem = bfp$resolveRenderedItem(animData, player, hand);
        this.bfp$renderHeldItem(player, heldItem, poseStack, itemChannel, nodeCollector, combinedLight, armSide, hand, renderType);

        poseStack.popPose();
    }

    public void render(float partialTicks, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int combinedLight) {
        CURRENT_PARTIAL_TICKS = partialTicks;
        JointAnimatorDispatcher dispatcher = JointAnimatorDispatcher.getInstance();

        dispatcher.getFirstPersonPlayerDataContainer().ifPresent(
                animData -> dispatcher.getInterpolatedFirstPersonPlayerPose().ifPresent(
                        animPose -> {
                            JointChannel rArmCh = animPose.getJointChannel(FirstPersonJointAnimator.RIGHT_ARM_JOINT);
                            JointChannel lArmCh = animPose.getJointChannel(FirstPersonJointAnimator.LEFT_ARM_JOINT);
                            JointChannel rItemCh = animPose.getJointChannel(FirstPersonJointAnimator.RIGHT_ITEM_JOINT);
                            JointChannel lItemCh = animPose.getJointChannel(FirstPersonJointAnimator.LEFT_ITEM_JOINT);

                            poseStack.pushPose();
                            poseStack.mulPose(Axis.ZP.rotationDegrees(180));

                            //? if mc >= 12110 {
                            AvatarRenderer<AbstractClientPlayer> playerRenderer = this.bfp$renderDispatcher.getPlayerRenderer(player);
                            //?} else {
                            /*PlayerRenderer playerRenderer = (PlayerRenderer)this.bfp$renderDispatcher.getRenderer(player);
                            *///?}


                            PlayerModel mdl = playerRenderer.getModel();
                            mdl.resetPose();

                            ((MatrixModelPart)(Object) mdl.rightArm).bfp$setMatrix(rArmCh.getTransform());
                            ((MatrixModelPart)(Object) mdl.leftArm).bfp$setMatrix(lArmCh.getTransform());
                            mdl.body.visible = false;

                            this.bfp$renderArmGeometry(player, mdl, HumanoidArm.LEFT, poseStack, nodeCollector, combinedLight);
                            this.bfp$renderArmGeometry(player, mdl, HumanoidArm.RIGHT, poseStack, nodeCollector, combinedLight);

                            boolean isLeftHanded = this.bfp$mc.options.mainHand().get() == HumanoidArm.LEFT;

                            ResourceLocation lGenericId = animData.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(isLeftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            ResourceLocation rGenericId = animData.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(!isLeftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonGenericItems.GenericItemPoseDefinition lGenericDef = FirstPersonGenericItems.getOrThrowFromIdentifier(lGenericId);
                            FirstPersonGenericItems.GenericItemPoseDefinition rGenericDef = FirstPersonGenericItems.getOrThrowFromIdentifier(rGenericId);
                            ResourceLocation lPoseId = animData.getDriverValue(FirstPersonDrivers.getHandPoseDriver(isLeftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            ResourceLocation rPoseId = animData.getDriverValue(FirstPersonDrivers.getHandPoseDriver(!isLeftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonHandPoses.HandPoseDefinition lHandDef = FirstPersonHandPoses.getOrThrowFromIdentifier(lPoseId);
                            FirstPersonHandPoses.HandPoseDefinition rHandDef = FirstPersonHandPoses.getOrThrowFromIdentifier(rPoseId);

                            ItemRenderType lRenderType = lPoseId == FirstPersonHandPoses.GENERIC_ITEM ? lGenericDef.itemRenderType() : lHandDef.itemRenderType();
                            ItemRenderType rRenderType = rPoseId == FirstPersonHandPoses.GENERIC_ITEM ? rGenericDef.itemRenderType() : rHandDef.itemRenderType();

                            ItemStack mainItem = bfp$resolveRenderedItem(animData, player, InteractionHand.MAIN_HAND);
                            ItemStack offItem = bfp$resolveRenderedItem(animData, player, InteractionHand.OFF_HAND);

                            ItemStack rItem = isLeftHanded ? offItem : mainItem;
                            ItemStack lItem = isLeftHanded ? mainItem : offItem;

                            this.bfp$renderHeldItem(player, rItem, poseStack, rItemCh, nodeCollector, combinedLight, HumanoidArm.RIGHT, !isLeftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, rRenderType);
                            this.bfp$renderHeldItem(player, lItem, poseStack, lItemCh, nodeCollector, combinedLight, HumanoidArm.LEFT, isLeftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, lRenderType);

                            poseStack.popPose();
                        }
                )
        );

        this.bfp$mc.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        this.bfp$mc.renderBuffers().bufferSource().endBatch();
    }

    private static ItemStack bfp$resolveRenderedItem(AnimationDataContainer animData, AbstractClientPlayer localPlayer, InteractionHand hand) {
        ItemStack driverItem = animData.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemStack actualItem = localPlayer.getItemInHand(hand);

        if (!ItemStack.isSameItem(actualItem, driverItem)) return driverItem;
        if (ItemStack.isSameItemSameComponents(actualItem, driverItem)) return actualItem;

        for (TypedDataComponent<?> comp : actualItem.getComponents()) {
            if (comp.type() == DataComponents.DAMAGE) continue;
            if (!driverItem.getComponents().has(comp.type())) return driverItem;
            if (!Objects.equals(driverItem.get(comp.type()), comp.value())) return driverItem;
        }
        return actualItem;
    }

    private void bfp$renderArmGeometry(AbstractClientPlayer player, PlayerModel mdl, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector nodeCollector, int combinedLight) {
        //? if mc >= 12111 {
        PlayerSkin skin = player.getSkin();
        boolean slimArms = skin.model() == PlayerModelType.SLIM;
        ResourceLocation skinTex = skin.body().texture();
        //?} else if mc >= 12110 {
        /*PlayerSkin skin = player.getSkin();
        boolean slimArms = skin.model() == PlayerModelType.SLIM;
        ResourceLocation skinTex = skin.texture();
        *///?} else {
        /*boolean slimArms = player.getModelName().equals("slim");
        ResourceLocation skinTex = player.getSkinTexture();
        *///?}

        boolean isLeft = arm == HumanoidArm.LEFT;

        ModelPart sleevePart = isLeft ? mdl.leftSleeve : mdl.rightSleeve;
        PlayerModelPart sleeveFlag = isLeft ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE;

        poseStack.pushPose();
        if (slimArms) {
            poseStack.translate(0.5f / 16f * (isLeft ? 1 : -1), 0, 0);
        }

        sleevePart.visible = player.isModelPartShown(sleeveFlag);
        //? if mc >= 12111 {
        ModelPart armPart = mdl.getArm(arm);
        nodeCollector.submitModelPart(armPart, poseStack, RenderType.entityTranslucent(skinTex), combinedLight, OverlayTexture.NO_OVERLAY, null);
        //?} else if mc >= 12110 {
        /*ModelPart armPart = ((org.gbxteam.betterview.sys.bridge.FirstPersonPlayerRendererGetter)mdl).bfp$getModel();
        nodeCollector.submitModelPart(armPart, poseStack, net.minecraft.client.renderer.RenderType.entityTranslucent(skinTex), combinedLight, OverlayTexture.NO_OVERLAY, null);
        *///?} else {
        /*ModelPart armPart = mdl.bfp$getArm(arm);
        VertexConsumer consumer = nodeCollector.getBuffer(RenderType.entityTranslucent(skinTex));
        armPart.render(poseStack, consumer, combinedLight, OverlayTexture.NO_OVERLAY);
        *///?}


        poseStack.popPose();
    }

    public void bfp$renderHeldItem(LivingEntity entity, ItemStack stack, PoseStack poseStack, JointChannel channel, SubmitNodeCollector nodeCollector, int combinedLight, HumanoidArm side, InteractionHand hand, ItemRenderType renderType) {
        if (stack.isEmpty()) return;

        IS_RENDERING_BFP_FIRST_PERSON = true;
        CURRENT_ITEM_INTERACTION_HAND = hand;

        poseStack.pushPose();
        channel.transformPoseStack(poseStack, 16f);

        if (bfp$isThinBlock(stack)) {
            poseStack.translate(0, 0.25, 0);
        } else if (bfp$isButtonBlock(stack)) {
            poseStack.translate(0, -0.15, 0);
        }

        if (renderType.isMirrored() && side == HumanoidArm.LEFT) {
            SHOULD_FLIP_ITEM_TRANSFORM = true;
        }

        switch (renderType) {
            case MAP -> this.bfp$renderMapInHand(nodeCollector, poseStack, stack, combinedLight);
            case THIRD_PERSON_ITEM, MIRRORED_THIRD_PERSON_ITEM, ON_SHELF -> {
                //? if mc >= 12111 {
                ItemDisplayContext displayCtx = renderType.getItemDisplayContext(side);
                ItemStackRenderState renderState = new ItemStackRenderState();
                this.bfp$itemModelResolver.updateForTopItem(renderState, stack, displayCtx, entity.level(), entity, entity.getId() + displayCtx.ordinal());
                renderState.submit(poseStack, nodeCollector, combinedLight, OverlayTexture.NO_OVERLAY, 0);
                //?} else {
                /*ItemDisplayContext displayCtx = renderType.getItemDisplayContext(side);
                this.bfp$itemRenderer.renderStatic(entity, stack, displayCtx, side == HumanoidArm.LEFT, poseStack, nodeCollector, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayCtx.ordinal());
                *///?}
            }

        }

        SHOULD_FLIP_ITEM_TRANSFORM = false;
        IS_RENDERING_BFP_FIRST_PERSON = false;
        poseStack.popPose();
    }

    /** Checks if given item is a thin/flat block that clips through the hand. */
    private static boolean bfp$isThinBlock(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) return false;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return id.contains("trapdoor") || id.contains("carpet") || id.contains("pressure_plate")
                || id.contains("rail") || id.contains("slab") || id.contains("snow")
                || id.contains("lily_pad") || id.contains("heavy_core") || id.contains("vine")
                || id.contains("daylight_detector") || id.contains("sculk_sensor")
                || id.contains("repeater") || id.contains("comparator") || id.contains("stonecutter");
    }

    /** Checks if given item is a button block. */
    private static boolean bfp$isButtonBlock(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) return false;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("button");
    }

    //? if mc >= 12111 {
    private static final RenderType BFP_MAP_BG = RenderTypes.text(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType BFP_MAP_BG_CHECKER = RenderTypes.text(ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png"));
    private final MapRenderState bfp$mapState = new MapRenderState();
    //?} else if mc >= 12110 {
    /*private static final net.minecraft.client.renderer.RenderType BFP_MAP_BG = net.minecraft.client.renderer.RenderType.getText(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
    private static final net.minecraft.client.renderer.RenderType BFP_MAP_BG_CHECKER = net.minecraft.client.renderer.RenderType.getText(ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png"));
    private final MapRenderState bfp$mapState = new MapRenderState();
    *///?} else {
    /*private static final RenderType BFP_MAP_BG = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType BFP_MAP_BG_CHECKER = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
    *///?}


    private void bfp$renderMapInHand(SubmitNodeCollector nodeCollector, PoseStack poseStack, ItemStack stack, int light) {
        MapId mapId = stack.get(DataComponents.MAP_ID);
        assert this.bfp$mc.level != null;
        MapItemSavedData mapData = MapItem.getSavedData(mapId, this.bfp$mc.level);
        RenderType bgType = mapData == null ? BFP_MAP_BG : BFP_MAP_BG_CHECKER;

        poseStack.scale(-1, 1, -1);
        poseStack.scale(1f/16f, 1f/16f, 1f/16f);
        poseStack.translate(-2, -4, -1);
        poseStack.scale(1f/8f, 1f/8f, 1f/8f);
        poseStack.scale(1/4f, 1/4f, 1/4f);

        nodeCollector.submitCustomGeometry(poseStack, bgType, (pose, vc) -> {
            vc.addVertex(pose, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(light);
            vc.addVertex(pose, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(light);
            vc.addVertex(pose, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(light);
            vc.addVertex(pose, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(light);
        });

        if (mapData != null) {
            MapRenderer mapRenderer = this.bfp$mc.getMapRenderer();
            //? if mc >= 12110 {
            mapRenderer.extractRenderState(mapId, mapData, this.bfp$mapState);
            mapRenderer.render(this.bfp$mapState, poseStack, nodeCollector, false, light);
            //?} else {
            /*mapRenderer.render(poseStack, nodeCollector, mapId, mapData, false, light);
            *///?}
        }

    }

    public void transformCamera(PoseStack poseStack) {
        if (this.bfp$mc.options.getCameraType().isFirstPerson()) {
            this.bfp$animDispatcher.getInterpolatedFirstPersonPlayerPose().ifPresent(animPose -> {
                JointChannel camChannel = animPose.getJointChannel(FirstPersonJointAnimator.CAMERA_JOINT);
                Vector3f camRot = camChannel.getEulerRotationZYX();
                camRot.z *= -1;
                camChannel.rotate(camRot, JointChannel.TransformSpace.LOCAL, JointChannel.TransformType.REPLACE);
                camChannel.translate(camChannel.getTranslation().mul(1, 1, -1), JointChannel.TransformSpace.COMPONENT, JointChannel.TransformType.REPLACE);
                camChannel.transformPoseStack(poseStack, 16f);
            });
        }
    }

    @Override
    public @NotNull PlayerModel getModel() {
        assert bfp$mc.player != null;
        return bfp$renderDispatcher.getPlayerRenderer(bfp$mc.player).getModel();
    }
}
