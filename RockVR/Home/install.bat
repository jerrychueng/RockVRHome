echo "sign apk"
sign.bat
echo "push apk"
sleep 1
adb root
sleep 2
adb remount
sleep 2
adb push RockVRHome.apk /system/priv-app/RockVRHome/RockVRHome.apk
echo "restart app"
sleep 1
adb shell am force-stop com.rockchip.vr.home
sleep 1
adb shell am start com.rockchip.vr.home
