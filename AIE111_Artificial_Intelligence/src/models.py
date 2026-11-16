from dataclasses import dataclass, field
import math


@dataclass(frozen=True)
class Edge:
    target: str
    cost: float


@dataclass
class Graph:
    adjacency: dict[str, list[Edge]] = field(default_factory=dict)
    coordinates: dict[str, tuple[float, float]] = field(default_factory=dict)

    def add_node(self, node: str, coordinates: tuple[float, float] | None = None) -> None:
        if not node:
            raise ValueError("node id must not be empty")
        self.adjacency.setdefault(node, [])
        if coordinates is not None:
            latitude, longitude = coordinates
            if not (-90 <= latitude <= 90 and -180 <= longitude <= 180):
                raise ValueError("coordinates must be valid latitude/longitude values")
            self.coordinates[node] = coordinates

    def add_edge(self, a: str, b: str, cost: float, bidirectional: bool = True) -> None:
        numeric_cost = float(cost)
        if not math.isfinite(numeric_cost) or numeric_cost < 0:
            raise ValueError("edge cost must be a finite non-negative number")
        self.add_node(a)
        self.add_node(b)
        self.adjacency[a].append(Edge(b, numeric_cost))
        if bidirectional:
            self.adjacency[b].append(Edge(a, numeric_cost))

    def neighbors(self, node: str) -> list[Edge]:
        return self.adjacency.get(node, [])
