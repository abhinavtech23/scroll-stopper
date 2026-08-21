Write-Host "Building and running Scroll Stopper on your device..." -ForegroundColor Cyan

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:LOCALAPPDATA\Android\Sdk\platform-tools;C:\Users\dell\.gradle\wrapper\dists\gradle-8.13-all\54h0s9kvb6g2sinako7ub77ku\gradle-8.13\bin;$env:PATH"

cmd /c "gradle installDebug"
if ($LASTEXITCODE -eq 0) {
    Write-Host "Launching on phone..." -ForegroundColor Green
    & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" shell am start -n com.feedshield.android/.MainActivity
    Write-Host "Done! App running on device." -ForegroundColor Green
} else {
    Write-Host "Build failed. Check errors above." -ForegroundColor Red
}
