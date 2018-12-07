#!/bin/bash
echo "sign apk"
./sign.sh
adb root
sleep 1
adb remount
sleep 1
adb push RockVRGlobalActions.apk /system/priv-app/RockVRGlobalActions/
