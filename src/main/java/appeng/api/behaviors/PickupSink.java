package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface PickupSink {
    long insert(AEKey what, long amount, Actionable mode);
}
