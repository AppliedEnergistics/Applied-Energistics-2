package appeng.client.guidebook.document.interaction;

import java.util.List;

import appeng.siteexport.ExportableResourceProvider;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public interface GuideTooltip extends ExportableResourceProvider {

    default ItemStack getIcon() {
        return ItemStack.EMPTY;
    }

    List<ClientTooltipComponent> getLines();

}
