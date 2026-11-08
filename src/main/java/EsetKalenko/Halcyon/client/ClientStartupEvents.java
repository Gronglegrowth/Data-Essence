package EsetKalenko.Halcyon.client;

import com.cmdpro.databank.rendering.ColorUtil;
import com.cmdpro.databank.shaders.PostShaderInstance;
import com.cmdpro.databank.shaders.PostShaderManager;
import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.api.item.ItemDecorators;
import EsetKalenko.Halcyon.api.item.ItemEssenceContainer;
import EsetKalenko.Halcyon.api.util.client.AnimatedBlockItemUtil;
import EsetKalenko.Halcyon.client.dimension.SanctuarySpecialEffects;
import EsetKalenko.Halcyon.block.control.EssenceReaderBlockEntity;
import EsetKalenko.Halcyon.client.gui.PingsGuiLayer;
import EsetKalenko.Halcyon.client.renderers.entity.*;
import EsetKalenko.Halcyon.client.shaders.MachineOutputShader;
import EsetKalenko.Halcyon.client.shaders.OrePingShader;
import EsetKalenko.Halcyon.integration.DataNEssenceIntegration;
import EsetKalenko.Halcyon.integration.mekanism.ChemicalNodeRenderer;
import EsetKalenko.Halcyon.client.particle.*;
import EsetKalenko.Halcyon.item.equipment.GrapplingHook;
import EsetKalenko.Halcyon.particle.EnergyParticle;
import EsetKalenko.Halcyon.registry.*;
import EsetKalenko.Halcyon.client.renderers.block.*;
import EsetKalenko.Halcyon.client.renderers.layer.HornsLayer;
import EsetKalenko.Halcyon.client.renderers.layer.TailLayer;
import EsetKalenko.Halcyon.client.renderers.layer.WingsLayer;
import EsetKalenko.Halcyon.screen.*;
import EsetKalenko.Halcyon.client.shaders.GenderEuphoriaShader;
import EsetKalenko.Halcyon.client.shaders.ProgressionShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.joml.SimplexNoise;

import java.awt.*;

import static EsetKalenko.Halcyon.integration.DataNEssenceIntegration.hasOpalescence;

@EventBusSubscriber(value = Dist.CLIENT, modid = Halcyon.MOD_ID)
public class ClientStartupEvents {
    public static PostShaderInstance progressionShader;
    public static PostShaderInstance genderEuphoriaShader;
    public static PostShaderInstance machineOutputShader;
    public static PostShaderInstance orePingShader;

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Halcyon.locate("pings"), new PingsGuiLayer());
    }

    @SubscribeEvent
    public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(Halcyon.locate("sanctuary"), new SanctuarySpecialEffects());
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityRegistry.FABRICATOR.get(), FabricatorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.LUNARIUM.get(), LunariumRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ESSENCE_POINT.get(), EssencePointRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.LUNAR_ESSENCE_POINT.get(), LunarEssencePointRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.NATURAL_ESSENCE_POINT.get(), NaturalEssencePointRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.EXOTIC_ESSENCE_POINT.get(), ExoticEssencePointRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ITEM_POINT.get(), ItemPointRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_POINT.get(), FluidPointRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.INFUSER.get(), InfuserRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.CHARGER.get(), ChargerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.LASER_EMITTER.get(), LaserEmitterRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.AUTO_FABRICATOR.get(), AutoFabricatorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ESSENCE_BATTERY.get(), EssenceBatteryRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ENTROPIC_PROCESSOR.get(), EntropicProcessorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.FLUID_MIXER.get(), FluidMixerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.AREKKO.get(), ArekkoRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ENTICING_LURE.get(), EnticingLureRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.METAL_SHAPER.get(), MetalShaperRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.INDUSTRIAL_PLANT_SIPHON.get(), IndustrialPlantSiphonRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.CRYOCHAMBER.get(), CryochamberRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.CHEMICAL_NODE.get(), ChemicalNodeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.DRYING_TABLE.get(), DryingTableRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ESSENCE_BRIDGE.get(), EssenceBridgeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ENDER_PEARL_CAPTURE.get(), EnderPearlCaptureRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ENDER_PEARL_RELAY.get(), EnderPearlRelayRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ENDER_PEARL_DESTINATION.get(), EnderPearlDestinationRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.STRUCTURE_PROTECTOR.get(), StructureProtectorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.SURVEY_TUNNELER.get(), SurveyTunnelerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.ESSENCE_DERIVATION_SPIKE.get(), EssenceDerivationSpikeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.LUNAR_ESSENCE_BATTERY.get(), LunarEssenceBatteryRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.CRYSTALLINE_CRADLE.get(), CrystallineCradleRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.RF_NODE.get(), RFNodeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.HARVESTER.get(), SimpleHarvesterRenderer::new);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model i : event.getSkins()) {
            LivingEntityRenderer skin = event.getSkin(i);
            skin.addLayer(new WingsLayer(skin, event.getEntityModels()));
            skin.addLayer(new HornsLayer(skin, event.getEntityModels()));
            skin.addLayer(new TailLayer(skin, event.getEntityModels()));
        }
    }

    @SubscribeEvent
    public static void doSetup(FMLClientSetupEvent event) {

        event.enqueueWork(() -> {
            EntityRenderers.register(EntityRegistry.ESSENCE_BOMB.get(), ThrownTrailItemRenderer::new);
            EntityRenderers.register(EntityRegistry.LUNAR_ESSENCE_BOMB.get(), ThrownTrailItemRenderer::new);
            EntityRenderers.register(EntityRegistry.NATURAL_ESSENCE_BOMB.get(), ThrownTrailItemRenderer::new);
            EntityRenderers.register(EntityRegistry.EXOTIC_ESSENCE_BOMB.get(), ThrownTrailItemRenderer::new);
            EntityRenderers.register(EntityRegistry.BLACK_HOLE.get(), EmptyEntityRenderer::new);
            EntityRenderers.register(EntityRegistry.ANCIENT_SENTINEL.get(), AncientSentinelRenderer::new);
            EntityRenderers.register(EntityRegistry.ANCIENT_SENTINEL_PROJECTILE.get(), EmptyEntityRenderer::new);
            EntityRenderers.register(EntityRegistry.ESSENCE_SLASH_PROJECTILE.get(), EssenceSlashRenderer::new);
            EntityRenderers.register(EntityRegistry.ANCIENT_SENTINEL_LASER.get(), AncientSentinelLaserRenderer::new);
            EntityRenderers.register(EntityRegistry.LUNAR_STRIKE.get(), LunarStrikeRenderer::new);
            EntityRenderers.register(EntityRegistry.RIFLE_LASER.get(), RifleLaserRenderer::new);

            ItemProperties.register(ItemRegistry.MUSIC_DISC_PLAYER.get(), Halcyon.locate("playing"), (stack, level, entity, seed) -> {
                if (stack.has(DataComponentRegistry.PLAYING_MUSIC)) {
                    return 1;
                }
                return 0;
            });
            ItemProperties.register(ItemRegistry.GRAPPLING_HOOK.get(), Halcyon.locate("charged"), chargedGrapplingHookProperty);
            ItemProperties.register(ItemRegistry.GRAPPLING_HOOK.get(), Halcyon.locate("using"), usingGrapplingHookProperty);
            ItemProperties.register(ItemRegistry.TRANS_GRAPPLING_HOOK.get(), Halcyon.locate("charged"), chargedGrapplingHookProperty);
            ItemProperties.register(ItemRegistry.TRANS_GRAPPLING_HOOK.get(), Halcyon.locate("using"), usingGrapplingHookProperty);

            progressionShader = new ProgressionShader();
            PostShaderManager.addShader(progressionShader);
            genderEuphoriaShader = new GenderEuphoriaShader();
            PostShaderManager.addShader(genderEuphoriaShader);
            machineOutputShader = new MachineOutputShader();
            PostShaderManager.addShader(machineOutputShader);
            machineOutputShader.setActive(true);
            orePingShader = new OrePingShader();
            PostShaderManager.addShader(orePingShader);
            orePingShader.setActive(true);
        });
    }

    public static final ItemPropertyFunction usingGrapplingHookProperty = (stack, level, entity, seed) -> {
        if (entity != null) {
            if (entity.getData(AttachmentTypeRegistry.GRAPPLING_HOOK_DATA).isPresent()) {
                return 1;
            }
        }
        return 0;
    };

    public static final ItemPropertyFunction chargedGrapplingHookProperty = (stack, level, entity, seed) -> {
        if (stack.has(DataComponentRegistry.ESSENCE_STORAGE) && ItemEssenceContainer.getEssence(stack, GrapplingHook.FUEL_ESSENCE_TYPE) >= GrapplingHook.ESSENCE_COST) {
            return 1;
        }
        return 0;
    };

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/auto-fabricator.png"), Halcyon.locate("auto-fabricator")), BlockRegistry.AUTO_FABRICATOR.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/charger.png"), Halcyon.locate("charger")), BlockRegistry.CHARGER.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/entropic_processor.png"), Halcyon.locate("entropic_processor")), BlockRegistry.ENTROPIC_PROCESSOR.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/essence_point.png"), Halcyon.locate("essence_point")), BlockRegistry.ESSENCE_POINT.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/exotic_essence_point.png"), Halcyon.locate("essence_point")), BlockRegistry.EXOTIC_ESSENCE_POINT.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/fabricator.png"), Halcyon.locate("fabricator")), BlockRegistry.FABRICATOR.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/lunarium.png"), Halcyon.locate("lunarium")), BlockRegistry.LUNARIUM.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/fluid_point.png"), Halcyon.locate("essence_point")), BlockRegistry.FLUID_POINT.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/infuser.png"), Halcyon.locate("infuser")), BlockRegistry.INFUSER.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/item_point.png"), Halcyon.locate("essence_point")), BlockRegistry.ITEM_POINT.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/lunar_node.png"), Halcyon.locate("essence_point")), BlockRegistry.LUNAR_ESSENCE_POINT.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/natural_essence_point.png"), Halcyon.locate("essence_point")), BlockRegistry.NATURAL_ESSENCE_POINT.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/fluid_mixer.png"), Halcyon.locate("fluid_mixer")), BlockRegistry.FLUID_MIXER.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/metal_shaper.png"), Halcyon.locate("metal_shaper")), BlockRegistry.METAL_SHAPER.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/industrial_plant_siphon.png"), Halcyon.locate("industrial_plant_siphon")), BlockRegistry.INDUSTRIAL_PLANT_SIPHON.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/chemical_node.png"), Halcyon.locate("essence_point")), BlockRegistry.CHEMICAL_NODE.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/ender_pearl_capture.png"), Halcyon.locate("ender_pearl_capture")), BlockRegistry.ENDER_PEARL_CAPTURE.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/ender_pearl_relay.png"), Halcyon.locate("ender_pearl_relay")), BlockRegistry.ENDER_PEARL_RELAY.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/ender_pearl_destination.png"), Halcyon.locate("ender_pearl_destination")), BlockRegistry.ENDER_PEARL_DESTINATION.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/survey_tunneler.png"), Halcyon.locate("survey_tunneler")), BlockRegistry.SURVEY_TUNNELER.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/essence_derivation_spike.png"), Halcyon.locate("essence_derivation_spike")), BlockRegistry.ESSENCE_DERIVATION_SPIKE.get().asItem());
        event.registerItem(AnimatedBlockItemUtil.createBasicExtensions(Halcyon.locate("textures/block/rf_node.png"), Halcyon.locate("essence_point")), BlockRegistry.RF_NODE.get().asItem());
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(ItemRegistry.ESSENCE_SWORD.get(), ItemDecorators.essenceBarDecoration);
        event.register(ItemRegistry.ILLUMINATION_ROD.get(), ItemDecorators.essenceBarDecoration);
        event.register(ItemRegistry.PRIMITIVE_ANTI_GRAVITY_PACK.get(), ItemDecorators.essenceBarDecoration);
        event.register(ItemRegistry.REPULSION_ROD.get(), ItemDecorators.essenceBarDecoration);
        event.register(ItemRegistry.GRAPPLING_HOOK.get(), ItemDecorators.essenceBarDecoration);
        event.register(ItemRegistry.TRANS_GRAPPLING_HOOK.get(), ItemDecorators.essenceBarDecoration);
        event.register(ItemRegistry.ORE_SCANNER.get(), ItemDecorators.essenceBarDecoration);
        event.register(ItemRegistry.MOLD_ROD.get(), ItemDecorators.shapingImpression);
        event.register(ItemRegistry.MOLD_PANEL.get(), ItemDecorators.shapingImpression);
        event.register(ItemRegistry.ECLIPTRUM_MOLD_ROD.get(), ItemDecorators.shapingImpression);
        event.register(ItemRegistry.ECLIPTRUM_MOLD_PANEL.get(), ItemDecorators.shapingImpression);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.FABRICATOR_MENU.get(), FabricatorScreen::new);
        event.register(MenuRegistry.ESSENCE_BURNER_MENU.get(), EssenceBurnerScreen::new);
        event.register(MenuRegistry.INFUSER_MENU.get(), InfuserScreen::new);
        event.register(MenuRegistry.LASER_EMITTER_MENU.get(), LaserEmitterScreen::new);
        event.register(MenuRegistry.AUTO_FABRICATOR_MENU.get(), AutoFabricatorScreen::new);
        event.register(MenuRegistry.FLUID_BOTTLER_MENU.get(), FluidBottlerScreen::new);
        event.register(MenuRegistry.ENTROPIC_PROCESSOR_MENU.get(), EntropicProcessorScreen::new);
        event.register(MenuRegistry.ESSENCE_FURNACE_MENU.get(), EssenceFurnaceScreen::new);
        event.register(MenuRegistry.SYNTHESIS_CHAMBER_MENU.get(), SynthesisChamberScreen::new);
        event.register(MenuRegistry.FLUID_MIXER_MENU.get(), FluidMixerScreen::new);
        event.register(MenuRegistry.MINERAL_PURIFICATION_CHAMBER_MENU.get(), MineralPurificationChamberScreen::new);
        event.register(MenuRegistry.ITEM_FILTER_MENU.get(), ItemFilterScreen::new);
        event.register(MenuRegistry.MUSIC_DISC_PLAYER_MENU.get(), MusicDiscPlayerScreen::new);
        event.register(MenuRegistry.FILTER_NODE_UPGRADE_MENU.get(), FilterNodeUpgradeScreen::new);
        event.register(MenuRegistry.METAL_SHAPER_MENU.get(), MetalShaperScreen::new);
        event.register(MenuRegistry.INDUSTRIAL_PLANT_SIPHON_MENU.get(), IndustrialPlantSiphonScreen::new);
        event.register(MenuRegistry.MELTER_MENU.get(), MelterScreen::new);
        event.register(MenuRegistry.DRYING_TABLE_MENU.get(), DryingTableScreen::new);
        event.register(MenuRegistry.LUNARIUM_MENU.get(), LunariumScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.ESSENCE_SPARKLE.get(),
                EssenceSparkleParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.CIRCLE.get(),
                CircleParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.CIRCLE_SHADE.get(),
                CircleShadeParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.RHOMBUS.get(),
                RhombusParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SMALL_CIRCLE.get(),
                SmallCircleParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.MOTE.get(),
                MoteParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SANCTUARY_SPARKLE.get(),
                SanctuarySparkleParticle.Provider::new);

        event.registerSpriteSet(ParticleRegistry.ENERGY_PARTICLE.get(), EnergyParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register((pState, pLevel, pPos, pTintIndex) -> {
            if (pPos != null) {
                return ColorUtil.blendColors(new Color(0x0CB3FF), new Color(0xFF49C1), (SimplexNoise.noise(pPos.getX()/25f, pPos.getY()/25f, pPos.getZ()/25f)+1f)/2f).getRGB();
            } else {
                float blendAmount = Minecraft.getInstance().levelRenderer.getTicks()+Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                return ColorUtil.blendColors(new Color(0x0CB3FF), new Color(0xFF49C1), (float) (Math.sin(blendAmount/(360f/5f))+1f)/2f).getRGB();
            }
        }, BlockRegistry.SPIRE_GLASS.get());

        event.register((pState, pLevel, pPos, pTintIndex) -> {
            if (pPos != null) {
                double x = pPos.getX();
                double y = pPos.getY();
                double z = pPos.getZ();

                // y position / 14 + {z pos / 42 + 0.5} + { 0.5sin(x pos / 14)}
                float hue = (float) ((y/14)+((z/42)+0.5+0.5*Math.sin(x/14)));
                return Color.getHSBColor( hue, 0.8f, 1f ).getRGB();
            } else {
                float blendAmount = Minecraft.getInstance().levelRenderer.getTicks()+Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                return Color.getHSBColor((float) (Math.sin(blendAmount/(360f/5f))+1f)/2f, 0.8f, 1f).getRGB();
            }
        }, BlockRegistry.CRYSTALLINE_LEAVES.get());

        event.register( (pState, pLevel, pPos, pTintIndex) -> {
            if (pPos != null && pLevel != null) {
                if (pTintIndex == 1) {
                    if (pLevel.getBlockEntity(pPos) instanceof EssenceReaderBlockEntity reader) {
                        return reader.getColor().getRGB();
                    }
                }
            }
            return 0xFFFFFFFF;
        }, BlockRegistry.ESSENCE_READER.get());

        if (hasOpalescence) {
            event.register((pState, pLevel, pPos, pTintIndex) -> {
                if (pPos != null) {
                    if (pTintIndex == 0) {
                        return DataNEssenceIntegration.OpalescenseIntegration.getOpalBlockColor(pState, pLevel, pPos, pTintIndex);
                    }
                }
                return 0xFFFFFFFF;
            }, BlockRegistry.TRAVERSITE_ROAD_OPAL.get(), BlockRegistry.TRAVERSITE_ROAD_SLAB_OPAL.get(), BlockRegistry.TRAVERSITE_ROAD_STAIRS_OPAL.get());
        }
    }

    @SubscribeEvent
    public static void registerItemColorHandlers(RegisterColorHandlersEvent.Item event) {
        event.register((pStack, pTintIndex) -> {
            float blendAmount = Minecraft.getInstance().levelRenderer.getTicks()+Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
            return ColorUtil.blendColors(new Color(0x0CB3FF), new Color(0xFF49C1), (float) (Math.sin(blendAmount/(360f/5f))+1f)/2f).getRGB();
        }, BlockRegistry.SPIRE_GLASS.get());

        event.register((pStack, pTintIndex) -> {
            float blendAmount = Minecraft.getInstance().levelRenderer.getTicks()+Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
            return Color.getHSBColor((float) (Math.sin(blendAmount/(360f/5f))+1f)/2f, 1f, 1f).getRGB();
        }, BlockRegistry.CRYSTALLINE_LEAVES.get());
        
        if (hasOpalescence) {
            event.register((stack, tintIndex) -> tintIndex == 0
                    ? DataNEssenceIntegration.OpalescenseIntegration.getOpalItemColor(stack, tintIndex)
                    : 0xFFFFFFFF,
                    BlockRegistry.TRAVERSITE_ROAD_OPAL.get(), BlockRegistry.TRAVERSITE_ROAD_SLAB_OPAL.get(), BlockRegistry.TRAVERSITE_ROAD_STAIRS_OPAL.get());
        }
    }
}
