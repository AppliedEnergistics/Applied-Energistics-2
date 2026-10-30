package appeng.server.services.compass;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

@ExtendWith(EphemeralTestServerProvider.class)
@MockitoSettings
class CompassRegionTest {
    private static final int SECTION = 3;

    @Mock
    ServerLevel level;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp(MinecraftServer server) {
        var dimensionalDataStorage = new DimensionDataStorage(
                tempDir.toFile(),
                DataFixers.getDataFixer(),
                server.registryAccess());
        when(level.getDataStorage()).thenReturn(dimensionalDataStorage);
    }

    @Test
    void testMigratesPositiveCoordinates() throws IOException {
        writeLegacyRegion(1, 2, 1024 + 5, 2048 + 7);

        assertTrue(hasTarget(1024 + 5, 2048 + 7));
        assertFalse(hasTarget(5, 2048 + 7));
        assertFalse(hasTarget(1024 + 5, 7));
    }

    @Test
    void testMigratesNegativeCoordinates() throws IOException {
        // Legacy regions were chosen using truncating division
        writeLegacyRegion(-1, -1, -1500, -1100);

        assertTrue(hasTarget(-1500, -1100));
        // Same offset within the region, but in a different legacy region
        assertFalse(hasTarget(-1500 + 1024, -1100));
        assertFalse(hasTarget(-1500, -1100 + 1024));
    }

    @Test
    void testMigratesNegativeRegionBoundary() throws IOException {
        // A chunk at a multiple of the region size was stored in the region of its own index
        writeLegacyRegion(-1, 0, -1024, 0);

        assertTrue(hasTarget(-1024, 0));
        assertFalse(hasTarget(-2048, 0));
    }

    /**
     * In legacy region 0, chunks on both sides of the origin used the same bits, so they are migrated to both sides.
     */
    @Test
    void testMigratesAmbiguousLegacyDataToBothSides() throws IOException {
        writeLegacyRegion(0, 0, 5, 7);

        assertTrue(hasTarget(5, 7));
        assertTrue(hasTarget(5 - 1024, 7));
        assertTrue(hasTarget(5, 7 - 1024));
        assertTrue(hasTarget(5 - 1024, 7 - 1024));
        // Unrelated chunks are not affected
        assertFalse(hasTarget(5 + 1024, 7));
        assertFalse(hasTarget(5 - 2048, 7));
    }

    @Test
    void testMigratedRegionIsSaved() throws IOException {
        writeLegacyRegion(0, 0, 5, 7);

        assertTrue(CompassRegion.get(level, new ChunkPos(5, 7)).isDirty());
        // Regions without any legacy data do not need to be saved
        assertFalse(CompassRegion.get(level, new ChunkPos(5000, 5000)).isDirty());
    }

    private boolean hasTarget(int cx, int cz) {
        return CompassRegion.get(level, new ChunkPos(cx, cz)).hasCompassTarget(cx, cz, SECTION);
    }

    /**
     * Writes a region in the legacy format, using the legacy index calculation.
     */
    private void writeLegacyRegion(int regionX, int regionZ, int cx, int cz) throws IOException {
        var bits = new BitSet();
        bits.set((cx & 1023) + (cz & 1023) * 1024);
        var data = new CompoundTag();
        data.putByteArray("section" + SECTION, bits.toByteArray());
        var root = new CompoundTag();
        root.put("data", data);
        NbtIo.writeCompressed(root, tempDir.resolve("ae2_compass_" + regionX + "_" + regionZ + ".dat"));
    }
}
