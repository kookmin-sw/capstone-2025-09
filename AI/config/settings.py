import os
from dotenv import load_dotenv

load_dotenv()

# AWS 설정
AWS_CONFIG = {
    'bucket_name': os.getenv('AWS_BUCKET_NAME'),
    'region': os.getenv('AWS_DEFAULT_REGION'),
    'access_key_id': os.getenv('AWS_ACCESS_KEY_ID'),
    'secret_access_key': os.getenv('AWS_SECRET_ACCESS_KEY'),
    'register_queue_url': os.getenv('AWS_SQS_REGISTER_QUEUE_URL'),
    'synthesize_queue_url': os.getenv('AWS_SQS_SYNTHESIZE_QUEUE_URL')
}

# 모델 설정
MODEL_CONFIG = {
    'path': "./pretrained_models/CosyVoice2-0.5B",
    'model_id': 'iic/CosyVoice2-0.5B',
    'sample_rate': 16000
} 