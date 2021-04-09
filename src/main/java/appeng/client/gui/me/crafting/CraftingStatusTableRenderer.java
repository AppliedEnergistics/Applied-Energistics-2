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
    protected List<ITextComponent> getEntryDescription(CraftingStatusEntry entry) {
        List<ITextComponent> lines = new ArrayList<>(3);
        if (entry.getStoredAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getStoredAmount());
            lines.add(GuiText.FromStorage.text(amount));
        }

        if (entry.getActiveAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getActiveAmount());
            lines.add(GuiText.Crafting.text(amount));
        }

        if (entry.getPendingAmount() > 0) {
            String amount = ReadableNumberConverter.INSTANCE.toWideReadableForm(entry.getPendingAmount());
            lines.add(GuiText.Scheduled.text(amount));
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

        // The tooltip compares the unabbreviated amounts
        if (entry.getStoredAmount() > 0) {
            lines.add(GuiText.FromStorage.text(entry.getStoredAmount()));
        }
        if (entry.getActiveAmount() > 0) {
            lines.add(GuiText.Crafting.text(entry.getActiveAmount()));
        }
        if (entry.getPendingAmount() > 0) {
            lines.add(GuiText.Scheduled.text(entry.getPendingAmount()));
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
