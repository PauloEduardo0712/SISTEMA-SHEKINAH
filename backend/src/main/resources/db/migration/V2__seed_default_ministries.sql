INSERT INTO ministries (created_at, updated_at, name, description, active)
SELECT CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Diaconos', 'Diaconos', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ministries WHERE LOWER(name) = LOWER('Diaconos'));

INSERT INTO ministries (created_at, updated_at, name, description, active)
SELECT CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'EBD', 'EBD', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ministries WHERE LOWER(name) = LOWER('EBD'));

INSERT INTO ministries (created_at, updated_at, name, description, active)
SELECT CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Intercessao', 'Intercessao', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ministries WHERE LOWER(name) = LOWER('Intercessao'));

INSERT INTO ministries (created_at, updated_at, name, description, active)
SELECT CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Recepcao', 'Recepcao', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ministries WHERE LOWER(name) = LOWER('Recepcao'));

INSERT INTO ministries (created_at, updated_at, name, description, active)
SELECT CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Louvor', 'Louvor', TRUE
WHERE NOT EXISTS (SELECT 1 FROM ministries WHERE LOWER(name) = LOWER('Louvor'));
