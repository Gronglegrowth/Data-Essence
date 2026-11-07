package EsetKalenko.Halcyon.networking;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.networking.packet.c2s.PlayerChangeDriveData;
import EsetKalenko.Halcyon.networking.packet.c2s.PlayerFinishDataBankMinigame;
import EsetKalenko.Halcyon.networking.packet.c2s.PlayerSetItemHandlerLocked;
import EsetKalenko.Halcyon.networking.packet.c2s.RequestMachineEssenceValue;
import EsetKalenko.Halcyon.networking.packet.s2c.*;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Halcyon.MOD_ID)
public class ModMessages {
    public class Handler {
        public static <T extends CustomPacketPayload> void handle(T message, IPayloadContext ctx) {
            if (message instanceof Message msg) {
                if (ctx.flow().getReceptionSide() == LogicalSide.SERVER) {
                    ctx.enqueueWork(() -> {
                        Server.handle(msg, ctx);
                    });
                } else {
                    ctx.enqueueWork(() -> {
                        Client.handle(msg, ctx);
                    });
                }
            }
        }
        public class Client {
            public static <T extends Message> void handle(T message, IPayloadContext ctx) {
                message.handleClient(Minecraft.getInstance(), Minecraft.getInstance().player, ctx);
            }
        }
        public class Server {
            public static <T extends Message> void handle(T message, IPayloadContext ctx) {
                message.handleServer(ctx.player().getServer(), (ServerPlayer)ctx.player(), ctx);
            }
        }
        public abstract interface Reader<T extends Message> {
            public abstract T read(RegistryFriendlyByteBuf buf);
        }
        public abstract interface Writer<T extends Message> {
            public abstract void write(RegistryFriendlyByteBuf buf, T message);
        }
    }
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Halcyon.MOD_ID)
                .versioned("1.0");

        //S2C
        registrar.playToClient(UnlockEntry.TYPE, getNetworkCodec(UnlockEntry::read, UnlockEntry::write), Handler::handle);
        registrar.playToClient(UnlockedEntrySync.TYPE, getNetworkCodec(UnlockedEntrySync::read, UnlockedEntrySync::write), Handler::handle);
        registrar.playToClient(PlayerTierSync.TYPE, getNetworkCodec(PlayerTierSync::read, PlayerTierSync::write), Handler::handle);
        registrar.playToClient(PlayerDataSync.TYPE, getNetworkCodec(PlayerDataSync::read, PlayerDataSync::write), Handler::handle);
        registrar.playToClient(DataBankEntrySync.TYPE, getNetworkCodec(DataBankEntrySync::read, DataBankEntrySync::write), Handler::handle);
        registrar.playToClient(ComputerDataSync.TYPE, getNetworkCodec(ComputerDataSync::read, ComputerDataSync::write), Handler::handle);
        registrar.playToClient(EntrySync.TYPE, getNetworkCodec(EntrySync::read, EntrySync::write), Handler::handle);
        registrar.playToClient(DragonPartsSync.TYPE, getNetworkCodec(DragonPartsSync::read, DragonPartsSync::write), Handler::handle);
        registrar.playToClient(PingStructures.TYPE, getNetworkCodec(PingStructures::read, PingStructures::write), Handler::handle);
        registrar.playToClient(PingableSync.TYPE, getNetworkCodec(PingableSync::read, PingableSync::write), Handler::handle);
        registrar.playToClient(CreatePingShader.TYPE, getNetworkCodec(CreatePingShader::read, CreatePingShader::write), Handler::handle);
        registrar.playToClient(GrapplingHookSync.TYPE, getNetworkCodec(GrapplingHookSync::read, GrapplingHookSync::write), Handler::handle);
        registrar.playToClient(MachineEssenceValueSync.TYPE, getNetworkCodec(MachineEssenceValueSync::read, MachineEssenceValueSync::write), Handler::handle);
        registrar.playToClient(AddScannedOre.TYPE, getNetworkCodec(AddScannedOre::read, AddScannedOre::write), Handler::handle);
        registrar.playToClient(OpenEntryEditor.TYPE, getNetworkCodec(OpenEntryEditor::read, OpenEntryEditor::write), Handler::handle);
        registrar.playToClient(StructureMusicSync.TYPE, getNetworkCodec(StructureMusicSync::read, StructureMusicSync::write), Handler::handle);

        // FX
        registrar.playToClient(PlayBufferTransferParticle.TYPE, getNetworkCodec(PlayBufferTransferParticle::read, PlayBufferTransferParticle::write), Handler::handle);
        registrar.playToClient(ParticleBurst.TYPE, getNetworkCodec(ParticleBurst::read, ParticleBurst::write), Handler::handle);
        registrar.playToClient(PlayGenderfluidTransitionEffect.TYPE, getNetworkCodec(PlayGenderfluidTransitionEffect::read, PlayGenderfluidTransitionEffect::write), Handler::handle);
        registrar.playToClient(PlayEnderPearlRedirectionEffect.TYPE, getNetworkCodec(PlayEnderPearlRedirectionEffect::read, PlayEnderPearlRedirectionEffect::write), Handler::handle);
        registrar.playToClient(PlayDissipationFXSingleBlock.TYPE, getNetworkCodec(PlayDissipationFXSingleBlock::read, PlayDissipationFXSingleBlock::write), Handler::handle);

        //C2S
        registrar.playToServer(PlayerFinishDataBankMinigame.TYPE, getNetworkCodec(PlayerFinishDataBankMinigame::read, PlayerFinishDataBankMinigame::write), Handler::handle);
        registrar.playToServer(PlayerChangeDriveData.TYPE, getNetworkCodec(PlayerChangeDriveData::read, PlayerChangeDriveData::write), Handler::handle);
        registrar.playToServer(PlayerSetItemHandlerLocked.TYPE, getNetworkCodec(PlayerSetItemHandlerLocked::read, PlayerSetItemHandlerLocked::write), Handler::handle);
        registrar.playToServer(RequestMachineEssenceValue.TYPE, getNetworkCodec(RequestMachineEssenceValue::read, RequestMachineEssenceValue::write), Handler::handle);
    }

    public static <T extends Message> StreamCodec<RegistryFriendlyByteBuf, T> getNetworkCodec(Handler.Reader<T> reader, Handler.Writer<T> writer) {
        return StreamCodec.of(writer::write, reader::read);
    }

    public static <T extends Message> void sendToServer(T message) {
        PacketDistributor.sendToServer(message);
    }

    public static <T extends Message> void sendToPlayer(T message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
    public static <T extends Message> void sendToPlayersNear(T message, ServerLevel level, Vec3 position, float radius) {
        PacketDistributor.sendToPlayersNear(level, null, position.x, position.y, position.z, radius, message);
    }
    public static <T extends Message> void sendToPlayersTrackingEntityAndSelf(T message, ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, message);
    }
}
