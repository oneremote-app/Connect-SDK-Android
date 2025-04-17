plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
    jacoco
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

group = "com.github.ConnectSDK"

jacoco {
    toolVersion = "0.7.1.201405082137"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("check"))
    group = "Reporting"
    description = "Generate Jacoco coverage reports"

    classDirectories.setFrom(fileTree("build/intermediates/classes/debug") {
        exclude(
            "**/R.class",
            "**/R\\$*.class",
            "**/*\\\$ViewInjector*.*",
            "**/BuildConfig.*",
            "**/Manifest*.*"
        )
    })

    additionalSourceDirs.setFrom(android.sourceSets.getByName("main").java.srcDirs)
    sourceDirectories.setFrom(android.sourceSets.getByName("main").java.srcDirs)
    executionData.setFrom(file("build/jacoco/testDebug.exec"))

    reports {
        html.required.set(true)
    }
}

tasks.named("build") {
    dependsOn("jacocoTestReport")
}

android {
    compileSdk = 35
    buildToolsVersion = "30.0.3"

    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }

    packagingOptions {
        resources {
            excludes += "LICENSE.txt"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("AndroidManifest.xml")
            java.srcDirs("src", "core/src", "modules/google_cast/src", "modules/firetv/src")
            resources.srcDirs("src")
            aidl.srcDirs("src")
            renderscript.srcDirs("src")
            res.srcDirs("core/res")
            assets.srcDirs("assets")
            jniLibs.srcDirs("core/jniLibs")
        }
        getByName("test") {
            java.srcDirs("core/test/src", "modules/google_cast/test/src", "modules/firetv/test/src")
        }
    }

    buildTypes {
        getByName("debug") {
            isTestCoverageEnabled = true
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    lintOptions {
        isAbortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    useLibrary("org.apache.http.legacy")
    ndkVersion = "22.1.7171670"

    testOptions {
        unitTests {
            all {
                it.include("**/*Test.class")
            }
        }
    }
}

dependencies {
    implementation("org.java-websocket:Java-WebSocket:1.5.0")
    implementation("javax.jmdns:jmdns:3.4.1")
    implementation(fileTree(mapOf("dir" to "modules/firetv/libs", "include" to listOf("*.jar"))))

    implementation("androidx.mediarouter:mediarouter:1.2.0")
    implementation("androidx.annotation:annotation:1.0.0")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.googlecode.plist:dd-plist:1.23")
    implementation("com.nimbusds:srp6a:2.1.0")
    implementation("net.i2p.crypto:eddsa:0.3.0")
    implementation("com.google.android.gms:play-services-cast-framework:9.4.0")
    implementation(files("core/libs/lgcast-android-lib.jar"))

    testImplementation("org.apache.maven:maven-ant-tasks:2.1.3")
    testImplementation("junit:junit:4.12")
    testImplementation("org.robolectric:robolectric:2.4")
    testImplementation("org.mockito:mockito-all:1.10.19")
    testImplementation("org.powermock:powermock-api-mockito:1.6.2")
    testImplementation("xmlunit:xmlunit:1.4")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "POM_ARTIFACT_ID"  // update with your artifact id
            version = "VERSION_NAME"       // update with your version

            // Add any artifacts, for example your AAR or jar:
            artifact("$buildDir/outputs/aar/${project.name}-release.aar")

            pom {
                name.set("POM_NAME")
                description.set("POM_DESCRIPTION")
                url.set("POM_URL")
                licenses {
                    license {
                        name.set("POM_LICENCE_NAME")
                        url.set("POM_LICENCE_URL")
                        distribution.set("POM_LICENCE_DIST")
                    }
                }
                scm {
                    url.set("POM_SCM_URL")
                    connection.set("POM_SCM_CONNECTION")
                    developerConnection.set("POM_SCM_DEV_CONNECTION")
                }
                developers {
                    developer {
                        id.set("POM_DEVELOPER_ID")
                        name.set("POM_DEVELOPER_NAME")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(
                if (!project.version.toString().contains("SNAPSHOT"))
                    "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                else
                    "https://oss.sonatype.org/content/repositories/snapshots/"
            )
            credentials {
                username = System.getenv("NEXUS_USERNAME") ?: ""
                password = System.getenv("NEXUS_PASSWORD") ?: ""
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}