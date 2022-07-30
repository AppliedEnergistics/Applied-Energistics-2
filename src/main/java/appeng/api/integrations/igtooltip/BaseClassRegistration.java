package appeng.api.integrations.igtooltip;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.parts.IPartHost;
import appeng.me.helpers.IGridConnectedBlockEntity;

/**
 * Allows add-ons to notify AE2 of their {@link BlockEntity} classes that do derive from AE2 block entity classes. AE2
 * will try to add default tooltip providers for common AE2 API interfaces for these addon block entity classes.
 */
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface BaseClassRegistration {
    /**
     * Adds AE2s tooltip providers for the following interfaces to a given block entity/block and their subclasses.
     * <ul>
     * <li>{@link IGridConnectedBlockEntity}</li>
     * <li>{@link appeng.api.networking.energy.IAEPowerStorage}</li>
     * </ul>
     * <p/>
     * Please note that AE2 will already register these providers for its own block entity base class
     * (AEBaseBlockEntity). This method is only useful if you implement any of the interfaces listed above on your own
     * block entity class, which does not extend from an internal AE2 block entity base class.
     * <p/>
     * This method is needed because some tooltip mods only allow registering providers for subclasses of
     * {@link BlockEntity}, and not for arbitrary interfaces.
     *
     * @see TooltipProvider#registerBlockEntityBaseClasses
     */
    void addBaseBlockEntity(Class<? extends BlockEntity> blockEntityClass,
            Class<? extends Block> blockClass);

    /**
     * Adds AE2s part tooltip providers for third party {@link IPartHost} implementations.
     * <p/>
     * Please note that AE2 will already register these providers for its own part host (CableBusBlockEntity). This
     * method is only useful if your addon implements its own {@link IPartHost}, which does not extend from an internal
     * AE2 block entity base class.
     * <p/>
     * This method is needed because some tooltip mods only allow registering providers for subclasses of
     * {@link BlockEntity}, and not for arbitrary interfaces.
     *
     * @see TooltipProvider#registerBlockEntityBaseClasses
     */
    <T extends BlockEntity & IPartHost> void addPartHost(Class<T> blockEntityClass,
            Class<? extends Block> blockClass);
}
