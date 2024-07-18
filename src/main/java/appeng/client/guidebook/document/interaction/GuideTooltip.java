package appeng.client.guidebook.document.interaction;

import java.util.List;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import appeng.siteexport.ExportableResourceProvider;

public interface GuideTooltip extends ExportableResourceProvider {

    default ItemStack getIcon() {
        return ItemStack.EMPTY;
    }

    List<ClientTooltipComponent> getLines();

}
