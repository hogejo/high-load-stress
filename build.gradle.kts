plugins {
	application
}

// Application config

group = "me.hogejo.highload.stress"
version = "8.6.0"

application {
	mainClass.set("$group.Application")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
		targetCompatibility = JavaVersion.VERSION_21
		sourceCompatibility = JavaVersion.VERSION_21
	}
}

// Dependencies

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	// Dagger
	implementation(group = "com.google.dagger", name = "dagger", version = "2.51")
	annotationProcessor(group = "com.google.dagger", name = "dagger-compiler", version = "2.51")
	// OkHttp
	// TODO: Upgrade to 5.0, with *actual* support for virtual threads
	implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.12.0")
	// Jackson
	implementation(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.17.2")
	implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.17.2")
	implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310", version = "2.17.2")
	// JCommander
	implementation(group = "org.jcommander", name = "jcommander", version = "1.83")
	// TESTING
	// JUnit Jupiter
	testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.10.3")
	testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher")
}

// Tests
tasks.withType<Test> {
	useJUnitPlatform()
}

// Single JAR
tasks.register("flatJar", Jar::class) {
	group = "distribution"
	dependsOn(configurations.runtimeClasspath)
	manifest {
		attributes["Main-Class"] = application.mainClass
	}
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	with(tasks.jar.get())
	destinationDirectory = file("build/distributions/flat")
}

//
// Docker
//

val imageName = project.name + ":" + project.version
val dockerWorkingDir = file(".")

tasks.register<Exec>("buildImage") {
	group = "build"
	dependsOn(tasks.named("flatJar"))
	workingDir = dockerWorkingDir
	commandLine = listOf(
		"docker", "build",
		"-f", "docker/Dockerfile",
		"-t", imageName,
		"--build-arg", "PROJECT_NAME=" + project.name,
		"--build-arg", "PROJECT_VERSION=" + project.version,
		"."
	)
	doLast { println("Built Docker image: $imageName") }
}

tasks.register<Exec>("saveImage") {
	group = "distribution"
	dependsOn(tasks.named("buildImage"))
	workingDir = dockerWorkingDir
	val outputDirectory = "build/distributions/docker"
	val output = "${outputDirectory}/${imageName.replace(":","-")}.tar"
	mkdir(outputDirectory)
	commandLine = listOf(
		"docker", "save", imageName,
		"--output", output,
	)
	doLast { println("Saved Docker image $imageName to $output") }
}

tasks.register<Exec>("pushImage") {
	group = "publishing"
	dependsOn(tasks.named("buildImage"))
	workingDir = dockerWorkingDir
	commandLine = listOf("docker", "push", imageName)
	doLast { println("Pushed Docker image: $imageName") }
}

tasks.register<Exec>("runImage") {
	group = "application"
	dependsOn(tasks.named("buildImage"))
	workingDir = dockerWorkingDir
	doFirst { println("Running Docker image $imageName ...") }
	commandLine = listOf("docker", "run", "-P", imageName)
}
