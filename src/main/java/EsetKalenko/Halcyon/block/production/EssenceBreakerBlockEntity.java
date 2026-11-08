package EsetKalenko.Halcyon.block.production;

import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class EssenceBreakerBlockEntity extends BlockEntity implements EssenceBlockEntity {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 500);
    private final float breakCost = 25f;
    public int interval;

    public EssenceBreakerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.ESSENCE_BREAKER.get(), pPos, pBlockState);
    }

    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, EssenceBreakerBlockEntity tile) {

        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0,
                    tile, world, pos);

            if (tile.interval <= 0) {
                if (!world.hasNeighborSignal(pos) && tile.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= tile.breakCost) {
                    BlockPos breakPos = pos.relative(state.getValue(EssenceBreaker.FACING));
                    BlockState breakState = world.getBlockState(breakPos);

                    if ((breakState.is(BlockTags.MINEABLE_WITH_PICKAXE) ||
                            breakState.is(BlockTags.MINEABLE_WITH_AXE) ||
                            breakState.is(BlockTags.MINEABLE_WITH_HOE) ||
                            breakState.is(BlockTags.MINEABLE_WITH_SHOVEL)) &&
                            !breakState.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL) &&
                            !(breakState.getDestroySpeed(world, breakPos) <= -1.0f)
                    ) {
                        world.destroyBlock(breakPos, true);
                        ((ServerLevel) world).sendParticles(ParticleTypes.EXPLOSION, breakPos.getCenter().x, breakPos.getCenter().y, breakPos.getCenter().z, 1, 0, 0, 0, 0);
                        tile.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), tile.breakCost);
                        tile.interval = 20;
                    }
                }
            } else {
                tile.interval--;
            }
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("BreakInterval", interval);
        super.saveAdditional(tag, provider);
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        storage.fromNbt(tag.getCompound("EssenceStorage"));
        interval = tag.getInt("BreakInterval");
        super.loadAdditional(tag, provider);
    }
    @Override
    public float getMeterSideLength(Direction direction) {
        Direction facing = getBlockState().getValue(EssenceBreaker.FACING);
        if (direction.equals(facing)) {
            return EssenceBlockEntity.super.getMeterSideLength(direction)*1.5f;
        }
        return EssenceBlockEntity.super.getMeterSideLength(direction);
    }
}
