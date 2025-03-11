import boto3
from botocore.exceptions import ClientError
import json
import numpy as np
import logging
from config.settings import AWS_CONFIG
import io
import torch

logger = logging.getLogger(__name__)

class S3Service:
    def __init__(self):
        """S3 서비스 초기화"""
        self.s3_client = boto3.client(
            's3',
            region_name=AWS_CONFIG['region'],
            aws_access_key_id=AWS_CONFIG['access_key_id'],
            aws_secret_access_key=AWS_CONFIG['secret_access_key']
        )
        self.bucket_name = AWS_CONFIG['bucket_name']

    def _convert_tensor_to_list(self, obj):
        """PyTorch 텐서를 리스트로 변환"""
        if torch.is_tensor(obj):
            return obj.cpu().numpy().tolist()
        elif isinstance(obj, dict):
            return {key: self._convert_tensor_to_list(value) for key, value in obj.items()}
        elif isinstance(obj, list):
            return [self._convert_tensor_to_list(item) for item in obj]
        return obj

    def _convert_list_to_tensor(self, obj, device='cpu'):
        """리스트를 PyTorch 텐서로 변환"""
        if isinstance(obj, dict):
            return {key: self._convert_list_to_tensor(value, device) for key, value in obj.items()}
        elif isinstance(obj, list):
            try:
                return torch.tensor(obj, device=device)
            except:
                return [self._convert_list_to_tensor(item, device) for item in obj]
        return obj

    def save_speaker_features(self, speaker_id: str, features: dict) -> bool:
        """화자의 특징을 S3에 저장"""
        try:
            json_features = self._convert_tensor_to_list(features)
            features_json = json.dumps(json_features)
            
            key = f"speakers/{speaker_id}/features.json"
            
            self.s3_client.put_object(
                Bucket=self.bucket_name,
                Key=key,
                Body=features_json
            )
            logger.info(f"화자 특징 저장 완료: {speaker_id}")
            return True
        except Exception as e:
            logger.error(f"화자 특징 저장 실패: {str(e)}")
            return False

    def get_speaker_features(self, speaker_id: str) -> dict:
        """S3에서 화자의 특징 불러오기"""
        try:
            key = f"speakers/{speaker_id}/features.json"
            response = self.s3_client.get_object(
                Bucket=self.bucket_name,
                Key=key
            )
            
            json_features = json.loads(response['Body'].read().decode('utf-8'))
            features = self._convert_list_to_tensor(json_features)
            
            return features
        except Exception as e:
            logger.error(f"화자 특징 로드 실패: {str(e)}")
            return None

    def save_generated_audio(self, speaker_id: str, audio_data: bytes, filename: str) -> str:
        """생성된 음성을 S3에 저장"""
        try:
            key = f"generated_audio/{speaker_id}/{filename}"
            
            self.s3_client.put_object(
                Bucket=self.bucket_name,
                Key=key,
                Body=audio_data
            )
            
            # S3 URL 생성
            url = f"https://{self.bucket_name}.s3.{AWS_CONFIG['region']}.amazonaws.com/{key}"
            logger.info(f"생성된 음성 저장 완료: {url}")
            return url
        except Exception as e:
            logger.error(f"생성된 음성 저장 실패: {str(e)}")
            return None

    def speaker_exists(self, speaker_id: str) -> bool:
        """화자 존재 여부 확인"""
        try:
            key = f"speakers/{speaker_id}/features.json"
            self.s3_client.head_object(
                Bucket=self.bucket_name,
                Key=key
            )
            return True
        except ClientError:
            return False 