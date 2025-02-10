pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("de.fayard.refreshVersions") version "0.60.5"
    }
}

plugins {
    id("de.fayard.refreshVersions")
}


rootProject.name = "tuul-be"
include("app", "adapters:web", "domain", "adapters:firestore", "integration-test")
