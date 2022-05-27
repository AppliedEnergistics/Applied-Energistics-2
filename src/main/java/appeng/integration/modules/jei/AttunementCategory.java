package appeng.integration.modules.jei;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;

import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.localization.ItemModText;

public class AttunementCategory implements DisplayCategory<AttunementDisplay> {

    public static final CategoryIdentifier<AttunementDisplay> ID = CategoryIdentifier
            .of(AppEng.makeId("attunement"));

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AEParts.ME_P2P_TUNNEL);
    }

    @Override
    public Component getTitle() {
        return ItemModText.P2P_TUNNEL_ATTUNEMENT.text();
    }

    @Override
    public CategoryIdentifier<? extends AttunementDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public List<Widget> setupDisplay(AttunementDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 5)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 4, startPoint.y + 5))
                .entries(display.getInputEntries().get(0)).markInput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5))
                .entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 36;
    }
}
