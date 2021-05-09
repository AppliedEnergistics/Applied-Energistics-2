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