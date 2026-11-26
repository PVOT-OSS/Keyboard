plugins {
    application
}

application {
    mainClass = "org.prauga.keyboard.keyboard.tools.MakeKeyboardText"
}

sourceSets {
    main {
        java.srcDir("src")
        resources.srcDir("res")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    manifest {
        attributes("Main-Class" to application.mainClass)
    }
}

tasks.register<JavaExec>("generate") {
    mainClass = application.mainClass
    dependsOn(tasks.jar)
    classpath(tasks.jar.flatMap { it.archiveFile })
    workingDir = rootProject.projectDir
    args("-java", "app/src/main/java")
    doLast {
        println()
        println("===========================================================================")
        println("WARNING: The locales will be messed up when running this on a host JVM. You")
        println("should manually merge the required changes.")
        println("===========================================================================")
        println()
    }
}
