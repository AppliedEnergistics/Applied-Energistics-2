package appeng.crafting;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.storage.data.IAEItemStack;

public class CraftingLink implements ICraftingLink
{

	boolean canceled = false;
	boolean done = false;

	CraftingLinkNexus tie;

	final ICraftingRequester req;
	final ICraftingCPU cpu;

	final String CraftID;
	final boolean standalone;

	public CraftingLink(NBTTagCompound data, ICraftingRequester req) {
		CraftID = data.getString( "CraftID" );
		canceled = data.getBoolean( "canceled" );
		done = data.getBoolean( "done" );
		standalone = data.getBoolean( "standalone" );

		if ( !data.hasKey( "req" ) || data.getBoolean( "req" ) != true )
			throw new RuntimeException( "Invalid Crafting Link for Object" );

		this.req = req;
		cpu = null;
	}

	public CraftingLink(NBTTagCompound data, ICraftingCPU cpu) {
		CraftID = data.getString( "CraftID" );
		canceled = data.getBoolean( "canceled" );
		done = data.getBoolean( "done" );
		standalone = data.getBoolean( "standalone" );

		if ( !data.hasKey( "req" ) || data.getBoolean( "req" ) == true )
			throw new RuntimeException( "Invalid Crafting Link for Object" );

		this.cpu = cpu;
		req = null;
	}

	@Override
	public boolean isCanceled()
	{
		if ( canceled )
			return true;

		if ( done )
			return false;

		if ( tie == null )
			return false;

		return tie.isCanceled();
	}

	@Override
	public boolean isDone()
	{
		if ( done )
			return true;

		if ( canceled )
			return false;

		if ( tie == null )
			return false;

		return tie.isDone();
	}

	@Override
	public void cancel()
	{
		if ( done )
			return;

		canceled = true;

		if ( tie != null )
			tie.cancel();

		tie = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		tag.setString( "CraftID", CraftID );
		tag.setBoolean( "canceled", canceled );
		tag.setBoolean( "done", done );
		tag.setBoolean( "standalone", standalone );
		tag.setBoolean( "req", req != null );
	}

	public void setNextus(CraftingLinkNexus n)
	{
		if ( tie != null )
			tie.remove( this );

		if ( canceled && n != null )
		{
			n.cancel();
			tie = null;
			return;
		}

		tie = n;

		if ( n != null )
			n.add( this );
	}

	@Override
	public String getCraftingID()
	{
		return CraftID;
	}

	@Override
	public boolean isStandalone()
	{
		return standalone;
	}

	public IAEItemStack injectItems(IAEItemStack input)
	{
		if ( tie == null || tie.req == null || tie.req.req == null )
			return input;

		return tie.req.req.injectCratedItems( tie.req, input );
	}

	public void markDone()
	{
		if ( tie != null )
			tie.markDone();
	}
}
