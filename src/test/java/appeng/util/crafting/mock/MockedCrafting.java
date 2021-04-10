package appeng.util.crafting.mock;

import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingJob;
import appeng.me.helpers.BaseActionSource;
import net.minecraft.world.World;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockedCrafting {
    public final MockedCraftingGrid cg = new MockedCraftingGrid();
    public final MockedStorageGrid sg = new MockedStorageGrid();
    public final World world;

    public MockedCrafting() {
        world = mock(World.class);
        when(world.isRemote()).thenReturn(false);
    }

    public CraftingJob doFullJob(IAEItemStack craftWhat) {
        try {
            // Create job
            CraftingJob job = new CraftingJob(world, cg, sg, new BaseActionSource(), craftWhat, j -> {
            });
            Thread jobThread = new Thread(job);
            jobThread.start();
            // Wait for it to finish
            job.simulateFor(10000000);
            return job;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
