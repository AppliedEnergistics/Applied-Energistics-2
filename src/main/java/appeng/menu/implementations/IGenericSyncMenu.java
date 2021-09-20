package appeng.menu.implementations;

import java.util.Map;

import appeng.api.storage.data.IAEStack;

public interface IGenericSyncMenu {
    void receiveGenericStacks(Map<Integer, IAEStack> stacks);
}
