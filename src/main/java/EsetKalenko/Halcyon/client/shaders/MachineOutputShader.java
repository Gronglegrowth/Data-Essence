package EsetKalenko.Halcyon.client.shaders;

import EsetKalenko.Halcyon.Halcyon;
import com.cmdpro.databank.mixin.client.BufferSourceMixin;
import com.cmdpro.databank.mixin.client.RenderBuffersMixin;
import com.cmdpro.databank.rendering.RenderHandler;
import com.cmdpro.databank.rendering.ShaderHelper;
import com.cmdpro.databank.shaders.PostShaderInstance;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.SequencedMap;

@EventBusSubscriber(value = Dist.CLIENT, modid = Halcyon.MOD_ID)
public class MachineOutputShader extends PostShaderInstance {
    private static RenderTarget machineOutputTarget;
    static MultiBufferSource.BufferSource machineOutputBufferSource = null;

    @Override
    public ResourceLocation getShaderLocation() {
        return Halcyon.locate("shaders/post/hologram.json");
    }

    @Override
    public void setUniforms(PostPass instance) {
        super.setUniforms(instance);
        instance.getEffect().setSampler("HologramSampler", getMachineOutputTarget()::getColorTextureId);
    }

    public static RenderTarget getMachineOutputTarget() {
        if (machineOutputTarget == null) {
            machineOutputTarget = new MainTarget(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height);
            machineOutputTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
        return machineOutputTarget;
    }

    public static MultiBufferSource.BufferSource createMachineOutputBufferSource() {
        if (machineOutputBufferSource == null) {
            RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();
            MultiBufferSource.BufferSource source = ShaderHelper.needsBufferWorkaround() ? ((RenderBuffersMixin)renderBuffers).getBufferSource() : renderBuffers.bufferSource();
            BufferSourceMixin mixin = (BufferSourceMixin)source;
            SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers = mixin.getFixedBuffers();
            ByteBufferBuilder sharedBuffer = mixin.getSharedBuffer();
            machineOutputBufferSource = MultiBufferSource.immediateWithBuffers(fixedBuffers, sharedBuffer);
        }
        return machineOutputBufferSource;
    }

    @Override
    public void beforeProcess() {
        super.beforeProcess();
        if (ShaderHelper.shouldUseAlternateRendering()) {
            RenderSystem.getModelViewStack().pushMatrix().set(RenderHandler.matrix4f);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderFogStart(RenderHandler.fogStart);
            doEffectRendering();
            FogRenderer.setupNoFog();
            RenderSystem.getModelViewStack().popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!ShaderHelper.shouldUseAlternateRendering()) {
            if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_WEATHER)) {
                doEffectRendering();
            }
        }
    }

    private static void doEffectRendering() {
        RenderSystem.depthMask(true);
        getMachineOutputTarget().clear(Minecraft.ON_OSX);
        getMachineOutputTarget().copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
        getMachineOutputTarget().bindWrite(true);
        createMachineOutputBufferSource().endBatch();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        RenderSystem.depthMask(false);
    }
}
