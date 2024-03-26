package appeng.items.tools.powered;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

        assertNotEquals(0, item.getAEMaxPower(applicator));
        assertEquals(item.getAEMaxPower(applicator), item.getAECurrentPower(applicator));

        // Get new storage and list content
        var dyeStorage = BasicCellHandler.INSTANCE.getCellInventory(applicator, null);
        var availableStacks = dyeStorage.getAvailableStacks();
        assertEquals(128, availableStacks.get(AEItemKey.of(Items.SNOWBALL)));
    }
}
