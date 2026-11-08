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
import EsetKalenko.Halcyon.client.particle.CircleParticleOptions;
import EsetKalenko.Halcyon.client.particle.ConfigurableParticleOptions;
import EsetKalenko.Halcyon.item.DataDrive;
import EsetKalenko.Halcyon.recipe.InfusionRecipe;
import EsetKalenko.Halcyon.screen.InfuserMenu;
import EsetKalenko.Halcyon.data.datatablet.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class InfuserBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
    public DatabankAnimationState animState = new DatabankAnimationState("idle")
            .addAnim(new DatabankAnimationReference("idle", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("active", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("deactivated", (state, anim) -> {}, (state, anim) -> {}));
    public MultiEssenceContainer storage = new MultiEssenceContainer(List.of(EssenceTypeRegistry.ESSENCE.get(), EssenceTypeRegistry.LUNAR_ESSENCE.get(), EssenceTypeRegistry.NATURAL_ESSENCE.get(), EssenceTypeRegistry.EXOTIC_ESSENCE.get()), 1000);
    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        animState.setLevel(level);
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            checkRecipes();
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
            if (slot == 0) {
                return stack.getItem() instanceof DataDrive;
            }
            return super.isItemValid(slot, stack);
        }
    };
    private final ItemStackHandler outputItemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
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
    public InfuserBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.INFUSER.get(), pos, state);
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
        tag.putInt("workTime", workTime);
        tag.put("item", item.saveOptional(pRegistries));
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
        tag.put("input", itemHandler.serializeNBT(pRegistries));
        tag.put("dataDrive", dataDriveHandler.serializeNBT(pRegistries));
        tag.put("output", outputItemHandler.serializeNBT(pRegistries));
        tag.putInt("workTime", workTime);
        tag.put("EssenceStorage", storage.toNbt());
        super.saveAdditional(tag, pRegistries);
    }
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("input"));
        dataDriveHandler.deserializeNBT(pRegistries, nbt.getCompound("dataDrive"));
        outputItemHandler.deserializeNBT(pRegistries, nbt.getCompound("output"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        workTime = nbt.getInt("workTime");
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
    public RecipeInput getCraftingInv() {
        RecipeInput inventory = new SingleRecipeInput(itemHandler.getStackInSlot(0));
        return inventory;
    }
    public void checkRecipes() {
        Optional<RecipeHolder<InfusionRecipe>> recipe = level.getRecipeManager().getRecipesFor(RecipeRegistry.INFUSION_TYPE.get(), getCraftingInv(), level).stream().filter((a) -> a.value().getEntry().equals(DataDrive.getEntryId(dataDriveHandler.getStackInSlot(0)))).findFirst();
        if (recipe.isPresent()) {
            this.recipe = recipe.get().value();
            essenceCost = recipe.get().value().getEssenceCost();
        } else {
            this.recipe = null;
        }
    }
    public int workTime;
    public InfusionRecipe recipe;
    public boolean enoughEssence;
    public Map<ResourceLocation, Float> essenceCost;
    public static void tick(Level world, BlockPos pPos, BlockState pState, InfuserBlockEntity tile) {
        if (!world.isClientSide()) {
            DissipationUtil.dissipate(world.random.nextInt() % 17 == 0 && tile.workTime < 0,
                    tile, world, pPos);

            BufferUtil.getEssenceFromBuffersBelow(tile, List.of(EssenceTypeRegistry.ESSENCE.get(), EssenceTypeRegistry.LUNAR_ESSENCE.get(), EssenceTypeRegistry.NATURAL_ESSENCE.get(), EssenceTypeRegistry.EXOTIC_ESSENCE.get()));
            BufferUtil.getItemsFromBuffersBelow(tile, tile.itemHandler);
            tile.item = tile.itemHandler.getStackInSlot(0);
            boolean shouldReset = true;
            if (tile.recipe != null) {
                boolean enoughEssence = true;
                for (Map.Entry<ResourceLocation, Float> i : tile.essenceCost.entrySet()) {
                    EssenceType type = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(i.getKey());
                    if (tile.storage.getEssence(type) < i.getValue()/50f) {
                        enoughEssence = false;
                    }
                }
                if (enoughEssence) {
                    Entry entry = DataDrive.getEntry(tile.dataDriveHandler.getStackInSlot(0));
                    if (entry != null) {
                        if (tile.dataDriveHandler.getStackInSlot(0).has(DataComponentRegistry.DATA_ID) && tile.dataDriveHandler.getStackInSlot(0).has(DataComponentRegistry.DATA_INCOMPLETE)) {
                            if (tile.recipe == null || (tile.recipe.getEntry().equals(entry.id) && DataDrive.getEntryCompletionStage(tile.dataDriveHandler.getStackInSlot(0)) >= tile.recipe.getCompletionStage())) {
                                if (hasNotReachedStackLimit(tile, tile.recipe.getResultItem(world.registryAccess()))) {
                                    for (Map.Entry<ResourceLocation, Float> i : tile.essenceCost.entrySet()) {
                                        EssenceType type = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(i.getKey());
                                        tile.storage.removeEssence(type, i.getValue() / 50f);
                                    }
                                    if (tile.workTime >= 50) {
                                        ItemStack stack = tile.recipe.assemble(tile.getCraftingInv(), world.registryAccess()).copy();
                                        tile.outputItemHandler.insertItem(0, stack, false);
                                        tile.itemHandler.extractItem(0, 1, false);
                                        world.playSound(null, pPos, SoundRegistry.INFUSER_CRAFT.value(), SoundSource.BLOCKS, 2, 1);
                                        tile.workTime = 0;
                                    } else {
                                        tile.workTime++;
                                    }
                                    shouldReset = false;
                                }
                            }
                        }
                    }
                }
                tile.enoughEssence = enoughEssence;
            }
            if (shouldReset) {
                tile.workTime = -1;
            }
            tile.updateBlock();
        } else {
            if (tile.workTime >= 0) {
                if (tile.essenceCost != null) {
                    List<EssenceType> essences = new ArrayList<>();
                    for (Map.Entry<ResourceLocation, Float> i : tile.essenceCost.entrySet()) {
                        if (i.getValue() > 0) {
                            EssenceType type = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(i.getKey());
                            essences.add(type);
                        }
                    }
                    if (!essences.isEmpty()) {
                        if (essences.size() == 1) {
                            ConfigurableParticleOptions options = new CircleParticleOptions().setFriction(0.8f).setColor(new Color(essences.getFirst().color)).setAdditive(false).setFullbright(true);
                            Vec3 pos = pPos.getBottomCenter();
                            Vec3 spd = pos.vectorTo(pPos.getCenter()).scale(0.2);
                            world.addParticle(options, pos.x, pos.y, pos.z, spd.x, spd.y, spd.z);
                        } else {
                            float rot = 45;
                            for (EssenceType i : essences) {
                                ConfigurableParticleOptions options = new CircleParticleOptions().setFriction(0.8f).setColor(new Color(i.color)).setAdditive(false).setFullbright(true);
                                Vec3 pos = pPos.getBottomCenter().add(Vec3.directionFromRotation(0, rot).scale(0.4));
                                Vec3 spd = pos.vectorTo(pPos.getCenter()).scale(0.2);
                                world.addParticle(options, pos.x, pos.y, pos.z, spd.x, spd.y, spd.z);
                                rot += 360f / essences.size();
                            }
                        }
                    }
                }
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
        return new InfuserMenu(pContainerId, pInventory, this);
    }
    private static boolean hasNotReachedStackLimit(InfuserBlockEntity entity, ItemStack toAdd) {
        if (toAdd.is(entity.outputItemHandler.getStackInSlot(0).getItem())) {
            return entity.outputItemHandler.getStackInSlot(0).getCount() + toAdd.getCount() <= entity.outputItemHandler.getStackInSlot(0).getMaxStackSize();
        } else return entity.outputItemHandler.getStackInSlot(0).isEmpty();
    }
}
