#!/bin/sh
sleep 10
mc alias set myminio http://localhost:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD"
mc mb myminio/ddm-bucket && echo "[init-bucket.sh] Bucket created." || echo "[init-bucket.sh] Bucket already exists or creation failed."
