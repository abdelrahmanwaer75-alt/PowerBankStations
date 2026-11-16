import heapq
import math
import time
from .models import Graph


def reconstruct(came_from: dict[str, str | None], goal: str) -> list[str]:
    path: list[str] = []
    current: str | None = goal
    while current is not None:
        path.append(current)
        current = came_from.get(current)
    return list(reversed(path))


def _validate_endpoints(graph: Graph, start: str, goal: str) -> None:
    if start not in graph.adjacency or goal not in graph.adjacency:
        raise ValueError("start and goal must be nodes in the graph")


def _result(
    path: list[str], cost: float | None, explored: int, started_at: float
) -> dict[str, object]:
    result: dict[str, object] = {
        "path": path,
        "cost": cost,
        "explored": explored,
        "runtime_ms": (time.perf_counter() - started_at) * 1000,
    }
    if not path:
        result["error"] = "No route found"
    return result


def ucs(graph: Graph, start: str, goal: str) -> dict[str, object]:
    _validate_endpoints(graph, start, goal)
    started_at = time.perf_counter()
    frontier: list[tuple[float, str]] = [(0.0, start)]
    came_from: dict[str, str | None] = {start: None}
    costs = {start: 0.0}
    explored = 0

    while frontier:
        cost, node = heapq.heappop(frontier)
        if cost != costs.get(node):
            continue
        explored += 1
        if node == goal:
            return _result(reconstruct(came_from, goal), cost, explored, started_at)
        for edge in graph.neighbors(node):
            next_cost = cost + edge.cost
            if next_cost < costs.get(edge.target, math.inf):
                costs[edge.target] = next_cost
                came_from[edge.target] = node
                heapq.heappush(frontier, (next_cost, edge.target))

    return _result([], None, explored, started_at)


def _distance_km(graph: Graph, first: str, second: str) -> float | None:
    if first not in graph.coordinates or second not in graph.coordinates:
        return None
    latitude1, longitude1 = map(math.radians, graph.coordinates[first])
    latitude2, longitude2 = map(math.radians, graph.coordinates[second])
    delta_latitude = latitude2 - latitude1
    delta_longitude = longitude2 - longitude1
    haversine = (
        math.sin(delta_latitude / 2) ** 2
        + math.cos(latitude1)
        * math.cos(latitude2)
        * math.sin(delta_longitude / 2) ** 2
    )
    return 6371 * 2 * math.asin(math.sqrt(min(1.0, haversine)))


def _heuristic_scale(graph: Graph) -> float:
    ratios: list[float] = []
    for source, edges in graph.adjacency.items():
        for edge in edges:
            distance = _distance_km(graph, source, edge.target)
            if distance is not None and distance > 0:
                ratios.append(edge.cost / distance)
            elif edge.cost == 0:
                ratios.append(0.0)
    return min(ratios, default=0.0)


def astar(graph: Graph, start: str, goal: str) -> dict[str, object]:
    _validate_endpoints(graph, start, goal)
    started_at = time.perf_counter()
    scale = _heuristic_scale(graph)

    def heuristic(node: str) -> float:
        distance = _distance_km(graph, node, goal)
        return scale * distance if distance is not None else 0.0

    frontier: list[tuple[float, float, str]] = [(heuristic(start), 0.0, start)]
    came_from: dict[str, str | None] = {start: None}
    costs = {start: 0.0}
    explored = 0

    while frontier:
        _, cost, node = heapq.heappop(frontier)
        if cost != costs.get(node):
            continue
        explored += 1
        if node == goal:
            return _result(reconstruct(came_from, goal), cost, explored, started_at)
        for edge in graph.neighbors(node):
            next_cost = cost + edge.cost
            if next_cost < costs.get(edge.target, math.inf):
                costs[edge.target] = next_cost
                came_from[edge.target] = node
                heapq.heappush(
                    frontier, (next_cost + heuristic(edge.target), next_cost, edge.target)
                )

    return _result([], None, explored, started_at)
