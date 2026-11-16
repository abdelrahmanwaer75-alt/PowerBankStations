const cfg = window.APP_CONFIG || {
    API_BASE: "http://localhost:8080/api",
    AI_BASE: "http://localhost:8001",
    DIP_BASE: "http://localhost:8002"
};
const page = document.body.dataset.page || "portal";
const title = document.body.dataset.title || page;
const admin = location.pathname.includes("/admin/");
const content = () => document.querySelector("#content");
const result = () => document.querySelector("#result");

async function request(base, path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (!(options.body instanceof FormData) && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }
    const response = await fetch(`${base}${path}`, { ...options, headers });
    const body = await response.text();
    let data = body;
    if (body) {
        try {
            data = JSON.parse(body);
        } catch {
            data = body;
        }
    }
    if (!response.ok) {
        throw new Error(
            (data && (data.detail || data.error || data.message)) ||
            (typeof data === "string" && data) ||
            response.statusText
        );
    }
    return data;
}

const api = (path, options) => request(cfg.API_BASE, path, options);

function showResult(value) {
    result().textContent = typeof value === "string" ? value : JSON.stringify(value, null, 2);
}

function shell(pageTitle) {
    document.querySelector("#app").innerHTML = `
        <header>
            <a class="brand" href="../index.html">⚡ PowerBank Stations</a>
            <nav>
                <a href="../index.html">Home</a>
                <a href="${admin ? "dashboard.html" : "home.html"}">Dashboard</a>
            </nav>
        </header>
        <main>
            <p class="muted">${admin ? "ADMINISTRATION" : "CUSTOMER PORTAL"}</p>
            <h1>${pageTitle}</h1>
            <section id="content" class="card"><p>Loading project data…</p></section>
            <p class="muted">Simulation environment · No physical kiosk or real payment processing</p>
        </main>`;
}

function renderForm(fields, buttonText) {
    content().innerHTML = `
        <form id="action-form">
            ${fields.map(field => `
                <label for="${field.name}">${field.label}</label>
                <input id="${field.name}" name="${field.name}" type="${field.type || "text"}"
                    ${field.required === false ? "" : "required"}
                    ${field.placeholder ? `placeholder="${field.placeholder}"` : ""}
                    ${field.min !== undefined ? `min="${field.min}"` : ""}
                    ${field.step ? `step="${field.step}"` : ""}>
            `).join("")}
            <button type="submit">${buttonText}</button>
        </form>
        <pre id="result" aria-live="polite"></pre>`;
}

function renderData(data) {
    content().innerHTML = `<pre id="result" aria-live="polite"></pre>`;
    showResult(data);
}

function currentUser() {
    try {
        return JSON.parse(localStorage.getItem("user") || "null");
    } catch {
        return null;
    }
}

function connectForm(handler) {
    const form = document.querySelector("#action-form");
    form.addEventListener("submit", async event => {
        event.preventDefault();
        const button = form.querySelector("button");
        button.disabled = true;
        try {
            await handler(Object.fromEntries(new FormData(form)));
        } catch (error) {
            showResult(error.message);
        } finally {
            button.disabled = false;
        }
    });
}

async function loadCollection(path) {
    renderData(await api(path));
}

async function load() {
    shell(title);

    if (title === "Login" || title === "Admin Login") {
        renderForm([
            { name: "email", label: "Email", type: "email" },
            { name: "password", label: "Password", type: "password" }
        ], "Sign in");
        connectForm(async values => {
            const user = await api("/auth/login", { method: "POST", body: JSON.stringify(values) });
            localStorage.setItem("user", JSON.stringify(user));
            location.href = admin ? "dashboard.html" : "home.html";
        });
        return;
    }

    if (title === "Register") {
        renderForm([
            { name: "name", label: "Full name" },
            { name: "email", label: "Email", type: "email" },
            { name: "phone", label: "Phone", required: false },
            { name: "password", label: "Password (at least 8 characters)", type: "password" }
        ], "Create account");
        connectForm(async values => {
            const user = await api("/auth/register", {
                method: "POST",
                body: JSON.stringify(values)
            });
            showResult({ id: user.id, name: user.name, email: user.email, role: user.role });
        });
        return;
    }

    if (title === "Reservation") {
        renderForm([{ name: "stationId", label: "Station ID", type: "number", min: 1 }], "Reserve a PowerBank");
        connectForm(async values => {
            const user = currentUser();
            if (!user) {
                throw new Error("Please sign in before making a reservation.");
            }
            showResult(await api("/reservations", {
                method: "POST",
                body: JSON.stringify({ stationId: Number(values.stationId), userId: user.id })
            }));
        });
        return;
    }

    if (title === "Start Rental") {
        renderForm([
            { name: "reservationId", label: "Reservation ID", type: "number", min: 1 },
            { name: "pickupCode", label: "Pickup code" }
        ], "Start rental");
        connectForm(async values => showResult(await api("/rentals/start", {
            method: "POST",
            body: JSON.stringify({ ...values, reservationId: Number(values.reservationId) })
        })));
        return;
    }

    if (title === "Return PowerBank") {
        renderForm([
            { name: "rentalId", label: "Rental ID", type: "number", min: 1 },
            { name: "stationId", label: "Return station ID", type: "number", min: 1 }
        ], "Return PowerBank");
        connectForm(async values => showResult(await api("/returns", {
            method: "POST",
            body: JSON.stringify({ rentalId: Number(values.rentalId), stationId: Number(values.stationId) })
        })));
        return;
    }

    if (title === "Route Planning" || title === "AI Route Analysis") {
        renderForm([
            { name: "start", label: "Start graph node", placeholder: "A" },
            { name: "goal", label: "Destination graph node", placeholder: "D" },
            { name: "algorithm", label: "Algorithm (ucs or astar)", placeholder: "astar" }
        ], "Find route");
        connectForm(async values => showResult(await api("/ai/find-route", {
            method: "POST",
            body: JSON.stringify(values)
        })));
        return;
    }

    if (title.includes("Photo Upload") || title.includes("Inspection")
        || title.includes("Infrastructure") || title.includes("Circuit Board")) {
        content().innerHTML = `
            <form id="upload-form">
                <label for="inspection-image">Inspection image</label>
                <input id="inspection-image" type="file" name="file" accept="image/jpeg,image/png,image/webp" required>
                <label for="inspection-kind">Inspection type</label>
                <select id="inspection-kind" name="kind">
                    <option value="powerbank">PowerBank exterior</option>
                    <option value="infrastructure">Concrete infrastructure</option>
                    <option value="pcb">PCB visual inspection</option>
                </select>
                <button type="submit">Analyze image</button>
            </form>
            <pre id="result" aria-live="polite"></pre>`;
        document.querySelector("#upload-form").addEventListener("submit", async event => {
            event.preventDefault();
            const form = event.currentTarget;
            const button = form.querySelector("button");
            button.disabled = true;
            try {
                const kind = new FormData(form).get("kind");
                const file = form.querySelector('input[type="file"]').files[0];
                const data = new FormData();
                data.append("file", file);
                showResult(await api(`/inspections/${kind}`, { method: "POST", body: data }));
            } catch (error) {
                showResult(error.message);
            } finally {
                button.disabled = false;
            }
        });
        return;
    }

    if (title === "Stations Map") {
        renderForm([
            { name: "lat", label: "Latitude", type: "number", min: -90, step: "any" },
            { name: "lon", label: "Longitude", type: "number", min: -180, step: "any" }
        ], "Find nearby stations");
        connectForm(async values => {
            const latitude = Number(values.lat);
            const longitude = Number(values.lon);
            if (latitude > 90 || longitude > 180) {
                throw new Error("Latitude must be at most 90 and longitude at most 180.");
            }
            showResult(await api(`/stations/nearby?lat=${latitude}&lon=${longitude}`));
        });
        return;
    }

    if (title === "Stations Management") {
        renderForm([
            { name: "name", label: "Station name" },
            { name: "address", label: "Address" },
            { name: "latitude", label: "Latitude", type: "number", min: -90, step: "any" },
            { name: "longitude", label: "Longitude", type: "number", min: -180, step: "any" },
            { name: "capacity", label: "Capacity", type: "number", min: 1 }
        ], "Create station");
        connectForm(async values => showResult(await api("/stations", {
            method: "POST",
            body: JSON.stringify({
                ...values,
                latitude: Number(values.latitude),
                longitude: Number(values.longitude),
                capacity: Number(values.capacity)
            })
        })));
        return;
    }

    const collectionByTitle = {
        "Customer Home": "/stations",
        "Stations List": "/stations",
        "Station Details": "/stations",
        "Admin Dashboard": "/admin/summary",
        "Analytics & Reports": "/admin/summary",
        "Station Details Management": "/stations",
        "PowerBank Management": "/powerbanks",
        "Inventory Management": "/inventory",
        "Reservations Management": "/reservations",
        "Rentals Management": "/rentals",
        "Returns Management": "/returns",
        "Customers Management": "/users",
        "Maintenance Management": "/maintenance",
        "Rental History": "/rentals",
        "Active Rental": "/rentals/active"
    };
    if (collectionByTitle[title]) {
        try {
            await loadCollection(collectionByTitle[title]);
        } catch (error) {
            renderData(`${error.message}. Check that the backend is running and sample data is loaded.`);
        }
        return;
    }

    renderData(`${title} is reserved for its documented workflow. This prototype does not yet expose a backend operation for this page.`);
}

if (document.querySelector("#app")) {
    load().catch(error => {
        shell(title);
        renderData(`Could not load this page: ${error.message}`);
    });
}
