package appeng.client.gui.me.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.AEBaseScreen;
import appeng.container.me.crafting.CraftingPlanSummaryEntry;
import appeng.core.localization.GuiText;
import appeng.util.ReadableNumberConverter;

public class CraftConfirmTableRenderer extends AbstractTableRenderer<CraftingPlanSummaryEntry> {

    public CraftConfirmTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y);
    }

    @Override
    protected List<String> getEntryDescription(CraftingPlanSummaryEntry entry) {
        List<String> lines = new ArrayList<>(3);
        if (entry.getStoredAmount() > 0) {
            String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getStoredAmount());
            str = GuiText.FromStorage.getLocal() + ": " + str;
            lines.add(str);
        }

        if (entry.getMissingAmount() > 0) {
            String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getMissingAmount());
            str = GuiText.Missing.text().getString() + ": " + str;
            lines.add(str);
        }

        if (entry.getCraftAmount() > 0) {
            String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getCraftAmount());
            str = GuiText.ToCraft.getLocal() + ": " + str;
            lines.add(str);
        }
        return lines;
    }

    @Override
    protected ItemStack getEntryItem(CraftingPlanSummaryEntry entry) {
        return entry.getItem();
    }

    @Override
    protected List<ITextComponent> getEntryTooltip(CraftingPlanSummaryEntry entry) {
        List<ITextComponent> lines = new ArrayList<>(screen.getTooltipFromItem(entry.getItem()));

        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage.withSuffix(": " + entry.getStoredAmount()));
        }
        if (entry.getMissingAmount() > 0) {
            lines.add(GuiText.Missing.withSuffix(": " + entry.getMissingAmount()));
        }
        if (entry.getCraftAmount() > 0) {
            lines.add(GuiText.ToCraft.withSuffix(": " + entry.getCraftAmount()));
        }

        return lines;

    }

    @Override
    protected int getEntryOverlayColor(CraftingPlanSummaryEntry entry) {
        return entry.getMissingAmount() > 0 ? 0x1AFF0000 : 0;
    }

}
