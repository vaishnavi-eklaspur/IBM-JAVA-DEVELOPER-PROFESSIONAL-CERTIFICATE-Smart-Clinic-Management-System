# Smart Clinic Management System – Database Design

## Overview
This document describes the relational database design for the Smart Clinic Management System developed as part of the IBM Java Developer Professional Certificate.

The database is designed using MySQL and supports doctors, patients, appointments, and prescriptions.

---

## Doctor Table
Stores doctor details.

| Column | Type | Description |
|------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(100) | Doctor name |
| email | VARCHAR(100) | Unique email |
| speciality | VARCHAR(50) | Medical speciality |

---

## Patient Table
Stores patient information.

| Column | Type | Description |
|------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(100) | Patient name |
| email | VARCHAR(100) | Unique email |
| phone | VARCHAR(15) | Contact number |

---

## Appointment Table
Stores appointment details.

| Column | Type |
|------|------|
| id | BIGINT |
| appointment_time | DATETIME |
| doctor_id | BIGINT (FK) |
| patient_id | BIGINT (FK) |

---

## Prescription Table
Stores prescriptions linked to appointments.

| Column | Type |
|------|------|
| id | BIGINT |
| notes | TEXT |
| appointment_id | BIGINT (FK) |
