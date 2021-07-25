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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

import appeng.util.LoadTranslations;

@LoadTranslations
class TooltipTest {

    @Test
    void testLineSplitting() {
        Tooltip tooltip = new Tooltip(
                new TextComponent("BOLD").withStyle(ChatFormatting.BOLD)
                        .append("More Text\nSecond Line")
                        .append("Continued Second Line"),
                new TranslatableComponent("Third Line"),
                new TextComponent("Fourth Line"));

        assertThat(tooltip.getContent())
                .extracting(net.minecraft.network.chat.Component::getString)
                .containsExactly(
                        "BOLDMore Text",
                        "Second LineContinued Second Line",
                        "Third Line",
                        "Fourth Line");
    }

    @Test
    void testSplitAtNewlineInTranslationText() {
        Tooltip tooltip = new Tooltip(
                new TranslatableComponent("gui.tooltips.appliedenergistics2.MatterBalls", 256));

        assertThat(tooltip.getContent())
                .extracting(net.minecraft.network.chat.Component::getString)
                .containsExactly(
                        "Condense Into Matter Balls",
                        "256 per item");
    }

    @Test
    void testNoLineSplitting() {
        Tooltip tooltip = new Tooltip(
                new TranslatableComponent("a"),
                new TranslatableComponent("b"));

        assertThat(tooltip.getContent())
                .extracting(net.minecraft.network.chat.Component::getString)
                .containsExactly(
                        "a", "b");
    }

}
