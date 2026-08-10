package com.mirabilis.core

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** `:core` is pure Kotlin/JVM (ADR-0003): only `javax.inject` + coroutines are allowed. */
class CorePurityTest {

    @Test
    fun `core sources import no framework packages`() {
        val srcDir = File("src/main/kotlin")
        assertTrue("expected core sources at ${srcDir.absolutePath}", srcDir.exists())

        val forbidden = Regex(
            "^import (android\\.|androidx\\.|retrofit2\\.|okhttp3\\.|" +
                "com\\.google\\.protobuf|dagger\\.|kotlinx\\.serialization)",
        )
        val offenders = srcDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file -> file.readLines().filter { forbidden.containsMatchIn(it.trim()) }.map { "${file.name}: $it" } }
            .toList()

        assertTrue(
            "Core must stay framework-free (ADR-0003). Offending imports:\n${offenders.joinToString("\n")}",
            offenders.isEmpty(),
        )
    }
}
