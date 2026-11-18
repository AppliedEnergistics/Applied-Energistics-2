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
        var opened = new boolean[1];
        CuriosIntegration.forEachStack(player, (slotType, index, stack) -> {
            if (!opened[0] && locatable.test(stack)
                    && opener.open(player, MenuLocators.forCurioSlot(slotType, index))) {
                opened[0] = true;
            }
        });
        return opened[0];
    }
}
