package EsetKalenko.Halcyon.item.equipment;

import EsetKalenko.Halcyon.Halcyon;
import com.cmdpro.databank.DatabankUtils;
import com.cmdpro.databank.hidden.types.BlockHiddenType;
import EsetKalenko.Halcyon.api.item.ItemEssenceContainer;
import EsetKalenko.Halcyon.networking.ModMessages;
import EsetKalenko.Halcyon.networking.packet.s2c.AddScannedOre;
import EsetKalenko.Halcyon.networking.packet.s2c.CreatePingShader;
import EsetKalenko.Halcyon.registry.DataComponentRegistry;
import EsetKalenko.Halcyon.registry.EssenceTypeRegistry;
import EsetKalenko.Halcyon.registry.SoundRegistry;
import EsetKalenko.Halcyon.registry.HalcyonTags;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;

public class OreScanner extends Item {
    public static ResourceLocation FUEL_ESSENCE_TYPE = Halcyon.locate("essence");
    public static int ESSENCE_COST = 50;
    public static int COOLDOWN = 20 * 10;

    public OreScanner(Properties pProperties) {
        super(pProperties.component(DataComponentRegistry.ESSENCE_STORAGE, new ItemEssenceContainer(List.of(FUEL_ESSENCE_TYPE), 2500)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (!pLevel.isClientSide) {
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            if (ItemEssenceContainer.getEssence(stack, FUEL_ESSENCE_TYPE) >= ESSENCE_COST) {
                pLevel.playSound(null, pPlayer.blockPosition(), SoundRegistry.ORE_SCANNER_PING.value(), SoundSource.PLAYERS);
                ModMessages.sendToPlayersNear(new CreatePingShader(pPlayer.position()), (ServerLevel)pLevel, pPlayer.position(), 128);
                AABB bounds = AABB.ofSize(pPlayer.getBoundingBox().getCenter(), 32, 32, 32);
                HashMap<BlockPos, Integer> ores = new HashMap<>();
                for (BlockPos i : BlockPos.betweenClosed(BlockPos.containing(bounds.getMinPosition()), BlockPos.containing(bounds.getMaxPosition()))) {
                    BlockState state = pLevel.getBlockState(i);
                    Block hiddenBlock = BlockHiddenType.getHiddenBlock(state.getBlock(), pPlayer);
                    if (hiddenBlock != null) {
                        state = DatabankUtils.changeBlockType(state, hiddenBlock);
                    }
                    if (state.is(HalcyonTags.Blocks.SCANNABLE_ORES)) {
                        ores.put(new BlockPos(i), (int)(bounds.getCenter().distanceTo(i.getCenter())*2f));
                    }
                }
                ModMessages.sendToPlayer(new AddScannedOre(ores), (ServerPlayer)pPlayer);
                pPlayer.getCooldowns().addCooldown(this, COOLDOWN);
                ItemEssenceContainer.removeEssence(stack, FUEL_ESSENCE_TYPE, ESSENCE_COST);
            }
        }
        return InteractionResultHolder.sidedSuccess(pPlayer.getItemInHand(pUsedHand), pLevel.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, context, components, flag);
        components.add(Component.translatable("item.halcyon.mineral_finding_rod.tooltip", Component.literal(String.valueOf(ESSENCE_COST)).withColor(0xFFFF96B5) ).withStyle(Style.EMPTY.withColor(EssenceTypeRegistry.ESSENCE.get().getColor())));
    }
}