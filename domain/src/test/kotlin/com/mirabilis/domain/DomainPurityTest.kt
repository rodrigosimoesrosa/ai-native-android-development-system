package com.mirabilis.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the constitution/ADR-0003 rule that `:domain` is a **pure** module — no Android, no
 * networking, no persistence framework. The Gradle module graph already enforces this at compile
 * time (these libraries aren't on the classpath); this test makes the guarantee explicit and
 * fails loudly if someone adds a forbidden dependency and import.
 */
class DomainPurityTest {

    @Test
    fun `domain sources import no framework packages`() {
        val srcDir = File("src/main/kotlin")
        assertTrue("expected domain sources at ${srcDir.absolutePath}", srcDir.exists())

        val forbidden = Regex(
            "^import (android\\.|androidx\\.|retrofit2\\.|okhttp3\\.|" +
                "com\\.google\\.protobuf|dagger\\.|kotlinx\\.serialization)",
        )
        val offenders = srcDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().filter {
                    forbidden.containsMatchIn(
                        it.trim()
                    )
                }.map { "${file.name}: $it" }
            }
            .toList()

        assertTrue(
            "Domain must stay framework-free (ADR-0003). Offending imports:\n${offenders.joinToString("\n")}",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `profile package is present so the purity walk actually covers it`() {
        // Guards that the framework-free scan above is not vacuously green for the profile feature
        // (spec 002): the package must exist with sources for the walk to have inspected them.
        val profileDir = File("src/main/kotlin/com/mirabilis/domain/profile")
        val sources = profileDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

        assertTrue(
            "expected pure :domain/profile sources at ${profileDir.absolutePath}",
            sources.isNotEmpty(),
        )
    }
}
