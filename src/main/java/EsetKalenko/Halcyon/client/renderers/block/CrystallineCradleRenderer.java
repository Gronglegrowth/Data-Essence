package EsetKalenko.Halcyon.client.renderers.block;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.block.production.CrystallineCradleBlockEntity;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import com.cmdpro.databank.model.DatabankModel;
import com.cmdpro.databank.model.DatabankModels;
import com.cmdpro.databank.model.blockentity.DatabankBlockEntityModel;
import com.cmdpro.databank.model.blockentity.DatabankBlockEntityRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class CrystallineCradleRenderer extends DatabankBlockEntityRenderer<CrystallineCradleBlockEntity> {

    public CrystallineCradleRenderer(BlockEntityRendererProvider.Context rendererProvider) {
        super(new Model());
    }

    @Override
    public void render(CrystallineCradleBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float u0 = 0;//sprite.getU0();
        float u1 = 1;//sprite.getU1();
        float v0 = 0;//sprite.getV0();
        float v1 = 1;//sprite.getV1();

        float range = blockEntity.getVisualDestroyRange(partialTick);
        float alpha = 1f-Math.clamp((((float)blockEntity.visualDestroyTicks-(float)blockEntity.visualMinTicks)+partialTick)/((float)blockEntity.maxDestroyTicks-(float)blockEntity.visualMinTicks), 0f, 1f);

        Color color = new Color(1f, 1f, 1f, alpha);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.25, 0.5);
        RenderSystem.enableBlend();
        renderQuad(bufferSource.getBuffer(RenderType.entityTranslucent(Halcyon.locate("textures/vfx/damage_circle.png"))), poseStack, -1*range, 0, -1*range, 1*range, 0, 1*range, 0, 0, 1, 1, packedLight, packedOverlay, color.getRGB());
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static class Model extends DatabankBlockEntityModel<CrystallineCradleBlockEntity> {
        public DatabankModel model;

        @Override
        public ResourceLocation getTextureLocation() {
            return Halcyon.locate("textures/block/crystalline_cradle.png");
        }

        @Override
        public void setupModelPose(CrystallineCradleBlockEntity cradle, float partialTick) {
            cradle.animState.updateAnimDefinitions(getModel());
            if ( cradle.interval <= 10
                    && !cradle.animState.isCurrentAnim("hit_bell")) {
                cradle.animState.setAnim("hit_bell");
            }
            animate(cradle.animState);
        }

        @Override
        public DatabankModel getModel() {
            if (model == null) {
                model = DatabankModels.models.get(Halcyon.locate("crystalline_cradle"));
            }
            return model;
        }
    }

    private static void drawVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int packedOverlay, int color) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(packedLight)
                .setOverlay(packedOverlay)
                .setNormal(0, 1, 0);
    }

    private static void renderQuad(VertexConsumer builder, PoseStack poseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, int packedLight, int packedOverlay, int color) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, packedOverlay, color);
        drawVertex(builder, poseStack, x0, y1, z1, u0, v1, packedLight, packedOverlay, color);
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, packedOverlay, color);
        drawVertex(builder, poseStack, x1, y0, z0, u1, v0, packedLight, packedOverlay, color);
    }
}
