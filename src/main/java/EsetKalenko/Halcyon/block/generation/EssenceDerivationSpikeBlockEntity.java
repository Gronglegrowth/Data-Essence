package EsetKalenko.Halcyon.block.generation;

import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import com.cmdpro.databank.model.animation.DatabankAnimationReference;
import com.cmdpro.databank.model.animation.DatabankAnimationState;
import com.cmdpro.databank.multiblock.Multiblock;
import com.cmdpro.databank.multiblock.MultiblockManager;
import com.cmdpro.databank.temperature.TemperatureUtil;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.api.block.Overheatable;
import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.client.FactorySong;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.DamageTypeRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;

import EsetKalenko.Halcyon.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EssenceDerivationSpikeBlockEntity extends BlockEntity implements EssenceBlockEntity, Overheatable {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 2000f);
    public int cooldown, temperature;
    public boolean isBroken, hasStructure, hasRedstone;
    final Multiblock structure = MultiblockManager.multiblocks.get(Halcyon.locate("generators/essence_derivation_spike"));

    private final FluidTank coolantTank = new FluidTank(4000);
    public IFluidHandler getFluidHandler() {
        return coolantTank;
    }

    public DatabankAnimationState animState = new DatabankAnimationState("idle")
            .addAnim(new DatabankAnimationReference("idle", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("rotate_rings", (state, anim) -> {}, (state, anim) -> {}))
            .addAnim(new DatabankAnimationReference("extend_spike", (state, anim) -> {}, (state, anim) -> state.setAnim("rotate_rings")))
            .addAnim(new DatabankAnimationReference("retract_spike", (state, anim) -> {}, (state, anim) -> state.setAnim("idle")));


    public EssenceDerivationSpikeBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ESSENCE_DERIVATION_SPIKE.get(), pos, blockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        animState.setLevel(level);
    }

    @Override
    public EssenceStorage getStorage() {
        return storage;
    }

    @Override
    public int getMaxTemperature() {
        return 1300;
    }

    @Override
    public void overheat(BlockEntity block) {
        BlockPos pos = block.getBlockPos();
        Level world = block.getLevel();
        temperature = TemperatureUtil.getBiomeTemperature(world.getBiome(pos).value());
        // angry hiss, damages everything in its AABB a LOT, shuts down.
        world.playSound(null, pos, SoundEvents.CREEPER_PRIMED, SoundSource.BLOCKS);
        world.playSound(null, pos, SoundEvents.ANVIL_BREAK, SoundSource.BLOCKS);
        for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pos.getCenter().add(0, 1, 0), 11, 3, 11))) {
            if (entity.isAlive())
                entity.hurt(entity.damageSources().source(DamageTypeRegistry.overheatFailure), 20);
        }
        isBroken = true;
    }

    public boolean isRedstonePowered() {
        BlockPos pos = this.getBlockPos();
        hasRedstone = this.getLevel().hasNeighborSignal(pos);
        return hasRedstone;
    }

    public boolean isAbleToWork() {
        return(hasRedstone && hasStructure && !isBroken);
    }

    // TODO give the big guy same treatment as leech

    public static void tick(Level world, BlockPos pos, BlockState state, EssenceDerivationSpikeBlockEntity spike) {
        if (!world.isClientSide()) {
            boolean hadStructure = spike.hasStructure;
            spike.hasStructure = spike.structure.checkMultiblockAll(world, pos);
            if (hadStructure && !spike.hasStructure) {
                spike.updateBlock();
            }

            var dissipateTick = world.random.nextInt() % 17 == 0;
                    //&& spike.generationTicks <= 0;

            if ( dissipateTick && spike.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 1 ) {
                spike.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 1);
                PacketDistributor.sendToPlayersNear(
                        (ServerLevel) world, null, pos.getX(), pos.getY(),pos.getZ(),
                        16d, new PlayDissipationFXSingleBlock(pos, new Color(EssenceTypeRegistry.ESSENCE.get().color)));
            }

            if (spike.hasStructure) {

                if (spike.isRedstonePowered() && spike.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) < spike.getStorage().getMaxEssence() && spike.cooldown <= 0 && !spike.isBroken) {

                    for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pos.getCenter().add(0, 1, 0), 11, 3, 11))) {
                        if (entity.isAlive())
                            entity.hurt(entity.damageSources().source(DamageTypeRegistry.essenceSiphoned), 10);
                        if (!entity.isAlive()) {
                            spike.getStorage().addEssence(EssenceTypeRegistry.ESSENCE.get(), Math.clamp((entity.getMaxHealth() * 4) * 5, 0f, 1000f));
                            spike.temperature += 5;
                        }
                    }
                    spike.cooldown = 20;
                }
                spike.cooldown--;

                if ( spike.temperature >= spike.getMaxTemperature() )
                    spike.overheat(spike);

                // todo coolant-dependent values
                if ( world.random.nextInt() % 15 == 0 && spike.coolantTank.getFluidAmount() >= 10) {
                    spike.temperature = Math.max(spike.temperature - 1, TemperatureUtil.getBiomeTemperature(world.getBiome(pos).value()));
                    spike.coolantTank.drain(10, IFluidHandler.FluidAction.EXECUTE);
                }

                if ( spike.isBroken && world.random.nextInt() % 17 == 0 ) {
                    world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 0.75f);
                    ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, 5, 0, 0, 0, 0);
                }
                spike.updateBlock();
            }
        } else {
            if (spike.isAbleToWork())
                ClientHandler.markFactorySong(pos);
        }
    }

    @Override
    public float getMeterSideLength(Direction direction) {
        if (direction.equals(Direction.UP)) {
            if (isAbleToWork()) {
                return EssenceBlockEntity.super.getMeterSideLength(direction)*4.25f;
            }
        }
        return EssenceBlockEntity.super.getMeterSideLength(direction);
    }

    protected void updateBlock() {
        BlockState blockState = level.getBlockState(this.getBlockPos());
        this.level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, 3);
        this.setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(tag, pRegistries);
        tag.putInt("cooldown", cooldown);
        tag.putInt("Temp", temperature);
        tag.putBoolean("IsBroken", isBroken);
        tag.put("EssenceStorage", storage.toNbt());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        cooldown = nbt.getInt("cooldown");
        temperature = nbt.getInt("Temp");
        isBroken = nbt.getBoolean("IsBroken");
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider pRegistries){
        CompoundTag tag = pkt.getTag();
        storage.fromNbt(tag.getCompound("EssenceStorage"));
        isBroken = tag.getBoolean("IsBroken");
        hasStructure = tag.getBoolean("Multi");
        hasRedstone = tag.getBoolean("Redstone");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.put("EssenceStorage", storage.toNbt());
        tag.putBoolean("IsBroken", isBroken);
        tag.putBoolean("Multi", hasStructure);
        tag.putBoolean("Redstone", hasRedstone);
        return tag;
    }

    private static class ClientHandler {
        static FactorySong.FactoryLoop leechSound = FactorySong.getLoop(SoundRegistry.LEECH_LOOP.value());
        static FactorySong.FactoryLoop spikeSound = FactorySong.getLoop(SoundRegistry.DERIVATION_SPIKE_LOOP.value());

        public static void markFactorySong(BlockPos pos) {
            leechSound.addSource(pos);
            spikeSound.addSource(pos);
        }
    }
}
