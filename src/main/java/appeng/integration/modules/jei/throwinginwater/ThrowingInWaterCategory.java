package appeng.integration.modules.jei.throwinginwater;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ItemModText;

public class ThrowingInWaterCategory implements DisplayCategory<ThrowingInWaterDisplay> {
    public static final CategoryIdentifier<ThrowingInWaterDisplay> ID = CategoryIdentifier
            .of(AppEng.makeId("throwing_in_water"));

    private final Renderer icon;

    public ThrowingInWaterCategory() {
        icon = EntryStacks.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED);
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return ItemModText.THROWING_IN_WATER_CATEGORY.text();
    }

    @Override
    public CategoryIdentifier<ThrowingInWaterDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(ThrowingInWaterDisplay display, Rectangle bounds) {

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        // First column contains ingredients
        final int col1 = bounds.x + 10;
        var y = bounds.y + 10;
        for (EntryIngredient input : display.getInputEntries()) {
            var slot = Widgets.createSlot(new Point(col1, y))
                    .entries(input)
                    .markInput();
            y += slot.getBounds().height;
            widgets.add(slot);
        }

        // To center everything but the ingredients vertically
        int yOffset = bounds.y + (display.getInputEntries().size() - 1) / 2 * 18 + 10;

        // Second column is arrow pointing into water
        final int col2 = col1 + 25;
        var arrow1 = Widgets.createArrow(new Point(col2, yOffset));
        widgets.add(arrow1);

        // Third column is water block
        final int col3 = col2 + arrow1.getBounds().getWidth() + 6;
        widgets.add(Widgets.wrapRenderer(
                new Rectangle(
                        col3,
                        yOffset,
                        14,
                        14),
                new WaterBlockRenderer()));

        // Fourth column is arrow pointing to results
        final int col4 = col3 + 16 + 5;
        var arrow2 = Widgets.createArrow(new Point(col4, yOffset));
        widgets.add(arrow2);

        // Fifth column is the result
        final int col5 = arrow2.getBounds().getMaxX() + 10;
        var slot = Widgets.createSlot(new Point(col5, yOffset))
                .entries(display.getOutputEntries().get(0))
                .markOutput();
        widgets.add(slot);

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }
}
