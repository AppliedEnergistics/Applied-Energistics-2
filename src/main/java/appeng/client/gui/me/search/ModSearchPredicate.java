package appeng.client.gui.me.search;

import java.util.Objects;
import java.util.function.Predicate;

import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.util.Platform;

final class ModSearchPredicate implements Predicate<GridInventoryEntry> {

    private final String inputModName;

    public ModSearchPredicate(String inputModName) {
        this.inputModName = standardify(inputModName);
    }

    @Override
    public boolean test(GridInventoryEntry gridInventoryEntry) {
        AEKey entryInfo = Objects.requireNonNull(gridInventoryEntry.getWhat());
        String modId  = entryInfo.getModId();


        if (modId != null) {
            if (modId.contains(inputModName)) {
                return true;
            }

            String modName = Platform.getModName(modId);
            modName = standardify(modName);
            return modName.contains(inputModName);
        }

        return false;
    }


    private String standardify(String input) {
        return input.toLowerCase().replace(" ", "");
    }
}
