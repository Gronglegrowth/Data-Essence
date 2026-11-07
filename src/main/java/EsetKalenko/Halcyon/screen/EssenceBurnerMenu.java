package EsetKalenko.Halcyon.screen;

import EsetKalenko.Halcyon.util.IDataNEssenceMenuHelper;
import EsetKalenko.Halcyon.block.generation.EssenceBurnerBlockEntity;
import EsetKalenko.Halcyon.registry.BlockRegistry;
import EsetKalenko.Halcyon.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EssenceBurnerMenu extends AbstractContainerMenu implements IDataNEssenceMenuHelper {
    public final EssenceBurnerBlockEntity blockEntity;
    private final Level level;

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return guiHelperQuickMoveStack(player, index, 2, this);
    }

    @Override
    public boolean guiHelperMoveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        return moveItemStackTo(pStack, pStartIndex, pEndIndex, pReverseDirection);
    }

    public EssenceBurnerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public EssenceBurnerMenu(int pContainerId, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.ESSENCE_BURNER_MENU.get(), pContainerId);
        blockEntity = ((EssenceBurnerBlockEntity) entity);
        this.level = inv.player.level();
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 17));
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 80, 53));
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, BlockRegistry.ESSENCE_BURNER.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

}
