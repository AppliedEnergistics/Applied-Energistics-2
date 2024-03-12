---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Network Tool
  icon: network_tool
  position: 410
categories:
- tools
item_ids:
- ae2:network_tool
---

# Network Tool

<ItemImage id="network_tool" scale="4" />

The Network Tool is a modified [wrench](wrench.md) that also shows network diagnostic information and can store [upgrade cards](upgrade_cards.md).
While it retains the wrench's ability to quickly disassemble things and pull [subparts](../ae2-mechanics/cable-subparts.md)
off of a cable, it cannot rotate things.

It has 9 slots in which to store [upgrade cards](upgrade_cards.md), and they will be available in any AE2 device UI if the tool
is anywhere in your inventory.

Right-clicking any part of a network will show a diagnostic info window, similar to right-clicking a <ItemLink id="controller" />.
This window shows

*   The amount of channels in-use on the network
*   A toggle for the global setting to view energy in AE or E/FE
*   The amount of [energy](../ae2-mechanics/energy.md) stored in the network, and the network's maximum energy capacity
*   The amount of energy entering and being used by the network
*   A list of all the [devices](../ae2-mechanics/devices.md) and components on the network

This window is also helpful for figuring out if two different cables or devices are part of the same network when messing around with
[Subnetworks](../ae2-mechanics/subnetworks.md).

## Hiding Facades

<a href="facades.md">Facades</a> will be hidden while holding a network tool in either hand.

You can interact with blocks behind hidden facades without having to remove the facades first.

## Recipe

<RecipeFor id="network_tool" />
