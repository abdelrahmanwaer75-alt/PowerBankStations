import pytest
from fastapi import HTTPException
from src.api.main import (
    NearestAvailableRequest,
    RouteRequest,
    StationCandidate,
    compare_algorithms,
    find_route,
    nearest_available,
)


def test_find_route_endpoint_returns_path_metrics():
    result = find_route(RouteRequest(start="A", goal="D", algorithm="astar"))
    assert result["path"][0] == "A"
    assert result["path"][-1] == "D"
    assert result["algorithm"] == "astar"
    assert result["cost"] is not None


def test_compare_endpoint_reports_both_algorithms():
    results = compare_algorithms(RouteRequest(start="A", goal="D"))
    assert set(results) == {"ucs", "astar"}
    assert results["ucs"]["cost"] == results["astar"]["cost"]


def test_route_endpoint_rejects_unknown_nodes():
    with pytest.raises(HTTPException) as error:
        find_route(RouteRequest(start="missing", goal="D"))
    assert error.value.status_code == 404


def test_nearest_available_skips_unavailable_stations():
    request = NearestAvailableRequest(
        latitude=30.0,
        longitude=29.0,
        stations=[
            StationCandidate(id="closed", latitude=30.001, longitude=29.001, available=False),
            StationCandidate(id="open", latitude=30.01, longitude=29.01, available=True),
        ],
    )
    assert nearest_available(request)["stationId"] == "open"


def test_nearest_available_reports_no_candidates():
    with pytest.raises(HTTPException) as error:
        nearest_available(NearestAvailableRequest(latitude=0, longitude=0))
    assert error.value.status_code == 404
