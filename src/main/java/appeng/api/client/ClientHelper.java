/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.client;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.cells.base.IBasicCellInfo;
import appeng.api.storage.data.IAEStack;
import appeng.core.localization.GuiText;

public class ClientHelper {
    private ClientHelper() {
    }

    /**
     * Add cell information to the provided list. Used for tooltip content.
     *
     * @param lines List of lines to add to.
     */
    public static <T extends IAEStack> void addCellInformation(@Nullable IBasicCellInfo info, boolean isPreformatted,
            boolean isFuzzy, IncludeExclude includeExcludeMode, List<Component> lines) {
        if (info != null) {
            lines.add(new TextComponent(info.getUsedBytes() + " ").append(GuiText.Of.text())
                    .append(" " + info.getTotalBytes() + " ").append(GuiText.BytesUsed.text()));

            lines.add(new TextComponent(info.getStoredItemTypes() + " ").append(GuiText.Of.text())
                    .append(" " + info.getTotalItemTypes() + " ").append(GuiText.Types.text()));
        }

        if (isPreformatted) {
            var list = (includeExcludeMode == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded)
                    .getLocal();

            if (isFuzzy) {
                lines.add(GuiText.Partitioned.withSuffix(" - " + list + " ").append(GuiText.Fuzzy.text()));
            } else {
                lines.add(GuiText.Partitioned.withSuffix(" - " + list + " ").append(GuiText.Precise.text()));
            }
        }
    }
}
