package appeng.client.gui.me.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.api.util.AEColor;
import appeng.client.gui.AEBaseScreen;
import appeng.container.me.crafting.CraftingStatusEntry;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.util.ReadableNumberConverter;

public class CraftingStatusTableRenderer extends AbstractTableRenderer<CraftingStatusEntry> {

    private static final int BACKGROUND_ALPHA = 0x5A000000;

    public CraftingStatusTableRenderer(AEBaseScreen<?> screen, int x, int y) {
        super(screen, x, y);
    }

    @Override
    protected List<String> getEntryDescription(CraftingStatusEntry entry) {
        List<String> lines = new ArrayList<>(3);
        if (entry.getStoredAmount() > 0) {
            String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getStoredAmount());
            str = GuiText.FromStorage.getLocal() + ": " + str;
            lines.add(str);
        }

        if (entry.getActiveAmount() > 0) {
            String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getActiveAmount());
            str = GuiText.Crafting.text().getString() + ": " + str;
            lines.add(str);
        }

        if (entry.getPendingAmount() > 0) {
            String str = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getPendingAmount());
            str = GuiText.Scheduled.getLocal() + ": " + str;
            lines.add(str);
        }
        return lines;
    }

    @Override
    protected ItemStack getEntryItem(CraftingStatusEntry entry) {
        return entry.getItem();
    }

    @Override
    protected List<ITextComponent> getEntryTooltip(CraftingStatusEntry entry) {
        List<ITextComponent> lines = new ArrayList<>(screen.getTooltipFromItem(entry.getItem()));

        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage.withSuffix(": " + entry.getStoredAmount()));
        }
        if (entry.getActiveAmount() > 0) {
            lines.add(GuiText.Crafting.withSuffix(": " + entry.getActiveAmount()));
        }
        if (entry.getPendingAmount() > 0) {
            lines.add(GuiText.Scheduled.withSuffix(": " + entry.getPendingAmount()));
        }

        return lines;

    }

    @Override
    protected int getEntryBackgroundColor(CraftingStatusEntry entry) {
        if (AEConfig.instance().isUseColoredCraftingStatus()) {
            if (entry.getActiveAmount() > 0) {
                return AEColor.GREEN.blackVariant | BACKGROUND_ALPHA;
            } else if (entry.getPendingAmount() > 0) {
                return AEColor.YELLOW.blackVariant | BACKGROUND_ALPHA;
            }
        }
        return 0;
    }

}
