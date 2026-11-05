package EsetKalenko.Halcyon.block.generation;

import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceStorage;
import EsetKalenko.Halcyon.api.essence.container.SingleEssenceContainer;
import EsetKalenko.Halcyon.client.FactorySong;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.DamageTypeRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class EssenceLeechBlockEntity extends BlockEntity implements EssenceBlockEntity {
    public SingleEssenceContainer storage = new SingleEssenceContainer(EssenceTypeRegistry.ESSENCE.get(), 1000);
    @Override
    public EssenceStorage getStorage() {
        return storage;
    }
    public int damageCooldown;
    public int generationTicks;

    public EssenceLeechBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.ESSENCE_LEECH.get(), pos, state);
    }
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(tag, pRegistries);
        tag.putInt("cooldown", damageCooldown);
        tag.putInt("GenerationTicks", generationTicks);
        tag.put("EssenceStorage", storage.toNbt());
    }
    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        storage.fromNbt(nbt.getCompound("EssenceStorage"));
        damageCooldown = nbt.getInt("cooldown");
        generationTicks = nbt.getInt("GenerationTicks");
    }

    // TODO right clicking the machine should play particles showing you the AABBs for 1-2 seconds.

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, EssenceLeechBlockEntity leech) {
        if (!pLevel.isClientSide) {
            if (leech.getStorage().getEssence(EssenceTypeRegistry.ESSENCE.get()) < leech.getStorage().getMaxEssence()) {

                if ( leech.generationTicks > 0) {
                    leech.getStorage().addEssence(EssenceTypeRegistry.ESSENCE.get(), 4);
                }

                for (LivingEntity victim : pLevel.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pPos.getCenter().add(0, 2, 0), 6, 3, 6))) {
                    if (!victim.isAlive()) {
                        leech.generationTicks = (int) (leech.generationTicks + Math.clamp(victim.getMaxHealth() * 5, 0, 100));
                    }
                }

                if ( leech.damageCooldown <= 0 ) {
                    for (LivingEntity victim : pLevel.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(pPos.getCenter().add(0, 2, 0), 3, 3, 3))) {
                        if (victim.isAlive()) {
                            victim.hurt(victim.damageSources().source(DamageTypeRegistry.essenceSiphoned), 5);
                        }
                    }

                    leech.damageCooldown = 10;
                }
            } else {
                leech.damageCooldown -= 1;
            }
        } else {
            ClientHandler.markFactorySong(pPos);
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
