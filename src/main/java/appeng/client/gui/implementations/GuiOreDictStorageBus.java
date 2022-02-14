package appeng.client.gui.implementations;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.container.implementations.ContainerOreDictStorageBus;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.misc.PartOreDicStorageBus;
import appeng.util.item.OreDictFilterMatcher;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.regex.Pattern;


public class GuiOreDictStorageBus extends AEBaseGui
{
    private final ContainerOreDictStorageBus container;
    PartOreDicStorageBus part;
    private GuiTabButton priority;
    private GuiImgButton partition;
    private GuiImgButton storageFilter;
    private GuiImgButton rwMode;
    private static final Pattern ORE_DICTIONARY_FILTER = Pattern.compile( "[(!]* *[0-9a-zA-Z*]* *\\)*( *[&|^]? *[(!]* *[0-9a-zA-Z*]* *\\)*)*" );
    private MEGuiTextField searchFieldInputs;

    public GuiOreDictStorageBus( final InventoryPlayer inventoryPlayer, final PartOreDicStorageBus te )
    {
        super( new ContainerOreDictStorageBus( inventoryPlayer, te ) );
        this.container = (ContainerOreDictStorageBus) super.inventorySlots;
        part = te;
        this.ySize = 84;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.searchFieldInputs = new MEGuiTextField( this.fontRenderer, this.guiLeft + 3, this.guiTop + 22, 170, 12 );
        this.searchFieldInputs.setEnableBackgroundDrawing( false );
        this.searchFieldInputs.setMaxStringLength( 512 );
        this.searchFieldInputs.setTextColor( 0xFFFFFF );
        this.searchFieldInputs.setVisible( true );
        this.searchFieldInputs.setFocused( false );
        this.searchFieldInputs.setValidator( str -> ORE_DICTIONARY_FILTER.matcher( str ).matches() );

        this.buttonList.add( this.priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRender ) );
        this.buttonList.add( this.partition = new GuiImgButton( this.guiLeft - 18, this.guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH ) );
        this.buttonList.add( this.rwMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 48, Settings.ACCESS, AccessRestriction.READ_WRITE ) );
        this.buttonList.add( this.storageFilter = new GuiImgButton( this.guiLeft - 18, this.guiTop + 68, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY ) );

        try
        {
            NetworkHandler.instance().sendToServer( new PacketValueConfig( "OreDictStorageBus.getRegex", "1" ) );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }

    }

    public void fillRegex( String regex )
    {
        this.searchFieldInputs.setText( regex );
    }

    @Override
    protected void actionPerformed( final GuiButton btn ) throws IOException
    {
        super.actionPerformed( btn );

        final boolean backwards = Mouse.isButtonDown( 1 );

        try
        {
            if( btn == this.priority )
            {
                NetworkHandler.instance().sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );
            }
            else if( btn == this.partition )
            {
                NetworkHandler.instance().sendToServer( new PacketValueConfig( "StorageBus.Action", "Partition" ) );
            }
            else if( btn == this.rwMode )
            {
                NetworkHandler.instance().sendToServer( new PacketConfigButton( this.rwMode.getSetting(), backwards ) );
            }
            else if( btn == this.storageFilter )
            {
                NetworkHandler.instance().sendToServer( new PacketConfigButton( this.storageFilter.getSetting(), backwards ) );
            }
        }
        catch( final IOException e )
        {
            AELog.debug( e );
        }
    }

    @Override
    protected void mouseClicked( final int xCoord, final int yCoord, final int btn ) throws IOException
    {
        boolean wasFocused = this.searchFieldInputs.isFocused();
        this.searchFieldInputs.mouseClicked( xCoord, yCoord, btn );

        if( btn == 1 && this.searchFieldInputs.isMouseIn( xCoord, yCoord ) )
        {
            this.searchFieldInputs.setText( "" );
        }

        if( !searchFieldInputs.isFocused() && wasFocused )
        {
            searchFieldInputs.setText( OreDictFilterMatcher.validateExp( searchFieldInputs.getText() ) );
            NetworkHandler.instance().sendToServer( new PacketValueConfig( "OreDictStorageBus.save", searchFieldInputs.getText() ) );
        }

        super.mouseClicked( xCoord, yCoord, btn );
    }

    @Override
    protected void keyTyped( final char character, final int key ) throws IOException
    {
        if( !this.checkHotbarKeys( key ) )
        {
            if( !this.searchFieldInputs.textboxKeyTyped( character, key ) )
            {
                super.keyTyped( character, key );
            }
        }
    }

    @Override
    public void drawFG( int offsetX, int offsetY, int mouseX, int mouseY )
    {
        this.fontRenderer.drawString( this.getGuiDisplayName( GuiText.OreDictStorageBus.getLocal() ), 8, 6, 4210752 );
        this.fontRenderer.drawString( this.searchFieldInputs.getText().length() + " / " + this.searchFieldInputs.getMaxStringLength(), 120, 36, 4210752 );
        this.fontRenderer.drawString( "& = AND    " + "| = OR", 8, 36, 4210752 );
        this.fontRenderer.drawString( "^ = XOR    " + "! = NOT", 8, 48, 4210752 );
        this.fontRenderer.drawString( "() for priority    " + "* for wildcard", 8, 60, 4210752 );
        this.fontRenderer.drawString( "Ex.: *Redstone*&!dustRedstone", 8, 72, 4210752 );

        if( this.storageFilter != null )
        {
            this.storageFilter.set( container.getStorageFilter() );
        }

        if( this.rwMode != null )
        {
            this.rwMode.set( container.getReadWriteMode() );
        }
    }

    @Override
    public void drawBG( int offsetX, int offsetY, int mouseX, int mouseY )
    {
        this.bindTexture( "guis/oredictstoragebus.png" );
        this.drawTexturedModalRect( offsetX, offsetY, 0, 0, 175, 85 );

        if( this.searchFieldInputs != null )
        {
            this.searchFieldInputs.drawTextBox();
        }
    }

}
