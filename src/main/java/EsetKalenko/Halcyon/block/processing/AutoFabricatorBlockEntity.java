package EsetKalenko.Halcyon.block.processing;

import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.registry.*;
import com.cmdpro.databank.model.animation.DatabankAnimationReference;
import com.cmdpro.databank.model.animation.DatabankAnimationState;
import EsetKalenko.Halcyon.api.DataNEssenceRegistries;
import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.EssenceType;
import EsetKalenko.Halcyon.api.essence.container.MultiEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.misc.ILockableContainer;
import EsetKalenko.Halcyon.client.FactorySong;
import EsetKalenko.Halcyon.item.DataDrive;
import EsetKalenko.Halcyon.api.LockableItemHandler;
import EsetKalenko.Halcyon.recipe.IFabricationRecipe;
import EsetKalenko.Halcyon.recipe.IHasRequiredKnowledge;
import EsetKalenko.Halcyon.recipe.NonMenuCraftingContainer;
import EsetKalenko.Halcyon.screen.AutoFabricatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class AutoFabricatorBlockEntity extends BlockEntity implements MenuProvider, ILockableContainer, EssenceBlockEntity, Machine {
    public DatabankAnimationState animState = new DatabankAnimationState("idle")
            .addAnim(new DatabankAnimationReference("idle", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("crafting", (state, anim) -> {}, (state, anim) -> {}));
    private final LockableItemHandler itemHandler = new LockableItemHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            checkRecipes();
        }

        @Override
        public void setLockedSlots() {
            super.setLockedSlots();
            setChanged();
        }

        @Override
        public void setLocked(boolean locked) {
            super.setLocked(locked);
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return super.isItemValid(slot, stack);
        }
    };
    private final ItemStackHandler dataDriveHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            checkRecipes();
        }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof DataDrive;
        }
    };
    private final ItemStackHandler outputItemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public void drops() {
        SimpleContainer inventory = getInv();

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }


    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        animState.setLevel(level);
    }
    

    public IItemHandler getItemHandler() {
        return itemHandler;
    }
    public IItemHandler getDataDriveHandler() {
        return dataDriveHandler;
    }
    public IItemHandler getOutputHandler() {
        return outputItemHandler;
    }
    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler, dataDriveHandler, outputItemHandler);
    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }

    public AutoFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.AUTO_FABRICATOR.get(), pos, state);
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
        craftingProgress = tag.getInt("craftingProgress");
        itemHandler.deserializeNBT(pRegistries, tag.getCompound("itemHandler"));
        maxTime = tag.getInt("maxTime");
        if (tag.contains("essenceCost")) {
            CompoundTag cost = tag.getCompound("essenceCost");
            essenceCost = new HashMap<>();
            for (String i : cost.getAllKeys()) {
                ResourceLocation location = ResourceLocation.tryParse(i);
                essenceCost.put(location, cost.getFloat(i));
            }
        } else {
            essenceCost = null;
        }
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.put("item", item.saveOptional(pRegistries));
        tag.putInt("craftingProgress", craftingProgress);
        tag.put("itemHandler", itemHandler.serializeNBT(pRegistries));
        tag.putInt("maxTime", maxTime);
        if (essenceCost != null) {
            CompoundTag cost = new CompoundTag();
            for (Map.Entry<ResourceLocation, Float> i : essenceCost.entrySet()) {
                cost.putFloat(i.getKey().toString(), i.getValue());
            }
            tag.put("essenceCost", cost);
        }
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inventory", itemHandler.serializeNBT(pRegistries));
        tag.put("inventoryOutput", outputItemHandler.serializeNBT(pRegistries));
        tag.put("inventoryDrive", dataDriveHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("craftingProgress", craftingProgress);
        super.saveAdditional(tag, pRegistries);
    }
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventory"));
        outputItemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventoryOutput"));
        dataDriveHandler.deserializeNBT(pRegistries, nbt.getCompound("inventoryDrive"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        craftingProgress = nbt.getInt("craftingProgress");
    }
    public ItemStack item;
    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+outputItemHandler.getSlots()+dataDriveHandler.getSlots());
        for (int i = 0; i < dataDriveHandler.getSlots(); i++) {
            inventory.setItem(i, dataDriveHandler.getStackInSlot(i));
        }
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i+dataDriveHandler.getSlots(), itemHandler.getStackInSlot(i));
        }
        for (int i = 0; i < outputItemHandler.getSlots(); i++) {
            inventory.setItem(i+dataDriveHandler.getSlots()+itemHandler.getSlots(), outputItemHandler.getStackInSlot(i));
        }
        return inventory;
    }
    public CraftingContainer getCraftingInv() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            items.add(itemHandler.getStackInSlot(i));
        }
        CraftingContainer inventory = new NonMenuCraftingContainer(items, 3, 3);
        return inventory;
    }
    public boolean tryCraft() {
        if (recipe instanceof IHasRequiredKnowledge recipe) {
            if (dataDriveHandler.getStackInSlot(0).has(DataComponentRegistry.DATA_ID) && dataDriveHandler.getStackInSlot(0).has(DataComponentRegistry.DATA_INCOMPLETE)) {
                if (recipe.getEntry().equals(DataDrive.getEntryId(dataDriveHandler.getStackInSlot(0)))) {
                    if (DataDrive.getEntryCompletionStage(dataDriveHandler.getStackInSlot(0)) < recipe.getCompletionStage()) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        CraftingInput craftingInput = getCraftingInv().asCraftInput();
        ItemStack stack = recipe.assemble(craftingInput, level.registryAccess()).copy();
        if (!recipe.matches(craftingInput, level)) {
            return false;
        }
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(craftingInput);
        int left = getCraftingInv().asPositionedCraftInput().left();
        int top = getCraftingInv().asPositionedCraftInput().top();
        int craftWidth = 3;
        for (int k = 0; k < craftingInput.height(); k++) {
            for (int l = 0; l < craftingInput.width(); l++) {
                int i1 = l + left + (k + top) * craftWidth;
                ItemStack existingItem = itemHandler.getStackInSlot(i1);
                ItemStack remainderItem = remaining.get(l + k * craftingInput.width());
                if (!existingItem.isEmpty()) {
                    itemHandler.extractItem(i1, 1, false);
                    ItemEntity entity = new ItemEntity(level, (float) getBlockPos().getX() + 0.5f, (float) getBlockPos().getY() + 1f, (float) getBlockPos().getZ() + 0.5f, remainderItem);
                    level.addFreshEntity(entity);
                }
            }
        }
        outputItemHandler.insertItem(0, stack, false);
        level.playSound(null, worldPosition, SoundRegistry.AUTO_FABRICATOR_CRAFT.value(), SoundSource.BLOCKS, 2, 1);
        return true;
    }
    public Recipe<CraftingInput> recipe;
    public boolean enoughEssence;
    public Map<ResourceLocation, Float> essenceCost;
    public int craftingProgress;
    public int maxTime;
    public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(
            RecipeType<T> type, Level level, I input
    ) {
        return level.getRecipeManager().getRecipesFor(type, input, level).stream().filter(a -> {
            if (a.value() instanceof IHasRequiredKnowledge recipe) {
                return recipe.getEntry().equals(DataDrive.getEntryId(dataDriveHandler.getStackInSlot(0)));
            }
            return true;
        }).findFirst();
    }
    public void checkRecipes() {
        Optional<RecipeHolder<IFabricationRecipe>> fabricationRecipe = getRecipeFor(RecipeRegistry.FABRICATIONCRAFTING.get(), level, getCraftingInv().asCraftInput());
        Recipe<CraftingInput> recipe = null;
        if (fabricationRecipe.isEmpty()) {
            Optional<RecipeHolder<CraftingRecipe>> vanillaRecipe = getRecipeFor(RecipeType.CRAFTING, level, getCraftingInv().asCraftInput());
            if (vanillaRecipe.isPresent()) {
                recipe = vanillaRecipe.get().value();
            }
        } else {
            recipe = fabricationRecipe.get().value();
        }
        if (recipe != null) {
            this.recipe = recipe;
            if (recipe instanceof IFabricationRecipe recipe2) {
                essenceCost = recipe2.getEssenceCost();
                maxTime = recipe2.getTime();
            } else {
                essenceCost = new HashMap<>();
                maxTime = 20;
            }
            item = recipe.getResultItem(level.registryAccess());
        } else {
            this.recipe = null;
            essenceCost = null;
            item = ItemStack.EMPTY;
            maxTime = 20;
        }
    }
    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            checkRecipes();
        }
    }

    public boolean areSlotsLocked() {
        for ( var lockedSlot : getLockable() ) {
            if (!lockedSlot.locked)
                return false;
        }
        return true;
    }

    public static void tick(Level world, BlockPos pos, BlockState state, AutoFabricatorBlockEntity fabricator) {

        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && fabricator.craftingProgress < 0,
                    fabricator, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(fabricator, List.of(EssenceTypeRegistry.ESSENCE.get(), EssenceTypeRegistry.LUNAR_ESSENCE.get(), EssenceTypeRegistry.NATURAL_ESSENCE.get(), EssenceTypeRegistry.EXOTIC_ESSENCE.get()));

            if (!fabricator.areSlotsLocked())
                return;

            BufferUtil.getItemsFromBuffersBelow(fabricator, fabricator.itemHandler);

            if (world.hasNeighborSignal(pos))
                return;

            if (fabricator.recipe != null && hasNotReachedStackLimit(fabricator, fabricator.recipe.getResultItem(world.registryAccess()))) {
                boolean enoughEssence = true;

                for (Map.Entry<ResourceLocation, Float> i : fabricator.essenceCost.entrySet()) {
                    EssenceType type = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(i.getKey());
                    if (fabricator.storage.getEssence(type) < i.getValue()) {
                        enoughEssence = false;
                    }
                }
                fabricator.enoughEssence = enoughEssence;

                if (fabricator.recipe.matches(fabricator.getCraftingInv().asCraftInput(), fabricator.level)) {

                    if (!isRecipeComplete(fabricator))
                        return;

                    fabricator.craftingProgress++;
                    for (Map.Entry<ResourceLocation, Float> i : fabricator.essenceCost.entrySet()) {
                        EssenceType type = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(i.getKey());
                        fabricator.storage.removeEssence(type, i.getValue()/fabricator.maxTime);
                    }
                } else {
                    fabricator.craftingProgress = -1;
                }

            } else {
                fabricator.craftingProgress = -1;
            }
            if (fabricator.craftingProgress >= fabricator.maxTime) {
                fabricator.tryCraft();
                fabricator.craftingProgress = 0;
            }
            fabricator.updateBlock();
        } else {
            if (fabricator.craftingProgress >= 0 && fabricator.essenceCost != null) {
                if ( fabricator.essenceCost.containsKey(DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.getKey(EssenceTypeRegistry.ESSENCE.get())) )
                    ClientHandler.markIndustrialFactorySong(pos);
            }
        }
    }

    /**
     * Returns whether all of the locked slots in this machine are filled with an item
     */
    private static boolean isRecipeComplete(AutoFabricatorBlockEntity fabber) {
        boolean areAllSlotsFilled = true;

        for ( var slotIndex : fabber.itemHandler.lockedSlots.keySet() ) {
            for (var slot : fabber.getLockable()) {
                if ( slot.locked && slot.getStackInSlot(slotIndex).isEmpty() )
                    areAllSlotsFilled = false;
            }
        }

        return areAllSlotsFilled;
    }

    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);
        this.setChanged();
    }

    private static boolean hasNotReachedStackLimit(AutoFabricatorBlockEntity entity, ItemStack toAdd) {
        if (toAdd.is(entity.outputItemHandler.getStackInSlot(0).getItem())) {
            return entity.outputItemHandler.getStackInSlot(0).getCount() + toAdd.getCount() <= entity.outputItemHandler.getStackInSlot(0).getMaxStackSize();
        } else return entity.outputItemHandler.getStackInSlot(0).isEmpty();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new AutoFabricatorMenu(pContainerId, pInventory, this);
    }

    @Override
    public List<LockableItemHandler> getLockable() {
        return List.of(itemHandler);
    }

    public MultiEssenceContainer storage = new MultiEssenceContainer(List.of(EssenceTypeRegistry.ESSENCE.get(), EssenceTypeRegistry.LUNAR_ESSENCE.get(), EssenceTypeRegistry.NATURAL_ESSENCE.get(), EssenceTypeRegistry.EXOTIC_ESSENCE.get()), 1000);
    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    private static class ClientHandler {
        static FactorySong.FactoryLoop industrialSound = FactorySong.getLoop(SoundRegistry.AUTO_FABRICATOR_LOOP_INDUSTRIAL.value());

        public static void markIndustrialFactorySong(BlockPos pos) {
            industrialSound.addSource(pos);
        }
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN);
    }
}
