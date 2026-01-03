-- Flyway migration: enforce persistent, role-exclusive email identities

CREATE TABLE user_identity (
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_identity PRIMARY KEY (email, role),
    CONSTRAINT uq_user_identity_email UNIQUE (email),
    CONSTRAINT chk_user_identity_role CHECK (role IN ('DOCTOR', 'PATIENT'))
);

ALTER TABLE doctor ADD COLUMN role VARCHAR(20) DEFAULT 'DOCTOR' NOT NULL;
ALTER TABLE patient ADD COLUMN role VARCHAR(20) DEFAULT 'PATIENT' NOT NULL;

ALTER TABLE doctor ADD CONSTRAINT chk_doctor_role CHECK (role = 'DOCTOR');
ALTER TABLE patient ADD CONSTRAINT chk_patient_role CHECK (role = 'PATIENT');

INSERT INTO user_identity (email, role)
SELECT email, 'DOCTOR' FROM doctor;

INSERT INTO user_identity (email, role)
SELECT p.email, 'PATIENT'
FROM patient p
WHERE NOT EXISTS (
    SELECT 1 FROM user_identity ui WHERE ui.email = p.email
);

ALTER TABLE doctor ADD CONSTRAINT fk_doctor_identity FOREIGN KEY (email, role)
    REFERENCES user_identity (email, role);
ALTER TABLE patient ADD CONSTRAINT fk_patient_identity FOREIGN KEY (email, role)
    REFERENCES user_identity (email, role);
