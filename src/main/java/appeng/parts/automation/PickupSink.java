package appeng.parts.automation;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

interface PickupSink {
    long insert(AEKey what, long amount, Actionable mode);
}
