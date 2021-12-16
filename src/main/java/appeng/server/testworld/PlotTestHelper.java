package appeng.server.testworld;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.phys.Vec3;

public class PlotTestHelper extends GameTestHelper {
    private final BlockPos plotTranslation;

    public PlotTestHelper(BlockPos plotTranslation, GameTestInfo gameTestInfo) {
        super(gameTestInfo);
        this.plotTranslation = plotTranslation;
    }

    @Override
    public BlockPos absolutePos(BlockPos pos) {
        return super.absolutePos(pos.offset(plotTranslation).offset(0, 1, 0));
    }

    @Override
    public BlockPos relativePos(BlockPos pos) {
        return super.relativePos(pos)
                .offset(
                        -plotTranslation.getX(),
                        -plotTranslation.getY(),
                        -plotTranslation.getZ())
                .offset(0, -1, 0);
    }

    @Override
    public Vec3 absoluteVec(Vec3 relativeVec3) {
        return super.absoluteVec(relativeVec3)
                .add(
                        plotTranslation.getX(),
                        plotTranslation.getY(),
                        plotTranslation.getZ());
    }
}
