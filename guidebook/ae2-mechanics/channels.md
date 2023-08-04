---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Channels
  icon: controller
---

# Channels

Applied Energistics 2's [ME Networks](me-network-connections.md) require
Channels to support [devices](../ae2-mechanics/devices.md) which use networked storage, or other network
services. Think of channels like USB cables to all your devices. A computer only has so many USB ports and can only support
so many devices connected to it. Most machines, full-block devices, and standard cables can only pass through
up to 8 channels. You can think of full-block devices and standard cables as a bundle of 8 "channel wires". However, [dense cables](../items-blocks-machines/cables.md#dense-cable) can support up
to 32 channels. The only other devices capable of transmitting 32 are <ItemLink id="me_p2p_tunnel" />
and the [Quantum Network Bridge](../items-blocks-machines/quantum_bridge.md). Each time a device uses up a channel, imagine pulling off a usb "wire" from
the bundle, which obviously means that "wire" isn't available further down the line.

<GameScene zoom="7" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_demonstration_1.snbt" />

  <LineAnnotation color="#33ff33" from="1 .4 .7" to="2.4 .4 .7" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .7" to="2.4 .6 .7" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .6" to="2.6 .4 .6" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .6" to="2.6 .6 .6" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .6 .6" to="2.6 .6 .6" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.4 .6 .7" to="2.4 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.4 .4 .7" to="2.4 .4 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .6 .6" to="2.6 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .4 .6" to="2.6 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.1 .6 1.5" to="2.4 .6 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.6 .4 1.5" to="2.9 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="2.6 .6 1.5" to="2.6 .9 1.5" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="2.4 .1 1.5" to="2.4 .4 1.5" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1 .6 .4" to="3.5 .6 .4" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .4" to="3.5 .4 .4" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="3.5 .6 .4" to="3.5 .9 .4" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="3.5 .1 .4" to="3.5 .4 .4" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1 .6 .3" to="1.5 .6 .3" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1 .4 .3" to="1.5 .4 .3" alwaysOnTop={true}/>

  <LineAnnotation color="#33ff33" from="1.5 .6 .3" to="1.5 .9 .3" alwaysOnTop={true}/>
  <LineAnnotation color="#33ff33" from="1.5 .1 .3" to="1.5 .4 .3" alwaysOnTop={true}/>

  <LineAnnotation color="#ff3333" from="3.5 .5 .5" to="5.5 .5 .5" alwaysOnTop={true}>
  All 8 channels in the cable have been used, so the Drive does not get one.  
  </LineAnnotation>

  <LineAnnotation color="#993333" from="1 .5 .5" to="1.25 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="1.5 .5 .5" to="1.75 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="2 .5 .5" to="2.25 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="2.5 .5 .5" to="2.75 .5 .5" alwaysOnTop={true}/>
  <LineAnnotation color="#993333" from="3 .5 .5" to="3.25 .5 .5" alwaysOnTop={true}/>

  <DiamondAnnotation pos="3.6 0.5 0.5" color="#ff0000">
        All 8 channels in the cable have been used, so the Drive does not get one.
    </DiamondAnnotation>

  <IsometricCamera yaw="15" pitch="30" />
</GameScene>

An easy way to see how channels are being used and routed through your network is to use [smart cables](../items-blocks-machines/cables.md), which will display on them the paths and usage of channels.

Channels will consume 1⁄128 ae/t per node they transverse, this means that by
adding a <ItemLink id="controller" /> for a
network with 8 devices and over 96 nodes your power usage might actually
decrease power consumption because it changes how channels are allocated.

Of note, **CHANNELS HAVE NOTHING TO DO WITH CABLE COLOR**, all cable color does is make cables not connect.

## Channel Routing

When using a <ItemLink id="controller" />,
channels route via 3 steps. They first take the shortest path through adjacent machines to the nearest [normal cable](../items-blocks-machines/cables.md)
(glass, covered, or smart). They then take the shortest path through that normal cable to the nearest [dense cable](../items-blocks-machines/cables.md)
(dense or dense smart). They then take the shortest path through that dense cable to the <ItemLink id="controller" />.
If the shortest path is already maxed out, some [devices](devices.md) may not get their required channels, use
colored cables, cable anchors and tunnels to your advantage to make sure your channels go in the path you desire.

For example, in this case some drives don't get channels because although there is enough capacity in the cables, the
channels try to take the shortest path, overloading some cables while leaving others empty.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_path_length_issue.snbt" />

  <LineAnnotation color="#33ff33" from="3 .5 1.4" to="0.4 0.5 1.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 .5 1.4" to="0.4 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 0.5 3.6" to="1.4 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.4 0.5 3.6" to="1.4 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#33ff33" from="3 0.5 3.6" to="1.6 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.6 0.5 3.6" to="1.6 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#ff3333" from="3 .5 1.6" to="0.6 .5 1.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#ff3333" from="0.6 .5 1.6" to="0.6 .5 3.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#ff3333" from="0.6 .5 3.4" to="1.4 .5 3.4" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#ff3333" from="3 .5 3.4" to="1.6 .5 3.4" alwaysOnTop={true} thickness="0.05"/>

  <BoxAnnotation color="#dddddd" min="1.2 0.2 3.2" max="1.8 0.8 3.8" alwaysOnTop={true} thickness="0.05">
        More than 8 channels attempt to route through here so some are cut off.
  </BoxAnnotation>

  <IsometricCamera yaw="90" pitch="90" />

</GameScene>

This can be fixed by more carefully constraining the paths channels can take. Networks should be treelike (or bushlike).
Loops and ambiguous channel paths should be minimized.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/channel_path_length_issue_fix.snbt" />

  <LineAnnotation color="#33ff33" from="3 .5 1.4" to="0.4 0.5 1.4" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 .5 1.4" to="0.4 0.5 5.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="0.4 0.5 5.6" to="1 0.5 5.6" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#33ff33" from="3 0.5 3.6" to="1.6 0.5 3.6" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1.6 0.5 3.6" to="1.6 0.5 5" alwaysOnTop={true} thickness="0.05"/>

  <IsometricCamera yaw="90" pitch="90" />

</GameScene>

## Ad-Hoc Networks

A Network without a <ItemLink id="controller" />
is considered to be Ad-Hoc, and can support up to 8 channel using devices.
Once you exceed 8 devices the network's channel using devices will shutdown,
you can either remove devices, or add a <ItemLink id="controller" />.

Unlike with controllered networks, [smart cables](../items-blocks-machines/cables.md) on ad-hoc networks will show the number
of channels in use network-wide instead of the number of channels flowing through that specific cable.

While using ad-hoc networks each device will
use 1 channel network wide, this is very different from how <ItemLink id="controller" /> allocate channels based on
shortest route.

## Design

As mentioned before in [channel routing](channels.md#channel-routing), it's best to design your network in a treelike structure, with dense cables branching out from the controller, regular cables
branching out from the dense, and [devices](../ae2-mechanics/devices.md) in clusters of 8 or fewer on the regular cables.

Here is an example of what not to do:

Following the channel paths,

1. Immediately exiting the controller to the right, we're bottlenecked to 8 channels because the drive acts like a normal cable.
However since we're not using a smart cable here we cannot see how many channels are in use. 8 channels left.
2. The drive takes a channel.
7 channels left.
3. 2 channels go up to the terminals.
5 channels left.
4. Continuing right, the interface takes another channel.
4 channels left.
5. 1 channel goes up to the pattern provider.
3 channels left.
6. Continuing right, 1 channel goes up to the import bus.
2 channels left.
7. The cluster of pattern providers feeding assemblers only gets 2 channels, so 2 of the providers do not get channels.

Ultimately the error is in bottlenecking the channels and not thinking through how channels will be distributed.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/bad_network_structure.snbt" />

<LineAnnotation color="#33ff33" from="6.5 .5 1.5" to="6 .5 1.5" alwaysOnTop={true} thickness="0.4">
  32 channels
</LineAnnotation>

<LineAnnotation color="#33ff33" from="6 .5 1.5" to="5.5 .5 1.5" alwaysOnTop={true} thickness="0.2">
  8 channels
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="5.5 1.5 1.5" alwaysOnTop={true} thickness="0.1">
  2 channels
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="5.5 .3 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 1.5 1.5" to="5.5 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 2.5 1.5" to="5.5 2.5 1.1" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="5.5 .5 1.5" to="4.5 .5 1.5" alwaysOnTop={true} thickness="0.158">
  5 channels
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="4.5 .3 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="4.5 1.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="4.5 .5 1.5" to="3.5 .5 1.5" alwaysOnTop={true} thickness="0.122">
  3 channels
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 .5 1.5" to="3.5 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 2.5 1.5" to="3.7 2.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="3.5 .5 1.5" to="1.5 .5 1.5" alwaysOnTop={true} thickness="0.1">
  2 channels
</LineAnnotation>

<LineAnnotation color="#33ff33" from="1.5 0.5 1.5" to="1.5 0.3 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="1.5 0.5 1.5" to="0.5 0.5 1.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#33ff33" from="0.5 0.5 1.5" to="0.5 0.5 0.5" alwaysOnTop={true} thickness="0.071">
  1 channel
</LineAnnotation>

<LineAnnotation color="#ff3333" from="0.5 1.5 1.5" to="0.5 1.3 1.5" alwaysOnTop={true} thickness="0.071">
  no channels
</LineAnnotation>

<LineAnnotation color="#ff3333" from="1.5 1.5 0.5" to="1.5 1.3 0.5" alwaysOnTop={true} thickness="0.071">
  no channels
</LineAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

---

Here is an example of a good structure:

<GameScene zoom="2.5" interactive={true}>
  <ImportStructure src="../assets/assemblies/treelike_network_structure.snbt" />

    <BoxAnnotation color="#dddddd" min="6.9 0 4.9" max="9.1 4 7.1" thickness="0.05">
        Notice that the pattern providers are in separate groups of 8.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="5 4 4" max="8 5 5" thickness="0.05">
        Two regular cables full of channels coming together mean you need a dense cable.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="5 0 13" max="8 1 14" thickness="0.05">
        Different cable colors are used to prevent adjacent cables from connecting.
    </BoxAnnotation>


  <IsometricCamera yaw="315" pitch="30" />
</GameScene>

## Channel Modes

AE2 10.0.0 for Minecraft 1.18 introduces new options to change how AE2 channels behave in your world.
There's a new configuration option in the general section (`channels`) which controls this option, and a new in-game
command for operators to change the mode and the config from inside the game. The command is `/ae2 channelmode <mode>`
to change it and `/ae2 channelmode` to show the current mode. When the mode is changed in-game, all existing grids will
reboot and use the new mode immediately.

This resurrects and improves upon the option that was available in Minecraft 1.12 and introduces better options for
players that just want a little more laid back gameplay but don't want the mechanic to be removed entirely.

The following table lists the available modes in both the configuration file and command.

| Setting    | Description                                                                                                                                                                                                                               |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `default`  | The standard mode with the channel capacities of cable and ad-hoc networks as described throughout this website                                                                                                                           |
| `x2`       | All channel capacities are doubled (16 on normal cable, 64 on dense cable, ad-hoc networks support 16 channels)                                                                                                                           |
| `x3`       | All channel capacities are tripled (24 on normal cable, 92 on dense cable, ad-hoc networks support 24 channels)                                                                                                                           |
| `x4`       | All channel capacities are quadrupled (32 on normal cable, 128 on dense cable, ad-hoc networks support 32 channels)                                                                                                                       |
| `infinite` | All channel restrictions are removed. Controllers still reduce the power consumption of grids *significantly*. Smart cables will only toggle between completely off (no channels carried) and completely on (1 or more channels carried). |