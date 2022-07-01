package appeng.crafting.execution;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingSubmitResult;

public record CraftingSubmitResultImpl(boolean successful,
        @Nullable ICraftingLink link) implements ICraftingSubmitResult {
    public static final ICraftingSubmitResult FAILED = new CraftingSubmitResultImpl(false, null);
}
