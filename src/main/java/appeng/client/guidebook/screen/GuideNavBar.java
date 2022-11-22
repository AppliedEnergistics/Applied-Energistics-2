package appeng.client.guidebook.screen;

import appeng.client.guidebook.GuidebookManager;
import appeng.client.guidebook.navigation.NavigationNode;
import appeng.client.guidebook.navigation.NavigationTree;
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

    public GuideNavBar(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Navigation Tree"));
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

        fillGradient(poseStack, x, y, x + width, y + height, 0xFF000000, 0x7F000000);

        enableScissor(x, y, width, height);
        var currentY = y;
        for (var row : rows) {
            for (var line : row.textLines) {
                font.draw(poseStack, line, x, currentY, -1);
                currentY += font.lineHeight;
            }
        }
        disableScissor();
    }

    private void recreateRows() {
        this.navTree = GuidebookManager.INSTANCE.getNavigationTree();
        // Save Freeze expanded / scroll position
        this.rows.clear();
        var font = Minecraft.getInstance().font;

        for (var rootNode : this.navTree.getRootNodes()) {
            var label = Component.literal(rootNode.title());
            var lines = font.split(label, width);
            rows.add(new Row(lines, rootNode));
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        super.renderBg(poseStack, minecraft, mouseX, mouseY);
    }

    private class Row {
        private final List<FormattedCharSequence> textLines;
        private final NavigationNode node;
        private boolean expanded;

        public Row(List<FormattedCharSequence> textLines, NavigationNode node) {
            this.textLines = textLines;
            this.node = node;
        }
    }
}
