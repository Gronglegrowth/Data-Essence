package EsetKalenko.Halcyon.block.processing;

import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.recipe.DryingRecipe;
import EsetKalenko.Halcyon.recipe.RecipeInputWithFluid;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.RecipeRegistry;
import EsetKalenko.Halcyon.screen.DryingTableMenu;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DryingTableBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {

    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
    public int workTime;
    public int maxWorkTime;
    public DryingRecipe recipe;

    private final FluidTank fluidHandler = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            checkRecipe();
            updateBlock();
        }
    };
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            checkRecipe();
            updateBlock();
        }
    };
    private final ItemStackHandler outputItemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
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

    public DryingTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.DRYING_TABLE.get(), pos, blockState);
    }

    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DryingTableMenu(containerId, playerInventory, this);
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

    public RecipeInputWithFluid getCraftingInv() {
        RecipeInput inventory = new SingleRecipeInput(itemHandler.getStackInSlot(0));
        List<FluidStack> fluids = new ArrayList<>();
        for (int i = 0; i < fluidHandler.getTanks(); i++) {
            fluids.add(fluidHandler.getFluidInTank(i));
        }
        return new RecipeInputWithFluid(inventory, fluids);
    }

    public void drops() {
        SimpleContainer inventory = getInv();

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void checkRecipe() {
        Optional<RecipeHolder<DryingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeRegistry.DRYING_TYPE.get(), getCraftingInv(), level);
        if (recipe.isPresent())
            this.recipe = recipe.get().value();
        if (recipe.isEmpty())
            this.recipe = null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            checkRecipe();
        }
    }

    public static void tick(Level world, BlockPos pos, BlockState state, DryingTableBlockEntity dryingTable) {
        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && dryingTable.workTime < 0,
                    dryingTable, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(dryingTable, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getFluidsFromBuffersBelow(dryingTable, dryingTable.fluidHandler);
            BufferUtil.getItemsFromBuffersBelow(dryingTable, dryingTable.itemHandler);

            if (dryingTable.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 0.5f && dryingTable.recipe != null) {

                ItemStack assembled = dryingTable.recipe.assemble(dryingTable.getCraftingInv(), world.registryAccess());
                if (dryingTable.outputItemHandler.insertItem(0, assembled, true).isEmpty()) {
                    dryingTable.workTime++;
                    dryingTable.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 0.5f);

                    if (dryingTable.workTime >= dryingTable.recipe.getTime()) {
                        dryingTable.fluidHandler.drain(dryingTable.recipe.getInput(), IFluidHandler.FluidAction.EXECUTE);
                        dryingTable.outputItemHandler.insertItem(0, assembled, false);
                        dryingTable.itemHandler.extractItem(0, 1, false);
                        dryingTable.workTime = 0;
                    }
                    dryingTable.updateBlock();
                }
            } else {
                dryingTable.workTime = -1;
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inventory", itemHandler.serializeNBT(pRegistries));
        tag.put("fluids", fluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("output", outputItemHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventory"));
        fluidHandler.readFromNBT(pRegistries, nbt.getCompound("fluids"));
        outputItemHandler.deserializeNBT(pRegistries, nbt.getCompound("output"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        workTime = nbt.getInt("workTime");
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries){
        CompoundTag tag = pkt.getTag();
        storage.fromNbt(tag.getCompound("EssenceStorage"));
        workTime = tag.getInt("workTime");
        itemHandler.deserializeNBT(pRegistries, tag.getCompound("itemHandler"));
        fluidHandler.readFromNBT(pRegistries, tag.getCompound("fluidHandler"));
        outputItemHandler.deserializeNBT(pRegistries, tag.getCompound("outputItemHandler"));
        maxWorkTime = tag.getInt("maxWorkTime");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.put("itemHandler", itemHandler.serializeNBT(pRegistries));
        tag.put("fluidHandler", fluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("outputItemHandler", outputItemHandler.serializeNBT(pRegistries));
        tag.putInt("maxWorkTime", recipe != null ? recipe.getTime() : -1);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);
        this.setChanged();
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.UP);
    }
}
