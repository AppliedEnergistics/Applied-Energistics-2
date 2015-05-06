[![Latest Build](https://img.shields.io/teamcity/http/ci.tsr.me/s/AppliedEnergistics_AutoHeadBuild.svg?label=Latest Build)](http://ci.tsr.me/viewType.html?buildTypeId=AppliedEnergistics_AutoHeadBuild&tab=buildTypeStatusDiv)
[![Latest Tag](https://img.shields.io/github/tag/AppliedEnergistics/Applied-Energistics-2.svg?label=Latest Tag)](https://github.com/AppliedEnergistics/Applied-Energistics-2/tags)
[![Latest Release](https://img.shields.io/github/release/AppliedEnergistics/Applied-Energistics-2.svg?label=Latest Release)](https://github.com/AppliedEnergistics/Applied-Energistics-2/releases)

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
* [API](#applied-energistics-2-api)
* [Localization](#applied-energistics-2-localization)
* [Credits](#credits)

## About

A Mod about Matter, Energy and using them to conquer the world..

## Contacts

* [Website](http://ae-mod.info/)
* [IRC #appliedenergistics on esper.net](http://webchat.esper.net/?channels=appliedenergistics&prompt=1)
* [GitHub](https://github.com/AppliedEnergistics/Applied-Energistics-2)

## License

* Applied Energistics 2 API
  - (c) 2013 - 2015 AlgorithmX2 et al
  - ![License](https://img.shields.io/badge/License-MIT-red.svg?style=flat)
* Applied Energistics 2
  - (c) 2013 - 2015 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-LGPLv3-blue.svg?style=flat)](https://raw.githubusercontent.com/AppliedEnergistics/Applied-Energistics-2/rv2/LICENSE)
* Textures and Models
  - (c) 2013 - 2015 AlgorithmX2 et al
  - [![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%203.0-yellow.svg?style=flat)](https://creativecommons.org/licenses/by-nc-sa/3.0/)
* Text and Translations
  - ![License](https://img.shields.io/badge/License-No%20Restriction-green.svg?style=flat)

## Downloads

Downloads can be found on [CurseForge](http://www.curse.com/mc-mods/minecraft/223794-applied-energistics-2) or on the [official website](http://ae-mod.info/Downloads/).

## Installation

You install this mod by putting it into the `minecraft/mods/` folder. It has no additional hard dependencies.

## Issues

Applied Energistics 2 crashing, have a suggestion, found a bug?  Create an issue now!

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
  - SSH `git clone git@github.com:AppliedEnergistics/Applied-Energistics-2.git` or 
  - HTTPS `git clone https://github.com/AppliedEnergistics/Applied-Energistics-2.git`
2. Setup workspace 
  - Decompiled source `gradlew setupDecompWorkspace`
  - Obfuscated source `gradlew setupDevWorkspace`
  - CI server `gradlew setupCIWorkspace`
3. Build `gradlew build`. Jar will be in `build/libs`
4. For core developer: Setup IDE
  - IntelliJ: Import into IDE and execute `gradlew genIntellijRuns` afterwards
  - Eclipse: execute `gradlew eclipse`
5. For add-on developer: Core-Mod Detection
  - In order to have FML detect AE from your dev environment, add the following VM Option to your run profile
  - `-Dfml.coreMods.load=appeng.transformer.AppEngCore`

## Contribution

Before you want to add major changes, you might want to discuss them with us first, before wasting your time.
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
8. Wait for review
9. Squash commits for cleaner history

If you are only doing single file pull requests, GitHub supports using a quick way without the need of cloning your fork. Also read up about [synching](https://help.github.com/articles/syncing-a-fork) if you plan to contribute on regular basis.

## Applied Energistics 2 API

The API for Applied Energistics 2. It is open source to discuss changes, improve documentation, and provide better add-on support in general.

Development and standard builds can be obtained [Here](http://ae2.ae-mod.info/Downloads/).

### Maven

When compiling against the AE2 API you can use gradle dependencies, just add

    dependencies {
	    compile "appeng:appliedenergistics2:rv_-_____-__:dev"
    }

or add the compile line to your existing dependencies task to your build.gradle

Where the __ are filled in with the correct version criteria; AE2 is available from the default forge maven so no additional repositories are necessary.

An example string would be `appeng:appliedenergistics2:rv2-alpha-30:dev`

## Applied Energistics 2 Localization

### English Text

`en_US` is included in this repository, fixes to typos are welcome.

### Encoding

Files must be encoded as UTF-8.

### New Translations

You can provide any additional languages by creating a new file with the [appropriate language code](http://download1.parallels.com/SiteBuilder/Windows/docs/3.2/en_US/sitebulder-3.2-win-sdk-localization-pack-creation-guide/30801.htm).

### Final Note

If you have have issues localizing something feel free to contact us on IRC, at #AppliedEnergistics on Esper.net

Thanks to everyone helping out to improve localization of AE2.

## Credits

Thanks to
 
* Notch et al for Minecraft
* Lex et al for MinecraftForge
* AlgorithmX2 for AppliedEnergistics2
* all [contributors](https://github.com/AppliedEnergistics/Applied-Energistics-2/graphs/contributors)
