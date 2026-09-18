plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.6.1" // 최신 버전은 https://plugins.gradle.org/plugin/com.gradleup.shadow 확인
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    implementation("org.reflections:reflections:0.10.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveClassifier.set("") // 접미사 없는 최종 산출물로

        // reflections + 내부적으로 쓰는 javassist를 패키지 이동해서
        // 다른 플러그인의 동일 라이브러리와 충돌 방지
        relocate("org.reflections", "cat.lacycat.perKS.libs.reflections")
        relocate("javassist", "cat.lacycat.perKS.libs.javassist")
    }

    build {
        dependsOn(shadowJar)
    }
}