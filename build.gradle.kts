plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
}

javafx {
    version = "21.0.2"
    modules("javafx.controls")
}

application {
    mainClass.set("Main")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    // Nombre del archivo final
    archiveBaseName.set("medicore-desktop")
    archiveVersion.set("1.0")
    archiveClassifier.set("standalone")

    // Atributos del manifiesto (Apunta directamente a Launcher porque no tiene paquete)
    manifest {
        attributes(
            "Main-Class" to "Launcher" 
        )
    }

    // Incluye el código compilado
    from(sourceSets.main.get().output)

    // Extrae y empaca todas las dependencias dentro del JAR
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    // Evita problemas de firmas duplicadas al mezclar librerías
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    
    // Soluciona conflictos si hay archivos duplicados
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}