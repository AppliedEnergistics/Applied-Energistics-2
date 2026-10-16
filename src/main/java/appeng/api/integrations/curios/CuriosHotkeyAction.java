package appeng.api.integrations.curios;

import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.item.ItemResource;

import top.theillusivec4.curios.api.CuriosCapability;

import appeng.api.features.HotkeyAction;
import appeng.hotkeys.InventoryHotkeyAction;

public record CuriosHotkeyAction(Predicate<ItemResource> locatable,
        InventoryHotkeyAction.Opener opener) implements HotkeyAction {

    public CuriosHotkeyAction(ItemLike item, InventoryHotkeyAction.Opener opener) {
        this((resource) -> resource.is(item), opener);
    }

    @Override
    public boolean run(Player player) {
        var cap = player.getCapability(CuriosCapability.ITEM_HANDLER);
        if (cap == null)
            return false;
        for (int i = 0; i < cap.size(); i++) {
            if (locatable.test(cap.getResource(i))) {
                if (opener.open(player, CuriosItemLocator.forCurioSlot(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
