import boto3
from botocore.exceptions import ClientError
import json
import numpy as np
import logging
from config.settings import AWS_CONFIG
import io
import torch

logger = logging.getLogger(__name__)

class StorageManager:
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

    def save_audio(self, 
                  audio_data: bytes, 
                  file_path: str) -> str:
        """음성 파일을 S3에 저장"""
        try:
            self.s3_client.put_object(
                Bucket=self.bucket_name,
                Key=file_path,
                Body=audio_data
            )

            logger.info(f"audio file saved: {file_path}")
            return file_path
        
        except Exception as e:
            logger.error(f"failed to save audio file: {str(e)}")
            return None

    def save_speaker_features(self, voicepackId: str, features: torch.Tensor) -> bool:
        """화자의 특징을 S3에 저장"""
        try:
            features = features.float()
            json_features = self._convert_tensor_to_list(features)
            features_json = json.dumps(json_features)
            
            key = f"speakers/{voicepackId}/features.json"
            
            self.s3_client.put_object(
                Bucket=self.bucket_name,
                Key=key,
                Body=features_json
            )

            return True
        
        except Exception as e:
            logger.error(f"failed to save speaker features to s3: {str(e)}")
            return False

    def get_speaker_features(self, voicepackId: str) -> torch.Tensor:
        """S3에서 화자의 특징 불러오기"""
        try:
            key = f"speakers/{voicepackId}/features.json"
            response = self.s3_client.get_object(
                Bucket=self.bucket_name,
                Key=key
            )
            
            json_features = json.loads(response['Body'].read().decode('utf-8'))
            features = self._convert_list_to_tensor(json_features)
            features = features.to(torch.bfloat16)
            return features
        
        except Exception as e:
            logger.error(f"failed to load speaker features: {str(e)}")
            return None

    def save_generated_audio(self, 
                            voicepackId: str, 
                            audio_data: bytes, 
                            filename: str,
                            userId: int) -> str:
        """기능 1: 베이직 기능"""
        file_path = f"generated_audio/{userId}/{voicepackId}/{filename}"
        url = self.save_audio(audio_data, file_path)
        return url

    def save_speaker_test_audio(self, 
                              voicepackId: str, 
                              audio_data: bytes, 
                              filename: str) -> bool:
        """보이스팩 샘플 음성을 s3에 저장"""
        file_path = f"speakers/{voicepackId}/{filename}"
        url = self.save_audio(audio_data, file_path)
        return url is not None 

    def speaker_exists(self, voicepackId: str) -> bool:
        """화자 존재 여부 확인"""
        try:
            key = f"speakers/{voicepackId}/features.json"
            self.s3_client.head_object(
                Bucket=self.bucket_name,
                Key=key
            )
            return True
        except ClientError:
            return False

    def get_audio_url(self, file_path: str) -> bool:
        """S3에서 오디오 파일의 존재 여부를 확인하고 URL 반환
        
        Args:
            file_path (str): S3 버킷 내의 파일 경로
            
        Returns:
            bool: 파일이 존재하면 True, 없으면 False 반환
        """
        try:
            self.s3_client.head_object(
                Bucket=self.bucket_name,
                Key=file_path
            )
            return True
        except ClientError:
            return False