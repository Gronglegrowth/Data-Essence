package EsetKalenko.Halcyon.block.world;

import EsetKalenko.Halcyon.client.particle.CircleParticleOptions;
import EsetKalenko.Halcyon.networking.ModMessages;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.ParticleBurst;
import EsetKalenko.Halcyon.registry.BlockEntityRegistry;
import EsetKalenko.Halcyon.registry.BlockRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TidalMeteoriteBlockEntity extends BlockEntity {
    public int wispTimer;
    public List<LunarCrystalSeedWisp> wisps = new ArrayList<>();
    public int crystalSpawnTime;
    public int crystalSpawnsLeft;

    public TidalMeteoriteBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.LUNAR_CRYSTAL_SEED.get(), pos, state);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.Provider pRegistries) {
        tag.putInt("crystalSpawnTime", crystalSpawnTime);
        tag.putInt("CrystalSpawnsLeft", crystalSpawnsLeft);
        super.saveAdditional(tag, pRegistries);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        crystalSpawnTime = nbt.getInt("crystalSpawnTime");
        crystalSpawnsLeft = nbt.getInt("CrystalSpawnsLeft");
        super.loadAdditional(nbt, pRegistries);
    }

    public void resetCrystalSpawnTime() {
        crystalSpawnTime = level.getRandom().nextIntBetweenInclusive(6*20, 8*20);
    }

    public static void tick(Level world, BlockPos pos, BlockState pState, TidalMeteoriteBlockEntity meteorite) {
        if (!world.isClientSide) {
            if (meteorite.crystalSpawnTime <= 0) {
                meteorite.resetCrystalSpawnTime();
            }

            if (meteorite.crystalSpawnsLeft <= 0) {
                meteorite.crystalSpawnsLeft = world.getRandom().nextIntBetweenInclusive(1, 3) + 1;
            }

            meteorite.crystalSpawnTime--;

            if ( meteorite.crystalSpawnsLeft <= 1 || (world.isDay() && world.getBrightness(LightLayer.SKY, pos) > 3)) {
                ModMessages.sendToPlayersNear(new ParticleBurst(pos.getCenter(), new Color(EssenceTypeRegistry.LUNAR_ESSENCE.get().color), 100, 0.75f), (ServerLevel)world, pos.getCenter(), 64);
                world.removeBlock(pos, false);
            }

            if (meteorite.crystalSpawnTime <= 0) {
                meteorite.resetCrystalSpawnTime();
                List<BlockPos> locations = new ArrayList<>();
                for (int x = -3; x <= 3; x++) {
                    for (int y = -3; y <= 3; y++) {
                        for (int z = -3; z <= 3; z++) {
                            BlockPos blockPos = new BlockPos(x, y, z);
                            if (world.getBlockState(blockPos.offset(pos)).canBeReplaced()) {
                                locations.add(blockPos);
                            }
                        }
                    }
                }
                Util.shuffle(locations, world.getRandom());
                for (BlockPos i : locations) {
                    BlockPos candidatePos = pos.offset(i);
                    boolean valid = false;
                    BlockState state = BlockRegistry.CRYSTAL_OF_TRANSFORMATION.get().defaultBlockState();
                    Collection<Direction> directionsToTry = Direction.allShuffled(world.getRandom());
                    for (Direction j : directionsToTry) {
                        BlockPos relative = candidatePos.relative(j.getOpposite());
                        if (world.getBlockState(relative).isFaceSturdy(world, relative, j)) {
                            state = state.setValue(TidalCrystalLunar.FACING, j);
                            valid = true;
                            break;
                        }
                    }
                    if (valid) {
                        ModMessages.sendToPlayersNear(new ParticleBurst(candidatePos.getCenter(), new Color(EssenceTypeRegistry.LUNAR_ESSENCE.get().color), 25, 0.1f), (ServerLevel)world, pos.getCenter(), 64);
                        world.playSound(null, candidatePos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS);
                        world.setBlockAndUpdate(candidatePos, state);
                        meteorite.crystalSpawnsLeft--;
                        break;
                    }
                }
            }
        } else {
            meteorite.wispTimer--;
            if (meteorite.wispTimer <= 0) {
                meteorite.wispTimer = world.getRandom().nextIntBetweenInclusive(5, 25);
                int wisps = world.getRandom().nextIntBetweenInclusive(1, 2);
                for (int i = 0; i < wisps; i++) {
                    meteorite.wisps.add(LunarCrystalSeedWisp.create(world.getRandom()));
                }
            }
            List<LunarCrystalSeedWisp> wispsToRemove = new ArrayList<>();
            for (LunarCrystalSeedWisp i : meteorite.wisps) {
                i.tick(world, pos.getCenter());
                if (i.shouldDelete()) {
                    wispsToRemove.add(i);
                }
            }
            for (LunarCrystalSeedWisp i : wispsToRemove) {
                meteorite.wisps.remove(i);
            }
        }
    }

    public static class LunarCrystalSeedWisp {
        public static final float speedMult = 10;
        private RandomSource random;
        public int lifetime;
        public int initialLifetime;
        public Vec3 offset;
        public Vec3 motion;
        public int timeUntilRedirect;
        private boolean deletionQueued;

        public void tick(Level level, Vec3 center) {
            timeUntilRedirect--;
            if (timeUntilRedirect <= 0) {
                motion = new Vec3(random.nextFloat()-0.5, random.nextFloat()-0.5, random.nextFloat()-0.5).normalize().scale(speedMult);
                timeUntilRedirect = random.nextIntBetweenInclusive(5, 10);
            }
            for (int i = 0; i < 5; i++) {
                offset = offset.add(motion.scale(1f / 20f).scale(1f/5f));
                addParticles(level, center);
            }
            lifetime--;
            if (lifetime <= 0) {
                queueDeletion();
            }
        }

        public void addParticles(Level level, Vec3 center) {
            Vec3 vec = center.add(offset);
            level.addParticle(new CircleParticleOptions().setColor(new Color(EssenceTypeRegistry.LUNAR_ESSENCE.get().color)).setAdditive(true), vec.x, vec.y, vec.z, 0, 0, 0);
        }

        public void queueDeletion() {
            deletionQueued = true;
        }

        public boolean shouldDelete() {
            return deletionQueued;
        }

        public static LunarCrystalSeedWisp create(RandomSource random) {
            LunarCrystalSeedWisp wisp = new LunarCrystalSeedWisp();
            wisp.initialLifetime = 150;
            wisp.lifetime = wisp.initialLifetime;
            wisp.offset = new Vec3(0, 0, 0);
            wisp.random = random;
            return wisp;
        }
    }
}
