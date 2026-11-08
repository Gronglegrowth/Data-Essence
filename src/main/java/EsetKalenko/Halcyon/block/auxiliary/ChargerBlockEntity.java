package EsetKalenko.Halcyon.block.auxiliary;

import EsetKalenko.Halcyon.api.block.RedstoneMonitorable;
import com.cmdpro.databank.model.animation.DatabankAnimationReference;
import com.cmdpro.databank.model.animation.DatabankAnimationState;
import EsetKalenko.Halcyon.api.DataNEssenceRegistries;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.EssenceType;
import EsetKalenko.Halcyon.api.essence.container.MultiEssenceContainer;
import EsetKalenko.Halcyon.api.item.ItemEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChargerBlockEntity extends BlockEntity implements EssenceBlockEntity, RedstoneMonitorable {
    public DatabankAnimationState animState = new DatabankAnimationState("idle_empty")
            .addAnim(new DatabankAnimationReference("idle_empty", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("orb_spin", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("orb_rise", (state, anim) -> {}, (state, anim) -> state.setAnim("orb_spin")))
            .addAnim(new DatabankAnimationReference("extend_exciters", (state, anim) -> {}, (state, anim) -> state.setAnim("idle_exciters_out")))
            .addAnim(new DatabankAnimationReference("retract_exciters", (state, anim) -> {}, (state, anim) -> state.setAnim("idle_empty")))
            .addAnim(new DatabankAnimationReference("idle_exciters_out", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("orb_fall", (state, anim) -> {}, (state, anim) -> state.setAnim("idle_exciters_out")));

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        animState.setLevel(level);
    }

    public MultiEssenceContainer storage = new MultiEssenceContainer(List.of(EssenceTypeRegistry.ESSENCE.get(), EssenceTypeRegistry.LUNAR_ESSENCE.get(), EssenceTypeRegistry.NATURAL_ESSENCE.get(), EssenceTypeRegistry.EXOTIC_ESSENCE.get()), 1000);
    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler);
    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }
    public void drops() {
        SimpleContainer inventory = getInv();
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.CHARGER.get(), pos, state);
        item = ItemStack.EMPTY;
    }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries){
        CompoundTag tag = pkt.getTag();
        storage.fromNbt(tag.getCompound("EssenceStorage"));
        item = ItemStack.parseOptional(pRegistries, tag.getCompound("item"));
        charging = tag.getBoolean("charging");
        canCharge = tag.getBoolean("canCharge");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.put("item", item.saveOptional(pRegistries));
        tag.putBoolean("charging", charging);
        tag.putBoolean("canCharge", canCharge);
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inventory", itemHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        super.saveAdditional(tag, pRegistries);
    }
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventory"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
    }
    public ItemStack item;
    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return inventory;
    }
    public float essenceCost;
    public float lunarEssenceCost;
    public float naturalEssenceCost;
    public float exoticEssenceCost;
    public boolean charging;
    public boolean canCharge;
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, ChargerBlockEntity pBlockEntity) {
        if (!pLevel.isClientSide()) {
            pBlockEntity.charging = false;
            pBlockEntity.canCharge = false;
            BufferUtil.getEssenceFromBuffersBelow(pBlockEntity, List.of(EssenceTypeRegistry.ESSENCE.get(), EssenceTypeRegistry.LUNAR_ESSENCE.get(), EssenceTypeRegistry.NATURAL_ESSENCE.get(), EssenceTypeRegistry.EXOTIC_ESSENCE.get()));
            ItemStack stack = pBlockEntity.itemHandler.getStackInSlot(0).copy();
            pBlockEntity.item = stack;
            for (EssenceType i : pBlockEntity.storage.getSupportedEssenceTypes()) {
                ResourceLocation essenceType = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.getKey(i);
                float maxEssence = ItemEssenceContainer.getMaxEssence(stack);
                if (maxEssence > 0 && ItemEssenceContainer.getSupportedEssenceTypes(stack).contains(essenceType)) {
                    float essence = ItemEssenceContainer.getEssence(stack, essenceType);
                    if (essence < maxEssence) {
                        float fill = Math.min(maxEssence - essence, Math.min(5f, pBlockEntity.getStorage().getEssence(i)));
                        pBlockEntity.canCharge = true;
                        if (fill > 0) {
                            ItemEssenceContainer.addEssence(stack, essenceType, fill);
                            pBlockEntity.getStorage().removeEssence(i, fill);
                            pBlockEntity.charging = true;
                        }
                    }
                }
            }
            pBlockEntity.itemHandler.setStackInSlot(0, stack);
            pBlockEntity.updateBlock();
        }
    }
    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);
        this.setChanged();
    }



    /**
     * Checks if the given ItemStack can be inserted into the charger's slot.
     * The slot must be empty AND the item must be chargeable.
     */
    public boolean isItemValidForInsertion(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!this.itemHandler.getStackInSlot(0).isEmpty()) {
            return false;
        }

        for (EssenceType supportedType : storage.getSupportedEssenceTypes()) {
            if (ItemEssenceContainer.getMaxEssence(stack) > 0 && ItemEssenceContainer.getSupportedEssenceTypes(stack).contains(DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.getKey(supportedType))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to insert the player's held item into the charger's slot.
     */
    public InteractionResult tryInsertItem(Player player, ItemStack heldStack) {
        if (!isItemValidForInsertion(heldStack)) {
            return InteractionResult.PASS;
        }

        ItemStack stackToInsert = heldStack.split(1);
        ItemStack remainder = this.itemHandler.insertItem(0, stackToInsert, false);

        if (remainder.isEmpty()) {
            this.updateBlock();
            return InteractionResult.SUCCESS;
        } else {
            heldStack.grow(1);
            return InteractionResult.PASS;
        }
    }

    /**
     * Attempts to extract the item currently in the charger's slot.
     */
    public InteractionResult tryExtractItem(Player player) {
        ItemStack storedStack = this.itemHandler.getStackInSlot(0);

        if (!storedStack.isEmpty()) {
            player.getInventory().placeItemBackInInventory(storedStack);

            this.itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            this.updateBlock();

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getMeterSideLength(Direction direction) {
        if (!charging) {
            if (direction.equals(Direction.UP)) {
                return -0.25f;
            }
        }
        return EssenceBlockEntity.super.getMeterSideLength(direction);
    }

    @Override
    public boolean hasWork() {
        return canCharge;
    }
}