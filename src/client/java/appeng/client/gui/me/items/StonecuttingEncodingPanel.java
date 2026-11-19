package appeng.client.gui.me.items;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.SpriteRenderer;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.util.Icon;

/**
 * Implements the panel for encoding stonecutting recipes.
 */
public final class StonecuttingEncodingPanel extends EncodingModePanel {
    private static final Identifier BG = AppEng.makeId("background/pattern_mode_stonecutting");
    private static final Identifier BG_SLOT = AppEng.makeId("stonecutting_recipe");
    private static final Identifier BG_SLOT_SELECTED = AppEng.makeId("stonecutting_recipe_selected");
    private static final Identifier BG_SLOT_HOVER = AppEng.makeId("stonecutting_recipe_hover");

    private static final int RECIPE_WIDTH = 20;
    private static final int RECIPE_HEIGHT = 22;
    private static final int COLS = 4;
    private static final int ROWS = 2;

    private final Scrollbar scrollbar;

    public StonecuttingEncodingPanel(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
        this.scrollbar = widgets.addScrollBar("stonecuttingPatternModeScrollbar", Scrollbar.SMALL);
        this.scrollbar.setRange(0, 0, COLS);
        this.scrollbar.setCaptureMouseWheel(false);
    }

    @Override
    public void updateBeforeRender() {
        // Set up the scroll bar to have a range only for the rows outside the viewport
        var totalRows = (menu.getStonecuttingRecipes().size() + COLS - 1) / COLS;
        scrollbar.setRange(0, totalRows - ROWS, ROWS);
    }

    @Override
    public void drawBackgroundLayer(GuiGraphicsExtractor guiGraphics, Rect2i bounds, Point mouse) {
        SpriteRenderer.of(guiGraphics).blitSprite(BG, bounds.getX() + 8, bounds.getY() + bounds.getHeight() - 165, -1);
        drawRecipes(guiGraphics, bounds, mouse);
    }

    private RegistryAccess getRegistryAccess() {
        return Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
    }

    private void drawRecipes(GuiGraphicsExtractor guiGraphics, Rect2i bounds, Point mouse) {
        var recipes = menu.getStonecuttingRecipes();
        var startIndex = scrollbar.getCurrentScroll() * COLS;
        var endIndex = startIndex + ROWS * COLS;

        var selectedRecipe = menu.getStonecuttingRecipeId();

        for (int i = startIndex; i < endIndex && i < recipes.size(); ++i) {
            var slotBounds = getRecipeBounds(i - startIndex);

            var recipe = recipes.get(i);
            boolean selected = selectedRecipe != null && selectedRecipe.equals(recipe.id());

            var sprite = BG_SLOT;
            if (selected) {
                sprite = BG_SLOT_SELECTED;
            } else if (mouse.isIn(slotBounds)) {
                sprite = BG_SLOT_HOVER;
            }

            var renderX = bounds.getX() + slotBounds.getX();
            var renderY = bounds.getY() + slotBounds.getY();
            SpriteRenderer.of(guiGraphics).blitSprite(sprite, renderX, renderY, -1);
            ItemStack resultItem = recipe.value().result().create();

            if (selected || mouse.isIn(slotBounds)) {
                guiGraphics.item(resultItem, renderX + 2, renderY + 3);
                guiGraphics.itemDecorations(Minecraft.getInstance().font, resultItem, renderX + 2, renderY + 3);
            } else {
                guiGraphics.item(resultItem, renderX + 2, renderY + 2);
                guiGraphics.itemDecorations(Minecraft.getInstance().font, resultItem, renderX + 2, renderY + 2);
            }
        }
    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        var recipe = getRecipeAt(mousePos);
        if (recipe != null) {
            menu.setStonecuttingRecipeId(recipe.id());

            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        var recipe = getRecipeAt(new Point(mouseX, mouseY));
        if (recipe != null) {
            var lines = screen.getTooltipFromContainerItem(recipe.value().result().create());

            return new Tooltip(lines);
        }
        return null;
    }

    @Nullable
    private RecipeHolder<StonecutterRecipe> getRecipeAt(Point point) {
        var recipes = menu.getStonecuttingRecipes();

        if (!recipes.isEmpty()) {
            var startIndex = scrollbar.getCurrentScroll() * COLS;
            var endIndex = startIndex + COLS * ROWS;

            for (int i = startIndex; i < endIndex && i < recipes.size(); ++i) {
                var slotBounds = getRecipeBounds(i - startIndex);
                if (point.isIn(slotBounds)) {
                    return recipes.get(i);
                }
            }
        }

        return null;
    }

    // Return bounds of a recipe slot relative to the screen
    private Rect2i getRecipeBounds(int index) {
        var col = index % COLS;
        var row = index / COLS;
        int slotX = x + 26 + col * RECIPE_WIDTH;
        int slotY = y + 12 + row * RECIPE_HEIGHT;
        return new Rect2i(slotX, slotY, RECIPE_WIDTH, RECIPE_HEIGHT);
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        return scrollbar.onMouseWheel(mousePos, delta);
    }

    @Override
    Icon getIcon() {
        return Icon.TAB_STONECUTTING;
    }

    @Override
    public Component getTabTooltip() {
        return GuiText.StonecuttingPattern.text();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        scrollbar.setVisible(visible);
        screen.setSlotsHidden(SlotSemantics.STONECUTTING_INPUT, !visible);
    }
}
