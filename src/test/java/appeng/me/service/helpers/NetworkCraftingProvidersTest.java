package appeng.me.service.helpers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.crafting.simulation.helpers.ProcessingPatternBuilder;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class NetworkCraftingProvidersTest {
    @Test
    void basicTest() {
        var craftingProviders = new NetworkCraftingProviders();
        var pattern = new ProcessingPatternBuilder(GenericStack.fromItemStack(new ItemStack(Items.ANDESITE))).build();
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

        assertThat(craftingProviders.canEmitFor(diamond)).isFalse();
        assertThat(craftingProviders.getMediums(pattern)).isEmpty();

        craftingProviders.addProvider(testNode1);
        craftingProviders.addProvider(testNode2);
        craftingProviders.addProvider(testNode3);

        craftingProviders.getMediums(pattern).iterator().next();

        assertThat(craftingProviders.canEmitFor(diamond)).isTrue();
        assertThat(craftingProviders.getMediums(pattern)).isNotEmpty();

        craftingProviders.removeProvider(testNode1);

        // Ensure there is no CME when providers are removed after a partial iteration.
        craftingProviders.getMediums(pattern).iterator().next();

        craftingProviders.removeProvider(testNode2);
        craftingProviders.removeProvider(testNode3);

        assertThat(craftingProviders.canEmitFor(diamond)).isFalse();
        assertThat(craftingProviders.getMediums(pattern)).isEmpty();
    }
}
