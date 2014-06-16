package appeng.crafting;

import appeng.api.storage.data.IAEItemStack;

public class CraftBranchFailure extends Exception
{

	private static final long serialVersionUID = 654603652836724823L;

	IAEItemStack missing;

	public CraftBranchFailure(IAEItemStack what, long howMany) {
		super( "this should have been caught!" );
		missing = what.copy();
		missing.setStackSize( howMany );
	}
}
