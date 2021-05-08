package appeng.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import appeng.core.localization.GuiText;

/**
 * Models a tooltip shown by a custom widget or button.
 */
public final class Tooltip {

    private final List<ITextComponent> content;

    public Tooltip(List<ITextComponent> content) {
        this.content = content;
    }

    public Tooltip(ITextComponent content) {
        this.content = Collections.singletonList(content);
    }

    public Tooltip(GuiText text) {
        String[] lines = text.getLocal().split("\n");

        this.content = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            StringTextComponent tc = new StringTextComponent(lines[i]);
            if (i == 0) {
                this.content.add(tc);
            } else {
                this.content.add(tc.mergeStyle(TextFormatting.GRAY));
            }
        }
    }

    /**
     * The tooltip content.
     */
    public List<ITextComponent> getContent() {
        return content;
    }

}
