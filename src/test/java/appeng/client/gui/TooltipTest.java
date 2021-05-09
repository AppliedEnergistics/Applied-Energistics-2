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

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import appeng.util.LoadTranslations;

@LoadTranslations
class TooltipTest {

    @Test
    void testLineSplitting() {
        Tooltip tooltip = new Tooltip(
                new StringTextComponent("BOLD").mergeStyle(TextFormatting.BOLD)
                        .appendString("More Text\nSecond Line")
                        .appendString("Continued Second Line"),
                new TranslationTextComponent("Third Line"),
                new StringTextComponent("Fourth Line"));

        assertThat(tooltip.getContent())
                .extracting(ITextComponent::getString)
                .containsExactly(
                        "BOLDMore Text",
                        "Second LineContinued Second Line",
                        "Third Line",
                        "Fourth Line");
    }

    @Test
    void testSplitAtNewlineInTranslationText() {
        Tooltip tooltip = new Tooltip(
                new TranslationTextComponent("gui.tooltips.appliedenergistics2.MatterBalls", 256));

        assertThat(tooltip.getContent())
                .extracting(ITextComponent::getString)
                .containsExactly(
                        "Condense Into Matter Balls",
                        "256 per item");
    }

    @Test
    void testNoLineSplitting() {
        Tooltip tooltip = new Tooltip(
                new TranslationTextComponent("a"),
                new TranslationTextComponent("b"));

        assertThat(tooltip.getContent())
                .extracting(ITextComponent::getString)
                .containsExactly(
                        "a", "b");
    }

}