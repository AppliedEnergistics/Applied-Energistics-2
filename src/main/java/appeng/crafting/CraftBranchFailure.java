package appeng.crafting;

import appeng.api.storage.data.IAEItemStack;

public class CraftBranchFailure extends Exception
{

	private static final long serialVersionUID = 654603652836724823L;

	final IAEItemStack missing;

	public CraftBranchFailure(IAEItemStack what, long howMany) {
		super( "Failed: " + what.getItem().getUnlocalizedName() + " x " + howMany );
		missing = what.copy();
		missing.setStackSize( howMany );
	}
}
