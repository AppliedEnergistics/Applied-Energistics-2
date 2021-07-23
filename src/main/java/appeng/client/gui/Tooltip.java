/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText.StyledContentConsumer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Style;

/**
 * Models a tooltip shown by a custom widget or button.
 */
public final class Tooltip {

    private final List<net.minecraft.network.chat.Component> content;

    public Tooltip(List<net.minecraft.network.chat.Component> unprocessedLines) {
        this.content = new ArrayList<>(unprocessedLines.size());

        // Split translated lines at line-breaks found in the translated text
        for (net.minecraft.network.chat.Component unprocessedLine : unprocessedLines) {
            splitLine(unprocessedLine, this.content);
        }
    }

    private static void splitLine(net.minecraft.network.chat.Component unprocessedLine, List<net.minecraft.network.chat.Component> lines) {
        LineSplittingVisitor visitor = new LineSplittingVisitor(lines);
        unprocessedLine.visit(visitor, net.minecraft.network.chat.Style.EMPTY);
        visitor.flush();
    }

    public Tooltip(net.minecraft.network.chat.Component... content) {
        this(Arrays.asList(content));
    }

    /**
     * The tooltip content.
     */
    public List<net.minecraft.network.chat.Component> getContent() {
        return content;
    }

    private static class LineSplittingVisitor implements StyledContentConsumer<Object> {
        private final List<net.minecraft.network.chat.Component> lines;

        private MutableComponent currentPart;

        public LineSplittingVisitor(List<net.minecraft.network.chat.Component> lines) {
            this.lines = lines;
        }

        @Override
        public Optional<Object> accept(net.minecraft.network.chat.Style style, String text) {
            // a new-line character in the text should split the line and "flush" the
            // current part into the lines list
            String[] parts = text.split("\n", -1);
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    flush();
                }

                String line = parts[i];
                MutableComponent part = new TextComponent(line).setStyle(style);
                if (currentPart != null) {
                    currentPart = currentPart.append(part);
                } else {
                    currentPart = part;
                }
            }
            return Optional.empty();
        }

        public void flush() {
            if (currentPart != null) {
                // Set default tooltip styles only if no explicit style has been set
                if (currentPart.getStyle() == net.minecraft.network.chat.Style.EMPTY) {
                    // First line should be white, other lines gray
                    if (lines.isEmpty()) {
                        currentPart.setStyle(net.minecraft.network.chat.Style.EMPTY.applyFormat(ChatFormatting.WHITE));
                    } else {
                        currentPart.setStyle(net.minecraft.network.chat.Style.EMPTY.applyFormat(ChatFormatting.GRAY));
                    }
                }

                lines.add(currentPart);
                currentPart = null;
            }
        }
    }
}
