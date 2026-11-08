package EsetKalenko.Halcyon.api.block;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.api.DataNEssenceRegistries;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.EssenceType;
import EsetKalenko.Halcyon.api.essence.container.MultiEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.api.util.DataTabletUtil;
import EsetKalenko.Halcyon.api.util.DissipationUtil;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.recipe.IFabricationRecipe;
import EsetKalenko.Halcyon.recipe.NonMenuCraftingContainer;
import EsetKalenko.Halcyon.registry.HalcyonCriteria;
import EsetKalenko.Halcyon.registry.RecipeRegistry;
import EsetKalenko.Halcyon.registry.SoundRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class BaseFabricatorBlockEntity extends BlockEntity implements EssenceBlockEntity, PlayerOwned {
    public MultiEssenceContainer storage = new MultiEssenceContainer(getSupportedEssenceTypes(), getMaxEssence());
    public int time;
    public int maxTime;
    public Recipe<CraftingInput> recipe;
    public boolean enoughEssence;
    public Map<ResourceLocation, Float> essenceCost;
    public ItemStack item;
    protected UUID placer;

    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            checkRecipes();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 9) {
                return false;
            }
            return super.isItemValid(slot, stack);
        }
    };
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

    private ClientHandler clientHandler;

    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler);
    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }
    public IItemHandler getItemHandler() {
        return itemHandler;
    }
    public void checkRecipes() {
        Optional<RecipeHolder<IFabricationRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeRegistry.FABRICATIONCRAFTING.get(), getCraftingInv().asCraftInput(), level);
        if (recipe.isPresent()) {
            this.recipe = recipe.get().value();
            essenceCost = recipe.get().value().getEssenceCost();
            item = recipe.get().value().getResultItem(level.registryAccess());
            maxTime = recipe.get().value().getTime();
        } else {
            Optional<RecipeHolder<CraftingRecipe>> recipe2 = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, getCraftingInv().asCraftInput(), level);
            if (recipe2.isPresent()) {
                this.recipe = recipe2.get().value();
                item = recipe2.get().value().getResultItem(level.registryAccess());
                essenceCost = null;
            } else {
                this.recipe = null;
                essenceCost = null;
                item = ItemStack.EMPTY;
            }
            maxTime = 20;
        }
    }
    public void drops() {
        SimpleContainer inventory = getInv();

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public abstract List<EssenceType> getSupportedEssenceTypes();

    public abstract float getMaxEssence();

    public BaseFabricatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
        time = tag.getInt("time");
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
        tag.putInt("time", time);
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
        tag.put("EssenceStorage", storage.toNbt());
        tag.putInt("time", time);
        PlayerOwned.writePlacer(tag, placer);
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventory"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        placer = PlayerOwned.readPlacer(nbt);
        time = nbt.getInt("time");
    }

    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
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

    /**
     * Override this to add conditions that your Fabricator needs to meet before it's allowed to perform crafting operations
     * e.g. multiblock checks
     * @return whether this Fabricator may craft or not
     */
    public boolean canCraft() {
        return true;
    }

    public static InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        if (!pLevel.isClientSide) {
            if (blockEntity instanceof BaseFabricatorBlockEntity ent) {
                if (ent.time >= 0) {
                    ent.time = -1;
                } else {
                    if (ent.recipe != null) {

                        if (!ent.canCraft()) {
                            pPlayer.sendSystemMessage(
                                    Component.translatable("block.halcyon.fabricator.cannot_work_check_tablet"));
                            return InteractionResult.FAIL;
                        }

                        if (ent.enoughEssence) {
                            IFabricationRecipe fabricationRecipe = null;
                            if (ent.recipe instanceof IFabricationRecipe) {
                                fabricationRecipe = (IFabricationRecipe) ent.recipe;
                            }
                            if (fabricationRecipe == null || DataTabletUtil.playerHasEntry(pPlayer, fabricationRecipe.getEntry(), fabricationRecipe.getCompletionStage())) {
                                ent.time = 0;
                                pLevel.playSound(null, pPos, SoundRegistry.FABRICATOR_START.value(), SoundSource.BLOCKS, 2, 1);
                            } else {
                                pPlayer.sendSystemMessage(Component.translatable("block.halcyon.fabricator.dont_know_how"));
                            }
                        } else {
                            pPlayer.sendSystemMessage(Component.translatable("block.halcyon.fabricator.not_enough_essence"));
                        }
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    public void tryCraft() {
        CraftingInput craftingInput = getCraftingInv().asCraftInput();
        ItemStack result = recipe.assemble(craftingInput, level.registryAccess()).copy();
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(craftingInput);
        int left = getCraftingInv().asPositionedCraftInput().left();
        int top = getCraftingInv().asPositionedCraftInput().top();
        int craftWidth = 3;

        if (essenceCost != null) {
            for (Map.Entry<ResourceLocation, Float> cost : essenceCost.entrySet()) {
                EssenceType type = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(cost.getKey());
                storage.removeEssence(type, cost.getValue());
            }
        }

        for (int k = 0; k < craftingInput.height(); k++) {
            for (int l = 0; l < craftingInput.width(); l++) {
                int i1 = l + left + (k + top) * craftWidth;
                ItemStack existingItem = itemHandler.getStackInSlot(i1);
                ItemStack remainderItem = remaining.get(l + k * craftingInput.width());

                if (!existingItem.isEmpty()) {
                    itemHandler.extractItem(i1, 1, false);
                    existingItem = itemHandler.getStackInSlot(i1); // we re-check this slot after crafting
                }

                if (!existingItem.isEmpty()) {
                    ItemEntity entity = new ItemEntity(level, (float) getBlockPos().getX() + 0.5f, (float) getBlockPos().getY() + 1f, (float) getBlockPos().getZ() + 0.5f, remainderItem);
                    level.addFreshEntity(entity);
                } else {
                    itemHandler.insertItem(i1, remainderItem, false);
                }
            }

            var placer = (ServerPlayer) getPlacerIfOnline();
            if (placer != null)
                HalcyonCriteria.FABRICATOR_RECIPE_CRAFTED.trigger(placer, result);
        }

        ItemEntity entity = new ItemEntity(level, (float) getBlockPos().getX() + 0.5f, (float) getBlockPos().getY() + 1f, (float) getBlockPos().getZ() + 0.5f, result);
        level.addFreshEntity(entity);
        level.playSound(null, getBlockPos(), SoundRegistry.FABRICATOR_CRAFT.value(), SoundSource.BLOCKS, 2, 1);
    }

    /** Some modpacks contain broken recipes with empty ingredients or empty results.
     * Because that results in an Incredibly Annoying barrage of sounds from this block, let's just not do anything when that happens.
     * Those packs should really prevent such recipes existing in the first place, but....
     @return True if the recipe is not broken (i.e. does not have either an empty result item, or a totally empty set of input items)**/
    public boolean isNotTryingToCraftBrokenRecipe() {
        var isBroken = (recipe.getResultItem(level.registryAccess()).isEmpty() || recipe.getIngredients().stream().allMatch(Ingredient::hasNoItems));
        if (isBroken && !level.isClientSide)
            Halcyon.LOGGER.warn("[HALCYON] Fabricator at {}, {}, {} is trying to craft \"{}\", which has an erroneously empty ingredient list or result! It is highly advised to correct or remove this recipe to prevent further errors.", this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), recipe);
        return !isBroken;
    }

    public void tick(Level world, BlockPos pPos, BlockState pState, BaseFabricatorBlockEntity fabricator) {
        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && fabricator.time < 0,
                    fabricator, world, pPos);

            BufferUtil.getEssenceFromBuffersBelow(fabricator, fabricator.getSupportedEssenceTypes());
            if (fabricator.recipe != null && isNotTryingToCraftBrokenRecipe()) {

                boolean enoughEssence = true;
                if (fabricator.essenceCost != null) {
                    for (Map.Entry<ResourceLocation, Float> i : fabricator.essenceCost.entrySet()) {
                        EssenceType type = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(i.getKey());
                        if (fabricator.storage.getEssence(type) < i.getValue()) {
                            enoughEssence = false;
                        }
                    }
                }
                fabricator.enoughEssence = enoughEssence;
                if (fabricator.recipe.matches(fabricator.getCraftingInv().asCraftInput(), fabricator.level)) {
                    if (fabricator.time != -1) {
                        fabricator.time++;
                    }
                } else {
                    fabricator.time = -1;
                }
            } else {
                fabricator.time = -1;
            }
            if (fabricator.time >= fabricator.maxTime) {
                fabricator.tryCraft();
                fabricator.time = 0;
            }
            fabricator.updateBlock();
        } else {
            if (fabricator.time >= 0 && ( fabricator.recipe != null && isNotTryingToCraftBrokenRecipe())) {
                if (!fabricator.clientHandler.isSoundPlaying())
                    fabricator.clientHandler.startSound();
            } else {
                if (fabricator.clientHandler.isSoundPlaying())
                    fabricator.clientHandler.stopSound();
            }
        }
    }

    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, Block.UPDATE_ALL);
        this.setChanged();
    }

    private static boolean hasNotReachedStackLimit(BaseFabricatorBlockEntity entity) {
        return entity.itemHandler.getStackInSlot(2).getCount() < entity.itemHandler.getStackInSlot(2).getMaxStackSize();
    }

    @Override
    public UUID getPlacerID() {
        return placer;
    }

    @Override
    public void setPlacerID(Player player) {
        this.placer = player.getUUID();
    }

    private static class ClientHandler {
        public SoundInstance workingSound;
        public void createWorkingSound(BlockPos pos) {
            workingSound = new SimpleSoundInstance(SoundRegistry.FABRICATOR_WORKING.value().getLocation(), SoundSource.BLOCKS, 2f, 1f, SoundInstance.createUnseededRandom(), true, 0, SoundInstance.Attenuation.LINEAR, pos.getX(), pos.getY(), pos.getZ(), false);
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
}
