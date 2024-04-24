package appeng.api.implementations.items;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import appeng.api.util.AEColor;

/**
 * Describes the custom colors to show o n a memory card item.
 *
 * @param top1
 * @param top2
 * @param top3
 * @param top4
 * @param bottom1
 * @param bottom2
 * @param bottom3
 * @param bottom4
 */
public record MemoryCardColors(AEColor top1, AEColor top2, AEColor top3, AEColor top4,
        AEColor bottom1, AEColor bottom2, AEColor bottom3, AEColor bottom4) {

    public static final Codec<MemoryCardColors> CODEC = AEColor.CODEC.listOf().xmap(
            aeColors -> new MemoryCardColors(
                    0 < aeColors.size() ? aeColors.get(0) : AEColor.TRANSPARENT,
                    1 < aeColors.size() ? aeColors.get(1) : AEColor.TRANSPARENT,
                    2 < aeColors.size() ? aeColors.get(2) : AEColor.TRANSPARENT,
                    3 < aeColors.size() ? aeColors.get(3) : AEColor.TRANSPARENT,
                    4 < aeColors.size() ? aeColors.get(4) : AEColor.TRANSPARENT,
                    5 < aeColors.size() ? aeColors.get(5) : AEColor.TRANSPARENT,
                    6 < aeColors.size() ? aeColors.get(6) : AEColor.TRANSPARENT,
                    7 < aeColors.size() ? aeColors.get(7) : AEColor.TRANSPARENT),
            colors -> List.of(
                    colors.top1(),
                    colors.top2(),
                    colors.top3(),
                    colors.top4(),
                    colors.bottom1(),
                    colors.bottom2(),
                    colors.bottom3(),
                    colors.bottom4()));

    public static StreamCodec<FriendlyByteBuf, MemoryCardColors> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, MemoryCardColors>() {
        @Override
        public MemoryCardColors decode(FriendlyByteBuf buffer) {
            return new MemoryCardColors(
                    buffer.readEnum(AEColor.class),
                    buffer.readEnum(AEColor.class),
                    buffer.readEnum(AEColor.class),
                    buffer.readEnum(AEColor.class),
                    buffer.readEnum(AEColor.class),
                    buffer.readEnum(AEColor.class),
                    buffer.readEnum(AEColor.class),
                    buffer.readEnum(AEColor.class));
        }

        @Override
        public void encode(FriendlyByteBuf buffer, MemoryCardColors colors) {
            buffer.writeEnum(colors.top1);
            buffer.writeEnum(colors.top2);
            buffer.writeEnum(colors.top3);
            buffer.writeEnum(colors.top4);
            buffer.writeEnum(colors.bottom1);
            buffer.writeEnum(colors.bottom2);
            buffer.writeEnum(colors.bottom3);
            buffer.writeEnum(colors.bottom4);
        }
    };

    public static final MemoryCardColors DEFAULT = new MemoryCardColors(AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT);

    public AEColor get(int x, int y) {
        var index = x + y * 4;
        return switch (index) {
            case 0 -> top1;
            case 1 -> top2;
            case 2 -> top3;
            case 3 -> top4;
            case 4 -> bottom1;
            case 5 -> bottom2;
            case 6 -> bottom3;
            case 7 -> bottom4;
            default -> AEColor.TRANSPARENT;
        };
    }
}
