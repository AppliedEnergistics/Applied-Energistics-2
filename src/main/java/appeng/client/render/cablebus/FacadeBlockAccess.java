package appeng.client.render.cablebus;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

/**
 * Created by covers1624 on 22/06/18.
 */
public class FacadeBlockAccess implements IBlockAccess {

    private final IBlockAccess world;
    private final BlockPos pos;
    private final EnumFacing side;
    private final IBlockState state;

    public FacadeBlockAccess(IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state) {
        this.world = world;
        this.pos = pos;
        this.side = side;
        this.state = state;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return world.getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return world.getCombinedLight(pos, lightValue);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (this.pos == pos) {
            return state;
        }
        return world.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        IBlockState state = getBlockState(pos);
        return state.getBlock().isAir(state, world, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return world.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return world.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return world.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (pos.getX() < -30000000 || pos.getZ() < -30000000 || pos.getX() >= 30000000 || pos.getZ() >= 30000000) {
            return _default;
        } else {
            return getBlockState(pos).isSideSolid(this, pos, side);
        }
    }
}
