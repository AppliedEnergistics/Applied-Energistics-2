---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Channels
  icon: controller
---
# Channels

Applied Energistics 2's [ME Networks](../me-network.md) require
Channels to support devices which use networked storage, or other network
services. Think of channels like USB cables to all your devices. A computer only has so many USB ports and can only support
so many devices connected to it. Most devices such as standard cables, and machines can only pass through
up to 8 channels. You can think of standard cables as a bundle of 8 "channel wires". However <ItemLink id="fluix_covered_dense_cable"/> can support up
to 32 channels, the only other devices capable of transmitting 32 are <ItemLink id="me_p2p_tunnel" />
and the [Quantum Network Bridge](quantum-bridge.md). Each time a device uses up a channel, imagine pulling off a usb "wire" from
the bundle, which obviously means that "wire" isn't available further down the line.

An easy way to see how channels are being used and routed through your network is to use [smart cables](../items-blocks-machines/cables.md), which will display on them the paths and usage of channels.

Channels will consume 1⁄128 ae/t per node they transverse, this means that by
adding a <ItemLink id="controller"/> for a
network with 8 devices and over 96 nodes your power usage might actually
decrease power consumption because it changes how channels are allocated.

When using a <ItemLink id="controller"/>,
channels route via 3 steps. They first take the shortest path through adjacent machines to the nearest [normal cable](../items-blocks-machines/cables.md)
(glass, covered, or smart). They then take the shortest path through that normal cable to the nearest [dense cable](../items-blocks-machines/cables.md)
(dense or dense smart). They then take the shortest path through that dense cable to the <ItemLink id="controller"/>. 
If the shortest path is already maxed out, some devices may not get their required channels, use
colored cables, cable anchors and tunnels to your advantage to make sure your channels go in the path you desire.

Of note, **CHANNELS HAVE NOTHING TO DO WITH CABLE COLOR**, all cable color does is make cables not connect.

# Ad-Hoc Networks

A Network without a <ItemLink id="controller"/>
is considered to be Ad-Hoc, and can support up to 8 channel using devices.
Once you exceed 8 devices the networks channel using devices will shutdown,
you can either remove devices, or add a <ItemLink id="controller"/>.

Unlike with controllered networks, [smart cables](../items-blocks-machines/cables.md) on ad-hoc networks will show the number
of channels in use network-wide instead of the number of channels flowing through that specific cable.

While using [Ad-Hoc](ad-hoc-networks.md) networks each device will
use 1 channel network wide, this is very different from how <ItemLink
id="controller"/> allocate channels based on
shortest route.

# A Visual Example

WAITING UNTIL 3D SCENES ARE IMPLEMENTED

# Channel Modes

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

# Design

It's best to design your network in a treelike structure, with dense cables branching out from the controller, regular cables
branching out from the dense, and devices in clusters of 8 or fewer on the regular cables.

ILLUSTRATION IN-PROGRESS