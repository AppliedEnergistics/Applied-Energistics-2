package appeng.menu.me.items;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.items.contents.PatternBoxMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;

public class PatternBoxMenu extends AEBaseMenu {
    public static final MenuType<PatternBoxMenu> TYPE = MenuTypeBuilder
            .create(PatternBoxMenu::new, PatternBoxMenuHost.class)
            .build("pattern_box");

    public PatternBoxMenu(MenuType<?> menuType, int id, Inventory playerInventory, PatternBoxMenuHost host) {
        super(menuType, id, playerInventory, host);

        for (int i = 0; i < 27; i++) {
            this.addSlot(new RestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN,
                    host.getInventory(),
                    i), SlotSemantics.ENCODED_PATTERN);
        }

        this.createPlayerInventorySlots(playerInventory);
    }
}
