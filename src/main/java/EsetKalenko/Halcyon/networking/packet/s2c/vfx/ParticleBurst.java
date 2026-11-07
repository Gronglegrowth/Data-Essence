package EsetKalenko.Halcyon.networking.packet.s2c.vfx;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.client.particle.MoteParticleOptions;
import EsetKalenko.Halcyon.networking.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.awt.*;

public record ParticleBurst(Vec3 pos, Color color, int amount, float speed) implements Message {

    public static final Type<ParticleBurst> TYPE = new Type<>(Halcyon.locate("particle_burst"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void write(RegistryFriendlyByteBuf buf, ParticleBurst obj) {
        buf.writeVec3(obj.pos);
        buf.writeInt(obj.color.getRGB());
        buf.writeInt(obj.amount);
        buf.writeFloat(obj.speed);
    }

    public static ParticleBurst read(RegistryFriendlyByteBuf buf) {
        Vec3 pos = buf.readVec3();
        Color color = new Color(buf.readInt());
        int amount = buf.readInt();
        float speed = buf.readFloat();
        return new ParticleBurst(pos, color, amount, speed);
    }

    @Override
    public void handleClient(Minecraft minecraft, Player player, IPayloadContext ctx) {
        Level world = player.level();

        RandomSource random = world.getRandom();
        for (int i = 0; i < amount; i++) {
            Vec3 motion = Vec3.directionFromRotation(random.nextIntBetweenInclusive(-180, 180), random.nextIntBetweenInclusive(-180, 180));
            motion.scale(speed);
            world.addParticle(new MoteParticleOptions().setColor(color).setAdditive(true).setLifetime(20), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        }
    }
}