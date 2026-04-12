package org.gbxteam.betterview.sys.hooks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.gbxteam.betterview.sys.bridge.FirstPersonSingleBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
import org.gbxteam.betterview.core.utils.BFPRenderTypeWrappers; // Assuming we might need a wrapper or just use standard
*///?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;


@Mixin(BlockRenderDispatcher.class)
public abstract class BFPBlockRenderDispatcherMixin implements FirstPersonSingleBlockRenderer {
    private static final String BFP_NODE_REF = "bfp_blockrend_mixin";

    @Shadow public abstract BlockStateModel getBlockModel(BlockState arg);
    @Shadow @Final private BlockColors blockColors;

    @Unique
    public void bfp$submitSingleBlockWithEmission(BlockState blockState, PoseStack poseStack, SubmitNodeCollector nodeCollector, int combinedLight) {
        if (blockState.getRenderShape() == RenderShape.INVISIBLE) return;
        
        int bfpRenderLight = LightTexture.lightCoordsWithEmission(combinedLight, blockState.getLightEmission());
        BlockStateModel model = this.getBlockModel(blockState);
        
        int colorTint = this.blockColors.getColor(blockState, null, null, 0);
        float redFilter = (float)(colorTint >> 16 & 0xFF) / 255.0f;
        float greenFilter = (float)(colorTint >> 8 & 0xFF) / 255.0f;
        float blueFilter = (float)(colorTint & 0xFF) / 255.0f;
        
        for (BlockModelPart part : model.collectParts(RandomSource.create(42L))) {
            for (Direction dir : Direction.values()) {
                for (BakedQuad quad : part.getQuads(dir)) {
                    this.bfp$renderBakedQuad(quad, poseStack, nodeCollector, redFilter, greenFilter, blueFilter, bfpRenderLight, blockState);
                }
            }
            for (BakedQuad quad : part.getQuads(null)) {
                this.bfp$renderBakedQuad(quad, poseStack, nodeCollector, redFilter, greenFilter, blueFilter, bfpRenderLight, blockState);
            }
        }
        Minecraft.getInstance().getModelManager().specialBlockModelRenderer().renderByBlock(blockState.getBlock(), ItemDisplayContext.NONE, poseStack, nodeCollector, bfpRenderLight, OverlayTexture.NO_OVERLAY, 0);
    }

    @Unique
    private void bfp$renderBakedQuad(BakedQuad bakedQuad, PoseStack poseStack, SubmitNodeCollector nodeCollector, float r, float g, float b, int combinedLight, BlockState blockState) {
        float bfpR = bakedQuad.isTinted() ? Mth.clamp(r, 0.0f, 1.0f) : 1.0f;
        float bfpG = bakedQuad.isTinted() ? Mth.clamp(g, 0.0f, 1.0f) : 1.0f;
        float bfpB = bakedQuad.isTinted() ? Mth.clamp(b, 0.0f, 1.0f) : 1.0f;

        //? if >= 1.21.10 {
        RenderType activeLayer = (bakedQuad.shade() && blockState.getLightEmission() == 0) ? ItemBlockRenderTypes.getRenderType(blockState) : RenderTypes.cutoutMovingBlock();
        
        nodeCollector.submitCustomGeometry(poseStack, activeLayer, (matricesEntry, consumer) -> consumer.putBulkData(
                matricesEntry, bakedQuad, new float[]{1, 1, 1, 1}, bfpR, bfpG, bfpB, 1.0f, new int[]{combinedLight, combinedLight, combinedLight, combinedLight}, OverlayTexture.NO_OVERLAY
        ));
        //?} else {
        /*RenderType activeLayer = (bakedQuad.isShade() && blockState.getLightEmission() == 0) ? ItemBlockRenderTypes.getRenderLayer(blockState, true) : RenderType.cutoutMipped();
        nodeCollector.submitCustomGeometry(poseStack, activeLayer, (matricesEntry, consumer) -> consumer.putBulkData(
                matricesEntry, bakedQuad, bfpR, bfpG, bfpB, combinedLight, OverlayTexture.NO_OVERLAY
        ));
        *///?}
    }
    }
}
