---
navigation:
  title: Sandbox
  position: 0
---

# Sandbox

<GameScene zoom="4" interactive="true">
    <Block id="minecraft:stone" x="2" />
    <Block id="minecraft:stone" x="-2" />
    <Block id="minecraft:stone" z="2" />
    <Block id="minecraft:stone" z="-2" />
    <BoxAnnotation color="#336633" x1="-2" x2="1.5" y1="0" y2="0.5" z1="-0.5" z2="1.25">
        *Hello World this is a very* long tooltip with long lines and more stuff about
        whatever you are hovering **your** mouse over!

        <GameScene zoom="2">
              <ImportStructure src="assets/assemblies/ore_fortuner.snbt" />
        </GameScene>
    </BoxAnnotation>
    <DiamondAnnotation x="2.5" y="0.5" z="0.5" color="#00ff00">
        This is very special stone.
    </DiamondAnnotation>
    <DiamondAnnotation z="2.5" y="0.5" x="0.5" color="#3333ff">
        This is very boring stone.
    </DiamondAnnotation>
</GameScene>
