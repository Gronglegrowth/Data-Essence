package EsetKalenko.Halcyon.block.processing;

import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.recipe.MeltingRecipe;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.RecipeRegistry;
import EsetKalenko.Halcyon.screen.MelterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MelterBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
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
    private final FluidTank outputFluidHandler = new FluidTank(2000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            updateBlock();
        }
    };
    private final FluidTank fuelFluidHandler = new FluidTank(2000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            updateBlock();
            checkRecipes();
        }
    };

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            checkRecipes();
        }
    }
    public void drops() {
        SimpleContainer inventory = getInv();

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    
    
    

    public IItemHandler getItemHandler() {
        return itemHandler;
    }
    public IFluidHandler getOutputHandler() {
        return outputFluidHandler;
    }
    public IFluidHandler getFuelHandler() {
        return fuelFluidHandler;
    }
    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler);
    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }
    public MelterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.MELTER.get(), pos, state);
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
        maxTime = tag.getInt("maxTime");
        outputFluidHandler.readFromNBT(pRegistries, tag.getCompound("outputFluidHandler"));
        fuelFluidHandler.readFromNBT(pRegistries, tag.getCompound("fuelFluidHandler"));
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.putInt("maxTime", recipe == null ? -1 : recipe.getTime());
        tag.put("outputFluidHandler", outputFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("fuelFluidHandler", fuelFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("input", itemHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.put("outputFluidHandler", outputFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("fuelFluidHandler", fuelFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        super.saveAdditional(tag, pRegistries);
    }
    public float maxTime;
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("input"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        workTime = nbt.getInt("workTime");
        outputFluidHandler.readFromNBT(pRegistries, nbt.getCompound("outputFluidHandler"));
        fuelFluidHandler.readFromNBT(pRegistries, nbt.getCompound("fuelFluidHandler"));
    }
    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return inventory;
    }
    public RecipeInput getCraftingInv() {
        RecipeInput inventory = new SingleRecipeInput(itemHandler.getStackInSlot(0));
        return inventory;
    }
    public void checkRecipes() {
        Optional<RecipeHolder<MeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeRegistry.MELTING_TYPE.get(), getCraftingInv(), level);
        if (recipe.isPresent()) {
            if (!recipe.get().value().equals(this.recipe)) {
                workTime = 0;
            }
            this.recipe = recipe.get().value();
        } else {
            this.recipe = null;
        }
    }
    public int workTime;
    public MeltingRecipe recipe;
    public float essenceCost;
    public int drainCooldown;
    public static void tick(Level world, BlockPos pos, BlockState pState, MelterBlockEntity pBlockEntity) {
        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && pBlockEntity.workTime < 0,
                    pBlockEntity, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(pBlockEntity, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getItemsFromBuffersBelow(pBlockEntity, pBlockEntity.itemHandler);
            BufferUtil.getFluidsFromBuffersBelow(pBlockEntity, pBlockEntity.fuelFluidHandler);
            boolean resetWorkTime = true;
            if (pBlockEntity.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 1) {
                if (pBlockEntity.recipe != null) {
                    FluidStack assembled = pBlockEntity.recipe.getOutput().copy();
                    if (pBlockEntity.getFuelHandler().drain(1, IFluidHandler.FluidAction.SIMULATE).getAmount() >= 1 && pBlockEntity.outputFluidHandler.fill(assembled, IFluidHandler.FluidAction.SIMULATE) >= assembled.getAmount()) {
                        resetWorkTime = false;
                        pBlockEntity.workTime++;
                        pBlockEntity.drainCooldown++;
                        if (pBlockEntity.drainCooldown >= 5) {
                            pBlockEntity.drainCooldown = 0;
                            pBlockEntity.getFuelHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
                        }
                        pBlockEntity.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 1);
                        if (pBlockEntity.workTime >= pBlockEntity.recipe.getTime()) {
                            pBlockEntity.outputFluidHandler.fill(assembled, IFluidHandler.FluidAction.EXECUTE);
                            pBlockEntity.itemHandler.extractItem(0, 1, false);
                            pBlockEntity.workTime = 0;
                        }
                        pBlockEntity.updateBlock();
                    }
                } else {
                    pBlockEntity.drainCooldown = 0;
                }
            } else {
                pBlockEntity.drainCooldown = 0;
            }
            if (resetWorkTime) {
                pBlockEntity.workTime = -1;
                pBlockEntity.updateBlock();
            }
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
        return new MelterMenu(pContainerId, pInventory, this);
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.UP);
    }
}
