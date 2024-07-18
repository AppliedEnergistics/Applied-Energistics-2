package appeng.client.guidebook.document.interaction;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.siteexport.ResourceExporter;

public class ItemTooltip implements GuideTooltip {
    private final ItemStack stack;

    public ItemTooltip(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getIcon() {
        return stack;
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        var lines = Screen.getTooltipFromItem(Minecraft.getInstance(), stack);
        return lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    @Override
    public void exportResources(ResourceExporter exporter) {
        exporter.referenceItem(stack);
    }
}
