package appeng.integration.modules;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import rblocks.api.IOrientable;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IRB;

public class RB extends BaseModule implements IRB
{

	private class RBWrapper implements appeng.api.util.IOrientable
	{

		final private IOrientable internal;

		public RBWrapper(IOrientable ww) {
			internal = ww;
		}

		@Override
		public boolean canBeRotated()
		{
			return internal.canBeRotated();
		}

		@Override
		public ForgeDirection getForward()
		{
			return internal.getForward();
		}

		@Override
		public ForgeDirection getUp()
		{
			return internal.getUp();
		}

		@Override
		public void setOrientation(ForgeDirection Forward, ForgeDirection Up)
		{
			internal.setOrientation( Forward, Up );
		}

	}

	public static RB instance;

	@Override
	public void Init() throws Throwable
	{
		TestClass( IOrientable.class );
	}

	@Override
	public void PostInit() throws Throwable
	{

	}

	@Override
	public appeng.api.util.IOrientable getOrientable(TileEntity te)
	{
		if ( te instanceof IOrientable )
			return new RBWrapper( (IOrientable) te );
		return null;
	}

}
