package EsetKalenko.Halcyon.block.generation;

import EsetKalenko.Halcyon.api.DataNEssenceRegistries;
import EsetKalenko.Halcyon.api.block.Machine;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.EssenceType;
import EsetKalenko.Halcyon.api.essence.container.MultiEssenceContainer;
import EsetKalenko.Halcyon.api.util.BufferUtil;
import EsetKalenko.Halcyon.client.particle.CircleShadeParticleOptions;
import EsetKalenko.Halcyon.datamaps.HalcyonDatamaps;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.screen.EssenceBurnerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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
import java.util.Map;
import java.util.function.Supplier;

public class EssenceBurnerBlockEntity extends BlockEntity implements MenuProvider, EssenceBlockEntity, Machine {
    public MultiEssenceContainer storage = new MultiEssenceContainer(List.of(EssenceTypeRegistry.ESSENCE.get(), EssenceTypeRegistry.LUNAR_ESSENCE.get(), EssenceTypeRegistry.NATURAL_ESSENCE.get(), EssenceTypeRegistry.EXOTIC_ESSENCE.get()), 1000);
    public boolean lit;
    public float burnTime;
    public float maxBurnTime;
    public float essenceBurnCooldown;

    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) {
                return stack.getItemHolder().getData(HalcyonDatamaps.SHARD_SUBLIMATION) != null;
            }
            if (slot == 1) {
                return stack.getBurnTime(RecipeType.SMELTING) > 0;
            }
            return super.isItemValid(slot, stack);
        }
    };

    public void drops() {
        SimpleContainer inventory = getInv();

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    

    public EssenceBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ESSENCE_BURNER.get(), pos, state);
        maxBurnTime = 1;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    private final CombinedInvWrapper combinedInvWrapper = new CombinedInvWrapper(itemHandler);

    public CombinedInvWrapper getCombinedInvWrapper() {
        return combinedInvWrapper;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries){
        CompoundTag tag = pkt.getTag();
        storage.fromNbt(tag.getCompound("EssenceStorage"));
        maxBurnTime = tag.getFloat("maxBurnTime");
        burnTime = tag.getFloat("burnTime");
        essenceBurnCooldown = tag.getFloat("essenceBurnCooldown");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putFloat("maxBurnTime", maxBurnTime);
        tag.putFloat("burnTime", burnTime);
        tag.putFloat("essenceBurnCooldown", essenceBurnCooldown);
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.put("inventory", itemHandler.serializeNBT(pRegistries));
        tag.put("EssenceStorage", storage.toNbt());
        tag.putFloat("maxBurnTime", maxBurnTime);
        tag.putFloat("burnTime", burnTime);
        tag.putFloat("essenceBurnCooldown", essenceBurnCooldown);
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        itemHandler.deserializeNBT(pRegistries, nbt.getCompound("inventory"));
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        maxBurnTime = nbt.getFloat("maxBurnTime");
        burnTime = nbt.getFloat("burnTime");
        essenceBurnCooldown = nbt.getFloat("essenceBurnCooldown");
    }

    public SimpleContainer getInv() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        return inventory;
    }

    public static void tick(Level world, BlockPos pos, BlockState pState, EssenceBurnerBlockEntity burner) {
        if (!world.isClientSide()) {
            BufferUtil.getItemsFromBuffersBelow(burner, burner.itemHandler);
            burner.lit = false;

            var dissipateTick = world.random.nextInt() % 17 == 0;
            // TODO make this also generate over time?

            if ( dissipateTick ) {
                for ( EssenceType type : burner.getStorage().getSupportedEssenceTypes() ) {
                    if ( burner.getStorage().getEssence(type) >= 1 ) {
                        burner.getStorage().removeEssence(type, 1);
                        PacketDistributor.sendToPlayersNear(
                                (ServerLevel) world, null, pos.getX(), pos.getY(),pos.getZ(),
                                16d, new PlayDissipationFXSingleBlock(pos, new Color(type.color)));
                    }
                }
            }

            if (burner.itemHandler.getStackInSlot(0).getItemHolder().getData(HalcyonDatamaps.SHARD_SUBLIMATION) == null)
                return;

            var sublimationData = burner.itemHandler.getStackInSlot(0).getItemHolder().getData(HalcyonDatamaps.SHARD_SUBLIMATION);

            boolean hasSpaceToGenerate = true;
            for (Map.Entry<ResourceLocation, Float> energy : sublimationData.energyContained().entrySet()) {
                if ( burner.storage.getEssence(DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(energy.getKey()))
                        + energy.getValue() > burner.storage.getMaxEssence()) {
                    hasSpaceToGenerate = false;
                    break;
                }
            }

            if (burner.burnTime <= 0) {
                burner.essenceBurnCooldown = 0;
                if (hasSpaceToGenerate) {
                    var fuel = burner.itemHandler.getStackInSlot(1);

                    if (fuel.getBurnTime(RecipeType.SMELTING) > 0) {
                        burner.maxBurnTime = fuel.getBurnTime(RecipeType.SMELTING);
                        burner.itemHandler.extractItem(1, 1, false);

                        if ( fuel.hasCraftingRemainingItem() ) {
                            var remainder = fuel.getCraftingRemainingItem();
                            world.addFreshEntity(new ItemEntity(world, pos.getCenter().x, pos.getCenter().y+0.6, pos.getCenter().z, remainder));
                        }

                        burner.burnTime = burner.maxBurnTime;
                        burner.lit = true;
                    }
                }
            } else {
                burner.lit = true;
                burner.essenceBurnCooldown--;
                burner.burnTime--;
                if (burner.essenceBurnCooldown <= 0) {
                    if (hasSpaceToGenerate) {
                        burner.itemHandler.extractItem(0, 1, false);
                        for (Map.Entry<ResourceLocation, Float> energy : sublimationData.energyContained().entrySet()) {
                            burner.storage.addEssence(DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(energy.getKey()),
                                    energy.getValue());
                        }
                        burner.essenceBurnCooldown = 50;
                    }
                }
            }

            if (burner.lit != pState.getValue(EssenceBurner.LIT)) {
                BlockState state = pState.setValue(EssenceBurner.LIT, burner.lit);
                burner.level.setBlock(pos, state, 3);
            }

            burner.updateBlock();
        } else {

            if (burner.essenceBurnCooldown <= 1
                    && pState.getValue(EssenceBurner.LIT)
                    && burner.itemHandler.getStackInSlot(0).getItemHolder().getData(HalcyonDatamaps.SHARD_SUBLIMATION) != null) {

                var sublimationData = burner.itemHandler.getStackInSlot(0).getItemHolder().getData(HalcyonDatamaps.SHARD_SUBLIMATION);
                for (Map.Entry<ResourceLocation, Float> tide : sublimationData.energyContained().entrySet()) {
                    var tideKey = DataNEssenceRegistries.ESSENCE_TYPE_REGISTRY.get(tide.getKey());
                    if (tideKey == null)
                        continue;

                    if (burner.storage.getEssence(tideKey) + tide.getValue() > burner.storage.getMaxEssence()) {
                        continue;
                    }

                    // sublimation particles
                    var cloud = new CircleShadeParticleOptions()
                            .setColor(new Color(tideKey.color))
                            .setAdditive(true)
                            .setFriction(0f)
                            .setGravity(0f)
                            .setLifetime(50);

                    for (int count = 0; count < 50; count++) {
                        var origin = pos.getCenter().add(
                                Mth.nextDouble(world.random, -0.5d, 0.5d),
                                Mth.nextDouble(world.random, 0.5d, 1.0d),
                                Mth.nextDouble(world.random, -0.5d, 0.5d)
                        );

                        world.addParticle(
                                cloud,
                                origin.x,
                                origin.y,
                                origin.z,
                                Mth.nextDouble(world.random, -0.05, 0.05),
                                Mth.nextDouble(world.random, 0.01, 0.3),
                                Mth.nextDouble(world.random, -0.05, 0.05)
                        );
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
        return new EssenceBurnerMenu(pContainerId, pInventory, this);
    }

    @Override
    public List<Direction> getValidInputDirections() {
        return List.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    }
}
