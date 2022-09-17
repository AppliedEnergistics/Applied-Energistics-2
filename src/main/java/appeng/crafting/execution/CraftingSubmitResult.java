package appeng.crafting.execution;

import org.jetbrains.annotations.Nullable;

import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.crafting.UnsuitableCpus;
import appeng.api.stacks.GenericStack;

public record CraftingSubmitResult(@Nullable ICraftingLink link,
        @Nullable CraftingSubmitErrorCode errorCode,
        @Nullable Object errorDetail) implements ICraftingSubmitResult {

    public static ICraftingSubmitResult successful(@Nullable ICraftingLink link) {
        return new CraftingSubmitResult(link, null, null);
    }

    public static final ICraftingSubmitResult NO_CPU_FOUND = simpleError(CraftingSubmitErrorCode.NO_CPU_FOUND);
    public static final ICraftingSubmitResult INCOMPLETE_PLAN = simpleError(CraftingSubmitErrorCode.INCOMPLETE_PLAN);
    public static final ICraftingSubmitResult CPU_BUSY = simpleError(CraftingSubmitErrorCode.CPU_BUSY);

    public static final ICraftingSubmitResult CPU_OFFLINE = simpleError(CraftingSubmitErrorCode.CPU_OFFLINE);

    public static final ICraftingSubmitResult CPU_TOO_SMALL = simpleError(CraftingSubmitErrorCode.CPU_TOO_SMALL);

    public static ICraftingSubmitResult simpleError(CraftingSubmitErrorCode code) {
        return new CraftingSubmitResult(null, code, null);
    }

    public static ICraftingSubmitResult missingIngredient(GenericStack missingIngredient) {
        return new CraftingSubmitResult(null, CraftingSubmitErrorCode.MISSING_INGREDIENT, missingIngredient);
    }

    public static ICraftingSubmitResult noSuitableCpu(UnsuitableCpus unsuitableCpus) {
        return new CraftingSubmitResult(null, CraftingSubmitErrorCode.NO_SUITABLE_CPU_FOUND, unsuitableCpus);
    }
}
