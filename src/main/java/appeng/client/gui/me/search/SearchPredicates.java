package appeng.client.gui.me.search;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.world.item.ItemStack;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListSearchManager;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.runtime.IJeiRuntime;

import appeng.core.AEConfig;
import appeng.integration.abstraction.IJEI;
import appeng.integration.abstraction.JEIFacade;
import appeng.integration.abstraction.REIFacade;
import appeng.integration.modules.jei.JeiRuntimeAdapter;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.Platform;

final class SearchPredicates {

    static Predicate<GridInventoryEntry> fromStringUsingJEI(String searchString, IJEI jei) {
        IJeiRuntime runtime = ((JeiRuntimeAdapter) jei).getRuntime();

        var filter = runtime.getIngredientFilter();

        var old = filter.getFilterText();
        filter.setFilterText(searchString);

        var filtered = new ArrayList<>();

        for (var type : runtime.getIngredientManager().getRegisteredIngredientTypes()) {
            if (!(type instanceof IIngredientTypeWithSubtypes subtype))
                continue;

            filtered.addAll(
                    filter.getFilteredIngredients(type)
                            .stream()
                            .map((e) -> subtype.getBase(e))
                            .toList());
        }

        if (!AEConfig.instance().isSyncWithExternalSearch())
            filter.setFilterText(old);

        return (entry) -> filtered.contains(entry.getWhat().getPrimaryKey());
    }

    static Predicate<GridInventoryEntry> fromStringUsingREI(String searchString) {
        var search = EntryListSearchManager.INSTANCE.getSearchManager();

        var old = search.filter.getFilter();

        search.markDirty();
        search.updateFilter(searchString);

        var filtered = search.getNow()
                .stream()
                .map(HashedEntryStackWrapper::unwrap)
                .map((e) -> {
                    if (e.getValue() instanceof ItemStack item) {
                        return (Object) item.getItem();
                    } else if (e.getValue() instanceof FluidStack fluid) {
                        return (Object) fluid.getFluid();
                    } else
                        return null;
                })
                .filter(Objects::nonNull)
                .toList();

        if (!AEConfig.instance().isSyncWithExternalSearch()) {
            search.markDirty();
            search.updateFilter(old);
        }

        return (entry) -> filtered.contains(entry.getWhat().getPrimaryKey());
    }

    static Predicate<GridInventoryEntry> fromString(String searchString, RepoSearch repoSearch) {
        if (searchString.isEmpty())
            return (e) -> true;

        if (AEConfig.instance().isUseExternalSearchEngine()) {
            IJEI jei = JEIFacade.instance();
            if (jei.isEnabled())
                return fromStringUsingJEI(searchString, jei);
            if (REIFacade.instance().isEnabled())
                return fromStringUsingREI(searchString);
        }

        if (searchString.startsWith("@")) {
            return createModIdPredicate(searchString.substring(1))
                    .or(createModNamePredicate(searchString.substring(1)));
        } else if (searchString.startsWith("*")) {
            return createIdPredicate(searchString.substring(1));
        } else if (searchString.startsWith("#")) {
            var pattern = createPattern(searchString.substring(1));
            return new TagPredicate(pattern);
        } else {
            var pattern = createPattern(searchString);

            if (AEConfig.instance().isSearchTooltips()) {
                // The tooltip obviously includes the display name too
                return createTooltipPredicate(pattern, repoSearch);
            } else {
                return createNamePredicate(pattern);
            }
        }
    }

    private static Predicate<GridInventoryEntry> createModIdPredicate(String searchText) {
        var searchPattern = createPattern(searchText);
        return entry -> {
            var what = Objects.requireNonNull(entry.getWhat());
            return searchPattern.matcher(what.getModId()).find();
        };
    }

    private static Predicate<GridInventoryEntry> createModNamePredicate(String searchText) {
        var searchPattern = createPattern(searchText);
        return entry -> {
            var what = Objects.requireNonNull(entry.getWhat());
            return searchPattern.matcher(Platform.getModName(what.getModId())).find();
        };
    }

    private static Predicate<GridInventoryEntry> createIdPredicate(String searchText) {
        var searchPattern = createPattern(searchText);
        return entry -> {
            var what = Objects.requireNonNull(entry.getWhat());
            return searchPattern.matcher(what.getId().toString()).find();
        };
    }

    private static Predicate<GridInventoryEntry> createNamePredicate(Pattern searchPattern) {
        return entry -> {
            var what = Objects.requireNonNull(entry.getWhat());
            String displayName = what.getDisplayName().getString();
            return searchPattern.matcher(displayName).find();
        };
    }

    private static Predicate<GridInventoryEntry> createTooltipPredicate(Pattern searchPattern, RepoSearch repoSearch) {
        return entry -> {
            var tooltipText = repoSearch.getTooltipText(entry.getWhat());
            return searchPattern.matcher(tooltipText).find();
        };
    }

    private static Pattern createPattern(String searchText) {
        try {
            return Pattern.compile(searchText.toLowerCase(),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException ignored) {
            return Pattern.compile(Pattern.quote(searchText.toLowerCase()),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        }
    }

}
