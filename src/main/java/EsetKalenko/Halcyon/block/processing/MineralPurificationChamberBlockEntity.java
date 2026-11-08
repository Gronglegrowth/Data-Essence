package EsetKalenko.Halcyon.block.processing;

import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.recipe.MineralPurificationRecipe;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.RecipeRegistry;
import EsetKalenko.Halcyon.registry.SoundRegistry;
import EsetKalenko.Halcyon.screen.MineralPurificationChamberMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.material.Fluids;
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

public class MineralPurificationChamberBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
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
    private final ItemStackHandler outputItemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final FluidTank waterFluidHandler = new FluidTank(2000) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.is(Fluids.WATER);
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
    
    
    

    public IFluidHandler getWaterHandler() {
        return waterFluidHandler;
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
    public MineralPurificationChamberBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.MINERAL_PURIFICATION_CHAMBER.get(), pos, state);
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
        waterFluidHandler.readFromNBT(pRegistries, tag.getCompound("waterHandler"));
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.putInt("maxTime", recipe == null ? -1 : recipe.getTime());
        tag.put("waterHandler", waterFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("input", itemHandler.serializeNBT(pRegistries));
        tag.put("output", outputItemHandler.serializeNBT(pRegistries));
        tag.put("water", waterFluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        super.saveAdditional(tag, pRegistries);
    }
    public float maxTime;
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("input"));
        outputItemHandler.deserializeNBT(pRegistries, nbt.getCompound("output"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        waterFluidHandler.readFromNBT(pRegistries, nbt.getCompound("water"));
        workTime = nbt.getInt("workTime");
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
    public RecipeInput getCraftingInv() {
        RecipeInput inventory = new SingleRecipeInput(itemHandler.getStackInSlot(0));
        return inventory;
    }
    public void checkRecipes() {
        Optional<RecipeHolder<MineralPurificationRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeRegistry.MINERAL_PURIFICATION_TYPE.get(), getCraftingInv(), level);
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
    public MineralPurificationRecipe recipe;
    public float essenceCost;
    public static void tick(Level world, BlockPos pos, BlockState pState, MineralPurificationChamberBlockEntity pBlockEntity) {
        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && pBlockEntity.workTime < 0,
                    pBlockEntity, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(pBlockEntity, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getItemsFromBuffersBelow(pBlockEntity, pBlockEntity.itemHandler);
            BufferUtil.getFluidsFromBuffersBelow(pBlockEntity, pBlockEntity.waterFluidHandler);
            boolean resetWorkTime = true;
            if (pBlockEntity.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 1 && pBlockEntity.waterFluidHandler.getFluidAmount() >= 250) {
                if (pBlockEntity.recipe != null) {
                    ItemStack assembled = pBlockEntity.recipe.assemble(pBlockEntity.getCraftingInv(), world.registryAccess());
                    ItemStack assembled2 = pBlockEntity.recipe.assembleNuggetOutput(world.random, pBlockEntity.getCraftingInv(), world.registryAccess());
                    if (pBlockEntity.outputItemHandler.insertItem(0, assembled, true).isEmpty() && pBlockEntity.outputItemHandler.insertItem(1, assembled2, true).isEmpty()) {
                        resetWorkTime = false;
                        pBlockEntity.workTime++;
                        pBlockEntity.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 1);
                        if (pBlockEntity.workTime >= pBlockEntity.recipe.getTime()) {
                            pBlockEntity.waterFluidHandler.drain(250, IFluidHandler.FluidAction.EXECUTE);
                            pBlockEntity.outputItemHandler.insertItem(0, assembled, false);
                            pBlockEntity.outputItemHandler.insertItem(1, assembled2, false);
                            pBlockEntity.itemHandler.extractItem(0, 1, false);
                            world.playSound(null, pos, SoundRegistry.MINERAL_PURIFICATION_CHAMBER_CRAFT.value(), SoundSource.BLOCKS, 1, 1);
                            pBlockEntity.workTime = 0;
                        }
                    }
                }
            }
            if (resetWorkTime) {
                pBlockEntity.workTime = -1;
            }
            pBlockEntity.updateBlock();
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
        return new MineralPurificationChamberMenu(pContainerId, pInventory, this);
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.UP);
    }
}
