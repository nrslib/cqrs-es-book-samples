import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.5.15" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.4.0" apply false
    kotlin("plugin.spring") version "2.4.0" apply false
    kotlin("plugin.jpa") version "2.4.0" apply false
}

allprojects {
    group = "org.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    dependencies {
        // 06 は書籍6章の前提（WebFlux/ノンブロッキング）どおりリアクティブ起動にするため、
        // サーブレットスタックを入れない（webflux と webmvc が両方あるとサーブレット起動になる）
        if (project.name != "06") {
            "implementation"("org.springframework.boot:spring-boot-starter-web")
        }
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.axonframework:axon-spring-boot-starter:4.13.1") {
            exclude(group = "org.axonframework", module = "axon-server-connector")
        }
        "runtimeOnly"("com.h2database:h2")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.axonframework:axon-test:4.13.1")
        "testImplementation"("org.awaitility:awaitility")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    // サンプルは bootRun / test で動作させるため、実行可能jarは作らない
    tasks.withType<BootJar> { enabled = false }
    tasks.named<Jar>("jar") { enabled = true }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
