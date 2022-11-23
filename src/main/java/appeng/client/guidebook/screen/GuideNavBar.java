package appeng.client.guidebook.screen;

import appeng.client.guidebook.GuidebookManager;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.navigation.NavigationNode;
import appeng.client.guidebook.navigation.NavigationTree;
import appeng.client.guidebook.render.LightDarkMode;
import appeng.client.guidebook.render.SimpleRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class GuideNavBar extends AbstractWidget {
    private NavigationTree navTree;

    private final List<Row> rows = new ArrayList<>();

    private final GuideScreen screen;

    public GuideNavBar(GuideScreen screen, int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Navigation Tree"));
        this.screen = screen;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return true;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // Check if we need to re-layout
        var currentNavTree = GuidebookManager.INSTANCE.getNavigationTree();
        if (currentNavTree != this.navTree) {
            recreateRows();
        }

        var font = Minecraft.getInstance().font;

        var renderContext = new SimpleRenderContext(screen, LytRect.empty(), poseStack, LightDarkMode.LIGHT_MODE);

        fillGradient(poseStack, x, y, x + width, y + height, 0xFF000000, 0x7F000000);

        enableScissor(x, y, width, height);
        for (var row : rows) {
            var bounds = row.bounds;
            boolean containsMouse = bounds.contains(mouseX, mouseY);

            var textLines = row.textLines;
            for (int i = 0; i < textLines.size(); i++) {
                font.draw(
                        poseStack,
                        textLines.get(i),
                        bounds.x(),
                        bounds.y() + i * font.lineHeight,
                        -1
                );
            }
        }
        disableScissor();
    }

    private void recreateRows() {
        this.navTree = GuidebookManager.INSTANCE.getNavigationTree();
        // Save Freeze expanded / scroll position
        this.rows.clear();

        for (var rootNode : this.navTree.getRootNodes()) {
            rows.add(new Row(rootNode));
        }

        updateLayout();
    }

    private void updateLayout() {
        var font = Minecraft.getInstance().font;

        var currentY = 0;
        for (var row : this.rows) {
            row.textLines = font.split(row.text, width);
            var height = row.textLines.size() * font.lineHeight;
            row.bounds = new LytRect(
                    this.x,
                    currentY,
                    width,
                    height
            );
            currentY += height;
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        super.renderBg(poseStack, minecraft, mouseX, mouseY);
    }

    private class Row {
        private final Component text;
        private final NavigationNode node;
        private List<FormattedCharSequence> textLines = List.of();
        private boolean expanded;
        private LytRect bounds = LytRect.empty();

        public Row(NavigationNode node) {
            this.node = node;
            this.text = Component.literal(node.title())
                    .withStyle(s -> s.withFont(Minecraft.UNIFORM_FONT));
        }
    }
}
