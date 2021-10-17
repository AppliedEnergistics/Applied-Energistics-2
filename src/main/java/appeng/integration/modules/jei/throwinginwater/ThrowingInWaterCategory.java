package appeng.integration.modules.jei.throwinginwater;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.entity.GrowingCrystalEntity;
import appeng.items.misc.CrystalSeedItem;

public class ThrowingInWaterCategory implements DisplayCategory<ThrowingInWaterDisplay> {
    public static final CategoryIdentifier<ThrowingInWaterDisplay> ID = CategoryIdentifier
            .of(AppEng.makeId("throwing_in_water"));

    private final Renderer icon;

    public ThrowingInWaterCategory() {
        var stage1 = AEItems.CERTUS_CRYSTAL_SEED.stack();
        CrystalSeedItem.setGrowthTicks(stage1, 0);
        var stage2 = AEItems.CERTUS_CRYSTAL_SEED.stack();
        CrystalSeedItem.setGrowthTicks(stage2, (int) (CrystalSeedItem.GROWTH_TICKS_REQUIRED * 0.4f));
        var stage3 = AEItems.CERTUS_CRYSTAL_SEED.stack();
        CrystalSeedItem.setGrowthTicks(stage3, (int) (CrystalSeedItem.GROWTH_TICKS_REQUIRED * 0.7f));
        var result = AEItems.CERTUS_QUARTZ_CRYSTAL.stack();

        icon = new GrowingSeedIconRenderer(List.of(
                stage1,
                stage2,
                stage3,
                result));
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("rei.appliedenergistics2.throwing_in_water_category");
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

        // Add descriptive text explaining the duration centered on the water block
        if (display.isSupportsAccelerators()) {
            var durationY = bounds.y + 10 + display.getInputEntries().size() * 18 + 2;

            List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(
                    new TranslatableComponent("rei.appliedenergistics2.with_crystal_growth_accelerators"));
            for (var i = 1; i <= 5; i++) {
                var duration = GrowingCrystalEntity.getGrowthDuration(i).toMillis();
                tooltipLines.add(new TextComponent(i + ": " + DurationFormatUtils.formatDurationWords(
                        duration, true, true)));
            }

            var defaultDuration = GrowingCrystalEntity.getGrowthDuration(0).toMillis();
            widgets.add(Widgets.createLabel(
                    new Point(col3 + 7, durationY),
                    new TextComponent(DurationFormatUtils.formatDurationWords(
                            defaultDuration, true, true)))
                    .noShadow().color(0xFF404040, 0xFFBBBBBB)
                    .centered().tooltipLines(tooltipLines.stream().map(Component::getString).toArray(String[]::new)));
        }

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 72;
    }
}
