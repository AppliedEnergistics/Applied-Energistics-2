package appeng.parts.reporting;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import static appeng.helpers.PatternHelper.PROCESSING_INPUT_LIMIT;
import static appeng.helpers.PatternHelper.PROCESSING_OUTPUT_LIMIT;


public class PartExpandedProcessingPatternTerminal extends AbstractPartTerminal
{
    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation( AppEng.MOD_ID, "part/expanded_processing_pattern_terminal_off" );
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation( AppEng.MOD_ID, "part/expanded_processing_pattern_terminal_on" );

    public static final IPartModel MODELS_OFF = new PartModel( MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF );
    public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, MODEL_ON, MODEL_STATUS_ON );
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel( MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL );

    private final AppEngInternalInventory crafting = new AppEngInternalInventory( this, PROCESSING_INPUT_LIMIT );

    private final AppEngInternalInventory output = new AppEngInternalInventory( this, PROCESSING_OUTPUT_LIMIT );
    private final AppEngInternalInventory pattern = new AppEngInternalInventory( this, 2 );

    @Reflected
    public PartExpandedProcessingPatternTerminal( final ItemStack is )
    {
        super( is );
    }

    @Override
    public void readFromNBT( NBTTagCompound data )
    {
        super.readFromNBT( data );
        this.crafting.readFromNBT( data, "processingGrid" );
        this.pattern.readFromNBT( data, "pattern" );
        this.output.readFromNBT( data, "outputList" );
    }

    @Override
    public void writeToNBT( NBTTagCompound data )
    {
        super.writeToNBT( data );
        this.crafting.writeToNBT( data, "processingGrid" );
        this.pattern.writeToNBT( data, "pattern" );
        this.output.writeToNBT( data, "outputList" );
    }

    @Override
    public GuiBridge getGui( final EntityPlayer p )
    {
        int x = (int) p.posX;
        int y = (int) p.posY;
        int z = (int) p.posZ;
        if( this.getHost().getTile() != null )
        {
            x = this.getTile().getPos().getX();
            y = this.getTile().getPos().getY();
            z = this.getTile().getPos().getZ();
        }

        if( GuiBridge.GUI_EXPANDED_PROCESSING_PATTERN_TERMINAL.hasPermissions( this.getHost().getTile(), x, y, z, this.getSide(), p ) )
        {
            return GuiBridge.GUI_EXPANDED_PROCESSING_PATTERN_TERMINAL;
        }
        return GuiBridge.GUI_ME;
    }

    @Override
    public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
    {
        if( inv == this.pattern && slot == 1 )
        {
            final ItemStack is = this.pattern.getStackInSlot( 1 );
            if( !is.isEmpty() && is.getItem() instanceof ICraftingPatternItem )
            {
                final ICraftingPatternItem pattern = (ICraftingPatternItem) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem( is, this.getHost().getTile().getWorld() );
                if( details != null )
                {
                    for( int x = 0; x < this.crafting.getSlots() && x < details.getInputs().length; x++ )
                    {
                        final IAEItemStack item = details.getInputs()[x];
                        this.crafting.setStackInSlot( x, item == null ? ItemStack.EMPTY : item.createItemStack() );
                    }

                    for( int x = 0; x < this.output.getSlots() && x < details.getOutputs().length; x++ )
                    {
                        final IAEItemStack item = details.getOutputs()[x];
                        this.output.setStackInSlot( x, item == null ? ItemStack.EMPTY : item.createItemStack() );
                    }
                }
            }
        }

        this.getHost().markForSave();
    }

    @Override
    public IItemHandler getInventoryByName( final String name )
    {
        if( name.equals( "crafting" ) )
        {
            return this.crafting;
        }
        if( name.equals( "output" ) )
        {
            return this.output;
        }
        if( name.equals( "pattern" ) )
        {
            return this.pattern;
        }
        return super.getInventoryByName( name );
    }

    @Override
    public IPartModel getStaticModels()
    {
        return this.selectModel( MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL );
    }
}

