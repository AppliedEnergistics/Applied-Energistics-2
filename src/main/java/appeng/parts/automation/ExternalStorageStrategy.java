package appeng.parts.automation;

import javax.annotation.Nullable;

import appeng.api.storage.MEStorage;

public interface ExternalStorageStrategy {
    @Nullable
    MEStorage createWrapper(boolean extractableOnly, Runnable injectOrExtractCallback);
}
