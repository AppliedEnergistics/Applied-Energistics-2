package appeng.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

/**
 * Models a tooltip shown by a custom widget or button.
 */
public final class Tooltip {

    private final List<ITextComponent> content;

    public Tooltip(List<ITextComponent> unprocessedLines) {
        this.content = new ArrayList<>(unprocessedLines.size());

        // Split translated lines at line-breaks found in the translated text
        for (ITextComponent unprocessedLine : unprocessedLines) {
            splitLine(unprocessedLine, this.content);
        }
    }

    private static void splitLine(ITextComponent unprocessedLine, List<ITextComponent> lines) {
        LineSplittingVisitor visitor = new LineSplittingVisitor(lines);
        unprocessedLine.getComponentWithStyle(visitor, Style.EMPTY);
        visitor.flush();
    }

    public Tooltip(ITextComponent... content) {
        this(Arrays.asList(content));
    }

    /**
     * The tooltip content.
     */
    public List<ITextComponent> getContent() {
        return content;
    }

    private static class LineSplittingVisitor implements ITextProperties.IStyledTextAcceptor<Object> {
        private final List<ITextComponent> lines;

        private IFormattableTextComponent currentPart;

        public LineSplittingVisitor(List<ITextComponent> lines) {
            this.lines = lines;
        }

        @Override
        public Optional<Object> accept(Style style, String text) {
            // a new-line character in the text should split the line and "flush" the
            // current part into the lines list
            String[] parts = text.split("\n", -1);
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    flush();
                }

                String line = parts[i];
                IFormattableTextComponent part = new StringTextComponent(line).setStyle(style);
                if (currentPart != null) {
                    currentPart = currentPart.append(part);
                } else {
                    currentPart = part;
                }
            }
            return Optional.empty();
        }

        public void flush() {
            if (currentPart != null) {
                // Set default tooltip styles only if no explicit style has been set
                if (currentPart.getStyle() == Style.EMPTY) {
                    // First line should be white, other lines gray
                    if (lines.isEmpty()) {
                        currentPart.setStyle(Style.EMPTY.applyFormatting(TextFormatting.WHITE));
                    } else {
                        currentPart.setStyle(Style.EMPTY.applyFormatting(TextFormatting.GRAY));
                    }
                }

                lines.add(currentPart);
                currentPart = null;
            }
        }
    }
}
