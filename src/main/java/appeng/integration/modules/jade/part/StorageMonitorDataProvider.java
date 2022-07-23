package appeng.integration.modules.jade.part;

import net.minecraft.nbt.CompoundTag;

import snownee.jade.api.ITooltip;

import appeng.api.parts.IPart;
import appeng.core.localization.InGameTooltip;
import appeng.parts.reporting.AbstractMonitorPart;

/**
 * Displays the stack if present and if the monitor is locked.
 */
public final class StorageMonitorDataProvider implements IPartDataProvider {

    @Override
    public void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
        if (part instanceof AbstractMonitorPart monitor) {
            var displayed = monitor.getDisplayed();
            var isLocked = monitor.isLocked();

            if (displayed != null) {
                tooltip.add(InGameTooltip.Showing.text().append(": ")
                        .append(displayed.getDisplayName()));
            }

            tooltip.add(isLocked ? InGameTooltip.Locked.text() : InGameTooltip.Unlocked.text());
        }
    }

}
