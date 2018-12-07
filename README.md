RKVR-Rajawali
================

This repository is the library with sample app showing how to run a high performance VR app on Rockchip VR platforms. The 3d engine is using Rajawali.

##Device
IMPORTANT info: The VR App could only runs on Rockchip VR Platform, which have AP RK3288/RK3399 inside and powerd by lots of GDCV's optimization.

Update: Now if you choose vr-debug.aar the app could runs on normal mobile phones, without any performance optimization. Only for debug.

##Library

There're two aar files you should use in your VR app,please check here:
```
./app/libs/vr-release.aar
./app/libs/rajawali-release.aar
```
For using them, just add few lines in your moudle's build.gradle file:
```
repositories {
    flatDir {
        dirs 'libs'
    }
}
```
```
dependencies {
    ...
    compile(name:'vr-release', ext:'aar')
    compile(name:'rajawali-release', ext:'aar')
}
```

The API is actually all provided by Rajawali, please check:
https://github.com/Rajawali/Rajawali/wiki

##Sample App
Just import the project to Android Studio and compile it as a normal Android App.

