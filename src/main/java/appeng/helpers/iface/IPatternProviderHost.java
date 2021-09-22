package appeng.helpers.iface;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.IPriorityHost;
import appeng.menu.implementations.PatternProviderMenu;

public interface IPatternProviderHost extends IConfigurableObject, IPriorityHost {
    DualityPatternProvider getDuality();

    /**
     * @return The block entity that is in-world and hosts the interface.
     */
    BlockEntity getBlockEntity();

    EnumSet<Direction> getTargets();

    void saveChanges();

    @Override
    default int getPriority() {
        return getDuality().getPriority();
    }

    @Override
    default void setPriority(int newValue) {
        getDuality().setPriority(newValue);
    }

    @Nonnull
    @Override
    default IConfigManager getConfigManager() {
        return getDuality().getConfigManager();
    }

    @Override
    default MenuType<?> getMenuType() {
        return PatternProviderMenu.TYPE;
    }
}
