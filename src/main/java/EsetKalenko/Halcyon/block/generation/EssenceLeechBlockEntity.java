package EsetKalenko.Halcyon.block.generation;

import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.client.FactorySong;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.DamageTypeRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EssenceLeechBlockEntity extends BlockEntity implements EssenceBlockEntity {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
    @Override
    public EssenceStorage getStorage() {
        return storage;
    }
    public int cooldown;
    public int generationTicks;

    public EssenceLeechBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ESSENCE_LEECH.get(), pos, state);
    }
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(tag, pRegistries);
        tag.putInt("cooldown", cooldown);
        tag.putInt("GenerationTicks", generationTicks);
        tag.put("EssenceStorage", storage.toNbt());
    }
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        cooldown = nbt.getInt("cooldown");
        generationTicks = nbt.getInt("GenerationTicks");
    }

    // TODO right clicking the machine should play particles showing you the AABBs for 1-2 seconds.

    public static void tick(Level world, BlockPos leechPos, BlockState leechState, EssenceLeechBlockEntity leech) {
        var dissipateTick = world.random.nextInt() % 17 == 0
                && leech.generationTicks <= 0;

        if (!world.isClientSide) {

            if ( dissipateTick && leech.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) >= 1 ) {
                leech.getStorage().removeEssence(EssenceTypeRegistry.ESSENCE.get(), 1);
                PacketDistributor.sendToPlayersNear(
                        (ServerLevel) world, null, leechPos.getX(), leechPos.getY(),leechPos.getZ(),
                        16d, new PlayDissipationFXSingleBlock(leechPos, new Color(EssenceTypeRegistry.ESSENCE.get().color)));
            }

            if ( leech.generationTicks > 0) {
                leech.getStorage().addEssence(EssenceTypeRegistry.ESSENCE.get(), 4);
                leech.generationTicks--;
                if (leech.generationTicks < 0)
                    leech.generationTicks = 0;
            }

            if (leech.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) < leech.getStorage().getMaxEssence()
            && leech.cooldown <= 0) {

                for (LivingEntity victim : world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(leechPos.getCenter().add(0, 2, 0), 3, 3, 3))) {
                    if (victim.isAlive()) {
                        victim.hurt(victim.damageSources().source(DamageTypeRegistry.essenceSiphoned), 5);
                    }
                }

                for (LivingEntity victim : world.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(leechPos.getCenter().add(0, 2, 0), 6, 3, 6))) {
                    if (!victim.isAlive()) {
                        var toAdd = Math.clamp(victim.getMaxHealth() * 2, 0, 100);
                        leech.generationTicks = (int) (leech.generationTicks + toAdd);
                    }
                }

                leech.cooldown = 10;

            } else {
                leech.cooldown -= 1;
            }
        } else {
            ClientHandler.markFactorySong(leechPos);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    private static class ClientHandler {
        static FactorySong.FactoryLoop workingSound = FactorySong.getLoop(SoundRegistry.LEECH_LOOP.value());

        public static void markFactorySong(BlockPos pos) {
            workingSound.addSource(pos);
        }
    }
}
