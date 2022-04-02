package appeng.menu.me.interaction;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.util.CowMap;

public class StackInteractions {
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

        for (var entry : strategies.getMap().entrySet()) {
            var content = entry.getValue().getContainedStack(stack);
            if (content != null) {
                return content;
            }
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

    @Nullable
    public static ContainerItemContext findCarriedContext(Player player, AbstractContainerMenu menu) {
        for (var entry : strategies.getMap().entrySet()) {
            var context = entry.getValue().findCarriedContext(player, menu);
            if (context != null) {
                return new ContainerItemContext((ContainerItemStrategy<AEKey, Object>) entry.getValue(), context);
            }
        }
        return null;
    }
}
