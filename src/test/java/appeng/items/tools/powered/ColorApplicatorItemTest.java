package appeng.items.tools.powered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.junit.jupiter.api.Test;

import net.minecraft.world.item.Items;

import appeng.api.stacks.AEItemKey;
import appeng.me.cells.BasicCellHandler;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class ColorApplicatorItemTest {
    @Test
    void testCreateFullColorApplicator() {
        var applicator = ColorApplicatorItem.createFullColorApplicator();
        var item = (ColorApplicatorItem) applicator.getItem();
        var access = ItemAccess.forStack(applicator);

        assertNotEquals(0, item.getAEMaxPower(access));
        assertEquals(item.getAEMaxPower(access), item.getAECurrentPower(access));

        // Get new storage and list content
        var dyeStorage = BasicCellHandler.INSTANCE.getCellInventory(applicator, null);
        var availableStacks = dyeStorage.getAvailableStacks();
        assertEquals(128, availableStacks.get(AEItemKey.of(Items.SNOWBALL)));
    }
}
