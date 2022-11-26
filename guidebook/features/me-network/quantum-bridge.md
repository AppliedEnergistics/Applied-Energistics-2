---
navigation:
  title: Quantum Bridge
  icon: singularity
item_ids:
  - ae2:quantum_link
  - ae2:quantum_ring
  - ae2:quantum_entangled_singularity
---

![A formed Quantum Network Bridge](../../large/qnb.png)

_Quantum Network Bridges_ can connect two networks over infinite distances and even between dimensions.
They can carry 32 channels in total (regardless of how cables are connected to each face).

## Quantum Ring

Eight of these blocks placed around a <ItemLink id="quantum_link"/> will create a
_Quantum Network Bridge_. Only the 4 <ItemLink id="quantum_ring"/> blocks adjacent to
the <ItemLink id="quantum_link" /> will accept network connections,
the 4 corner blocks cannot connect to cables.

<RecipeFor id="quantum_ring" />

## Quantum Link Chamber

One of these blocks surrounded by a <ItemLink id="quantum_ring"/>
will create a _Quantum Network Bridge_. This block doesn't connect to any cables and only registers
as part of the network with the full bridge is made.

This blocks inventory can only hold a single <ItemLink id="quantum_entangled_singularity"/> and is
automation accessible.

<RecipeFor id="quantum_link" />

## Quantum Entangled Singularity

Required to create a connection between to _Quantum Network Bridges_, they are always produced in matching
pairs, to create a connection place 1 of the pair of <ItemLink
id="quantum_entangled_singularity"/> into the <ItemLink id="quantum_link" /> of
the bridge on each side.

They are crafted by causing a reaction between <ItemLink id="minecraft:ender_pearl"/> or <ItemLink id="ender_dust"/>  
and a <ItemLink id="singularity"/>. Any explosive force should be enough to trigger the reaction.

**_Nearly any explosion - even creepers - will work._**

Always produced in pairs, but only require a single <ItemLink id="singularity"/>.

It might be a good idea to label these with names when you create them using the vanilla anvil.

### Note for Anti Griefing Servers

AE also includes a block called <ItemLink
id="tiny_tnt"/>, this is a small craftable TNT
which can have its block damage disabled, but can still hurt a little, and
can be used as an alternative to vanilla tnt / other explosions even when
block damage is disabled.
