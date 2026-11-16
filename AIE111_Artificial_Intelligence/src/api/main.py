import math
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from ..models import Graph
from ..search import astar, ucs

app = FastAPI(title="PowerBank AI Route Service", version="1.1.0")


def sample_graph() -> Graph:
    graph = Graph(
        coordinates={
            "A": (30.848, 28.955),
            "B": (30.850, 28.958),
            "C": (30.846, 28.960),
            "D": (30.852, 28.962),
        }
    )
    for first, second, cost in [
        ("A", "B", 0.6),
        ("A", "C", 0.8),
        ("B", "D", 0.7),
        ("C", "D", 0.9),
        ("B", "C", 0.5),
    ]:
        graph.add_edge(first, second, cost)
    return graph


class RouteRequest(BaseModel):
    start: str = Field(min_length=1, max_length=100)
    goal: str = Field(min_length=1, max_length=100)
    algorithm: str = Field(default="astar", min_length=2, max_length=10)


class StationCandidate(BaseModel):
    id: str = Field(min_length=1, max_length=100)
    latitude: float = Field(ge=-90, le=90)
    longitude: float = Field(ge=-180, le=180)
    available: bool = True


class NearestAvailableRequest(BaseModel):
    latitude: float = Field(ge=-90, le=90)
    longitude: float = Field(ge=-180, le=180)
    stations: list[StationCandidate] = Field(default_factory=list, max_length=500)


def _route_result(request: RouteRequest, algorithm: str) -> dict[str, object]:
    graph = sample_graph()
    if request.start not in graph.adjacency or request.goal not in graph.adjacency:
        raise HTTPException(status_code=404, detail="Unknown graph node")
    result = (ucs if algorithm == "ucs" else astar)(graph, request.start, request.goal)
    result["algorithm"] = algorithm
    result["path_length"] = max(0, len(result["path"]) - 1)
    return result


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.get("/nodes")
def nodes() -> dict[str, list[str]]:
    return {"nodes": list(sample_graph().adjacency)}


@app.post("/find-route")
@app.post("/route", include_in_schema=False)
def find_route(request: RouteRequest) -> dict[str, object]:
    algorithm = request.algorithm.strip().lower()
    if algorithm == "a*":
        algorithm = "astar"
    if algorithm not in {"ucs", "astar"}:
        raise HTTPException(status_code=400, detail="algorithm must be ucs or astar")
    return _route_result(request, algorithm)


@app.post("/compare-algorithms")
@app.post("/compare", include_in_schema=False)
def compare_algorithms(request: RouteRequest) -> dict[str, object]:
    graph = sample_graph()
    if request.start not in graph.adjacency or request.goal not in graph.adjacency:
        raise HTTPException(status_code=404, detail="Unknown graph node")
    results: dict[str, object] = {}
    for name, algorithm in (("ucs", ucs), ("astar", astar)):
        result = algorithm(graph, request.start, request.goal)
        result["algorithm"] = name
        result["path_length"] = max(0, len(result["path"]) - 1)
        results[name] = result
    return results


@app.post("/nearest-available")
def nearest_available(request: NearestAvailableRequest) -> dict[str, object]:
    candidates = []
    for station in request.stations:
        if not station.available:
            continue
        lat1, lat2 = math.radians(request.latitude), math.radians(station.latitude)
        delta_lat = math.radians(station.latitude - request.latitude)
        delta_lon = math.radians(station.longitude - request.longitude)
        value = (
            math.sin(delta_lat / 2) ** 2
            + math.cos(lat1) * math.cos(lat2) * math.sin(delta_lon / 2) ** 2
        )
        distance_km = 6371 * 2 * math.asin(math.sqrt(min(1.0, value)))
        candidates.append({"stationId": station.id, "distanceKm": distance_km})
    if not candidates:
        raise HTTPException(status_code=404, detail="No available stations were supplied")
    return min(candidates, key=lambda candidate: candidate["distanceKm"])
