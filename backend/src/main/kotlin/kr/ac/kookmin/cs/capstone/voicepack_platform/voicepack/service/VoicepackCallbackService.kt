package kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kr.ac.kookmin.cs.capstone.voicepack_platform.voicepack.VoicepackService
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
    private val sqsClient: SqsClient,
    private val objectMapper: ObjectMapper,
    private val voicepackService: VoicepackService,
    @Value("\${aws.sqs.voicepack-creation-callback-queue-url}") private val creationQueueUrl: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Creation 콜백 메시지 구조
    data class CreationCallbackMessage(
        val voicepackRequestId: Long,
        val status: String
    )

    /**
     * SQS 큐에서 메시지를 주기적으로 소비하여 콜백을 처리합니다.
     * 10초마다 실행됩니다.
     */
    @Scheduled(fixedDelay = 10000)
    fun consumeCallbackMessages() {
        logger.info("SQS 큐에서 콜백 메시지 소비 시작")
        processCreationQueueMessages()
    }
    
    private fun processCreationQueueMessages() {
        try {
            val receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(creationQueueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build()
            
            val messages = sqsClient.receiveMessage(receiveMessageRequest).messages()
            
            if (messages.isEmpty()) {
                logger.debug("Creation 큐에 처리할 메시지가 없습니다.")
                return
            }
            
            logger.info("Creation 큐에서 ${messages.size}개의 메시지를 처리합니다.")
            
            for (message in messages) {
                processCreationMessage(message)
            }
        } catch (e: Exception) {
            logger.error("Creation SQS 메시지 소비 중 오류 발생: ${e.message}", e)
        }
    }
    
    private fun processCreationMessage(message: Message) {
        try {
            val callbackMessage = objectMapper.readValue<CreationCallbackMessage>(message.body())
            logger.info("Creation 콜백 메시지 처리: requestId=${callbackMessage.voicepackRequestId}, status=${callbackMessage.status}")
            
            // VoicepackService의 handleCallback 메서드는 voicepackRequestId와 status만 받음
            voicepackService.handleCreationCallback(
                callbackMessage.voicepackRequestId,
                callbackMessage.status
            )
            
            // 메시지 삭제
            val deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(creationQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build()
            
            sqsClient.deleteMessage(deleteMessageRequest)
            logger.info("Creation 메시지 처리 완료 및 삭제: messageId=${message.messageId()}")
        } catch (e: Exception) {
            logger.error("Creation 메시지 처리 중 오류 발생: messageId=${message.messageId()}, error=${e.message}", e)
        }
    }
} 