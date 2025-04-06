package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackService
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.synthesis.dto.VoicepackCallbackRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest

@Service
class VoicepackCallbackService(
    @Value("\${aws.sqs.voicepack-creation-callback-queue-url}") private val creationQueueUrl: String,
    @Value("\${aws.sqs.voicepack-synthesis-callback-queue-url}") private val synthesisQueueUrl: String,
    private val voicepackService: VoicepackService,
    private val sqsClient: SqsClient,
    private val objectMapper: ObjectMapper,
    private val scope: CoroutineScope
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    data class CreationCallbackMessage(
        val voicepackRequestId: Long,
        val status: String
    )

    data class SynthesisCallbackMessage(
        val id: Long,
        val success: Boolean,
        val resultUrl: String?,
        val errorMessage: String?
    )

    @Scheduled(fixedDelay = 10000)
    fun consumeCallbackMessages() {
        scope.launch {
            logger.info("SQS 큐에서 콜백 메시지 소비 시작")
            listOf(
                async { processCreationQueueMessages() },
                async { processSynthesisQueueMessages() }
            ).awaitAll()
            logger.info("SQS 큐에서 콜백 메시지 소비 완료")
        }
    }

    private suspend fun processCreationQueueMessages() = coroutineScope {
        try {
            val receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(creationQueueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build()

            val messages = sqsClient.receiveMessage(receiveMessageRequest).messages()

            if (messages.isEmpty()) {
                logger.debug("Creation 큐에 처리할 메시지가 없습니다.")
                return@coroutineScope
            }

            logger.info("[Parallel] Creation 큐에서 ${messages.size}개의 메시지를 병렬로 처리합니다.")

            messages.map { message ->
                async(Dispatchers.IO) {
                    processCreationMessage(message)
                }
            }.awaitAll()

        } catch (e: Exception) {
            logger.error("Creation SQS 메시지 소비 중 오류 발생: ${e.message}", e)
        }
    }

    private suspend fun processSynthesisQueueMessages() = coroutineScope {
        try {
            val receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(synthesisQueueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build()

            val messages = sqsClient.receiveMessage(receiveMessageRequest).messages()

            if (messages.isEmpty()) {
                logger.debug("Synthesis 큐에 처리할 메시지가 없습니다.")
                return@coroutineScope
            }

            logger.info("[Parallel] Synthesis 큐에서 ${messages.size}개의 메시지를 병렬로 처리합니다.")

            messages.map { message ->
                async(Dispatchers.IO) {
                    processSynthesisMessage(message)
                }
            }.awaitAll()

        } catch (e: Exception) {
            logger.error("Synthesis SQS 메시지 소비 중 오류 발생: ${e.message}", e)
        }
    }

    private suspend fun processCreationMessage(message: Message) {
        val messageId = message.messageId()
        try {
            val callbackMessage: CreationCallbackMessage = objectMapper.readValue(message.body())
            logger.info("[Creation] 메시지 처리 시작: messageId={}, requestId={}, status={}",
                messageId, callbackMessage.voicepackRequestId, callbackMessage.status)

            // voicepackService.handleCreationCallback(
            //     callbackMessage.voicepackRequestId,
            //     callbackMessage.status
            // )
            logger.warn("Creation 콜백 코드가 주석처리되어 있습니다. 브랜치 병합 후 처리 필요합니다.")

            deleteMessageFromQueue(creationQueueUrl, message.receiptHandle(), messageId, "Creation")
            logger.info("[Creation] 메시지 처리 완료 및 삭제: messageId={}", messageId)

        } catch (e: Exception) {
            logger.error("[Creation] 메시지 처리 중 오류 발생: messageId={}, error={}", messageId, e.message, e)
        }
    }

    private suspend fun processSynthesisMessage(message: Message) {
        val messageId = message.messageId()
        try {
            val callbackMessage: SynthesisCallbackMessage = objectMapper.readValue(message.body())
            logger.info("[Synthesis] 메시지 처리 시작: messageId={}, id={}, success={}, resultUrl={}, errorMessage={}",
                messageId, callbackMessage.id, callbackMessage.success, callbackMessage.resultUrl, callbackMessage.errorMessage)

            val callbackRequest = VoicepackCallbackRequest(
                id = callbackMessage.id,
                success = callbackMessage.success,
                resultUrl = callbackMessage.resultUrl,
                errorMessage = callbackMessage.errorMessage
            )
            voicepackService.handleSynthesisCallback(callbackRequest)

            deleteMessageFromQueue(synthesisQueueUrl, message.receiptHandle(), messageId, "Synthesis")
            logger.info("[Synthesis] 메시지 처리 완료 및 삭제: messageId={}", messageId)

        } catch (e: Exception) {
            logger.error("[Synthesis] 메시지 처리 중 오류 발생: messageId={}, error={}", messageId, e.message, e)
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