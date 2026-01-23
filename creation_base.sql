CREATE SCHEMA "public";
CREATE TABLE "agent" (
                     "id" serial PRIMARY KEY,
                     "matricule" text NOT NULL CONSTRAINT "agent_matricule_key" UNIQUE,
                     "first_name" text NOT NULL,
                     "last_name" text NOT NULL,
                     "service_id" integer NOT NULL
);
CREATE TABLE "assignment" (
                      "id" serial PRIMARY KEY,
                      "vehicle_id" integer NOT NULL,
                      "agent_id" integer NOT NULL,
                      "start_date" date,
                      "end_date" date,
                      "comment" text,
                      "created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                      "status" varchar(20) DEFAULT 'DEMANDEE' NOT NULL,
                      CONSTRAINT "assignment_check" CHECK (CHECK (((end_date IS NULL) OR (end_date >= start_date))))
);
CREATE TABLE "etat" (
                        "id" integer GENERATED ALWAYS AS IDENTITY (sequence name "Etat_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START WITH 1 CACHE 1),
                        "name" varchar(30) NOT NULL CONSTRAINT "etat_name_key" UNIQUE,
                        CONSTRAINT "Etat_pkey" PRIMARY KEY("id")
);
CREATE TABLE "service" (
                           "id" serial PRIMARY KEY,
                           "name" text NOT NULL
);
CREATE TABLE "vehicle" (
                           "id" serial PRIMARY KEY,
                           "plate" text NOT NULL CONSTRAINT "vehicle_plate_key" UNIQUE,
                           "type" text NOT NULL,
                           "brand" text NOT NULL,
                           "model" text NOT NULL,
                           "mileage" integer NOT NULL,
                           "acquisition_date" date NOT NULL,
                           "status" text NOT NULL,
                           "etat" integer DEFAULT 3,
                           CONSTRAINT "vehicle_mileage_check" CHECK (CHECK ((mileage >= 0))),
	CONSTRAINT "vehicle_status_check" CHECK (CHECK ((status = ANY (ARRAY['DISPONIBLE'::text, 'AFFECTE'::text, 'ENTRETIEN'::text])))),
	CONSTRAINT "vehicle_type_check" CHECK (CHECK ((type = ANY (ARRAY['4x4'::text, 'SUV & Crossover'::text, 'Citadine'::text, 'Berline'::text, 'Break'::text, 'Cabriolet'::text, 'Coupé'::text, 'Monospace'::text, 'Bus et minibus'::text, 'Fourgonnette'::text, 'Fourgon (< 3,5 t)'::text, 'Pick-up'::text, 'Voiture société / commerciale'::text, 'Sans permis'::text, 'Camion (> 3,5 t)'::text]))))
);
CREATE TABLE "vehicle_type" (
                                "label" text PRIMARY KEY
);
ALTER TABLE "agent" ADD CONSTRAINT "agent_service_id_fkey" FOREIGN KEY ("service_id") REFERENCES "service"("id");
ALTER TABLE "assignment" ADD CONSTRAINT "assignment_agent_id_fkey" FOREIGN KEY ("agent_id") REFERENCES "agent"("id");
ALTER TABLE "assignment" ADD CONSTRAINT "assignment_vehicle_id_fkey" FOREIGN KEY ("vehicle_id") REFERENCES "vehicle"("id");
ALTER TABLE "vehicle" ADD CONSTRAINT "constraint_etat" FOREIGN KEY ("etat") REFERENCES "etat"("id");
ALTER TABLE "vehicle" ADD CONSTRAINT "fk_vehicle_type" FOREIGN KEY ("type") REFERENCES "vehicle_type"("label");
CREATE UNIQUE INDEX "agent_matricule_key" ON "agent" ("matricule");
CREATE UNIQUE INDEX "agent_pkey" ON "agent" ("id");
CREATE UNIQUE INDEX "assignment_pkey" ON "assignment" ("id");
CREATE UNIQUE INDEX "etat_name_key" ON "etat" ("name");
CREATE UNIQUE INDEX "Etat_pkey" ON "etat" ("id");
CREATE UNIQUE INDEX "service_pkey" ON "service" ("id");
CREATE UNIQUE INDEX "vehicle_pkey" ON "vehicle" ("id");
CREATE UNIQUE INDEX "vehicle_plate_key" ON "vehicle" ("plate");
CREATE UNIQUE INDEX "vehicle_type_pkey" ON "vehicle_type" ("label");
