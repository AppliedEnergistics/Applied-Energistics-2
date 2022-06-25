package appeng.recipes.transform;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public final class TransformLogic {
    public static boolean canTransform(ItemEntity entity) {
        return getTransformableItems(entity.getLevel()).contains(entity.getItem().getItem());
    }

    public static boolean tryTransform(ItemEntity entity) {
        var level = entity.level;

        var region = new AABB(entity.getX() - 1, entity.getY() - 1, entity.getZ() - 1, entity.getX() + 1,
                entity.getY() + 1, entity.getZ() + 1);
        List<ItemEntity> itemEntities = level.getEntities(null, region).stream()
                .filter(e -> e instanceof ItemEntity && !e.isRemoved()).map(e -> (ItemEntity) e).toList();

        for (var recipe : level.getRecipeManager().byType(TransformRecipe.TYPE).values()) {
            List<Ingredient> missingIngredients = Lists.newArrayList(recipe.ingredients);
            Set<ItemEntity> selectedEntities = new ReferenceOpenHashSet<>(missingIngredients.size());

            if (missingIngredients.size() == 0 || !missingIngredients.get(0).test(entity.getItem())) {
                continue;
            }

            missingIngredients.remove(0);
            selectedEntities.add(entity);

            for (var itemEntity : itemEntities) {
                final ItemStack other = itemEntity.getItem();
                if (!other.isEmpty()) {
                    for (var it = missingIngredients.iterator(); it.hasNext();) {
                        Ingredient ing = it.next();
                        if (ing.test(other)) {
                            selectedEntities.add(itemEntity);
                            it.remove();
                            break;
                        }
                    }
                }
            }

            if (missingIngredients.isEmpty()) {
                for (var e : selectedEntities) {
                    e.getItem().grow(-1);

                    if (e.getItem().getCount() <= 0) {
                        e.discard();
                    }
                }

                var random = level.getRandom();
                final double x = Math.floor(entity.getX()) + .25d + random.nextDouble() * .5;
                final double y = Math.floor(entity.getY()) + .25d + random.nextDouble() * .5;
                final double z = Math.floor(entity.getZ()) + .25d + random.nextDouble() * .5;
                final double xSpeed = random.nextDouble() * .25 - 0.125;
                final double ySpeed = random.nextDouble() * .25 - 0.125;
                final double zSpeed = random.nextDouble() * .25 - 0.125;

                final ItemEntity newEntity = new ItemEntity(level, x, y, z, new ItemStack(recipe.output, recipe.count));
                newEntity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
                level.addFreshEntity(newEntity);

                return true;
            }
        }

        return false;
    }

    private static volatile Set<Item> transformableItemsCache = null;

    private static Set<Item> getTransformableItems(Level level) {
        Set<Item> ret = transformableItemsCache;

        if (ret == null) {
            ret = Collections.newSetFromMap(new IdentityHashMap<>());
            for (var recipe : level.getRecipeManager().getAllRecipesFor(TransformRecipe.TYPE)) {
                for (var ingredient : recipe.ingredients) {
                    for (var stack : ingredient.getItems()) {
                        ret.add(stack.getItem());
                    }
                    break; // only process first ingredient (they're all required anyway)
                }
            }
            transformableItemsCache = ret;
        }

        return ret;
    }

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> transformableItemsCache = null);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                transformableItemsCache = null;
            }
        });
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (client) {
                // a bit cheesy, but probably fine (technically we should be listening for recipes, not tags)
                // TODO: PR client recipe load callback to fabric
                transformableItemsCache = null;
            }
        });
    }

    private TransformLogic() {
    }
}
