
package appeng.core.sync;


import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;


public interface AppEngPacketHandler<R extends IMessage, S extends IMessage> extends IMessageHandler<R, S>
{

}
