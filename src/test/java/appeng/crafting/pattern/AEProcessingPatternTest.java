package appeng.crafting.pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.AppEng;
import appeng.util.BootstrapMinecraft;
import appeng.util.LoadTranslations;
import appeng.util.RecursiveTagReplace;

@BootstrapMinecraft
@LoadTranslations
@MockitoSettings(strictness = Strictness.LENIENT)
class AEProcessingPatternTest {
    private final RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Mock
    MockedStatic<AppEng> appEngMock;

    @BeforeEach
    void setUp() {
        var appEngInstance = mock(AppEng.class);
        var clientLevel = mock(Level.class);
        when(appEngInstance.getClientLevel()).thenReturn(clientLevel);
        appEngMock.when(AppEng::instance).thenReturn(appEngInstance);
    }

    @Test
    void testDecodeWithEmptyTag() {
        assertNull(decode(new CompoundTag()));
    }

    /**
     * Sanity check that an encoded pattern that contains item-ids that are now invalid (i.e. mod removed, item removed
     * from mod, etc.) do not crash when being decoded.
     */
    @Test
    void testDecodeWithRemovedIngredientItemIds() {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                List.of(
                        GenericStack.fromItemStack(new ItemStack(Items.TORCH)),
                        GenericStack.fromItemStack(new ItemStack(Items.DIAMOND))),
                List.of(
                        GenericStack.fromItemStack(new ItemStack(Items.STICK))));
        var encodedTag = (CompoundTag) encoded.save(registryAccess);

        assertEquals(1, RecursiveTagReplace.replace(encodedTag, "minecraft:torch", "minecraft:unknown_item_id"));

        var reDecoded = decode(encodedTag);
        assertNull(reDecoded);
    }

    /**
     * Sanity check that an encoded pattern that contains item-ids that are now invalid (i.e. mod removed, item removed
     * from mod, etc.) does not crash when being decoded.
     */
    @Test
    void testDecodeWithRemovedResultItemIds() {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                List.of(
                        GenericStack.fromItemStack(new ItemStack(Items.TORCH)),
                        GenericStack.fromItemStack(new ItemStack(Items.DIAMOND))),
                List.of(
                        GenericStack.fromItemStack(new ItemStack(Items.STICK))));
        var encodedTag = (CompoundTag) encoded.save(registryAccess);

        // Replace the diamond ID string with an unknown ID string
        assertEquals(1, RecursiveTagReplace.replace(encodedTag, "minecraft:stick", "minecraft:does_not_exist"));

        assertNull(decode(encodedTag));
        assertThat(getExtraTooltip(encodedTag)).containsExactly(
                "Invalid Pattern",
                "Produces: 1 x minecraft:does_not_exist",
                "with: 1 x Torch",
                " and 1 x Diamond");
    }

    /**
     * Sanity check that an encoded pattern that contains stacks that reference missing storage channels.
     */
    @Test
    void testDecodeWithRemovedStorageChannels() {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                List.of(
                        GenericStack.fromItemStack(new ItemStack(Items.TORCH)),
                        GenericStack.fromItemStack(new ItemStack(Items.DIAMOND))),
                List.of(
                        GenericStack.fromItemStack(new ItemStack(Items.STICK))));
        var encodedTag = (CompoundTag) encoded.save(registryAccess);

        // Replace the channel of all items
        assertEquals(3, RecursiveTagReplace.replace(encodedTag, "ae2:i", "some_mod:missing_chan"));

        assertNull(decode(encodedTag));
        assertThat(getExtraTooltip(encodedTag)).containsExactly(
                "Invalid Pattern",
                "Produces: 1 x minecraft:stick (some_mod:missing_chan)",
                "with: 1 x minecraft:torch (some_mod:missing_chan)",
                " and 1 x minecraft:diamond (some_mod:missing_chan)");
    }

    private List<String> getExtraTooltip(CompoundTag tag) {
        var stack = ItemStack.parseOptional(registryAccess, tag);

        var lines = new ArrayList<Component>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.EMPTY, lines, TooltipFlag.ADVANCED);
        return lines.stream().map(Component::getString).toList();
    }

    private AEProcessingPattern decode(CompoundTag tag) {
        var stack = ItemStack.parseOptional(registryAccess, tag);

        var details = PatternDetailsHelper.decodePattern(AEItemKey.of(stack), mock(ServerLevel.class));
        if (details == null) {
            return null;
        }
        return assertInstanceOf(AEProcessingPattern.class, details);

    }
}
