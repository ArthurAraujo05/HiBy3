CREATE TABLE
    employee (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        hourly_rate DECIMAL(10, 2) NOT NULL
    );

CREATE TABLE
    punch (
        id INT AUTO_INCREMENT PRIMARY KEY,
        employee_id INT,
        timestamp DATETIME NOT NULL,
        event_type VARCHAR(20) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'ORIGINAL',
        requested_timestamp DATETIME NULL,
        edit_reason TEXT NULL,
        reviewed_by_rh_id INT NULL,
        FOREIGN KEY (employee_id) REFERENCES employee (id)
    );

CREATE VIEW
    view_daily_work_summary AS
SELECT
    e.name AS funcionario,
    DATE (p.timestamp) AS data,
    e.id AS employee_id_fix,
    SUM(0.0) AS horas_trabalhadas,
    SUM(0.0) AS total_pago
FROM
    employee e
    JOIN punch p ON e.id = p.employee_id
GROUP BY
    e.id,
    e.name,
    DATE (p.timestamp)
ORDER BY
    DATE (p.timestamp) DESC;