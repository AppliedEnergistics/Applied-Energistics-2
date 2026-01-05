---
navigation:
  parent: example-setups/example-setups-index.md
  title: Аметистовая ферма
  icon: minecraft:amethyst_shard
---

# Аметистовая ферма

Хотя <ItemLink id="growth_accelerator" /> работает на аметисте, обычные методы фильтрации [бутонов истинного кварца](../items-blocks-machines/budding_certus.md)
с помощью <ItemLink id="annihilation_plane" /> не работают на аметистовых бутонах. В отличие от незрелых бутонов истинного кварца, с которых выпадает
<ItemLink id="certus_quartz_dust" />, с незрелых бутонов аметиста не выпадает ничего, поэтому плоскость уничтожения будет всегда ломать их,
так как сеть не может хранить «ничего».

Обойти это можно, зачаровав плоскость уничтожения шёлковым касанием. Тогда с незрелых аметистовых бутонов *действительно* что-то выпадет
(с различных стадий физических блоков бутонов), и таким образом их можно отфильтровать.

Затем <ItemLink id="minecraft:amethyst_cluster" /> необходимо снова разместить с помощью <ItemLink id="formation_plane" />, а затем
повторно сломать с помощью <ItemLink id="annihilation_plane" /> без шёлкового касания, чтобы получить <ItemLink id="minecraft:amethyst_shard" />.

Обратите внимание, что из-за направленности друзы сплошная грань блока должна быть прямо противоположна плоскости формирования.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/amethyst_farm.snbt" />

  <BoxAnnotation color="#dddddd" min="2.7 1 1" max="3 2 2">
        (1) Плоскость уничтожения #1: без GUI для конфигурации, но с зачарованием шёлкового касания.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="2 1 1" max="2.3 2 2">
        (2) Плоскость формирования: фильтруется по аметистовой друзе.
        <ItemImage id="minecraft:amethyst_cluster" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1.3 0.7 1" max="2 1 2">
        (3) Плоскость уничтожения #2: без GUI для конфигурации, но может быть с зачарованием удачи.
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 1" max="1.3 1 2">
        (4) ME Шина хранения #1: фильтруется по аметистовому осколку.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0 0 .7" max="1 1 1">
        (5) ME Шина хранения #2: фильтруется по аметистовому осколку. Имеет более высокий приоритет, чем у вашего основного хранилища.
        <ItemImage id="minecraft:amethyst_shard" scale="2" />
  </BoxAnnotation>

<DiamondAnnotation pos="0 0.5 0.5" color="#00ff00">
        к основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* Первая <ItemLink id="annihilation_plane" /> (1) не имеет GUI и не может быть настроена, но должна быть с зачарованием шёлкового касания.
* <ItemLink id="formation_plane" /> (2) фильтруется по <ItemLink id="minecraft:amethyst_cluster" />.
* Вторая <ItemLink id="annihilation_plane" /> (3) не имеет GUI и не может быть настроена, но может быть с зачарованием удачи.
* Первая <ItemLink id="storage_bus" /> (4) фильтруется по <ItemLink id="minecraft:amethyst_shard" />.
* Вторая <ItemLink id="storage_bus" /> (5) фильтруется по <ItemLink id="minecraft:amethyst_shard" />, и имеет
  [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority) выше, чем у вашего основного хранилища.

## Как это работает

1. Первая <ItemLink id="annihilation_plane" /> пытается сломать то, что находится перед ней, но может сломать только <ItemLink id="minecraft:amethyst_cluster" />,
   потому что единственное хранилище в подсети — это <ItemLink id="formation_plane" />, отфильтрованная до аметистовой друзы. Это работает только потому, что
   плоскость с зачарованием шёлкового касания, иначе она смогла бы сломать незрелые бутоны, поскольку они ничего не дадут.
2. <ItemLink id="formation_plane" /> размещает друзу на противоположный ей блок.
3. Вторая <ItemLink id="annihilation_plane" /> ломает друзу, производя <ItemLink id="minecraft:amethyst_shard" />.
4. Первая <ItemLink id="storage_bus" /> хранит осколки в бочке. Технически, фильтрация не требуется, поскольку единственное,
   с чем должна сталкиваться вторая плоскость уничтожения, — это полностью сформировавшиеся друзы.
5. Вторая <ItemLink id="storage_bus" /> предоставляет основной сетевой доступ ко всем осколкам аметиста в бочке. Ей присвоен
   высокий [приоритет](../ae2-mechanics/import-export-storage.md#storage-priority), чтобы осколки аметиста приоритетно
   помещались обратно в бочку, а не в основное хранилище.
