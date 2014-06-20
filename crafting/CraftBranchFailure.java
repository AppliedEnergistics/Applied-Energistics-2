package appeng.crafting;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;

public class CraftBranchFailure extends Exception
{

	private static final long serialVersionUID = 654603652836724823L;

	IAEItemStack missing;

	public CraftBranchFailure(IAEItemStack what, long howMany) {
		super( "Failed: " + Platform.getItemDisplayName( what ) + " x " + howMany );
		missing = what.copy();
		missing.setStackSize( howMany );
	}
}
