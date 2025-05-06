package kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.dto.request.callback.AiAssistantJobCallbackRequest
import kr.ac.kookmin.cs.capstone.voicepack_platform.aiAssistant.entity.SynthesisStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest

@Service
class AiAssistantCallbackService(
    @Value("\${aws.sqs.ai-assistant-callback-queue-url}") private val aiAssistantQueueUrl: String,
    private val aiAssistantService: AiAssistantService,
    private val sqsClient: SqsClient,
    private val objectMapper: ObjectMapper,
    private val scope: CoroutineScope
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    data class AiAssistantCallbackMessage(
        val jobId: Long,
        val success: Boolean,
        val resultUrl: String?,
        val errorMessage: String?
    )

    @Scheduled(fixedDelay = 10000)
    fun consumeCallbackMessages() {
        scope.launch {
            logger.info("[ai-assistant] SQS 큐에서 콜백 메시지 소비 시작")
            listOf(
                async { processAiAssistantQueueMessages() }
            ).awaitAll()
            logger.info("[ai-assistant] SQS 큐에서 콜백 메시지 소비 완료")
        }
    }

    private suspend fun processAiAssistantQueueMessages() = coroutineScope {
        try {
            val receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(aiAssistantQueueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build()

            val messages = sqsClient.receiveMessage(receiveMessageRequest).messages()

            if (messages.isEmpty()) {
                logger.debug("ai-assistant 큐에 처리할 메시지가 없습니다.")
                return@coroutineScope
            }

            logger.info("[Parallel] ai-assistant 큐에서 ${messages.size}개의 메시지를 병렬로 처리합니다.")

            messages.map { message ->
                async(Dispatchers.IO) {
                    processAiAssistantMessage(message)
                }
            }.awaitAll()

        } catch (e: Exception) {
            logger.error("ai-assistant SQS 메시지 소비 중 오류 발생: ${e.message}", e)
        }
    }

    private suspend fun processAiAssistantMessage(message: Message) {
        val messageId = message.messageId()
        try {
            val callbackMessage: AiAssistantCallbackMessage = objectMapper.readValue(message.body())
            logger.info("[ai-assistant] 메시지 처리 시작: messageId={}, jobId={}, success={}, resultUrl={}, errorMessage={}",
                messageId, callbackMessage.jobId, callbackMessage.success, callbackMessage.resultUrl, callbackMessage.errorMessage)

            val jobStatus = if (callbackMessage.success) SynthesisStatus.SUCCESS else SynthesisStatus.FAILURE

            val callbackDto = AiAssistantJobCallbackRequest(
                jobId = callbackMessage.jobId,
                status = jobStatus,
                resultS3Key = callbackMessage.resultUrl
            )
            withContext(Dispatchers.IO) {
                aiAssistantService.handleSynthesisCallback(callbackDto)
            }

            deleteMessageFromQueue(aiAssistantQueueUrl, message.receiptHandle(), messageId, "ai-assistant")
            logger.info("[ai-assistant] 메시지 처리 완료 및 삭제: messageId={}", messageId)

        } catch (e: Exception) {
            logger.error("[ai-assistant] 메시지 처리 중 오류 발생: messageId={}, error={}", messageId, e.message, e)
        }
    }

    private suspend fun deleteMessageFromQueue(queueUrl: String, receiptHandle: String, messageId: String, queueType: String) {
        try {
            withContext(Dispatchers.IO) {
                val deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build()
                sqsClient.deleteMessage(deleteMessageRequest)
            }
            logger.debug("[{}] 메시지 삭제 성공: messageId={}", queueType, messageId)
        } catch (e: Exception) {
            logger.error("[{}] 메시지 삭제 중 오류 발생: messageId={}, error={}", queueType, messageId, e.message, e)
        }
    }
}