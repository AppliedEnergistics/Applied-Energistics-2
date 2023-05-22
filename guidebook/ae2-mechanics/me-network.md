---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: "Network"
  icon: fluix_glass_cable
---

# What Does "Network" Mean?

A "Network" is a group of [devices](../ae2-mechanics/devices.md) linked by blocks that can pass [channels](../ae2-mechanics/channels.md),
like [cables](../items-blocks-machines/cables.md) or fullblock machines and [devices](../ae2-mechanics/devices.md). 
(<ItemLink id="charger" />, <ItemLink id="interface" />, <ItemLink id="drive" />, etc.)
Technically a single cable is a network, actually.

An easy way of determining what's connected in a network

For example, this is 2 separate networks.

<GameScene zoom="6">
  <ImportStructure src="../assets/assemblies/2_networks_1.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

This is also 2 separate networks, because the <ItemLink id="quartz_fiber" /> shares [energy](../ae2-mechanics/energy.md)
without providing a network connection.

<GameScene zoom="6">
  <ImportStructure src="../assets/assemblies/2_networks_2.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

However, this is just 1 network, not 2 separate ones. The [quantum bridge](../items-blocks-machines/quantum_bridge.md) acts like
a wireless [dense cable](../items-blocks-machines/cables.md#dense-cable), so both ends are on the same network.

<GameScene zoom="4">
  <ImportStructure src="../assets/assemblies/actually_1_network.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>