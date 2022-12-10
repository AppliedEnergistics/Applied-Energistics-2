---
navigation:
  parent: website/index.md
  title: Ad Hoc Networks
---

# Ad Hoc Networks

Ad-Hoc networks are small [ME Networks](../me-network.md) that do not have an <ItemLink id="controller" />.
They can have up to 8 [channels](channels.md) using devices.

You can use them as small stand alone systems, or as systems designed to
enhance a larger [ME Network](../me-network.md), generally they are
powered via <ItemLink id="quartz_fiber"/>
however they can also be powered via a <ItemLink id="energy_acceptor"/> or even an energy cell if
you don't want to keep it running for extend periods of time.

Smart Cables on Ad-Hoc networks will show the channel usage for every device on
the network at all points on the network, this is different from how they will
show usage if you are using a <ItemLink id="controller"/>.

Once an ad-hoc network exceeds 8 devices, the network will be unable to
allocate channels and everything will shutdown, you will either need to remove
devices, or install a <ItemLink id="controller"/> and to convert it to a
standard network, instead of an ad-hoc network.
