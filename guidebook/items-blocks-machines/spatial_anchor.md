---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Spatial Anchor
  icon: spatial_anchor
  position: 110
item_ids:
- ae2:spatial_anchor
---
# The Spatial Anchor

<BlockImage id="spatial_anchor" p:powered="true" scale="8" perspective="up" />

An AE2 network needs to be chunkloaded for any of its [devices](../ae2-mechanics/devices.md) to be able to function, and if only some of it is loaded,
it may not function correctly. The Spatial Anchor solves this problem. It forceloads the chunks that its network occupies.
A single cable extending across a chunk border is enough to load that new chunk.

It will propagate its "loading" across [quantum bridges](quantum_bridge.md), but not cross-dimensionally, so if you
have a quantum bridge to the nether, you need a spatial anchor on the network in your base and on the network in the nether.

By default it will also enable random ticks in its loaded chunks, this can be turned off in the ae2 config.

It can be rotated with a <ItemLink id="certus_quartz_wrench" /> if for some reason you want to do that.

# Settings

- The spatial anchor provides access to the global setting to view energy in AE or E/FE.
- An in-world hologram can be displayed showing the chunks being loaded.

# Energy

The spatial anchor will use [energy](../ae2-mechanics/energy.md) according to this equation:

e = 80 + (x*(x+1))/2

where x is the number of chunks being loaded

# Recipe

<RecipeFor id="spatial_anchor" />