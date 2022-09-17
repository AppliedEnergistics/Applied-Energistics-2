package appeng.api.networking.crafting;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Result of {@linkplain ICraftingService#submitJob submitting a crafting job}.
 */
@ApiStatus.NonExtendable
public interface ICraftingSubmitResult {
    default boolean successful() {
        return errorCode() == null;
    }

    /**
     * @return A not-null error code if the auto-crafting request was not submitted succesfully.
     */
    @Nullable
    CraftingSubmitErrorCode errorCode();

    /**
     * If {@link #errorCode()} is not-null, this may optionally give additional error details. Type depends on the error
     * code returned in {@link #errorCode()}.
     */
    @Nullable
    Object errorDetail();

    /**
     * The crafting link, only available for successful requests with a requester. Make sure to properly keep track of
     * this object, save it to NBT, load it from NBT, and make it available to the network via
     * {@link ICraftingRequester#getRequestedJobs()}.
     */
    @Nullable
    ICraftingLink link();
}
