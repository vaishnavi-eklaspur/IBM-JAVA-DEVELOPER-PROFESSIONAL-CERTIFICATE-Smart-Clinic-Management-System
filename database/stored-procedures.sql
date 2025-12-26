DELIMITER $$

CREATE PROCEDURE GetDailyAppointmentReportByDoctor()
BEGIN
  SELECT d.name AS doctor_name, COUNT(a.id) AS total_appointments
  FROM Doctor d
  JOIN Appointment a ON d.id = a.doctor_id
  GROUP BY d.name;
END$$

CREATE PROCEDURE GetDoctorWithMostPatientsByMonth()
BEGIN
  SELECT doctor_id, COUNT(*) AS total_patients
  FROM Appointment
  GROUP BY doctor_id;
END$$

CREATE PROCEDURE GetDoctorWithMostPatientsByYear()
BEGIN
  SELECT doctor_id, COUNT(*) AS total_patients
  FROM Appointment
  GROUP BY doctor_id;
END$$

DELIMITER ;
