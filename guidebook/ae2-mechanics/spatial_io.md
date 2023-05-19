---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Spatial IO
  icon: spatial_storage_cell_2
---

# Spatial IO

<GameScene zoom="4">
<ImportStructure src="../assets/assemblies/spatial_storage_1x1x1.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

Spatial IO is a way to cut-and-paste physical volumes of space in your world. It can be used to move <ItemLink id="flawless_budding_quartz" />,
have a room in your base where you can swap out various interiors to use it for different purposes, or even move
the end portal!

It works by swapping the defined volume with an identically-sized volume in the spatial storage dimension.

This means that if you have a way of travelling between dimensions (spatial IO *can* be used to make a teleporter,
but doing it is very complex, a bit janky, and beyond the scope of the guide), you can use them like custom-sized compact machines or pocket
dimensions.

# The Multiblock Setup

Spatial IO requires a specific arrangement of its components in order to function, and define the area to be cut-and-pasted.

All of the components must be on the same [network](me-network.md) in order to function, and you can have only one
spatial IO setup on a network. Thus, a [subnetwork](subnetworks.md) is recommended.

## Pylons

<BlockImage id="spatial_pylon" p:powered_on="true" scale="4" />

<ItemLink id="spatial_pylon" />s are the main part of a spatial IO setup, and define the volume to be affected. 