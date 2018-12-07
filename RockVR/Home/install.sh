#!/bin/bash
echo "sign apk"
./sign.sh build/outputs/apk/RockVRHome-debug.apk RockVRHome.apk
echo "push apk"
sleep 1
adb root
sleep 1 
adb remount
sleep 1
adb push RockVRHome.apk /system/priv-app/RockVRHome/RockVRHome.apk
echo "restart app"
sleep 1
adb shell am force-stop com.rockchip.vr.home
sleep 1
adb shell am start com.rockchip.vr.home/.VRMainActivity
