#!/bin/sh
sleep 10
mc alias set myminio http://localhost:9000 ROOTUSER CHANGEME123
mc mb myminio/ddm-bucket && echo "[init-bucket.sh] Bucket created." || echo "[init-bucket.sh] Bucket already exists or creation failed."
