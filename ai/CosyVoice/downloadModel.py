from google.cloud import storage
import os

def downloadFolder(bucket_name, folder_name, destination_folder):
    # GCS 클라이언트 생성
    client = storage.Client()
    bucket = client.bucket(bucket_name)

    # 폴더 내의 모든 파일 나열
    blobs = bucket.list_blobs(prefix=folder_name)  # 폴더 내의 모든 파일을 가져옵니다.

    # 다운로드할 폴더 생성
    os.makedirs(destination_folder, exist_ok=True)

    for blob in blobs:
        # 파일 경로 설정
        file_path = os.path.join(destination_folder, blob.name[len(folder_name):])  # 폴더 이름을 제외한 경로

        if os.path.exists(file_path):
            print(f"{file_path} 파일이 이미 존재합니다. 다운로드를 건너뜁니다.")
            continue  # 파일이 존재하면 다음 파일로 넘어감
        
        os.makedirs(os.path.dirname(file_path), exist_ok=True)  # 필요한 경우 디렉토리 생성

        # 파일 다운로드
        blob.download_to_filename(file_path)
        print(f"{blob.name} 다운로드 완료.")