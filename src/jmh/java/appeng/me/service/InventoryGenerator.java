package appeng.me.service;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
    private final List<Holder<Enchantment>> enchantments;

    public InventoryGenerator(Random random, RegistryAccess registryAccess) {
        this.random = random;
        // Sort by ID, since the registry's entry set is ordered by identity hash codes, which differ between JVMs
        items = BuiltInRegistries.ITEM.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toList();
        // Enchantments are data-driven and only available from a server's registries
        enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .<Holder<Enchantment>>map(holder -> holder)
                .toList();
    }

    private Item randomItem() {
        return items.get(random.nextInt(items.size()));
    }

    private Holder<Enchantment> randomEnchantment() {
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
        if (stack.isEnchantable()) {
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
        Holder<Enchantment> enchantment = randomEnchantment();
        stack.enchant(enchantment, 1 + random.nextInt(enchantment.value().getMaxLevel()));
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
