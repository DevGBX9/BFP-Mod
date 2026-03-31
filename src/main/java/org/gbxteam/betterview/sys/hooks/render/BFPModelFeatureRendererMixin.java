package org.gbxteam.betterview.sys.hooks.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.gbxteam.betterview.core.engine.controller.JointAnimatorDispatcher;
import org.gbxteam.betterview.core.context.AnimationDataContainer;
import org.gbxteam.betterview.core.logic.states.Pose;
import org.gbxteam.betterview.core.visuals.BFPWrappedRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ModelFeatureRenderer.class)
public class BFPModelFeatureRendererMixin<S> {
    private static final String BFP_NODE_REF = "bfp_feature_render_mixin";

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V"))
    public void bfp$interceptFeatureSetupAnim(Model<S> instance, S renderState, Operation<Void> original, @Local(argsOnly = true) SubmitNodeStorage.ModelSubmit<S> modelSubmit) {
        boolean isWrappedState = renderState instanceof BFPWrappedRenderState<?>;
        
        if (isWrappedState) {
            BFPWrappedRenderState<?> wrapped = (BFPWrappedRenderState<?>) renderState;
            original.call(instance, wrapped.getInnerValue());
            
            Optional<AnimationDataContainer> dataBox = wrapped.getDataContainer();
            dataBox.ifPresent(container -> {
                float partials = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
                Pose activePose = container.computePose(partials);
                container.setupAnimWithAnimationPose(instance, partials);
            });
        } else {
            original.call(instance, renderState);
        }
    }
}
