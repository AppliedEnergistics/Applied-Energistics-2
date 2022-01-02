package appeng.container.implementations;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.container.slot.*;
import appeng.helpers.IContainerCraftingPacket;
import appeng.parts.reporting.PartExpandedProcessingPatternTerminal;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static appeng.helpers.PatternHelper.PROCESSING_INPUT_LIMIT;
import static appeng.helpers.PatternHelper.PROCESSING_OUTPUT_LIMIT;


public class ContainerExpandedProcessingPatternTerm extends ContainerMEMonitorable implements IAEAppEngInventory, IOptionalSlotHost, IContainerCraftingPacket
{
    private final IItemHandler crafting;
    private final SlotFakeCraftingMatrix[] gridSlots = new SlotFakeCraftingMatrix[PROCESSING_INPUT_LIMIT];
    private final OptionalSlotFake[] outputSlots = new OptionalSlotFake[PROCESSING_OUTPUT_LIMIT];
    private final SlotRestrictedInput patternSlotIN;
    private final SlotRestrictedInput patternSlotOUT;
    private final PartExpandedProcessingPatternTerminal expandedProcessingPatternTerminal;

    public ContainerExpandedProcessingPatternTerm( InventoryPlayer ip, ITerminalHost monitorable )
    {
        super( ip, monitorable, false );

        this.expandedProcessingPatternTerminal = (PartExpandedProcessingPatternTerminal) monitorable;

        final IItemHandler patternInv = this.getExpandedPatternTerminal().getInventoryByName( "pattern" );
        final IItemHandler output = this.getExpandedPatternTerminal().getInventoryByName( "output" );

        this.crafting = this.getExpandedPatternTerminal().getInventoryByName( "crafting" );

        for( int y = 0; y < 4; y++ )
        {
            for( int x = 0; x < 4; x++ )
            {
                this.addSlotToContainer( this.gridSlots[x + y * 4] = new SlotFakeCraftingMatrix( this.crafting, x + y * 4, 4 + x * 18, -85 + y * 18 ) );
            }
        }

        for( int y = 0; y < 3; y++ )
        {
            for( int x = 0; x < 2; x++ )
            {
                this.addSlotToContainer( this.outputSlots[x + y * 2] = new SlotPatternOutputs( output, this, x + y * 2, 96 + x * 18, -76 + y * 18, 0, 0, 1 ) );
                this.outputSlots[x + y * 2].setRenderDisabled( false );
                this.outputSlots[x + y * 2].setIIcon( -1 );
            }
        }

        this.addSlotToContainer( this.patternSlotIN = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, patternInv, 0, 147, -72 - 9, this.getInventoryPlayer() ) );
        this.addSlotToContainer( this.patternSlotOUT = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, patternInv, 1, 147, -72 + 34, this.getInventoryPlayer() ) );

        this.patternSlotOUT.setStackLimit( 1 );

        this.bindPlayerInventory( ip, 0, 0 );
    }

    @Override
    public ItemStack transferStackInSlot( final EntityPlayer p, final int idx )
    {
        if( Platform.isClient() )
        {
            return ItemStack.EMPTY;
        }
        if( this.inventorySlots.get( idx ) instanceof SlotPlayerInv || this.inventorySlots.get( idx ) instanceof SlotPlayerHotBar )
        {
            final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get( idx ); // require AE SLots!
            ItemStack itemStack = clickSlot.getStack();
            if( AEApi.instance().definitions().materials().blankPattern().isSameAs( itemStack ) )
            {
                IItemHandler patternInv = this.getExpandedPatternTerminal().getInventoryByName( "pattern" );
                ItemStack remainder = patternInv.insertItem( 0, itemStack, false );
                clickSlot.putStack( remainder );
            }
        }
        return super.transferStackInSlot( p, idx );
    }

    @Override
    public void saveChanges()
    {

    }

    @Override
    public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
    {

    }

    public void encodeAndMoveToInventory()
    {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if( !output.isEmpty() )
        {
            if( !getPlayerInv().addItemStackToInventory( output ) )
            {
                getPlayerInv().player.dropItem( output, false );
            }
            this.patternSlotOUT.putStack( ItemStack.EMPTY );
        }
    }

    public void encode()
    {
        ItemStack output = this.patternSlotOUT.getStack();

        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();

        // if there is no input, this would be silly.
        if( in == null || out == null )
        {
            return;
        }

        // first check the output slots, should either be null, or a pattern
        if( !output.isEmpty() && !this.isPattern( output ) )
        {
            return;
        } // if nothing is there we should snag a new pattern.
        else if( output.isEmpty() )
        {
            output = this.patternSlotIN.getStack();
            if( output.isEmpty() || !this.isPattern( output ) )
            {
                return; // no blanks.
            }

            // remove one, and clear the input slot.
            output.setCount( output.getCount() - 1 );
            if( output.getCount() == 0 )
            {
                this.patternSlotIN.putStack( ItemStack.EMPTY );
            }

            // add a new encoded pattern.
            Optional<ItemStack> maybePattern = AEApi.instance().definitions().items().encodedPattern().maybeStack( 1 );
            if( maybePattern.isPresent() )
            {
                output = maybePattern.get();
                this.patternSlotOUT.putStack( output );
            }
        }

        // encode the slot.
        final NBTTagCompound encodedValue = new NBTTagCompound();

        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        for( final ItemStack i : in )
        {
            tagIn.appendTag( this.createItemTag( i ) );
        }

        for( final ItemStack i : out )
        {
            tagOut.appendTag( this.createItemTag( i ) );
        }

        encodedValue.setTag( "in", tagIn );
        encodedValue.setTag( "out", tagOut );
        encodedValue.setBoolean( "crafting", false );
        encodedValue.setBoolean( "substitute", false );

        output.setTagCompound( encodedValue );
    }

    boolean isPattern( final ItemStack output )
    {
        if( output.isEmpty() )
        {
            return false;
        }

        final IDefinitions definitions = AEApi.instance().definitions();

        boolean isPattern = definitions.items().encodedPattern().isSameAs( output );
        isPattern |= definitions.materials().blankPattern().isSameAs( output );

        return isPattern;
    }

    NBTBase createItemTag( final ItemStack i )
    {
        final NBTTagCompound c = new NBTTagCompound();

        if( !i.isEmpty() )
        {
            i.writeToNBT( c );
        }

        return c;
    }

    @Override
    public boolean isSlotEnabled( final int idx )
    {
        return true;
    }

    protected ItemStack[] getInputs()
    {
        final ItemStack[] input = new ItemStack[16];
        boolean hasValue = false;

        for( int x = 0; x < this.gridSlots.length; x++ )
        {
            input[x] = this.gridSlots[x].getStack();
            if( !input[x].isEmpty() )
            {
                hasValue = true;
            }
        }

        if( hasValue )
        {
            return input;
        }

        return null;
    }

    protected ItemStack[] getOutputs()
    {
        final List<ItemStack> list = new ArrayList<>( 3 );
        boolean hasValue = false;

        for( final OptionalSlotFake outputSlot : this.outputSlots )
        {
            final ItemStack out = outputSlot.getStack();

            if( !out.isEmpty() && out.getCount() > 0 )
            {
                list.add( out );
                hasValue = true;
            }
        }

        if( hasValue )
        {
            return list.toArray( new ItemStack[0] );
        }
        return null;
    }

    public void clear()
    {
        for( final Slot s : this.gridSlots )
        {
            s.putStack( ItemStack.EMPTY );
        }

        for( final Slot s : this.outputSlots )
        {
            s.putStack( ItemStack.EMPTY );
        }

        this.detectAndSendChanges();
    }

    @Override
    public IItemHandler getInventoryByName( final String name )
    {
        if( name.equals( "player" ) )
        {
            return new PlayerInvWrapper( this.getInventoryPlayer() );
        }
        return this.getExpandedPatternTerminal().getInventoryByName( name );
    }

    @Override
    public boolean useRealItems()
    {
        return false;
    }

    public PartExpandedProcessingPatternTerminal getExpandedPatternTerminal()
    {
        return this.expandedProcessingPatternTerminal;
    }

    public void multiply( int multiple )
    {
        ItemStack[] input = new ItemStack[gridSlots.length];
        boolean canMultiplyInputs = true;
        boolean canMultiplyOutputs = true;

        for( int x = 0; x < this.gridSlots.length; x++ )
        {
            input[x] = this.gridSlots[x].getStack();
            if( !input[x].isEmpty() && input[x].getCount() * multiple > input[x].getMaxStackSize() )
            {
                canMultiplyInputs = false;
            }
        }
        for( final OptionalSlotFake outputSlot : this.outputSlots )
        {
            final ItemStack out = outputSlot.getStack();
            if( !out.isEmpty() && out.getCount() * multiple > out.getMaxStackSize() )
            {
                canMultiplyOutputs = false;
            }
        }
        if( canMultiplyInputs && canMultiplyOutputs )
        {
            for( SlotFakeCraftingMatrix craftingSlot : this.gridSlots )
            {
                ItemStack stack = craftingSlot.getStack();
                if( !stack.isEmpty() )
                {
                    craftingSlot.getStack().setCount( stack.getCount() * multiple );
                }
            }
            for( OptionalSlotFake outputSlot : this.outputSlots )
            {
                ItemStack stack = outputSlot.getStack();
                if( !stack.isEmpty() )
                {
                    outputSlot.getStack().setCount( stack.getCount() * multiple );
                }
            }
        }
    }

    public void divide( int divide )
    {
        ItemStack[] input = new ItemStack[gridSlots.length];
        boolean canDivideInputs = true;
        boolean canDivideOutputs = true;

        for( int x = 0; x < this.gridSlots.length; x++ )
        {
            input[x] = this.gridSlots[x].getStack();
            if( !input[x].isEmpty() && input[x].getCount() % divide != 0 )
            {
                canDivideInputs = false;
            }
        }
        for( final OptionalSlotFake outputSlot : this.outputSlots )
        {
            final ItemStack out = outputSlot.getStack();
            if( !out.isEmpty() && out.getCount() % divide != 0 )
            {
                canDivideOutputs = false;
            }
        }
        if( canDivideInputs && canDivideOutputs )
        {
            for( SlotFakeCraftingMatrix craftingSlot : this.gridSlots )
            {
                ItemStack stack = craftingSlot.getStack();
                if( !stack.isEmpty() )
                {
                    craftingSlot.getStack().setCount( stack.getCount() / divide );
                }
            }
            for( OptionalSlotFake outputSlot : this.outputSlots )
            {
                ItemStack stack = outputSlot.getStack();
                if( !stack.isEmpty() )
                {
                    outputSlot.getStack().setCount( stack.getCount() / divide );
                }
            }
        }
    }

    public void increase( int increase )
    {
        ItemStack[] input = new ItemStack[gridSlots.length];
        boolean canIncreaseInputs = true;
        boolean canIncreaseOutputs = true;

        for( int x = 0; x < this.gridSlots.length; x++ )
        {
            input[x] = this.gridSlots[x].getStack();
            if( !input[x].isEmpty() && input[x].getCount() + increase > input[x].getMaxStackSize() )
            {
                canIncreaseInputs = false;
            }
        }
        for( final OptionalSlotFake outputSlot : this.outputSlots )
        {
            final ItemStack out = outputSlot.getStack();
            if( !out.isEmpty() && out.getCount() + increase > out.getMaxStackSize() )
            {
                canIncreaseOutputs = false;
            }
        }
        if( canIncreaseInputs && canIncreaseOutputs )
        {
            for( SlotFakeCraftingMatrix craftingSlot : this.gridSlots )
            {
                ItemStack stack = craftingSlot.getStack();
                if( !stack.isEmpty() )
                {
                    craftingSlot.getStack().setCount( stack.getCount() + increase );
                }
            }
            for( OptionalSlotFake outputSlot : this.outputSlots )
            {
                ItemStack stack = outputSlot.getStack();
                if( !stack.isEmpty() )
                {
                    outputSlot.getStack().setCount( stack.getCount() + increase );
                }
            }
        }
    }

    public void decrease( int decrease )
    {
        ItemStack[] input = new ItemStack[gridSlots.length];
        boolean canDecreaseInputs = true;
        boolean canDecreaseOutputs = true;

        for( int x = 0; x < this.gridSlots.length; x++ )
        {
            input[x] = this.gridSlots[x].getStack();
            if( !input[x].isEmpty() && input[x].getCount() - decrease < 1 )
            {
                canDecreaseInputs = false;
            }
        }
        for( final OptionalSlotFake outputSlot : this.outputSlots )
        {
            final ItemStack out = outputSlot.getStack();
            if( !out.isEmpty() && out.getCount() - decrease < 1 )
            {
                canDecreaseOutputs = false;
            }
        }
        if( canDecreaseInputs && canDecreaseOutputs )
        {
            for( SlotFakeCraftingMatrix craftingSlot : this.gridSlots )
            {
                ItemStack stack = craftingSlot.getStack();
                if( !stack.isEmpty() )
                {
                    craftingSlot.getStack().setCount( stack.getCount() - decrease );
                }
            }
            for( OptionalSlotFake outputSlot : this.outputSlots )
            {
                ItemStack stack = outputSlot.getStack();
                if( !stack.isEmpty() )
                {
                    outputSlot.getStack().setCount( stack.getCount() - decrease );
                }
            }
        }
    }

    public void maximizeCount()
    {
        ItemStack[] input = new ItemStack[gridSlots.length];
        boolean canGrowInputs = true;
        boolean canGrowOutputs = true;
        int maxInputStackGrowth = 0;
        int maxOutputStackGrowth = 0;

        for( int x = 0; x < this.gridSlots.length; x++ )
        {
            input[x] = this.gridSlots[x].getStack();
            if( !input[x].isEmpty() && input[x].getMaxStackSize() - input[x].getCount() > maxInputStackGrowth )
            {
                maxInputStackGrowth = input[x].getMaxStackSize() - input[x].getCount();
            }
            if( !input[x].isEmpty() && input[x].getCount() + maxInputStackGrowth > input[x].getMaxStackSize() )
            {
                canGrowInputs = false;
            }
        }
        for( final OptionalSlotFake outputSlot : this.outputSlots )
        {
            final ItemStack out = outputSlot.getStack();
            {
                maxOutputStackGrowth = out.getMaxStackSize() - out.getCount();
            }
            if( !out.isEmpty() && out.getCount() + maxOutputStackGrowth > out.getMaxStackSize() )
            {
                canGrowOutputs = false;
            }
        }
        if( canGrowInputs && canGrowOutputs )
        {
            int maxStackGrowth = Math.min( maxInputStackGrowth, maxOutputStackGrowth );
            for( SlotFakeCraftingMatrix craftingSlot : this.gridSlots )
            {
                ItemStack stack = craftingSlot.getStack();
                if( !stack.isEmpty() )
                {
                    craftingSlot.getStack().setCount( stack.getCount() + maxStackGrowth );
                }
            }
            for( OptionalSlotFake outputSlot : this.outputSlots )
            {
                ItemStack stack = outputSlot.getStack();
                if( !stack.isEmpty() )
                {
                    outputSlot.getStack().setCount( stack.getCount() + maxStackGrowth );
                }
            }
        }
    }

}
