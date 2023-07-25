---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Quantum Bridge
  icon: quantum_ring
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:quantum_link
- ae2:quantum_ring
---

# The Quantum Network Bridge

![A formed Quantum Network Bridge](../assets/diagrams/quantum_bridge_demonstration.png)

Quantum Network Bridges can extend a [network](../ae2-mechanics/me-network-connections.md) over infinite distances and even between dimensions.
They can carry 32 channels in total (regardless of how cables are connected to each face), essentially
acting like a wireless [dense cable](cables.md#dense-cable).

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/quantum_bridge_internal_structure_1.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/quantum_bridge_internal_structure_2.snbt" />

  <BoxAnnotation color="#33dd33" min="1 1 1" max="6 2 3">
        An imaginary cable between the two endpoints
  </BoxAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Of note, **both sides must be chunkloaded** so a <ItemLink id="spatial_anchor" /> or other chunkloader must be used
if the 2 sides are far apart.

# Quantum Ring

<BlockImage id="quantum_ring" scale="8" />

Eight of these blocks placed around a <ItemLink id="quantum_link" /> will create a
Quantum Network Bridge. Only the 4 <ItemLink id="quantum_ring" /> blocks adjacent to
the <ItemLink id="quantum_link" /> will accept network connections,
the 4 corner blocks cannot connect to cables.

## Recipe

<RecipeFor id="quantum_ring" />

# Quantum Link Chamber

<BlockImage id="quantum_link" scale="8" />

One of these blocks surrounded by a <ItemLink id="quantum_ring" />
will create a Quantum Network Bridge. This block doesn't connect to any cables and only registers
as part of the network with the full bridge is made.

This block's inventory can only hold a single <ItemLink id="quantum_entangled_singularity" /> and is
automation accessible.

## Recipe

<RecipeFor id="quantum_link" />
