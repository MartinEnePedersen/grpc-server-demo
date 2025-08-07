import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Duration

val protobufPluginVersion by extra("0.9.4") //Can't see how to reference this in the plugins block
val protobufVersion by extra("4.29.3")
val grpcKotlinVersion by extra("1.4.1")
val grpcVersion by extra("1.57.2")
val mockkVersion by extra("1.14.2")
val generatedSourcesPath = "$rootDir/generatedClient" //Have not yet found a way to avoid using deprecated $buildDir
val apiDescriptionFile = "$rootDir/swagger-pretty.json" //https://jppolaccessservice.test.aws.jyllands-posten.dk/swagger/v4/swagger.json
val apiRootName = "dk.eb.saapi.adaptor"

plugins {
	kotlin("jvm") version "2.1.10"
	kotlin("plugin.spring") version "2.1.10"
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.4"
	id("org.openapi.generator") version "7.12.0"
}
//    id("org.hidetake.swagger.generator") version "2.19.2"

group = "dk.eb"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17  //can higher work?

val mavenRepoUrl: String = findProperty("MAVEN_REPO_URL") as String? ?: System.getenv("MAVEN_REPO_URL")
val repoUsername: String = findProperty("MAVEN_REPO_USERNAME") as String? ?: System.getenv("MAVEN_REPO_USERNAME")
val repoPassword: String = findProperty("MAVEN_REPO_PASSWORD") as String? ?: System.getenv("MAVEN_REPO_PASSWORD")

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)  //can higher work?
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

openApiGenerate {
	validateSpec.set(false)
	skipValidateSpec.set(true)
	generatorName.set("kotlin")
	inputSpec.set(apiDescriptionFile)
	outputDir.set("$rootDir/generatedClient")
	apiPackage.set("dk.eb.adapter.services.saapi.client.api")
	invokerPackage.set("dk.eb.adapter.services.saapi.client.invoker")
	modelPackage.set("dk.eb.adapter.services.saapi.client.model")
	additionalProperties.set(
		mapOf(
			"hideGenerationTimestamp" to "true",
			"java17" to "true",
			"serializableModel" to "true",
			"useRuntimeException" to "false",
			"dateLibrary" to "java17",
			"library" to "jvm-okhttp4"
			// "scmConnection" to "scm:git:git@github.com:EkstraBladetUdvikling/subscription-service-scapi.git",
			// "scmDeveloperConnection" to "scm:git:git@github.com:EkstraBladetUdvikling/subscription-service-scapi.git",
			// "scmUrl" to "https://github.com/EkstraBladetUdvikling/subscription-service-scapi"
		)
	)
}

/*openApiGenerate {
	generatorName.set("kotlin")
	inputSpec.set("$rootDir/src/main/resources/api.yaml")
	outputDir.set("$buildDir/generated")
	apiPackage.set("dk.eb.adapter.services.scapi.client.api")
	modelPackage.set("dk.eb.adapter.services.scapi.client.model")
	configOptions.set(mapOf(
		"enumPropertyNaming" to "UPPERCASE",
		"modelNameSuffix" to "Dto",
		"typeMappings" to "string=CustomString,number=CustomNumber"
	))
}*/


repositories {
	mavenCentral()
	maven {
		url = uri(mavenRepoUrl)
		credentials {
			username = repoUsername
			password = repoPassword
		}
		metadataSources {
			artifact()
		}
	}
}

extra["springCloudVersion"] = "2024.0.0" //For Resilience4J

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

dependencies {
	implementation("software.amazon.awssdk:sns:2.25.6")
	implementation("software.amazon.awssdk:sqs:2.25.6")
	implementation("software.amazon.awssdk:netty-nio-client:2.25.6")
	implementation ("com.google.code.gson:gson:2.8.5")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	implementation("ch.qos.logback:logback-core:1.5.16")  //in spring boot bom, but still must specify version...
	implementation("ch.qos.logback:logback-classic:1.5.16")
	implementation("ch.qos.logback:logback-access:1.5.16")
	implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5") // https://mvnrepository.com/artifact/ch.qos.logback.contrib/logback-json-classic
	implementation("ch.qos.logback.contrib:logback-jackson:0.1.5") // https://mvnrepository.com/artifact/ch.qos.logback.contrib/logback-jackson

	implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
	implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
	implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.2")

	//Dependencies for protobuf and gRPC
//see https://github.com/grpc/grpc-kotlin/blob/master/compiler/README.md for more info
	implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
	implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion") //https://mvnrepository.com/artifact/io.grpc/grpc-kotlin-stub/1.4.1
	implementation("io.grpc:grpc-protobuf:$grpcVersion") //https://mvnrepository.com/artifact/io.grpc/grpc-protobuf/1.57.2
	implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion") //https://mvnrepository.com/artifact/com.google.protobuf/protobuf-kotlin/4.29.3

	implementation("org.jetbrains.kotlin:kotlin-stdlib")

	//Dependencies for openApiGenerate
	// https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
	//implementation("com.squareup.okhttp3:okhttp:3.14.9") // should not be a higher version, due the generated code and essenic - autogenerated code is not compatible with newer versions
	//implementation("com.squareup.okhttp3:okhttp:3.14.9")
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("io.ktor:ktor-client-core:2.3.1")
	implementation("io.ktor:ktor-client-cio:2.3.1")
	implementation("io.ktor:ktor-client-json:2.3.1")
	implementation("io.ktor:ktor-client-serialization:2.3.1")
	implementation("io.ktor:ktor-client-logging:2.3.1")
	implementation("io.ktor:ktor-client-content-negotiation:2.3.1")

	// https://mvnrepository.com/artifact/com.squareup.moshi/moshi-kotlin
	implementation("com.squareup.moshi:moshi-kotlin:1.15.2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
	testImplementation("io.mockk:mockk:${mockkVersion}")
	// cache
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("com.github.ben-manes.caffeine:caffeine")

	// jackson for string to object mapping in tests
	implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.1")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:$protobufVersion"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
		}
		id("grpckt") {
			artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				create("grpc")
				create("grpckt")
			}
			it.builtins {
				create("kotlin")
			}
		}
	}
}

kotlin {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
	sourceSets {
		getByName("main").kotlin.srcDir("$generatedSourcesPath/src/main/kotlin")
	}
}
sourceSets {
	getByName("main").kotlin.srcDir("$generatedSourcesPath/src/main/kotlin")
}
tasks.withType<KotlinCompile>().configureEach {
	dependsOn("openApiGenerate")
}


tasks.register("cleanClient") {
	doLast {
		delete(
			"$rootDir/generatedClient/README.md",
			"$rootDir/generatedClient/.swagger-codegen",
			"$rootDir/generatedClient/docs",
			"$rootDir/generatedClient/gradle",
			"$rootDir/generatedClient/src/main/kotlin/",
			"$rootDir/generatedClient/src/test/kotlin"
		)
	}
}

tasks.register("postProcess") {
	doLast {
		println("Post-processing...")
		println("PP1 - Make all Enums compare using equalsIgnoreCase(). Files affected:")
		exec {
			commandLine("sh", "-c", "find . -name \"*.java\" -exec sh -c 'grep --with-filename --line-number --color=always \"if (b.value.equals(input)) {\" {} && sed -i \"\" \"s/if (b.value.equals(input)) {/if (b.value.equalsIgnoreCase(input)) {/g\" {}' \\;")
		}
		println("PP2 - google-java-format")
		exec {
			commandLine("sh", "-c", "find . -name \"*.java\" -exec sh -c 'java -jar google-java-format-1.21.0-all-deps.jar --replace {} && echo {}' \\;")
		}
		println("Post-processing complete.")
	}
}

tasks.named("openApiGenerate") {
	dependsOn("cleanClient")
	finalizedBy("postProcess")
}



tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	testLogging {
		events("passed", "skipped", "failed", "standardOut", "standardError")
		showStandardStreams = true
	}
	timeout.set(Duration.ofMinutes(5))
}
