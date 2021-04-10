package appeng.util.crafting;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.crafting.CraftingJob;
import appeng.util.crafting.mock.MockedApi;
import appeng.util.crafting.mock.MockedCrafting;
import appeng.util.crafting.mock.SimpleProcessingPattern;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

class CraftingJobTests {
    @BeforeAll
    static void bootstrapAndApi() {
        Bootstrap.register();
        MockedApi.init();
    }

    private static final int N = 3;
    private final List<IAEItemStack> items = new ArrayList<>();

    CraftingJobTests() {
        for (int i = 0; i < N; ++i) {
            ItemStack is = new ItemStack(Items.NAME_TAG);
            is.getOrCreateTag().putInt("i", i);
            items.add(AEItemStack.fromItemStack(is));
        }
    }

    /**
     * N-1 processing patterns each requiring 4 of item i+1 to make 2 of item i.
     * Every item is already in stock with an amount of 4, and the last one is emitable.
     * Requesting 8 items of the first kind should get 4 from storage and attempt to craft 4 others,
     * resulting in a request of 8 items of the second kind, etc...
     * Last one is emitable so the crafting should succeed.
     */
    @Test
    public void testProcessing() {
        MockedCrafting mockedCrafting = new MockedCrafting();
        List<ICraftingPatternDetails> patterns = new ArrayList<>();

        // Add patterns
        for (int i = 0; i < N-1; ++i) {
            ICraftingPatternDetails details = new SimpleProcessingPattern(items.get(i+1).copy().setStackSize(4), items.get(i).copy().setStackSize(2));
            patterns.add(details);
            mockedCrafting.cg.addPattern(details);
        }

        // Add storage - 4 of each
        for (int i = 0; i < N-1; ++i) {
            mockedCrafting.sg.getList().add(items.get(i).copy().setStackSize(4));
        }
        // Last one is emitable
        mockedCrafting.cg.setEmitable(items.get(N-1));

        // Do the thing
        CraftingJob job = mockedCrafting.doFullJob(items.get(0).copy().setStackSize(8));

        IItemList<IAEItemStack> plan = new ItemList();
        job.populatePlan(plan);

        // Somehow validate that it worked.
        for (int i = 0; i < N; ++i) {
            IAEItemStack stack = plan.findPrecise(items.get(i));
            if (i == N-1) {
                // emit 8
                assertEquals(stack.getCountRequestable(), 8);
            } else {
                // request 4
                assertEquals(stack.getCountRequestable(), 4);
                // use 4
                assertEquals(stack.getStackSize(), 4);
            }
        }
    }
}
