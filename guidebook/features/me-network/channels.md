---
navigation:
  title: Channels
  icon: controller
---

Applied Energistics 2's [ME Networks](../me-network.md) require
Channels to support devices which use networked storage, or other network
services. Most devices such as standard cables, and machines can only support
up to 8 channels. However <ItemLink id="fluix_covered_dense_cable"/> can support up
to 32 channels, the only other devices capable of transmitting 32 are <ItemLink id="me_p2p_tunnel" />
and the [Quantum Network Bridge](quantum-bridge.md).

A Network without a <ItemLink id="controller"/>
is considered to be Ad-Hoc, and can support up to 8 channel using devices.
Once you exceed 8 devices the networks channel using devices will shutdown,
you can either remove devices, or add a <ItemLink id="controller"/>.

While using [Ad-Hoc](ad-hoc-networks.md) networks each device will
use 1 channel network wide, this is very different from how <ItemLink
id="controller"/> allocate channels based on
shortest route.

Channels will consume 1⁄128 ae/t per node they transverse, this means that by
adding a <ItemLink id="controller"/> for a
network with 8 devices and over 96 nodes your power usage might actually
decrease power consumption because it changes how channels are allocated.

When using a <ItemLink id="controller"/>
Channels must route via the shortest path from the <ItemLink
id="controller"/> to the device. If the path is
already maxed out, some devices may not get their required channels, use
colored cables, cable anchors and tunnels to your advantage to make sure your
channels go in the path you desire.

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
| `infinite` | All channel restrictions are removed. Controllers still reduce the power consumption of grids _significantly_. Smart cables will only toggle between completely off (no channels carried) and completely on (1 or more channels carried). |

## Design

Designing your layouts with channels can be tricky because of their shortest
route nature, if any specific spot in your system has two possible routes, you
may find yourself returning home from a mining trip to see half your devices
offline. Take a look at the following example:

![Diagram showing that two equal length paths are bad.](../../assets/channels/badLength.png)

---

Equal Length Route

In the above image the controller is represented by the Green Block, Cables or
machines by green lines. The blue square indicate which Locations only have 1
route; this is good, but there is a red block, which indicates that there is
two possible routes, this can be bad, especially if your exceeding 8 channels
on cable, or machines for a specific block of machines. Now that you can
understand that basic issue and diagram look at these other diagrams.

<div className="tile"><div className="tile">

![An example of a good layout](../../assets/channels/good_split.png)

</div><div className="tile">

![An example of a bad layout](../../assets/channels/bad_split3.png)

</div><div className="tile">

![An example of a bad layout](../../assets/channels/bad_split.png)

</div><div className="tile">

![An example of a bad layout](../../assets/channels/bad_split2.png)

</div></div>

You can see that depending on how you run your cable, you might end up with
different possible outcomes in a block of machines, you can also see that
using a controller you can ensure that the channels equilibrium is kept from a
straight line.

In the second setup you can see that the middle line is red, however its
important to remember that it only matters if that line of machines uses
channels, if that line was for instance molecular assemblers, it wouldn't
matter, so that could be a valid setup for building.

In the Last two you can see that you might run an extra cable into a block of
machines, and it might appear to work, but you can see that it can break quite
easily.

Now that you understand how this works, I'll leave you with one final piece of
helpful information, if you run into a situation where you can't use a
controller, and your design is imbalanced, consider using p2p tunnels, since a
tunnel connection is considered a single "hop" you can get the system to have
a different outcome.

![Diagram showing how to fix a previous setup with a p2p tunnel.](../../assets/channels/p2psplit.png)

## Using P2P-Tunnels to adjust route lengths

One last important note about this, you can see that the p2p tunnel is
directly on the controller, and directly on the block of machines at the
bottom of the setup, this is done because the in and out tunnel are both
considered "a node", so the two cable from the controller and in and out
tunnel balance to create the final balanced setup.
