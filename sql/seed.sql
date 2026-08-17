-- Demo data so the console app can be walked through without the McGill DB2 server.
-- Practitioner ID: MW001
-- Appointment date: 2026-03-15
-- When prescribing a test, any testID is fine; use technician T001.

INSERT INTO MIDWIVES (pracID, email, name, phonenum) VALUES
    ('MW001', 'jane.midwife@example.com', 'Jane Midwife', '514-555-0101'),
    ('MW002', 'omar.backup@example.com', 'Omar Backup', '514-555-0102');

INSERT INTO MOTHERS (QCHCN, MNAME, email, dateofbirth, phonenum) VALUES
    ('HCN001', 'Alice Smith', 'alice@example.com', '1994-04-12', '514-555-1001'),
    ('HCN002', 'Carol Jones', 'carol@example.com', '1991-09-03', '514-555-1002');

INSERT INTO COUPLE (CID, QCHCN, fatherID) VALUES
    ('C001', 'HCN001', NULL),
    ('C002', 'HCN002', NULL);

INSERT INTO PREGNANCIES (NTHPREG, CID) VALUES
    (1, 'C001'),
    (1, 'C002');

INSERT INTO ASSIGNEDMW (pracID, NTHPREG, CID, IS_PRIMARY) VALUES
    ('MW001', 1, 'C001', TRUE),
    ('MW002', 1, 'C001', FALSE),
    ('MW001', 1, 'C002', FALSE),
    ('MW002', 1, 'C002', TRUE);

INSERT INTO APPOINTMENTS (APPOINTID, NTHPREG, CID, ADATE, ATIME) VALUES
    ('A100', 1, 'C001', '2026-03-01', '09:00:00'),
    ('A101', 1, 'C001', '2026-03-15', '09:30:00'),
    ('A102', 1, 'C001', '2026-03-15', '14:00:00'),
    ('A200', 1, 'C002', '2026-03-15', '11:15:00');

INSERT INTO SETAPPOINT (pracID, APPOINTID) VALUES
    ('MW001', 'A100'),
    ('MW001', 'A101'),
    ('MW001', 'A102'),
    ('MW001', 'A200'),
    ('MW002', 'A200');

INSERT INTO NOTES (NTIME, APPOINTID, OBSERV) VALUES
    ('09:05:00', 'A100', 'First prenatal visit. Blood pressure normal.'),
    ('09:40:00', 'A101', 'Follow-up: baby movement reported as normal.'),
    ('14:10:00', 'A102', 'Discussed birth plan and questions about labor.'),
    ('11:20:00', 'A200', 'Backup midwife check-in. No concerns today.');

INSERT INTO TECHNICIANS (TECHID, name, phonenum) VALUES
    ('T001', 'Lab Tech One', '514-555-2001');

INSERT INTO TESTS (TESTID, TECHID, APPOINTID, PRESCDATE, SAMPDATE, TESTTYPE, LABDATE, RESULT) VALUES
    ('TST01', 'T001', 'A100', '2026-03-01', '2026-03-01', 'blood work', '2026-03-03', 'iron slightly low'),
    ('TST02', 'T001', 'A101', '2026-03-15', '2026-03-15', 'glucose', NULL, NULL);
