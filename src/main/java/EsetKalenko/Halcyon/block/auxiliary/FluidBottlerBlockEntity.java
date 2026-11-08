package EsetKalenko.Halcyon.block.auxiliary;

import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.screen.FluidBottlerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class FluidBottlerBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
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
    private final ItemStackHandler outputItemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler, outputItemHandler);
    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }
    private final FluidTank fluidHandler = new FluidTank(4000);
    public void drops() {
        SimpleContainer inventory = getInv();

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }
    public IItemHandler getItemHandler() {
        return itemHandler;
    }
    public IItemHandler getOutputHandler() {
        return outputItemHandler;
    }

    public FluidBottlerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.FLUID_BOTTLER.get(), pos, state);
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
        item = ItemStack.parseOptional(pRegistries, tag.getCompound("item"));
        fluidHandler.readFromNBT(pRegistries, tag.getCompound("fluid"));
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.put("item", item.saveOptional(pRegistries));
        tag.put("fluid", fluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("input", itemHandler.serializeNBT(pRegistries));
        tag.put("output", outputItemHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.put("fluid", fluidHandler.writeToNBT(pRegistries, new CompoundTag()));
        super.saveAdditional(tag, pRegistries);
    }
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("input"));
        outputItemHandler.deserializeNBT(pRegistries, nbt.getCompound("output"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        workTime = nbt.getInt("workTime");
        fluidHandler.readFromNBT(pRegistries, nbt.getCompound("fluid"));
    }
    public ItemStack item;
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
    public int workTime;
    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FluidBottlerBlockEntity pBlockEntity) {
        if (!pLevel.isClientSide()) {

            DissipationUtil.dissipate(pLevel.random.nextInt() % 17 == 0 && pBlockEntity.workTime < 0,
                    pBlockEntity, pLevel, pPos);

            BufferUtil.getEssenceFromBuffersBelow(pBlockEntity, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getItemsFromBuffersBelow(pBlockEntity, pBlockEntity.itemHandler);
            BufferUtil.getFluidsFromBuffersBelow(pBlockEntity, pBlockEntity.fluidHandler);
            boolean resetWorkTime = true;
            if (pBlockEntity.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 2.5) {
                ItemStack stack = pBlockEntity.itemHandler.getStackInSlot(0);
                if (stack.is(Items.BUCKET)) {
                    if (pBlockEntity.fluidHandler.getFluid().getAmount() >= 1000) {
                        ItemStack bucket = FluidUtil.getFilledBucket(pBlockEntity.fluidHandler.getFluid());
                        if (pBlockEntity.outputItemHandler.insertItem(0, bucket, true).isEmpty()) {
                            pBlockEntity.workTime++;
                            resetWorkTime = false;
                            pBlockEntity.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), (float)50/20);
                            if (pBlockEntity.workTime >= 20) {
                                pBlockEntity.itemHandler.extractItem(0, 1, false);
                                pBlockEntity.outputItemHandler.insertItem(0, bucket, false);
                                pBlockEntity.fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                                pBlockEntity.workTime = 0;
                                pLevel.playSound(null, pPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                            }
                        }
                    }
                }
                if (stack.is(Items.GLASS_BOTTLE)) {
                    if (pBlockEntity.fluidHandler.getFluid().getAmount() >= 250) {

                        // TODO dehardcode. whuh :rivstare:
                        if (pBlockEntity.fluidHandler.getFluid().is(Fluids.WATER)) {
                            ItemStack bottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                            if (pBlockEntity.outputItemHandler.insertItem(0, bottle, true).isEmpty()) {
                                pBlockEntity.workTime++;
                                resetWorkTime = false;
                                pBlockEntity.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), (float)50/20);
                                if (pBlockEntity.workTime >= 20) {
                                    pBlockEntity.itemHandler.extractItem(0, 1, false);
                                    pBlockEntity.outputItemHandler.insertItem(0, bottle, false);
                                    pBlockEntity.fluidHandler.drain(250, IFluidHandler.FluidAction.EXECUTE);
                                    pBlockEntity.workTime = 0;
                                    pLevel.playSound(null, pPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                                }
                            }
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
        return new FluidBottlerMenu(pContainerId, pInventory, this);
    }
    private static boolean hasNotReachedStackLimit(FluidBottlerBlockEntity entity, ItemStack toAdd) {
        if (toAdd.is(entity.outputItemHandler.getStackInSlot(0).getItem())) {
            return entity.outputItemHandler.getStackInSlot(0).getCount() + toAdd.getCount() <= entity.outputItemHandler.getStackInSlot(0).getMaxStackSize();
        } else return entity.outputItemHandler.getStackInSlot(0).isEmpty();
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.SOUTH);
    }
}
