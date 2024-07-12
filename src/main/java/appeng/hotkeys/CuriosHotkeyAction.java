package appeng.hotkeys;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.features.HotkeyAction;
import appeng.integration.modules.curios.CuriosIntegration;
import appeng.menu.locator.MenuLocators;

public record CuriosHotkeyAction(Predicate<ItemStack> locatable,
        InventoryHotkeyAction.Opener opener) implements HotkeyAction {

    public CuriosHotkeyAction(ItemLike item, InventoryHotkeyAction.Opener opener) {
        this((stack) -> stack.is(item.asItem()), opener);
    }

    @Override
    public boolean run(Player player) {
        var cap = player.getCapability(CuriosIntegration.ITEM_HANDLER);
        if (cap == null)
            return false;
        for (int i = 0; i < cap.getSlots(); i++) {
            if (locatable.test(cap.getStackInSlot(i))) {
                if (opener.open(player, MenuLocators.forCurioSlot(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
