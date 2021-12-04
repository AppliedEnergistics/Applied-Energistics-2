package appeng.api.storage.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;

import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class AEItemKeyTest {
    @Test
    void testFuzzySearchValues() {
        var undamaged = AEItemKey.of(Items.DIAMOND_PICKAXE);
        var damagedStack = undamaged.toStack();
        damagedStack.setDamageValue(undamaged.getItem().getMaxDamage());
        var damaged = AEItemKey.of(damagedStack);

        assertEquals(damaged.getFuzzySearchMaxValue(), Items.DIAMOND_PICKAXE.getMaxDamage());
        assertEquals(damaged.getFuzzySearchValue(), Items.DIAMOND_PICKAXE.getMaxDamage());
        assertEquals(undamaged.getFuzzySearchValue(), 0);
    }

    @Nested
    class GenericTagSerialization {
        @Test
        void deserializeFromTagWithoutChannel() {
            assertNull(AEKey.fromTagGeneric(new CompoundTag()));
        }

        @Test
        void deserializeFromTagWithUnknownChannelId() {
            var tag = new CompoundTag();
            tag.putString("#c", "modid:doesnt_exist");

            assertNull(AEKey.fromTagGeneric(tag));
        }

        @Test
        void deserializeFromTagWithMalformedChannelId() {
            var tag = new CompoundTag();
            tag.putString("#c", "modid!!!!!doesnt_exist");

            assertNull(AEKey.fromTagGeneric(tag));
        }
    }
}
