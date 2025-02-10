dependencies {
    implementation("org.springframework:spring-context:_")
    implementation("org.springframework.security:spring-security-crypto:_")
    implementation("io.jsonwebtoken:jjwt-api:_")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:_")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:_")

    testImplementation("org.mockito:mockito-core:_")
    testImplementation("org.mockito:mockito-junit-jupiter:_")
    testImplementation("org.assertj:assertj-core:_")
    testImplementation("org.junit.jupiter:junit-jupiter-api:_")
    testImplementation("org.junit.jupiter:junit-jupiter-params:_")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:_")
}

tasks.test {
    useJUnitPlatform()
}