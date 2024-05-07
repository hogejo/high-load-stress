plugins {
	application
}

// Application config

group = "hu.laba"
version = "5.3.0"

application {
	mainClass.set("hu.laba.Stress")
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
	// OkHttp
	// TODO: Upgrade to 5.0, with *actual* support for virtual threads
	implementation(group = "com.squareup.okhttp3", name = "okhttp", version = "4.12.0")
	// Jackson
	implementation(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.17.0")
	implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.17.0")
	implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310", version = "2.17.0")
	// JCommander
	implementation(group = "org.jcommander", name = "jcommander", version = "1.83")
	// TESTING
	// JUnit Jupiter
	testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.10.2")
	testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher")
}

// Tests

tasks.withType<Test> {
	useJUnitPlatform()
}

// Single JAR

tasks.register<Jar>("distJar") {
	group = "distribution"
	manifest {
		attributes["Main-Class"] = application.mainClass
	}
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	dependsOn(configurations.runtimeClasspath)
	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
	with(tasks.jar.get())
	destinationDirectory = file("build/distributions")
}
