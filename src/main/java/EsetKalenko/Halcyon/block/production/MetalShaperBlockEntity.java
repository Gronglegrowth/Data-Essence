package EsetKalenko.Halcyon.block.production;

import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.registry.*;
import com.cmdpro.databank.model.animation.DatabankAnimationReference;
import com.cmdpro.databank.model.animation.DatabankAnimationState;
import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.client.FactorySong;
import EsetKalenko.Halcyon.recipe.MetalShaperRecipe;
import EsetKalenko.Halcyon.screen.MetalShaperMenu;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetalShaperBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
    public DatabankAnimationState animState = new DatabankAnimationState("idle")
            .addAnim(new DatabankAnimationReference("idle",
                    (state, anim) -> {},
                    (state, anim) -> {}
            ))
            .addAnim(new DatabankAnimationReference("work_that_thang",
                    (state, anim) -> {},
                    (state, anim) -> {}
            ));
    public AnimationDefinition anim;
    public ItemStack item;
    public ItemStack impression;
    public ItemStack result;
    public MetalShaperRecipe recipe;
    public int workTime;
    public int maxWorkTime;

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

    private final ItemStackHandler moldHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            checkRecipes();
        }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.has(DataComponentRegistry.SHAPING_IMPRESSION);
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

    public IItemHandler getMoldHandler() {
        return moldHandler;
    }

    public IItemHandler getOutputHandler() {
        return outputItemHandler;
    }

    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler, moldHandler, outputItemHandler);

    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }

    public MetalShaperBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.METAL_SHAPER.get(), pos, state);
        item = ItemStack.EMPTY;
        impression = ItemStack.EMPTY;
        result = ItemStack.EMPTY;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            checkRecipes();
        }
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
        impression = ItemStack.parseOptional(pRegistries, tag.getCompound("impression"));
        result = ItemStack.parseOptional(pRegistries, tag.getCompound("result"));
        workTime = tag.getInt("workTime");
        itemHandler.deserializeNBT(pRegistries, tag.getCompound("itemHandler"));
        moldHandler.deserializeNBT(pRegistries, tag.getCompound("inventoryDrive"));
        maxWorkTime = tag.getInt("maxWorkTime");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.put("item", item.saveOptional(pRegistries));
        tag.put("impression", impression.saveOptional(pRegistries));
        tag.put("result", result.saveOptional(pRegistries));
        tag.putInt("workTime", workTime);
        tag.putInt("maxWorkTime", recipe == null ? -1 : recipe.getTime());
        tag.put("itemHandler", itemHandler.serializeNBT(pRegistries));
        tag.put("inventoryDrive", moldHandler.serializeNBT(pRegistries));
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inventory", itemHandler.serializeNBT(pRegistries));
        tag.put("inventoryOutput", outputItemHandler.serializeNBT(pRegistries));
        tag.put("inventoryDrive", moldHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventory"));
        outputItemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventoryOutput"));
        moldHandler.deserializeNBT(pRegistries, nbt.getCompound("inventoryDrive"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        workTime = nbt.getInt("workTime");
    }

    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+outputItemHandler.getSlots()+moldHandler.getSlots());
        for (int i = 0; i < moldHandler.getSlots(); i++) {
            inventory.setItem(i, moldHandler.getStackInSlot(i));
        }
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i+moldHandler.getSlots(), itemHandler.getStackInSlot(i));
        }
        for (int i = 0; i < outputItemHandler.getSlots(); i++) {
            inventory.setItem(i+moldHandler.getSlots()+itemHandler.getSlots(), outputItemHandler.getStackInSlot(i));
        }
        return inventory;
    }

    public RecipeInput getCraftingInv() {
        RecipeInput inventory = new RecipeInput() {
            @Override
            public ItemStack getItem(int pIndex) {
                return itemHandler.getStackInSlot(pIndex);
            }

            @Override
            public int size() {
                return 1;
            }
        };
        return inventory;
    }

    public void checkRecipes() {
        List<RecipeHolder<MetalShaperRecipe>> recipes = level.getRecipeManager().getRecipesFor(RecipeRegistry.METAL_SHAPING_TYPE.get(), getCraftingInv(), level);
        RecipeHolder<MetalShaperRecipe> recipe = null;
        ResourceLocation moldRecipe = null;
        if (!moldHandler.getStackInSlot(0).isEmpty()) {
            moldRecipe = moldHandler.getStackInSlot(0).get(DataComponentRegistry.SHAPING_IMPRESSION);
        }
        for (RecipeHolder<MetalShaperRecipe> i : recipes) {
            if (i.id().equals(moldRecipe)) {
                recipe = i;
            }
        }
        if (recipe != null) {
            if (!recipe.value().equals(this.recipe)) {
                workTime = 0;
            }
            this.recipe = recipe.value();
            this.result = recipe.value().getResultItem(level.registryAccess());

        } else {
            this.recipe = null;
            this.result = ItemStack.EMPTY;
        }
    }

    public static void tick(Level world, BlockPos pos, BlockState pState, MetalShaperBlockEntity pBlockEntity) {
        if (!world.isClientSide()) {

            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && pBlockEntity.workTime < 0,
                    pBlockEntity, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(pBlockEntity, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getItemsFromBuffersBelow(pBlockEntity, pBlockEntity.itemHandler);
            boolean resetWorkTime = true;
            if (pBlockEntity.recipe != null) {
                if (pBlockEntity.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) > 0) {
                    ItemStack assembled = pBlockEntity.recipe.assemble(pBlockEntity.getCraftingInv(), world.registryAccess());
                    if (pBlockEntity.outputItemHandler.insertItem(0, assembled, true).isEmpty()) {
                        resetWorkTime = false;
                        // working vfx
                        if (world.random.nextInt() % 20 == 0) {
                            world.playSound(null, pos, SoundRegistry.METAL_SHAPER_CRAFTING.value(), SoundSource.BLOCKS, 0.4f, 1f);
                            ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, 5, 0, 0, 0, 0);
                        }
                        pBlockEntity.workTime++;
                        pBlockEntity.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 1);
                        if (pBlockEntity.workTime >= pBlockEntity.recipe.getTime()) {
                            pBlockEntity.outputItemHandler.insertItem(0, assembled, false);
                            pBlockEntity.itemHandler.extractItem(0, 1, false);
                            pBlockEntity.workTime = 0;
                        }
                    }
                }
            }
            if (resetWorkTime) {
                pBlockEntity.workTime = -1;
            }
            pBlockEntity.updateBlock();
        } else {
            if (pBlockEntity.workTime >= 0)
                ClientHandler.markFactorySong(pos);
        }
    }

    @Override
    public float getMeterSideLength(Direction direction) {
        if (direction.equals(Direction.UP)) {
            return EssenceBlockEntity.super.getMeterSideLength(direction)*1.5f;
        }
        return EssenceBlockEntity.super.getMeterSideLength(direction);
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
        return new MetalShaperMenu(pContainerId, pInventory, this);
    }

    private static class ClientHandler {
        static FactorySong.FactoryLoop workingSound = FactorySong.getLoop(SoundRegistry.METAL_SHAPER_LOOP.value());

        public static void markFactorySong(BlockPos pos) {
            workingSound.addSource(pos);
        }
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.EAST, Direction.WEST);
    }
}
