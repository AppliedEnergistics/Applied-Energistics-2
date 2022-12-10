---
categories:
  - ME Network/Misc
item_ids:
  - ae2:me_p2p_tunnel
navigation:
  parent: website/index.md
  title: P2P Tunnel
---

The <ItemLink id="me_p2p_tunnel" /> or "Point to Point Tunnel" is
a versatile configurable system to move items / redstone / power / and fluids from
one location to another though an existing [ME Network](../../me-network.md) without
storage.

<RecipeFor id="me_p2p_tunnel" />

Tunnels are 1 input to N outputs. This means you can output to as many points
as you want, but only input at a single point per tunnel.

Networks can support any number of tunnels, of any different types, and they
all function independently.

ME Tunnels can be used to carry channels from one location to another, and can
carry up to 32 [channels](../channels.md), same as a <ItemLink
id="fluix_covered_dense_cable"/>, while only
requiring a single channel per point, making tunnels a very powerful tool to
expand [me networks](../../me-network.md), especially over a distance.

The channel required by a P2P-Tunnel cannot be carried through another P2P-Tunnel.

To configure a <ItemLink id="me_p2p_tunnel"/>
you must first attune the tunnel to carry what you want it to (see below), then you need
to configure the outputs to their input. You configure the connections by
using the <ItemLink id="memory_card"/>; First
Shift+Right Click the input to save it on your memory card, then simply right-click the different outputs to
store the input onto the outputs. this also sets the type of the output to match the type of the input.

## Tunnel Types

<P2PTunnelTypes />
