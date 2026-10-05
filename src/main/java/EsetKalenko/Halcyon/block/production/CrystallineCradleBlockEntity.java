package EsetKalenko.Halcyon.block.production;

import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.HalcyonTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CrystallineCradleBlockEntity extends BlockEntity implements EssenceBlockEntity {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 2000);
    private final float breakCost = 25f;
    public int interval;
    public final float destroyRange = 6f;
    public final int maxDestroyTicks = 30;
    public final int visualMinTicks = 15;
    public int destroyTicks;
    public int visualDestroyTicks;
    // sixth note on every line does not play; but they are needed for positioning
    private final int[] notes = {6, 8, 10, 13, 18, 0,
                                 18, 13, 17, 18, 20, 0};
    private final int[] chords = {6, 0, 0, 0, 0, 0,
                                  6, 0, 0, 0, 0, 0};
    private int currentNote;

    public CrystallineCradleBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.CRYSTALLINE_CRADLE.get(), pPos, pBlockState);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries){
        CompoundTag tag = pkt.getTag();
        visualDestroyTicks = tag.getInt("destroyTicks");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("destroyTicks", destroyTicks);
        return tag;
    }

    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);
        this.setChanged();
    }

    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, CrystallineCradleBlockEntity tile) {
        if (!world.isClientSide()) {
            if (!world.hasNeighborSignal(pos)) {
                if (tile.interval <= 0) {
                    tile.destroyTicks = 0;
                    tile.updateBlock();
                    tile.interval = 100;
                } else {
                    tile.interval--;
                }
            }
            if (tile.destroyTicks < tile.maxDestroyTicks) {
                float range = tile.getDestroyRange();
                int rangeMax = (int)Math.ceil(tile.destroyRange);
                for (BlockPos i : BlockPos.betweenClosed(pos.offset(-rangeMax, -rangeMax, -rangeMax), pos.offset(rangeMax, rangeMax, rangeMax))) {
                    Vec3 furthest = i.getCenter();
                    furthest = furthest.add(furthest.vectorTo(pos.getCenter()).reverse().scale(0.2f));
                    Vec3 closest = i.getCenter();
                    closest = closest.add(closest.vectorTo(pos.getCenter()).scale(0.2f));
                    if (closest.distanceTo(pos.getCenter()) <= range && furthest.distanceTo(pos.getCenter()) >= range) {
                        BlockState breakState = world.getBlockState(i);
                        if (breakState.is(HalcyonTags.Blocks.CRYSTALLINE_CRADLE_BREAKABLE)) {
                            if (tile.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= tile.breakCost) {
                                world.destroyBlock(i, true);
                                tile.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), tile.breakCost);
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            if (tile.destroyTicks < tile.maxDestroyTicks) {
                tile.destroyTicks++;
                tile.playNote(world, pos, tile);
            }
        } else {
            if (tile.visualDestroyTicks < tile.maxDestroyTicks) {
                tile.visualDestroyTicks++;
            }
        }
    }

    public void playNote(Level world, BlockPos pos, CrystallineCradleBlockEntity tile) {
        if (tile.destroyTicks % 5 == 0) {

            var noteIndex = tile.notes[tile.currentNote];
            var chordIndex = tile.chords[tile.currentNote];
            if (noteIndex != 0)
                world.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1f-((float)tile.destroyTicks/(float)tile.maxDestroyTicks), NoteBlock.getPitchFromNote(noteIndex));
            if (chordIndex != 0)
                world.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1f-((float)tile.destroyTicks/(float)tile.maxDestroyTicks), NoteBlock.getPitchFromNote(chordIndex));
            tile.currentNote++;
            if (tile.currentNote >= tile.notes.length)
                tile.currentNote = 0;
        }
    }

    public float getDestroyRange() {
        return ((float)destroyTicks/(float)maxDestroyTicks)*destroyRange;
    }

    public float getVisualDestroyRange(float partialTick) {
        return (((float)visualDestroyTicks+partialTick)/(float)maxDestroyTicks)*destroyRange;
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
}
