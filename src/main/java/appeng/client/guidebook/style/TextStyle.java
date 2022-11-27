package appeng.client.guidebook.style;

import appeng.client.guidebook.render.ColorRef;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record TextStyle(
        @Nullable Float fontScale,
        @Nullable Boolean bold,
        @Nullable Boolean italic,
        @Nullable Boolean underlined,
        @Nullable Boolean strikethrough,
        @Nullable Boolean obfuscated,
        @Nullable ResourceLocation font,
        @Nullable ColorRef color,
        @Nullable WhiteSpaceMode whiteSpace,
        @Nullable TextAlignment alignment
) {

    public static final TextStyle EMPTY = new TextStyle(null, null, null, null, null, null, null, null, null, null);

    public ResolvedTextStyle mergeWith(ResolvedTextStyle base) {
        var fontScale = this.fontScale != null ? this.fontScale : base.fontScale();
        var bold = this.bold != null ? this.bold : base.bold();
        var italic = this.italic != null ? this.italic : base.italic();
        var underlined = this.underlined != null ? this.underlined : base.underlined();
        var strikethrough = this.strikethrough != null ? this.strikethrough : base.strikethrough();
        var obfuscated = this.obfuscated != null ? this.obfuscated : base.obfuscated();
        var font = this.font != null ? this.font : base.font();
        var color = this.color != null ? this.color : base.color();
        var whiteSpace = this.whiteSpace != null ? this.whiteSpace : base.whiteSpace();
        var alignment = this.alignment != null ? this.alignment : base.alignment();
        return new ResolvedTextStyle(
                fontScale,
                bold,
                italic,
                underlined,
                strikethrough,
                obfuscated,
                font,
                color,
                whiteSpace,
                alignment);
    }

    public Builder toBuilder() {
        var builder = new Builder();
        builder.fontScale = fontScale;
        builder.bold = bold;
        builder.italic = italic;
        builder.underlined = underlined;
        builder.strikethrough = strikethrough;
        builder.obfuscated = obfuscated;
        builder.font = font;
        builder.color = color;
        builder.whiteSpace = whiteSpace;
        builder.alignment = alignment;
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Float fontScale;
        private Boolean bold;
        private Boolean italic;
        private Boolean underlined;
        private Boolean strikethrough;
        private Boolean obfuscated;
        private ResourceLocation font;
        private ColorRef color;
        private WhiteSpaceMode whiteSpace;
        private TextAlignment alignment;

        public Builder fontScale(Float fontScale) {
            this.fontScale = fontScale;
            return this;
        }

        public Builder bold(Boolean bold) {
            this.bold = bold;
            return this;
        }

        public Builder italic(Boolean italic) {
            this.italic = italic;
            return this;
        }

        public Builder underlined(Boolean underlined) {
            this.underlined = underlined;
            return this;
        }

        public Builder strikethrough(Boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        public Builder obfuscated(Boolean obfuscated) {
            this.obfuscated = obfuscated;
            return this;
        }

        public Builder font(ResourceLocation font) {
            this.font = font;
            return this;
        }

        public Builder color(ColorRef color) {
            this.color = color;
            return this;
        }

        public Builder whiteSpace(WhiteSpaceMode whiteSpace) {
            this.whiteSpace = whiteSpace;
            return this;
        }

        public Builder alignment(TextAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public TextStyle build() {
            return new TextStyle(fontScale,
                    bold,
                    italic,
                    underlined,
                    strikethrough,
                    obfuscated,
                    font,
                    color,
                    whiteSpace,
                    alignment);
        }
    }
}
