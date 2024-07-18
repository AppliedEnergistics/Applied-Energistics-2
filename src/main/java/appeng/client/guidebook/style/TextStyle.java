package appeng.client.guidebook.style;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.color.ColorValue;

public record TextStyle(
        @Nullable Float fontScale,
        @Nullable Boolean bold,
        @Nullable Boolean italic,
        @Nullable Boolean underlined,
        @Nullable Boolean strikethrough,
        @Nullable Boolean obfuscated,
        @Nullable ResourceLocation font,
        @Nullable ColorValue color,
        @Nullable WhiteSpaceMode whiteSpace,
        @Nullable TextAlignment alignment) {

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
        private ColorValue color;
        private WhiteSpaceMode whiteSpace;
        private TextAlignment alignment;

        public Builder apply(TextStyle style) {
            if (style.fontScale() != null) {
                fontScale = style.fontScale();
            }
            if (style.bold() != null) {
                bold = style.bold();
            }
            if (style.italic() != null) {
                italic = style.italic();
            }
            if (style.underlined() != null) {
                underlined = style.underlined();
            }
            if (style.strikethrough() != null) {
                strikethrough = style.strikethrough();
            }
            if (style.obfuscated() != null) {
                obfuscated = style.obfuscated();
            }
            if (style.font() != null) {
                font = style.font();
            }
            if (style.color() != null) {
                color = style.color();
            }
            if (style.whiteSpace() != null) {
                whiteSpace = style.whiteSpace();
            }
            if (style.alignment() != null) {
                alignment = style.alignment();
            }
            return this;
        }

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

        public Builder color(ColorValue color) {
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
