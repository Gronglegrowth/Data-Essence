package EsetKalenko.Halcyon.datagen.loot;

import EsetKalenko.Halcyon.registry.BlockRegistry;
import EsetKalenko.Halcyon.registry.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

public class HalcyonBlockLoot extends BlockLootSubProvider {
    public HalcyonBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(BlockRegistry.FABRICATOR.get());
        dropSelf(BlockRegistry.LUNARIUM.get());
        dropSelf(BlockRegistry.ESSENCE_POINT.get());
        dropSelf(BlockRegistry.LUNAR_ESSENCE_POINT.get());
        dropSelf(BlockRegistry.NATURAL_ESSENCE_POINT.get());
        dropSelf(BlockRegistry.EXOTIC_ESSENCE_POINT.get());
        dropSelf(BlockRegistry.ESSENCE_BUFFER.get());
        dropSelf(BlockRegistry.ITEM_BUFFER.get());
        dropSelf(BlockRegistry.FLUID_BUFFER.get());
        dropSelf(BlockRegistry.ITEM_POINT.get());
        dropSelf(BlockRegistry.FLUID_POINT.get());
        dropSelf(BlockRegistry.RF_NODE.get());
        dropSelf(BlockRegistry.ESSENCE_CRYSTAL.get());
        dropSelf(BlockRegistry.ESSENCE_BURNER.get());
        dropSelf(BlockRegistry.DECO_ESSENCE_BUFFER.get());
        dropSelf(BlockRegistry.DECO_ITEM_BUFFER.get());
        dropSelf(BlockRegistry.DECO_FLUID_BUFFER.get());
        dropSelf(BlockRegistry.COPPER_VENT.get());
        this.add(BlockRegistry.ANCIENT_DATA_BANK.get(),
                block -> noDrop());
        this.add(BlockRegistry.COMPUTER.get(),
                block -> noDrop());
        this.add(BlockRegistry.CRYOCHAMBER.get(),
                block -> noDrop());
        this.add(BlockRegistry.CRYOCHAMBER_FILLER.get(),
                block -> noDrop());
        this.add(BlockRegistry.EMPTY_CRYOCHAMBER.get(),
                block -> noDrop());
        this.add(BlockRegistry.EMPTY_CRYOCHAMBER_ROUTER.get(),
                block -> noDrop());
        this.add(BlockRegistry.ESSENCE_CRYSTAL.get(), block -> createEssenceCrystalDrops(block, ItemRegistry.ESSENCE_SHARD.get()));
        this.add(BlockRegistry.CRYSTAL_OF_TRANSFORMATION.get(), block -> createEssenceCrystalDrops(block, ItemRegistry.SHARD_OF_TRANSFORMATION.get()));
        dropOther(BlockRegistry.ANCIENT_ROCK_COLUMN.get(), BlockRegistry.SHIELDLESS_ANCIENT_ROCK_COLUMN.get().asItem());
        dropOther(BlockRegistry.ANCIENT_ROCK_BRICKS.get(), BlockRegistry.SHIELDLESS_ANCIENT_ROCK_BRICKS.get().asItem());
        dropOther(BlockRegistry.ANCIENT_ROCK_TILES.get(), BlockRegistry.SHIELDLESS_ANCIENT_ROCK_TILES.get().asItem());
        dropOther(BlockRegistry.ENERGIZED_ANCIENT_ROCK_COLUMN.get(), BlockRegistry.SHIELDLESS_ENERGIZED_ANCIENT_ROCK_COLUMN.get().asItem());
        dropOther(BlockRegistry.ANCIENT_LANTERN.get(), BlockRegistry.SHIELDLESS_ANCIENT_LANTERN.get().asItem());
        dropOther(BlockRegistry.ANCIENT_SHELF.get(), BlockRegistry.SHIELDLESS_ANCIENT_SHELF.get().asItem());
        dropOther(BlockRegistry.ANCIENT_WINDOW.get(), BlockRegistry.SHIELDLESS_ANCIENT_WINDOW.get().asItem());
        dropOther(BlockRegistry.ANCIENT_GLYPH_STONE_BLANK.get(), BlockRegistry.SHIELDLESS_ANCIENT_GLYPH_STONE_BLANK.get().asItem());
        dropOther(BlockRegistry.ANCIENT_GLYPH_STONE_MAKUTUIN.get(), BlockRegistry.SHIELDLESS_ANCIENT_GLYPH_STONE_MAKUTUIN.get().asItem());
        dropOther(BlockRegistry.ANCIENT_GLYPH_STONE_ESSENCE.get(), BlockRegistry.SHIELDLESS_ANCIENT_GLYPH_STONE_ESSENCE.get().asItem());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_ROCK_COLUMN.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_ROCK_BRICKS.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_ROCK_TILES.get());
        dropSelf(BlockRegistry.SHIELDLESS_ENERGIZED_ANCIENT_ROCK_COLUMN.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_LANTERN.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_SHELF.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_WINDOW.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_GLYPH_STONE_BLANK.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_GLYPH_STONE_MAKUTUIN.get());
        dropSelf(BlockRegistry.SHIELDLESS_ANCIENT_GLYPH_STONE_ESSENCE.get());
        dropSelf(BlockRegistry.POLISHED_OBSIDIAN.get());
        dropSelf(BlockRegistry.POLISHED_OBSIDIAN_BRICKS.get());
        dropSelf(BlockRegistry.POLISHED_OBSIDIAN_TRACT.get());
        dropSelf(BlockRegistry.POLISHED_OBSIDIAN_TILES.get());
        dropSelf(BlockRegistry.POLISHED_OBSIDIAN_COLUMN.get());
        dropSelf(BlockRegistry.ENGRAVED_POLISHED_OBSIDIAN.get());
        dropSelf(BlockRegistry.TRAVERSITE_ROAD.get());
        dropSelf(BlockRegistry.ESSENCE_BATTERY.get());
        dropSelf(BlockRegistry.LUNAR_ESSENCE_BATTERY.get());
        dropSelf(BlockRegistry.NATURAL_ESSENCE_BATTERY.get());
        dropSelf(BlockRegistry.EXOTIC_ESSENCE_BATTERY.get());
        dropSelf(BlockRegistry.INFUSER.get());
        dropSelf(BlockRegistry.PATTERNED_COPPER.get());
        dropSelf(BlockRegistry.FLUID_COLLECTOR.get());
        dropSelf(BlockRegistry.FLUID_SPILLER.get());
        dropSelf(BlockRegistry.CHARGER.get());
        dropSelf(BlockRegistry.LASER_EMITTER.get());
        dropSelf(BlockRegistry.VACUUM.get());
        this.add(BlockRegistry.STRUCTURE_PROTECTOR.get(),
                block -> noDrop());
        dropSelf(BlockRegistry.ESSENCE_LEECH.get());
        this.add(BlockRegistry.LENSING_CRYSTAL_ORE.get(), block -> createLensingCrystalDrops(block, ItemRegistry.LENSING_CRYSTAL.get()));
        dropSelf(BlockRegistry.AUTO_FABRICATOR.get());
        dropSelf(BlockRegistry.FLUID_TANK.get());
        dropSelf(BlockRegistry.FLUID_MIXER.get());
        dropSelf(BlockRegistry.SYNTHESIS_CHAMBER.get());
        dropSelf(BlockRegistry.FLUID_BOTTLER.get());
        dropSelf(BlockRegistry.ENTROPIC_PROCESSOR.get());
        dropSelf(BlockRegistry.ESSENCE_FURNACE.get());
        dropSelf(BlockRegistry.ENTICING_LURE.get());
        dropSelf(BlockRegistry.FLUIDIC_GLASS.get());
        dropSelf(BlockRegistry.AETHER_RUNE.get());
        dropSelf(BlockRegistry.MINERAL_PURIFICATION_CHAMBER.get());
        dropSelf(BlockRegistry.ITEM_FILTER.get());
        dropSelf(BlockRegistry.ESSENCE_BREAKER.get());
        dropSelf(BlockRegistry.LIMITED_ITEM_BUFFER.get());
        dropSelf(BlockRegistry.SPIRE_GLASS.get());
        dropSelf(BlockRegistry.METAL_SHAPER.get());
        dropSelf(BlockRegistry.LIGHT_FIXTURE.get());
        dropSelf(BlockRegistry.CRYSTALLINE_LEAVES.get());
        dropSelf(BlockRegistry.CRYSTALLINE_LOG.get());
        dropSelf(BlockRegistry.INDUSTRIAL_PLANT_SIPHON.get());
        dropSelf(BlockRegistry.MELTER.get());
        dropSelf(BlockRegistry.TRAVERSITE_ROAD_SLAB.get());
        dropSelf(BlockRegistry.TRAVERSITE_ROAD_STAIRS.get());
        dropSelf(BlockRegistry.CHEMICAL_NODE.get());
        dropSelf(BlockRegistry.DEWLAMP.get());
        dropSelf(BlockRegistry.DRYING_TABLE.get());
        dropSelf(BlockRegistry.TRAVERSITE_ROAD_OPAL.get());
        dropSelf(BlockRegistry.TRAVERSITE_ROAD_STAIRS_OPAL.get());
        dropSelf(BlockRegistry.TRAVERSITE_ROAD_SLAB_OPAL.get());
        dropSelf(BlockRegistry.ESSENCE_BRIDGE.get()); // TODO maybe? i just needed to get datagen operational ~Eset
        dropSelf(BlockRegistry.DATA_BANK.get());
        dropSelf(BlockRegistry.ENDER_PEARL_DESTINATION.get());
        dropSelf(BlockRegistry.ENDER_PEARL_RELAY.get());
        dropSelf(BlockRegistry.ENDER_PEARL_CAPTURE.get());
        this.add(BlockRegistry.CREATIVE_ESSENCE_BATTERY.get(),
                block -> noDrop());
        dropSelf(BlockRegistry.OBSIDIAN_FRAMED_GLASS.get());
        dropSelf(BlockRegistry.ESSENCE_READER.get());
        dropSelf(BlockRegistry.OVERSEEING_SPIKE.get());
        dropSelf(BlockRegistry.SURVEY_TUNNELER.get());
        dropSelf(BlockRegistry.TWINING_LANTERN.get());
        dropSelf(BlockRegistry.MAKUTUIN_RUNNER.get());
        dropSelf(BlockRegistry.MAKUTUIN_RUNNER_CROSS.get());
        dropSelf(BlockRegistry.ESSENCE_DERIVATION_SPIKE.get());
        dropSelf(BlockRegistry.TIDAL_METEORITE.get());
        dropSelf(BlockRegistry.ECLIPTRUM_BLOCK.get());
        dropSelf(BlockRegistry.CRYSTALLINE_CRADLE.get());
        dropSelf(BlockRegistry.VERMILION_GRASS_BLOCK.get());
        dropSelf(BlockRegistry.SANCTUARY_DIRT.get());
        dropSelf(BlockRegistry.SANCTUARY_SAND.get());
        dropSelf(BlockRegistry.DEEP_SANCTUARY_SAND.get());
        dropSelf(BlockRegistry.CYAN_PEARLESCENT_SPIRAL.get());
        dropSelf(BlockRegistry.MAGENTA_PEARLESCENT_SPIRAL.get());
        dropSelf(BlockRegistry.YELLOW_PEARLESCENT_SPIRAL.get());
        dropSelf(BlockRegistry.RED_PEARLESCENT_SAND.get());
        dropSelf(BlockRegistry.GREEN_PEARLESCENT_SAND.get());
        dropSelf(BlockRegistry.BLUE_PEARLESCENT_SAND.get());
        dropSelf(BlockRegistry.ASTRAL_GOOP.get());
        dropSelf(BlockRegistry.VIVID_LOTUS.get());
        dropSelf(BlockRegistry.BLOCK_OF_INDUSTRY.get());
        dropSelf(BlockRegistry.BLOCK_OF_TRANSFORMATION.get());
        dropSelf(BlockRegistry.SIMPLE_HARVESTER.get());
        this.add(BlockRegistry.TALL_SANCTUARY_GRASS.get(),
                block -> createSpecialGrassDrops(BlockRegistry.TALL_SANCTUARY_GRASS.get(), Items.AIR));
        this.add(BlockRegistry.VERMILION_SPROUT.get(),
                block -> createSpecialGrassDrops(BlockRegistry.VERMILION_SPROUT.get(), Items.AIR));
        this.add(BlockRegistry.CYAN_PEARLESCENT_GRASS.get(),
                block -> createSpecialGrassDrops(BlockRegistry.CYAN_PEARLESCENT_GRASS.get(), Items.AIR));
        this.add(BlockRegistry.MAGENTA_PEARLESCENT_GRASS.get(),
                block -> createSpecialGrassDrops(BlockRegistry.MAGENTA_PEARLESCENT_GRASS.get(), Items.AIR));
        this.add(BlockRegistry.YELLOW_PEARLESCENT_GRASS.get(),
                block -> createSpecialGrassDrops(BlockRegistry.YELLOW_PEARLESCENT_GRASS.get(), Items.AIR));

        this.add(BlockRegistry.TETHERGRASS.get(),
                block -> createTethergrassDrops(block, ItemRegistry.BONDING_POWDER.get()));

        this.add(BlockRegistry.AREKKO.get(),
                block -> createArekkoDrops(BlockRegistry.AREKKO.get(), Items.BONE));
        this.add(BlockRegistry.FROZEN_MOONLIGHT.get(), block -> createFrozenMoonlightDrops(block, ItemRegistry.FROZEN_MOONLIGHT_CHUNK.get()));
    }
    protected LootTable.Builder createSpecialGrassDrops(Block block, Item seeds) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createShearsDispatchTable(
                block,
                (LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
                        block,
                        LootItem.lootTableItem(seeds)
                                .when(LootItemRandomChanceCondition.randomChance(0.125F))
                                .apply(ApplyBonusCount.addUniformBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE), 2))
                )
        );
    }
    protected LootTable.Builder createFrozenMoonlightDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(this.registries.holderOrThrow(Enchantments.FORTUNE)))));
    }
    protected LootTable.Builder createEssenceCrystalDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(this.registries.holderOrThrow(Enchantments.FORTUNE)))));
    }
    protected LootTable.Builder createTethergrassDrops(Block pBlock, Item item) {
        return createSilkTouchOrShearsDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(this.registries.holderOrThrow(Enchantments.FORTUNE)))));
    }
    protected LootTable.Builder createArekkoDrops(Block pBlock, Item item) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))));
    }
    protected LootTable.Builder createLensingCrystalDrops(Block pBlock, Item item) {
        return createSilkTouchDispatchTable(pBlock,
                this.applyExplosionDecay(pBlock,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(this.registries.holderOrThrow(Enchantments.FORTUNE)))));
    }
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream().map((block) -> (Block)block.get()).filter((block) -> {
            if (block instanceof LiquidBlock) {
                return false;
            }
            return true;
        })::iterator;
    }
}
