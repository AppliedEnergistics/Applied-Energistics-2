package appeng.crafting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.hooks.TickHandler;

import com.google.common.base.Stopwatch;

public class CraftingJob implements Runnable, ICraftingJob
{

	IAEItemStack output;

	IItemList<IAEItemStack> storage;

	HashSet<IAEItemStack> prophecies;

	boolean simulate = false;
	final MECraftingInventory original;

	MECraftingInventory availableCheck;
	public CraftingTreeNode tree;
	private BaseActionSource actionSrc;
	private ICraftingCallback callback;

	long bytes = 0;
	World world;

	public IAEItemStack getOutput()
	{
		return output;
	}

	public CraftingJob(World w, NBTTagCompound data)
	{
		world = wrapWorld( w );
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();
		original = null;
		availableCheck = null;
	}

	public void refund(IAEItemStack o)
	{
		availableCheck.injectItems( o, Actionable.MODULATE, this.actionSrc );
	}

	public IAEItemStack checkUse(IAEItemStack available)
	{
		return availableCheck.extractItems( available, Actionable.MODULATE, this.actionSrc );
	}

	public CraftingJob(World w, IGrid grid, BaseActionSource actionSrc, IAEItemStack what, ICraftingCallback callback)
	{
		world = wrapWorld( w );
		output = what.copy();
		storage = AEApi.instance().storage().createItemList();
		prophecies = new HashSet();
		this.actionSrc = actionSrc;

		this.callback = callback;
		ICraftingGrid cc = grid.getCache( ICraftingGrid.class );
		IStorageGrid sg = grid.getCache( IStorageGrid.class );
		original = new MECraftingInventory( sg.getItemInventory(), false, false, false );
		original.filterPermissions( actionSrc );

		tree = getCraftingTree( cc, what );
		availableCheck = null;
	}

	private World wrapWorld(World w)
	{
		return w;
	}

	private CraftingTreeNode getCraftingTree(ICraftingGrid cc, IAEItemStack what)
	{
		return new CraftingTreeNode( cc, this, what, null, -1, 0 );
	}

	public long getByteTotal()
	{
		return bytes;
	}

	public void writeToNBT(NBTTagCompound out)
	{

	}

	IItemList<IAEItemStack> crafting = AEApi.instance().storage().createItemList();
	IItemList<IAEItemStack> missing = AEApi.instance().storage().createItemList();

	public void addTask(IAEItemStack what, long crafts, ICraftingPatternDetails details, int depth)
	{
		if ( crafts > 0 )
		{
			what = what.copy();
			what.setStackSize( what.getStackSize() * crafts );
			crafting.add( what );
		}
	}

	public void addMissing(IAEItemStack what)
	{
		what = what.copy();
		missing.add( what );
	}

	class twoIntegers
	{

		public long perOp = 0;
		public long times = 0;
	};

	HashMap<String, twoIntegers> opsAndMultiplier = new HashMap();

	@Override
	public void run()
	{
		try
		{
			try
			{
				TickHandler.instance.registerCraftingSimulation( world, this );
				handlepausing();

				Stopwatch timer = Stopwatch.createStarted();

				MECraftingInventory meci = new MECraftingInventory( original, true, false, true );
				meci.ignore( output );

				availableCheck = new MECraftingInventory( original, false, false, false );
				tree.request( meci, output.getStackSize(), actionSrc );
				tree.dive( this );

				for (String s : opsAndMultiplier.keySet())
				{
					twoIntegers ti = opsAndMultiplier.get( s );
					AELog.crafting( s + " * " + ti.times + " = " + (ti.perOp * ti.times) );
				}

				AELog.crafting( "------------- " + getByteTotal() + "b real" + timer.elapsed( TimeUnit.MILLISECONDS ) + "ms" );
				// if ( mode == Actionable.MODULATE )
				// meci.moveItemsToStorage( storage );
			}
			catch (CraftBranchFailure e)
			{
				simulate = true;

				try
				{
					Stopwatch timer = Stopwatch.createStarted();
					MECraftingInventory meci = new MECraftingInventory( original, true, false, true );
					meci.ignore( output );

					availableCheck = new MECraftingInventory( original, false, false, false );

					tree.setSimulate();
					tree.request( meci, output.getStackSize(), actionSrc );
					tree.dive( this );

					for (String s : opsAndMultiplier.keySet())
					{
						twoIntegers ti = opsAndMultiplier.get( s );
						AELog.crafting( s + " * " + ti.times + " = " + (ti.perOp * ti.times) );
					}

					AELog.crafting( "------------- " + getByteTotal() + "b simulate" + timer.elapsed( TimeUnit.MILLISECONDS ) + "ms" );
				}
				catch (CraftBranchFailure e1)
				{
					AELog.error( e1 );
				}
				catch (CraftingCalculationFailure f)
				{
					AELog.error( f );
				}
				catch (InterruptedException e1)
				{
					AELog.crafting( "Crafting calculation canceled." );
					finish();
					return;
				}
			}
			catch (CraftingCalculationFailure f)
			{
				AELog.error( f );
			}
			catch (InterruptedException e1)
			{
				AELog.crafting( "Crafting calculation canceled." );
				finish();
				return;
			}

			log( "crafting job now done" );
		}
		catch (Throwable t)
		{
			finish();
			throw new RuntimeException( t );
		}

		finish();

	}

	public void finish()
	{
		if ( callback != null )
			callback.calculationComplete( this );

		availableCheck = null;

		synchronized (monitor)
		{
			running = false;
			done = true;
			monitor.notify();
		}
	}

	public boolean isSimulation()
	{
		return simulate;
	}

	public boolean isDone()
	{
		return done;
	}

	public World getWorld()
	{
		return world;
	}

	private boolean running = false;
	private boolean done = false;
	private Object monitor = new Object();
	private Stopwatch watch = Stopwatch.createUnstarted();
	private int time = 5;

	/**
	 * returns true if this needs more simulation.
	 * 
	 * @param milli
	 * @return
	 */
	public boolean simulateFor(int milli)
	{
		time = milli;

		synchronized (monitor)
		{
			if ( isDone() )
				return false;

			watch.reset();
			watch.start();
			running = true;

			log( "main thread is now going to sleep" );

			monitor.notify();

			while (running)
			{
				try
				{
					monitor.wait();
				}
				catch (InterruptedException e)
				{
				}
			}

			log( "main thread is now active" );
		}

		return true;
	}

	private int incTime = Integer.MAX_VALUE;

	public void handlepausing() throws InterruptedException
	{
		if ( incTime++ > 100 )
		{
			incTime = 0;

			synchronized (monitor)
			{
				if ( watch.elapsed( TimeUnit.MICROSECONDS ) > time )
				{
					running = false;
					watch.stop();
					monitor.notify();
				}

				if ( !running )
				{
					log( "crafting job will now sleep" );

					while (!running)
					{
						monitor.wait();
					}

					log( "crafting job now active" );
				}
			}

			if ( Thread.interrupted() )
				throw new InterruptedException();
		}
	}

	private void log(String string)
	{
		// AELog.crafting( string );
	}

	public void addBytes(long crafts)
	{
		bytes += crafts;
	}

	@Override
	public void populatePlan(IItemList<IAEItemStack> plan)
	{
		if ( tree != null )
			tree.getPlan( plan );
	}

}
