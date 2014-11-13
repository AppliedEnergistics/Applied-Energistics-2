# Applied Energistics 2

## Table of Contents

* [About](#about)
* [Contacts](#contacts)
* [License](#license)
* [Downloads](#downloads)
* [Installation](#installation)
* [Issues](#issues)
* [Building](#building)
* [Contribution](#contribution)
* [Credits](#credits)

## About

A Mod about Matter, Energy and using them to conquer the world.

## Contacts

* [Website](http://ae-mod.info/)
* [IRC #appliedenergistics on esper.net](http://webchat.esper.net/?channels=appliedenergistics&prompt=1)
* [GitHub](https://github.com/AppliedEnergistics/Applied-Energistics-2)

## License

Applied Energistics 2 is (c) 2013 - 2014 AlgorithmX2 and licensed under LGPL v3. See the LICENSE.txt for details or go to http://www.gnu.org/licenses/lgpl-3.0.txt for more information. Textures and Models are licensed under Creative Commons 3.

## Downloads

Downloads can be found on [CurseForge](http://www.curse.com/mc-mods/minecraft/223794-applied-energistics-2) or on the [official website](http://ae-mod.info/Downloads/).

## Installation

You can install this mod by putting the .jar file into the `minecraft/mods/` folder after you install Minecraft Forge. It has no additional hard dependencies.

## Issues

Applied Energistics 2 crashing, have a suggestion, or found a bug?  Create an issue now!

1. Make sure your issue hasn't already been answered or fixed.  Also think about whether your issue is a valid one before submitting it.
2. Go to [the issues page](https://github.com/AppliedEnergistics/Applied-Energistics-2/issues)
3. Click new issue
4. Enter your Issue's title (something that summarizes your issue), and then create a detailed description of the issue.
	* To help in resolving your issues please try to include the follow if applicable:
		* The problem that is happening?
		* What was expected?
		* How to reproduce the problem?
		* Server or Single Player?
		* Mod Pack using and version?
		* Crash log (Please make sure to use [pastebin](http://pastebin.com/) for the log file) 
		* Screen shots or Pictures of the problem
5. Click `Submit New Issue`, and wait for feedback!

## Building

1. Clone this repository via 
  - SSH `git clone --recursive git@github.com:AppliedEnergistics/Applied-Energistics-2.git` or 
  - HTTPS `git clone --recursive https://github.com/AppliedEnergistics/Applied-Energistics-2.git`
  - Note the `--recursive` option. This enables to automatically clones of all submodules. AE2 uses the [AE2-API](https://github.com/AlgorithmX2/Applied-Energistics-2-API) and [AE2-Lang](https://github.com/AppliedEnergistics/AppliedEnergistics-2-Localization) repositories. 
2. Setup workspace 
  - Decompiled source `gradlew setupDecompWorkspace`
  - Obfuscated source `gradlew setupDevWorkspace`
  - CI server `gradlew setupCIWorkspace`
3. Setup IDE
  - IntelliJ: Import into IDE and execute `gradlew genIntellijRuns` afterwards
  - Eclipse: execute `gradlew eclipse`
4. Build `gradlew build`. Jar will be in `build/libs`
5. (In order to have FML detect AE from your dev environment, add the following VM Option to your run profile `-Dfml.coreMods.load=appeng.transformer.AppEngCore` TODO)

## Contribution

Before you want to add any major changes, you might want to discuss them with us first, before wasting your time.
If you are still willing to contribute to this project, you can contribute via [Pull-Request](https://help.github.com/articles/creating-a-pull-request).

Here are a few things to keep in mind that will help get your PR approved.

* A PR should be focused on content. Any PRs where the changes are only syntax will be rejected.
* Use the file you are editing as a style guide.
* Consider your feature. [Suggestion Guidelines](http://ae-mod.info/Suggestion-Guidelines/)
  - Is your suggestion already possible using Vanilla + AE2?
  - Make sure your feature isn't already in the works, or hasn't been rejected previously.
  - Does your feature simplify another feature of AE2? These changes will not be accepted.
  - If your feature can be done by any popular mod, discuss with us first.

Getting Started

1. Fork this repository
2. Clone the fork via
  * SSH `git clone git@github.com:<your username>/Applied-Energistics-2.git` or 
  * HTTPS `git clone https://github.com/<your username>/Applied-Energistics-2.git`
3. Change code base
4. Add changes to git `git add -A`
5. Commit changes to your clone `git commit -m "<summery of made changes>"`
6. Push to your fork `git push`
7. Create a Pull-Request on GitHub

If you are only doing single file pull requests, GitHub supports using a quick way without the need of cloning your fork. Also read up about [syncing](https://help.github.com/articles/syncing-a-fork) if you plan on contributing on a regular basis.

## Credits

Thanks to
 
* Notch et al for Minecraft
* Lex et al for MinecraftForge
* AlgorithmX2 for Applied Energistics 2
* All [contributors](https://github.com/AppliedEnergistics/Applied-Energistics-2/graphs/contributors) helping to make this mod the best that it can be.
