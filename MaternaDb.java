package P3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access used by the Swing UI. Queries match the original console app.
 */
public class MaternaDb implements AutoCloseable {
    private final Connection connection;

    public MaternaDb() throws SQLException {
        this.connection = openConnection();
    }

    public MaternaDb(Connection connection) {
        this.connection = connection;
    }

    // JDBC_URL / DATABASE_URL, or a local materna.db SQLite file.
    static Connection openConnection() throws SQLException {
        String url = firstEnv("JDBC_URL", "DATABASE_URL");
        if (url.isEmpty()) {
            url = "jdbc:sqlite:materna.db";
            System.err.println("No JDBC_URL/DATABASE_URL set; using local SQLite file materna.db");
        }
        url = toJdbcUrl(url);

        String userid = firstEnv("JDBC_USER", "SOCSUSER");
        String password = firstEnv("JDBC_PASSWORD", "SOCSPASSWD");
        registerDriver(url);

        if (userid.isEmpty() && password.isEmpty()) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, userid, password);
    }

    // Returns null if that practitioner id is not in MIDWIVES.
    Midwife findMidwife(String pracId) throws SQLException {
        String sql = "SELECT pracID, name FROM MIDWIVES WHERE pracID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pracId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String name = rs.getString("name");
                if (name == null || name.trim().isEmpty()) {
                    name = rs.getString("pracID");
                }
                return new Midwife(rs.getString("pracID"), name);
            }
        }
    }

    // Visits for this midwife on one date, ordered by time.
    List<Appointment> listAppointments(String pracId, String date) throws SQLException {
        String sql = "SELECT DISTINCT app.APPOINTID, app.ATIME, amw.IS_PRIMARY, m.MNAME, m.QCHCN "
                + "FROM ASSIGNEDMW amw "
                + "JOIN APPOINTMENTS app ON app.CID = amw.CID AND app.NTHPREG = amw.NTHPREG "
                + "JOIN SETAPPOINT sa ON sa.PRACID = amw.PRACID AND sa.APPOINTID = app.APPOINTID "
                + "JOIN COUPLE c ON app.CID = c.CID "
                + "JOIN MOTHERS m ON c.QCHCN = m.QCHCN "
                + "WHERE amw.PRACID = ? AND app.ADATE = ? "
                + "ORDER BY app.ATIME";
        List<Appointment> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pracId);
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Appointment(
                            rs.getString("APPOINTID"),
                            normalizeTime(rs.getString("ATIME")),
                            rs.getBoolean("IS_PRIMARY"),
                            rs.getString("MNAME"),
                            rs.getString("QCHCN")));
                }
            }
        }
        return rows;
    }

    // Notes for the pregnancy linked to this visit (newest first).
    List<Note> listNotes(String pracId, String date, String healthCard) throws SQLException {
        String sql = "WITH mwInfo(nthpreg, cid) AS ("
                + "SELECT app.NTHPREG, app.CID FROM ASSIGNEDMW amw, APPOINTMENTS app, COUPLE c "
                + "WHERE amw.PRACID = ? AND app.ADATE = ? AND c.QCHCN = ? AND c.CID = app.CID) "
                + "SELECT DISTINCT ADATE, NTIME, OBSERV "
                + "FROM APPOINTMENTS app, mwInfo mwI, NOTES n "
                + "WHERE app.NTHPREG = mwI.nthpreg AND app.CID = mwI.cid "
                + "AND app.APPOINTID = n.APPOINTID ORDER BY ADATE DESC, NTIME DESC";
        List<Note> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pracId);
            ps.setString(2, date);
            ps.setString(3, healthCard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Note(
                            String.valueOf(rs.getString("ADATE")),
                            normalizeTime(rs.getString("NTIME")),
                            rs.getString("OBSERV")));
                }
            }
        }
        return rows;
    }

    // Tests for that pregnancy. Empty RESULT is shown as PENDING.
    List<LabTest> listTests(String pracId, String date, String healthCard) throws SQLException {
        String sql = "WITH mwInfo(nthpreg, cid) AS ("
                + "SELECT app.NTHPREG, app.CID FROM ASSIGNEDMW amw, APPOINTMENTS app, COUPLE c "
                + "WHERE amw.PRACID = ? AND app.ADATE = ? AND c.QCHCN = ? AND c.CID = app.CID) "
                + "SELECT DISTINCT PRESCDATE, TESTTYPE, "
                + "COALESCE(RESULT, 'PENDING') AS RESULT "
                + "FROM APPOINTMENTS app, mwInfo mwI, TESTS t "
                + "WHERE app.NTHPREG = mwI.nthpreg AND app.CID = mwI.cid "
                + "AND app.APPOINTID = t.APPOINTID ORDER BY PRESCDATE DESC";
        List<LabTest> rows = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pracId);
            ps.setString(2, date);
            ps.setString(3, healthCard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String result = rs.getString("RESULT");
                    if (result == null || result.trim().isEmpty()) {
                        result = "PENDING";
                    }
                    rows.add(new LabTest(
                            String.valueOf(rs.getString("PRESCDATE")),
                            rs.getString("TESTTYPE"),
                            result));
                }
            }
        }
        return rows;
    }

    // Observation on this appointment; NTIME is the database clock.
    void addNote(String appointId, String observation) throws SQLException {
        String sql = "INSERT INTO NOTES (NTIME, APPOINTID, OBSERV) VALUES (CURRENT_TIME, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, appointId);
            ps.setString(2, observation);
            if (ps.executeUpdate() < 1) {
                throw new SQLException("No appointment matched; note was not saved.");
            }
        }
    }

    // New test: sample date is today, result starts empty.
    void addTest(String appointId, String testId, String techId, String testType) throws SQLException {
        String sql = "INSERT INTO TESTS (TESTID, TECHID, APPOINTID, PRESCDATE, SAMPDATE, TESTTYPE, LABDATE, RESULT) "
                + "VALUES (?, ?, ?, CURRENT_DATE, CURRENT_DATE, ?, NULL, NULL)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, testId);
            ps.setString(2, techId);
            ps.setString(3, appointId);
            ps.setString(4, testType);
            if (ps.executeUpdate() < 1) {
                throw new SQLException("No appointment matched; test was not saved.");
            }
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // closing is best-effort
        }
    }

    // First non-empty environment variable from the list.
    static String firstEnv(String... keys) {
        for (String key : keys) {
            String value = System.getenv(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    // postgres://... from Neon/Supabase becomes jdbc:postgresql://...
    static String toJdbcUrl(String url) {
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + url.substring(url.indexOf("://") + 3);
        }
        return url;
    }

    // Keep HH:MM:SS if the driver returns a datetime string.
    static String normalizeTime(String raw) {
        if (raw == null) {
            return "";
        }
        String timePart = raw;
        int space = raw.indexOf(' ');
        if (space >= 0 && space + 1 < raw.length()) {
            timePart = raw.substring(space + 1);
        }
        if (timePart.length() >= 8 && timePart.charAt(2) == ':') {
            return timePart.substring(0, 8);
        }
        return timePart;
    }

    // Load the JDBC driver for SQLite, PostgreSQL, or DB2.
    static void registerDriver(String url) {
        String className;
        if (url.startsWith("jdbc:db2:")) {
            className = "com.ibm.db2.jcc.DB2Driver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            className = "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:sqlite:")) {
            className = "org.sqlite.JDBC";
        } else {
            return;
        }
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found on the classpath: " + className);
            System.err.println("Download the matching driver JAR (see README) and include it with -cp.");
            System.exit(1);
        }
    }

    // Simple rows passed to the Swing tables.
    static final class Midwife {
        final String pracId;
        final String name;

        Midwife(String pracId, String name) {
            this.pracId = pracId;
            this.name = name;
        }
    }

    static final class Appointment {
        final String appointId;
        final String time;
        final boolean primary;
        final String motherName;
        final String healthCard;

        Appointment(String appointId, String time, boolean primary, String motherName, String healthCard) {
            this.appointId = appointId;
            this.time = time;
            this.primary = primary;
            this.motherName = motherName;
            this.healthCard = healthCard;
        }

        String roleLabel() {
            return primary ? "Primary" : "Backup";
        }

        String roleCode() {
            return primary ? "P" : "B";
        }
    }

    static final class Note {
        final String date;
        final String time;
        final String observation;

        Note(String date, String time, String observation) {
            this.date = date;
            this.time = time;
            this.observation = observation == null ? "" : observation;
        }
    }

    static final class LabTest {
        final String date;
        final String type;
        final String result;

        LabTest(String date, String type, String result) {
            this.date = date;
            this.type = type == null ? "" : type;
            this.result = result == null ? "PENDING" : result;
        }
    }
}
