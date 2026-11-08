package EsetKalenko.Halcyon.registry;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.block.auxiliary.*;
import EsetKalenko.Halcyon.block.control.EssenceReaderBlockEntity;
import EsetKalenko.Halcyon.block.control.OverseeingSpikeBlockEntity;
import EsetKalenko.Halcyon.block.processing.*;
import EsetKalenko.Halcyon.block.production.*;
import EsetKalenko.Halcyon.block.storage.*;
import EsetKalenko.Halcyon.block.transmission.*;
import EsetKalenko.Halcyon.block.decoration.EssenceBridgeBlockEntity;
import EsetKalenko.Halcyon.block.decoration.FlareLightBlockEntity;
import EsetKalenko.Halcyon.block.generation.EssenceBurnerBlockEntity;
import EsetKalenko.Halcyon.block.generation.EssenceLeechBlockEntity;
import EsetKalenko.Halcyon.block.generation.IndustrialPlantSiphonBlockEntity;
import EsetKalenko.Halcyon.block.generation.EssenceDerivationSpikeBlockEntity;
import EsetKalenko.Halcyon.block.technical.ArekkoBlockEntity;
import EsetKalenko.Halcyon.block.technical.ComputerBlockEntity;
import EsetKalenko.Halcyon.block.technical.AncientDataBankBlockEntity;
import EsetKalenko.Halcyon.block.technical.StructureProtectorBlockEntity;
import EsetKalenko.Halcyon.block.technical.cryochamber.CryochamberBlockEntity;
import EsetKalenko.Halcyon.block.transportation.EnderPearlCaptureBlockEntity;
import EsetKalenko.Halcyon.block.transportation.EnderPearlDestinationBlockEntity;
import EsetKalenko.Halcyon.block.transportation.EnderPearlRelayBlockEntity;
import EsetKalenko.Halcyon.block.world.TidalMeteoriteBlockEntity;
import EsetKalenko.Halcyon.integration.mekanism.ChemicalNodeBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Halcyon.MOD_ID);
    public static final Supplier<BlockEntityType<FluidMixerBlockEntity>> FLUID_MIXER =
            register("fluid_mixer", () ->
                    BlockEntityType.Builder.of(FluidMixerBlockEntity::new,
                            BlockRegistry.FLUID_MIXER.get()).build(null));
    public static final Supplier<BlockEntityType<StructureProtectorBlockEntity>> STRUCTURE_PROTECTOR =
            register("structure_protector", () ->
                    BlockEntityType.Builder.of(StructureProtectorBlockEntity::new,
                            BlockRegistry.STRUCTURE_PROTECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<VacuumBlockEntity>> VACUUM =
            register("vacuum", () ->
                    BlockEntityType.Builder.of(VacuumBlockEntity::new,
                            BlockRegistry.VACUUM.get()).build(null));
    public static final Supplier<BlockEntityType<FluidBottlerBlockEntity>> FLUID_BOTTLER =
            register("fluid_bottler", () ->
                    BlockEntityType.Builder.of(FluidBottlerBlockEntity::new,
                            BlockRegistry.FLUID_BOTTLER.get()).build(null));
    public static final Supplier<BlockEntityType<MineralPurificationChamberBlockEntity>> MINERAL_PURIFICATION_CHAMBER =
            register("mineral_purification_chamber", () ->
                    BlockEntityType.Builder.of(MineralPurificationChamberBlockEntity::new,
                            BlockRegistry.MINERAL_PURIFICATION_CHAMBER.get()).build(null));
    public static final Supplier<BlockEntityType<EntropicProcessorBlockEntity>> ENTROPIC_PROCESSOR =
            register("entropic_processor", () ->
                    BlockEntityType.Builder.of(EntropicProcessorBlockEntity::new,
                            BlockRegistry.ENTROPIC_PROCESSOR.get()).build(null));
    public static final Supplier<BlockEntityType<ItemFilterBlockEntity>> ITEM_FILTER =
            register("item_filter", () ->
                    BlockEntityType.Builder.of(ItemFilterBlockEntity::new,
                            BlockRegistry.ITEM_FILTER.get()).build(null));
    public static final Supplier<BlockEntityType<SynthesisChamberBlockEntity>> SYNTHESIS_CHAMBER =
            register("synthesis_chamber", () ->
                    BlockEntityType.Builder.of(SynthesisChamberBlockEntity::new,
                            BlockRegistry.SYNTHESIS_CHAMBER.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceFurnaceBlockEntity>> ESSENCE_FURNACE =
            register("essence_furnace", () ->
                    BlockEntityType.Builder.of(EssenceFurnaceBlockEntity::new,
                            BlockRegistry.ESSENCE_FURNACE.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceLeechBlockEntity>> ESSENCE_LEECH =
            register("essence_leech", () ->
                    BlockEntityType.Builder.of(EssenceLeechBlockEntity::new,
                            BlockRegistry.ESSENCE_LEECH.get()).build(null));
    public static final Supplier<BlockEntityType<LaserEmitterBlockEntity>> LASER_EMITTER =
            register("laser_emitter", () ->
                    BlockEntityType.Builder.of(LaserEmitterBlockEntity::new,
                            BlockRegistry.LASER_EMITTER.get()).build(null));
    public static final Supplier<BlockEntityType<FluidSpillerBlockEntity>> FLUID_SPILLER =
            register("fluid_spiller", () ->
                    BlockEntityType.Builder.of(FluidSpillerBlockEntity::new,
                            BlockRegistry.FLUID_SPILLER.get()).build(null));
    public static final Supplier<BlockEntityType<FluidCollectorBlockEntity>> FLUID_COLLECTOR =
            register("fluid_collector", () ->
                    BlockEntityType.Builder.of(FluidCollectorBlockEntity::new,
                            BlockRegistry.FLUID_COLLECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<ComputerBlockEntity>> COMPUTER =
            register("computer", () ->
                    BlockEntityType.Builder.of(ComputerBlockEntity::new,
                            BlockRegistry.COMPUTER.get()).build(null));
    public static final Supplier<BlockEntityType<FabricatorBlockEntity>> FABRICATOR =
            register("fabricator", () ->
                    BlockEntityType.Builder.of(FabricatorBlockEntity::new,
                            BlockRegistry.FABRICATOR.get()).build(null));
    public static final Supplier<BlockEntityType<InfuserBlockEntity>> INFUSER =
            register("infuser", () ->
                    BlockEntityType.Builder.of(InfuserBlockEntity::new,
                            BlockRegistry.INFUSER.get()).build(null));
    public static final Supplier<BlockEntityType<ChargerBlockEntity>> CHARGER =
            register("charger", () ->
                    BlockEntityType.Builder.of(ChargerBlockEntity::new,
                            BlockRegistry.CHARGER.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceBurnerBlockEntity>> ESSENCE_BURNER =
            register("essence_burner", () ->
                    BlockEntityType.Builder.of(EssenceBurnerBlockEntity::new,
                            BlockRegistry.ESSENCE_BURNER.get()).build(null));
    public static final Supplier<BlockEntityType<EssencePointBlockEntity>> ESSENCE_POINT =
            register("essence_point", () ->
                    BlockEntityType.Builder.of(EssencePointBlockEntity::new,
                            BlockRegistry.ESSENCE_POINT.get()).build(null));
    public static final Supplier<BlockEntityType<LunarEssencePointBlockEntity>> LUNAR_ESSENCE_POINT =
            register("lunar_essence_point", () ->
                    BlockEntityType.Builder.of(LunarEssencePointBlockEntity::new,
                            BlockRegistry.LUNAR_ESSENCE_POINT.get()).build(null));
    public static final Supplier<BlockEntityType<NaturalEssencePointBlockEntity>> NATURAL_ESSENCE_POINT =
            register("natural_essence_point", () ->
                    BlockEntityType.Builder.of(NaturalEssencePointBlockEntity::new,
                            BlockRegistry.NATURAL_ESSENCE_POINT.get()).build(null));
    public static final Supplier<BlockEntityType<ExoticEssencePointBlockEntity>> EXOTIC_ESSENCE_POINT =
            register("exotic_essence_point", () ->
                    BlockEntityType.Builder.of(ExoticEssencePointBlockEntity::new,
                            BlockRegistry.EXOTIC_ESSENCE_POINT.get()).build(null));
    public static final Supplier<BlockEntityType<ItemPointBlockEntity>> ITEM_POINT =
            register("item_point", () ->
                    BlockEntityType.Builder.of(ItemPointBlockEntity::new,
                            BlockRegistry.ITEM_POINT.get()).build(null));
    public static final Supplier<BlockEntityType<FluidPointBlockEntity>> FLUID_POINT =
            register("fluid_point", () ->
                    BlockEntityType.Builder.of(FluidPointBlockEntity::new,
                            BlockRegistry.FLUID_POINT.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceBufferBlockEntity>> ESSENCE_BUFFER =
            register("essence_buffer", () ->
                    BlockEntityType.Builder.of(EssenceBufferBlockEntity::new,
                            BlockRegistry.ESSENCE_BUFFER.get()).build(null));
    public static final Supplier<BlockEntityType<ItemBufferBlockEntity>> ITEM_BUFFER =
            register("item_buffer", () ->
                    BlockEntityType.Builder.of(ItemBufferBlockEntity::new,
                            BlockRegistry.ITEM_BUFFER.get()).build(null));
    public static final Supplier<BlockEntityType<LimitedItemBufferBlockEntity>> LIMITED_ITEM_BUFFER =
            register("limited_item_buffer", () ->
                    BlockEntityType.Builder.of(LimitedItemBufferBlockEntity::new,
                            BlockRegistry.LIMITED_ITEM_BUFFER.get()).build(null));
    public static final Supplier<BlockEntityType<FluidBufferBlockEntity>> FLUID_BUFFER =
            register("fluid_buffer", () ->
                    BlockEntityType.Builder.of(FluidBufferBlockEntity::new,
                            BlockRegistry.FLUID_BUFFER.get()).build(null));
    public static final Supplier<BlockEntityType<AncientDataBankBlockEntity>> ANCIENT_DATA_BANK =
            register("data_bank", () ->
                    BlockEntityType.Builder.of(AncientDataBankBlockEntity::new,
                            BlockRegistry.ANCIENT_DATA_BANK.get()).build(null));
    public static final Supplier<BlockEntityType<DataBankBlockEntity>> DATA_BANK =
            register("player_data_bank", () ->
                    BlockEntityType.Builder.of(DataBankBlockEntity::new,
                            BlockRegistry.DATA_BANK.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceBatteryBlockEntity>> ESSENCE_BATTERY =
            register("essence_battery", () ->
                    BlockEntityType.Builder.of(EssenceBatteryBlockEntity::new,
                            BlockRegistry.ESSENCE_BATTERY.get()).build(null));
    public static final Supplier<BlockEntityType<LunarEssenceBatteryBlockEntity>> LUNAR_ESSENCE_BATTERY =
            register("lunar_essence_battery", () ->
                    BlockEntityType.Builder.of(LunarEssenceBatteryBlockEntity::new,
                            BlockRegistry.LUNAR_ESSENCE_BATTERY.get()).build(null));
    public static final Supplier<BlockEntityType<NaturalEssenceBatteryBlockEntity>> NATURAL_ESSENCE_BATTERY =
            register("natural_essence_battery", () ->
                    BlockEntityType.Builder.of(NaturalEssenceBatteryBlockEntity::new,
                            BlockRegistry.NATURAL_ESSENCE_BATTERY.get()).build(null));
    public static final Supplier<BlockEntityType<ExoticEssenceBatteryBlockEntity>> EXOTIC_ESSENCE_BATTERY =
            register("exotic_essence_battery", () ->
                    BlockEntityType.Builder.of(ExoticEssenceBatteryBlockEntity::new,
                            BlockRegistry.EXOTIC_ESSENCE_BATTERY.get()).build(null));
    public static final Supplier<BlockEntityType<CreativeEssenceBatteryBlockEntity>> CREATIVE_ESSENCE_BATTERY =
            register("creative_essence_battery", () ->
                    BlockEntityType.Builder.of(CreativeEssenceBatteryBlockEntity::new,
                            BlockRegistry.CREATIVE_ESSENCE_BATTERY.get()).build(null));
    public static final Supplier<BlockEntityType<FluidTankBlockEntity>> FLUID_TANK =
            register("fluid_tank", () ->
                    BlockEntityType.Builder.of(FluidTankBlockEntity::new,
                            BlockRegistry.FLUID_TANK.get()).build(null));
    public static final Supplier<BlockEntityType<AutoFabricatorBlockEntity>> AUTO_FABRICATOR =
            register("auto-fabricator", () ->
                    BlockEntityType.Builder.of(AutoFabricatorBlockEntity::new,
                            BlockRegistry.AUTO_FABRICATOR.get()).build(null));
    public static final Supplier<BlockEntityType<EnticingLureBlockEntity>> ENTICING_LURE =
            register("enticing_lure", () ->
                    BlockEntityType.Builder.of(EnticingLureBlockEntity::new,
                            BlockRegistry.ENTICING_LURE.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceBreakerBlockEntity>> ESSENCE_BREAKER =
            register("essence_breaker", () ->
                    BlockEntityType.Builder.of(EssenceBreakerBlockEntity::new,
                            BlockRegistry.ESSENCE_BREAKER.get()).build(null));
    public static final Supplier<BlockEntityType<MetalShaperBlockEntity>> METAL_SHAPER =
            register("metal_shaper", () ->
                    BlockEntityType.Builder.of(MetalShaperBlockEntity::new,
                            BlockRegistry.METAL_SHAPER.get()).build(null));
    public static final Supplier<BlockEntityType<ArekkoBlockEntity>> AREKKO =
            register("arekko", () ->
                    BlockEntityType.Builder.of(ArekkoBlockEntity::new,
                            BlockRegistry.AREKKO.get()).build(null));
    public static final Supplier<BlockEntityType<IndustrialPlantSiphonBlockEntity>> INDUSTRIAL_PLANT_SIPHON =
            register("industrial_plant_siphon", () ->
                    BlockEntityType.Builder.of(IndustrialPlantSiphonBlockEntity::new,
                            BlockRegistry.INDUSTRIAL_PLANT_SIPHON.get()).build(null));
    public static final Supplier<BlockEntityType<MelterBlockEntity>> MELTER =
            register("melter", () ->
                    BlockEntityType.Builder.of(MelterBlockEntity::new,
                            BlockRegistry.MELTER.get()).build(null));
    public static final Supplier<BlockEntityType<CryochamberBlockEntity>> CRYOCHAMBER =
            register("cryochamber", () ->
                    BlockEntityType.Builder.of(CryochamberBlockEntity::new,
                            BlockRegistry.CRYOCHAMBER.get()).build(null));
    public static final Supplier<BlockEntityType<FlareLightBlockEntity>> FLARE_LIGHT =
            register("flare_light", () ->
                    BlockEntityType.Builder.of(FlareLightBlockEntity::new,
                            BlockRegistry.FLARE_LIGHT.get()).build(null));
    public static final Supplier<BlockEntityType<ChemicalNodeBlockEntity>> CHEMICAL_NODE =
            register("chemical_node", () ->
                    BlockEntityType.Builder.of(ChemicalNodeBlockEntity::new,
                            BlockRegistry.CHEMICAL_NODE.get()).build(null));
    public static final Supplier<BlockEntityType<RFNodeBlockEntity>> RF_NODE =
            register("rf_node", () ->
                    BlockEntityType.Builder.of(RFNodeBlockEntity::new,
                            BlockRegistry.RF_NODE.get()).build(null));
    public static final Supplier<BlockEntityType<DryingTableBlockEntity>> DRYING_TABLE =
            register("drying_table", () ->
                    BlockEntityType.Builder.of(DryingTableBlockEntity::new,
                            BlockRegistry.DRYING_TABLE.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceBridgeBlockEntity>> ESSENCE_BRIDGE =
            register("essence_bridge", () ->
                    BlockEntityType.Builder.of(EssenceBridgeBlockEntity::new,
                            BlockRegistry.ESSENCE_BRIDGE.get()).build(null));
    public static final Supplier<BlockEntityType<EnderPearlCaptureBlockEntity>> ENDER_PEARL_CAPTURE =
            register("ender_pearl_capture", () ->
                    BlockEntityType.Builder.of(EnderPearlCaptureBlockEntity::new,
                            BlockRegistry.ENDER_PEARL_CAPTURE.get()).build(null));
    public static final Supplier<BlockEntityType<EnderPearlDestinationBlockEntity>> ENDER_PEARL_DESTINATION =
            register("ender_pearl_destination", () ->
                    BlockEntityType.Builder.of(EnderPearlDestinationBlockEntity::new,
                            BlockRegistry.ENDER_PEARL_DESTINATION.get()).build(null));
    public static final Supplier<BlockEntityType<EnderPearlRelayBlockEntity>> ENDER_PEARL_RELAY =
            register("ender_pearl_relay", () ->
                    BlockEntityType.Builder.of(EnderPearlRelayBlockEntity::new,
                            BlockRegistry.ENDER_PEARL_RELAY.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceReaderBlockEntity>> ESSENCE_READER =
            register("essence_reader", () ->
                    BlockEntityType.Builder.of(EssenceReaderBlockEntity::new,
                            BlockRegistry.ESSENCE_READER.get()).build(null));
    public static final Supplier<BlockEntityType<OverseeingSpikeBlockEntity>> OVERSEEING_SPIKE =
            register("overseeing_spike", () ->
                    BlockEntityType.Builder.of(OverseeingSpikeBlockEntity::new,
                            BlockRegistry.OVERSEEING_SPIKE.get()).build(null));
    public static final Supplier<BlockEntityType<SurveyTunnelerBlockEntity>> SURVEY_TUNNELER =
            register("survey_tunneler", () ->
                    BlockEntityType.Builder.of(SurveyTunnelerBlockEntity::new,
                            BlockRegistry.SURVEY_TUNNELER.get()).build(null));
    public static final Supplier<BlockEntityType<TwiningLanternBlockEntity>> TWINING_LANTERN =
            register("twining_lantern", () ->
                    BlockEntityType.Builder.of(TwiningLanternBlockEntity::new,
                            BlockRegistry.TWINING_LANTERN.get()).build(null));
    public static final Supplier<BlockEntityType<EssenceDerivationSpikeBlockEntity>> ESSENCE_DERIVATION_SPIKE =
            register("essence_derivation_spike", () ->
                    BlockEntityType.Builder.of(EssenceDerivationSpikeBlockEntity::new,
                            BlockRegistry.ESSENCE_DERIVATION_SPIKE.get()).build(null));
    public static final Supplier<BlockEntityType<TidalMeteoriteBlockEntity>> LUNAR_CRYSTAL_SEED =
            register("lunar_crystal_seed", () ->
                    BlockEntityType.Builder.of(TidalMeteoriteBlockEntity::new,
                            BlockRegistry.TIDAL_METEORITE.get()).build(null));
    public static final Supplier<BlockEntityType<CrystallineCradleBlockEntity>> CRYSTALLINE_CRADLE =
            register("crystalline_cradle", () ->
                    BlockEntityType.Builder.of(CrystallineCradleBlockEntity::new,
                            BlockRegistry.CRYSTALLINE_CRADLE.get()).build(null));
    public static final Supplier<BlockEntityType<LunariumBlockEntity>> LUNARIUM =
            register("lunarium", () ->
                    BlockEntityType.Builder.of(LunariumBlockEntity::new,
                            BlockRegistry.LUNARIUM.get()).build(null));
    public static final Supplier<BlockEntityType<SimpleHarvesterBlockEntity>> HARVESTER =
            register("harvester", () ->
                    BlockEntityType.Builder.of(SimpleHarvesterBlockEntity::new,
                            BlockRegistry.SIMPLE_HARVESTER.get()).build(null));


    private static <T extends BlockEntityType<?>> Supplier<T> register(final String name, final Supplier<T> blockentity) {
        return BLOCK_ENTITIES.register(name, blockentity);
    }
}
