import torch
import os
import json
import logging

logger = logging.getLogger(__name__)

class SpeakerManager:
    def __init__(self, save_dir: str):
        """화자 특징 관리자 초기화"""
        self.save_dir = save_dir
        os.makedirs(save_dir, exist_ok=True)
        self.speaker_ids = set()  # 화자 ID만 저장
        self._load_speaker_ids()
        print(self.speaker_ids)
    
    def _load_speaker_ids(self):
        """저장된 화자 ID 목록만 로드"""
        for filename in os.listdir(self.save_dir):
            if filename.startswith("speakers_") and filename.endswith(".json"):
                speaker_id = filename[9:-5]  # "speakers_"와 ".json" 제거
                self.speaker_ids.add(speaker_id)
        logger.info(f"Found {len(self.speaker_ids)} speakers")
    
    def save_speaker(self, speaker_id: str, features: dict):
        """화자의 특징을 개별 파일로 저장"""
        features_to_save = {}
        for key, value in features.items():
            if isinstance(value, torch.Tensor):
                features_to_save[key] = value.cpu().numpy().tolist()
            else:
                features_to_save[key] = value
        
        # 개별 파일로 저장
        with open(os.path.join(self.save_dir, f"speakers_{speaker_id}.json"), "w") as f:
            json.dump(features_to_save, f)
        
        self.speaker_ids.add(speaker_id)
        logger.info(f"Saved features for speaker {speaker_id}")
        print(self.speaker_ids)
    
    def get_speaker(self, speaker_id: str) -> dict:
        """특정 화자의 특징을 파일에서 로드"""
        if not self.has_speaker(speaker_id):
            raise KeyError(f"Speaker {speaker_id} not found")
        
        # 해당 화자의 파일에서 특징 로드
        with open(os.path.join(self.save_dir, f"speakers_{speaker_id}.json"), "r") as f:
            features_data = json.load(f)
        
        # 리스트를 텐서로 변환
        features = {}
        for key, value in features_data.items():
            if isinstance(value, list):
                features[key] = torch.tensor(value)
            else:
                features[key] = value
        
        return features
    
    def has_speaker(self, speaker_id: str) -> bool:
        """화자 ID가 존재하는지 확인"""
        return speaker_id in self.speaker_ids 