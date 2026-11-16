# CSE281 – Digital Image Processing

This module provides classical OpenCV/NumPy candidate detection for visual inspection. It does not diagnose battery health or electrical functionality.

## Structure

- `src/processing.py` – NumPy mean filtering, Otsu threshold calculation, candidate-region analysis, and IoU calculation.
- `src/api/main.py` – upload validation and FastAPI inspection endpoints.
- `datasets/` – dataset licensing and evaluation guidance; no external dataset is bundled.
- `experiments/run_synthetic_experiment.py` – smoke experiment only; synthetic data is not academic accuracy evidence.
- `tests/test_processing.py` – synthetic unit tests.

## Run and test

```powershell
python -m pip install -r requirements.txt
python -m pytest -q
python -m uvicorn src.api.main:app --reload --port 8002
```

Standalone API: `GET /health` and multipart `POST /process/powerbank`, `/process/infrastructure`, or `/process/pcb` with the image in `file`. Uploads are limited to 10 MB, JPEG/PNG/WebP, 4096 pixels per dimension, and 16,777,216 total pixels. The old `/inspect/{kind}` path remains as a compatibility alias. The Java backend forwards requests through `/api/inspections/{kind}`.

No measured accuracy is reported because a licensed, labeled evaluation set is not present. Use verified labels and record per-image and aggregate metrics before making performance claims.
