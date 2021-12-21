package appeng.menu.locator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.parts.AEBasePart;

/**
 * {@link MenuLocator} is used to find the host of a menu on both the server and client-side in a predictable manner and
 * transfer this information from the server to the client when a menu should be opened there.
 * <p/>
 * This class exposes several useful factory methods for obtaining a menu locator, as well as a registry for addons to
 * register their own menu locators.
 */
public final class MenuLocators {
    private static final Map<String, Registration<?>> REGISTRY = new HashMap<>();

    static {
        register(BlockEntityLocator.class, BlockEntityLocator::writeToPacket, BlockEntityLocator::readFromPacket);
        register(PartLocator.class, PartLocator::writeToPacket, PartLocator::readFromPacket);
        register(MenuItemLocator.class, MenuItemLocator::writeToPacket, MenuItemLocator::readFromPacket);
    }

    /**
     * Register a new implementation of {@link MenuLocator}. This is required to serialize it to/from the network.
     */
    public static synchronized <T extends MenuLocator> void register(Class<T> locatorClass,
            BiConsumer<T, FriendlyByteBuf> packetWriter,
            Function<FriendlyByteBuf, T> packetReader) {
        String classKey = locatorClass.getName();
        if (REGISTRY.containsKey(classKey)) {
            throw new IllegalStateException("MenuLocator type " + classKey + " is already registered.");
        }
        REGISTRY.put(classKey, new Registration<>(locatorClass, packetWriter, packetReader));
    }

    private synchronized static Registration<?> getRegistration(String classKey) {
        var registration = REGISTRY.get(classKey);
        if (registration == null) {
            throw new IllegalArgumentException("Unregistered menu locator class: " + classKey);
        }
        return registration;
    }

    @SuppressWarnings("unchecked")
    public static <T extends MenuLocator> void writeToPacket(FriendlyByteBuf buf, T locator) {
        var classKey = locator.getClass().getName();
        var registration = (Registration<T>) getRegistration(classKey);
        buf.writeUtf(classKey);
        registration.writeToPacket.accept(locator, buf);
    }

    public static MenuLocator readFromPacket(FriendlyByteBuf buf) {
        var classKey = buf.readUtf();

        var registration = getRegistration(classKey);
        return registration.readFromPacket.apply(buf);
    }

    public static MenuLocator forBlockEntity(BlockEntity te) {
        if (te.getLevel() == null) {
            throw new IllegalArgumentException("Cannot open a block entity that is not in a level");
        }
        return new BlockEntityLocator(te.getBlockPos());
    }

    public static MenuLocator forPart(AEBasePart part) {
        var pos = part.getHost().getLocation();
        return new PartLocator(pos.getPos(), part.getSide());
    }

    /**
     * Construct a menu locator for an item being used on a block. The item could still open a menu for itself, but it
     * might also open a special menu for the block being right-clicked.
     */
    public static MenuLocator forItemUseContext(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            throw new IllegalArgumentException("Cannot open a menu without a player");
        }
        int slot = getPlayerInventorySlotFromHand(player, context.getHand());
        return new MenuItemLocator(slot, context.getClickedPos());
    }

    public static MenuLocator forHand(Player player, InteractionHand hand) {
        int slot = getPlayerInventorySlotFromHand(player, hand);
        return forInventorySlot(slot);
    }

    public static MenuLocator forInventorySlot(int inventorySlot) {
        return new MenuItemLocator(inventorySlot, null);
    }

    private static int getPlayerInventorySlotFromHand(Player player, InteractionHand hand) {
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty()) {
            throw new IllegalArgumentException("Cannot open an item-inventory with empty hands");
        }
        int invSize = player.getInventory().getContainerSize();
        for (int i = 0; i < invSize; i++) {
            if (player.getInventory().getItem(i) == is) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find item held in hand " + hand + " in player inventory");
    }

    private record Registration<T extends MenuLocator> (
            Class<T> locatorClass,
            BiConsumer<T, FriendlyByteBuf> writeToPacket,
            Function<FriendlyByteBuf, T> readFromPacket) {
    }
}
