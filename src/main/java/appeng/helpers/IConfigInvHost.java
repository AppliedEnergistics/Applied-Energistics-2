package appeng.helpers;

import appeng.helpers.externalstorage.GenericStackInv;

/**
 * Used by the memory card to export/import the config inventory.
 */
public interface IConfigInvHost {
    GenericStackInv getConfig();
}
