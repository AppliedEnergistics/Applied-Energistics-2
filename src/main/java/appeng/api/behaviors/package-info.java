/**
 * Classes to allow addons to define behavior of AE2's own devices when they have to interact with custom key types.
 *
 * <h3>Part implementations</h3>
 * <ul>
 * <li>Import bus: {@link appeng.api.behaviors.StackImportStrategy}.</li>
 * <li>Export bus: {@link appeng.api.behaviors.StackExportStrategy}.</li>
 * <li>Formation plane: {@link appeng.api.behaviors.PlacementStrategy}.</li>
 * <li>Annihilation plane: {@link appeng.api.behaviors.PickupStrategy}.</li>
 * </ul>
 *
 * <h3>Working with inventories</h3>
 * <ul>
 * <li>Building {@link appeng.api.storage.MEStorage}s from other kinds of inventories, used by the storage bus and the
 * pattern provider: {@link appeng.api.behaviors.ExternalStorageStrategy}.</li>
 * <li>Exposing AE2's generic inventories, such as the interface's or pattern provider's:
 * {@link appeng.api.behaviors.GenericInternalInventory}.</li>
 * <li>Defining the max capacity of interface and pattern provider slots:
 * {@link appeng.api.behaviors.GenericSlotCapacities}.</li>
 * </ul>
 *
 * @apiNote These classes are experimental: we might release breaking changes to them in any release.
 */
@ApiStatus.Experimental
package appeng.api.behaviors;

import org.jetbrains.annotations.ApiStatus;
