package appeng.core.network.clientbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.menu.me.common.IncrementalUpdateHelper;

class MEInventoryUpdatePacketTest {
    @Test
    void fullUpdateIsSentWhenItContainsNoEntries() {
        var packets = MEInventoryUpdatePacket.builder(42, true, RegistryAccess.EMPTY).build();

        assertThat(packets).hasSize(1);
        assertThat(packets.getFirst().fullUpdate()).isTrue();
        assertThat(packets.getFirst().containerId()).isEqualTo(42);
        assertThat(packets.getFirst().entries()).isEmpty();
        assertThat(packets.getFirst().encodedEntryCount()).isZero();
    }

    @Test
    void fullUpdateIsSentWhenAllEntriesAreFilteredOut() {
        var key = mock(AEKey.class);
        var storage = new KeyCounter();
        storage.set(key, 1);

        var builder = MEInventoryUpdatePacket.builder(42, true, RegistryAccess.EMPTY);
        builder.setFilter(candidate -> false);
        builder.addFull(new IncrementalUpdateHelper(), storage, Set.of(), new KeyCounter());

        var packets = builder.build();
        assertThat(packets).hasSize(1);
        assertThat(packets.getFirst().fullUpdate()).isTrue();
        assertThat(packets.getFirst().entries()).isEmpty();
    }

    @Test
    void emptyIncrementalUpdateDoesNotSendAPacket() {
        var packets = MEInventoryUpdatePacket.builder(42, false, RegistryAccess.EMPTY).build();

        assertThat(packets).isEmpty();
    }
}
