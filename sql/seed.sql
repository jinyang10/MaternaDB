-- Demo data for the Swing UI and the original console app.
-- Reload with: delete materna.db, then bash scripts/run.sh (or java P3.InitDb).
-- Sign in as MW001 or MW002. Technician for new tests: T001.

INSERT INTO MIDWIVES (pracID, email, name, phonenum) VALUES
    ('MW001', 'jane.midwife@example.com', 'Jane Midwife', '514-555-0101'),
    ('MW002', 'omar.backup@example.com', 'Omar Backup', '514-555-0102');

INSERT INTO MOTHERS (QCHCN, MNAME, email, dateofbirth, phonenum) VALUES
    ('HCN001', 'Alice Smith', 'alice@example.com', '1994-04-12', '514-555-1001'),
    ('HCN002', 'Carol Jones', 'carol@example.com', '1991-09-03', '514-555-1002'),
    ('HCN003', 'Dana Patel', 'dana@example.com', '1996-11-21', '514-555-1003'),
    ('HCN004', 'Elena Rossi', 'elena@example.com', '1989-02-08', '514-555-1004'),
    ('HCN005', 'Fatima Hassan', 'fatima@example.com', '1998-07-30', '514-555-1005');

INSERT INTO COUPLE (CID, QCHCN, fatherID) VALUES
    ('C001', 'HCN001', NULL),
    ('C002', 'HCN002', NULL),
    ('C003', 'HCN003', NULL),
    ('C004', 'HCN004', NULL),
    ('C005', 'HCN005', NULL);

INSERT INTO PREGNANCIES (NTHPREG, CID) VALUES
    (1, 'C001'),
    (1, 'C002'),
    (1, 'C003'),
    (1, 'C004'),
    (1, 'C005');

-- MW001 is primary for Alice, Dana, Fatima; backup for Carol and Elena.
INSERT INTO ASSIGNEDMW (pracID, NTHPREG, CID, IS_PRIMARY) VALUES
    ('MW001', 1, 'C001', TRUE),
    ('MW002', 1, 'C001', FALSE),
    ('MW001', 1, 'C002', FALSE),
    ('MW002', 1, 'C002', TRUE),
    ('MW001', 1, 'C003', TRUE),
    ('MW002', 1, 'C003', FALSE),
    ('MW001', 1, 'C004', FALSE),
    ('MW002', 1, 'C004', TRUE),
    ('MW001', 1, 'C005', TRUE),
    ('MW002', 1, 'C005', FALSE);

INSERT INTO APPOINTMENTS (APPOINTID, NTHPREG, CID, ADATE, ATIME) VALUES
    -- Alice Smith
    ('A100', 1, 'C001', '2026-03-01', '09:00:00'),
    ('A101', 1, 'C001', '2026-03-15', '09:30:00'),
    ('A102', 1, 'C001', '2026-03-15', '14:00:00'),
    ('A103', 1, 'C001', '2026-03-22', '10:00:00'),
    ('A104', 1, 'C001', '2026-04-02', '09:15:00'),
    -- Carol Jones
    ('A200', 1, 'C002', '2026-03-15', '11:15:00'),
    ('A201', 1, 'C002', '2026-03-22', '13:30:00'),
    ('A202', 1, 'C002', '2026-04-10', '11:00:00'),
    -- Dana Patel
    ('A300', 1, 'C003', '2026-03-15', '16:00:00'),
    ('A301', 1, 'C003', '2026-03-22', '08:45:00'),
    ('A302', 1, 'C003', '2026-04-02', '14:30:00'),
    -- Elena Rossi
    ('A400', 1, 'C004', '2026-03-22', '15:00:00'),
    ('A401', 1, 'C004', '2026-04-10', '09:45:00'),
    -- Fatima Hassan
    ('A500', 1, 'C005', '2026-04-02', '11:00:00'),
    ('A501', 1, 'C005', '2026-04-10', '13:15:00');

-- Both assigned midwives can open each visit (P vs B comes from ASSIGNEDMW).
INSERT INTO SETAPPOINT (pracID, APPOINTID) VALUES
    ('MW001', 'A100'), ('MW002', 'A100'),
    ('MW001', 'A101'), ('MW002', 'A101'),
    ('MW001', 'A102'), ('MW002', 'A102'),
    ('MW001', 'A103'), ('MW002', 'A103'),
    ('MW001', 'A104'), ('MW002', 'A104'),
    ('MW001', 'A200'), ('MW002', 'A200'),
    ('MW001', 'A201'), ('MW002', 'A201'),
    ('MW001', 'A202'), ('MW002', 'A202'),
    ('MW001', 'A300'), ('MW002', 'A300'),
    ('MW001', 'A301'), ('MW002', 'A301'),
    ('MW001', 'A302'), ('MW002', 'A302'),
    ('MW001', 'A400'), ('MW002', 'A400'),
    ('MW001', 'A401'), ('MW002', 'A401'),
    ('MW001', 'A500'), ('MW002', 'A500'),
    ('MW001', 'A501'), ('MW002', 'A501');

INSERT INTO NOTES (NTIME, APPOINTID, OBSERV) VALUES
    ('09:05:00', 'A100', 'First prenatal visit. Blood pressure 118/76. Fundal height on track.'),
    ('09:12:00', 'A100', 'Discussed prenatal vitamins and nausea in the morning.'),
    ('09:40:00', 'A101', 'Follow-up: baby movement reported as normal.'),
    ('09:48:00', 'A101', 'Glucose screen explained. Client has questions about fasting.'),
    ('14:10:00', 'A102', 'Discussed birth plan and questions about labor.'),
    ('10:08:00', 'A103', 'Fetal heart rate 140. Client reports mild swelling in the evening.'),
    ('09:20:00', 'A104', 'Growth looks good. Reviewed kick counts for the third trimester.'),
    ('11:20:00', 'A200', 'Backup midwife check-in. No concerns today.'),
    ('13:38:00', 'A201', 'Blood pressure a little high today (138/88). Recheck next visit.'),
    ('11:10:00', 'A202', 'Feeling well. Planning a hospital tour next week.'),
    ('16:05:00', 'A300', 'New client intake. Estimated due date late July.'),
    ('16:18:00', 'A300', 'Allergy to penicillin noted. No medications at home besides prenatal vitamins.'),
    ('08:50:00', 'A301', 'Dating ultrasound booked. Diet and exercise discussed.'),
    ('14:40:00', 'A302', 'Client anxious about work travel. Reviewed warning signs.'),
    ('15:10:00', 'A400', 'Transfer of care from another clinic. Records requested.'),
    ('09:52:00', 'A401', 'Group B strep swab done. Client wants a water birth if available.'),
    ('11:06:00', 'A500', 'First visit with Jane. BMI in healthy range. Flu shot offered.'),
    ('13:22:00', 'A501', 'Partner attended. Asked about parental leave paperwork.');

INSERT INTO TECHNICIANS (TECHID, name, phonenum) VALUES
    ('T001', 'Lab Tech One', '514-555-2001'),
    ('T002', 'Lab Tech Two', '514-555-2002');

INSERT INTO TESTS (TESTID, TECHID, APPOINTID, PRESCDATE, SAMPDATE, TESTTYPE, LABDATE, RESULT) VALUES
    ('TST01', 'T001', 'A100', '2026-03-01', '2026-03-01', 'blood work', '2026-03-03', 'iron slightly low'),
    ('TST02', 'T001', 'A101', '2026-03-15', '2026-03-15', 'glucose', NULL, NULL),
    ('TST03', 'T001', 'A201', '2026-03-22', '2026-03-22', 'urine dip', '2026-03-22', 'trace protein'),
    ('TST04', 'T002', 'A300', '2026-03-15', '2026-03-15', 'blood type', '2026-03-16', 'A+'),
    ('TST05', 'T001', 'A401', '2026-04-10', '2026-04-10', 'GBS swab', NULL, NULL);
