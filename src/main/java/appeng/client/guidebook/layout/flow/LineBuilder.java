package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.DefaultStyles;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.InlineBlockAlignment;
import appeng.client.guidebook.document.flow.LytFlowBreak;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowInlineBlock;
import appeng.client.guidebook.document.flow.LytFlowText;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.style.ResolvedTextStyle;
import appeng.client.guidebook.style.TextAlignment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Does inline-flow layout similar to how it is described here:
 * https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Flow_Layout/Block_and_Inline_Layout_in_Normal_Flow
 */
class LineBuilder implements Consumer<LytFlowContent> {
    private final LayoutContext context;
    private final List<Line> lines;
    // Contains any floating elements we construct as part of processing flow content
    private final List<LineBlock> floats;
    private final int lineBoxX;
    private final int startY;
    private int innerX;
    private int lineBoxY;
    private final int lineBoxWidth;
    private int remainingLineWidth;
    @Nullable
    private LineElement openLineElement;
    private final TextAlignment alignment;

    public LineBuilder(LayoutContext context,
                       int x,
                       int y,
                       int availableWidth,
                       List<Line> lines,
                       List<LineBlock> floats,
                       TextAlignment alignment) {
        this.floats = floats;
        this.alignment = alignment;
        this.context = context;
        this.startY = y;
        lineBoxX = x;
        lineBoxY = y;
        lineBoxWidth = availableWidth;
        remainingLineWidth = getAvailableHorizontalSpace();
        this.lines = lines;
    }

    @Override
    public void accept(LytFlowContent content) {
        if (content instanceof LytFlowText text) {
            appendText(text.getText(), content);
        } else if (content instanceof LytFlowBreak) {
            appendBreak(content);
        } else if (content instanceof LytFlowInlineBlock inlineBlock) {
            appendInlineBlock(inlineBlock);
        } else {
            throw new IllegalArgumentException("Don't know how to layout flow content: " + content);
        }
    }

    private void appendBreak(@Nullable LytFlowContent flowContent) {
        // Append an empty line with the default style
        if (openLineElement == null) {
            openLineElement = new LineTextRun("", DefaultStyles.BASE_STYLE, DefaultStyles.BASE_STYLE);
            openLineElement.flowContent = flowContent;
        }
        closeLine();
    }

    private void appendInlineBlock(LytFlowInlineBlock inlineBlock) {
        var size = inlineBlock.getPreferredSize(lineBoxWidth);
        var block = inlineBlock.getBlock();
        var marginLeft = block.getMarginLeft();
        var marginRight = block.getMarginRight();
        var marginTop = block.getMarginTop();
        var marginBottom = block.getMarginBottom();

        // Is there enough space to have this element here?
        var outerWidth = size.width() + marginLeft + marginRight;
        ensureSpaceIsAvailable(outerWidth);

        var el = new LineBlock(block);
        el.bounds = new LytRect(innerX + marginLeft, marginTop, size.width(), size.height());
        el.flowContent = inlineBlock;

        if (inlineBlock.getAlignment() == InlineBlockAlignment.FLOAT_LEFT) {
            // Float it to the left of the actual text content.
            // endLine will take care of moving any existing text in the line
            el.bounds = el.bounds.withX(getInnerLeftEdge() + marginLeft).withY(lineBoxY + marginTop);
            el.floating = true;
            context.addLeftFloat(el.bounds.expand(0, 0, marginRight, marginBottom));
            floats.add(el);
            remainingLineWidth -= outerWidth;
        } else if (inlineBlock.getAlignment() == InlineBlockAlignment.FLOAT_RIGHT) {
            // Float it to the right the actual text content.
            el.bounds = el.bounds.withX(getInnerRightEdge() - el.bounds.width() + marginRight).withY(lineBoxY + marginTop);
            el.floating = true;
            context.addRightFloat(el.bounds.expand(marginLeft, 0, 0, marginBottom));
            floats.add(el);
            remainingLineWidth -= outerWidth;
        } else {
            // Treat as a normal inline element for positioning
            innerX += size.width();
            appendToOpenLine(el);

            // Since no margin is actually accounted for here, the remaining line width should just
            // be reduced
            remainingLineWidth -= size.width();
        }
    }

    private void ensureSpaceIsAvailable(int width) {
        if (width <= remainingLineWidth) {
            return; // Got enough
        }

        // First, try closing out any open line if we don't have enough space
        closeLine();

        if (width <= remainingLineWidth) {
            return; // We got enough by ending the current line and advancing to the next
        }

        // If we *still* don't have enough room, we need to advance down to clear floats
        // as long as any float is still open
        var nextFloatEdge = context.getNextFloatBottomEdge(lineBoxY);
        while (nextFloatEdge.isPresent()) {
            lineBoxY = nextFloatEdge.getAsInt();
            context.clearFloatsAbove(lineBoxY);
            remainingLineWidth = getAvailableHorizontalSpace();
            if ( width <= remainingLineWidth) {
                break; // Finally, we're good!
            }
            nextFloatEdge = context.getNextFloatBottomEdge(lineBoxY);
        }
    }

    @Nullable
    private LineElement getEndOfOpenLine() {
        var el = openLineElement;
        if (el != null) {
            while (el.next != null) {
                el = el.next;
            }
        }
        return el;
    }

    private void appendText(String text, LytFlowContent flowContent) {
        var style = flowContent.resolveStyle();
        var hoverStyle = flowContent.resolveHoverStyle(style);

        char lastChar = '\0';
        var endOfOpenLine = getEndOfOpenLine();
        if (endOfOpenLine instanceof LineTextRun textRun && !textRun.text.isEmpty()) {
            lastChar = textRun.text.charAt(textRun.text.length() - 1);
        } else if (endOfOpenLine == null || endOfOpenLine.floating) {
            // Treat the first text in a line or text directly after a float as if it was after a line-break.
            lastChar = '\n';
        }

        iterateRuns(text, style, lastChar, (run, width, endLine) -> {
            if (!run.isEmpty()) {
                var el = new LineTextRun(run.toString(), style, hoverStyle);
                el.flowContent = flowContent;
                el.bounds = new LytRect(
                        innerX,
                        0,
                        Math.round(width),
                        context.getLineHeight(style));
                appendToOpenLine(el);
                innerX += el.bounds.width();
                remainingLineWidth -= el.bounds.width();
            }
            if (endLine) {
                closeLine();
            }
        });
    }

    private void iterateRuns(CharSequence text, ResolvedTextStyle style, char lastChar, LineConsumer consumer) {
        int lastBreakOpportunity = -1;
        float widthAtBreakOpportunity = 0;
        float curLineWidth = 0;

        var lineBuffer = new StringBuilder();

        boolean lastCharWasWhitespace = Character.isWhitespace(lastChar);
        // When starting after a whitespace on an existing line, we have a break opportunity at the start
        if (lastCharWasWhitespace) {
            lastBreakOpportunity = 0;
        }

        for (var i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int codePoint = ch;

            // UTF-16 surrogate handling
            if (Character.isHighSurrogate(ch) && i + 1 < text.length()) {
                // Always consume the next char if it's a low surrogate
                char low = text.charAt(i + 1);
                if (Character.isLowSurrogate(low)) {
                    i++; // Skip the low surrogate
                    codePoint = Character.toCodePoint(ch, low);
                }
            }

            // Handle explicit line breaks
            if (codePoint == '\n') {
                if (style.whiteSpace().isCollapseSegmentBreaks()) {
                    codePoint = ' ';
                } else {
                    consumer.visitRun(lineBuffer, curLineWidth, true);
                    lineBuffer.setLength(0);
                    widthAtBreakOpportunity = curLineWidth = 0;
                    lastBreakOpportunity = 0;
                    lastCharWasWhitespace = true;
                    remainingLineWidth = getAvailableHorizontalSpace();
                    continue;
                }
            }

            if (Character.isWhitespace(codePoint)) {
                // Skip if the last one was a space already
                if (lastCharWasWhitespace && style.whiteSpace().isCollapseWhitespace()) {
                    continue; // White space collapsing
                }
                // Treat spaces as a safe-point for going back to when needing to line-break later
                lastBreakOpportunity = lineBuffer.length();
                widthAtBreakOpportunity = curLineWidth;
                lastCharWasWhitespace = true;
            } else {
                lastCharWasWhitespace = false;
            }

            var advance = context.getAdvance(codePoint, style);
            // Break line if necessary
            if (curLineWidth + advance > remainingLineWidth) {
                // If we had a break opportunity, use it
                // In this scenario, the space itself is discarded
                if (lastBreakOpportunity != -1) {
                    consumer.visitRun(lineBuffer.subSequence(0, lastBreakOpportunity), widthAtBreakOpportunity, true);
                    curLineWidth -= widthAtBreakOpportunity;
                    lineBuffer.delete(0, lastBreakOpportunity);
                    if (!lineBuffer.isEmpty() && Character.isWhitespace(lineBuffer.charAt(0))) {
                        var firstChar = lineBuffer.charAt(0);
                        lineBuffer.deleteCharAt(0);
                        curLineWidth -= context.getAdvance(firstChar, style);
                    }
                } else {
                    // We exceeded the line length, but did not find a break opportunity
                    // this causes a forced break mid-word
                    consumer.visitRun(lineBuffer, curLineWidth, true);
                    lineBuffer.setLength(0);
                    curLineWidth = 0;
                }
                lastBreakOpportunity = 0;
                widthAtBreakOpportunity = curLineWidth;
                remainingLineWidth = getAvailableHorizontalSpace();
                // If a white-space character broke the line, ignore it as it
                // would otherwise be at the start of the next line
                if (lastCharWasWhitespace) {
                    continue;
                }
            }
            curLineWidth += advance;
            lineBuffer.appendCodePoint(codePoint);
        }

        if (!lineBuffer.isEmpty()) {
            consumer.visitRun(lineBuffer, curLineWidth, false);
        }
    }

    private void closeLine() {
        if (openLineElement == null) {
            return;
        }

        var lineHeight = 1;
        var lineWidth = 0;
        for (var el = openLineElement; el != null; el = el.next) {
            lineHeight = Math.max(lineHeight, el.bounds.bottom());
            lineWidth = Math.max(lineWidth, el.bounds.right());
        }

        var textAreaStart = getInnerLeftEdge();
        var textAreaEnd = getInnerRightEdge();

        // Apply alignment
        int xTranslation = textAreaStart;
        if (alignment == TextAlignment.RIGHT) {
            xTranslation = textAreaEnd - lineWidth;
        } else if (alignment == TextAlignment.CENTER) {
            xTranslation = textAreaStart + ((textAreaEnd - textAreaStart) - lineWidth) / 2;
        }

        // reposition all line elements
        for (var el = openLineElement; el != null; el = el.next) {
            el.bounds = el.bounds.move(xTranslation, lineBoxY);
        }

        var lineBounds = new LytRect(lineBoxX, lineBoxY, lineBoxWidth, lineHeight);
        var line = new Line(lineBounds, openLineElement);
        lines.add(line);

        // Advance vertically
        lineBoxY += line.bounds().height();

        // Close out any floats that are above the fold
        context.clearFloatsAbove(lineBoxY);

        // Reset horizontal position
        openLineElement = null;
        innerX = 0;

        // Recompute now that floats may have been closed, what the horizontal space really is
        remainingLineWidth = getInnerRightEdge() - getInnerLeftEdge();
    }

    // Clear any remaining floats
    private void clearFloats() {
        context.clearFloats(true, true)
                .ifPresent(floatBottom -> lineBoxY = Math.max(lineBoxY, floatBottom));
    }

    // How much horizontal space is available in a new line, accounting for active floats that take up space
    private int getAvailableHorizontalSpace() {
        return Math.max(0, getInnerRightEdge() - getInnerLeftEdge());
    }

    // Absolute X coord of the beginning of the text area of the current line box
    private int getInnerLeftEdge() {
        return context.getLeftFloatRightEdge().orElse(lineBoxX);
    }

    // Absolute X coord of the end of the text area of the current line box
    private int getInnerRightEdge() {
        return context.getRightFloatLeftEdge().orElse(this.lineBoxX + lineBoxWidth);
    }

    private void appendToOpenLine(LineElement el) {
        if (openLineElement != null) {
            var l = openLineElement;
            while (l.next != null) {
                l = l.next;
            }
            l.next = el;
        } else {
            openLineElement = el;
        }
    }

    public void end() {
        closeLine();
    }

    public LytRect getBounds() {
        return new LytRect(
                lineBoxX, startY,
                lineBoxWidth, lineBoxY - startY
        );
    }

    @FunctionalInterface
    interface LineConsumer {
        void visitRun(CharSequence run, float width, boolean end);
    }
}
