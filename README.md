<p align="center">
  <a href="http://shiruka.net">
    <img src="logo/SHIRUKA.png" width="200px"/>
  </a>
</p>
<h1 align="center">Shiru ka</h1>
<p align="center">
  <strong>A fully open-source server software for Minecraft: Bedrock Edition</strong>
</p>

Shiru ka is a brand new server software for Minecraft: Bedrock Edition, similar to 
[GoMint](https://github.com/gomint/), 
[CloudburstMC](https://github.com/cloudburstmc/), 
[PowerNukkit](https://github.com/powernukkit/), 
and many others.
The following set of features makes Shiru ka unique among all others:


![master](https://github.com/shiruka/shiruka/workflows/build/badge.svg)
[![codecov](https://codecov.io/gh/shiruka/shiruka/branch/master/graph/badge.svg?token=R8GSQZLTS9)](https://codecov.io/gh/shiruka/shiruka)
[![Maintainability](https://api.codeclimate.com/v1/badges/39cc4c7bce400a705913/maintainability)](https://codeclimate.com/github/shiruka/shiruka/maintainability)
[![Hits-of-Code](https://hitsofcode.com/github/shiruka/shiruka)](https://hitsofcode.com/github/shiruka/shiruka/view)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/shiruka/shiruka/blob/master/LICENSE)
![Java 11](https://img.shields.io/badge/java-11-green)

### Contributing

`mvn clean install`

### Start Shiru ka

`java -jar shiruka.jar [options]`

#### With RAM

`java -Xms3G -Xmx3G -jar shiruka.jar [options]`

#### Shiru ka's Options

| Commands                                       | Descriptions                                            |
|------------------------------------------------|---------------------------------------------------------|
| -?, --help                                     | Show the help                                           |
| -C, --config <File: Server configuration file> | Server configuration file to use (default: shiruka.yml) |
| -D, --debug [Boolean: Debug mode]              | Debug mode to use (default: true)                       |
| -O, --ops <File: Ops file>                     | Ops file to use (default: ops.hjson)                    |
| -P, --plugins <File: Plugin directory>         | Plugin directory to use (default: plugins)              |
| -U, --usercache <File: User cache file>        | User cache file to use (default: usercache.hjson)       |
| -V, --version                                  | Show the Shiru ka's version                             |

### Translation

[Crowdin](https://crowdin.com/project/shiru-ka)
