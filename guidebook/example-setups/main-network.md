---
navigation:
  parent: example-setups/example-setups-index.md
  title: An Example "Main Network"
  icon: controller
---

# An Example "Main Network"

Many other setups reference a "Main Network". You might also ask how all these [devices](../ae2-mechanics/devices.md) come
together into a functional system. Here is an example:

<GameScene zoom="2.5" interactive={true}>
  <ImportStructure src="../assets/assemblies/small_base_network.snbt" />

    <BoxAnnotation color="#33dd33" min="5 1 10" max="9 7 14" thickness="0.05">
        A big cluster of pattern providers and assemblers give a lot of space for crafting, stonecutting, and smithing patterns.
        The checkerboard pattern allows providers to utilize multiple assemblers in parallel while keeping it compact.
        Groups of 8 make it impossible for channels to route incorrectly.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="13 10 12" max="14 11 14" thickness="0.05">
        You don't actually need that big of a controller, all those huge rings and cubes designs you see in people's bases
        are mainly just to look cool.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="13 12 13" max="14 13 14" thickness="0.05">
        Every good network has an energy cell, to allow higher energy input per gametick and 
        smooth out power fluctuations.
    </BoxAnnotation>
    
    <BoxAnnotation color="#33dd33" min="2 1 10" max="4 4 13" thickness="0.05">
        You probably want to use some other mod's power source, a reactor or solar panel or generator or
        whatever. Vibration Chambers are ok-ish but AE2 is designed to be used in a modpack and use your 
        base's main power generator.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="15 1 9" max="16 3 14" thickness="0.05">
        Facades can be used to hide stuff behind walls.
    </BoxAnnotation>
    <BoxAnnotation color="#33dd33" min="15 3 12" max="16 10 14" thickness="0.05">
        Facades can be used to hide stuff behind walls.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="13 9 7" max="14 10 9" thickness="0.05">
        You don't need that many drive bays and cells for your general storage, 2-4 Drives worth of 4k or 16k
        cells is almost always enough.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="13 9 10" max="14 11 11" thickness="0.05">
        For bulk storage, you want big cells filtered to specific items, in separate drives set to a higher
        priority.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="10 9 13" max="11.7 13 14" thickness="0.05">
        Interface-based auto-stocking.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="6 10 12" max="9 12 15" thickness="0.05">
        The logical expansion of the charger automation setup to multiple chargers.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="2 10 12" max="5 11 15" thickness="0.05">
        Another way to automate processors, since inscribers can now auto-eject the output in 1.20.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="3 10 10" max="4 12 11" thickness="0.05">
        Another way to automate processors, since inscribers can now auto-eject the output in 1.20.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="7.2 9.2 8.2" max="7.8 10 8.8" thickness="0.05">
        The wireless access point is in the middle because its range is spherical.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="14 1 2" max="16 5 7" thickness="0.05">
        Typically you'll have 1-2 big crafting CPUs for big jobs and a few smaller ones to handle secondary
        jobs while the big CPUs are busy.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="5 3 6" max="6 4 7" thickness="0.05">
        Sometimes subnets might need their own controller if there are more than 8 devices (like distributing to more
        than 8 places).
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="7.3 1 3.3" max="9.7 4 6" thickness="0.05">
        Certus farm.
    </BoxAnnotation>

    <BoxAnnotation color="#33dd33" min="10.3 1 2.3" max="12.7 3.7 5" thickness="0.05">
        Throwing-In-Water automation.
    </BoxAnnotation>

  <IsometricCamera yaw="135" pitch="15" />
</GameScene>
