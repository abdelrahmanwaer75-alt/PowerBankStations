# AIE111 – Artificial Intelligence

This module implements classical graph search for the project’s route-planning prototype. Its graph is synthetic and for teaching only; it is not a real street network.

## Structure

- `src/models.py` – validated weighted graph and edge models.
- `src/search.py` – Uniform Cost Search and A* with stale-entry handling and timing/expanded-state metrics.
- `src/api/main.py` – FastAPI routes and a small sample graph.
- `datasets/road_network.json` – illustrative graph data; no real road dataset is bundled.
- `tests/test_search.py` – route optimality, invalid input, repeated states, and no-route tests.

## Run and test

```powershell
python -m pip install -r requirements.txt
python -m pytest -q
python -m uvicorn src.api.main:app --reload --port 8001
```

Standalone endpoints: `GET /health`, `POST /find-route`, `POST /nearest-available`, and `POST /compare-algorithms`. Compatibility aliases `/route` and `/compare` remain available. The Spring API proxies these under `/api/ai/`.

To use real navigation data, obtain a suitably licensed graph, record provenance and units, validate node coordinates and non-negative edge costs, and update the graph loader and heuristic assumptions. Do not treat the sample graph’s node IDs or distances as actual directions.
