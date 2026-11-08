package EsetKalenko.Halcyon.block.production;

import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class SimpleHarvesterBlockEntity extends BlockEntity implements EssenceBlockEntity {
    SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 2000);
    static final int INTERVAL = 100;
    static final int WORK_COST = 40;
    int workTicks;

    public SimpleHarvesterBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.HARVESTER.get(), pos, blockState);
    }

    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, SimpleHarvesterBlockEntity tile) {
        if ( !world.isClientSide ) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0,
                    tile, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(tile, EssenceTypeRegistry.ESSENCE.get());

            if (world.hasNeighborSignal(pos))
                return;

            if ( tile.workTicks >= INTERVAL ) {
                if ( tile.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= WORK_COST
                        && tile.harvest(world, pos, tile) ) {
                    world.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1, 1);
                    tile.getStorage().removeEssence( EssenceTypeRegistry.ESSENCE.get(), WORK_COST );
                }
                tile.workTicks = 0;
            }
            tile.workTicks++;
        }
        else {
            // particles maybe?
        }
    }

    public boolean harvest(Level world, BlockPos pos, SimpleHarvesterBlockEntity harvester) {
        boolean harvestedSomething = false;

        for (BlockPos queryPos : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 2, 1))) {
            BlockState queryState = world.getBlockState(queryPos);

            if (queryState.getBlock() instanceof CropBlock crop) {
                if ( !crop.isMaxAge(queryState) )
                    continue;

                dropYields(world, queryState, queryPos, harvester);
                world.setBlockAndUpdate(queryPos, crop.getStateForAge(0));
                if ( queryState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) )
                    world.setBlockAndUpdate(queryPos.above(), Blocks.AIR.defaultBlockState());

                harvestedSomething = true;
            }

            if (queryState.getBlock() instanceof NetherWartBlock) {
                if ( queryState.getValue( NetherWartBlock.AGE ) != 3 )
                    continue;

                dropYields(world, queryState, queryPos, harvester);
                world.setBlockAndUpdate(queryPos, queryState.setValue(NetherWartBlock.AGE, 0));
                harvestedSomething = true;
            }
        }

        return harvestedSomething;
    }

    public void dropYields(Level world, BlockState state, BlockPos pos, SimpleHarvesterBlockEntity harvester) {
        List<ItemStack> drops = Block.getDrops(state, (ServerLevel) world, pos, harvester);

        // replanting is not free
        for ( ItemStack item : drops ) {
            if ( item.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == state.getBlock() ) {
                item.shrink(1);
                break;
            }
        }

        drops.forEach( drop -> {
            if (drop.isEmpty())
                return;
            world.addFreshEntity( new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), drop) );
        } );
    }



    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        storage.fromNbt(tag.getCompound("EssenceStorage"));
        workTicks = tag.getInt("WorkTicks");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("WorkTicks", workTicks);
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("WorkTicks", workTicks);
        super.saveAdditional(tag, registries);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        storage.fromNbt(tag.getCompound("EssenceStorage"));
        workTicks = tag.getInt("WorkTicks");
    }
}
