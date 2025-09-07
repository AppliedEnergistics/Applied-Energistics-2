package appeng.helpers.patternprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.me.helpers.BaseActionSource;
import appeng.util.BootstrapMinecraft;
import appeng.util.ConfigInventory;

@BootstrapMinecraft
class GenericStackInvTest {
    private final RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    public static final AEItemKey STICK_KEY = AEItemKey.of(Items.STICK);
    public static final GenericStack ONE_STICK = new GenericStack(STICK_KEY, 1);
    public static final IActionSource SRC = new BaseActionSource();
    private final AtomicInteger changeNotifications = new AtomicInteger();

    private final GenericStackInv inv = new GenericStackInv(changeNotifications::incrementAndGet, 2);

    @Test
    void testIsEmptyOnEmptyInventory() {
        assertTrue(inv.isEmpty());
    }

    @Test
    void testIsEmptyOnNonEmptyInventory() {
        inv.setStack(0, ONE_STICK);
        assertFalse(inv.isEmpty());
    }

    /**
     * Tests that saving a larger inventory and loading it into a smaller one works and just loads the first few slots.
     */
    @Test
    void testSaveLargeAndLoadIntoSmallerInventory() {
        var large = ConfigInventory.configStacks(2).supportedType(AEKeyType.items()).build();
        large.setStack(0, ONE_STICK);
        large.setStack(1, ONE_STICK);

        roundtripList(large::writeToTag, inv::readFromTag);

        assertEquals(ONE_STICK, inv.getStack(0));
        // Size didnt change...
        assertEquals(2, inv.size());
    }

    @Test
    void testLoadingFromEmptyTagClearsFilledSlots() {
        inv.setStack(0, ONE_STICK);
        inv.readFromTag(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, new CompoundTag())
                .childrenListOrEmpty("x"));
        assertNull(inv.getStack(0));
    }

    @Test
    void testWritingAnEmptyInventoryProducesAnEmptyTag() {
        var serializedTag = toListTag(inv::writeToTag);
        assertEquals(0, serializedTag.size());
    }

    /**
     * We optimize empty inventories by not saving their child tag.
     */
    @Test
    void testWritingAnEmptyInventoryProducesNoChildTag() {
        var tag = toTag(o -> inv.writeToChildTag(o, "child"));
        assertEquals(new CompoundTag(), tag);
    }

    /**
     * Writing to child tags works as expected and only produces the named child tag.
     */
    @Test
    void testWritingToChildTag() {
        inv.setStack(0, ONE_STICK);
        var tag = toTag(o -> inv.writeToChildTag(o, "child"));
        assertThat(tag.keySet()).containsOnly("child");
    }

    /**
     * Reading from child tags restores the inventory.
     */
    @Test
    void testReadingFromChildTag() {
        inv.setStack(0, ONE_STICK);
        var tag = toTag(o -> inv.writeToChildTag(o, "child"));
        inv.clear();
        changeNotifications.set(0);
        fromTag(tag, i -> inv.readFromChildTag(i, "child"));

        assertEquals(ONE_STICK, inv.getStack(0));
        assertEquals(1, changeNotifications.get());
    }

    /**
     * Change notifications shouldn't occur if readFromTag doesn't actually change anything
     */
    @Test
    void testReadFromTagOnlyNotifiesOnChanges() {
        var otherInv = new GenericStackInv(null, 2);
        otherInv.setStack(0, ONE_STICK);

        // Read once
        roundtripList(otherInv::writeToTag, inv::readFromTag);
        assertEquals(1, changeNotifications.get());

        // Read again
        roundtripList(otherInv::writeToTag, inv::readFromTag);
        assertEquals(1, changeNotifications.get());

        // Notification on clear
        fromListTag(new ListTag(), inv::readFromTag);
        assertEquals(2, changeNotifications.get());
    }

    @Test
    void testClear() {
        inv.setStack(0, ONE_STICK);
        inv.setStack(1, ONE_STICK);
        changeNotifications.set(0);
        inv.clear();
        assertTrue(inv.isEmpty());

        // Should only call change notifications once
        assertEquals(1, changeNotifications.get());
    }

    @Test
    void testClearDoesntNotifyWhenNothingChanges() {
        inv.clear();
        assertEquals(0, changeNotifications.get());
    }

    /**
     * readFromChildTag should clear the inventory if the tag doesn't exist.
     */
    @Test
    void testReadingFromMissingChildTag() {
        inv.setStack(0, ONE_STICK);
        fromTag(new CompoundTag(), i -> inv.readFromChildTag(i, "child"));
        assertNull(inv.getStack(0));
    }

    @Test
    void testGetKeyForEmptySlot() {
        assertNull(inv.getKey(0));
    }

    @Test
    void testGetAmountForEmptySlot() {
        assertEquals(0, inv.getAmount(0));
    }

    @Test
    void testGetKeyForFilledSlot() {
        inv.setStack(0, ONE_STICK);
        assertEquals(STICK_KEY, inv.getKey(0));
    }

    @Test
    void testGetAmountForFilledSlot() {
        inv.setStack(0, ONE_STICK);
        assertEquals(1, inv.getAmount(0));
    }

    @Nested
    class ChangeBatching {
        @Test
        void testChangeBatching() {
            inv.beginBatch();
            inv.setStack(0, ONE_STICK);
            inv.setStack(0, null);
            inv.setStack(0, ONE_STICK);
            fromListTag(new ListTag(), inv::readFromTag);
            assertEquals(0, changeNotifications.get());
            inv.endBatch();
            assertEquals(1, changeNotifications.get());
        }

        @Test
        void testEndBatchDoesntNotifyIfNothingHappened() {
            inv.beginBatch();
            inv.endBatch();
            assertEquals(0, changeNotifications.get());
        }

        @Test
        void testBeginAndEndBatchMustBePaired() {
            inv.beginBatch();
            assertThrows(IllegalStateException.class, inv::beginBatch);
            inv.endBatch();
            assertThrows(IllegalStateException.class, inv::endBatch);
        }
    }

    @Nested
    class Insert {
        @Test
        void testInsertIntoEmptySlot() {
            assertEquals(1, inv.insert(0, STICK_KEY, 1, Actionable.SIMULATE));
            assertNull(inv.getStack(0));
            assertEquals(1, inv.insert(0, STICK_KEY, 1, Actionable.MODULATE));
            assertEquals(ONE_STICK, inv.getStack(0));
        }

        @Test
        void testBulkInsert() {
            assertEquals(70, inv.insert(STICK_KEY, 70, Actionable.SIMULATE, SRC));
        }
    }

    @Nested
    class Extract {
        @Test
        void testPartialExtract() {
            inv.setStack(0, new GenericStack(STICK_KEY, 32));
            assertEquals(10, inv.extract(0, STICK_KEY, 10, Actionable.SIMULATE));
            assertEquals(new GenericStack(STICK_KEY, 32), inv.getStack(0));
            assertEquals(10, inv.extract(0, STICK_KEY, 10, Actionable.MODULATE));
            assertEquals(new GenericStack(STICK_KEY, 22), inv.getStack(0));
        }

        @Test
        void testBulkExtract() {
            inv.setStack(0, new GenericStack(STICK_KEY, 64));
            inv.setStack(1, new GenericStack(STICK_KEY, 64));
            assertEquals(70, inv.extract(STICK_KEY, 70, Actionable.SIMULATE, SRC));
        }
    }

    private CompoundTag toTag(Consumer<ValueOutput> serializer) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registryAccess);
        serializer.accept(output);
        return output.buildResult();
    }

    private ListTag toListTag(Consumer<ValueOutput.ValueOutputList> serializer) {
        return toTag(tag -> serializer.accept(tag.childrenList("x"))).getListOrEmpty("x");
    }

    private void fromTag(CompoundTag tag, Consumer<ValueInput> deserializer) {
        var input = TagValueInput.create(ProblemReporter.DISCARDING, registryAccess, tag);
        deserializer.accept(input);
    }

    private void fromListTag(ListTag tag, Consumer<ValueInput.ValueInputList> deserializer) {
        var compound = new CompoundTag();
        compound.put("x", tag);
        fromTag(compound, o -> deserializer.accept(o.childrenListOrEmpty("x")));
    }

    private void roundtripList(Consumer<ValueOutput.ValueOutputList> serializer,
            Consumer<ValueInput.ValueInputList> deserializer) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registryAccess);
        serializer.accept(output.childrenList("x"));
        fromTag(output.buildResult(), o -> deserializer.accept(o.childrenListOrEmpty("x")));
    }
}
