package appeng.helpers;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.util.Platform;
import gregtech.api.items.metaitem.MetaItem;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Map;


public class NonBlockingItems {
    public static Map<String, Object2ObjectOpenHashMap<Item, IntSet>> NON_BLOCKING_MAP = new HashMap<>();
    public static NonBlockingItems INSTANCE = new NonBlockingItems();

    private NonBlockingItems() {
        String[] strings = AEConfig.instance().getNonBlockingItems();
        String[] modids = new String[0];
        if (strings.length > 0) {
            for (String s : strings) {
                if (s.startsWith("[") && s.endsWith("]")) {
                    modids = s.substring(1, s.length() - 1).split("\\|");
                } else {
                    for (String modid : modids) {
                        if (!Loader.isModLoaded(modid)) {
                            continue;
                        }
                        NON_BLOCKING_MAP.putIfAbsent(modid, new Object2ObjectOpenHashMap<>());

                        String[] ModItemMeta = s.split(":");

                        if (ModItemMeta.length < 2 || ModItemMeta.length > 3) {
                            AELog.error("Invalid non blocking item entry: " + s);
                            continue;
                        }

                        if (ModItemMeta[0].equals("gregtech") && Platform.isModLoaded("gregtech")) {
                            boolean found = false;
                            for (MetaItem<?> metaItem : MetaItem.getMetaItems()) {
                                MetaItem<?>.MetaValueItem metaItem2 = metaItem.getItem(ModItemMeta[1]);
                                if (metaItem.getItem(ModItemMeta[1]) != null) {
                                    found = true;
                                    ItemStack itemStack = metaItem2.getStackForm();
                                    NON_BLOCKING_MAP.get(modid).putIfAbsent(itemStack.getItem(), new IntOpenHashSet());
                                    NON_BLOCKING_MAP.get(modid).computeIfPresent(itemStack.getItem(), (item, intSet) ->
                                    {
                                        intSet.add(itemStack.getItemDamage());
                                        return intSet;
                                    });
                                } else {
                                    ItemStack itemStack = GameRegistry.makeItemStack(ModItemMeta[0] + ":" + ModItemMeta[1], ModItemMeta.length == 3 ? Integer.parseInt(ModItemMeta[2]) : 0, 1, null);
                                    if (!itemStack.isEmpty()) {
                                        NON_BLOCKING_MAP.get(modid).putIfAbsent(itemStack.getItem(), new IntOpenHashSet());
                                        NON_BLOCKING_MAP.get(modid).computeIfPresent(itemStack.getItem(), (item, intSet) ->
                                        {
                                            intSet.add(itemStack.getItemDamage());
                                            return intSet;
                                        });
                                    }
                                }
                            }
                            if (!found) {
                                AELog.error("Item not found on nonBlocking config: " + s);
                            }
                        } else if (ModItemMeta[0].equals("ore")) {
                            OreDictionary.getOres(ModItemMeta[1]).forEach(itemStack ->
                            {
                                NON_BLOCKING_MAP.get(modid).putIfAbsent(itemStack.getItem(), new IntOpenHashSet());
                                NON_BLOCKING_MAP.get(modid).computeIfPresent(itemStack.getItem(), (item, intSet) ->
                                {
                                    intSet.add(itemStack.getItemDamage());
                                    return intSet;
                                });
                            });
                        } else {
                            ItemStack itemStack = GameRegistry.makeItemStack(ModItemMeta[0] + ":" + ModItemMeta[1], ModItemMeta.length == 3 ? Integer.parseInt(ModItemMeta[2]) : 0, 1, null);
                            if (!itemStack.isEmpty()) {
                                NON_BLOCKING_MAP.get(modid).putIfAbsent(itemStack.getItem(), new IntOpenHashSet());
                                NON_BLOCKING_MAP.get(modid).computeIfPresent(itemStack.getItem(), (item, intSet) ->
                                {
                                    intSet.add(itemStack.getItemDamage());
                                    return intSet;
                                });
                            } else {
                                AELog.error("Item not found on nonBlocking config: " + s);
                            }
                        }
                    }
                }
            }
        }
    }

    public Map<String, Object2ObjectOpenHashMap<Item, IntSet>> getMap() {
        return NON_BLOCKING_MAP;
    }

    public void init() {
    }
}
