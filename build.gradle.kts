plugins { java }

group = "dev.batterycraft"
version = providers.gradleProperty("mod_version").get()

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    compileOnly("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

val targets = mapOf(
    "mc1_21" to Pair(">=1.21 <26.1-", "mc1.21"),
    "mc26_1" to Pair(">=26.1 <26.2-", "mc26.1"),
    "mc26_2" to Pair(">=26.2 <26.3-", "mc26.2")
)

targets.forEach { (taskSuffix, target) ->
    tasks.register<Jar>("jar_${taskSuffix}") {
        group = "build"
        dependsOn(tasks.classes)
        archiveBaseName.set("batterycraft")
        archiveVersion.set(providers.gradleProperty("mod_version").get())
        archiveClassifier.set(target.second)
        from(sourceSets.main.get().output.classesDirs)
        from("src/targets/$taskSuffix")
        manifest {
            attributes("Implementation-Title" to "BatteryCraft", "Implementation-Version" to providers.gradleProperty("mod_version").get())
        }
    }
}

tasks.register("releaseJars") {
    group = "build"
    dependsOn(targets.keys.map { "jar_$it" })
}
