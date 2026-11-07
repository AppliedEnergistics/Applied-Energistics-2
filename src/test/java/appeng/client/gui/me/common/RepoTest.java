package appeng.client.gui.me.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Items;

import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.client.gui.widgets.ISortSource;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class RepoTest {

    private final Repo repo = new Repo(() -> 0, new ISortSource() {
        @Override
        public SortOrder getSortBy() {
            return SortOrder.NAME;
        }

        @Override
        public SortDir getSortDir() {
            return SortDir.ASCENDING;
        }

        @Override
        public ViewItems getSortDisplay() {
            return ViewItems.ALL;
        }

        @Override
        public Set<AEKeyType> getSortKeyTypes() {
            return Set.of(AEKeyType.items());
        }
    });

    @Test
    void craftableKeysAreCachedAndInvalidatedOnUpdate() {
        var diamond = AEItemKey.of(Items.DIAMOND);
        var dirt = AEItemKey.of(Items.DIRT);

        repo.handleUpdate(false, List.of(
                new GridInventoryEntry(1, diamond, 1, 0, false),
                new GridInventoryEntry(2, dirt, 1, 0, true)));

        assertThat(repo.isCraftable(dirt)).isTrue();
        assertThat(repo.isCraftable(diamond)).isFalse();

        var revision = repo.getRevision();
        var cachedKeys = repo.getCraftableKeys();
        assertThat(cachedKeys).containsExactlyInAnyOrder(dirt);
        assertThat(repo.getCraftableKeys()).isSameAs(cachedKeys);
        assertThat(repo.getRevision()).isEqualTo(revision);

        repo.handleUpdate(false, List.of(new GridInventoryEntry(1, diamond, 1, 0, true)));

        assertThat(repo.getRevision()).isGreaterThan(revision);
        assertThat(repo.getCraftableKeys()).isNotSameAs(cachedKeys);
        assertThat(repo.getCraftableKeys()).containsExactlyInAnyOrder(dirt, diamond);
        assertThat(repo.isCraftable(diamond)).isTrue();
    }

    @Test
    void clearingInvalidatesCraftableKeys() {
        var dirt = AEItemKey.of(Items.DIRT);
        repo.handleUpdate(false, List.of(new GridInventoryEntry(1, dirt, 1, 0, true)));
        assertThat(repo.getCraftableKeys()).containsExactlyInAnyOrder(dirt);

        var revision = repo.getRevision();
        repo.clear();

        assertThat(repo.getRevision()).isGreaterThan(revision);
        assertThat(repo.getCraftableKeys()).isEmpty();
        assertThat(repo.isCraftable(dirt)).isFalse();
    }

    @Test
    void changedKeysAreDrainedPerUpdate() {
        var diamond = AEItemKey.of(Items.DIAMOND);
        var dirt = AEItemKey.of(Items.DIRT);

        repo.handleUpdate(false, List.of(
                new GridInventoryEntry(1, diamond, 1, 0, false),
                new GridInventoryEntry(2, dirt, 1, 0, false)));
        assertThat(repo.drainChangedKeys()).containsExactlyInAnyOrder(diamond, dirt);
        assertThat(repo.drainChangedKeys()).isEmpty();

        // A removal is reported with the key that is gone
        repo.handleUpdate(false, List.of(
                new GridInventoryEntry(1, diamond, 5, 0, false),
                new GridInventoryEntry(2, null, 0, 0, false)));
        assertThat(repo.drainChangedKeys()).containsExactlyInAnyOrder(diamond, dirt);
        assertThat(repo.drainChangedKeys()).isEmpty();
    }

    @Test
    void fullUpdatesReportUnknownChangedKeys() {
        var dirt = AEItemKey.of(Items.DIRT);

        repo.handleUpdate(true, List.of(new GridInventoryEntry(1, dirt, 1, 0, false)));
        assertThat(repo.drainChangedKeys()).isNull();
        assertThat(repo.drainChangedKeys()).isEmpty();

        repo.clear();
        assertThat(repo.drainChangedKeys()).isNull();
    }

    @Test
    void emptyIncrementalUpdatesAreIgnored() {
        var dirt = AEItemKey.of(Items.DIRT);
        repo.handleUpdate(false, List.of(new GridInventoryEntry(1, dirt, 1, 0, true)));
        assertThat(repo.drainChangedKeys()).containsExactly(dirt);

        var revision = repo.getRevision();
        var cachedKeys = repo.getCraftableKeys();

        repo.handleUpdate(false, List.of());

        assertThat(repo.getRevision()).isEqualTo(revision);
        assertThat(repo.getCraftableKeys()).isSameAs(cachedKeys);
        assertThat(repo.drainChangedKeys()).isEmpty();

        // A full update still replaces the contents, even if it is empty
        repo.handleUpdate(true, List.of());

        assertThat(repo.getRevision()).isGreaterThan(revision);
        assertThat(repo.getCraftableKeys()).isEmpty();
        assertThat(repo.drainChangedKeys()).isNull();
    }
}
