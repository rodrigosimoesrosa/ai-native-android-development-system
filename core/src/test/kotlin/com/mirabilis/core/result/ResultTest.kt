package com.mirabilis.core.result

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun `fold routes success to onSuccess`() {
        val r: Result<Int> = Result.Success(42)
        val out = r.fold(onError = { -1 }, onSuccess = { it * 2 })
        assertEquals(84, out)
    }

    @Test
    fun `fold routes error to onError`() {
        val r: Result<Int> = Result.Error(AppError.Network)
        val out = r.fold(onError = { "err" }, onSuccess = { "ok" })
        assertEquals("err", out)
    }

    @Test
    fun `map transforms success and passes error through`() {
        assertEquals(Result.Success(4), Result.Success(2).map { it * 2 })
        val err: Result<Int> = Result.Error(AppError.Database)
        assertEquals(err, err.map { it * 2 })
    }

    @Test
    fun `getOrNull returns data or null`() {
        assertEquals(7, Result.Success(7).getOrNull())
        assertNull(Result.Error(AppError.EmptyData).getOrNull())
    }

    @Test
    fun `asResult emits Loading then Success`() = runTest {
        val emissions = flow { emit(99) }.asResult().toList()
        assertEquals(FlowResult.Loading, emissions.first())
        assertEquals(FlowResult.Success(99), emissions.last())
    }

    @Test
    fun `asResult emits Error on throw`() = runTest {
        val emissions = flow<Int> { throw IllegalStateException("boom") }.asResult().toList()
        assertEquals(FlowResult.Loading, emissions.first())
        assertTrue(emissions.last() is FlowResult.Error)
    }
}
