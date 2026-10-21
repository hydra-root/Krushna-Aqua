$adminDeps = @"

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:repository"))
    implementation(project(":core:ui"))
    implementation(project(":core:models"))
}
"@
Add-Content -Path "admin\build.gradle.kts" -Value $adminDeps

$employeeDeps = @"

dependencies {
    implementation(project(":core:sync"))
    implementation(project(":core:ui"))
    implementation(project(":core:utils"))
    implementation(project(":core:models"))
}
"@
Add-Content -Path "employee\build.gradle.kts" -Value $employeeDeps

$networkDeps = @"

dependencies {
    implementation(project(":core:models"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
}
"@
Add-Content -Path "core\network\build.gradle.kts" -Value $networkDeps

$databaseDeps = @"

dependencies {
    implementation(project(":core:models"))
}
"@
Add-Content -Path "core\database\build.gradle.kts" -Value $databaseDeps

$repositoryDeps = @"

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:models"))
}
"@
Add-Content -Path "core\repository\build.gradle.kts" -Value $repositoryDeps

$syncDeps = @"

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:repository"))
    implementation(project(":core:models"))
}
"@
Add-Content -Path "core\sync\build.gradle.kts" -Value $syncDeps

$uiDeps = @"

dependencies {
    implementation(project(":core:models"))
}
"@
Add-Content -Path "core\ui\build.gradle.kts" -Value $uiDeps

$reportsDeps = @"

dependencies {
    implementation(project(":core:models"))
}
"@
Add-Content -Path "core\reports\build.gradle.kts" -Value $reportsDeps

Write-Output "Dependencies added successfully"
