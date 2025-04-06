import json
import logging
import boto3
from botocore.exceptions import ClientError
from config.settings import AWS_CONFIG

logger = logging.getLogger(__name__)

class SQSHandler:
    def __init__(self):
        self.sqs_client = boto3.client(
            'sqs',
            aws_access_key_id=AWS_CONFIG['access_key_id'],
            aws_secret_access_key=AWS_CONFIG['secret_access_key'],
            region_name=AWS_CONFIG['region']
        )
        self.register_queue_url = AWS_CONFIG['register_queue_url']
        self.synthesize_queue_url = AWS_CONFIG['synthesize_queue_url']

    async def send_register_message(
        self,
        voicepackRequestId: int,
        status: str,
        additional_params: dict = None
    ):
        """SQS에 메시지를 전송하는 함수
        
        Args:
            voicepackRequestId (int): 음성팩 요청 ID
            status (str): 상태 ('success' 또는 'failed')
            additional_params (dict): 추가 파라미터
        """
        try:
            # 메시지 본문 구성
            message_body = {
                'voicepackRequestId': voicepackRequestId,
                'status': status
            }
            
            # 추가 파라미터가 있다면 병합
            if additional_params:
                message_body.update(additional_params)

            # SQS에 메시지 전송
            response = self.sqs_client.send_message(
                QueueUrl=self.register_queue_url,
                MessageBody=json.dumps(message_body)
            )

            logger.info(f"Message sent to SQS: {message_body}")
            return response

        except ClientError as e:
            logger.error(f"Failed to send message to SQS: {str(e)}")
            raise 
        
    async def send_synthesize_message(
        self,
        jobId: int,
        success: bool,
        additional_params: dict = None
    ):
        """SQS에 메시지를 전송하는 함수
        
        Args:
            voicepackId (str): 음성팩 ID
            voicepackRequestId (int): 음성팩 요청 ID
            status (str): 상태 ('success' 또는 'failed')
            additional_params (dict): 추가 파라미터
        """
        try:
            # 메시지 본문 구성
            message_body = {
                'jobId': jobId,
                'success': success
            }
            
            # 추가 파라미터가 있다면 병합
            if additional_params:
                message_body.update(additional_params)  
        
            # SQS에 메시지 전송
            response = self.sqs_client.send_message(
                QueueUrl=self.synthesize_queue_url,
                MessageBody=json.dumps(message_body)
            )
            
            logger.info(f"Message sent to SQS: {message_body}")
            return response
        
        except ClientError as e:
            logger.error(f"Failed to send message to SQS: {str(e)}")
            raise 
