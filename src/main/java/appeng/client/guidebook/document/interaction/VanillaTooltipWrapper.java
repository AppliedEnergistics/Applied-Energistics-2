package appeng.client.guidebook.document.interaction;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class VanillaTooltipWrapper implements GuideTooltip {
    private final Tooltip tooltip;

    public VanillaTooltipWrapper(Tooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        return tooltip.toCharSequence(Minecraft.getInstance())
                .stream()
                .<ClientTooltipComponent>map(ClientTextTooltip::new)
                .toList();
    }
}
