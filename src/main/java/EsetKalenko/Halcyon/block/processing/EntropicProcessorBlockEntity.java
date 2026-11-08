package EsetKalenko.Halcyon.block.processing;

import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.registry.*;
import com.cmdpro.databank.model.animation.DatabankAnimationReference;
import com.cmdpro.databank.model.animation.DatabankAnimationState;
import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.recipe.EntropicProcessingRecipe;
import EsetKalenko.Halcyon.screen.EntropicProcessorMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class EntropicProcessorBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {

    public DatabankAnimationState animState = new DatabankAnimationState("idle")
            .addAnim(new DatabankAnimationReference("idle", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("working", (state, anim) -> {}, (state, anim) -> {}));

    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);

    public EntropicProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ENTROPIC_PROCESSOR.get(), pos, state);
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
    public IItemHandler getOutputHandler() {
        return outputItemHandler;
    }
    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler, outputItemHandler);
    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide) {
            clientHandler = new ClientHandler();
            clientHandler.createWorkingSound(getBlockPos());
        } else {
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
        workTime = tag.getInt("workTime");
        maxTime = tag.getInt("maxTime");
        if (tag.contains("item")) {
            item = ItemStack.parseOptional(pRegistries, tag.getCompound("item"));
        } else {
            item = null;
        }
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("workTime", workTime);
        tag.putInt("maxTime", recipe == null ? -1 : recipe.getTime());
        if (item != null) {
            tag.put("item", item.saveOptional(pRegistries));
        }
        return tag;
    }
    public ItemStack item;

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("input", itemHandler.serializeNBT(pRegistries));
        tag.put("output", outputItemHandler.serializeNBT(pRegistries));
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
        Optional<RecipeHolder<EntropicProcessingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeRegistry.ENTROPIC_PROCESSING_TYPE.get(), getCraftingInv(), level);
        if (recipe.isPresent()) {
            if (!recipe.get().value().equals(this.recipe))
                workTime = 0;

            this.recipe = recipe.get().value();
        } else {
            this.recipe = null;
        }
    }
    public int workTime;
    public EntropicProcessingRecipe recipe;
    public float essenceCost;


    public static void tick(Level world, BlockPos pos, BlockState pState, EntropicProcessorBlockEntity grindingChampion) {
        if (!world.isClientSide()) {

            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && grindingChampion.workTime < 0,
                    grindingChampion, world, pos);

            BufferUtil.getEssenceFromBuffersBelow(grindingChampion, EssenceTypeRegistry.ESSENCE.get());
            BufferUtil.getItemsFromBuffersBelow(grindingChampion, grindingChampion.itemHandler);


            // Get the list of items inside the block
            List<ItemEntity> items = getItemsAt(world, pos);

            // Loop through them and try to add them to the inventory
            for (ItemEntity itemEntity : items) {
                ItemStack stack = itemEntity.getItem();

                // Try to insert into the input
                ItemStack remainder = grindingChampion.itemHandler.insertItem(0, stack, false);

                //update the itemstack
                if (remainder.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(remainder);
                }
            }

            boolean resetWorkTime = true;
            if (grindingChampion.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 1) {

                if (grindingChampion.recipe != null) {
                    ItemStack assembled = grindingChampion.recipe.assemble(grindingChampion.getCraftingInv(), world.registryAccess());
                    if (grindingChampion.outputItemHandler.insertItem(0, assembled, true).isEmpty()) {
                        for (LivingEntity victim : world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pos.getCenter(), 0.9f, 0.9f, 0.9f))) {
                            victim.hurt(victim.damageSources().source(DamageTypeRegistry.crushed), 2f);
                        }
                        resetWorkTime = false;
                        grindingChampion.workTime++;
                        grindingChampion.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 1);

                        if (grindingChampion.workTime >= grindingChampion.recipe.getTime()) {
                            grindingChampion.outputItemHandler.insertItem(0, assembled, false);
                            grindingChampion.itemHandler.extractItem(0, 1, false);
                            grindingChampion.workTime = 0;
                        }
                    }
                }
            }
            if (resetWorkTime) {
                grindingChampion.workTime = -1;
            }
            grindingChampion.item = grindingChampion.itemHandler.getStackInSlot(0);
            grindingChampion.updateBlock();
        } else {
            if (grindingChampion.workTime >= 0) {
                if (!grindingChampion.clientHandler.isSoundPlaying()) {
                    grindingChampion.clientHandler.startSound();
                }
                if (grindingChampion.item != null && !grindingChampion.item.isEmpty()) {
                    Vec3 particlePos = pos.getCenter().add(0, 0.25, 0);
                    Vec3 particleSpeed = Vec3.directionFromRotation(0, world.getRandom().nextIntBetweenInclusive(-180, 180)).scale(0.1).add(0, 0.25, 0);
                    world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, grindingChampion.item), particlePos.x, particlePos.y, particlePos.z, particleSpeed.x, particleSpeed.y, particleSpeed.z);
                }
            } else {
                if (grindingChampion.clientHandler.isSoundPlaying()) {
                    grindingChampion.clientHandler.stopSound();
                }
            }
        }
    }

    // Returns items inside the block
    public static List<ItemEntity> getItemsAt(Level level, BlockPos pPos) {
        return level.getEntitiesOfClass(ItemEntity.class, AABB.ofSize(pPos.getCenter(), 0.9f, 0.9f, 0.9f), EntitySelector.ENTITY_STILL_ALIVE);
    }

    private ClientHandler clientHandler;
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
        return new EntropicProcessorMenu(pContainerId, pInventory, this);
    }

    private static boolean hasNotReachedStackLimit(EntropicProcessorBlockEntity entity, ItemStack toAdd) {
        if (toAdd.is(entity.outputItemHandler.getStackInSlot(0).getItem())) {
            return entity.outputItemHandler.getStackInSlot(0).getCount() + toAdd.getCount() <= entity.outputItemHandler.getStackInSlot(0).getMaxStackSize();
        } else return entity.outputItemHandler.getStackInSlot(0).isEmpty();
    }

    private static class ClientHandler {
        public SoundInstance workingSound;
        public void createWorkingSound(BlockPos pos) {
            workingSound = new SimpleSoundInstance(SoundRegistry.ENTROPIC_PROCESSOR_WORKING.value(), SoundSource.BLOCKS, 0.5f, 1.25f, SoundInstance.createUnseededRandom(), pos);
        }
        public void startSound() {
            Minecraft.getInstance().getSoundManager().play(workingSound);
        }
        public void stopSound() {
            Minecraft.getInstance().getSoundManager().stop(workingSound);
        }
        public boolean isSoundPlaying() {
            return Minecraft.getInstance().getSoundManager().isActive(workingSound);
        }
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.UP);
    }
}