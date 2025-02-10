dependencies {
    implementation(project(":domain"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("javax.validation:validation-api:_")
    implementation("org.hibernate.validator:hibernate-validator:_")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:_")
}