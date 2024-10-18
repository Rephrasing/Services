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
    implementation("org.apache.logging.log4j:log4j-core:2.24.1") // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.mongodb:mongodb-driver-sync:5.2.0") // https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}