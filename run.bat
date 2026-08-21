@echo off
setlocal
echo Building and running Scroll Stopper on your device...

set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
set "PATH=C:\Program Files\Android\Android Studio\jbr\bin;%LOCALAPPDATA%\Android\Sdk\platform-tools;C:\Users\dell\.gradle\wrapper\dists\gradle-8.13-all\54h0s9kvb6g2sinako7ub77ku\gradle-8.13\bin;%PATH%"

call gradle installDebug
if %ERRORLEVEL% equ 0 (
    echo Launching on phone...
    adb shell am start -n com.feedshield.android/.MainActivity
    echo Done! App running on device.
) else (
    echo Build failed. Please check errors above.
)
