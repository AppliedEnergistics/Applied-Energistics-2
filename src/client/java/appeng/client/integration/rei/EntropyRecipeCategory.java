package appeng.client.integration.rei;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.integration.modules.rei.CategoryIds;
import appeng.integration.modules.rei.EntropyRecipeDisplay;
import appeng.items.tools.powered.EntropyManipulatorItem;

public class EntropyRecipeCategory implements DisplayCategory<EntropyRecipeDisplay> {
    private static final int PADDING = 5;
    private static final int BODY_TEXT_COLOR = 0x7E7E7E;

    @Override
    public CategoryIdentifier<? extends EntropyRecipeDisplay> getCategoryIdentifier() {
        return CategoryIds.ENTROPY_MANIPULATOR;
    }

    @Override
    public Renderer getIcon() {
        return (graphics, bounds, mouseX, mouseY, delta) -> graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                AppEng.makeId("textures/item/entropy_manipulator.png"),
                bounds.getX(),
                bounds.getY(),
                0,
                0,
                16, 16,
                16, 16);
    }

    @Override
    public Component getTitle() {
        return AEItems.ENTROPY_MANIPULATOR.asItem().getName();
    }

    @Override
    public List<Widget> setupDisplay(EntropyRecipeDisplay recipe, Rectangle bounds) {
        for (var ingredient : recipe.getConsumed()) {
            for (EntryStack<?> entryStack : ingredient) {
                entryStack.tooltip(ItemModText.CONSUMED.text().withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                addConsumedOverlay(entryStack);
            }
        }

        var mode = recipe.getRecipe().getMode();

        var widgets = new ArrayList<Widget>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var centerX = bounds.getCenterX();
        var y = bounds.getY() + PADDING;

        var labelText = switch (mode) {
            case HEAT -> ItemModText.ENTROPY_MANIPULATOR_HEAT.text(EntropyManipulatorItem.ENERGY_PER_USE);
            case COOL -> ItemModText.ENTROPY_MANIPULATOR_COOL.text(EntropyManipulatorItem.ENERGY_PER_USE);
        };
        var interaction = switch (mode) {
            case HEAT -> ItemModText.RIGHT_CLICK.text();
            case COOL -> ItemModText.SHIFT_RIGHT_CLICK.text();
        };

        var modeLabel = Widgets.createLabel(new Point(centerX + 4, y + 2), labelText)
                .color(BODY_TEXT_COLOR)
                .noShadow()
                .centered();
        var modeLabelX = modeLabel.getBounds().x;
        widgets.add(modeLabel);
        var modeIcon = switch (mode) {
            case HEAT -> Widgets.createTexturedWidget(ReiClientPlugin.TEXTURE, modeLabelX - 9, y + 3, 0, 68, 6, 6);
            case COOL -> Widgets.createTexturedWidget(ReiClientPlugin.TEXTURE, modeLabelX - 9, y + 3, 6, 68, 6, 6);
        };
        widgets.add(modeIcon);

        widgets.add(Widgets.createArrow(new Point(centerX - 12, y + 14)));
        widgets.add(Widgets.createLabel(new Point(centerX, y + 38), interaction)
                .color(BODY_TEXT_COLOR).noShadow().centered());

        widgets.add(Widgets.createSlot(new Point(centerX - 34, y + 15)).entries(recipe.getInput()).markInput());

        int x = centerX + 20;

        // In-World Block or Fluid output
        for (var entries : recipe.getConsumed()) {
            widgets.add(Widgets.createSlot(new Point(x, y + 15)).entries(entries));
            x += 18;
        }
        for (var entries : recipe.getOutputEntries()) {
            widgets.add(Widgets.createSlot(new Point(x, y + 15)).entries(entries).markOutput());
            x += 18;
        }

        return widgets;
    }

    private static <T> void addConsumedOverlay(EntryStack<T> entryStack) {
        entryStack.withRenderer(new EntryRenderer<>() {
            @Override
            public void render(EntryStack<T> entry, GuiGraphics graphics, Rectangle bounds, int mouseX,
                    int mouseY, float delta) {
                var baseRenderer = entry.getDefinition().getRenderer();
                baseRenderer.render(entry, graphics, bounds, mouseX, mouseY, delta);
                graphics.blit(RenderPipelines.GUI_TEXTURED, ReiClientPlugin.TEXTURE, bounds.x, bounds.y, 0, 0,
                        0, 52, 16, 16);
            }

            @Override
            public @Nullable Tooltip getTooltip(EntryStack<T> entry, TooltipContext context) {
                var baseRenderer = entry.getDefinition().getRenderer();
                return baseRenderer.getTooltip(entry, context);
            }
        });
    }

    @Override
    public int getDisplayHeight() {
        return 50 + 2 * PADDING;
    }

    @Override
    public int getDisplayWidth(EntropyRecipeDisplay display) {
        return 130 + 2 * PADDING;
    }
}
