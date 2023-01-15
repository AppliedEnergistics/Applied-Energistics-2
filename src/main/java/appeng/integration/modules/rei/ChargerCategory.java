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
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.core.definitions.AEBlocks;

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

        // TODO 1.19.3 widgets.add(
        // TODO 1.19.3 Widgets.createSlot(new Point(x + 31, y + 8))
        // TODO 1.19.3 .markInput()
        // TODO 1.19.3 .backgroundEnabled(true)
        // TODO 1.19.3 .entries(EntryIngredients.ofIngredient(display.recipe().getIngredient())));
        // TODO 1.19.3 widgets.add(
        // TODO 1.19.3 Widgets.createSlot(new Point(x + 81, y + 8))
        // TODO 1.19.3 .markOutput()
        // TODO 1.19.3 .backgroundEnabled(true)
        // TODO 1.19.3 .entry(EntryStacks.of(display.recipe().getResultItem())));
// TODO 1.19.3
        // TODO 1.19.3 widgets.add(
        // TODO 1.19.3 Widgets.createSlot(new Point(x + 3, y + 30))
        // TODO 1.19.3 .unmarkInputOrOutput()
        // TODO 1.19.3 .backgroundEnabled(false)
        // TODO 1.19.3 .entry(EntryStacks.of(AEBlocks.CRANK.stack())));

        widgets.add(Widgets.createArrow(new Point(x + 52, y + 8)));

        // TODO 1.19.3 var turns = (ChargerBlockEntity.POWER_MAXIMUM_AMOUNT + CrankBlockEntity.POWER_PER_CRANK_TURN - 1)
        // TODO 1.19.3 / CrankBlockEntity.POWER_PER_CRANK_TURN;
        // TODO 1.19.3 widgets.add(Widgets
        // TODO 1.19.3 .createLabel(new Point(x + 20, y + 35),
        // TODO 1.19.3 ItemModText.CHARGER_REQUIRED_POWER.text(turns, ChargerBlockEntity.POWER_MAXIMUM_AMOUNT))
        // TODO 1.19.3 .color(0x7E7E7E)
        // TODO 1.19.3 .noShadow()
        // TODO 1.19.3 .leftAligned());
// TODO 1.19.3
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
