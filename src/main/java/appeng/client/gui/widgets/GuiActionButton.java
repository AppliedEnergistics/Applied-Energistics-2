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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.api.config.*;
import appeng.core.localization.ButtonToolTips;

public class GuiActionButton extends GuiIconButton implements ITooltip {
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
    private final int iconIndex;

    public GuiActionButton(final int x, final int y, final ActionItems action, Consumer<ActionItems> onPress) {
        super(x, y, btn -> onPress.accept(action));

        ButtonToolTips displayName;
        ButtonToolTips displayValue;
        switch (action) {
            case WRENCH:
                iconIndex = 66;
                displayName = ButtonToolTips.PartitionStorage;
                displayValue = ButtonToolTips.PartitionStorageHint;
                break;
            case CLOSE:
                iconIndex = 6;
                displayName = ButtonToolTips.Clear;
                displayValue = ButtonToolTips.ClearSettings;
                break;
            case STASH:
                iconIndex = 6;
                displayName = ButtonToolTips.Stash;
                displayValue = ButtonToolTips.StashDesc;
                break;
            case ENCODE:
                iconIndex = 8;
                displayName = ButtonToolTips.Encode;
                displayValue = ButtonToolTips.EncodeDescription;
                break;
            case ENABLE_SUBSTITUTION:
                iconIndex = 4 + 3 * 16;
                displayName = ButtonToolTips.Substitutions;
                displayValue = ButtonToolTips.SubstitutionsDescEnabled;
                break;
            case DISABLE_SUBSTITUTION:
                iconIndex = 7 + 3 * 16;
                displayName = ButtonToolTips.Substitutions;
                displayValue = ButtonToolTips.SubstitutionsDescDisabled;
                break;
            default:
                throw new IllegalArgumentException("Unknown ActionItem: " + action);
        }

        setMessage(buildMessage(displayName, displayValue));
    }

    @Override
    protected int getIconIndex() {
        return iconIndex;
    }

    private String buildMessage(ButtonToolTips displayName, ButtonToolTips displayValue) {
        String name = displayName.getTranslationKey().getString();
        String value = displayValue.getTranslationKey().getString();

        value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
        final StringBuilder sb = new StringBuilder(value);

        int i = sb.lastIndexOf("\n");
        if (i <= 0) {
            i = 0;
        }
        while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
            sb.replace(i, i + 1, "\n");
        }

        return name + '\n' + sb;
    }

}
