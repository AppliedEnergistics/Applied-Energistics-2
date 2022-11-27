package appeng.client.guidebook.document.interaction;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public interface GuideTooltip {

    default ItemStack getIcon() {
        return ItemStack.EMPTY;
    }

    List<ClientTooltipComponent> getLines(Screen screen);

}
