import requests
import random
from datetime import datetime, timedelta, date
import time

BASE_URL    = "http://localhost:8080"
KEYCLOAK_URL = "http://localhost:8180"
REALM       = "padelcenter-dev"
CLIENT_ID   = "padelcenter-web"

NOMBRES = [
    "Carlos","Maria","Juan","Ana","Pedro","Laura","Miguel","Sara",
    "David","Elena","Javier","Carmen","Antonio","Isabel","Francisco",
    "Marta","Jose","Lucia","Manuel","Pilar","Rafael","Rosa",
    "Alejandro","Cristina","Fernando","Patricia","Alberto","Natalia",
    "Sergio","Silvia","Pablo","Raquel","Andres","Beatriz","Diego",
    "Nuria","Roberto","Monica","Alvaro","Veronica","Marcos","Alicia",
    "Hugo","Sandra","Adrian","Lorena","Daniel","Miriam","Victor","Claudia"
]

APELLIDOS = [
    "Garcia","Martinez","Lopez","Sanchez","Gonzalez","Perez",
    "Rodriguez","Fernandez","Gomez","Martin","Jimenez","Ruiz",
    "Hernandez","Diaz","Moreno","Munoz","Alvarez","Romero",
    "Alonso","Gutierrez","Navarro","Torres","Dominguez","Vazquez",
    "Ramos","Gil","Ramirez","Serrano","Blanco","Suarez"
]

def get_token():
    r = requests.post(
        f"{KEYCLOAK_URL}/realms/{REALM}/protocol/openid-connect/token",
        data={"grant_type":"password","client_id":CLIENT_ID,
              "username":"admin@padelcenter.com","password":"admin123"}
    )
    r.raise_for_status()
    return r.json()["access_token"]

def h(token):
    return {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

def sync_admin(token):
    r = requests.post(f"{BASE_URL}/api/v1/auth/sync", headers=h(token))
    if r.status_code in (200, 201):
        print(f"  Admin OK: {r.json()['email']}")
        return r.json()
    raise RuntimeError(f"Admin sync failed: {r.status_code} {r.text}")

def create_user(token, i):
    email = f"jugador{i}@padelcenter.com"
    r = requests.post(f"{BASE_URL}/api/v1/users", headers=h(token), json={
        "firstName": random.choice(NOMBRES),
        "lastName": f"{random.choice(APELLIDOS)} {random.choice(APELLIDOS)}",
        "email": email,
        "password": f"Password{i}!",
        "phoneNumber": f"+346{random.randint(10000000,99999999)}"
    })
    if r.status_code == 201:
        return r.json()
    if r.status_code == 409:
        return None  # already exists
    print(f"  WARN user {email}: {r.status_code}")
    return None

def create_center(token, nombre, ciudad, i):
    safe = nombre.lower().replace(" ","").replace("á","a").replace("é","e").replace("ó","o").replace("ú","u")
    r = requests.post(f"{BASE_URL}/api/v1/centers", headers=h(token), json={
        "name": nombre,
        "address": f"Calle Mayor {i*10}",
        "city": ciudad,
        "phoneNumber": f"+349{random.randint(10000000,99999999)}",
        "email": f"info@{safe}.com"
    })
    if r.status_code == 201:
        print(f"  Centro: {nombre}")
        return r.json()
    raise RuntimeError(f"Center failed {nombre}: {r.status_code} {r.text[:150]}")

def create_field(token, center_id, nombre, tipo, precio):
    r = requests.post(f"{BASE_URL}/api/v1/centers/{center_id}/fields", headers=h(token), json={
        "name": nombre, "type": tipo, "pricePerHour": precio,
        "isAvailable": random.random() > 0.1
    })
    if r.status_code == 201:
        return r.json()
    raise RuntimeError(f"Field failed {nombre}: {r.status_code} {r.text[:150]}")

def create_booking(token, field_id, user_id, start, end, price):
    r = requests.post(f"{BASE_URL}/api/v1/bookings", headers=h(token), json={
        "fieldId": field_id,
        "startTime": start.strftime("%Y-%m-%dT%H:%M:%SZ"),
        "endTime":   end.strftime("%Y-%m-%dT%H:%M:%SZ"),
        "totalPrice": price
    })
    return r.status_code == 201

def create_tournament(token, center_id, nombre, formato, max_pairs, d_start, d_end):
    r = requests.post(f"{BASE_URL}/api/v1/tournaments", headers=h(token), json={
        "centerId": center_id, "name": nombre,
        "description": f"Torneo de padel {nombre}",
        "format": formato, "maxPairs": max_pairs,
        "startDate": d_start.isoformat(), "endDate": d_end.isoformat()
    })
    if r.status_code == 201:
        print(f"  Torneo: {nombre} [{formato}]")
        return r.json()
    raise RuntimeError(f"Tournament failed {nombre}: {r.status_code} {r.text[:150]}")

def open_reg(token, tid):
    return requests.patch(f"{BASE_URL}/api/v1/tournaments/{tid}/registration/open", headers=h(token)).status_code == 200

def close_reg(token, tid):
    return requests.patch(f"{BASE_URL}/api/v1/tournaments/{tid}/registration/close", headers=h(token)).status_code == 200

def register_pair(token, tid, p1, p2):
    r = requests.post(f"{BASE_URL}/api/v1/tournaments/{tid}/pairs", headers=h(token),
                      json={"player1Id": p1, "player2Id": p2})
    return r.json() if r.status_code == 201 else None

def confirm_pair(token, tid, pair_id):
    return requests.patch(f"{BASE_URL}/api/v1/tournaments/{tid}/pairs/{pair_id}/confirm",
                          headers=h(token)).status_code == 200

def get_matches(token, tid):
    r = requests.get(f"{BASE_URL}/api/v1/tournaments/{tid}/matches",
                     headers=h(token), params={"page":0,"size":100})
    if r.status_code == 200:
        d = r.json()
        return d.get("content", d) if isinstance(d, dict) else d
    return []

def get_pairs(token, tid):
    r = requests.get(f"{BASE_URL}/api/v1/tournaments/{tid}/pairs", headers=h(token))
    if r.status_code == 200:
        d = r.json()
        return d if isinstance(d, list) else d.get("content", [])
    return []

def report_result(token, match_id, winner, sa, sb):
    return requests.post(f"{BASE_URL}/api/v1/matches/{match_id}/result", headers=h(token),
                         json={"winnerPairId": winner, "scoreA": sa, "scoreB": sb}).status_code == 200

# ============================================================
print("\n=== Generando datos de prueba ===\n")
random.seed(42)

token = get_token()

# --- ADMIN ---
print("-- Admin --")
sync_admin(token)

# --- USUARIOS ---
print("\n-- Usuarios (100) --")
users = []
for i in range(1, 101):
    token = get_token() if i % 25 == 0 else token
    u = create_user(token, i)
    if u:
        users.append(u)
    if i % 20 == 0:
        print(f"  {i}/100")
    time.sleep(0.03)
user_ids = [u["userId"] for u in users]
print(f"  OK: {len(users)} usuarios")

# --- CENTROS ---
print("\n-- Centros (5) --")
token = get_token()
centros_data = [
    ("Padel Club Madrid Centro", "Madrid"),
    ("Arena Padel Barcelona",    "Barcelona"),
    ("Padel Valley Valencia",    "Valencia"),
    ("Sur Padel Sevilla",        "Sevilla"),
    ("Padel Premium Malaga",     "Malaga"),
]
centers = [create_center(get_token(), n, c, i+1) for i,(n,c) in enumerate(centros_data)]
print(f"  OK: {len(centers)} centros")

# --- PISTAS ---
print("\n-- Pistas (4-6 por centro) --")
token = get_token()
all_fields = []
for center in centers:
    num = random.randint(4, 6)
    for j in range(1, num+1):
        tipo  = "INDOOR" if j <= num//2 else "OUTDOOR"
        price = random.choice([20.0, 22.5, 25.0, 27.5, 30.0])
        f = create_field(token, center["centerId"], f"Pista {j}", tipo, price)
        all_fields.append({**f, "centerId": center["centerId"], "price": price})
        time.sleep(0.02)
print(f"  OK: {len(all_fields)} pistas")

# --- RESERVAS ---
print("\n-- Reservas (50) --")
token = get_token()
now = datetime.now()
created_b = 0

def try_book(field, uid, start, end):
    global created_b, token
    if create_booking(token, field["fieldId"], uid, start, end, field["price"]):
        created_b += 1

# 20 pasadas
for _ in range(20):
    d = random.randint(1, 30); hr = random.randint(9, 20)
    s = (now - timedelta(days=d)).replace(hour=hr, minute=0, second=0, microsecond=0)
    try_book(random.choice(all_fields), random.choice(user_ids), s, s + timedelta(hours=1))
    time.sleep(0.03)

# 20 futuras
for _ in range(20):
    d = random.randint(1, 30); hr = random.randint(9, 20)
    s = (now + timedelta(days=d)).replace(hour=hr, minute=0, second=0, microsecond=0)
    try_book(random.choice(all_fields), random.choice(user_ids), s, s + timedelta(hours=1))
    time.sleep(0.03)

# 10 de hoy
for hr in random.sample(range(9, 21), 10):
    s = now.replace(hour=hr, minute=0, second=0, microsecond=0)
    try_book(random.choice(all_fields), random.choice(user_ids), s, s + timedelta(hours=1))
    time.sleep(0.03)

print(f"  OK: {created_b} reservas")

# --- TORNEOS ---
print("\n-- Torneos (5) --")
torneos_cfg = [
    ("Liga Otono 2024",    "ROUND_ROBIN",            8, -60, -30, "COMPLETED"),
    ("Copa Invierno 2025", "ELIMINATION",             8, -45, -15, "COMPLETED"),
    ("Torneo Primavera",   "GROUPS_AND_ELIMINATION",  8, -10,  10, "IN_PROGRESS"),
    ("Open Verano 2025",   "ELIMINATION",             8,   5,  25, "IN_PROGRESS"),
    ("Gran Final 2025",    "ROUND_ROBIN",             8,  30,  60, "DRAFT"),
]

torneos_ok = 0
for nombre, fmt, max_p, d0, d1, estado in torneos_cfg:
    token = get_token()
    center = random.choice(centers)
    t = create_tournament(token, center["centerId"], nombre, fmt, max_p,
                          date.today() + timedelta(days=d0),
                          date.today() + timedelta(days=d1))
    tid = t["tournamentId"]
    torneos_ok += 1

    if estado in ("COMPLETED", "IN_PROGRESS"):
        open_reg(token, tid)
        time.sleep(0.3)

        players = random.sample(user_ids, min(16, len(user_ids)))
        pairs = []
        for k in range(0, len(players)-1, 2):
            p = register_pair(token, tid, players[k], players[k+1])
            if p:
                pairs.append(p)
            time.sleep(0.08)

        for pair in pairs:
            confirm_pair(token, tid, pair["pairId"])
            time.sleep(0.08)

        close_reg(token, tid)
        time.sleep(0.8)
        print(f"  Bracket generado: '{nombre}' ({len(pairs)} parejas)")

        if estado == "COMPLETED":
            token = get_token()
            matches = get_matches(token, tid)
            r1 = [m for m in matches if m.get("round") == 1]
            reported = 0
            for m in r1:
                pa, pb = m.get("pairAId"), m.get("pairBId")
                if pa and pb:
                    winner = random.choice([pa, pb])
                    sa = random.randint(0, 1); sb = 2
                    if winner == pb: sa, sb = sb, sa
                    if report_result(token, m["matchId"], winner, sa, sb):
                        reported += 1
                    time.sleep(0.08)
            print(f"    {reported}/{len(r1)} resultados reportados")
    else:
        print(f"  '{nombre}' en borrador")

# === RESUMEN ===
print("\n" + "="*52)
print(" DATOS DE PRUEBA GENERADOS CORRECTAMENTE")
print("="*52)
print(f"  Usuarios  : {len(users)}")
print(f"  Centros   : {len(centers)}")
print(f"  Pistas    : {len(all_fields)}")
print(f"  Reservas  : {created_b}")
print(f"  Torneos   : {torneos_ok}  (2 completados, 2 en curso, 1 borrador)")
print("="*52)
