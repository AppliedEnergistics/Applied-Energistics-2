package appeng.client.render;

import net.minecraft.util.math.BlockPos;

// taken from McJty's McJtyLib
public class BlockPosHighlighter
{
    private static BlockPos hilightedBlock;
    private static long expireHilight;

    public static void hilightBlock( BlockPos c, long expireHilight ) {
        hilightedBlock = c;
        BlockPosHighlighter.expireHilight = expireHilight;
    }

    public static BlockPos getHilightedBlock() {
        return hilightedBlock;
    }

    public static long getExpireHilight() {
        return expireHilight;
    }


}
