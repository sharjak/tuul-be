dependencies {
	implementation(project(":adapters:web"))
	implementation(project(":adapters:firestore"))
	implementation("org.springframework.boot:spring-boot-starter:_")

	testImplementation("org.springframework.boot:spring-boot-starter-test:_") {
		exclude(group = "org.mockito")
	}

	testImplementation("org.junit.jupiter:junit-jupiter-api:_")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:_")
}

tasks.test {
	useJUnitPlatform()
}

tasks.named<Jar>("jar") {
	enabled = true
}
