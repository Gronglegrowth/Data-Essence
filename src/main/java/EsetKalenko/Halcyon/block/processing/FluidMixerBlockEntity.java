package EsetKalenko.Halcyon.block.processing;

import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import com.cmdpro.databank.model.animation.DatabankAnimationReference;
import com.cmdpro.databank.model.animation.DatabankAnimationState;
import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.item.DataDrive;
import EsetKalenko.Halcyon.api.MultiFluidTank;
import EsetKalenko.Halcyon.api.MultiFluidTankNoDuplicateFluids;
import EsetKalenko.Halcyon.recipe.FluidMixingRecipe;
import EsetKalenko.Halcyon.recipe.RecipeInputWithFluid;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.DataComponentRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.RecipeRegistry;
import EsetKalenko.Halcyon.screen.FluidMixerMenu;
import net.minecraft.core.BlockPos;
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

public class FluidMixerBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
    public DatabankAnimationState animState = new DatabankAnimationState("idle")
            .addAnim(new DatabankAnimationReference("idle", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("working", (state, anim) -> {}, (state, anim) -> {}));

    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
    public FluidMixingRecipe recipe;
    public boolean enoughEssence;
    public float essenceCost;
    public int workTime, maxWorkTime;
    public ItemStack item;
    public int age;

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        animState.setLevel(level);
    }

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

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            checkRecipes();
        }
    }

    private final MultiFluidTank fluidHandler = new MultiFluidTankNoDuplicateFluids(List.of(new FluidTank(1000) { @Override protected void onContentsChanged() { checkRecipes(); } }, new FluidTank(1000) { @Override protected void onContentsChanged() { checkRecipes(); } }));

    private final FluidTank outputFluidHandler = new FluidTank(1000);
    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }
    

    public void drops() {
        SimpleContainer inventory = getInv();

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IItemHandler getDataDriveHandler() {
        return dataDriveHandler;
    }

    public IFluidHandler getOutputHandler() {
        return outputFluidHandler;
    }

    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler, dataDriveHandler);

    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }

    public FluidMixerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_MIXER.get(), pos, state);
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
        maxWorkTime = tag.getInt("maxWorkTime");
        itemHandler.deserializeNBT(pRegistries, tag.getCompound("itemHandler"));
        fluidHandler.readFromNBT(pRegistries, tag.getCompound("fluidHandler"));
        outputFluidHandler.readFromNBT(pRegistries, tag.getCompound("outputFluidHandler"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.putInt("maxWorkTime", recipe != null ? recipe.getTime() : -1);
        tag.put("itemHandler", itemHandler.serializeNBT(pRegistries));
        tag.put("fluidHandler", fluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("outputFluidHandler", outputFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inventory", itemHandler.serializeNBT(pRegistries));
        tag.put("inventoryDrive", dataDriveHandler.serializeNBT(pRegistries));
        tag.put("fluids", fluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("outputFluid", outputFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventory"));
        dataDriveHandler.deserializeNBT(pRegistries, nbt.getCompound("inventoryDrive"));
        fluidHandler.readFromNBT(pRegistries, nbt.getCompound("fluids"));
        outputFluidHandler.readFromNBT(pRegistries, nbt.getCompound("outputFluid"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        workTime = nbt.getInt("workTime");
    }

    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+dataDriveHandler.getSlots());
        for (int i = 0; i < dataDriveHandler.getSlots(); i++) {
            inventory.setItem(i, dataDriveHandler.getStackInSlot(i));
        }
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i+dataDriveHandler.getSlots(), itemHandler.getStackInSlot(i));
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

    public void checkRecipes() {
        Optional<RecipeHolder<FluidMixingRecipe>> recipe = level.getRecipeManager().getRecipesFor(RecipeRegistry.FLUID_MIXING_TYPE.get(), getCraftingInv(), level).stream().filter((a) -> a.value().getEntry().equals(DataDrive.getEntryId(dataDriveHandler.getStackInSlot(0)))).findFirst();
        if (recipe.isPresent()) {
            if (!recipe.get().value().equals(this.recipe)) {
                workTime = 0;
            }
            if (dataDriveHandler.getStackInSlot(0).has(DataComponentRegistry.DATA_ID) && dataDriveHandler.getStackInSlot(0).has(DataComponentRegistry.DATA_INCOMPLETE)) {
                if (recipe.get().value().getEntry().equals(DataDrive.getEntryId(dataDriveHandler.getStackInSlot(0))) && DataDrive.getEntryCompletionStage(dataDriveHandler.getStackInSlot(0)) >= recipe.get().value().getCompletionStage()) {
                    this.recipe = recipe.get().value();
                    essenceCost = recipe.get().value().getEssenceCost();
                } else {
                    this.recipe = null;
                }
            } else {
                this.recipe = null;
            }
        } else {
            this.recipe = null;
        }
    }

    public static void tick(Level world, BlockPos pos, BlockState pState, FluidMixerBlockEntity tile) {
        tile.age++;

        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && tile.workTime < 0,
                    tile, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(tile, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getItemsFromBuffersBelow(tile, tile.itemHandler);
            BufferUtil.getFluidsFromBuffersBelow(tile, tile.fluidHandler);
            boolean resetWorkTime = true;
            if (tile.recipe != null) {
                boolean enoughEssence = false;
                if (tile.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= tile.essenceCost/tile.recipe.getTime()) {
                    enoughEssence = true;
                }
                tile.enoughEssence = enoughEssence;
                if (enoughEssence) {
                    FluidStack result = tile.recipe.getOutput();
                    if (tile.outputFluidHandler.fill(result, IFluidHandler.FluidAction.SIMULATE) >= result.getAmount()) {
                        resetWorkTime = false;
                        tile.workTime++;
                        tile.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), tile.essenceCost/tile.recipe.getTime());
                        if (tile.workTime >= tile.recipe.getTime()) {
                            FluidStack input1 = tile.recipe.getInput1();
                            FluidStack input2 = tile.recipe.getInput2();
                            tile.outputFluidHandler.fill(result, IFluidHandler.FluidAction.EXECUTE);
                            tile.itemHandler.extractItem(0, 1, false);
                            tile.fluidHandler.drain(input1, IFluidHandler.FluidAction.EXECUTE);
                            tile.fluidHandler.drain(input2, IFluidHandler.FluidAction.EXECUTE);
                            tile.workTime = 0;
                        }
                    }
                }
            }
            if (resetWorkTime) {
                tile.workTime = -1;
            }
            tile.updateBlock();
        } else {
            Optional<RecipeHolder<FluidMixingRecipe>> recipe = world.getRecipeManager().getRecipeFor(RecipeRegistry.FLUID_MIXING_TYPE.get(), tile.getCraftingInv(), world);
            recipe.ifPresentOrElse(recipeHolder -> tile.recipe = recipeHolder.value(), () -> tile.recipe = null);
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
        return new FluidMixerMenu(pContainerId, pInventory, this);
    }
}
