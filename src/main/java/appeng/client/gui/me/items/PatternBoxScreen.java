package appeng.client.gui.me.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.items.PatternBoxMenu;

public class PatternBoxScreen extends AEBaseScreen<PatternBoxMenu> {
    public PatternBoxScreen(PatternBoxMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }
}
