package P3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads schema and sample data into whichever database JDBC_URL points at.
 *
 *   javac -cp lib/*: -d . InitDb.java goBabbyApp.java
 *   java -cp lib/*:. P3.InitDb
 */
public class InitDb {
    public static void main(String[] args) throws Exception {
        Path schema = Path.of(args.length > 0 ? args[0] : "sql/schema.sql");
        Path seed = Path.of(args.length > 1 ? args[1] : "sql/seed.sql");

        try (Connection con = goBabbyApp.openConnection();
             Statement st = con.createStatement()) {
            runSqlFile(st, schema);
            runSqlFile(st, seed);
            System.out.println("Loaded " + schema + " and " + seed);

            try (ResultSet rs = st.executeQuery(
                    "SELECT pracID FROM MIDWIVES ORDER BY pracID")) {
                System.out.print("Sample practitioner IDs: ");
                List<String> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
                System.out.println(String.join(", ", ids));
            }
            System.out.println("You can now run: java -cp lib/*:. P3.goBabbyApp");
        }
    }

    static void runSqlFile(Statement st, Path file) throws IOException, SQLException {
        if (!Files.exists(file)) {
            throw new IOException("SQL file not found: " + file.toAbsolutePath());
        }
        String sql = Files.readString(file, StandardCharsets.UTF_8);
        for (String statement : splitStatements(sql)) {
            st.execute(statement);
        }
    }

    static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String rawLine : sql.split("\n")) {
            String line = rawLine;
            int comment = line.indexOf("--");
            if (comment >= 0) {
                line = line.substring(0, comment);
            }
            current.append(line).append('\n');
            if (line.contains(";")) {
                String stmt = current.toString().trim();
                if (stmt.endsWith(";")) {
                    stmt = stmt.substring(0, stmt.length() - 1).trim();
                }
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                current.setLength(0);
            }
        }
        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements;
    }
}
