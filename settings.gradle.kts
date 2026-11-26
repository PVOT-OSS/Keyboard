pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Keyboard"

includeBuild("PvotLib") {
    dependencySubstitution {
        substitute(module("com.prauga.pvot:design-system")).using(project(":design-system"))
        substitute(module("com.prauga.pvot:core-ui")).using(project(":core-ui"))
        substitute(module("com.prauga.pvot:design-system-views"))
            .using(project(":design-system-views"))
    }
}

include(":app")

include(":tools:make-keyboard-text")
