plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    // EL CAMBIO MAESTRO: Cambiamos 'jlink' por 'runtime'
    id("org.beryx.runtime") version "1.13.0" 
}

group = "org.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    // Apunta al Launcher sin paquete
    mainClass.set("Launcher")
}

runtime {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    
    modules.set(listOf(
        "java.desktop", 
        "java.sql", 
        "java.net.http", 
        "java.logging", 
        "jdk.unsupported", 
        "jdk.crypto.ec",
        "java.se"
    ))
    
    launcher {
        noConsole = false 
    }
    
    jpackage {
        appVersion = "1.0.0"
        
        // El ícono se pasa como una opción de imagen
        imageOptions.addAll(listOf(
            "--icon", "src/main/resources/icon/hospital.ico"
        ))
        
        // El vendor y las demás configuraciones van a las opciones del instalador
        installerOptions.addAll(
            listOf(
                "--vendor", "MediCore Health Systems",
                "--win-dir-chooser",
                "--win-shortcut",
                "--win-menu",
                "--win-menu-group", "MediCore"
            )
        )
    }
}

tasks.test {
    useJUnitPlatform()
}