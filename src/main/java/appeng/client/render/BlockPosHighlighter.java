package appeng.client.render;

import net.minecraft.util.math.BlockPos;

// taken from McJty's McJtyLib
public class BlockPosHighlighter
{
    private static BlockPos hilightedBlock;
    private static long expireHilight;



    private static int dimension;

    public static void hilightBlock( BlockPos c, long expireHilight, int dimension ) {
        hilightedBlock = c;
        BlockPosHighlighter.expireHilight = expireHilight;
        BlockPosHighlighter.dimension = dimension;
    }

    public static BlockPos getHilightedBlock() {
        return hilightedBlock;
    }

    public static long getExpireHilight() {
        return expireHilight;
    }

    public static int getDimension()
    {
        return dimension;
    }

}
