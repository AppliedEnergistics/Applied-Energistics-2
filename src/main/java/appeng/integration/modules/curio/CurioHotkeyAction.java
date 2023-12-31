package appeng.integration.modules.curio;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.features.HotkeyAction;
import appeng.hotkeys.InventoryHotkeyAction;

public record CurioHotkeyAction(Predicate<ItemStack> locatable,
        InventoryHotkeyAction.Opener opener) implements HotkeyAction {

    public CurioHotkeyAction(Item item, InventoryHotkeyAction.Opener opener) {
        this((stack) -> stack.getItem() == item, opener);
    }

    @Override
    public boolean run(Player player) {
        var cap = CurioModule.ITEM_HANDLER.getCapability(player, null);
        if (cap == null)
            return false;
        for (int i = 0; i < cap.getSlots(); i++) {
            if (locatable.test(cap.getStackInSlot(i))) {
                if (opener.open(player, new CurioItemLocator(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
