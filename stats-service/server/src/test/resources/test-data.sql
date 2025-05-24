INSERT INTO hits (app, uri, ip, timestamp)
VALUES
    ('event-service', '/events/1', '192.168.1.1', TIMESTAMP '2023-01-01 12:00:00'),
    ('event-service', '/events/1', '192.168.1.2', TIMESTAMP '2023-01-01 12:30:00'),
    ('event-service', '/events/1', '192.168.1.1', TIMESTAMP '2023-01-02 10:00:00'),
    ('event-service', '/events/2', '192.168.1.3', TIMESTAMP '2023-01-01 15:00:00'),
    ('event-service', '/events/2', '192.168.1.3', TIMESTAMP '2023-01-02 11:00:00'),
    ('user-service', '/users', '192.168.1.5', TIMESTAMP '2023-01-02 14:00:00');