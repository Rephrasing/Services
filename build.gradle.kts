plugins {
    `maven-publish`
    `java-library`
    id("java")
}

group = "io.github.rephrasing"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:5.2.0") // https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync
    implementation("com.google.code.gson:gson:2.11.0") // https://mvnrepository.com/artifact/com.google.code.gson/gson
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}