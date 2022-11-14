package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.document.flow.LytFlowText;
import appeng.client.guidebook.layout.LayoutContext;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class LineBuilderTest {
    /**
     * When a white-space character causes a line-break, it is removed.
     */
    @Test
    void testWhitespaceCausingLineBreakGetsRemoved() {
        var lines = getLines("A B", 1);

        assertThat(lines).extracting(this::getTextContent).containsExactly(
                "A",
                "B"
        );
    }

    /**
     * Test white-space collapsing.
     */
    @Test
    void testWhitespaceCollapsing() {
        var lines = getLines("A  B", 3);

        assertThat(lines).extracting(this::getTextContent).containsExactly(
                "A B"
        );
    }

    @NotNull
    private static ArrayList<Line> getLines(String text, int charsPerLine) {
        var lines = new ArrayList<Line>();
        var available = new LytRect(0, 0, charsPerLine * 5, 50);
        var context = new MockLayoutContext(available);
        var lineBuilder = new LineBuilder(context, lines);
        var flowContent = new LytFlowText();
        flowContent.setText(text);
        flowContent.setParentSpan(new LytFlowSpan());
        lineBuilder.accept(flowContent);
        lineBuilder.end();
        return lines;
    }

    private String getTextContent(Line line) {
        var result = new StringBuilder();
        for (var el = line.firstElement(); el != null; el = el.next) {
            if (el instanceof LineTextRun run) {
                result.append(run.text);
            }
        }
        return result.toString();
    }

    // Every character is 5 pixels wide
    record MockLayoutContext(LytRect available) implements LayoutContext {
        @Override
        public LytRect available() {
            return available;
        }

        @Override
        public LytRect viewport() {
            return available;
        }

        @Override
        public LayoutContext withAvailable(LytRect available) {
            return new MockLayoutContext(available);
        }

        @Override
        public float getAdvance(int codePoint, Style style) {
            return 5;
        }

        @Override
        public int getLineHeight(Style style) {
            return 10;
        }
    }
}
