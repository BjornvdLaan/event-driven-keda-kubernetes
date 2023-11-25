package nl.bjornvanderlaan.eventdrivenscaling.producer

import nl.bjornvanderlaan.eventdrivenscaling.producer.queue.QueueService
import nl.bjornvanderlaan.eventdrivenscaling.producer.simulation.simulation
import nl.bjornvanderlaan.eventdrivenscaling.producer.simulation.executeWith
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TaskProducer(val queueService: QueueService) {

    @EventListener(ApplicationReadyEvent::class)
    suspend fun startSimulation() {
        val simulation = simulation {
            10.seconds constantNumberOfRequests 10
            5.seconds.pause()
            15.seconds linearlyGrowingNumberOfRequestsFrom (1 to 3)
        }

        simulation.executeWith { produceRandomTask()}
    }

    fun produceRandomTask() {
        val result = queueService.publishTask(Task.randomTask())
        return if (result != null) {
            LOGGER.info("Task is successfully published in message ${result.messageId}.")
        } else {
            LOGGER.error("Task publishing failed.")
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(QueueService::class.java)
    }
}