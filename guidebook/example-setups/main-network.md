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

    <BoxAnnotation color="#dddddd" min="5 1 10" max="9 7 14" thickness="0.05">
        A big cluster of pattern providers and assemblers give a lot of space for crafting, stonecutting, and smithing patterns.
        The checkerboard pattern allows providers to utilize multiple assemblers in parallel while keeping it compact.
        Groups of 8 make it impossible for channels to route incorrectly.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="13 10 12" max="14 11 14" thickness="0.05">
        You don't actually need that big of a controller, all those huge rings and cubes designs you see in people's bases
        are mainly just to look cool.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="13 12 13" max="14 13 14" thickness="0.05">
        Every good network has an energy cell, to allow higher energy input per gametick and 
        smooth out power fluctuations.
    </BoxAnnotation>
    
    <BoxAnnotation color="#dddddd" min="2 1 10" max="4 4 13" thickness="0.05">
        You probably want to use some other mod's power source, a reactor or solar panel or generator or
        whatever. Vibration Chambers are ok-ish but AE2 is designed to be used in a modpack and use your 
        base's main power generator.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="15 1 9" max="16 3 14" thickness="0.05">
        Facades can be used to hide stuff behind walls
    </BoxAnnotation>
    <BoxAnnotation color="#dddddd" min="15 3 12" max="16 10 14" thickness="0.05">
        Facades can be used to hide stuff behind walls
    </BoxAnnotation>

  <IsometricCamera yaw="135" pitch="15" />
</GameScene>
