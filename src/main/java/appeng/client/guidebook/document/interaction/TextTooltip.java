package appeng.client.guidebook.document.interaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class TextTooltip implements GuideTooltip {
    private final List<ClientTooltipComponent> lines;

    public TextTooltip(String text) {
        this(Component.literal(text));
    }

    public TextTooltip(List<Component> lines) {
        this.lines = lines.stream()
                .<ClientTooltipComponent>map(line -> new ClientTextTooltip(line.getVisualOrderText()))
                .toList();
    }

    public TextTooltip(Component firstLine, Component... additionalLines) {
        this(makeLineList(firstLine, additionalLines));
    }

    private static List<Component> makeLineList(Component firstLine, Component[] additionalLines) {
        var lines = new ArrayList<Component>(1 + additionalLines.length);
        lines.add(firstLine);
        Collections.addAll(lines, additionalLines);
        return lines;
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        return lines;
    }
}
