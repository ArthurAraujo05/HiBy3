CREATE TABLE employee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL
);

CREATE TABLE punch (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    timestamp DATETIME NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ORIGINAL',
    requested_timestamp DATETIME NULL,
    edit_reason TEXT NULL,
    reviewed_by_rh_id INT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE VIEW view_daily_work_summary AS
SELECT 
    e.name AS funcionario,
    DATE(p_in.timestamp) AS data,
    (SELECT TIMESTAMPDIFF(SECOND, MIN(p.timestamp), MAX(p_out.timestamp)) / 3600
     FROM punch p
     LEFT JOIN punch p_out ON p_out.employee_id = p.id AND DATE(p_out.timestamp) = DATE(p.timestamp) AND p_out.event_type = 'SAIDA'
     WHERE p.employee_id = e.id AND DATE(p.timestamp) = DATE(p_in.timestamp) AND p.event_type = 'ENTRADA') 
    AS horas_trabalhadas,
    0.00 AS total_pago
FROM employee e
JOIN punch p_in ON e.id = p_in.employee_id AND p_in.event_type = 'ENTRADA'
GROUP BY e.name, DATE(p_in.timestamp);