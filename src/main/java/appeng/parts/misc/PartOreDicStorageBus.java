package appeng.parts.misc;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.GuiBridge;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import appeng.util.item.OreHelper;
import appeng.util.prioritylist.OreDictPriorityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;


public class PartOreDicStorageBus extends PartStorageBus
{
    public String oreMatch;
    OreDictPriorityList<IAEItemStack> priorityList;

    public PartOreDicStorageBus( ItemStack is )
    {
        super( is );
    }

    @Override
    public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
    {
        if( Platform.isServer() )
        {
            Platform.openGUI( player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_OREDICTSTORAGEBUS );
        }
        return true;
    }

    @Override
    public GuiBridge getGuiBridge()
    {
        return GuiBridge.GUI_OREDICTSTORAGEBUS;
    }

    @Override
    public MEInventoryHandler<IAEItemStack> getInternalHandler()
    {
        if( this.cached )
        {
            return this.handler;
        }

        final boolean wasSleeping = this.monitor == null;

        this.cached = true;
        final TileEntity self = this.getHost().getTile();
        final TileEntity target = self.getWorld().getTileEntity( self.getPos().offset( this.getSide().getFacing() ) );
        final int newHandlerHash = this.createHandlerHash( target );

        if( newHandlerHash != 0 && newHandlerHash == this.handlerHash )
        {
            return this.handler;
        }

        this.handlerHash = newHandlerHash;
        this.handler = null;
        if( this.monitor != null )
        {
            ( (IBaseMonitor<IAEItemStack>) monitor ).removeListener( this );
        }
        this.monitor = null;
        if( target != null )
        {
            IMEInventory<IAEItemStack> inv = this.getInventoryWrapper( target );

            if( inv instanceof ITickingMonitor )
            {
                this.monitor = (ITickingMonitor) inv;
                this.monitor.setActionSource( mySrc );
                this.monitor.setMode( (StorageFilter) this.getConfigManager().getSetting( Settings.STORAGE_FILTER ) );
            }

            if( inv != null )
            {
                this.handler = new MEInventoryHandler<>( inv, AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) );

                this.handler.setBaseAccess( (AccessRestriction) this.getConfigManager().getSetting( Settings.ACCESS ) );
                this.handler.setWhitelist( this.getInstalledUpgrades( Upgrades.INVERTER ) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST );
                this.handler.setPriority( this.priority );

                this.handler.setPartitionList( priorityList );

                if( inv instanceof IBaseMonitor )
                {
                    if( ( (AccessRestriction) ( (ConfigManager) this.getConfigManager() ).getSetting( Settings.ACCESS ) ).hasPermission( AccessRestriction.READ ) )
                    {
                        ( (IBaseMonitor<IAEItemStack>) inv ).addListener( this, this.handler );
                    }
                }
            }
        }

        // update sleep state...
        if( wasSleeping != ( this.monitor == null ) )
        {
            try
            {
                final ITickManager tm = this.getProxy().getTick();
                if( this.monitor == null )
                {
                    tm.sleepDevice( this.getProxy().getNode() );
                }
                else
                {
                    tm.wakeDevice( this.getProxy().getNode() );
                }
            }
            catch( final GridAccessException e )
            {
                // :(
            }
        }

        try
        {
            // force grid to update handlers...
            ( (GridStorageCache) this.getProxy().getGrid().getCache( IStorageGrid.class ) ).cellUpdate( null );
        }
        catch( final GridAccessException e )
        {
            // :3
        }

        return this.handler;
    }

    public String getOreMatch()
    {
        if( this.oreMatch == null )
        {
            return "";
        }
        return oreMatch;
    }

    public void saveOreMatch( String oreMatch )
    {
        if( !this.oreMatch.equals( oreMatch ) )
        {
            this.oreMatch = oreMatch;
            this.priorityList = new OreDictPriorityList<>( OreHelper.INSTANCE.getMatchingOre( oreMatch ), oreMatch );
            if( this.handler != null )
            {
                handler.setPartitionList( this.priorityList );
            }
            this.getHost().markForSave();
        }
    }

    @Override
    public void readFromNBT( NBTTagCompound data )
    {
        super.readFromNBT( data );
        this.oreMatch = data.getString( "oreMatch" );
        this.priorityList = new OreDictPriorityList<>( OreHelper.INSTANCE.getMatchingOre( oreMatch ), oreMatch );
    }

    @Override
    public void writeToNBT( NBTTagCompound data )
    {
        super.writeToNBT( data );
        data.setString( "oreMatch", oreMatch );
    }
}
