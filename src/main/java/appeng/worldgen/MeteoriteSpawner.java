package appeng.worldgen;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.core.AEConfig;
import appeng.worldgen.meteorite.IMeteoriteWorld;
import appeng.worldgen.meteorite.StandardWorld;

/**
 * Makes decisions about spawning meteorites in the world.
 */
public class MeteoriteSpawner {

    private final Set<Block> validSpawn = new HashSet<>();
    private final Set<Block> invalidSpawn = new HashSet<>();

    public MeteoriteSpawner() {
        final IBlocks blocks = AEApi.instance().definitions().blocks();

        this.validSpawn.add(Blocks.STONE);
        this.validSpawn.add(Blocks.COBBLESTONE);
        this.validSpawn.add(Blocks.GRASS);
        this.validSpawn.add(Blocks.SAND);
        this.validSpawn.add(Blocks.DIRT);
        this.validSpawn.add(Blocks.GRAVEL);
        this.validSpawn.add(Blocks.NETHERRACK);
        this.validSpawn.add(Blocks.IRON_ORE);
        this.validSpawn.add(Blocks.GOLD_ORE);
        this.validSpawn.add(Blocks.DIAMOND_ORE);
        this.validSpawn.add(Blocks.REDSTONE_ORE);
        this.validSpawn.add(Blocks.ICE);
        this.validSpawn.add(Blocks.SNOW);

        this.invalidSpawn.add(blocks.skyStoneBlock().block());
        this.invalidSpawn.add(Blocks.IRON_DOOR);
        this.invalidSpawn.add(Blocks.IRON_BARS);
        this.invalidSpawn.add(Blocks.OAK_DOOR);
        this.invalidSpawn.add(Blocks.ACACIA_DOOR);
        this.invalidSpawn.add(Blocks.BIRCH_DOOR);
        this.invalidSpawn.add(Blocks.DARK_OAK_DOOR);
        this.invalidSpawn.add(Blocks.JUNGLE_DOOR);
        this.invalidSpawn.add(Blocks.SPRUCE_DOOR);
        this.invalidSpawn.add(Blocks.BRICKS);
        this.invalidSpawn.add(Blocks.CLAY);
        this.invalidSpawn.add(Blocks.WATER);
    }

    public PlacedMeteoriteSettings trySpawnMeteoriteAtSuitableHeight(IWorldReader world, int x, int y, int z) {

        for (int tries = 0; tries < 20; tries++) {
            PlacedMeteoriteSettings spawned = trySpawnMeteorite(world, new BlockPos(x, y, z));
            if (spawned != null) {
                return spawned;
            }

            y -= 15;
            if (y < 40) {
                return null;
            }
        }

        return null;
    }

    @Nullable
    public PlacedMeteoriteSettings trySpawnMeteorite(IWorldReader world, BlockPos pos) {
        Block blk = world.getBlockState(pos).getBlock();
        if (!this.validSpawn.contains(blk)) {
            return null; // must spawn on a valid block..
        }

        double meteoriteSize = (Math.random() * 6.0) + 2;
        double realCrater = meteoriteSize * 2 + 5;
        boolean lava = Math.random() > 0.9;

        if (!areSurroundingsSuitable(world, pos)) {
            return null;
        }

        // we can spawn here!
        int skyMode = countBlockWithSkyLight(world, pos);

        boolean solid = !isAirBelowSpawnPoint(world, pos);

        if (!solid) {
            skyMode = 0;
        }

        return new PlacedMeteoriteSettings(pos, blk.getRegistryName(), lava, skyMode, meteoriteSize, realCrater);
    }

    private static boolean isAirBelowSpawnPoint(IWorldReader w, BlockPos pos) {
        BlockPos.Mutable testPos = new BlockPos.Mutable(pos);
        for (int j = pos.getY() - 15; j < pos.getY() - 1; j++) {
            testPos.setY(j);
            if (w.isAirBlock(testPos)) {
                return true;
            }
        }
        return false;
    }

    private int countBlockWithSkyLight(IWorldReader w, BlockPos pos) {
        int skyMode = 0;

        BlockPos.Mutable testPos = new BlockPos.Mutable();
        for (int i = pos.getX() - 15; i < pos.getX() + 15; i++) {
            testPos.setX(i);
            for (int j = pos.getY() - 15; j < pos.getY() + 11; j++) {
                testPos.setY(j);
                for (int k = pos.getZ() - 15; k < pos.getZ() + 15; k++) {
                    testPos.setZ(k);
                    if (w.canBlockSeeSky(testPos)) {
                        skyMode++;
                    }
                }
            }
        }
        return skyMode;
    }

    private boolean areSurroundingsSuitable(IWorldReader w, BlockPos pos) {
        int realValidBlocks = 0;

        BlockPos.Mutable testPos = new BlockPos.Mutable();
        for (int i = pos.getX() - 6; i < pos.getX() + 6; i++) {
            testPos.setX(i);
            for (int j = pos.getY() - 6; j < pos.getY() + 6; j++) {
                testPos.setY(j);
                for (int k = pos.getZ() - 6; k < pos.getZ() + 6; k++) {
                    testPos.setZ(k);
                    Block testBlk = w.getBlockState(testPos).getBlock();
                    if (this.validSpawn.contains(testBlk)) {
                        realValidBlocks++;
                    }
                }
            }
        }

        int validBlocks = 0;
        for (int i = pos.getX() - 15; i < pos.getX() + 15; i++) {
            testPos.setX(i);
            for (int j = pos.getY() - 15; j < pos.getY() + 15; j++) {
                testPos.setY(j);
                for (int k = pos.getZ() - 15; k < pos.getZ() + 15; k++) {
                    testPos.setZ(k);
                    Block testBlk = w.getBlockState(testPos).getBlock();
                    if (this.invalidSpawn.contains(testBlk)) {
                        return false;
                    }
                    if (this.validSpawn.contains(testBlk)) {
                        validBlocks++;
                    }
                }
            }
        }

        final int minBlocks = 200;
        return validBlocks > minBlocks && realValidBlocks > 80;
    }

}
