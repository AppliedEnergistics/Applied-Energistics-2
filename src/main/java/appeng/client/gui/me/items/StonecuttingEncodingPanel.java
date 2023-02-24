package appeng.client.gui.me.items;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;

/**
 * Implements the panel for encoding stonecutting recipes.
 */
public final class StonecuttingEncodingPanel extends EncodingModePanel {
    private static final Blitter BG = Blitter.texture("guis/pattern_modes.png").src(0, 141, 126, 68);
    private static final Blitter BG_SLOT = BG
            .copy()
            .src(126, 141, 16, 18);
    private static final Blitter BG_SLOT_SELECTED = BG
            .copy()
            .src(126, 159, 16, 18);
    private static final Blitter BG_SLOT_HOVER = BG
            .copy()
            .src(126, 177, 16, 18);

    private static final int COLS = 4;
    private static final int ROWS = 3;

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
    public void drawBackgroundLayer(PoseStack poseStack, Rect2i bounds, Point mouse) {
        BG.dest(bounds.getX() + 9, bounds.getY() + bounds.getHeight() - 164).blit(poseStack);

        drawRecipes(poseStack, bounds, mouse);

    }

    private RegistryAccess getRegistryAccess() {
        return Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
    }

    private void drawRecipes(PoseStack poseStack, Rect2i bounds, Point mouse) {
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
            blitter.dest(renderX, renderY - 1).blit(poseStack);
            minecraft.getItemRenderer().renderAndDecorateItem(poseStack, recipe.getResultItem(getRegistryAccess()),
                    renderX, renderY);
        }
    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        var recipe = getRecipeAt(mousePos);
        if (recipe != null) {
            menu.setStonecuttingRecipeId(recipe.getId());
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
            var lines = screen.getTooltipFromItem(recipe.getResultItem(getRegistryAccess()));
            return new Tooltip(lines);
        }
        return null;
    }

    @Nullable
    private StonecutterRecipe getRecipeAt(Point point) {
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
        int slotX = x + 44 + col * BG_SLOT.getSrcWidth();
        int slotY = y + 8 + row * BG_SLOT.getSrcHeight();
        return new Rect2i(slotX, slotY, BG_SLOT.getSrcWidth(), BG_SLOT.getSrcHeight());
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        return scrollbar.onMouseWheel(mousePos, delta);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(Items.STONECUTTER);
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
