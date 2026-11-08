package EsetKalenko.Halcyon.registry;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.api.block.SpreadingPlant;

import EsetKalenko.Halcyon.block.DirectionalPillarBlock;
import EsetKalenko.Halcyon.block.auxiliary.*;
import EsetKalenko.Halcyon.block.control.EssenceReader;
import EsetKalenko.Halcyon.block.control.OverseeingSpike;
import EsetKalenko.Halcyon.block.decoration.*;
import EsetKalenko.Halcyon.block.processing.*;
import EsetKalenko.Halcyon.block.production.*;
import EsetKalenko.Halcyon.block.storage.*;
import EsetKalenko.Halcyon.block.transmission.*;
import EsetKalenko.Halcyon.block.transportation.*;
import EsetKalenko.Halcyon.block.world.*;
import EsetKalenko.Halcyon.block.world.sanctuary.*;
import EsetKalenko.Halcyon.block.world.shieldingstone.*;
import EsetKalenko.Halcyon.block.generation.EssenceBurner;
import EsetKalenko.Halcyon.block.generation.EssenceLeech;
import EsetKalenko.Halcyon.block.generation.IndustrialPlantSiphon;
import EsetKalenko.Halcyon.block.generation.EssenceDerivationSpike;
import EsetKalenko.Halcyon.block.technical.Arekko;
import EsetKalenko.Halcyon.block.technical.Computer;
import EsetKalenko.Halcyon.block.technical.AncientDataBank;
import EsetKalenko.Halcyon.block.technical.StructureProtector;
import EsetKalenko.Halcyon.block.technical.cryochamber.Cryochamber;
import EsetKalenko.Halcyon.block.technical.cryochamber.CryochamberRouter;
import EsetKalenko.Halcyon.block.technical.empty_cryochamber.EmptyCryochamber;
import EsetKalenko.Halcyon.block.technical.empty_cryochamber.EmptyCryochamberRouter;
import EsetKalenko.Halcyon.integration.mekanism.ChemicalNode;
import EsetKalenko.Halcyon.item.EssenceBatteryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockRegistry {
    public static BlockBehaviour.Properties getMachineProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().strength(2.0f);
    }

    public static BlockBehaviour.Properties getToollessMachineProperties() {
        return BlockBehaviour.Properties.of()
                .strength(2.0f, 6.0f)
                .noOcclusion()
                .sound(SoundType.COPPER)
                .mapColor(MapColor.COLOR_ORANGE);
    }

    public static BlockBehaviour.Properties getShieldlessAncientRockProperties() {
        return BlockBehaviour.Properties.of()
                .requiresCorrectToolForDrops()
                .strength(50.0F, 360000f)
                .sound(SoundType.DEEPSLATE_TILES)
                .mapColor(MapColor.COLOR_PURPLE)
                .instrument(NoteBlockInstrument.BASEDRUM);
    }

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK,
            Halcyon.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = ItemRegistry.ITEMS;

    // World
    public static final Supplier<Block> ESSENCE_CRYSTAL = register("essence_crystal",
            () -> new TidalCrystalIndustrial(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER).noOcclusion().strength(2.0f)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CRYSTAL_OF_TRANSFORMATION = register("crystal_of_transformation",
            () -> new TidalCrystalLunar(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER).noOcclusion().strength(2.0f)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> LENSING_CRYSTAL_ORE = register("lensing_crystal_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SPIRE_GLASS = register("spire_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 6)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TETHERGRASS = register("tethergrass",
            () -> new SpreadingPlant(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).lightLevel((blockState) -> { return 5;}),
                    17),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> VIVID_LOTUS = register("vivid_lotus",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CRYSTALLINE_LOG = register("crystalline_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CRYSTALLINE_LEAVES = register("crystalline_leaves",
            () -> new CrystallineLeaves(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).emissiveRendering(BlockRegistry::always).lightLevel(state -> 5).sound(SoundType.AMETHYST)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TIDAL_METEORITE = register("tidal_meteorite",
            () -> new TidalMeteorite(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).emissiveRendering(BlockRegistry::always).lightLevel(state -> 3).noOcclusion()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FROZEN_MOONLIGHT = register("frozen_moonlight",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.PACKED_ICE)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> VERMILION_GRASS_BLOCK = register("vermilion_grass",
            () -> new SanctuaryGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SANCTUARY_DIRT = register("sanctuary_dirt",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TALL_SANCTUARY_GRASS = register("tall_sanctuary_grass",
            () -> new TallSanctuaryGrass(BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 3)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> VERMILION_SPROUT = register("vermilion_sprout",
            () -> new SanctuaryGrass(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 1)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SANCTUARY_SAND = register("sanctuary_sand",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> DEEP_SANCTUARY_SAND = register("deep_sanctuary_sand",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CYAN_PEARLESCENT_SPIRAL = register("cyan_pearlescent_spiral",
            () -> new PearlescentSpiral(BlockBehaviour.Properties.ofFullCopy(Blocks.KELP).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 12)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CYAN_PEARLESCENT_GRASS = register("cyan_pearlescent_grass",
            () -> new PearlescentGrass(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 6)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> MAGENTA_PEARLESCENT_SPIRAL = register("magenta_pearlescent_spiral",
            () -> new PearlescentSpiral(BlockBehaviour.Properties.ofFullCopy(Blocks.KELP).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 12)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> MAGENTA_PEARLESCENT_GRASS = register("magenta_pearlescent_grass",
            () -> new PearlescentGrass(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 6)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> YELLOW_PEARLESCENT_SPIRAL = register("yellow_pearlescent_spiral",
            () -> new PearlescentSpiral(BlockBehaviour.Properties.ofFullCopy(Blocks.KELP).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 12)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> YELLOW_PEARLESCENT_GRASS = register("yellow_pearlescent_grass",
            () -> new PearlescentGrass(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).emissiveRendering(BlockRegistry::always).lightLevel((state) -> 6)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> RED_PEARLESCENT_SAND = register("red_pearlescent_sand",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> GREEN_PEARLESCENT_SAND = register("green_pearlescent_sand",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> BLUE_PEARLESCENT_SAND = register("blue_pearlescent_sand",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ASTRAL_GOOP = register("astral_goop",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).sound(SoundType.SLIME_BLOCK)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Generators
    public static final Supplier<Block> ESSENCE_BURNER = register("essence_burner",
            () -> new EssenceBurner(getMachineProperties().lightLevel(
                    (state -> state.getValue(EssenceBurner.LIT) ? 13 : 0)
            )),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ESSENCE_LEECH = register("essence_leech",
            () -> new EssenceLeech(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> INDUSTRIAL_PLANT_SIPHON = register("industrial_plant_siphon",
            () -> new IndustrialPlantSiphon(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ESSENCE_DERIVATION_SPIKE = register("essence_derivation_spike",
            () -> new EssenceDerivationSpike(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Machines
    public static final Supplier<Block> FABRICATOR = register("fabricator",
            () -> new Fabricator(getMachineProperties().lightLevel((blockState) -> { return 4;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> LUNARIUM = register("lunarium",
            () -> new Lunarium(getMachineProperties().lightLevel((blockState) -> { return 4;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> INFUSER = register("infuser",
            () -> new Infuser(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUID_COLLECTOR = register("fluid_collector",
            () -> new FluidCollector(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUID_MIXER = register("fluid_mixer",
            () -> new FluidMixer(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> MINERAL_PURIFICATION_CHAMBER = register("mineral_purification_chamber",
            () -> new MineralPurificationChamber(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> VACUUM = register("vacuum",
            () -> new Vacuum(getMachineProperties().lightLevel((blockState) -> { return 3;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> LASER_EMITTER = register("laser_emitter",
            () -> new LaserEmitter(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUID_BOTTLER = register("fluid_bottler",
            () -> new FluidBottler(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ENTROPIC_PROCESSOR = register("entropic_processor",
            () -> new EntropicProcessor(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SYNTHESIS_CHAMBER = register("synthesis_chamber",
            () -> new SynthesisChamber(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ESSENCE_FURNACE = register("essence_furnace",
            () -> new EssenceFurnace(getMachineProperties().lightLevel(
                    (state -> state.getValue(EssenceFurnace.LIT) ? 13 : 0)
            )),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUID_SPILLER = register("fluid_spiller",
            () -> new FluidSpiller(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CHARGER = register("charger",
            () -> new Charger(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> AUTO_FABRICATOR = register("auto-fabricator",
            () -> new AutoFabricator(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ENTICING_LURE = register("enticing_lure",
            () -> new EnticingLure(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ITEM_FILTER = register("item_filter",
            () -> new ItemFilter(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ESSENCE_BREAKER = register("essence_breaker",
            () -> new EssenceBreaker(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> METAL_SHAPER = register("metal_shaper",
            () -> new MetalShaper(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> MELTER = register("melter",
            () -> new Melter(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> DRYING_TABLE = register("drying_table",
            () -> new DryingTable(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> DATA_BANK = register("player_data_bank",
            () -> new DataBank(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().lightLevel((blockState) -> { return 4;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SURVEY_TUNNELER = register("survey_tunneler",
            () -> new SurveyTunneler(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TWINING_LANTERN = register("twining_lantern",
            () -> new TwiningLantern(getToollessMachineProperties().lightLevel((blockState) -> { return 15;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SIMPLE_HARVESTER = register("harvester",
            () -> new SimpleHarvester(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Buffers
    public static final Supplier<Block> ESSENCE_BUFFER = register("essence_buffer",
            () -> new EssenceBuffer(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ITEM_BUFFER = register("item_buffer",
            () -> new ItemBuffer(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> LIMITED_ITEM_BUFFER = register("limited_item_buffer",
            () -> new LimitedItemBuffer(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUID_BUFFER = register("fluid_buffer",
            () -> new FluidBuffer(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> DECO_ESSENCE_BUFFER = register("deco_essence_buffer",
            () -> new RotatedPillarBlock(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> DECO_FLUID_BUFFER = register("deco_fluid_buffer",
            () -> new RotatedPillarBlock(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> DECO_ITEM_BUFFER = register("deco_item_buffer",
            () -> new RotatedPillarBlock(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CRYSTALLINE_CRADLE = register("crystalline_cradle",
            () -> new CrystallineCradle(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Nodes
    public static final Supplier<Block> ESSENCE_POINT = register("essence_point",
            () -> new EssencePoint(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> LUNAR_ESSENCE_POINT = register("lunar_essence_point",
            () -> new LunarEssencePoint(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> NATURAL_ESSENCE_POINT = register("natural_essence_point",
            () -> new NaturalEssencePoint(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> EXOTIC_ESSENCE_POINT = register("exotic_essence_point",
            () -> new ExoticEssencePoint(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUID_POINT = register("fluid_point",
            () -> new FluidPoint(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ITEM_POINT = register("item_point",
            () -> new ItemPoint(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CHEMICAL_NODE = register("chemical_node",
            () -> new ChemicalNode(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> RF_NODE = register("rf_node",
            () -> new RFNode(getToollessMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Redstone Components
    public static final Supplier<Block> ESSENCE_READER = register("essence_reader",
            () -> new EssenceReader(getToollessMachineProperties().instabreak()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> OVERSEEING_SPIKE = register("overseeing_spike",
            () -> new OverseeingSpike(getToollessMachineProperties().instabreak().noCollission()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Storage
    public static final Supplier<Block> ESSENCE_BATTERY = register("essence_battery",
            () -> new EssenceBattery(getMachineProperties()),
            object -> () -> new EssenceBatteryItem(
                    object.get(),
                    new Item.Properties(),
                    Halcyon.locate("essence")));
    public static final Supplier<Block> LUNAR_ESSENCE_BATTERY = register("lunar_essence_battery",
            () -> new LunarEssenceBattery(getMachineProperties()),
            object -> () -> new EssenceBatteryItem(
                    object.get(),
                    new Item.Properties(),
                    Halcyon.locate("lunar_essence")));
    public static final Supplier<Block> NATURAL_ESSENCE_BATTERY = register("natural_essence_battery",
            () -> new NaturalEssenceBattery(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> EXOTIC_ESSENCE_BATTERY = register("exotic_essence_battery",
            () -> new ExoticEssenceBattery(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CREATIVE_ESSENCE_BATTERY = register("creative_essence_battery",
            () -> new CreativeEssenceBattery(getMachineProperties()),
            object -> () -> new GameMasterBlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUID_TANK = register("fluid_tank",
            () -> new FluidTank(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Technical Blocks
    public static final Supplier<Block> STRUCTURE_PROTECTOR = register("structure_protector",
            () -> new StructureProtector(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().instabreak().noCollission()),
            object -> () -> new GameMasterBlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_DATA_BANK = register("data_bank",
            () -> new AncientDataBank(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().strength(2.0f, 3600000.0F).lightLevel((blockState) -> { return 4;})),
            object -> () -> new GameMasterBlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> COMPUTER = register("computer",
            () -> new Computer(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion().strength(2.0f, 3600000.0F).lightLevel((blockState) -> { return 4;})),
            object -> () -> new GameMasterBlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> AREKKO = registerBlock("arekko",
            () -> new Arekko(BlockBehaviour.Properties.ofFullCopy(Blocks.BONE_BLOCK).noOcclusion().noCollission()));
    public static final Supplier<Block> CRYOCHAMBER = register("cryochamber",
            () -> new Cryochamber(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> CRYOCHAMBER_FILLER = registerBlock("cryochamber_filler",
            () -> new CryochamberRouter(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion()));
    public static final Supplier<Block> ESSENCE_BRIDGE = register("essence_bridge",
            () -> new EssenceBridge(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(-1.0F, 3600000.0F).noOcclusion()),
            object -> () -> new GameMasterBlockItem(object.get(), new Item.Properties()));

    // Transportation Blocks
    public static final Supplier<Block> TRAVERSITE_ROAD = register("traversite_road",
            () -> new TraversiteRoad(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).noOcclusion().strength(2.0f), 2),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TRAVERSITE_ROAD_STAIRS = register("traversite_road_stairs",
            () -> new TraversiteStairs(TRAVERSITE_ROAD.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).noOcclusion().strength(2.0f), 2),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TRAVERSITE_ROAD_SLAB = register("traversite_road_slab",
            () -> new TraversiteSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).noOcclusion().strength(2.0f), 2),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TRAVERSITE_ROAD_OPAL = register("traversite_road_opal",
            () -> new TraversiteRoad(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).noOcclusion().strength(2.0f), 2),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TRAVERSITE_ROAD_STAIRS_OPAL = register("traversite_road_stairs_opal",
            () -> new TraversiteStairs(TRAVERSITE_ROAD.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).noOcclusion().strength(2.0f), 2),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> TRAVERSITE_ROAD_SLAB_OPAL = register("traversite_road_slab_opal",
            () -> new TraversiteSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).noOcclusion().strength(2.0f), 2),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ENDER_PEARL_CAPTURE = register("ender_pearl_capture",
            () -> new EnderPearlCapture(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ENDER_PEARL_DESTINATION = register("ender_pearl_destination",
            () -> new EnderPearlDestination(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ENDER_PEARL_RELAY = register("ender_pearl_relay",
            () -> new EnderPearlRelay(getMachineProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));


    // Shielding Rock - Structure-Only
    public static final Supplier<Block> ANCIENT_ROCK_BRICKS = register("ancient_rock_bricks",
            AncientShieldingStone::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_ROCK_TILES = register("ancient_rock_tiles",
            AncientShieldingStone::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_ROCK_COLUMN = register("ancient_rock_column",
            AncientShieldingPillar::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ENERGIZED_ANCIENT_ROCK_COLUMN = register("energized_ancient_rock_column",
            () -> new LuminousAncientShieldingPillar(3),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_LANTERN = register("ancient_lantern",
            () -> new LuminousAncientShieldingStone(13),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_SHELF = register("ancient_shelf",
            AncientShieldingStone::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_WINDOW = register("ancient_window",
            AncientShieldingGlass::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_GLYPH_STONE_BLANK = register("ancient_glyph_stone_blank",
            AncientShieldingStone::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_GLYPH_STONE_MAKUTUIN = register("ancient_glyph_stone_makutuin",
            AncientShieldingStone::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ANCIENT_GLYPH_STONE_ESSENCE = register("ancient_glyph_stone_essence",
            AncientShieldingStone::new,
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Ancient Rock - Survival-Obtainable
    public static final Supplier<Block> SHIELDLESS_ANCIENT_ROCK_BRICKS = register("ancient_rock_bricks_shieldless",
            () -> new Block(getShieldlessAncientRockProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_ROCK_TILES = register("ancient_rock_tiles_shieldless",
            () -> new Block(getShieldlessAncientRockProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_ROCK_COLUMN = register("ancient_rock_column_shieldless",
            () -> new DirectionalPillarBlock(getShieldlessAncientRockProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ENERGIZED_ANCIENT_ROCK_COLUMN = register("energized_ancient_rock_column_shieldless",
            () -> new DirectionalPillarBlock(getShieldlessAncientRockProperties().lightLevel((blockState) -> { return 3;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_LANTERN = register("ancient_lantern_shieldless",
            () -> new Block(getShieldlessAncientRockProperties().lightLevel((blockState) -> { return 13;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_SHELF = register("ancient_shelf_shieldless",
            () -> new Block(getShieldlessAncientRockProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_WINDOW = register("ancient_window_shieldless",
            () -> new TransparentBlock(getShieldlessAncientRockProperties().noOcclusion().isViewBlocking(BlockRegistry::never).isSuffocating(BlockRegistry::never).isValidSpawn(BlockRegistry::never).isRedstoneConductor(BlockRegistry::never)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_GLYPH_STONE_BLANK = register("ancient_glyph_stone_blank_shieldless",
            () -> new Block(getShieldlessAncientRockProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_GLYPH_STONE_ESSENCE = register("ancient_glyph_stone_essence_shieldless",
            () -> new Block(getShieldlessAncientRockProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> SHIELDLESS_ANCIENT_GLYPH_STONE_MAKUTUIN = register("ancient_glyph_stone_makutuin_shieldless",
            () -> new Block(getShieldlessAncientRockProperties()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Obsidian Deco
    public static final Supplier<Block> POLISHED_OBSIDIAN = register("polished_obsidian",
            () -> new PolishedObsidian(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> POLISHED_OBSIDIAN_COLUMN = register("polished_obsidian_column",
            () -> new PolishedObsidianColumn(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> ENGRAVED_POLISHED_OBSIDIAN = register("engraved_polished_obsidian",
            () -> new PolishedObsidian(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> POLISHED_OBSIDIAN_BRICKS = register("polished_obsidian_bricks",
            () -> new PolishedObsidian(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> POLISHED_OBSIDIAN_TRACT = register("polished_obsidian_tract",
            () -> new PolishedObsidianColumn(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> POLISHED_OBSIDIAN_TILES = register("polished_obsidian_tiles",
            () -> new PolishedObsidian(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> OBSIDIAN_FRAMED_GLASS = register("obsidian_framed_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).noOcclusion().isViewBlocking(BlockRegistry::never).isSuffocating(BlockRegistry::never).isValidSpawn(BlockRegistry::never).isRedstoneConductor(BlockRegistry::never)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> AETHER_RUNE = register("aether_rune",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> LIGHT_FIXTURE = register("essence_light_fixture",
            () -> new LightFixture(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .instabreak()
                    .noOcclusion()
                    .lightLevel((blockState) -> { return 15;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Copper Deco
    public static final Supplier<Block> PATTERNED_COPPER = register("patterned_copper",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> COPPER_VENT = register("copper_vent",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> FLUIDIC_GLASS = register("fluidic_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Ecliptrum Deco
    public static final Supplier<Block> ECLIPTRUM_BLOCK = register("ecliptrum_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_YELLOW)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Misc Deco
    public static final Supplier<Block> FLARE_LIGHT = registerBlock("flare_light",
            () -> new FlareLight(
                    BlockBehaviour.Properties.of()
                            .replaceable()
                            .strength(-1.0F, 3600000.8F)
                            .mapColor(MapColor.NONE)
                            .lightLevel((state) -> 15)
                            .noLootTable()
                            .sound(SoundType.AMETHYST)
                            .noOcclusion().instabreak().noCollission()));
    public static final Supplier<Block> DEWLAMP = register("dewlamp",
            () -> new Dewlamp(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).noOcclusion().noCollission().lightLevel((blockState) -> { return 5;})),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> EMPTY_CRYOCHAMBER = register("empty_cryochamber",
            () -> new EmptyCryochamber(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion()),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> EMPTY_CRYOCHAMBER_ROUTER = registerBlock("empty_cryochamber_router",
            () -> new EmptyCryochamberRouter(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion()));
    public static final Supplier<Block> MAKUTUIN_RUNNER = register("makutuin_runner",
            () -> new MakutuinRunner(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_CARPET)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));
    public static final Supplier<Block> MAKUTUIN_RUNNER_CROSS = register("makutuin_runner_cross",
            () -> new MakutuinRunnerCross(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_CARPET)),
            object -> () -> new BlockItem(object.get(), new Item.Properties()));

    // Resource Storage Blocks
    public static final Supplier<Block> BLOCK_OF_INDUSTRY = register("block_of_industry",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .mapColor(MapColor.COLOR_MAGENTA)
                    .lightLevel((state) -> 7)
                    .noOcclusion()),
            object -> () -> new BlockItem(object.get(),
                    new Item.Properties()));
    public static final Supplier<Block> BLOCK_OF_TRANSFORMATION = register("block_of_transformation",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .mapColor(MapColor.COLOR_YELLOW)
                    .lightLevel((state) -> 7)
                    .noOcclusion()),
            object -> () -> new BlockItem(object.get(),
                    new Item.Properties()));


    private static <T extends Block> Supplier<T> registerBlock(final String name,
                                                                     final Supplier<? extends T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> Supplier<T> register(final String name, final Supplier<? extends T> block,
                                                                Function<Supplier<T>, Supplier<? extends Item>> item) {
        Supplier<T> obj = registerBlock(name, block);
        ITEMS.register(name, item.apply(obj));
        return obj;
    }

    public static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
        return false;
    }

    public static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }

    public static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return true;
    }
}
