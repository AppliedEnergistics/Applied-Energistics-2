---
navigation:
  parent: example-setups/example-setups-index.md
  title: Излучатель уровня автозаполнения
  icon: level_emitter
---

# Излучатель уровня автозаполнения

Можно задать вопрос: «Как мне сохранить определённое количество предметов в хранении, создавая их по мере необходимости?»

Одним из решений является использование <ItemLink id="export_bus" />, <ItemLink id="level_emitter" />, и <ItemLink id="crafting_card" /> для автоматического запроса новых предметов
из [автоматического создания](../ae2-mechanics/autocrafting.md) вашей сети. Такая установка предназначена для хранения большого количества одного предмета.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/level_emitter_autostocking.snbt" />

  <BoxAnnotation color="#dddddd" min="1 1 0" max="2 1.3 1">
        (1) ME Шина экспорта: фильтруется по нужному предмету. Имеет редстоуновую карту и карту создания. Режим редстоуна установлен на
        «активен при наличии сигнала», режим создания установлен на «не использовать имеющиеся в наличии предметы».
        <Row><ItemImage id="redstone_card" scale="2" /> <ItemImage id="crafting_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0.7 1 0" max="1 2 1">
        (2) Излучатель уровня: настроено с указанием нужного предмета и количества, установлено значение «выдавать при уровне ниже лимита».
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (3) Интерфейс: в конфигурации по умолчанию.
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        К основной сети
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Конфигурации

* <ItemLink id="export_bus" /> (1) фильтруется по нужному предмету. У неё есть <ItemLink id="redstone_card" /> и <ItemLink id="crafting_card" />.
  Режим редстоуна установлен на «активен при наличии сигнала», поведение создания — на «не использовать имеющиеся в наличии предметы».
* <ItemLink id="level_emitter" /> (2) настроен на нужный предмет и количество, а также на «выдавать при уровне ниже лимита».
* <ItemLink id="interface" /> (3) имеет конфигурацию по умолчанию.

## Как это работает

1. Если количество нужного предмета в [сетевом хранилище](../ae2-mechanics/import-export-storage.md) меньше количества, указанного в
   <ItemLink id="level_emitter" />, он подаст сигнал редстоуна.
2. При получении сигнала редстоуна (и поскольку <ItemLink id="crafting_card" /> настроена на отключение использования имеющихся предметов),
   <ItemLink id="export_bus" /> запросит у сети [автоматическое создание](../ae2-mechanics/autocrafting.md)
   большого количества нужного предмета, а затем экспортирует его.
3. После помещения в него предмета (и при отсутствии настроек, позволяющих хранить что-либо во внутреннем инвентаре), <ItemLink id="interface" /> помещает этот предмет в сетевое хранилище.