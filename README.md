# opencv-ia-recognition

This application (Android based) allow to identify somebody through camera

# OpenCV Android Facial Recognition Template

![Android API](https://img.shields.io/badge/API-28%2B-brightgreen)
![OpenCV](https://img.shields.io/badge/OpenCV-4.9.0-green.svg)
![NDK](https://img.shields.io/badge/NDK-26.1.10909125-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Gradle](https://img.shields.io/badge/Gradle-8.5.0-02303A.svg)
![CMake](https://img.shields.io/badge/CMake-3.10.2-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)

## Introduction

This Android Studio project utilizes OpenCV 4.9.0 for facial recognition with PCA through a dual-fragment system: Camera and Gallery. Targeting rapid deployment, it integrates NDK (26.1.10909125) for robust development, supports API levels 28 to 34, and maintains compatibility with `JavaVersion.VERSION_17`.

This Android application is a streamlined foundation for integrating OpenCV 4.9.0, as well as other 4.x.y versions, within the Android Studio environment, augmented with the power of the Native Development Kit (NDK). The NDK support empowers developers to craft sophisticated OpenCV image processing pipelines in C++ and seamlessly invoke this native codebase from within the Android Java ecosystem via the Java Native Interface (JNI).

## Demo

![](./screenshots/how_to_use.gif)

## Features

- **Dual Fragment System**: Camera and Gallery fragments for a seamless user experience.
- **PCA Recognition**: Utilizes a five-component PCA for accurate facial recognition.
- **Smooth Navigation**: Drawer layout for easy toggling between Camera and Gallery.
- **Gallery Preview**: Review selected training images before processing.
- **Real-Time Recognition**: Post-training, the system can immediately recognize faces.
- **Training Reset**: Easily restart PCA training within the Gallery.
- **JNI Troubleshooting**: Solutions for common JNI issues are included.

## Prerequisites

- Android Studio with CMake and NDK installed.
- OpenCV Android SDK (version 4.9.0 recommended).
- Android device or emulator (API level 28+ recommended).

## Setup

1. **Clone and Sync**: Clone this repo and sync with Gradle.
2. **OpenCV Configuration**: Validate OpenCV SDK path in `settings.gradle`.
3. **Build and Deploy**: Build the project and run it on your device or emulator.

## Training the PCA Model

1. Go to **Gallery Fragment**.
2. Select images for training and press "Process".
3. Wait for the Toast indicating training completion.
4. Switch to **Camera Fragment** to experience facial recognition.

## How to use this repository

1. [Download and Install Android Studio](https://developer.android.com/studio)
2. [Install NDK and CMake](https://developer.android.com/studio/projects/install-ndk.md)
3. Clone this repository as an Android Studio project
4. Shared objects install *OpenCV Android release* :
    * Copy from `/opencv/native` to `/sdk/native`. It will be necesary to build the application.

5. Sync Gradle and run the application on your device or AVD

# Troubleshooting
## Unable to find `libcpufeatures` static libs.
```
CMake Error at /opencv/native/jni/abi-arm64-v8a/OpenCVModules.cmake:238 (message):
  The imported target "libcpufeatures" references the file

     "/sdk/native/3rdparty/libs/arm64-v8a/libcpufeatures.a"

  but this file does not exist.  Possible reasons include:

  * The file was deleted, renamed, or moved to another location.

  * You have changed OpenCV implement project statment.
```
So, in order to fix it you need to copy `/opencv/native...` to `/sdk/native`. Dont delete anything.

## Contributing

Contributions are welcome. Please refer to CONTRIBUTING.md for guidelines.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Gratitude to the OpenCV community for their extensive resources and support.

Author
===
[Miguel Angel](https://github.com/miguelanruiz)
