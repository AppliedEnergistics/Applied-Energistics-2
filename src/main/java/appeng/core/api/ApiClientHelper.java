
package appeng.core.api;

import java.util.List;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IClientHelper;
import appeng.core.localization.GuiText;

public class ApiClientHelper implements IClientHelper {
    @Override
    public <T extends IAEStack<T>> void addCellInformation(ICellInventoryHandler<T> handler,
            List<ITextComponent> lines) {
        if (handler == null) {
            return;
        }

        final ICellInventory<?> cellInventory = handler.getCellInv();

        if (cellInventory != null) {
            lines.add(new StringTextComponent(cellInventory.getUsedBytes() + " ")
                    .appendSibling(GuiText.Of.textComponent()).appendText(" " + cellInventory.getTotalBytes() + " ")
                    .appendSibling(GuiText.BytesUsed.textComponent()));

            lines.add(new StringTextComponent(cellInventory.getStoredItemTypes() + " ")
                    .appendSibling(GuiText.Of.textComponent()).appendText(" " + cellInventory.getTotalItemTypes() + " ")
                    .appendSibling(GuiText.Types.textComponent()));
        }

        if (handler.isPreformatted()) {
            final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
                    : GuiText.Excluded).getLocal();

            if (handler.isFuzzy()) {
                lines.add(GuiText.Partitioned.textComponent().appendText(" - " + list + " ")
                        .appendSibling(GuiText.Fuzzy.textComponent()));
            } else {
                lines.add(GuiText.Partitioned.textComponent().appendText(" - " + list + " ")
                        .appendSibling(GuiText.Precise.textComponent()));
            }
        }

    }

}
