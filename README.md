# Applied Energistics 2

## Table of Content

* [About](#about)
* [Contacts](#contacts)
* [License](#license)
* [Downloads](#downloads)
* [Installation](#installation)
* [Building](#building)
* [Contribution](#contribution)
* [Credits](#credits)

## About

A Mod about Matter, Energy and using them to conquer the world..

## Contacts

* [Website](http://ae-mod.info/)
* [IRC #appliedenergistics on esper.net](http://webchat.esper.net/?channels=appliedenergistics&prompt=1)
* [GitHub](https://github.com/AppliedEnergistics/Applied-Energistics-2)

## License

Applied Energistics 2 is (c) 2013 - 2014 AlgorithmX2 and licensed under LGPL v3. See the LICENSE.txt for details or go to http://www.gnu.org/licenses/lgpl-3.0.txt for more information.

## Downloads

Downloads can be found on [CurseForge](http://www.curse.com/mc-mods/minecraft/223794-applied-energistics-2) or on the [official website](http://ae-mod.info/Downloads/).

## Installation

You install this mod by putting it into the `minecraft/mods/` folder. It has no additional hard dependencies.

## Building

1. Clone this repository via 
  - SSH `git clone --recursive git@github.com:AppliedEnergistics/Applied-Energistics-2.git` or 
  - HTTPS `git clone --recursive https://github.com/AppliedEnergistics/Applied-Energistics-2.git`
  - Note the `--recursive` option. This enables to automatically clones of all submodules. AE2 uses the [AE2-API](https://github.com/AlgorithmX2/Applied-Energistics-2-API) and [AE2-Lang](https://github.com/AppliedEnergistics/AppliedEnergistics-2-Localization) repositories. 
2. Extract 3rd party APIs (better solution coming soon)
  - http://ae-mod.info/assets/CompileDeps.zip
  - Extract to the project root
3. Setup workspace 
  - Decompiled source `gradlew setupDecompWorkspace`
  - Obfuscated source `gradlew setupDevWorkspace`
  - CI server `gradlew setupCIWorkspace`
4. Setup IDE
  - IntelliJ: Import into IDE and execute `gradlew genIntellijRuns` afterwards
  - Eclipse: execute `gradlew eclipse`
5. Build `gradlew build`. Jar will be in `build/libs`
6. (In order to have FML detect AE from your dev environment, add the following VM Option to your run profile `-Dfml.coreMods.load=appeng.transformer.AppEngCore` TODO)

## Contribution

Before you want to add major changes, you might want to discuss them with us first, before wasting your time.
If you are still willing to contribute to this project, you can contribute via [Pull-Request](https://help.github.com/articles/creating-a-pull-request).

1. Fork this repository
2. Clone the fork via
  * SSH `git clone git@github.com:<your username>/Applied-Energistics-2.git` or 
  * HTTPS `git clone https://github.com/<your username>/Applied-Energistics-2.git`
3. Change code base
4. Add changes to git `git add -A`
5. Commit changes to your clone `git commit -m "<summery of made changes>"`
6. Push to your fork `git push`
7. Create a Pull-Request on GitHub

If you are only doing single file pull requests, GitHub supports using a quick way without the need of cloning your fork.

## Credits

Thanks to
 
* Notch et al for Minecraft
* Lex et al for MinecraftForge
* AlgorithmX2 for AppliedEnergistics2
* all [contributors](https://github.com/AppliedEnergistics/Applied-Energistics-2/graphs/contributors) helping making this mod.
