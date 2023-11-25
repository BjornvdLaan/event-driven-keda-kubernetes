package nl.bjornvanderlaan.eventdrivenscaling.producer.simulation

import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

typealias RequestAction = () -> Unit
typealias SimulationStep = suspend (RequestAction) -> Unit

class Simulation {
    private val steps: MutableList<SimulationStep> = mutableListOf()

    fun addConstantRequestsStep(durationSeconds: Int, requestsPerSecond: Int) {
        steps.add { requestAction ->
            repeat(durationSeconds) {
                val executionTime = measureTimeMillis {
                    repeat(requestsPerSecond) {
                        requestAction.invoke()
                    }
                }
                delay(1000L - executionTime)
            }

            println("""Applied constant traffic of $requestsPerSecond
            requests per second for $durationSeconds seconds.""")
        }
    }

    fun addLinearRequestsStep(durationSeconds: Int, startRequestsPerSecond: Int, endRequestsPerSecond: Int) {
        steps.add { requestAction ->
            val slope = (endRequestsPerSecond - startRequestsPerSecond) / (durationSeconds - 1)

            repeat(durationSeconds) {
                val executionTime = measureTimeMillis {
                    repeat(startRequestsPerSecond + (slope * it)) {
                        requestAction.invoke()
                    }
                }
                delay(1000L - executionTime)
            }
            println("""Applied linear traffic, growing from $startRequestsPerSecond to
                to $endRequestsPerSecond requests per second for $durationSeconds seconds.""")
        }
    }

    fun addPauseStep(durationSeconds: Int) {
        steps.add {
            delay(durationSeconds * 1000L)
            println("Paused for $durationSeconds seconds.")
        }
    }

    suspend fun start(requestAction: RequestAction) {
        println("""
         +-+-+-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+-+-+
         |S|i|m|u|l|a|t|i|o|n| |s|t|a|r|t|e|d|.|.|.|
         +-+-+-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+-+-+
        """.trimIndent())

        val executionTime = measureTimeMillis {
            steps.map {
                it.invoke(requestAction)
                println("")
            }
        }

        println("""
         +-+-+-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+-+-+
         |S|i|m|u|l|a|t|i|o|n| |c|o|m|p|l|e|t|e|d|!|
         |Run time: $executionTime milliseconds.
         +-+-+-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+-+-+
        """.trimIndent())
    }

}
suspend fun Simulation.executeWith(requestAction: RequestAction) {
    this.start(requestAction)
}
