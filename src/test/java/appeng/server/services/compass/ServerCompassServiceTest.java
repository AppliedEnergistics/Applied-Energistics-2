package appeng.server.services.compass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.SavedDataStorage;
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

        var dimensionalDataStorage = new SavedDataStorage(
                level,
                tempDir,
                DataFixers.getDataFixer(),
                server.registryAccess());
        when(level.getDataStorage()).thenReturn(dimensionalDataStorage);

        var region1 = CompassRegion.get(level, cp1);
        var region2 = CompassRegion.get(level, cp2);
        region1.setHasCompassTarget(cp1.x(), cp1.z(), 0, true);
        assertTrue(region1.hasCompassTarget(cp1.x(), cp1.z(), 0));
        assertFalse(region2.hasCompassTarget(cp2.x(), cp2.z(), 0));
        region2.setHasCompassTarget(cp2.x(), cp2.z(), 0, true);
    }

    @Test
    public void testClosestMeteoriteQueryIsCached(MinecraftServer server) {
        var dimensionalDataStorage = new SavedDataStorage(
                level,
                tempDir,
                DataFixers.getDataFixer(),
                server.registryAccess());
        when(level.getDataStorage()).thenReturn(dimensionalDataStorage);

        var origin = new ChunkPos(0, 0);
        assertTrue(ServerCompassService.getClosestMeteorite(level, origin).isEmpty());
        verify(level, atLeastOnce()).getDataStorage();

        clearInvocations(level);
        assertTrue(ServerCompassService.getClosestMeteorite(level, new ChunkPos(origin.x(), origin.z())).isEmpty());
        verify(level, never()).getDataStorage();
    }

    @Test
    public void testClosestMeteoriteCacheInvalidatedOnAnyChunkUpdate(MinecraftServer server) {
        var dimensionalDataStorage = new SavedDataStorage(
                level,
                tempDir,
                DataFixers.getDataFixer(),
                server.registryAccess());
        when(level.getDataStorage()).thenReturn(dimensionalDataStorage);
        when(level.getChunkSource()).thenReturn(mock(ServerChunkCache.class));

        var origin = new ChunkPos(0, 0);
        var changedChunkPos = new ChunkPos(1, 0);
        var compassRegion = CompassRegion.get(level, origin);
        compassRegion.setHasCompassTarget(origin.x(), origin.z(), 0, true);
        assertEquals(origin.getMiddleBlockPosition(0),
                ServerCompassService.getClosestMeteorite(level, origin).orElseThrow());

        compassRegion.setHasCompassTarget(origin.x(), origin.z(), 0, false);
        compassRegion.setHasCompassTarget(changedChunkPos.x(), changedChunkPos.z(), 0, true);
        var changedChunk = mock(ChunkAccess.class);
        when(changedChunk.getPos()).thenReturn(changedChunkPos);
        ServerCompassService.updateArea(level, changedChunk);

        assertEquals(changedChunkPos.getMiddleBlockPosition(0),
                ServerCompassService.getClosestMeteorite(level, origin).orElseThrow());
    }

}
