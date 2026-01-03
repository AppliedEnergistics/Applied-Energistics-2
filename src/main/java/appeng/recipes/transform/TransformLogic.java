package appeng.recipes.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import appeng.recipes.AERecipeTypes;

public final class TransformLogic {
    public static boolean canTransformInFluid(ItemEntity entity, FluidState fluid) {
        return getTransformableItems(entity.level(), fluid.getType()).contains(entity.getItem().getItem());
    }

    public static boolean canTransformInAnyFluid(ItemEntity entity) {
        return getTransformableItemsAnyFluid(entity.level()).contains(entity.getItem().getItem());
    }

    public static boolean canTransformInExplosion(ItemEntity entity) {
        return getTransformableItemsExplosion(entity.level()).contains(entity.getItem().getItem());
    }

    public static boolean tryTransform(ItemEntity entity, Predicate<TransformCircumstance> circumstancePredicate) {
        var level = entity.level();

        var region = new AABB(entity.getX() - 1, entity.getY() - 1, entity.getZ() - 1, entity.getX() + 1,
                entity.getY() + 1, entity.getZ() + 1);
        List<ItemEntity> itemEntities = level.getEntities(null, region).stream()
                .filter(e -> e instanceof ItemEntity && !e.isRemoved()).map(e -> (ItemEntity) e).toList();

        for (var holder : level.getRecipeManager().byType(AERecipeTypes.TRANSFORM)) {
            var recipe = holder.value();
            if (!circumstancePredicate.test(recipe.circumstance))
                continue;

            if (recipe.ingredients.isEmpty())
                continue;

            List<Ingredient> missingIngredients = Lists.newArrayList(recipe.ingredients);
            Reference2IntMap<ItemEntity> consumedItems = new Reference2IntOpenHashMap<>(missingIngredients.size());

            if (recipe.circumstance.isExplosion()) {
                if (missingIngredients.stream().noneMatch(i -> i.test(entity.getItem())))
                    continue;
            } else {
                if (!recipe.catalyst.test(entity.getItem()))
                    continue;
            }

            for (var itemEntity : itemEntities) {
                var other = itemEntity.getItem();
                if (!other.isEmpty()) {
                    for (var it = missingIngredients.iterator(); it.hasNext();) {
                        Ingredient ing = it.next();
                        var alreadyClaimed = consumedItems.getInt(itemEntity);
                        if (ing.test(other) && other.getCount() - alreadyClaimed > 0) {
                            consumedItems.merge(itemEntity, 1, Integer::sum);
                            it.remove();
                        }
                    }
                }
            }

            if (missingIngredients.isEmpty()) {
                var items = new ArrayList<ItemStack>(consumedItems.size());
                for (var e : consumedItems.reference2IntEntrySet()) {
                    var itemEntity = e.getKey();
                    items.add(itemEntity.getItem().split(e.getIntValue()));

                    if (itemEntity.getItem().getCount() <= 0) {
                        itemEntity.discard();
                    }
                }
                var recipeInput = new TransformRecipeInput(items);
                var craftResult = recipe.assemble(recipeInput, level.registryAccess());

                var random = level.getRandom();
                final double x = Math.floor(entity.getX()) + .25d + random.nextDouble() * .5;
                final double y = Math.floor(entity.getY()) + .25d + random.nextDouble() * .5;
                final double z = Math.floor(entity.getZ()) + .25d + random.nextDouble() * .5;
                final double xSpeed = random.nextDouble() * .25 - 0.125;
                final double ySpeed = random.nextDouble() * .25 - 0.125;
                final double zSpeed = random.nextDouble() * .25 - 0.125;

                final ItemEntity newEntity = new ItemEntity(level, x, y, z, craftResult);

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
            for (var holder : level.getRecipeManager().getAllRecipesFor(AERecipeTypes.TRANSFORM)) {
                var recipe = holder.value();
                if (!(recipe.circumstance.isFluid(fluid)))
                    continue;
                for (var stack : recipe.catalyst.getItems()) {
                    ret.add(stack.getItem());
                }
            }
            return ret;
        });
    }

    private static Set<Item> getTransformableItemsAnyFluid(Level level) {
        Set<Item> ret = anyFluidCache;
        if (ret == null) {
            ret = Collections.newSetFromMap(new IdentityHashMap<>());
            for (var holder : level.getRecipeManager().getAllRecipesFor(AERecipeTypes.TRANSFORM)) {
                var recipe = holder.value();
                if (!recipe.circumstance.isFluid())
                    continue;
                for (var stack : recipe.catalyst.getItems()) {
                    ret.add(stack.getItem());
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
            for (var holder : level.getRecipeManager().getAllRecipesFor(AERecipeTypes.TRANSFORM)) {
                var recipe = holder.value();
                if (!recipe.circumstance.isExplosion())
                    continue;
                for (var ingredient : recipe.ingredients) {
                    for (var stack : ingredient.getItems()) {
                        ret.add(stack.getItem());
                    }
                    // ingredients that aren't processed may be destroyed in the explosion, so
                    // process all of them.
                }
            }
            explosionCache = ret;
        }
        return ret;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent e) {
        clearCache();
    }

    @SubscribeEvent
    public static void onReloadServerResources(AddReloadListenerEvent e) {
        clearCache();
    }

    @SubscribeEvent
    public static void onClientRecipesUpdated(RecipesUpdatedEvent e) {
        clearCache();
    }

    private TransformLogic() {
    }
}
