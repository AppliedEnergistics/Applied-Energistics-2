package appeng.api.integrations.curios;

import appeng.api.features.HotkeyAction;
import appeng.hotkeys.InventoryHotkeyAction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import top.theillusivec4.curios.api.CuriosCapability;

import java.util.function.Predicate;

public record CuriosHotkeyAction(Predicate<ItemStack> locatable,
                                 InventoryHotkeyAction.Opener opener) implements HotkeyAction {

    public CuriosHotkeyAction(ItemLike item, InventoryHotkeyAction.Opener opener) {
        this((stack) -> stack.is(item.asItem()), opener);
    }

    @Override
    public boolean run(Player player) {
        var cap = player.getCapability(CuriosCapability.ITEM_HANDLER);
        if (cap == null)
            return false;
        for (int i = 0; i < cap.size(); i++) {
            if (locatable.test(cap.getResource(i).toStack())) {
                if (opener.open(player, CuriosItemLocator.forCurioSlot(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
