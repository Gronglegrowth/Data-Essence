package EsetKalenko.Halcyon.block.processing;

import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.screen.EssenceFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import java.util.List;
import java.util.Optional;

public class EssenceFurnaceBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
    public boolean lit;
    public ItemStack item;
    public int workTime;
    public double recipeTime;
    public SmeltingRecipe recipe;
    public float essenceCost;


    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            checkRecipes();
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
    
    

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IItemHandler getOutputHandler() {
        return outputItemHandler;
    }

    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler, outputItemHandler);

    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }

    public EssenceFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ESSENCE_FURNACE.get(), pos, state);
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
        workTime = tag.getInt("workTime");
        recipeTime = tag.getDouble("recipeTime");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.putDouble("recipeTime", recipeTime);
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("input", itemHandler.serializeNBT(pRegistries));
        tag.put("output", outputItemHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.putDouble("recipeTime", recipeTime);
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("input"));
        outputItemHandler.deserializeNBT(pRegistries, nbt.getCompound("output"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        workTime = nbt.getInt("workTime");
        recipeTime = nbt.getDouble("recipeTime");
    }

    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+outputItemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        for (int i = 0; i < outputItemHandler.getSlots(); i++) {
            inventory.setItem(i+itemHandler.getSlots(), outputItemHandler.getStackInSlot(i));
        }
        return inventory;
    }

    public SingleRecipeInput getCraftingInv() {
        SingleRecipeInput inventory = new SingleRecipeInput(itemHandler.getStackInSlot(0));
        return inventory;
    }

    public void checkRecipes() {
        Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, getCraftingInv(), level);
        if (recipe.isPresent()) {
            if (!recipe.get().value().equals(this.recipe)) {
                workTime = 0;
            }
            this.recipe = recipe.get().value();
            recipeTime = recipe.get().value().getCookingTime() * 0.5; // 50% faster than a vanilla Furnace
        } else {
            this.recipe = null;
        }
    }

    public static void tick(Level world, BlockPos pos, BlockState state, EssenceFurnaceBlockEntity furnace) {
        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && furnace.workTime < 0,
                    furnace, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(furnace, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getItemsFromBuffersBelow(furnace, furnace.itemHandler);

            boolean resetWorkTime = true;
            furnace.lit = false;

            if (furnace.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 1 && furnace.recipe != null) {
                ItemStack result = furnace.recipe.getResultItem(world.registryAccess());
                if (furnace.outputItemHandler.insertItem(0, result, true).isEmpty()) {
                    resetWorkTime = false;
                    furnace.lit = true;
                    furnace.workTime++;
                    furnace.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 1);

                    if (furnace.workTime >= furnace.recipeTime) {
                        furnace.outputItemHandler.insertItem(0, furnace.recipe.assemble(furnace.getCraftingInv(), world.registryAccess()), false);
                        furnace.itemHandler.extractItem(0, 1, false);
                        furnace.workTime = 0;
                    }
                }
            }

            if (resetWorkTime) {
                furnace.workTime = -1;
            }

            if (furnace.lit != state.getValue(EssenceFurnace.LIT)) {
                BlockState targetState = state.setValue(EssenceFurnace.LIT, furnace.lit);
                furnace.level.setBlock(pos, targetState, 3);
            }

            furnace.updateBlock();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            checkRecipes();
        }
    }

    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);
        this.setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new EssenceFurnaceMenu(pContainerId, pInventory, this);
    }

    private static boolean hasNotReachedStackLimit(EssenceFurnaceBlockEntity entity, ItemStack toAdd) {
        if (toAdd.is(entity.outputItemHandler.getStackInSlot(0).getItem())) {
            return entity.outputItemHandler.getStackInSlot(0).getCount() + toAdd.getCount() <= entity.outputItemHandler.getStackInSlot(0).getMaxStackSize();
        } else return entity.outputItemHandler.getStackInSlot(0).isEmpty();
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.UP);
    }
}
