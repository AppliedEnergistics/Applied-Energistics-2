package appeng.fmp;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.facade.FacadeContainer;
import appeng.util.Platform;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class FMPPlacementHelper implements IPartHost
{

	private boolean hasPart = false;
	private TileMultipart myMP;
	private CableBusPart myPart;

	private CableBusPart getPart()
	{
		if ( myPart == null )
			myPart = (CableBusPart) PartRegistry.CableBusPart.construct( 0 );

		BlockCoord loc = new BlockCoord( myMP.xCoord, myMP.yCoord, myMP.zCoord );

		if ( myMP.canAddPart( myPart ) && Platform.isServer() )
		{
			TileMultipart.addPart( myMP.getWorldObj(), loc, myPart );
			hasPart = true;
		}

		return myPart;
	}

	public void removePart()
	{
		if ( myPart.isEmpty() )
		{
			scala.collection.Iterator<TMultiPart> i = myMP.partList().iterator();
			while (i.hasNext())
			{
				TMultiPart p = i.next();
				if ( p == myPart )
				{
					myMP.remPart( myPart );
					break;
				}
			}
			hasPart = false;
			myPart = null;
		}
	}

	public FMPPlacementHelper(TileMultipart mp) {
		myMP = mp;
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		if ( myPart == null )
			return new FacadeContainer();
		return myPart.getFacadeContainer();
	}

	@Override
	public boolean canAddPart(ItemStack part, ForgeDirection side)
	{
		CableBusPart myPart = getPart();

		boolean returnValue = hasPart && myPart.canAddPart( part, side );

		removePart();

		return returnValue;
	}

	@Override
	public ForgeDirection addPart(ItemStack is, ForgeDirection side, EntityPlayer owner)
	{
		CableBusPart myPart = getPart();

		ForgeDirection returnValue = hasPart ? myPart.addPart( is, side, owner ) : null;

		removePart();

		return returnValue;
	}

	@Override
	public IPart getPart(ForgeDirection side)
	{
		if ( myPart == null )
			return null;
		return myPart.getPart( side );
	}

	@Override
	public void removePart(ForgeDirection side, boolean suppressUpdate)
	{
		if ( myPart == null )
			return;
		myPart.removePart( side, suppressUpdate );
	}

	@Override
	public void markForUpdate()
	{
		if ( myPart == null )
			return;
		myPart.markForUpdate();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		if ( myPart == null )
			return new DimensionalCoord( myMP );
		return myPart.getLocation();
	}

	@Override
	public TileEntity getTile()
	{
		return myMP;
	}

	@Override
	public AEColor getColor()
	{
		if ( myPart == null )
			return AEColor.Transparent;
		return myPart.getColor();
	}

	@Override
	public void clearContainer()
	{
		if ( myPart == null )
			return;
		myPart.clearContainer();
	}

	@Override
	public boolean isBlocked(ForgeDirection side)
	{
		getPart();

		boolean returnValue = myPart.isBlocked( side );

		removePart();

		return returnValue;
	}

	@Override
	public SelectedPart selectPart(Vec3 pos)
	{
		if ( myPart == null )
			return new SelectedPart();
		return myPart.selectPart( pos );
	}

	@Override
	public void markForSave()
	{
		if ( myPart == null )
			return;
		myPart.markForSave();
	}

	@Override
	public void partChanged()
	{
		if ( myPart == null )
			return;
		myPart.partChanged();
	}

	@Override
	public boolean hasRedstone(ForgeDirection side)
	{
		if ( myPart == null )
			return false;
		return myPart.hasRedstone( side );
	}

	@Override
	public boolean isEmpty()
	{
		if ( myPart == null )
			return true;
		return myPart.isEmpty();
	}

	@Override
	public Set<LayerFlags> getLayerFlags()
	{
		if ( myPart == null )
			return EnumSet.noneOf( LayerFlags.class );
		return myPart.getLayerFlags();
	}

	@Override
	public void cleanup()
	{
		if ( myPart == null )
			return;
		myPart.cleanup();
	}

	@Override
	public void notifyNeighbors()
	{
		if ( myPart == null )
			return;
		myPart.notifyNeighbors();
	}

	@Override
	public boolean isInWorld()
	{
		if ( myPart == null )
			return myMP.getWorldObj() != null;
		return myPart.isInWorld();
	}

}
