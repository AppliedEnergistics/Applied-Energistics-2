package appeng.recipes.transform;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public final class TransformLogic {
    public static boolean canTransformInFluid(ItemEntity entity, FluidState fluid) {
        return getTransformableItems(entity.getLevel(), fluid.getType()).contains(entity.getItem().getItem());
    }

    public static boolean canTransformInAnyFluid(ItemEntity entity) {
        return getTransformableItemsAnyFluid(entity.getLevel()).contains(entity.getItem().getItem());
    }

    public static boolean canTransformInExplosion(ItemEntity entity) {
        return getTransformableItemsExplosion(entity.getLevel()).contains(entity.getItem().getItem());
    }

    public static boolean tryTransform(ItemEntity entity, Predicate<TransformCircumstance> circumstancePredicate) {
        var level = entity.level;

        var region = new AABB(entity.getX() - 1, entity.getY() - 1, entity.getZ() - 1, entity.getX() + 1,
                entity.getY() + 1, entity.getZ() + 1);
        List<ItemEntity> itemEntities = level.getEntities(null, region).stream()
                .filter(e -> e instanceof ItemEntity && !e.isRemoved()).map(e -> (ItemEntity) e).toList();

        for (var recipe : level.getRecipeManager().byType(TransformRecipe.TYPE).values()) {
            if (!circumstancePredicate.test(recipe.circumstance))
                continue;

            if (recipe.ingredients.size() == 0)
                continue;

            List<Ingredient> missingIngredients = Lists.newArrayList(recipe.ingredients);
            Set<ItemEntity> selectedEntities = new ReferenceOpenHashSet<>(missingIngredients.size());

            if (recipe.circumstance.isExplosion()) {
                if (missingIngredients.stream().noneMatch(i -> i.test(entity.getItem())))
                    continue;
            } else {
                if (!missingIngredients.get(0).test(entity.getItem()))
                    continue;
            }

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
                SimpleContainer recipeContainer = new SimpleContainer(selectedEntities.size());
                int i = 0;
                for (var e : selectedEntities) {
                    recipeContainer.setItem(i++, e.getItem().split(1));

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

                final ItemEntity newEntity = new ItemEntity(level, x, y, z, recipe.assemble(recipeContainer));

                newEntity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
                level.addFreshEntity(newEntity);

                return true;
            }
        }

        return false;
    }

    // not using a Multimap here because we need to cache the empty set
    static Map<Fluid, Set<Item>> fluidCache = new IdentityHashMap<>();
    static Set<Item> explosionCache = null;
    static Set<Item> anyFluidCache = null;

    private static void clearCache() {
        fluidCache.clear();
        explosionCache = null;
        anyFluidCache = null;
    }

    private static Set<Item> getTransformableItems(Level level, Fluid fluid) {
        return fluidCache.computeIfAbsent(fluid, f -> {
            Set<Item> ret = Collections.newSetFromMap(new IdentityHashMap<>());
            for (var recipe : level.getRecipeManager().getAllRecipesFor(TransformRecipe.TYPE)) {
                if (!(recipe.circumstance.isFluid(fluid)))
                    continue;
                for (var ingredient : recipe.ingredients) {
                    for (var stack : ingredient.getItems()) {
                        ret.add(stack.getItem());
                    }
                    break; // only process first ingredient (they're all required anyway)
                }
            }
            return ret;
        });
    }

    private static Set<Item> getTransformableItemsAnyFluid(Level level) {
        Set<Item> ret = anyFluidCache;
        if (ret == null) {
            ret = Collections.newSetFromMap(new IdentityHashMap<>());
            for (var recipe : level.getRecipeManager().getAllRecipesFor(TransformRecipe.TYPE)) {
                if (!recipe.circumstance.isFluid())
                    continue;
                for (var ingredient : recipe.ingredients) {
                    for (var stack : ingredient.getItems()) {
                        ret.add(stack.getItem());
                    }
                    break; // only process first ingredient (they're all required anyway)
                }
            }
            anyFluidCache = ret;
        }
        return ret;
    }

    private static Set<Item> getTransformableItemsExplosion(Level level) {
        Set<Item> ret = explosionCache;
        if (ret == null) {
            ret = Collections.newSetFromMap(new IdentityHashMap<>());
            for (var recipe : level.getRecipeManager().getAllRecipesFor(TransformRecipe.TYPE)) {
                if (!recipe.circumstance.isExplosion())
                    continue;
                for (var ingredient : recipe.ingredients) {
                    for (var stack : ingredient.getItems()) {
                        ret.add(stack.getItem());
                    }
                    // ingredients that aren't processed may be destroyed in the explosion, so process all of them.
                }
            }
            explosionCache = ret;
        }
        return ret;
    }

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> clearCache());
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success)
                clearCache();
        });
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (client) {
                // a bit cheesy, but probably fine (technically we should be listening for recipes, not tags)
                // TODO: PR client recipe load callback to fabric
                clearCache();
            }
        });
    }

    private TransformLogic() {
    }
}
