# AI service contract

The browser sends route requests through the Java API. The Java service forwards JSON to the AI service with a 15-second read timeout.

## Route search

- Java: `POST /api/ai/find-route`
- Python: `POST /find-route`
- Request: `{"start":"A","goal":"D","algorithm":"astar"}` where `algorithm` is `ucs` or `astar`.
- Success response includes `path`, `cost`, `explored`, `runtime_ms`, `algorithm`, and `path_length`.
- Unknown nodes produce a 404 from the standalone Python API. A downstream connection failure is reported by Java as 502.

## Algorithm comparison

- Java: `POST /api/ai/compare-algorithms`
- Python: `POST /compare-algorithms`
- Request: `{"start":"A","goal":"D"}`
- Response contains independent UCS and A* result objects.

## Nearest available station

- Java: `POST /api/ai/nearest-available`
- Python: `POST /nearest-available`
- Request includes `latitude`, `longitude`, and `stations`, an array of `{id, latitude, longitude, available}` objects.
- Response contains `stationId` and `distanceKm`. The caller must provide candidate inventory; the Python sample graph is not connected to production station inventory.

The bundled graph and coordinates are synthetic. Replace and validate the graph before using the results as navigation.
