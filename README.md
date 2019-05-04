# Kotlin Multiplatform Sample
> showcase for a multiplatform project with an android and an ios app

[![Kotlin](https://img.shields.io/badge/kotlin-1.0.0-blue.svg)](http://kotlinlang.org)
[![Build Status](https://img.shields.io/badge/build-passing-success.svg)](https://github.com/thebino/KotlinMultiplatformSample)
[![License: CC BY-SA 4.0](https://img.shields.io/badge/License-CC%20BY--SA%204.0-green.svg)](https://creativecommons.org/licenses/by-sa/4.0/)

This showcase contains

- an android app
- an ios app 
- a rest api based on ktor

![Android screen](https://github.com/thebino/KotlinMultiplatformSample/blob/master/github/Screen01.png)
![iOS screen](https://github.com/thebino/KotlinMultiplatformSample/blob/master/github/Screen02.png)

## Features

### Common

- Rest client based on Ktor httpclient
- Logger interface with implementations for android & ios
- Coroutines dispatcher


### Android & iOS app

- showing nearby events from meetup.com on a mapbox map


## Build

Run the gradle build on a command line to create the android app & the framework for iOS
```
./gradlew build
```

To run and debug version of the android app on an android device or emulator
```
./gradlew :android:app:installDebug
```

Open the xcode project, build and run the app on a device or simulator of your choice
```
open ios/app.xcodeproj
```



## Release History

* 0.0.1
    * Rest client as common library
    * Mapbox implementation for android & iOS

## Meta

Stürmer Benjamin – [@BenjaminStrmer](https://twitter.com/BenjaminStrmer) – webmaster@stuermer-benjamin.de

Distributed under the CC-BY-SA-4.0 license. See ``LICENSE`` for more information.

## Contributing

1. Fork it (<https://github.com/thebino/KotlinMultiplatformSample>)
2. Create your feature branch (`git checkout -b Feature/fooBar`)
3. Commit your changes (`git commit -am 'Add detailed description'`)
4. Push to the branch (`git push origin Feature/fooBar`)
5. Create a new Pull Request
