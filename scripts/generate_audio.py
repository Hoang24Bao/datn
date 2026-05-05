import sys
import tempfile
import os
import json
import re
import hashlib
import time
from gtts import gTTS
import cloudinary
import cloudinary.uploader

# ========== CẤU HÌNH CLOUDINARY ==========
CLOUDINARY_CONFIG = {
    "cloud_name": "dki3iui1t",
    "api_key": "922742958732767",  # THAY BẰNG API KEY THẬT
    "api_secret": "ygHiyEgl8bShKb0mtLQv7NC1m5M"  # THAY BẰNG API SECRET THẬT
}

cloudinary.config(
    cloud_name=CLOUDINARY_CONFIG["cloud_name"],
    api_key=CLOUDINARY_CONFIG["api_key"],
    api_secret=CLOUDINARY_CONFIG["api_secret"]
)


def sanitize_filename(name):
    return re.sub(r'[^a-z0-9]', '_', name.lower().strip())


def generate_and_upload(romaji, vocab_id):
    try:
        base_name = sanitize_filename(romaji)

        # Tạo mã ngẫu nhiên
        unique_suffix = hashlib.md5(f"{romaji}_{vocab_id}_{time.time()}".encode()).hexdigest()[:8]
        public_id = f"{base_name}_{unique_suffix}"

        with tempfile.NamedTemporaryFile(delete=False, suffix=".mp3") as tmp_file:
            temp_path = tmp_file.name

        tts = gTTS(text=romaji, lang='ja', slow=False)
        tts.save(temp_path)

        # Upload lên Cloudinary (không folder)
        upload_result = cloudinary.uploader.upload(
            temp_path,
            resource_type="video",
            folder="audios",
            public_id=public_id,
            format="mp3"
        )

        os.unlink(temp_path)

        audio_url = upload_result['secure_url']
        print(audio_url)

    except Exception as e:
        print(json.dumps({"error": str(e)}), file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(json.dumps({"error": "Thiếu tham số"}), file=sys.stderr)
        sys.exit(1)

    romaji = sys.argv[1]
    vocab_id = sys.argv[2]

    generate_and_upload(romaji, vocab_id)
