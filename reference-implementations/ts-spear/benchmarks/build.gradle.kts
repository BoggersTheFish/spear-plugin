val jmh: Configuration by configurations.creating

dependencies {
    implementation(project(":core"))
    jmh("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

sourceSets {
    create("jmh") {
        java.srcDir("src/jmh/java")
        compileClasspath += sourceSets["main"].compileClasspath + sourceSets["main"].output + jmh
        runtimeClasspath += output + compileClasspath
    }
}

tasks.named<JavaCompile>("compileJmhJava") {
    options.annotationProcessorPath = configurations.annotationProcessor.get()
}

tasks.register<JavaExec>("jmh") {
    group = "benchmark"
    description = "Run JMH benchmarks"
    dependsOn("compileJmhJava", "classes")
    classpath = sourceSets["jmh"].runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")
}