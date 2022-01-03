package appeng.container.implementations;

import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.parts.misc.PartOreDicStorageBus;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;


public class ContainerPartOreDictStorageBus extends AEBaseContainer
{
    private final PartOreDicStorageBus part;

    public ContainerPartOreDictStorageBus( final InventoryPlayer ip, final PartOreDicStorageBus anchor )
    {
        super( ip, anchor );
        this.part = anchor;

        this.bindPlayerInventory( ip, 14, 256 - /* height of player inventory */82 );
    }

    @Override
    public void detectAndSendChanges()
    {
        if( Platform.isClient() )
        {
            return;
        }

        super.detectAndSendChanges();

    }

    public void saveOreMatch( String value )
    {
        part.saveOreMatch( value );
    }

    public void sendRegex()
    {
        try
        {
            NetworkHandler.instance().sendTo( new PacketValueConfig( "OreDictStorageBus.sendRegex", part.getOreMatch() ), (EntityPlayerMP) getInventoryPlayer().player );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
