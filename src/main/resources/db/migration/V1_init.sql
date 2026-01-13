PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS service (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS agent (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  service_id INTEGER NOT NULL,
  FOREIGN KEY(service_id) REFERENCES service(id)
);

CREATE TABLE IF NOT EXISTS vehicle (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  registration TEXT NOT NULL UNIQUE,
  brand TEXT NOT NULL,
  model TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('DISPONIBLE', 'AFFECTE', 'ENTRETIEN'))
);

CREATE TABLE IF NOT EXISTS assignment (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  vehicle_id INTEGER NOT NULL,
  agent_id INTEGER NOT NULL,
  start_date TEXT NOT NULL, -- ISO yyyy-MM-dd
  end_date TEXT,            -- ISO yyyy-MM-dd, null = affectation en cours
  FOREIGN KEY(vehicle_id) REFERENCES vehicle(id),
  FOREIGN KEY(agent_id) REFERENCES agent(id)
);

CREATE INDEX IF NOT EXISTS idx_assignment_vehicle ON assignment(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_assignment_agent ON assignment(agent_id);
CREATE INDEX IF NOT EXISTS idx_assignment_dates ON assignment(start_date, end_date);
