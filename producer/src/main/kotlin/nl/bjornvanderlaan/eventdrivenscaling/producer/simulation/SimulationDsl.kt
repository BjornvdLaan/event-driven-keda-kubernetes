package nl.bjornvanderlaan.eventdrivenscaling.producer.simulation

class SimulationDsl {
    private val simulation = Simulation()

    inner class SimulationStepDsl(private val durationSeconds: Int) {

        infix fun linearlyGrowingNumberOfRequestsFrom(startAndEndRequestsPerSecond: Pair<Int, Int>) {
            val (start, end) = startAndEndRequestsPerSecond
            simulation.addLinearRequestsStep(durationSeconds, start, end)
        }

        infix fun constantNumberOfRequests(requestsPerSecond: Int) {
            simulation.addConstantRequestsStep(durationSeconds, requestsPerSecond)
        }

        fun pause() {
            simulation.addPauseStep(durationSeconds)
        }
    }

    val Int.seconds
        get() = SimulationStepDsl(this)


    fun build() = simulation
}

fun simulation(description: SimulationDsl.() -> Unit): Simulation {
    val simulationBuilder = SimulationDsl()
    simulationBuilder.apply(description)
    return simulationBuilder.build()
}
