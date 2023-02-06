package appeng.integration.modules.bogosorter;

import appeng.api.storage.data.IAEItemStack;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import net.minecraftforge.fml.common.Loader;

import java.util.Comparator;

public class InventoryBogoSortModule {
    private static final boolean loaded = Loader.isModLoaded("bogosorter");

    public static final Comparator<IAEItemStack> COMPARATOR = (o1, o2) -> SortHandler.getClientItemComparator().compare(o1.getDefinition(), o2.getDefinition());

    public static boolean isLoaded() {

        return loaded;
    }
}
