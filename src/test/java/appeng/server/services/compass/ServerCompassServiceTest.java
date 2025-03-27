package appeng.server.services.compass;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

@ExtendWith(EphemeralTestServerProvider.class)
@MockitoSettings
class ServerCompassServiceTest {
    @Mock
    ServerLevel level;

    @TempDir
    Path tempDir;

    /**
     * Regression test that checks that the chunks around the origin of the region grid do not actually overlap each
     * other which was the case when the coordinates for the grid were improperly calculated.
     */
    @Test
    public void testOverlapAtZero(MinecraftServer server) {
        var cp1 = new ChunkPos(1, 0);
        var cp2 = new ChunkPos(-1023, 0);

        var dimensionalDataStorage = new DimensionDataStorage(
                new SavedData.Context(level, 0L),
                tempDir,
                DataFixers.getDataFixer(),
                server.registryAccess());
        when(level.getDataStorage()).thenReturn(dimensionalDataStorage);

        var region1 = CompassRegion.get(level, cp1);
        var region2 = CompassRegion.get(level, cp2);
        region1.setHasCompassTarget(cp1.x, cp1.z, 0, true);
        assertTrue(region1.hasCompassTarget(cp1.x, cp1.z, 0));
        assertFalse(region2.hasCompassTarget(cp2.x, cp2.z, 0));
        region2.setHasCompassTarget(cp2.x, cp2.z, 0, true);
    }

}
