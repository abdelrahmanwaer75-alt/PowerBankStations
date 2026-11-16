# Image-processing service contract

The browser uploads images to Java; Java validates the basic upload metadata and forwards multipart field `file` to the image-processing service. Java limits uploads to 10 MB, and the Python API validates the actual image format and dimensions.

Supported kinds and paths:

| Kind | Java endpoint | Python endpoint |
|---|---|---|
| PowerBank exterior | `POST /api/inspections/powerbank` | `POST /process/powerbank` |
| Concrete infrastructure | `POST /api/inspections/infrastructure` | `POST /process/infrastructure` |
| Circuit board | `POST /api/inspections/pcb` | `POST /process/pcb` |

Success JSON contains image dimensions, candidate regions, candidate area, a base64-encoded PNG mask, and an inspection disclaimer. A dependent-service failure returns HTTP 502 through Java.

These are classical visual candidate detections, not certified damage, battery-health, or electrical-function diagnoses. No production ground-truth dataset is bundled.
