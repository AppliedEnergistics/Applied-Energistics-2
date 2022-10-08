package appeng.client.gui.me.items;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.Scrollbar;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;

public final class StonecuttingEncoding implements ICompositeWidget {
    private static final Blitter BG_SLOT = PatternEncodingTermScreen.STONECUTTING_MODE_BG
            .copy()
            .src(126, 141, 16, 18);
    private static final Blitter BG_SLOT_SELECTED = BG_SLOT
            .copy()
            .src(126, 159, 16, 18);
    private static final Blitter BG_SLOT_HOVER = BG_SLOT
            .copy()
            .src(126, 177, 16, 18);

    private static final int COLS = 4;
    private static final int ROWS = 3;

    private final PatternEncodingTermMenu menu;
    private final Scrollbar scrollbar;

    private int x;
    private int y;

    public StonecuttingEncoding(PatternEncodingTermMenu menu, WidgetContainer widgets) {
        this.menu = menu;
        this.scrollbar = widgets.addScrollBar("stonecuttingPatternModeScrollbar", Scrollbar.SMALL);
        this.scrollbar.setRange(0, 0, COLS);
        this.scrollbar.setCaptureMouseWheel(false);
    }

    @Override
    public boolean isVisible() {
        scrollbar.setVisible(menu.getMode() == EncodingMode.STONECUTTING);
        return menu.getMode() == EncodingMode.STONECUTTING;
    }

    @Override
    public void updateBeforeRender() {
        // Set up the scroll bar to have a range only for the rows outside the viewport
        var totalRows = (menu.getStonecuttingRecipes().size() + COLS - 1) / COLS;
        scrollbar.setRange(0, totalRows - ROWS, ROWS);
    }

    @Override
    public void drawBackgroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {

        var recipes = menu.getStonecuttingRecipes();
        var startIndex = scrollbar.getCurrentScroll() * COLS;
        var endIndex = startIndex + ROWS * COLS;

        var minecraft = Minecraft.getInstance();
        var selectedRecipe = menu.getStonecuttingRecipeId();

        for (int i = startIndex; i < endIndex && i < recipes.size(); ++i) {
            var slotBounds = getRecipeBounds(i - startIndex);

            var recipe = recipes.get(i);
            boolean selected = selectedRecipe != null && selectedRecipe.equals(recipe.getId());

            Blitter blitter = BG_SLOT;
            if (selected) {
                blitter = BG_SLOT_SELECTED;
            } else if (mouse.isIn(slotBounds)) {
                blitter = BG_SLOT_HOVER;
            }

            var renderX = bounds.getX() + slotBounds.getX();
            var renderY = bounds.getY() + slotBounds.getY();
            blitter.dest(renderX, renderY - 1).blit(poseStack, zIndex);
            minecraft.getItemRenderer().renderAndDecorateItem(recipe.getResultItem(), renderX, renderY);
        }

    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        var recipes = menu.getStonecuttingRecipes();

        if (!menu.getStonecuttingRecipes().isEmpty()) {
            var startIndex = scrollbar.getCurrentScroll() * COLS;
            var endIndex = startIndex + COLS * ROWS;

            for (int i = startIndex; i < endIndex && i < recipes.size(); ++i) {
                var slotBounds = getRecipeBounds(i - startIndex);
                if (mousePos.isIn(slotBounds)) {
                    menu.setStonecuttingRecipeId(recipes.get(i).getId());
                    Minecraft.getInstance().getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    return true;
                }
            }
        }

        return false;
    }

    // Return bounds of a recipe slot relative to the screen
    private Rect2i getRecipeBounds(int index) {
        var col = index % COLS;
        var row = index / COLS;
        int slotX = x + 44 + col * BG_SLOT.getSrcWidth();
        int slotY = y + 8 + row * BG_SLOT.getSrcHeight();
        return new Rect2i(slotX, slotY, BG_SLOT.getSrcWidth(), BG_SLOT.getSrcHeight());
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        return scrollbar.onMouseWheel(mousePos, delta);
    }

    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, 126, 68);
    }
}
