
package appeng.core.network.serverbound;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.helpers.ICraftingGridMenu;
import appeng.items.storage.ViewCellItem;
import appeng.me.storage.NullInventory;
import appeng.util.CraftingRecipeUtil;
import appeng.util.prioritylist.IPartitionList;

/**
 * This packet will attempt to fill a crafting grid with real items based on a crafting recipe for the purposes of
 * crafting that item.
 *
 * @param recipeId            The Recipe ID to be crafted. This is optional since some recipes in JEI/REI are generated
 *                            on-the-fly and do not really exist.
 * @param ingredientTemplates The client sends us the intended items for the crafting based on the recipe shown to them.
 *                            This can be used in place of the actual ingredients in case the recipe is not resolvable.
 *                            <p/>
 *                            The size of this list *has* to match the size and layout of the crafting matrix used in
 *                            the menu we're trying to fill the matrix in. Usually it'll be 3x3.
 * @param craftMissing        True if missing entries should be queued for autocrafting instead.
 */
public record FillCraftingGridFromRecipePacket(
        @Nullable ResourceKey<Recipe<?>> recipeId,
        NonNullList<ItemStack> ingredientTemplates,
        boolean craftMissing) implements ServerboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, FillCraftingGridFromRecipePacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    FillCraftingGridFromRecipePacket::write,
                    FillCraftingGridFromRecipePacket::decode);

    public static final Type<FillCraftingGridFromRecipePacket> TYPE = CustomAppEngPayload
            .createType("fill_crafting_grid_from_recipe");

    @Override
    public Type<FillCraftingGridFromRecipePacket> type() {
        return TYPE;
    }

    public FillCraftingGridFromRecipePacket(@Nullable ResourceKey<Recipe<?>> recipeId,
            NonNullList<ItemStack> ingredientTemplates,
            boolean craftMissing) {
        this.recipeId = recipeId;
        this.ingredientTemplates = NonNullList.copyOf(ingredientTemplates.stream().map(ItemStack::copy).toList());
        this.craftMissing = craftMissing;
    }

    public static FillCraftingGridFromRecipePacket decode(RegistryFriendlyByteBuf stream) {
        ResourceKey<Recipe<?>> recipeId = null;
        if (stream.readBoolean()) {
            recipeId = stream.readResourceKey(Registries.RECIPE);
        }

        var ingredientTemplates = NonNullList.withSize(stream.readInt(), ItemStack.EMPTY);
        for (int i = 0; i < ingredientTemplates.size(); i++) {
            ingredientTemplates.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(stream));
        }
        var craftMissing = stream.readBoolean();

        return new FillCraftingGridFromRecipePacket(recipeId, ingredientTemplates, craftMissing);
    }

    public void write(RegistryFriendlyByteBuf data) {
        if (recipeId != null) {
            data.writeBoolean(true);
            data.writeResourceKey(recipeId);
        } else {
            data.writeBoolean(false);
        }
        data.writeInt(ingredientTemplates.size());
        for (var stack : ingredientTemplates) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(data, stack);
        }
        data.writeBoolean(craftMissing);
    }

    /**
     * Serverside handler for this packet.
     */
    @Override
    public void handleOnServer(ServerPlayer player) {
        // Setup and verification
        var menu = player.containerMenu;
        if (!(menu instanceof ICraftingGridMenu cct)) {
            // Server might have closed the menu before the client-packet is processed. This is not an error.
            return;
        }

        var energy = cct.getEnergySource();
        @Nullable
        ICraftingService craftingService;
        @Nullable
        IStorageService storageService;
        MEStorage networkStorage;
        KeyCounter cachedStorage;

        @Nullable
        var node = cct.getGridNode();
        if (node != null && cct.getLinkStatus().connected()) {
            craftingService = node.getGrid().getCraftingService();
            storageService = node.getGrid().getStorageService();
            networkStorage = storageService.getInventory();
            cachedStorage = storageService.getCachedInventory();
        } else {
            craftingService = null;
            storageService = null;
            networkStorage = NullInventory.of();
            cachedStorage = new KeyCounter();
        }

        var craftMatrix = cct.getCraftingMatrix();

        // We'll try to use the best possible ingredients based on what's available in the network

        var filter = ViewCellItem.createItemFilter(cct.getViewCells());
        var ingredients = getDesiredIngredients(player);

        // Prepare to autocraft some stuff
        var toAutoCraft = new LinkedHashMap<AEItemKey, IntList>();
        boolean touchedGridStorage = false;

        // Handle each slot
        for (var x = 0; x < craftMatrix.size(); x++) {
            var currentItem = craftMatrix.getStackInSlot(x);
            var ingredient = ingredients.get(x).orElse(null);

            // Move out items blocking the grid
            if (!currentItem.isEmpty()) {
                // Put away old item, if not correct
                if (ingredient != null && ingredient.test(currentItem)) {
                    // Grid already has an item that matches the ingredient
                    continue;
                } else {
                    var in = AEItemKey.of(currentItem);
                    var inserted = StorageHelper.poweredInsert(energy, networkStorage, in, currentItem.getCount(),
                            cct.getActionSource());
                    if (inserted > 0) {
                        touchedGridStorage = true;
                    }
                    if (inserted < currentItem.getCount()) {
                        currentItem = currentItem.copy();
                        currentItem.shrink((int) inserted);
                    } else {
                        currentItem = ItemStack.EMPTY;
                    }

                    // If more is remaining, try moving it to the player inventory
                    player.getInventory().add(currentItem);

                    craftMatrix.setItemDirect(x, currentItem.isEmpty() ? ItemStack.EMPTY : currentItem);
                }
            }

            if (ingredient == null) {
                continue;
            }

            // Try to find the best item for this slot. Sort by the amount available in the last tick,
            // then try to extract from most to least available item until 1 can be extracted.
            if (currentItem.isEmpty()) {
                var request = findBestMatchingItemStack(ingredient, filter, cachedStorage);
                for (var what : request) {
                    var extracted = StorageHelper.poweredExtraction(energy, networkStorage, what, 1,
                            cct.getActionSource());
                    if (extracted > 0) {
                        touchedGridStorage = true;
                        currentItem = what.toStack(Ints.saturatedCast(extracted));
                        break;
                    }
                }
            }

            // If still nothing, try taking it from the player inventory
            if (currentItem.isEmpty()) {
                currentItem = takeIngredientFromPlayer(cct, player, ingredient);
            }

            craftMatrix.setItemDirect(x, currentItem);

            // If we couldn't find the item, schedule its autocrafting
            if (currentItem.isEmpty() && craftMissing && craftingService != null) {
                int slot = x;
                findCraftableKey(ingredient, craftingService).ifPresent(key -> {
                    toAutoCraft.computeIfAbsent(key, k -> new IntArrayList()).add(slot);
                });
            }
        }

        menu.slotsChanged(craftMatrix.toContainer());

        if (!toAutoCraft.isEmpty()) {
            // Invalidate the grid storage cache if we modified it. The crafting plan will use
            // the outdated cached inventory otherwise.
            if (touchedGridStorage) {
                storageService.invalidateCache();
            }

            // This must be the last call since it changes the menu!
            var stacks = toAutoCraft.entrySet().stream()
                    .map(e -> new ICraftingGridMenu.AutoCraftEntry(e.getKey(), e.getValue())).toList();
            cct.startAutoCrafting(stacks);
        }
    }

    private ItemStack takeIngredientFromPlayer(ICraftingGridMenu cct, ServerPlayer player, Ingredient ingredient) {
        var playerInv = player.getInventory();
        for (int i = 0; i < playerInv.items.size(); i++) {
            // Do not take ingredients out of locked slots
            if (cct.isPlayerInventorySlotLocked(i)) {
                continue;
            }

            var item = playerInv.getItem(i);
            if (ingredient.test(item)) {
                var result = item.split(1);
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private NonNullList<Optional<Ingredient>> getDesiredIngredients(ServerPlayer player) {
        // Try to retrieve the real recipe on the server-side
        if (this.recipeId != null) {
            var recipe = player.serverLevel().recipeAccess().byKey(this.recipeId).orElse(null);
            if (recipe != null) {
                return CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe.value());
            }
        }

        // If the recipe is unavailable for any reason, use the templates provided by the client
        var ingredients = NonNullList.<Optional<Ingredient>>withSize(9, Optional.empty());
        Preconditions.checkArgument(ingredients.size() == this.ingredientTemplates.size(),
                "Got %d ingredient templates from client, expected %d",
                ingredientTemplates.size(), ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            var template = ingredientTemplates.get(i);
            if (!template.isEmpty()) {
                ingredients.set(i, Optional.of(Ingredient.of(template.getItem())));
            }
        }

        return ingredients;
    }

    /**
     * From a stream of AE item stacks, pick the one with the highest available amount in the network. Returns null if
     * the stream is empty.
     * <p/>
     * We normalize the stored amount vs. the amount needed for the recipe. While this is irrelevant for crafting
     * recipes, it can be relevant for processing patterns where an ingredient doesn't always have amount=1.
     *
     * <pre>
     * Example:
     * Recipe asks for 16xItem A or 1xItem B.
     * Storage has 16xItem A and 15xItem B.
     * Item B should have priority because the recipe could be crafted 15x, while with item A it could only be crafted
     * once.
     * </pre>
     */
    private List<AEItemKey> findBestMatchingItemStack(Ingredient ingredient, IPartitionList filter,
            KeyCounter storage) {
        if (!ingredient.isCustom()) {
            return ingredient.getValues().stream()
                    .map(Holder::value)
                    .map(AEItemKey::of)
                    .filter(r -> r != null && (filter == null || filter.isListed(r)))
                    .flatMap(s -> storage.findFuzzy(s, FuzzyMode.IGNORE_ALL).stream())
                    // While FuzzyMode.IGNORE_ALL will retrieve all stacks of the same Item which matches
                    // standard Vanilla Ingredient matching, there are NBT-matching Ingredient subclasses on Forge,
                    // and Mods might actually have mixed into Ingredient
                    .filter(e -> ((AEItemKey) e.getKey()).matches(ingredient))
                    // Sort in descending order of availability
                    .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))
                    .map(e -> (AEItemKey) e.getKey())
                    .toList();
        } else {
            return storage.keySet().stream()
                    .map(k -> {
                        return k instanceof AEItemKey itemKey ? itemKey : null;
                    })
                    .filter(r -> r != null && (filter == null || filter.isListed(r))
                            && ingredient.test(r.getReadOnlyStack()))
                    // Sort in descending order of availability
                    .sorted((a, b) -> Long.compare(storage.get(b), storage.get(a)))
                    .toList();
        }

    }

    private Optional<AEItemKey> findCraftableKey(Ingredient ingredient, ICraftingService craftingService) {
        return craftingService.getCraftables(AEItemKey.filter()).stream()
                .map(k -> (AEItemKey) k)
                .filter(k -> ingredient.test(k.getReadOnlyStack()))
                .findFirst();
    }
}
