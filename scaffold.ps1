$basePkg = "com.waterplant.distribution"

$modules = @{
    "admin" = @{ "type" = "app" }
    "employee" = @{ "type" = "app" }
    "core\database" = @{ "type" = "lib" }
    "core\models" = @{ "type" = "lib" }
    "core\network" = @{ "type" = "lib" }
    "core\repository" = @{ "type" = "lib" }
    "core\sync" = @{ "type" = "lib" }
    "core\ui" = @{ "type" = "lib" }
    "core\utils" = @{ "type" = "lib" }
    "core\reports" = @{ "type" = "lib" }
    "core\backup" = @{ "type" = "lib" }
}

function Create-Manifest {
    param ($path, $isApp, $moduleName)
    $pkg = $basePkg + "." + $moduleName.Replace("\", ".")
    
    if ($isApp) {
        $content = @"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.WaterJarDistribution"
        android:name="${pkg}.MainApplication"
        tools:targetApi="31">
        <activity
            android:name="${pkg}.MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.WaterJarDistribution">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
"@
    } else {
        $content = @"
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
"@
    }
    
    $dir = Split-Path $path -Parent
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    Set-Content -Path $path -Value $content
}

function Create-BuildGradle {
    param ($path, $isApp, $moduleName)
    $pkg = $basePkg + "." + $moduleName.Replace("\", ".")
    
    if ($isApp) {
        $plugin = "alias(libs.plugins.android.application)"
        $appId = "applicationId = `"$pkg`""
        $targSdk = "targetSdk = 34"
        $vCode = "versionCode = 1"
        $vName = "versionName = `"1.0`""
    } else {
        $plugin = "alias(libs.plugins.android.library)"
        $appId = ""
        $targSdk = ""
        $vCode = ""
        $vName = ""
    }

    $content = @"
plugins {
    $plugin
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "$pkg"
    compileSdk = 34

    defaultConfig {
        $appId
        minSdk = 29
        $targSdk
        $vCode
        $vName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
"@
    Set-Content -Path $path -Value $content
}

foreach ($entry in $modules.GetEnumerator()) {
    $moduleName = $entry.Name
    $info = $entry.Value
    $isApp = $info["type"] -eq "app"
    
    if (!(Test-Path $moduleName)) { New-Item -ItemType Directory -Force -Path $moduleName | Out-Null }
    
    $pkgPath = $basePkg.Replace(".", "\") + "\" + $moduleName
    $srcDir = Join-Path $moduleName "src\main\java\$pkgPath"
    if (!(Test-Path $srcDir)) { New-Item -ItemType Directory -Force -Path $srcDir | Out-Null }
    
    $manifestPath = Join-Path $moduleName "src\main\AndroidManifest.xml"
    Create-Manifest $manifestPath $isApp $moduleName
    
    $buildPath = Join-Path $moduleName "build.gradle.kts"
    Create-BuildGradle $buildPath $isApp $moduleName
    
    if ($isApp) {
        $valDir = Join-Path $moduleName "src\main\res\values"
        $xmlDir = Join-Path $moduleName "src\main\res\xml"
        if (!(Test-Path $valDir)) { New-Item -ItemType Directory -Force -Path $valDir | Out-Null }
        if (!(Test-Path $xmlDir)) { New-Item -ItemType Directory -Force -Path $xmlDir | Out-Null }
        
        $appName = if ($moduleName -eq "admin") { "Admin App" } else { "Employee App" }
        Set-Content -Path (Join-Path $valDir "strings.xml") -Value "<?xml version=`"1.0`" encoding=`"utf-8`"?><resources><string name=`"app_name`">$appName</string></resources>"
        Set-Content -Path (Join-Path $valDir "themes.xml") -Value "<?xml version=`"1.0`" encoding=`"utf-8`"?><resources><style name=`"Theme.WaterJarDistribution`" parent=`"android:Theme.Material.Light.NoActionBar`" /></resources>"
        Set-Content -Path (Join-Path $xmlDir "data_extraction_rules.xml") -Value "<?xml version=`"1.0`" encoding=`"utf-8`"?><data-extraction-rules><cloud-backup><include domain=`"sharedpref`" path=`".`"/></cloud-backup></data-extraction-rules>"
        Set-Content -Path (Join-Path $xmlDir "backup_rules.xml") -Value "<?xml version=`"1.0`" encoding=`"utf-8`"?><full-backup-content><include domain=`"sharedpref`" path=`".`"/></full-backup-content>"
    }
}
Write-Output "Scaffolding complete!"
