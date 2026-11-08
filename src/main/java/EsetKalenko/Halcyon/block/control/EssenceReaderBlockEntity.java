package EsetKalenko.Halcyon.block.control;

import EsetKalenko.Halcyon.datamaps.HalcyonDatamaps;
import com.cmdpro.databank.rendering.ColorUtil;
import EsetKalenko.Halcyon.api.DataNEssenceRegistries;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.EssenceType;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EssenceReaderBlockEntity extends BlockEntity {
    public Set<EssenceType> selectedEssences;
    public Item selectedShard; // for purposes of the renderer, because I have no idea how to do this otherwise ~Eset
    private int signal;

    public EssenceReaderBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntityRegistry.ESSENCE_READER.get(), pPos, pBlockState);
    }

    public boolean selectFromItem(ItemStack stack) {
        if (stack == null) {
            selectedEssences = null;
            selectedShard = null;
            updateBlock();
            return true;
        } else {
            if (isItemValid(stack)) {
                var sublimationData = stack.getItemHolder().getData(HalcyonDatamaps.SHARD_SUBLIMATION);

                selectedEssences = sublimationData.energyContained()
                        .keySet().stream()
                        .map(DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY::get)
                        .collect(Collectors.toSet());
                selectedShard = stack.getItem();
                updateBlock();
                return true;
            }
        }
        return false;
    }

    public boolean isItemValid(ItemStack stack) {
        if (stack.getItemHolder().getData(HalcyonDatamaps.SHARD_SUBLIMATION) != null) {
            return true;
        }
        return false;
    }

    public Color getColor() {
        if (selectedEssences == null || selectedEssences.isEmpty()) {
            return new Color(0x413257);
        }
        Color color = new Color(selectedEssences.stream().findFirst().orElseThrow().color);
        for (EssenceType i : selectedEssences) {
            Color newColor = new Color(i.color);
            if (!newColor.equals(color)) {
                color = ColorUtil.blendColors(color, newColor, 0.5f);
            }
        }
        return color;
    }

    public Set<EssenceType> getTypesToCheck(EssenceStorage storage) {
        if (selectedEssences != null) {
            return selectedEssences;
        }
        return storage.getSupportedEssenceTypes();
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

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (!pLevel.isClientSide) {
            int newSignal = updateSignal();
            boolean update = newSignal != signal;
            signal = newSignal;
            if (update) {
                updateNeighbors();
            }
        }
    }

    public int getSignal() {
        return signal;
    }

    private int updateSignal() {
        Direction detectDir = getBlockState().getValue(EssenceReader.FACING).getOpposite();
        BlockPos detectingPos = getBlockPos().relative(detectDir);
        if (level.getBlockEntity(detectingPos) instanceof EssenceBlockEntity essenceEntity) {
            EssenceStorage storage = essenceEntity.getStorage();
            float essence = 0;
            float maxEssence = 0;
            Set<EssenceType> typesToCheck = getTypesToCheck(storage);
            for (EssenceType i : typesToCheck) {
                essence += storage.getEssence(i);
                maxEssence += storage.getMaxEssence();
            }
            float amount = essence / maxEssence;
            return (int) Math.floor(amount * 15f);
        }
        return 0;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries){
        CompoundTag tag = pkt.getTag();
        handleData(tag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        handleData(tag);
    }

    public void handleData(CompoundTag tag) {
        if (tag.contains("selected")) {
            ListTag selected = tag.getList("selected", CompoundTag.TAG_STRING);
            List<EssenceType> types = new ArrayList<>();
            for (Tag i : selected) {
                StringTag str = (StringTag)i;
                types.add(DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(ResourceLocation.tryParse(str.getAsString())));
            }
            this.selectedEssences = new HashSet<>(types);
        } else {
            this.selectedEssences = null;
        }
        updateBlock();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        if (selectedEssences != null) {
            ListTag list = new ListTag();
            for (EssenceType i : selectedEssences) {
                ResourceLocation location = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.getKey(i);
                if (location != null) {
                    list.add(StringTag.valueOf(location.toString()));
                }
            }
            tag.put("selected", list);
        }
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        if (selectedEssences != null) {
            ListTag list = new ListTag();
            for (EssenceType i : selectedEssences) {
                ResourceLocation location = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.getKey(i);
                if (location != null) {
                    list.add(StringTag.valueOf(location.toString()));
                }
            }
            tag.put("selected", list);
        }
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        if (nbt.contains("selected")) {
            ListTag selected = nbt.getList("selected", CompoundTag.TAG_STRING);
            List<EssenceType> types = new ArrayList<>();
            for (Tag i : selected) {
                StringTag str = (StringTag)i;
                types.add(DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(ResourceLocation.tryParse(str.getAsString())));
            }
            this.selectedEssences = new HashSet<>(types);
        } else {
            this.selectedEssences = null;
        }
    }
}
