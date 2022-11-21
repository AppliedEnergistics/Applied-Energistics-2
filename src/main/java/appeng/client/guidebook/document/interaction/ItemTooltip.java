package appeng.client.guidebook.document.interaction;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
    public List<ClientTooltipComponent> getLines(Screen screen) {
        var lines = screen.getTooltipFromItem(stack);
        return lines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }
}
