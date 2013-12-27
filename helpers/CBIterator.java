package appeng.helpers;

import java.util.Iterator;

import net.minecraftforge.common.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.parts.CableBusContainer;

public class CBIterator implements Iterator<IPart>
{

	int side = 0;
	CableBusContainer container;

	public CBIterator(CableBusContainer cableBusContainer) {
		container = cableBusContainer;
	}

	private void scan()
	{
		while (container.getPart( ForgeDirection.getOrientation( side ) ) == null)
		{
			side++;
			if ( side > 6 )
				return;
		}
	}

	@Override
	public boolean hasNext()
	{
		scan();
		if ( side <= 6 )
			return true;
		return false;
	}

	@Override
	public IPart next()
	{
		scan();
		if ( side <= 6 )
		{
			side++;
			return container.getPart( ForgeDirection.getOrientation( side ) );
		}
		return null;
	}

	@Override
	public void remove()
	{
		container.removePart( ForgeDirection.getOrientation( side ) );
	}

}
