package appeng.server.services.compass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
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
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

@ExtendWith(EphemeralTestServerProvider.class)
@MockitoSettings
class ServerCompassServiceTest {
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
    public void testClosestMeteoriteQueryIsCached() {
        var origin = new ChunkPos(0, 0);
        assertTrue(ServerCompassService.getClosestMeteorite(level, origin).isEmpty());
        verify(level, atLeastOnce()).getDataStorage();

        clearInvocations(level);
        assertTrue(ServerCompassService.getClosestMeteorite(level, new ChunkPos(origin.x, origin.z)).isEmpty());
        verify(level, never()).getDataStorage();
    }

    @Test
    public void testClosestMeteoriteCacheInvalidatedOnAnyChunkUpdate() {
        when(level.getChunkSource()).thenReturn(mock(ServerChunkCache.class));

        var origin = new ChunkPos(0, 0);
        var changedChunkPos = new ChunkPos(1, 0);
        var compassRegion = CompassRegion.get(level, origin);
        compassRegion.setHasCompassTarget(origin.x, origin.z, 0, true);
        assertEquals(origin.getMiddleBlockPosition(0),
                ServerCompassService.getClosestMeteorite(level, origin).orElseThrow());

        compassRegion.setHasCompassTarget(origin.x, origin.z, 0, false);
        compassRegion.setHasCompassTarget(changedChunkPos.x, changedChunkPos.z, 0, true);
        var changedChunk = mock(ChunkAccess.class);
        when(changedChunk.getPos()).thenReturn(changedChunkPos);
        ServerCompassService.updateArea(level, changedChunk);

        assertEquals(changedChunkPos.getMiddleBlockPosition(0),
                ServerCompassService.getClosestMeteorite(level, origin).orElseThrow());
    }

    @Test
    public void testClosestMeteoriteCacheRemovedOnLevelUnload() {
        var origin = new ChunkPos(0, 0);
        assertTrue(ServerCompassService.getClosestMeteorite(level, origin).isEmpty());

        ServerCompassService.onLevelUnload(new LevelEvent.Unload(level));

        clearInvocations(level);
        assertTrue(ServerCompassService.getClosestMeteorite(level, origin).isEmpty());
        verify(level, atLeastOnce()).getDataStorage();
    }
}
