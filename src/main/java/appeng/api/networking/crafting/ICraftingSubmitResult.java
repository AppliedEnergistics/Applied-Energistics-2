package appeng.api.networking.crafting;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Result of {@linkplain ICraftingService#submitJob submitting a crafting job}.
 */
@ApiStatus.NonExtendable
public interface ICraftingSubmitResult {
    boolean successful();

    /**
     * The crafting link, only available for successful requests with a requester. Make sure to properly keep track of
     * this object, save it to NBT, load it from NBT, and make it available to the network via
     * {@link ICraftingRequester#getRequestedJobs()}.
     */
    @Nullable
    ICraftingLink link();
}
