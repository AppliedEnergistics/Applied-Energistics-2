package appeng.api.storage;

import appeng.api.stacks.AEKey;

@FunctionalInterface
public interface AEKeySlotFilter {
    boolean isAllowed(int slot, AEKey what);
}
