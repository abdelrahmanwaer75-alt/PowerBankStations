import base64
from io import BytesIO

import cv2
import numpy as np
from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image, UnidentifiedImageError

from ..processing import analyze_image

MAX_UPLOAD_BYTES = 10 * 1024 * 1024
MAX_IMAGE_DIMENSION = 4096
MAX_IMAGE_PIXELS = 16_777_216
ALLOWED_FORMATS = {"JPEG", "PNG", "WEBP"}

app = FastAPI(title="PowerBank Classical Image Processing", version="1.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


async def process_image(kind: str, file: UploadFile) -> dict[str, object]:
    if kind not in {"powerbank", "infrastructure", "pcb"}:
        raise HTTPException(
            status_code=400,
            detail="kind must be powerbank, infrastructure, or pcb",
        )
    if file.content_type is None or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=415, detail="Upload must use an image content type")

    raw = await file.read(MAX_UPLOAD_BYTES + 1)
    if len(raw) > MAX_UPLOAD_BYTES:
        raise HTTPException(status_code=413, detail="Image exceeds the 10 MB upload limit")

    try:
        with Image.open(BytesIO(raw)) as uploaded_image:
            if uploaded_image.format not in ALLOWED_FORMATS:
                raise HTTPException(status_code=415, detail="Only JPEG, PNG, and WebP images are supported")
            width, height = uploaded_image.size
            if (
                width < 1
                or height < 1
                or width > MAX_IMAGE_DIMENSION
                or height > MAX_IMAGE_DIMENSION
                or width * height > MAX_IMAGE_PIXELS
            ):
                raise HTTPException(status_code=413, detail="Image dimensions exceed the processing limit")
            rgb_image = np.asarray(uploaded_image.convert("RGB"))
    except HTTPException:
        raise
    except (UnidentifiedImageError, OSError, Image.DecompressionBombError) as exception:
        raise HTTPException(status_code=400, detail="Unsupported or invalid image") from exception

    bgr_image = cv2.cvtColor(rgb_image, cv2.COLOR_RGB2BGR)
    try:
        result = analyze_image(bgr_image, kind)
    except ValueError as exception:
        raise HTTPException(status_code=400, detail=str(exception)) from exception

    encoded_ok, encoded_mask = cv2.imencode(".png", result.pop("mask"))
    if not encoded_ok:
        raise HTTPException(status_code=500, detail="Could not encode the inspection mask")
    result["mask_png_base64"] = base64.b64encode(encoded_mask.tobytes()).decode("ascii")
    result["disclaimer"] = (
        "Classical image-processing candidate detection only; not certified safety, "
        "battery, or electrical diagnosis."
    )
    return result


@app.post("/process/powerbank")
async def process_powerbank(file: UploadFile = File(...)) -> dict[str, object]:
    return await process_image("powerbank", file)


@app.post("/process/infrastructure")
async def process_infrastructure(file: UploadFile = File(...)) -> dict[str, object]:
    return await process_image("infrastructure", file)


@app.post("/process/pcb")
async def process_pcb(file: UploadFile = File(...)) -> dict[str, object]:
    return await process_image("pcb", file)


@app.post("/inspect/{kind}", include_in_schema=False)
async def inspect_legacy(kind: str, file: UploadFile = File(...)) -> dict[str, object]:
    return await process_image(kind, file)
