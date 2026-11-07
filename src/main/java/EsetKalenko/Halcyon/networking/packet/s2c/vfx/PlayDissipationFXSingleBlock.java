package EsetKalenko.Halcyon.networking.packet.s2c.vfx;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.client.particle.MoteParticleOptions;
import EsetKalenko.Halcyon.networking.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.*;

public record PlayDissipationFXSingleBlock(BlockPos pos, Color color) implements Message {

    public static final Type<PlayDissipationFXSingleBlock> TYPE = new Type<>(Halcyon.locate("play_dissipation_fx_single_block"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void write(RegistryFriendlyByteBuf buf, PlayDissipationFXSingleBlock obj) {
        buf.writeBlockPos(obj.pos);
        buf.writeInt(obj.color.getRGB());
    }

    public static PlayDissipationFXSingleBlock read(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Color color = new Color(buf.readInt());

        return new PlayDissipationFXSingleBlock(pos, color);
    }

    @Override
    public void handleClient(Minecraft minecraft, Player player, IPayloadContext ctx) {
        Level world = player.level();

        RandomSource random = world.getRandom();

        var drip = new MoteParticleOptions()
                .setColor(color)
                .setAdditive(true)
                .setFriction(0f)
                .setGravity(0.3f)
                .setLifetime(20);

        // save me redstone ore particles. save me
        for (Direction direction : Direction.values()) {
            BlockPos blockpos = pos.relative(direction);
            BlockState dirState = world.getBlockState(blockpos);
            if (!world.getBlockState(blockpos).isSolidRender(world, blockpos)) {
                Direction.Axis direction$axis = direction.getAxis();
                double d1 = direction$axis == Direction.Axis.X ? 0.5 + 0.5625 * (double)direction.getStepX() : (double)random.nextFloat();
                double d2 = direction$axis == Direction.Axis.Y ? 0.5 + 0.5625 * (double)direction.getStepY() : (double)random.nextFloat();
                double d3 = direction$axis == Direction.Axis.Z ? 0.5 + 0.5625 * (double)direction.getStepZ() : (double)random.nextFloat();
                world.addParticle(
                        drip, (double)pos.getX() + d1, (double)pos.getY() + d2, (double)pos.getZ() + d3, 0.0, -0.01, 0.0
                );
            }
        }

        // TODO sound?
    }
}
