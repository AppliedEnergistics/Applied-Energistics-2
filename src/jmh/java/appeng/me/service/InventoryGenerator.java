package appeng.me.service;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

public class InventoryGenerator {

    private final Random random;
    private final List<Item> items;
    private final List<Enchantment> enchantments;

    public InventoryGenerator(Random random) {
        this.random = random;
        items = BuiltInRegistries.ITEM.entrySet().stream().map(Map.Entry::getValue).toList();
        enchantments = BuiltInRegistries.ENCHANTMENT.entrySet().stream().map(Map.Entry::getValue).toList();
    }

    private Item randomItem() {
        return items.get(random.nextInt(items.size()));
    }

    private Enchantment randomEnchantment() {
        return enchantments.get(random.nextInt(enchantments.size()));
    }

    public void fillInventory(MEStorage storage, long targetUniques) {
        long total = 0, scale = 1;
        int fails = 0;
        do {
            long inserted = attemptFill(storage, scale);
            if (inserted <= 0) {
                fails++;
            } else {
                fails = 0;
                total += inserted;
                if (total >= targetUniques) {
                    scale++;
                }
            }
            if (fails > 100) {
                break;
            }
        } while (scale > 0);
        KeyCounter finalContents = storage.getAvailableStacks();
        System.out.println("Filled storage with " + total + " items in " + finalContents.size() + " unique stacks");
    }

    public long attemptFill(MEStorage storage, long scale) {
        long inserted = 0;
        Item item = randomItem();
        ItemStack stack = new ItemStack(item);
        if (item.isDamageable(stack)) {
            inserted += fillDamaged(storage, scale, item);
        }
        if (item.isEnchantable(stack)) {
            inserted += fillEnchantable(storage, scale, item);
        }
        inserted += fillRegular(storage, scale, new ItemStack(item));
        return inserted;
    }

    public long fillDamaged(MEStorage storage, long scale, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(random.nextInt(stack.getMaxDamage()));
        return fillRegular(storage, scale, stack);
    }

    public long fillEnchantable(MEStorage storage, long scale, Item item) {
        ItemStack stack = new ItemStack(item);
        Enchantment enchantment = randomEnchantment();
        stack.enchant(enchantment, 1 + random.nextInt(enchantment.getMaxLevel()));
        return fillRegular(storage, scale, stack);
    }

    public long fillRegular(MEStorage storage, long scale, ItemStack stack) {
        GenericStack item = GenericStack.fromItemStack(stack);
        if (item == null) {
            return 1;
        }
        return storage.insert(item.what(), 1L + random.nextLong(scale), Actionable.MODULATE, IActionSource.empty());
    }
}
