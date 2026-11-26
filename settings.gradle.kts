pluginManagement {
    includeBuild("PvotLib/build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

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
