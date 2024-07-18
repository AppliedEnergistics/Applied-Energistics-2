package appeng.client.guidebook.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.AlignItems;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.render.RenderContext;

public class LayoutsTest {

    private static class TestCase {
        LayoutContext context;
        List<LytBlock> children;
        int x, y, availableWidth;
        int paddingLeft, paddingTop, paddingRight, paddingBottom;
        int gap;
        LytRect expectedOutput;
        List<LytRect> expectedChildBounds;

        TestCase(LayoutContext context, List<LytBlock> children,
                int x, int y, int availableWidth,
                int paddingLeft, int paddingTop, int paddingRight, int paddingBottom,
                int gap, LytRect expectedOutput, List<LytRect> expectedChildBounds) {
            this.context = context;
            this.children = children;
            this.x = x;
            this.y = y;
            this.availableWidth = availableWidth;
            this.paddingLeft = paddingLeft;
            this.paddingTop = paddingTop;
            this.paddingRight = paddingRight;
            this.paddingBottom = paddingBottom;
            this.gap = gap;
            this.expectedOutput = expectedOutput;
            this.expectedChildBounds = expectedChildBounds;
        }
    }

    private static class TestBox extends LytBlock {
        private final int layoutWidth;
        private final int layoutHeight;

        public TestBox(int layoutWidth, int layoutHeight) {
            this.layoutWidth = layoutWidth;
            this.layoutHeight = layoutHeight;
        }

        @Override
        protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
            return new LytRect(x, y, layoutWidth, layoutHeight);
        }

        @Override
        protected void onLayoutMoved(int deltaX, int deltaY) {
        }

        @Override
        public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        }

        @Override
        public void render(RenderContext context) {
        }
    }

    static Stream<Arguments> horizontalLayoutTestCases() {
        var context = new LayoutContext(new MockFontMetrics());

        var child1 = new TestBox(30, 20);
        var child2 = new TestBox(40, 15);
        var child3 = new TestBox(20, 10);
        return Stream.of(
                Arguments.of(
                        "Empty container only has padding",
                        new TestCase(
                                context,
                                List.of(),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 10, 18),
                                List.of())),
                Arguments.of(
                        "Single child",
                        new TestCase(
                                context,
                                List.of(child1),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 40, 38),
                                List.of(
                                        new LytRect(40, 96, 30, 20)))),
                Arguments.of(
                        "Two children",
                        new TestCase(
                                context,
                                List.of(child1, child2),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 90, 38),
                                List.of(
                                        // expected child1 bounds
                                        new LytRect(40, 96, 30, 20),
                                        // expected child2 bounds
                                        // left -> child1 x+width+gap
                                        new LytRect(80, 96, 40, 15)))),
                Arguments.of(
                        "Three children (wrapping)",
                        new TestCase(
                                context,
                                List.of(child1, child2, child3),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 90, 58),
                                List.of(
                                        // expected child1 bounds
                                        new LytRect(40, 96, 30, 20),
                                        // expected child2 bounds
                                        new LytRect(80, 96, 40, 15),
                                        // expected child3 bounds
                                        new LytRect(40, 126, 20, 10)))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("horizontalLayoutTestCases")
    void testHorizontalLayout(String testName, TestCase testCase) {
        var result = Layouts.horizontalLayout(
                testCase.context,
                testCase.children,
                testCase.x, testCase.y, testCase.availableWidth,
                testCase.paddingLeft, testCase.paddingTop, testCase.paddingRight, testCase.paddingBottom,
                testCase.gap,
                AlignItems.START);
        assertEquals(testCase.expectedOutput, result, "outer bounds are not as expected");

        for (int i = 0; i < testCase.children.size(); i++) {
            assertEquals(
                    testCase.expectedChildBounds.get(i),
                    testCase.children.get(i).getBounds(),
                    "bounds of child " + i + " are not as expected");
        }
    }

    static Stream<Arguments> verticalLayoutTestCases() {
        var context = new LayoutContext(new MockFontMetrics());

        var child1 = new TestBox(30, 20);
        var child2 = new TestBox(40, 15);
        var child3 = new TestBox(20, 10);
        return Stream.of(
                Arguments.of(
                        "Empty container only has padding",
                        new TestCase(
                                context,
                                List.of(),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 10, 18),
                                List.of())),
                Arguments.of(
                        "Single child",
                        new TestCase(
                                context,
                                List.of(child1),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 40, 38),
                                List.of(
                                        new LytRect(40, 96, 30, 20)))),
                Arguments.of(
                        "Two children",
                        new TestCase(
                                context,
                                List.of(child1, child2),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 50, 63),
                                List.of(
                                        // expected child1 bounds
                                        new LytRect(40, 96, 30, 20),
                                        // expected child2 bounds
                                        // top -> child1 y+height+gap
                                        new LytRect(40, 126, 40, 15)))),
                Arguments.of(
                        "Three children (no wrapping)",
                        new TestCase(
                                context,
                                List.of(child1, child2, child3),
                                37, 91, 100,
                                // Prime Number Padding
                                3, 5, 7, 13,
                                10,
                                // Expected outer layout box, sum of padding
                                new LytRect(37, 91, 50, 83),
                                List.of(
                                        // expected child1 bounds
                                        new LytRect(40, 96, 30, 20),
                                        // expected child2 bounds
                                        new LytRect(40, 126, 40, 15),
                                        // expected child3 bounds
                                        new LytRect(40, 151, 20, 10)))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("verticalLayoutTestCases")
    void testVerticalLayout(String testName, TestCase testCase) {
        var result = Layouts.verticalLayout(
                testCase.context,
                testCase.children,
                testCase.x, testCase.y, testCase.availableWidth,
                testCase.paddingLeft, testCase.paddingTop, testCase.paddingRight, testCase.paddingBottom,
                testCase.gap,
                AlignItems.START);
        assertEquals(testCase.expectedOutput, result, "outer bounds are not as expected");

        for (int i = 0; i < testCase.children.size(); i++) {
            assertEquals(
                    testCase.expectedChildBounds.get(i),
                    testCase.children.get(i).getBounds(),
                    "bounds of child " + i + " are not as expected");
        }
    }
}
