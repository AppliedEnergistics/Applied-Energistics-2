---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Network Connections
  icon: fluix_glass_cable
---

# Network Connections

## What Does "Network" Mean?

A "Network" is a group of [devices](../ae2-mechanics/devices.md) linked by blocks that can pass [channels](../ae2-mechanics/channels.md),
like [cables](../items-blocks-machines/cables.md) or fullblock machines and [devices](../ae2-mechanics/devices.md). 
(<ItemLink id="charger" />, <ItemLink id="interface" />, <ItemLink id="drive" />, etc.)
Technically a single cable is a network, actually.

## An Aside On Device Positioning

For [devices](../ae2-mechanics/devices.md) which have some specific network function (like an <ItemLink id="interface" />
pushing to and pulling from [network storage](../ae2-mechanics/import-export-storage.md), a <ItemLink id="level_emitter" />
reading the contents of network storage, an <ItemLink id="drive" /> being network storage, etc.)
the physical position of the device does not matter.

Again, **the physical position of the device does not matter**. All that matters is that the device is connected to the network
(and of course which network it's connected to).

## Network Connections

An easy way of determining what's connected in a network is using a <ItemLink id="network_tool" />. It will show every
component on the network, so if you see stuff you shouldn't or don't see stuff you should, you have a problem.

For example, this is 2 separate networks.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        Network 1
  </BoxAnnotation>

<BoxAnnotation color="#5CA7CD" min="2 0 0" max="3 2 2">
        Network 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

This is also 2 separate networks, because the <ItemLink id="quartz_fiber" /> shares [energy](../ae2-mechanics/energy.md)
without providing a network connection.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/2_networks_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1 2 2">
        Network 1
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="1.3 0 0" max="3 2 2">
        Network 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

However, this is just 1 network, not 2 separate ones. The [quantum bridge](../items-blocks-machines/quantum_bridge.md) acts like
a wireless [dense cable](../items-blocks-machines/cables.md#dense-cable), so both ends are on the same network.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="7 3 3">
        All 1 network
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

This is also just 1 network, as [cable](../items-blocks-machines/cables.md) color has nothing to do with network connections other than cables of different colors not
connecting to each other. All colors connect to fluix (or "uncolored") cables.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/actually_1_network_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        All 1 network
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Connections In The Context of Subnets

[Subnetworks](../ae2-mechanics/subnetworks.md) take advantage of network connections (and specifically **NOT** being connected)
to restrict what [devices](../ae2-mechanics/devices.md) have access to what other devices.

All a subnet is, really, is a separate network.

For example, take the [Automatic Ore Fortuner](../example-setups/ore-fortuner.md). There are 3 separate networks here,
all of which serve a specific purpose in the setup.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/ore_fortuner.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 2" max="3 1 3">
        Network 1, acts like a pipe subnet, restricts what the import bus has access to so it "stores" the ore blocks through the
        formation planes.
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="0 0 0" max="3 1 1">
        Network 2, Acts like another pipe subnet, restricts what the annihilation planes have access to so they store
        the fortuned ore chunks in the barrel and not in your main network. Also means they don't use up any channels on the
        main network.
  </BoxAnnotation>

  <BoxAnnotation color="#82CD5C" min="2 0 1" max="4 1 2">
        Network 3, The main network with all your storage and crafting on it. Just here to supply power, really, and specifically
        *not* connected to the 2 subnets.
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Connections In The Context of P2P

One variant of [P2P Tunnel](../items-blocks-machines/p2p_tunnels.md) moves [Channels](channels.md) instead of items or fluids
or redstone signal, and this confuses people for some reason. The network the tunnel is mounted to has nothing to do with the
network the tunnel is carrying. they *can* be the same network, but they don't have to be, and usually aren't.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/p2p_channels_network_connection.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1.98 2 1">
        Network 1, the network being carried (usually your main network)
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="2.02 0 0" max="3.98 1 1">
        Network 2, The network running the ME P2P tunnels (usually *not* your main network)
  </BoxAnnotation>

  <BoxAnnotation color="#915dcd" min="4.02 0 0" max="6 1 1">
        Network 1, the network being carried (usually your main network)
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Less Intuitive Connections

In this case, this is just 1 network, because the <ItemLink id="pattern_provider" />, being a fullblock device, acts like
a cable, and the <ItemLink id="inscriber" /> does similar. Thus, the network connection passes through
the provider and inscriber.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_1.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="4 2 2">
        All 1 network
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

To prevent this (useful for many autocrafting setups involving [subnetworks](../ae2-mechanics/subnetworks.md)),
you can right-click the provider with a <ItemLink id="certus_quartz_wrench" /> to make it directional, in which case it will
not pass channels through one side.

<Row gap="40">
<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_network_connection_2.snbt" />

  <BoxAnnotation color="#915dcd" min="0 0 0" max="1.98 2 2">
        Network 1
  </BoxAnnotation>

  <BoxAnnotation color="#5CA7CD" min="2.02 0 0" max="4 2 2">
        Network 2
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/pattern_provider_directional_connection.snbt" />

  <BoxAnnotation color="#ee3333" min="1 .3 .3" max="1.3 .7 .7">
        Observe how the cable does not connect
  </BoxAnnotation>

  <IsometricCamera yaw="255" pitch="30" />
</GameScene>
</Row>

Other parts that do not provide directional network connections are most [subpart](../ae2-mechanics/cable-subparts.md)
[devices](../ae2-mechanics/devices.md) like <ItemLink id="import_bus" />ses, <ItemLink id="storage_bus" />ses, and
<ItemLink id="cable_interface" />s.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/subpart_no_connection.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>