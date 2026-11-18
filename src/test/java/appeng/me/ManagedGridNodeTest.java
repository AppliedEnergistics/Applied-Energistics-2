package appeng.me;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.assertj.core.data.Offset;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;

class ManagedGridNodeTest extends AbstractGridNodeTest {
    public static final Offset<Double> TOLERANCE = Offset.offset(0.1);
    private final Object owner = new Object();

    @Test
    void testGridStorageRetrieval() {

        // Create the NBT tag that contains a fully loaded energy service
        var precursorNode = createAndInitNode();
        var precursorEnergyService = precursorNode.getGrid().getEnergyService();
        precursorEnergyService.injectPower(10, Actionable.MODULATE);
        assertThat(precursorEnergyService.extractAEPower(10, Actionable.SIMULATE, PowerMultiplier.ONE))
                .isCloseTo(10, Offset.offset(0.1));
        var savedNode = new CompoundTag();
        precursorNode.saveToNBT(savedNode);
        precursorNode.destroy();

        // We create 2 nodes and re-initialize both with the same storage
        // We should have twice the storage as before.
        var mgn1 = new ManagedGridNode(owner, listener);
        mgn1.loadFromNBT(savedNode);
        var mgn2 = new ManagedGridNode(owner, listener);
        mgn2.loadFromNBT(savedNode);

        mgn1.create(level, BlockPos.ZERO);
        mgn2.create(level, BlockPos.ZERO);
        GridConnection.create(
                mgn1.getNode(),
                mgn2.getNode(),
                null);

        assertSame(mgn1.getGrid(), mgn2.getGrid());
        assertThat(mgn1.getGrid().getEnergyService().extractAEPower(1000, Actionable.SIMULATE, PowerMultiplier.ONE))
                .isCloseTo(20, TOLERANCE);
    }

    /**
     * A block entity may be saved after its data was loaded but before its grid node was created (on the first tick).
     * The data read earlier must not get lost in that case (PR #8782).
     */
    @Test
    void testDataSurvivesSaveBeforeNodeCreation() {
        var precursorNode = createAndInitNode();
        ((GridNode) precursorNode.getNode()).setOwningPlayerId(42);
        var savedNode = new CompoundTag();
        precursorNode.saveToNBT(savedNode);
        precursorNode.destroy();

        // Load, then save again before the node exists
        var mgn1 = new ManagedGridNode(owner, listener);
        mgn1.loadFromNBT(savedNode);
        var intermediate = new CompoundTag();
        mgn1.saveToNBT(intermediate);

        // Load the intermediate save into a fresh node
        var mgn2 = new ManagedGridNode(owner, listener);
        mgn2.loadFromNBT(intermediate);
        mgn2.create(level, BlockPos.ZERO);

        assertThat(mgn2.getNode().getOwningPlayerId()).isEqualTo(42);
    }

    @NotNull
    private ManagedGridNode createAndInitNode() {
        var mgn = new ManagedGridNode(owner, listener);
        mgn.create(level, null);
        return mgn;
    }

}
