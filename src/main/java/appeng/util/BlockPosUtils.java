package appeng.util;

import net.minecraft.util.math.BlockPos;


public class BlockPosUtils
{
    public static long getDistance( BlockPos blockPos, BlockPos blockPos2 )
    {
        int x;
        if( (blockPos.getX() > 0 && blockPos2.getX() > 0) || (blockPos.getX() < 0 && blockPos2.getX() < 0))
        {
            x = blockPos.getX() - blockPos2.getX();
        }
        else
        {
            x = blockPos.getX() + blockPos2.getX();
        }

        int y;
        if( (blockPos.getY() > 0 && blockPos2.getY() > 0) || (blockPos.getY() < 0 && blockPos2.getY() < 0) )
        {
            y = blockPos.getY() - blockPos2.getY();
        }
        else
        {
            y = blockPos.getY() + blockPos2.getY();
        }

        int z;
        if( (blockPos.getZ() > 0 && blockPos2.getZ() > 0) || (blockPos.getZ() < 0 && blockPos2.getZ() < 0) )
        {
            z = blockPos.getZ() - blockPos2.getZ();
        }
        else
        {
            z = blockPos.getZ() + blockPos2.getZ();
        }
        return Math.abs( x ) + Math.abs( y ) + Math.abs( z );
    }
}
