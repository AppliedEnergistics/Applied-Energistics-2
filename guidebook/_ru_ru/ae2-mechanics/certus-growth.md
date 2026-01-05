---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Выращивание истинного кварца
  icon: quartz_cluster
---

# Выращивание истинного кварца

## По сути, просто скопировано с начальной страницы

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/budding_certus_1.snbt" />
</GameScene>

Бутоны истинного кварца прорастают из [цветущих блоков истинного кварца](../items-blocks-machines/budding_certus.md), подобно аметисту. Если сломать бутон,
который ещё не закончил рост, из него выпадет одна <ItemLink id="certus_quartz_dust" />, не смотря на зачарование удачи. Если сломать полностью выросший кластер, из него выпадет
в количестве четырёх штук <ItemLink id="certus_quartz_crystal" />, и зачарование удачи увеличит это число.

Существует 4 уровня цветущих блоков истинного кварца: безупречный, испорченный, потрескавшийся, и повреждённый.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/budding_blocks.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Каждый раз, когда бутон переходит на следующую стадию, у цветущего блока есть шанс деградировать на один уровень, в конечном итоге превратившись в
обычный блок истинного кварца. Их можно починить (и создать новые цветущие блоки), бросив цветущий блок (или
блок истинного кварца) в воду с одним или несколькими <ItemLink id="charged_certus_quartz_crystal" />.

<RecipeFor id="damaged_budding_quartz" />

Безупречные цветущие блоки истинного кварца не деградируют и будут генерировать истинный кварц бесконечно. Однако их нельзя создать или переместить
киркой, даже с помощью зачарования шёлкового касания. (хотя их *можно* переместить с помощью [пространственного хранилища](../ae2-mechanics/spatial-io.md))

Сами по себе бутоны истинного кварца растут очень медленно. К счастью, <ItemLink id="growth_accelerator" /> значительно
ускоряет этот процесс, если разместить его рядом с цветущим блоком. Вам следует в первую очередь построить несколько из них в качестве своей первоочерёдной задачи.

<GameScene zoom="4" background="transparent">
  <ImportStructure src="../assets/assemblies/budding_certus_2.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Если у вас недостаточно кварца для создания <ItemLink id="energy_acceptor" /> или <ItemLink id="vibration_chamber" />,
вы можете создать <ItemLink id="crank" /> и прикрепить его к концу вашего ускорителя роста.

Автоматический сбор кварца [описан здесь](../example-setups/simple-certus-farm.md).