package appeng.client.command;

import jdk.jfr.DataAmount;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Name(value = "ae2.PacketReceivedClientSide")
@Label("Packet Received (Client-Side)")
@Description("An AE2 packet was received on the client")
@StackTrace(false)
public class ClientPacketReceived extends Event {
    @Label("Type")
    public Class<?> type;

    @Label("Size")
    @DataAmount
    public long size;
}
