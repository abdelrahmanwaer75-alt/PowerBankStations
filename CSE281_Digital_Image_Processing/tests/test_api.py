import asyncio
from io import BytesIO

import cv2
import numpy as np
import pytest
from fastapi import HTTPException
from starlette.datastructures import Headers, UploadFile

from src.api.main import process_image


def upload_png():
    image = np.zeros((32, 32, 3), dtype=np.uint8)
    encoded, buffer = cv2.imencode(".png", image)
    assert encoded
    return UploadFile(
        filename="synthetic.png",
        file=BytesIO(buffer.tobytes()),
        headers=Headers({"content-type": "image/png"}),
    )


def test_valid_synthetic_image_returns_inspection_result():
    result = asyncio.run(process_image("powerbank", upload_png()))
    assert result["kind"] == "powerbank"
    assert result["image_width"] == 32
    assert result["image_height"] == 32
    assert result["mask_png_base64"]
    assert "not certified" in result["disclaimer"]


def test_invalid_kind_is_rejected():
    with pytest.raises(HTTPException) as error:
        asyncio.run(process_image("unknown", upload_png()))
    assert error.value.status_code == 400


def test_non_image_content_type_is_rejected():
    upload = UploadFile(
        filename="bad.png",
        file=BytesIO(b"not an image"),
        headers=Headers({"content-type": "text/plain"}),
    )
    with pytest.raises(HTTPException) as error:
        asyncio.run(process_image("powerbank", upload))
    assert error.value.status_code == 415
