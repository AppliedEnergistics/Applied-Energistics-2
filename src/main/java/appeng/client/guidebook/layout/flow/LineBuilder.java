package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.document.flow.LytFlowText;
import appeng.client.guidebook.layout.LayoutContext;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

class LineBuilder implements Consumer<LytFlowContent> {
    private final LayoutContext context;
    private final List<Line> lines;
    private int innerX;
    private int innerY;
    private final int lineWidth;
    private int remainingLineWidth;
    @Nullable
    private LineElement openLineElement;
    private WhiteSpaceMode whiteSpaceMode = WhiteSpaceMode.NORMAL;

    public LineBuilder(LayoutContext context, List<Line> lines) {
        this.context = context;
        innerX = context.available().x();
        innerY = context.available().y();
        lineWidth = context.available().width();
        remainingLineWidth = lineWidth;
        this.lines = lines;
    }

    @Override
    public void accept(LytFlowContent content) {
        if (content instanceof LytFlowText text) {
            appendText(text.getText(), text.getParentSpan());
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

    private void appendText(String text, LytFlowSpan parentSpan) {
        var style = parentSpan.getEffectiveTextStyle();

        char lastChar = '\0';
        if (getEndOfOpenLine() instanceof LineTextRun textRun && !textRun.text.isEmpty()) {
            lastChar = textRun.text.charAt(textRun.text.length() - 1);
        }

        iterateRuns(text, style, lastChar, remainingLineWidth, lineWidth, (run, width, endLine) -> {
            var el = new LineTextRun(run.toString(), style);
            el.bounds = new LytRect(
                    innerX,
                    innerY,
                    Math.round(width),
                    context.getLineHeight(style)
            );
            appendToOpenLine(el);
            if (endLine) {
                closeLine();
            }
        });
    }

    private void iterateRuns(CharSequence text, Style style, char lastChar, float currentLineMaxWidth, float maxWidth, LineConsumer consumer) {
        int lastBreakOpportunity = 0;
        float widthAtBreakOpportunity = 0;
        float remainingSpace = currentLineMaxWidth;
        float curLineWidth = 0;

        var lineBuffer = new StringBuilder();

        boolean lastCharWasWhitespace = Character.isWhitespace(lastChar);

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
            // TODO: Fishy? Because HTML formatting ignores these
            if (codePoint == '\n') {
                if (whiteSpaceMode.isCollapseSegmentBreaks()) {
                    codePoint = ' ';
                } else {
                    consumer.visitRun(lineBuffer, curLineWidth, true);
                    lineBuffer.setLength(0);
                    widthAtBreakOpportunity = curLineWidth = 0;
                    lastBreakOpportunity = 0;
                    lastCharWasWhitespace = true;
                    remainingSpace = maxWidth;
                    continue;
                }
            }

            // Treat spaces as a safe-point for going back to when needing to line-break later
            if (Character.isWhitespace(codePoint)) {
                if (lastCharWasWhitespace && whiteSpaceMode.isCollapseWhitespace()) {
                    continue; // White space collapsing
                }
                // Skip if the last one was a space already
                lastBreakOpportunity = lineBuffer.length();
                widthAtBreakOpportunity = curLineWidth;
                lastCharWasWhitespace = true;
            } else {
                lastCharWasWhitespace = false;
            }

            var advance = context.getAdvance(codePoint, style);
            // Break line if necessary
            if (curLineWidth + advance > remainingSpace) {
                // If we had a break opportunity, use it
                // In this scenario, the space itself is discarded
                if (lastBreakOpportunity > 0) {
                    consumer.visitRun(lineBuffer.subSequence(0, lastBreakOpportunity), widthAtBreakOpportunity, true);
                    curLineWidth -= widthAtBreakOpportunity;
                    lineBuffer.delete(0, lastBreakOpportunity + 1);
                } else {
                    // We exceeded the line length, but did not find a break opportunity
                    // this causes a forced break mid-word
                    consumer.visitRun(lineBuffer, curLineWidth, true);
                    lineBuffer.setLength(0);
                    curLineWidth = advance;
                }
                lastBreakOpportunity = 0;
                widthAtBreakOpportunity = curLineWidth;
                remainingSpace = maxWidth;
                // If a white-space character broke the line, ignore it as it
                // would otherwise be at the start of the next line
                if (lastCharWasWhitespace) {
                    continue;
                }
            } else {
                curLineWidth += advance;
            }
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

        var lineBounds = openLineElement.bounds;
        for (var l = openLineElement.next; l != null; l = l.next) {
            lineBounds = LytRect.union(lineBounds, l.bounds);
        }

        var line = new Line(lineBounds, openLineElement);
        lines.add(line);

        openLineElement = null;
        remainingLineWidth = lineWidth;
        innerX = context.available().x();
        innerY += line.bounds().height();
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
        innerX += el.bounds.width();
        remainingLineWidth -= el.bounds.width();
    }

    public void end() {
        closeLine();
    }

    @FunctionalInterface
    interface LineConsumer {
        void visitRun(CharSequence run, float width, boolean end);
    }

}
