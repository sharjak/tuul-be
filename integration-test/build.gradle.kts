dependencies {
    implementation(project(":app"))
    implementation(project(":domain"))
    implementation(project(":adapters:web"))

    implementation("org.springframework.boot:spring-boot-starter-web:_")

    testImplementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test:_")
    testImplementation("com.google.firebase:firebase-admin:_")
    testImplementation("javax.validation:validation-api:_")
    testImplementation("org.hibernate.validator:hibernate-validator:_")

    testImplementation("org.junit.jupiter:junit-jupiter-api:_")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:_")

    testImplementation("org.mockito:mockito-core:_")
    testImplementation("org.mockito:mockito-junit-jupiter:_")
}

tasks.test {
    useJUnitPlatform()
}
