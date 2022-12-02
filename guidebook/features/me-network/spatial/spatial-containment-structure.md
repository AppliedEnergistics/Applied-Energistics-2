---
navigation:
  title: Spatial Containment Structure
---

# Spatial Containment Structure

A Spatial Containment Structure or SCS, is a Multiblock networked structure
that dictates a region of space as the target of the <ItemLink
id="spatial_io_port"/>.

The structure must be an [ME network](../../me-network.md) with a number
of <ItemLink id="spatial_pylon"/> which define
the target region.

The rules for a valid SCS are,

1. A minium size of 3x3x3 ( this will capture a single block. )
2. All <ItemLink id="spatial_pylon"/> must be in the outside bounding box.
3. All <ItemLink id="spatial_pylon"/> must be either connected with cable or via a QNB, and on the same network.

This also means you can only create 1 SCS per Controller.

The Formed Status of the SCS is displayed as the color of the <ItemLink
id="spatial_pylon"/> if it is a red color, that
means the configuration is invalid, if its a light purple color, it indicates
it is valid. The status is only available if the pylons are powered, and
connected.

Most SCS will require <ItemLink
id="energy_cell"/> to power the <ItemLink
id="spatial_io_port"/>, however, these blocks
are not considered part of the SCS.

**Be aware, that you travel to a dimension without a direct way to get back.
Setup your spatial IO in a way, that you can get back.**
