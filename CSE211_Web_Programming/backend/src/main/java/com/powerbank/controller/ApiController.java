package com.powerbank.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import com.powerbank.entity.MaintenanceRequest;
import com.powerbank.entity.Rental;
import com.powerbank.entity.Reservation;
import com.powerbank.entity.ReturnRecord;
import com.powerbank.entity.Station;
import com.powerbank.entity.User;
import com.powerbank.dto.NearestAvailableRequest;
import com.powerbank.dto.RentalStartRequest;
import com.powerbank.dto.ReservationRequest;
import com.powerbank.dto.ReturnRequest;
import com.powerbank.dto.RouteRequest;
import com.powerbank.integration.AIIntegrationService;
import com.powerbank.integration.ImageProcessingIntegrationService;
import com.powerbank.repository.MaintenanceRepository;
import com.powerbank.repository.PowerBankRepository;
import com.powerbank.repository.RentalRepository;
import com.powerbank.repository.ReservationRepository;
import com.powerbank.repository.ReturnRepository;
import com.powerbank.repository.StationRepository;
import com.powerbank.repository.UserRepository;
import com.powerbank.service.AuthService;
import com.powerbank.service.RentalService;
import com.powerbank.service.ReservationService;
import com.powerbank.service.StationService;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserRepository users;
    private final StationRepository stations;
    private final PowerBankRepository banks;
    private final ReservationRepository reservations;
    private final RentalRepository rentals;
    private final ReturnRepository returns;
    private final MaintenanceRepository maintenance;
    private final StationService stationService;
    private final AuthService authService;
    private final ReservationService reservationService;
    private final RentalService rentalService;
    private final AIIntegrationService aiIntegrationService;
    private final ImageProcessingIntegrationService imageProcessingIntegrationService;

    public ApiController(
            UserRepository users,
            StationRepository stations,
            PowerBankRepository banks,
            ReservationRepository reservations,
            RentalRepository rentals,
            ReturnRepository returns,
            MaintenanceRepository maintenance,
            StationService stationService,
            AuthService authService,
            ReservationService reservationService,
            RentalService rentalService,
            AIIntegrationService aiIntegrationService,
            ImageProcessingIntegrationService imageProcessingIntegrationService) {
        this.users = users;
        this.stations = stations;
        this.banks = banks;
        this.reservations = reservations;
        this.rentals = rentals;
        this.returns = returns;
        this.maintenance = maintenance;
        this.stationService = stationService;
        this.authService = authService;
        this.reservationService = reservationService;
        this.rentalService = rentalService;
        this.aiIntegrationService = aiIntegrationService;
        this.imageProcessingIntegrationService = imageProcessingIntegrationService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "PowerBank backend");
    }

    @PostMapping("/ai/find-route")
    public Map<String, Object> findRoute(@Valid @RequestBody RouteRequest request) {
        return aiIntegrationService.findRoute(Map.of(
                "start", request.start(),
                "goal", request.goal(),
                "algorithm", request.algorithm() == null ? "astar" : request.algorithm()));
    }

    @PostMapping("/ai/nearest-available")
    public Map<String, Object> nearestAvailable(@Valid @RequestBody NearestAvailableRequest request) {
        return aiIntegrationService.nearestAvailable(Map.of(
                "latitude", request.latitude(),
                "longitude", request.longitude(),
                "stations", request.stations().stream()
                        .map(station -> Map.of(
                                "id", station.id(),
                                "latitude", station.latitude(),
                                "longitude", station.longitude(),
                                "available", station.available()))
                        .toList()));
    }

    @PostMapping("/ai/compare-algorithms")
    public Map<String, Object> compareAlgorithms(@Valid @RequestBody RouteRequest request) {
        return aiIntegrationService.compareAlgorithms(Map.of(
                "start", request.start(),
                "goal", request.goal()));
    }

    @PostMapping("/inspections/{kind}")
    public Map<String, Object> inspect(
            @PathVariable String kind,
            @RequestPart("file") MultipartFile file) throws java.io.IOException {
        return imageProcessingIntegrationService.inspect(kind, file);
    }

    @PostMapping("/auth/register")
    public User register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        return authService.authenticate(credentials.get("email"), credentials.get("password"))
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(
                        Map.of("id", user.id, "name", user.name, "email", user.email, "role", user.role)))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }

    @GetMapping("/stations")
    public List<Station> stations() {
        return stationService.all();
    }

    @GetMapping("/stations/nearby")
    public List<Map<String, Object>> nearby(@RequestParam double lat, @RequestParam double lon) {
        return stationService.nearby(lat, lon);
    }

    @PostMapping("/stations")
    public Station saveStation(@RequestBody Station station) {
        return stationService.save(station);
    }

    @GetMapping("/powerbanks")
    public List<?> powerbanks() {
        return banks.findAll().stream()
                .map(bank -> Map.of(
                        "id", bank.id,
                        "serialNumber", bank.serialNumber == null ? "" : bank.serialNumber,
                        "status", bank.status == null ? "" : bank.status,
                        "batteryLevel", bank.batteryLevel == null ? 0 : bank.batteryLevel,
                        "stationId", bank.station == null ? 0L : bank.station.id))
                .toList();
    }

    @GetMapping("/inventory")
    public Map<String, Long> inventory() {
        List<com.powerbank.entity.PowerBank> powerBanks = banks.findAll();
        return Map.of(
                "available", powerBanks.stream().filter(bank -> "AVAILABLE".equals(bank.status)).count(),
                "reserved", powerBanks.stream().filter(bank -> "RESERVED".equals(bank.status)).count(),
                "rented", powerBanks.stream().filter(bank -> "RENTED".equals(bank.status)).count());
    }

    @PostMapping("/reservations")
    public Reservation reserve(@Valid @RequestBody ReservationRequest request) {
        return reservationService.create(request.userId(), request.stationId());
    }

    @GetMapping("/reservations")
    public List<Reservation> reservations() {
        return reservationService.all();
    }

    @PostMapping("/rentals/start")
    public Rental start(@Valid @RequestBody RentalStartRequest request) {
        return rentalService.start(request.reservationId(), request.pickupCode());
    }

    @GetMapping("/rentals")
    public List<Rental> rentals() {
        return rentalService.all();
    }

    @GetMapping("/rentals/active")
    public List<Rental> activeRentals() {
        return rentals.findAll().stream().filter(rental -> "ACTIVE".equals(rental.status)).toList();
    }

    @PostMapping("/returns")
    @Transactional
    public ReturnRecord doReturn(@Valid @RequestBody ReturnRequest request) {
        Station station = stations.findByIdForUpdate(request.stationId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Station not found"));
        Rental rental = rentals.findByIdForUpdate(request.rentalId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Rental not found"));
        if (!"ACTIVE".equals(rental.status) || rental.endTime != null) {
            throw new IllegalStateException("Rental is not active");
        }
        long deviceCount = banks.findAll().stream()
                .filter(bank -> bank.station != null && bank.station.id.equals(station.id))
                .count();
        if (deviceCount >= station.capacity) {
            throw new IllegalStateException("Station is full");
        }

        rental.endTime = Instant.now();
        rental.status = "COMPLETED";
        rental.powerBank.station = station;
        rental.powerBank.status = "AVAILABLE";
        banks.save(rental.powerBank);
        rentals.save(rental);

        ReturnRecord record = new ReturnRecord();
        record.rental = rental;
        record.returnStation = station;
        return returns.save(record);
    }

    @GetMapping("/returns")
    public List<ReturnRecord> returnList() {
        return returns.findAll();
    }

    @PostMapping("/maintenance")
    public MaintenanceRequest createMaintenanceRequest(@RequestBody MaintenanceRequest request) {
        return maintenance.save(request);
    }

    @GetMapping("/maintenance")
    public List<MaintenanceRequest> maintenanceList() {
        return maintenance.findAll();
    }

    @GetMapping("/admin/summary")
    public Map<String, Long> summary() {
        return Map.of(
                "stations", stations.count(),
                "powerbanks", banks.count(),
                "reservations", reservations.count(),
                "rentals", rentals.count(),
                "returns", returns.count(),
                "maintenance", maintenance.count());
    }

    @GetMapping("/users")
    public List<Map<String, Object>> userList() {
        return users.findAll().stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.id, "name", user.name, "email", user.email, "role", user.role))
                .toList();
    }
}
