package appeng.core.features.registries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import appeng.api.exceptions.AppEngException;
import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.movable.IMovableTile;
import appeng.spatial.DefaultSpatialHandler;

public class MovableTileRegistry implements IMovableRegistry
{

	private HashSet<Block> blacklisted = new HashSet<Block>();

	private HashMap<Class<? extends TileEntity>, IMovableHandler> Valid = new HashMap<Class<? extends TileEntity>, IMovableHandler>();
	private LinkedList<Class<? extends TileEntity>> test = new LinkedList<Class<? extends TileEntity>>();
	private LinkedList<IMovableHandler> handlers = new LinkedList<IMovableHandler>();
	private DefaultSpatialHandler dsh = new DefaultSpatialHandler();

	private IMovableHandler nullHandler = new DefaultSpatialHandler();

	private IMovableHandler testClass(Class myClass, TileEntity te)
	{
		IMovableHandler handler = null;

		// ask handlers...
		for (IMovableHandler han : handlers)
		{
			if ( han.canHandle( myClass, te ) )
			{
				handler = han;
				break;
			}
		}

		// if you have a handler your opted in
		if ( handler != null )
		{
			Valid.put( myClass, handler );
			return handler;

		}

		// if your movable our opted in
		if ( te instanceof IMovableTile )
		{
			Valid.put( myClass, dsh );
			return dsh;
		}

		// if you are on the white list your opted in.
		for (Class<? extends TileEntity> testClass : test)
		{
			if ( testClass.isAssignableFrom( myClass ) )
			{
				Valid.put( myClass, dsh );
				return dsh;
			}
		}

		Valid.put( myClass, nullHandler );
		return nullHandler;
	}

	@Override
	public boolean askToMove(TileEntity te)
	{
		Class myClass = te.getClass();
		IMovableHandler canMove = Valid.get( myClass );

		if ( canMove == null )
			canMove = testClass( myClass, te );

		if ( canMove != nullHandler )
		{
			if ( te instanceof IMovableTile )
				((IMovableTile) te).prepareToMove();

			te.invalidate();
			return true;
		}

		return false;
	}

	@Override
	public void doneMoving(TileEntity te)
	{
		if ( te instanceof IMovableTile )
		{
			IMovableTile mt = (IMovableTile) te;
			mt.doneMoving();
		}
	}

	@Override
	public void whiteListTileEntity(Class<? extends TileEntity> c)
	{

		if ( c.getName().equals( TileEntity.class.getName() ) )
		{
			throw new RuntimeException( new AppEngException(
					"Someone tried to make all tiles movable, this is a clear violation of the purpose of the white list." ) );
		}

		test.add( c );
	}

	@Override
	public void addHandler(IMovableHandler han)
	{
		handlers.add( han );
	}

	@Override
	public IMovableHandler getHandler(TileEntity te)
	{
		Class myClass = te.getClass();
		IMovableHandler h = Valid.get( myClass );
		return h == null ? dsh : h;
	}

	@Override
	public IMovableHandler getDefaultHandler()
	{
		return dsh;
	}

	@Override
	public void blacklistBlock(Block blk)
	{
		blacklisted.add( blk );
	}

	@Override
	public boolean isBlacklisted(Block blk)
	{
		return blacklisted.contains( blk );
	}

}
