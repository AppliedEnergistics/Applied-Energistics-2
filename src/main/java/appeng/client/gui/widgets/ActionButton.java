/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui.widgets;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;

import appeng.api.config.ActionItems;
import appeng.client.gui.Icon;
import appeng.core.localization.ButtonToolTips;

public class ActionButton extends IconButton {
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
    private final Icon icon;

    public ActionButton(ActionItems action, Runnable onPress) {
        this(action, a -> onPress.run());
    }

    public ActionButton(ActionItems action, Consumer<ActionItems> onPress) {
        super(btn -> onPress.accept(action));

        ButtonToolTips displayName;
        ButtonToolTips displayValue;
        switch (action) {
            case WRENCH -> {
                icon = Icon.WRENCH;
                displayName = ButtonToolTips.PartitionStorage;
                displayValue = ButtonToolTips.PartitionStorageHint;
            }
            case CLOSE -> {
                icon = Icon.CLEAR;
                displayName = ButtonToolTips.Clear;
                displayValue = ButtonToolTips.ClearSettings;
            }
            case STASH -> {
                icon = Icon.ARROW_UP;
                displayName = ButtonToolTips.Stash;
                displayValue = ButtonToolTips.StashDesc;
            }
            case STASH_TO_PLAYER_INV -> {
                icon = Icon.ARROW_DOWN;
                displayName = ButtonToolTips.StashToPlayer;
                displayValue = ButtonToolTips.StashToPlayerDesc;
            }
            case ENCODE -> {
                icon = Icon.WHITE_ARROW_DOWN;
                displayName = ButtonToolTips.Encode;
                displayValue = ButtonToolTips.EncodeDescription;
            }
            case CYCLE_PROCESSING_OUTPUT -> {
                icon = Icon.SCHEDULING_DEFAULT;
                displayName = ButtonToolTips.CycleProcessingOutput;
                displayValue = ButtonToolTips.CycleProcessingOutputTooltip;
            }
            case SEARCH_SETTINGS -> {
                icon = Icon.SEARCH_DEFAULT;
                displayName = ButtonToolTips.SearchSettings;
                displayValue = null;
            }
            default -> throw new IllegalArgumentException("Unknown ActionItem: " + action);
        }

        setMessage(buildMessage(displayName, displayValue));
    }

    @Override
    protected Icon getIcon() {
        return icon;
    }

    private Component buildMessage(ButtonToolTips displayName, @Nullable ButtonToolTips displayValue) {
        String name = displayName.text().getString();
        if (displayValue == null) {
            return Component.literal(name);
        }
        String value = displayValue.text().getString();

        value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
        final StringBuilder sb = new StringBuilder(value);

        int i = sb.lastIndexOf("\n");
        if (i <= 0) {
            i = 0;
        }
        while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
            sb.replace(i, i + 1, "\n");
        }

        return Component.literal(name + '\n' + sb);
    }

}
