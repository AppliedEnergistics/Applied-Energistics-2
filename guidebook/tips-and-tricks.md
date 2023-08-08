---
navigation:
  title: Tips and Tricks
  position: 20
---

# Tips and Tricks

A load of random little recommendations

* Remove Optifine
* You can rotate and zoom into guidebook scenes that have the zoom and annotation hide/show buttons
* Keep your network treelike and avoid loops
* Full-block [devices](ae2-mechanics/devices.md) in groups of 8 or less unless you deeply understand how [channels](ae2-mechanics/channels.md)
  route through a network
* Pick a wood and stick with it for all your [patterns](items-blocks-machines/patterns.md). Yes, enabling substitutions
  in patterns sometimes works, but using the same wood type everywhere greatly reduces hassle.
* Arrange your [patterns](items-blocks-machines/patterns.md) vertically in the <ItemLink id="pattern_access_terminal" />/
  distribute your patterns between your [providers](items-blocks-machines/pattern_provider.md) so that recipes can be performed in parallel.
* Add an [energy cell](items-blocks-machines/energy_cells.md) so that your network can handle power spikes.
* You can use water in the <ItemLink id="condenser" />
* The best way to keep your network clean is to not put random mob loot like swords and armor in. Each unique combination of
  enchantment and durability is another [type](ae2-mechanics/bytes-and-types.md).
* An "item entering system" event must occur when returning the result of a [processing pattern](items-blocks-machines/patterns.md),
  like through an <ItemLink id="import_bus" />, <ItemLink id="interface" />, or <ItemLink id="pattern_provider" /> return slot,
  you can't just pipe the result into a chest with a <ItemLink id="storage_bus" /> on it.
* Don't forget that can rotate and zoom into guidebook scenes that have the zoom and annotation hide/show buttons
* The <ItemLink id="pattern_provider" /> will only push complete recipe batches and only through a single side. This is useful
  for making sure machines don't get partial batches, but sometimes you want the ingredients to go to multiple places.
  You can achieve this using an <ItemLink id="interface" />, either as a ["pipe" subnet](example-setups/pipe-subnet.md) or using
  its ability to hold multiple different item stacks, fluids, chemicals, etc. all at once, to use it as a sort of intermediate chest/tank.
* You can zoom and rotate guidebook scenes that have the zoom and annotation hide/show buttons