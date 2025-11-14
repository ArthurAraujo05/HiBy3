CREATE VIEW
    view_daily_work_summary AS
SELECT
    e.name AS funcionario,
    DATE (p_in.timestamp) AS data,
    e.id AS employee_id_fix,
    -- Calcula a soma das horas trabalhadas:
    -- Encontra a diferença em SEGUNDOS entre SAIDA e ENTRADA e divide por 3600 (segundos por hora)
    SUM(
        TIMESTAMPDIFF (SECOND, p_in.timestamp, p_out.timestamp) / 3600
    ) AS horas_trabalhadas,
    -- Calcula o custo total: Horas Trabalhadas * Hourly Rate
    SUM(
        TIMESTAMPDIFF (SECOND, p_in.timestamp, p_out.timestamp) / 3600 * e.hourly_rate
    ) AS total_pago
FROM
    employee e
    -- Entradas (JOIN com a tabela punch)
    JOIN punch p_in ON e.id = p_in.employee_id
    AND p_in.event_type = 'ENTRADA'
    -- Saídas (JOIN com a tabela punch novamente, buscando a próxima SAÍDA)
    JOIN punch p_out ON p_in.employee_id = p_out.employee_id
    AND DATE (p_in.timestamp) = DATE (p_out.timestamp) -- No mesmo dia
    AND p_out.event_type = 'SAIDA' -- É um evento de SAÍDA
    AND p_out.timestamp > p_in.timestamp -- A saída deve ser DEPOIS da entrada
WHERE
    p_in.status = 'ORIGINAL'
    OR p_in.status = 'APROVADO' -- Considera apenas pontos válidos
GROUP BY
    e.id,
    e.name,
    DATE (p_in.timestamp)
ORDER BY
    DATE (p_in.timestamp) DESC;