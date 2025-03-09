package appeng.recipes.transform;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import appeng.core.AppEng;
import appeng.recipes.AERecipeTypes;

public final class TransformLogic {
    private static final Logger LOG = LoggerFactory.getLogger(TransformLogic.class);

    public static boolean canTransformInFluid(ItemEntity entity, FluidState fluid) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            return getTransformableItems(serverLevel, fluid.getType()).contains(entity.getItem().getItemHolder());
        }
        return false;
    }

    public static boolean canTransformInAnyFluid(ItemEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            return getTransformableItemsAnyFluid(serverLevel).contains(entity.getItem().getItemHolder());
        }
        return false;
    }

    public static boolean canTransformInExplosion(ItemEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            return getTransformableItemsExplosion(serverLevel).contains(entity.getItem().getItemHolder());
        }
        return false;
    }

    public static boolean tryTransform(ItemEntity entity, Predicate<TransformCircumstance> circumstancePredicate) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }

        var region = new AABB(entity.getX() - 1, entity.getY() - 1, entity.getZ() - 1, entity.getX() + 1,
                entity.getY() + 1, entity.getZ() + 1);
        List<ItemEntity> itemEntities = level.getEntities(null, region).stream()
                .filter(e -> e instanceof ItemEntity && !e.isRemoved()).map(e -> (ItemEntity) e).toList();

        for (var holder : level.recipeAccess().recipeMap().byType(AERecipeTypes.TRANSFORM)) {
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
                if (!missingIngredients.getFirst().test(entity.getItem()))
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
    static Map<Fluid, HolderSet<Item>> fluidCache = new IdentityHashMap<>();
    static HolderSet<Item> explosionCache = null;
    static HolderSet<Item> anyFluidCache = null;

    private static void clearCache() {
        fluidCache.clear();
        explosionCache = null;
        anyFluidCache = null;
    }

    private static HolderSet<Item> getTransformableItems(ServerLevel level, Fluid fluid) {
        return fluidCache.computeIfAbsent(fluid, f -> {
            List<HolderSet<Item>> holderSets = new ArrayList<>();
            for (var holder : level.recipeAccess().recipeMap().byType(AERecipeTypes.TRANSFORM)) {
                var recipe = holder.value();
                if (!(recipe.circumstance.isFluid(fluid)))
                    continue;
                for (var ingredient : recipe.ingredients) {
                    holderSets.add(ingredient.getValues());
                    break; // only process first ingredient (they're all required anyway)
                }
            }
            if (holderSets.size() == 1) {
                return holderSets.getFirst();
            }
            return new OrHolderSet<>(holderSets);
        });
    }

    private static HolderSet<Item> getTransformableItemsAnyFluid(ServerLevel level) {
        HolderSet<Item> ret = anyFluidCache;
        if (ret == null) {
            List<HolderSet<Item>> holderSets = new ArrayList<>();
            for (var holder : level.recipeAccess().recipeMap().byType(AERecipeTypes.TRANSFORM)) {
                var recipe = holder.value();
                if (!recipe.circumstance.isFluid())
                    continue;
                for (var ingredient : recipe.ingredients) {
                    holderSets.add(ingredient.getValues());
                    break; // only process first ingredient (they're all required anyway)
                }
            }
            if (holderSets.size() == 1) {
                ret = holderSets.getFirst();
            } else {
                ret = new OrHolderSet<>(holderSets);
            }
            anyFluidCache = ret;
        }
        return ret;
    }

    private static HolderSet<Item> getTransformableItemsExplosion(ServerLevel level) {
        HolderSet<Item> ret = explosionCache;
        if (ret == null) {
            List<HolderSet<Item>> holderSets = new ArrayList<>();
            for (var holder : level.recipeAccess().recipeMap().byType(AERecipeTypes.TRANSFORM)) {
                var recipe = holder.value();
                if (!recipe.circumstance.isExplosion())
                    continue;
                for (var ingredient : recipe.ingredients) {
                    if (!ingredient.isCustom()) {
                        holderSets.add(ingredient.getValues());
                    } else {
                        LOG.warn("Custom ingredient {} does not work in explosion transform recipe {}", ingredient,
                                holder.id());
                    }
                    // ingredients that aren't processed may be destroyed in the explosion, so process all of them.
                }
            }
            if (holderSets.size() == 1) {
                ret = holderSets.getFirst();
            } else {
                ret = new OrHolderSet<>(holderSets);
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
    public static void onReloadServerResources(AddServerReloadListenersEvent e) {
        e.addListener(AppEng.makeId("transform_logic_cache_invalidation"), new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                clearCache();
            }
        });
    }

    private TransformLogic() {
    }
}
