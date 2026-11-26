pluginManagement {
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
        substitute(module("com.prauga.pvot:design-system-views"))
            .using(project(":design-system-views"))
    }
}

include(":app")
include(":tools:make-keyboard-text")
