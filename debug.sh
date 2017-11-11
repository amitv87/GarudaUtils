set -e

PACKAGE_NAME="com.hps.garuda.utilstest"

MODE=$(adb shell dumpsys package $PACKAGE_NAME | grep "pkgFlags=" | awk '{split($0,a," "); print a[2]}')

if [[ "$MODE" == "HAS_CODE" ]] ; then
  echo "release package found uninstalling"
  adb uninstall $PACKAGE_NAME
fi

echo "building"
./gradlew $@ installDebug


# echo "building"
# ./gradlew $@ build -x lint

# MODE=$(adb shell dumpsys package $PACKAGE_NAME | grep "pkgFlags=" | awk '{split($0,a," "); print a[2]}')

# if [[ "$MODE" == "DEBUGGABLE" ]] ; then
#   echo "debug package found uninstalling"
#   adb uninstall $PACKAGE_NAME
# fi

# APK_PATH="app/build/outputs/apk/release"
# ls -lah $APK_PATH

# OUT_APK="app-release.apk"

# echo "installing $APK_PATH/$OUT_APK"

# adb install -r $APK_PATH/$OUT_APK

# ls -lah app/build/outputs/apk/*

APK_PATH=`adb shell pm path $PACKAGE_NAME | cut -c9- | tr -d '\r' | tr -d '\n'`
echo $APK_PATH

adb shell <<__EOF
  export CLASSPATH=$APK_PATH
  exec app_process /system/bin $PACKAGE_NAME.TestGrpc "hello world"
__EOF
