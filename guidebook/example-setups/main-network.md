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
  <ImportStructure src="../assets/assemblies/treelike_network_structure.snbt" />

    <BoxAnnotation color="#dddddd" min="3.9 0 1.9" max="9.1 5 7.1" thickness="0.05">
        A big cluster of pattern providers and assemblers give a lot of space for crafting, stonecutting, and smithing patterns.
        The checkerboard pattern allows providers to utilize multiple assemblers in parallel while keeping it compact.
        Groups of 8 make it impossible for channels to route incorrectly.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="3.9 0 9.9" max="5.1 3 12.1" thickness="0.05">
        Some machines, with a pipe subnet to push their outputs into the providers.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="-0.1 0 8.9" max="1.1 3 13.1" thickness="0.05">
      Some terminals and assorted utility doodads. (you probably want just a crafting terminal, not a regular terminal _and_ crafting terminal)
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="-0.1 0 -0.1" max="2.1 3 8.1" thickness="0.05">
      An array of crafting CPUs. A few with larger amounts of storage and a bit more with lower amounts of storage.
      You probably want to have more co-processors in an actual setup but that would be a bit large for this scene.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="5.9 0 13.9" max="7.1 1 15.1" thickness="0.05">
      Your controller should be in the middle of your base, and probably a bit larger than this. A stick-shape is pretty good.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="11.9 0 7.9" max="13.1 4 13.1" thickness="0.05">
        Various methods of doing storage, with drives or storage busses. Notice all in groups of 8.
    </BoxAnnotation>

    <BoxAnnotation color="#dddddd" min="10.9 0 0.9" max="13.1 2 7.1" thickness="0.05">
        Various methods of doing storage, with drives or storage busses. Notice all in groups of 8.
    </BoxAnnotation>

  <IsometricCamera yaw="315" pitch="30" />
</GameScene>
