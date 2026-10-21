import os

base_pkg = "com.waterplant.distribution"

modules = {
    "admin": {"type": "app"},
    "employee": {"type": "app"},
    "core/database": {"type": "lib"},
    "core/models": {"type": "lib"},
    "core/network": {"type": "lib"},
    "core/repository": {"type": "lib"},
    "core/sync": {"type": "lib"},
    "core/ui": {"type": "lib"},
    "core/utils": {"type": "lib"},
    "core/reports": {"type": "lib"},
    "core/backup": {"type": "lib"}
}

def create_manifest(path, is_app, module_name):
    pkg = base_pkg + "." + module_name.replace("/", ".")
    content = ""
    if is_app:
        content = f"""<?xml version="1.0" encoding="utf-8"?>
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
        android:name="{pkg}.MainApplication"
        tools:targetApi="31">
        <activity
            android:name="{pkg}.MainActivity"
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
"""
    else:
        content = f"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
"""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(content)

def create_build_gradle(path, is_app, module_name):
    pkg = base_pkg + "." + module_name.replace("/", ".")
    plugin = 'alias(libs.plugins.android.application)' if is_app else 'alias(libs.plugins.android.library)'
    
    # Extra plugins
    plugins = f"""plugins {{
    {plugin}
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}}"""

    content = f"""{plugins}

android {{
    namespace = "{pkg}"
    compileSdk = 34

    defaultConfig {{
        {"applicationId = \\"" + pkg + "\\"" if is_app else ""}
        minSdk = 29
        {"targetSdk = 34" if is_app else ""}
        {"versionCode = 1" if is_app else ""}
        {"versionName = \\"1.0\\"" if is_app else ""}

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {{
            useSupportLibrary = true
        }}
    }}

    buildTypes {{
        release {{
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }}
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }}
    kotlinOptions {{
        jvmTarget = "17"
    }}
    buildFeatures {{
        compose = true
    }}
    composeOptions {{
        kotlinCompilerExtensionVersion = "1.5.8"
    }}
}}

dependencies {{
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
}}
"""
    with open(path, "w") as f:
        f.write(content)

for module_name, info in modules.items():
    is_app = info["type"] == "app"
    os.makedirs(module_name, exist_ok=True)
    
    # src/main/java/...
    pkg_path = base_pkg.replace(".", "/") + "/" + module_name
    os.makedirs(os.path.join(module_name, "src/main/java", pkg_path), exist_ok=True)
    
    # AndroidManifest.xml
    create_manifest(os.path.join(module_name, "src/main/AndroidManifest.xml"), is_app, module_name)
    
    # build.gradle.kts
    create_build_gradle(os.path.join(module_name, "build.gradle.kts"), is_app, module_name)
    
    # For apps, create basic res folders
    if is_app:
        os.makedirs(os.path.join(module_name, "src/main/res/values"), exist_ok=True)
        os.makedirs(os.path.join(module_name, "src/main/res/xml"), exist_ok=True)
        with open(os.path.join(module_name, "src/main/res/values/strings.xml"), "w") as f:
            f.write(f'<?xml version="1.0" encoding="utf-8"?>\\n<resources>\\n    <string name="app_name">{"Admin App" if module_name == "admin" else "Employee App"}</string>\\n</resources>')
        with open(os.path.join(module_name, "src/main/res/values/themes.xml"), "w") as f:
            f.write('<?xml version="1.0" encoding="utf-8"?>\\n<resources>\\n    <style name="Theme.WaterJarDistribution" parent="android:Theme.Material.Light.NoActionBar" />\\n</resources>')
        with open(os.path.join(module_name, "src/main/res/xml/data_extraction_rules.xml"), "w") as f:
            f.write('<?xml version="1.0" encoding="utf-8"?>\\n<data-extraction-rules>\\n    <cloud-backup>\\n        <include domain="sharedpref" path="."/>\\n    </cloud-backup>\\n</data-extraction-rules>')
        with open(os.path.join(module_name, "src/main/res/xml/backup_rules.xml"), "w") as f:
            f.write('<?xml version="1.0" encoding="utf-8"?>\\n<full-backup-content>\\n    <include domain="sharedpref" path="."/>\\n</full-backup-content>')

print("Scaffolding complete!")
