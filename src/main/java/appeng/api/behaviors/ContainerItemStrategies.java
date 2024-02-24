package appeng.api.behaviors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.util.CowMap;

/**
 * Manages {@link ContainerItemStrategy} registrations for {@linkplain AEKeyType key types}.
 */
public class ContainerItemStrategies {
    private static final CowMap<AEKeyType, ContainerItemStrategy<?, ?>> strategies = CowMap.identityHashMap();

    static {
        register(AEKeyType.fluids(), AEFluidKey.class, new FluidContainerItemStrategy());
    }

    public static <T extends AEKey> void register(AEKeyType type, Class<T> keyClass,
            ContainerItemStrategy<T, ?> strategy) {
        Preconditions.checkArgument(type.getKeyClass() == keyClass, "%s != %s", type.getKeyClass(), keyClass);
        Preconditions.checkArgument(type != AEKeyType.items(), "Can't register container items for AEItemKey");

        strategies.putIfAbsent(type, strategy);
    }

    public static boolean isTypeSupported(AEKeyType type) {
        return strategies.getMap().containsKey(type);
    }

    public static boolean isKeySupported(@Nullable AEKey key) {
        return key != null && isTypeSupported(key.getType());
    }

    @Nullable
    public static GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        for (var keyType : AEKeyTypes.getAll()) {
            var strategy = strategies.getMap().get(keyType);
            if (strategy != null) {
                var content = strategy.getContainedStack(stack);
                if (content != null) {
                    return content;
                }
            }
        }
        return null;
    }

    /**
     * Tries to get the content of the given key type contained in the given item - if any. Allows inspecting the
     * content of buckets, fluid tanks and other containers.
     */
    @Nullable
    public static GenericStack getContainedStack(ItemStack stack, AEKeyType keyType) {
        if (stack.isEmpty()) {
            return null;
        }

        var strategy = strategies.getMap().get(keyType);
        if (strategy != null) {
            return strategy.getContainedStack(stack);
        }
        return null;
    }

    @Nullable
    public static EmptyingAction getEmptyingAction(ItemStack stack) {
        var contents = getContainedStack(stack);
        if (contents == null) {
            return null;
        }

        var description = contents.what().getDisplayName();
        return new EmptyingAction(description, contents.what(), contents.amount());
    }

    public static ContainerItemContext findCarriedContextForKey(@Nullable AEKey key, Player player,
            AbstractContainerMenu menu) {
        return findCarriedContext(key == null ? null : key.getType(), player, menu);
    }

    @Nullable
    private static ContainerItemContext findContext(@Nullable AEKeyType keyType,
            Function<ContainerItemStrategy<?, ?>, @Nullable Object> contextFinder) {
        var candidates = keyType == null ? AEKeyTypes.getAll() : List.of(keyType);
        Map<AEKeyType, ContainerItemContext.Entry<?>> entries = new LinkedHashMap<>();
        for (var type : candidates) {
            var strategy = strategies.getMap().get(type);
            if (strategy != null) {
                var context = contextFinder.apply(strategy);
                if (context != null) {
                    // noinspection unchecked
                    entries.put(type, new ContainerItemContext.Entry<>((ContainerItemStrategy<AEKey, Object>) strategy,
                            context, type));
                }
            }
        }
        return entries.isEmpty() ? null : new ContainerItemContext(entries);
    }

    /**
     * @param keyType Desired key type, or null if any is ok.
     */
    @Nullable
    public static ContainerItemContext findCarriedContext(@Nullable AEKeyType keyType, Player player,
            AbstractContainerMenu menu) {
        return findContext(keyType, strategy -> strategy.findCarriedContext(player, menu));
    }

    public static Set<AEKeyType> getSupportedKeyTypes() {
        return strategies.getMap().keySet();
    }

    /**
     * Finds a context for an item that is in the possession of the player, but the precise location is unknown. It
     * might be in the inventory, on the body, or just in hand.
     *
     * @param keyType Desired key type, or null if any is ok.
     */
    @Nullable
    public static ContainerItemContext findOwnedItemContext(@Nullable AEKeyType keyType,
            Player player,
            ItemStack stack) {
        // Check if the player has an open menu and the stack is the carried stack first
        if (player.containerMenu != null && player.containerMenu.getCarried() == stack) {
            return findCarriedContext(keyType, player, player.containerMenu);
        }

        // Find the item inside the inventory and create a context for it
        var slotIdx = -1;
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (inventory.getItem(i) == stack) {
                slotIdx = i;
                break;
            }
        }

        if (slotIdx == -1) {
            return null; // Couldn't find the stack in the player inventory
        }

        int slotIdxCopy = slotIdx;
        return findContext(keyType, strategy -> strategy.findPlayerSlotContext(player, slotIdxCopy));
    }

}
