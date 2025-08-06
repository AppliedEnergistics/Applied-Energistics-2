package appeng.items.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.networking.GridHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.contents.PatternBoxMenuHost;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.items.PatternBoxMenu;

public class PatternBoxItem extends AEBaseItem implements IMenuItem, AEToolItem {
    public PatternBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable PatternBoxMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack,
            @Nullable BlockPos pos) {
        var level = player.level();
        if (pos == null) {
            return new PatternBoxMenuHost(player, inventorySlot, stack, null);
        }
        var host = GridHelper.getNodeHost(level, pos);
        return new PatternBoxMenuHost(player, inventorySlot, stack, host);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            MenuOpener.open(PatternBoxMenu.TYPE, player, MenuLocators.forHand(player, hand));
        }

        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide),
                player.getItemInHand(hand));
    }

    @Nullable
    public static PatternBoxMenuHost findPatternBoxHost(Player player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var item = inv.getItem(i);
            if (!item.isEmpty() && item.getItem() instanceof PatternBoxItem patternBoxItem) {
                return patternBoxItem.getMenuHost(player, i, item, null);
            }
        }
        return null;
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        var host = new PatternBoxMenuHost(null, null, stack, null);
        if (host.getInventory().isEmpty()) {
            return Optional.empty();
        }

        var patterns = new LinkedHashMap<AEItemKey, Integer>();
        for (var pattern : host.getInventory()) {
            patterns.merge(AEItemKey.of(pattern), pattern.getCount(), Integer::sum);
        }

        var stacks = new ArrayList<GenericStack>(patterns.size());
        for (var entry : patterns.entrySet()) {
            stacks.add(new GenericStack(entry.getKey(), entry.getValue()));
        }

        stacks.sort(Comparator.comparingLong(GenericStack::amount).reversed());
        return Optional.of(new StorageCellTooltipComponent(List.of(), stacks, false, true));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        if (other.isEmpty()) {
            return false;
        }

        insertIntoTool(stack, other, player);
        return true;
    }

    private void insertIntoTool(ItemStack tool, ItemStack pattern, Player player) {
        var host = new PatternBoxMenuHost(player, null, tool, null);
        var amount = pattern.getCount();
        var overflow = host.getInventory().addItems(pattern);
        pattern.shrink(amount - overflow.getCount());
        host.saveChanges();
    }
}
