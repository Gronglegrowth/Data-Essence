package EsetKalenko.Halcyon.block.control;

import EsetKalenko.Halcyon.api.block.RedstoneMonitorable;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class OverseeingSpikeBlockEntity extends BlockEntity {
    private int signal;

    public OverseeingSpikeBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.OVERSEEING_SPIKE.get(), pos, blockState);
    }

    public int getSignal() {
        return signal;
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            int newSignal = updateSignal();
            boolean update = newSignal != signal;
            signal = newSignal;
            if (update) {
                getBlockState().setValue(OverseeingSpike.POWERED, signal > 0);
                updateNeighbors();
            }
        }
    }

    private int updateSignal() {
        Direction detectDir = getBlockState().getValue(OverseeingSpike.FACING).getOpposite();
        BlockPos detectingPos = getBlockPos().relative(detectDir);
        if (level.getBlockEntity(detectingPos) instanceof RedstoneMonitorable observed) {
            return observed.hasWork() ? 15 : 0;
        }
        return 0;
    }

    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, Block.UPDATE_ALL);
        this.setChanged();
    }

    private void updateNeighbors() {
        if (!hasLevel()) {
            return;
        }
        getLevel().updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
    }
}
