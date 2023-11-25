package nl.bjornvanderlaan.eventdrivenscaling.producer.simulation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimulationTest {
    private val simulation = Simulation()

    @Test
    fun `Test constant requests pattern`() = runTest {
        val startTime = currentTime

        simulation.addConstantRequestsStep(2, 5)
        val counter = AtomicInteger(0)

        simulation.executeWith { counter.incrementAndGet() }

        val endTime = currentTime
        assertEquals(endTime - startTime, 2000L, "Expected elapsed time equals 2 seconds")

        assertEquals(10, counter.get(), "Expected 10 requests")
    }

    @Test
    fun `Test linear requests pattern`() = runTest {
        val startTime = currentTime

        simulation.addLinearRequestsStep(2, 2, 4)
        val counter = AtomicInteger(0)

        simulation.executeWith { counter.incrementAndGet() }

        val endTime = currentTime
        assertEquals(endTime - startTime, 2000L, "Expected elapsed time equals 2 seconds")

        assertEquals(6, counter.get(), "Expected 6 requests")
    }

    @Test
    fun `Test pause pattern`() = runTest {
        val startTime = currentTime

        simulation.addPauseStep(5)
        val counter = AtomicInteger(0)

        simulation.executeWith { counter.incrementAndGet() }

        val endTime = currentTime
        assertEquals(endTime - startTime, 5000L, "Expected elapsed time equals 5 seconds")

        assertEquals(0, counter.get(), "Expected 0 requests")
    }

    @Test
    fun `Test all patterns together`() = runTest {
        val startTime = currentTime

        simulation.addConstantRequestsStep(3, 5)
        simulation.addPauseStep(5)
        simulation.addLinearRequestsStep(2, 2, 4)
        val counter = AtomicInteger(0)

        simulation.executeWith { counter.incrementAndGet() }

        val endTime = currentTime
        assertEquals(endTime - startTime, 10000L, "Expected elapsed time equals 10 seconds")

        assertEquals(21, counter.get(), "Expected 16 requests")
    }
}