package com.xebia.carbonaware.consumer

import com.xebia.carbonaware.consumer.queue.QueueService
import com.xebia.carbonaware.consumer.task.TaskProcessor
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class QueueConsumer(
    val taskProcessor: TaskProcessor,
    val queueService: QueueService
) {
    @EventListener
    fun consume(event: ApplicationReadyEvent) {
        while (true) {
            val task = queueService.receiveTask()
            task?.let {
                val result = taskProcessor.process(it)
                LOGGER.info("Task ${it.id} executed successfully. Result = ${result}.")
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(QueueConsumer::class.java)
    }
}