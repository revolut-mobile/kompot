# Kompot

![wallpaper](https://user-images.githubusercontent.com/17656589/171048602-3c8b83c1-4dec-419d-9435-484741458aa8.png)

Kompot is a framework for building android apps. It gives you a project setup with unidirectional data flow, navigation and multi-module support from the box, letting you concentrate on what's most important - building apps that users love.

With Kompot, you don't need to bother about the right way of calling fragment transactions or building tons of intents for Activities. You call your screens directly by giving them the desired input in the constructors, and the framework will do the job of bringing your UI to the user's attention

![wave_demo](https://user-images.githubusercontent.com/17656589/171048667-ecf071ca-2962-4c8a-8639-bb9a3ca289bf.gif)

# How to use Kompot?

## Installation

Kompot is hosted on `mavenCentral`. In order to fetch the dependency, add the following lines to your project level `build.gradle`:

```
allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}
```

> Jcenter will be removed in the stable version

And then to the module level `build.gradle`:

```
dependencies {
    implementation 'com.revolut.kompot:kompot:0.0.1'
    
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1'
    
    implementation 'com.google.dagger:dagger:2.40.2'
    kapt 'com.google.dagger:dagger-compiler:2.40.2'
    
    //test dependencies
    testImplementation 'com.revolut.kompot:core-test:0.0.1'
    testImplementation 'com.revolut.kompot:coroutines-test:0.0.1'
    
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.1"
}
```
> A coroutines dependency will be required to use some of the Kompot features and test them. Kompot has pre-built tools to work with DI and requires Dagger 2 to be present in your module.

## Wiki

* The [project's wiki][] gives step by step explanation of how to use a framework and guides you through its main features

[project's wiki]: https://github.com/revolut-mobile/kompot/wiki


## License


    Copyright 2022 Revolut

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
