package EsetKalenko.Halcyon.client.shaders;

import EsetKalenko.Halcyon.Halcyon;
import com.cmdpro.databank.mixin.client.BufferSourceMixin;
import com.cmdpro.databank.mixin.client.RenderBuffersMixin;
import com.cmdpro.databank.rendering.RenderHandler;
import com.cmdpro.databank.rendering.ShaderHelper;
import com.cmdpro.databank.shaders.PostShaderInstance;
import EsetKalenko.Halcyon.networking.packet.s2c.AddScannedOre;
import EsetKalenko.Halcyon.registry.HalcyonTags;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EventBusSubscriber(value = Dist.CLIENT, modid = Halcyon.MOD_ID)
public class OrePingShader extends PostShaderInstance {
    private static RenderTarget orePingTarget;
    static MultiBufferSource.BufferSource orePingBufferSource = null;

    @Override
    public ResourceLocation getShaderLocation() {
        return Halcyon.locate("shaders/post/hologram.json");
    }

    @Override
    public void setUniforms(PostPass instance) {
        super.setUniforms(instance);
        instance.getEffect().setSampler("HologramSampler", getOrePingTarget()::getColorTextureId);
    }

    public static RenderTarget getOrePingTarget() {
        if (orePingTarget == null) {
            orePingTarget = new MainTarget(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height);
            orePingTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        }
        return orePingTarget;
    }

    public static MultiBufferSource.BufferSource createOrePingBufferSource() {
        if (orePingBufferSource == null) {
            RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();
            MultiBufferSource.BufferSource source = ShaderHelper.needsBufferWorkaround() ? ((RenderBuffersMixin)renderBuffers).getBufferSource() : renderBuffers.bufferSource();
            BufferSourceMixin mixin = (BufferSourceMixin)source;
            SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers = mixin.getFixedBuffers();
            ByteBufferBuilder sharedBuffer = mixin.getSharedBuffer();
            orePingBufferSource = new OrePingBuffers(sharedBuffer, fixedBuffers);
        }
        return orePingBufferSource;
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
        if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_WEATHER)) {
            if (Minecraft.getInstance().level != null) {
                event.getPoseStack().pushPose();
                event.getPoseStack().translate(-event.getCamera().getPosition().x, -event.getCamera().getPosition().y, -event.getCamera().getPosition().z);
                getOrePingTarget().clear(Minecraft.ON_OSX);
                getOrePingTarget().bindWrite(true);
                for (Map.Entry<BlockPos, Integer> i : new ArrayList<>(AddScannedOre.scanned.entrySet())) {
                    BlockPos pos = i.getKey();
                    int value = i.getValue();
                    if (value <= 0) {
                        BlockState state = Minecraft.getInstance().level.getBlockState(pos);
                        if (state.is(HalcyonTags.Blocks.SCANNABLE_ORES)) {
                            renderBlock(state, pos, event.getPoseStack(), event.getPartialTick(), createOrePingBufferSource());
                        }
                    }
                }
                createOrePingBufferSource().endBatch();
                Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
                event.getPoseStack().popPose();
            }
        }
        if (!ShaderHelper.shouldUseAlternateRendering()) {
            if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_WEATHER)) {
                doEffectRendering();
            }
        }
    }

    private static void doEffectRendering() {

    }

    protected static void renderBlock(BlockState block, BlockPos pos, PoseStack stack, DeltaTracker partialTick, MultiBufferSource.BufferSource bufferSource) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        stack.pushPose();
        stack.translate(pos.getX(), pos.getY(), pos.getZ());
        if (block.getRenderShape() != RenderShape.INVISIBLE) {
            RenderType type = RenderType.SOLID;
            VertexConsumer buffer = bufferSource.getBuffer(type);
            Minecraft.getInstance().getBlockRenderer().renderBatched(block, pos, BLOCK_AND_TINT_GETTER, stack, buffer, false, Minecraft.getInstance().level.random, ModelData.EMPTY, type);
        }
        stack.popPose();
    }

    private static final BlockAndTintGetter BLOCK_AND_TINT_GETTER = new BlockAndTintGetter() {

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            if (Minecraft.getInstance().level == null) {
                return null;
            }
            return Minecraft.getInstance().level.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (Minecraft.getInstance().level == null) {
                return Blocks.AIR.defaultBlockState();
            }
            BlockState state = Minecraft.getInstance().level.getBlockState(pos);
            if (state.is(HalcyonTags.Blocks.SCANNABLE_ORES)) {
                return state;
            }
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            if (Minecraft.getInstance().level == null) {
                return Fluids.EMPTY.defaultFluidState();
            }
            return Minecraft.getInstance().level.getFluidState(pos);
        }

        @Override
        public float getShade(Direction direction, boolean shaded) {
            return 1.0F;
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return null;
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver color) {
            if (Minecraft.getInstance().level == null) {
                return 0xFFFFFFFF;
            }
            return Minecraft.getInstance().level.getBlockTint(pos, color);
        }

        @Override
        public int getBrightness(LightLayer type, BlockPos pos) {
            return 15;
        }

        @Override
        public int getRawBrightness(BlockPos pos, int ambientDarkening) {
            return 15;
        }

        @Override
        public int getLightEmission(BlockPos pos) {
            return 15;
        }

        @Override
        public int getHeight() {
            if (Minecraft.getInstance().level == null) {
                return 0;
            }
            return Minecraft.getInstance().level.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            if (Minecraft.getInstance().level == null) {
                return 0;
            }
            return Minecraft.getInstance().level.getMinBuildHeight();
        }
    };

    private static class OrePingBuffers extends MultiBufferSource.BufferSource {
        protected OrePingBuffers(ByteBufferBuilder fallback, SequencedMap<RenderType, ByteBufferBuilder> layerBuffers) {
            super(fallback, layerBuffers);
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            return super.getBuffer(HologramRenderType.remap(RenderType.SOLID));
        }
    }

    private static class HologramRenderType extends RenderType {
        private static final Map<RenderType, RenderType> remappedTypes = new IdentityHashMap<>();

        private HologramRenderType(RenderType original) {
            super(String.format("%s_%s_hologram", original.toString(), Halcyon.MOD_ID), original.format(), original.mode(), original.bufferSize(), original.affectsCrumbling(), true, () -> {
                original.setupRenderState();

                RenderSystem.disableDepthTest();
            }, () -> {
                RenderSystem.enableDepthTest();

                original.clearRenderState();
            });
        }

        public static RenderType remap(RenderType in) {
            if (in instanceof HologramRenderType) {
                return in;
            } else {
                return remappedTypes.computeIfAbsent(in, HologramRenderType::new);
            }
        }
    }
}
