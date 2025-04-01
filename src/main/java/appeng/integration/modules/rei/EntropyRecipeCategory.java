package appeng.integration.modules.rei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;
import appeng.items.tools.powered.EntropyManipulatorItem;

public class EntropyRecipeCategory implements DisplayCategory<EntropyRecipeDisplay> {
    private static final int PADDING = 5;
    private static final int BODY_TEXT_COLOR = 0x7E7E7E;

    static final CategoryIdentifier<EntropyRecipeDisplay> ID = CategoryIdentifier
            .of(AppEng.makeId("ae2.entropy_manipulator"));

    @Override
    public CategoryIdentifier<? extends EntropyRecipeDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Renderer getIcon() {
        return (graphics, bounds, mouseX, mouseY, delta) -> graphics.blit(
                RenderType::guiTextured,
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
            case HEAT -> Widgets.createTexturedWidget(ReiPlugin.TEXTURE, modeLabelX - 9, y + 3, 0, 68, 6, 6);
            case COOL -> Widgets.createTexturedWidget(ReiPlugin.TEXTURE, modeLabelX - 9, y + 3, 6, 68, 6, 6);
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

    @Override
    public int getDisplayHeight() {
        return 50 + 2 * PADDING;
    }

    @Override
    public int getDisplayWidth(EntropyRecipeDisplay display) {
        return 130 + 2 * PADDING;
    }
}
