package appeng.integration.modules.rei;

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
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.blockentity.misc.CrankBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.ItemModText;

public class ChargerCategory implements DisplayCategory<ChargerDisplay> {
    private final Renderer icon;

    public ChargerCategory() {
        this.icon = EntryStacks.of(AEBlocks.CHARGER.stack());
    }

    @Override
    public CategoryIdentifier<? extends ChargerDisplay> getCategoryIdentifier() {
        return ChargerDisplay.ID;
    }

    @Override
    public Component getTitle() {
        return AEBlocks.CHARGER.stack().getHoverName();
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    @Override
    public List<Widget> setupDisplay(ChargerDisplay display, Rectangle bounds) {
        var widgets = new ArrayList<Widget>();

        var x = bounds.x;
        var y = bounds.y;
        widgets.add(Widgets.createRecipeBase(bounds));

        widgets.add(
                Widgets.createSlot(new Point(x + 31, y + 8))
                        .markInput()
                        .backgroundEnabled(true)
                        .entries(EntryIngredients.ofIngredient(display.recipe().getIngredient())));
        widgets.add(
                Widgets.createSlot(new Point(x + 81, y + 8))
                        .markOutput()
                        .backgroundEnabled(true)
                        .entry(EntryStacks.of(display.recipe().getResultItem())));

        widgets.add(
                Widgets.createSlot(new Point(x + 3, y + 30))
                        .unmarkInputOrOutput()
                        .backgroundEnabled(false)
                        .entry(EntryStacks.of(AEBlocks.CRANK.stack())));

        widgets.add(Widgets.createArrow(new Point(x + 52, y + 8)));

        var turns = (ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + CrankBlockEntity.POWER_PER_CRANK_TURN - 1)
                / CrankBlockEntity.POWER_PER_CRANK_TURN;
        widgets.add(Widgets
                .createLabel(new Point(x + 20, y + 35),
                        ItemModText.CHARGER_REQUIRED_POWER.text(turns, ChargerBlockEntity.POWER_MAXIMUM_AMOUNT))
                .color(0x7E7E7E)
                .noShadow()
                .leftAligned());

        return widgets;
    }

    @Override
    public int getDisplayWidth(ChargerDisplay display) {
        return 130;
    }

    @Override
    public int getDisplayHeight() {
        return 50;
    }
}
