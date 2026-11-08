package EsetKalenko.Halcyon.api.util;

import EsetKalenko.Halcyon.api.essence.EssenceBlockEntity;
import EsetKalenko.Halcyon.api.essence.EssenceType;
import EsetKalenko.Halcyon.networking.packet.s2c.vfx.PlayDissipationFXSingleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;

public class DissipationUtil {

    /**
     * Trigger dissipation if the condition is true.
     * @param condition condition under which to dissipate
     * @param tile BE to affect
     * @param world BE's level
     * @param pos BE's pos
     */
    public static void dissipate(boolean condition, EssenceBlockEntity tile, Level world, BlockPos pos) {
        if ( !condition )
            return;

        for ( EssenceType type : tile.getStorage().getSupportedEssenceTypes() ) {
            if ( tile.getStorage().getEssence(type) >= 1 ) {
                tile.getStorage().removeEssence(type, 1);
                PacketDistributor.sendToPlayersNear(
                        (ServerLevel) world, null, pos.getX(), pos.getY(), pos.getZ(),
                        16d, new PlayDissipationFXSingleBlock(pos, new Color(type.color)));
            }
        }

    }
}
