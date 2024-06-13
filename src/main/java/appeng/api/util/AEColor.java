/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

package appeng.api.util;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

/**
 * List of all colors supported by AE, their names, and various colors for display.
 *
 * Should be the same order as Dyes, excluding Transparent.
 */

// TODO (RID): Sorted the colours according to the colour wheel
public enum AEColor implements StringRepresentable {
    // TODO (Rid): Sorted the colours based on the colour wheel.
    WHITE("White", "gui.ae2.White", "white", DyeColor.WHITE, 0xb4b4b4, 0xe0e0e0, 0xf9f9f9, 0x000000),
    LIGHT_GRAY("Light Gray", "gui.ae2.LightGray", "light_gray", DyeColor.LIGHT_GRAY, 0x7e7e7e, 0xa09fa0, 0xc4c4c4,
            0x000000),
    GRAY("Gray", "gui.ae2.Gray", "gray", DyeColor.GRAY, 0x4f4f4f, 0x6c6b6c, 0x949294, 0x000000),
    BLACK("Black", "gui.ae2.Black", "black", DyeColor.BLACK, 0x131313, 0x272727, 0x3b3b3b, 0xFFFFFF),
    LIME("Lime", "gui.ae2.Lime", "lime", DyeColor.LIME, 0x4ec04e, 0x70e259, 0xb3f86d, 0x000000),
    YELLOW("Yellow", "gui.ae2.Yellow", "yellow", DyeColor.YELLOW, 0xffcf40, 0xffe359, 0xf4ff80, 0x000000),
    ORANGE("Orange", "gui.ae2.Orange", "orange", DyeColor.ORANGE, 0xd9782f, 0xeca23c, 0xf2ba49, 0x000000),
    BROWN("Brown", "gui.ae2.Brown", "brown", DyeColor.BROWN, 0x6e4a12, 0x7e5c16, 0x8e6e1a, 0x000000),
    RED("Red", "gui.ae2.Red", "red", DyeColor.RED, 0xaa212b, 0xd73e42, 0xf07665, 0x000000),
    PINK("Pink", "gui.ae2.Pink", "pink", DyeColor.PINK, 0xd86eaa, 0xff99bb, 0xfbcad5, 0x000000),
    MAGENTA("Magenta", "gui.ae2.Magenta", "magenta", DyeColor.MAGENTA, 0xc15189, 0xd5719c, 0xe69ebf, 0x000000),
    PURPLE("Purple", "gui.ae2.Purple", "purple", DyeColor.PURPLE, 0x6e5cb8, 0x915dcd, 0xb06fdd, 0x000000),
    BLUE("Blue", "gui.ae2.Blue", "blue", DyeColor.BLUE, 0x337ff0, 0x3894ff, 0x40c1ff, 0x000000),
    LIGHT_BLUE("Light Blue", "gui.ae2.LightBlue", "light_blue", DyeColor.LIGHT_BLUE, 0x69b9ff, 0x70d2ff, 0x80f7ff,
            0x000000),
    CYAN("Cyan", "gui.ae2.Cyan", "cyan", DyeColor.CYAN, 0x22b0ae, 0x2fccb7, 0x65e8c9, 0x000000),
    GREEN("Green", "gui.ae2.Green", "green", DyeColor.GREEN, 0x079b6b, 0x17b86d, 0x32d850, 0x000000),
    TRANSPARENT("Fluix", "gui.ae2.Fluix", "fluix", null, 0x5a479e, 0x915dcd, 0xe2a3e3, 0x000000);

    public static final Codec<AEColor> CODEC = StringRepresentable.fromEnum(AEColor::values);

    public static final StreamCodec<FriendlyByteBuf, AEColor> STREAM_CODEC = NeoForgeStreamCodecs
            .enumCodec(AEColor.class);

    // TODO (RID): Sorted the colours according to the colour wheel
    public static final List<AEColor> VALID_COLORS = Arrays.asList(WHITE, LIGHT_GRAY, GRAY, BLACK, LIME, YELLOW,
            ORANGE, BROWN, RED, PINK, MAGENTA, PURPLE, BLUE, LIGHT_BLUE, CYAN, GREEN);

    /**
     * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get the {@link #blackVariant dark
     * variant} of the apprioriate AE color.
     */
    public static final int TINTINDEX_DARK = 1;

    /**
     * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get the {@link #mediumVariant medium
     * variant} of the apprioriate AE color.
     */
    public static final int TINTINDEX_MEDIUM = 2;

    /**
     * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get the {@link #whiteVariant bright
     * variant} of the apprioriate AE color.
     */
    public static final int TINTINDEX_BRIGHT = 3;

    /**
     * The {@link BakedQuad#getTintIndex() tint index} that can normally be used to get a color between the
     * {@link #mediumVariant medium} and {@link #whiteVariant bright variant} of the apprioriate AE color.
     */
    public static final int TINTINDEX_MEDIUM_BRIGHT = 4;

    /**
     * Unlocalized name for color.
     */
    public final String translationKey;

    /**
     * Darkest Variant of the color, nearly black; as a RGB HEX Integer
     */
    public final int blackVariant;

    /**
     * The Variant of the color that is used to represent the color normally; as a RGB HEX Integer
     */
    public final int mediumVariant;

    /**
     * Lightest Variant of the color, nearly white; as a RGB HEX Integer
     */
    public final int whiteVariant;

    /**
     * Vanilla Dye Equivilient
     */
    public final DyeColor dye;

    /**
     * A convenient ID prefix for use with registering color variants of items and blocks.
     */
    public final String registryPrefix;

    /**
     * English name of this color.
     */
    public final String englishName;

    /**
     * Text color that has good contrast with the medium version of this color.
     */
    public final int contrastTextColor;

    AEColor(String englishName, String translationKey, String registryPrefix, DyeColor dye, int blackHex,
            int medHex, int whiteHex, int contrastTextColor) {
        this.englishName = englishName;
        this.translationKey = translationKey;
        this.registryPrefix = registryPrefix;
        this.blackVariant = blackHex;
        this.mediumVariant = medHex;
        this.whiteVariant = whiteHex;
        this.contrastTextColor = contrastTextColor;
        this.dye = dye;
    }

    public static AEColor fromDye(DyeColor vanillaDye) {
        for (var value : values()) {
            if (value.dye == vanillaDye) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown Vanilla dye: " + vanillaDye);
    }

    /**
     * Will return a variant of this color based on the given tint index.
     *
     * @param tintIndex A tint index as it can be used for {@link BakedQuad#getTintIndex()}.
     * @return The appropriate color variant, or -1.
     */
    public int getVariantByTintIndex(int tintIndex) {
        switch (tintIndex) {
            // Please note that tintindex 0 is hardcoded for the block breaking particles.
            // Returning anything other than
            // -1 for tintindex=0 here
            // will cause issues with those particles
            case 0:
                return -1;
            case TINTINDEX_DARK:
                return this.blackVariant;
            case TINTINDEX_MEDIUM:
                return this.mediumVariant;
            case TINTINDEX_BRIGHT:
                return this.whiteVariant;
            case TINTINDEX_MEDIUM_BRIGHT:
                final int light = this.whiteVariant;
                final int dark = this.mediumVariant;
                return ((light >> 16 & 0xff) + (dark >> 16 & 0xff)) / 2 << 16
                        | ((light >> 8 & 0xff) + (dark >> 8 & 0xff)) / 2 << 8
                        | ((light & 0xff) + (dark & 0xff)) / 2;
            default:
                return -1;
        }
    }

    public String getEnglishName() {
        return englishName;
    }

    @Override
    public String toString() {
        return this.translationKey;
    }

    @Override
    public String getSerializedName() {
        return registryPrefix;
    }
}
