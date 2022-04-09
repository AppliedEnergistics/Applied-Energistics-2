package appeng.me.service.helpers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Items;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.simulation.helpers.ProcessingPatternBuilder;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class NetworkCraftingProvidersTest {
    @Test
    void basicTest() {
        var craftingProviders = new NetworkCraftingProviders();
        var andesite = AEItemKey.of(Items.ANDESITE);
        var pattern = new ProcessingPatternBuilder(new GenericStack(andesite, 1)).build();
        var diamond = AEItemKey.of(Items.DIAMOND);
        var testProvider = new ICraftingProvider() {
            @Override
            public List<IPatternDetails> getAvailablePatterns() {
                return List.of(pattern);
            }

            @Override
            public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
                return false;
            }

            @Override
            public boolean isBusy() {
                return true;
            }

            @Override
            public Set<AEKey> getEmitableItems() {
                return Set.of(diamond);
            }
        };
        var testNode1 = mock(IGridNode.class);
        when(testNode1.getService(ICraftingProvider.class)).thenReturn(testProvider);
        var testNode2 = mock(IGridNode.class);
        when(testNode2.getService(ICraftingProvider.class)).thenReturn(testProvider);
        var testNode3 = mock(IGridNode.class);
        when(testNode3.getService(ICraftingProvider.class)).thenReturn(testProvider);

        assertThat(craftingProviders.getCraftingFor(andesite)).isEmpty();
        assertThat(craftingProviders.getCraftables(k -> true)).isEmpty();
        assertThat(craftingProviders.canEmitFor(diamond)).isFalse();
        assertThat(craftingProviders.getMediums(pattern)).isEmpty();

        craftingProviders.addProvider(testNode1);
        craftingProviders.addProvider(testNode2);
        craftingProviders.addProvider(testNode3);

        craftingProviders.getMediums(pattern).iterator().next();

        assertThat(craftingProviders.getCraftingFor(andesite)).isNotEmpty();
        assertThat(craftingProviders.getCraftables(k -> true)).isNotEmpty();
        assertThat(craftingProviders.canEmitFor(diamond)).isTrue();
        assertThat(craftingProviders.getMediums(pattern)).isNotEmpty();

        craftingProviders.removeProvider(testNode1);

        // Ensure there is no CME when providers are removed after a partial iteration.
        craftingProviders.getMediums(pattern).iterator().next();

        craftingProviders.removeProvider(testNode2);
        craftingProviders.removeProvider(testNode3);

        assertThat(craftingProviders.getCraftingFor(andesite)).isEmpty();
        assertThat(craftingProviders.getCraftables(k -> true)).isEmpty();
        assertThat(craftingProviders.canEmitFor(diamond)).isFalse();
        assertThat(craftingProviders.getMediums(pattern)).isEmpty();
    }

    @Test
    void testPatternPriorities() {
        var craftingProviders = new NetworkCraftingProviders();
        var andesite = AEItemKey.of(Items.ANDESITE);

        final int TEST_COUNT = 50;
        IPatternDetails[] patterns = new IPatternDetails[TEST_COUNT];
        IGridNode[] providerNodes = new IGridNode[TEST_COUNT];

        for (int i = 0; i < TEST_COUNT; ++i) {
            var pattern = patterns[i] = new ProcessingPatternBuilder(new GenericStack(andesite, 1)).build();
            int priority = i / 2;
            var provider = new ICraftingProvider() {
                @Override
                public List<IPatternDetails> getAvailablePatterns() {
                    return List.of(pattern);
                }

                @Override
                public int getPatternPriority() {
                    return priority;
                }

                @Override
                public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
                    return false;
                }

                @Override
                public boolean isBusy() {
                    return true;
                }
            };
            var nodeMock = providerNodes[i] = mock(IGridNode.class);
            when(nodeMock.getService(ICraftingProvider.class)).thenReturn(provider);

            craftingProviders.addProvider(nodeMock);

            var firstPattern = craftingProviders.getCraftingFor(andesite).iterator().next();

            if (i % 2 == 0) {
                // Check that the new pattern is at the top
                assertThat(firstPattern).isEqualTo(pattern);
            } else {
                // Top 2 should have the same priority, which one comes first is unpredictable;
                var previousPattern = patterns[i - 1];
                assertThat(firstPattern).satisfiesAnyOf(
                        fp -> assertThat(fp).isEqualTo(pattern),
                        fp -> assertThat(fp).isEqualTo(previousPattern));
            }
        }

        // Now remove all
        for (int i = 0; i < TEST_COUNT; ++i) {
            craftingProviders.removeProvider(providerNodes[i]);

            assertThat(craftingProviders.getCraftingFor(andesite)).hasSize(TEST_COUNT - i - 1);
        }
    }
}
