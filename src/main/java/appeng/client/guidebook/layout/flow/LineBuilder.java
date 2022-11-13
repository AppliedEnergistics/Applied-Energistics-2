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

    private void appendText(String text, LytFlowSpan parentSpan) {
        var style = parentSpan.getEffectiveTextStyle();

        iterateRuns(text, style, remainingLineWidth, lineWidth, (from, to, width, endLine) -> {
            var el = new LineTextRun(text.substring(from, to), style);
            el.bounds = new LytRect(
                    innerX,
                    innerY,
                    Math.round(width),
                    context.getLineHeight(style)
            );
            innerX += el.bounds.width();
            appendToOpenLine(el);
            if (endLine) {
                closeLine();
            }
        });
    }

    private void iterateRuns(CharSequence text, Style style, float firstLineMaxWidth, float maxWidth, LineConsumer consumer) {
        int curLineStart = 0;
        int lastBreakOpportunity = 0;
        float widthAtBreakOpportunity = 0;
        float curLineWidth = maxWidth - firstLineMaxWidth;
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
                consumer.visitRun(curLineStart, i, curLineWidth, true);
                curLineWidth = 0;
                widthAtBreakOpportunity = 0;
                curLineStart = i + 1;
                lastBreakOpportunity = i + 1;
                continue;
            }

            var advance = context.getAdvance(codePoint, style);
            if (Character.isSpaceChar(codePoint)) {
                lastBreakOpportunity = i;
                widthAtBreakOpportunity = curLineWidth;
            }
            curLineWidth += advance;
            if (curLineWidth > maxWidth) {
                // Break line!
                if (curLineStart != lastBreakOpportunity) {
                    consumer.visitRun(curLineStart, lastBreakOpportunity, widthAtBreakOpportunity, true);
                    curLineWidth -= widthAtBreakOpportunity;
                    curLineStart = ++lastBreakOpportunity;
                    widthAtBreakOpportunity = curLineWidth;
                } else {
                    // We exceeded the line length, but can't break
                    consumer.visitRun(curLineStart, i + 1, curLineWidth, true);
                    widthAtBreakOpportunity = curLineWidth = 0;
                    lastBreakOpportunity = curLineStart = i + 1;
                }
            }
        }

        if (curLineStart < text.length()) {
            consumer.visitRun(curLineStart, text.length(), curLineWidth, false);
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
    }

    public void end() {
        closeLine();
    }

    @FunctionalInterface
    interface LineConsumer {
        void visitRun(int from, int to, float width, boolean end);
    }
}
