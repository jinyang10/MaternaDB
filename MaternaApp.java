package P3;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Swing window: sign in, then appointments on the left and visit details on the right.
 */
public class MaternaApp {
    // Teal / slate palette (no extra look-and-feel library).
    private static final Color HEADER = new Color(31, 78, 90);
    private static final Color ACCENT = new Color(45, 122, 114);
    private static final Color PAGE = new Color(244, 247, 246);
    private static final Color CARD = Color.WHITE;
    private static final Color MUTED = new Color(92, 107, 114);
    private static final Color LINE = new Color(220, 228, 226);
    private static final Font TITLE = new Font("SansSerif", Font.BOLD, 22);
    private static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);
    private static final String LOGIN = "login";
    private static final String WORKSPACE = "workspace";

    private final MaternaDb db;
    private final JFrame frame;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final JTextField pracField = new JTextField();
    private final JLabel sessionLabel = new JLabel(" ");
    // Seed data uses this date, so the first Load shows visits.
    private final JTextField dateField = new JTextField("2026-03-15");
    private final DefaultTableModel appointmentModel = new DefaultTableModel(
            new String[] {"Time", "Role", "Mother", "Health card"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable appointmentTable = new JTable(appointmentModel);
    private final JLabel visitTitle = new JLabel("Select an appointment");
    private final JLabel visitMeta = new JLabel("Choose a row on the left to review notes and tests.");
    private final DefaultTableModel notesModel = new DefaultTableModel(
            new String[] {"Date", "Time", "Observation"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel testsModel = new DefaultTableModel(
            new String[] {"Prescribed", "Type", "Result"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTextArea noteArea = new JTextArea(3, 20);
    private final JTextField testTypeField = new JTextField();
    private final JTextField testIdField = new JTextField();
    private final JTextField techIdField = new JTextField("T001"); // sample tech in sql/seed.sql
    private final JButton addNoteButton = new JButton("Add note");
    private final JButton prescribeButton = new JButton("Prescribe test");

    private MaternaDb.Midwife session;
    private String currentDate = "2026-03-15";
    private final List<MaternaDb.Appointment> appointments = new ArrayList<>();
    private MaternaDb.Appointment selected;

    public static void main(String[] args) {
        applyLookAndFeel();
        // All Swing work has to run on the event-dispatch thread.
        SwingUtilities.invokeLater(() -> {
            try {
                new MaternaApp().show();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not open the database.\n" + e.getMessage(),
                        "MaternaDB",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    MaternaApp() throws SQLException {
        db = new MaternaDb();
        frame = new JFrame("MaternaDB");
        root.setBackground(PAGE);
        root.add(buildLogin(), LOGIN);
        root.add(buildWorkspace(), WORKSPACE);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(root);
        frame.setMinimumSize(new Dimension(980, 640));
        frame.setSize(1080, 700);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                db.close(); // close JDBC
            }
        });
        setVisitEnabled(false);
    }

    void show() {
        cards.show(root, LOGIN);
        frame.setVisible(true);
        pracField.requestFocusInWindow();
    }

    // Sign-in card (practitioner id).
    private JPanel buildLogin() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(PAGE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(36, 40, 36, 40)));
        card.setPreferredSize(new Dimension(420, 340));

        JLabel kicker = label("MIDWIFERY SERVICE", SMALL, ACCENT);
        JLabel title = label("MaternaDB", TITLE, HEADER);
        JLabel subtitle = label("Sign in with your practitioner ID to see appointments.", BODY, MUTED);

        JLabel idLabel = label("Practitioner ID", SMALL, MUTED);
        styleField(pracField);
        pracField.setToolTipText("Sample ID: MW001");

        JButton signIn = primaryButton("Sign in");
        signIn.addActionListener(e -> signIn());
        pracField.addActionListener(e -> signIn());

        JLabel hint = label("See README for sample IDs, dates, and mothers.", SMALL, MUTED);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        int y = 0;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(kicker, gbc(c, y++));
        c.insets = new Insets(0, 0, 8, 0);
        card.add(title, gbc(c, y++));
        c.insets = new Insets(0, 0, 22, 0);
        card.add(subtitle, gbc(c, y++));
        c.insets = new Insets(0, 0, 6, 0);
        card.add(idLabel, gbc(c, y++));
        c.insets = new Insets(0, 0, 16, 0);
        card.add(pracField, gbc(c, y++));
        c.insets = new Insets(0, 0, 14, 0);
        card.add(signIn, gbc(c, y++));
        card.add(hint, gbc(c, y));

        page.add(card);
        return page;
    }

    // Header, appointment list on the left, visit panel on the right.
    private JPanel buildWorkspace() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(PAGE);
        page.add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildAppointmentPanel(), buildVisitPanel());
        split.setResizeWeight(0.42);
        split.setBorder(new EmptyBorder(16, 16, 16, 16));
        split.setContinuousLayout(true);
        page.add(split, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titles = new JPanel(new GridBagLayout());
        titles.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        titles.add(label("MaternaDB", new Font("SansSerif", Font.BOLD, 18), Color.WHITE), gbc(c, 0));
        sessionLabel.setFont(SMALL);
        sessionLabel.setForeground(new Color(190, 214, 214));
        titles.add(sessionLabel, gbc(c, 1));

        JButton signOut = ghostButton("Sign out");
        signOut.addActionListener(e -> signOut());

        header.add(titles, BorderLayout.WEST);
        header.add(signOut, BorderLayout.EAST);
        return header;
    }

    private JPanel buildAppointmentPanel() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 12));

        JLabel heading = label("Appointments", new Font("SansSerif", Font.BOLD, 16), HEADER);
        JLabel help = label("Load visits for a date, then select a row.", SMALL, MUTED);

        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setOpaque(false);
        styleField(dateField);
        dateField.setToolTipText("YYYY-MM-DD");
        JButton load = primaryButton("Load");
        load.setPreferredSize(new Dimension(88, 36));
        load.addActionListener(e -> loadAppointments());
        dateField.addActionListener(e -> loadAppointments());
        dateRow.add(dateField, BorderLayout.CENTER);
        dateRow.add(load, BorderLayout.EAST);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        top.add(heading, gbc(c, 0));
        c.insets = new Insets(4, 0, 12, 0);
        top.add(help, gbc(c, 1));
        c.insets = new Insets(0, 0, 0, 0);
        top.add(label("Date", SMALL, MUTED), gbc(c, 2));
        c.insets = new Insets(6, 0, 0, 0);
        top.add(dateRow, gbc(c, 3));

        styleTable(appointmentTable);
        appointmentTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onAppointmentSelected();
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(wrapTable(appointmentTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildVisitPanel() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 12));

        visitTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        visitTitle.setForeground(HEADER);
        visitMeta.setFont(SMALL);
        visitMeta.setForeground(MUTED);

        JPanel heading = new JPanel(new GridBagLayout());
        heading.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        heading.add(visitTitle, gbc(c, 0));
        c.insets = new Insets(4, 0, 0, 0);
        heading.add(visitMeta, gbc(c, 1));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BODY);
        tabs.addTab("Notes", buildNotesTab());
        tabs.addTab("Tests", buildTestsTab());

        panel.add(heading, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    // Notes for the pregnancy, plus a box to add a new observation.
    private JPanel buildNotesTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 4, 4, 4));

        JTable table = new JTable(notesModel);
        styleTable(table);
        table.getColumnModel().getColumn(2).setPreferredWidth(280);

        noteArea.setFont(BODY);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(8, 8, 8, 8)));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBorder(BorderFactory.createEmptyBorder());
        noteScroll.setPreferredSize(new Dimension(100, 86));

        addNoteButton.addActionListener(e -> addNote());
        styleSecondary(addNoteButton);

        JPanel composer = new JPanel(new BorderLayout(8, 8));
        composer.setOpaque(false);
        composer.add(label("New observation", SMALL, MUTED), BorderLayout.NORTH);
        composer.add(noteScroll, BorderLayout.CENTER);
        composer.add(alignRight(addNoteButton), BorderLayout.SOUTH);

        tab.add(wrapTable(table), BorderLayout.CENTER);
        tab.add(composer, BorderLayout.SOUTH);
        return tab;
    }

    // Tests for the pregnancy, plus fields to prescribe a new one.
    private JPanel buildTestsTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setOpaque(false);
        tab.setBorder(new EmptyBorder(10, 4, 4, 4));

        JTable table = new JTable(testsModel);
        styleTable(table);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(labeledField("Test type", testTypeField), c);
        c.weightx = 0.5;
        form.add(labeledField("Test ID", testIdField), c);
        form.add(labeledField("Technician ID", techIdField), c);
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTH;
        c.insets = new Insets(18, 0, 0, 0);
        prescribeButton.addActionListener(e -> prescribeTest());
        styleSecondary(prescribeButton);
        form.add(prescribeButton, c);

        tab.add(wrapTable(table), BorderLayout.CENTER);
        tab.add(form, BorderLayout.SOUTH);
        return tab;
    }

    private JPanel labeledField(String name, JTextField field) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(false);
        styleField(field);
        box.add(label(name, SMALL, MUTED), BorderLayout.NORTH);
        box.add(field, BorderLayout.CENTER);
        return box;
    }

    // Check the id against MIDWIVES, then open the workspace.
    private void signIn() {
        String pracId = pracField.getText() == null ? "" : pracField.getText().trim();
        if (pracId.isEmpty()) {
            alert("Enter a practitioner ID.", "Sign in");
            return;
        }
        try {
            MaternaDb.Midwife midwife = db.findMidwife(pracId);
            if (midwife == null) {
                alert("Practitioner ID not found.", "Sign in");
                return;
            }
            session = midwife;
            sessionLabel.setText(midwife.name + "  ·  " + midwife.pracId);
            cards.show(root, WORKSPACE);
            loadAppointments();
        } catch (SQLException e) {
            alert(e.getMessage(), "Sign in");
        }
    }

    // Clear tables and go back to the sign-in card.
    private void signOut() {
        session = null;
        selected = null;
        appointments.clear();
        appointmentModel.setRowCount(0);
        notesModel.setRowCount(0);
        testsModel.setRowCount(0);
        noteArea.setText("");
        testTypeField.setText("");
        pracField.setText("");
        visitTitle.setText("Select an appointment");
        visitMeta.setText("Choose a row on the left to review notes and tests.");
        setVisitEnabled(false);
        cards.show(root, LOGIN);
        pracField.requestFocusInWindow();
    }

    // Date must be YYYY-MM-DD. Selects the first visit if any exist.
    private void loadAppointments() {
        if (session == null) {
            return;
        }
        String date = dateField.getText() == null ? "" : dateField.getText().trim();
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            alert("Use a date like 2026-03-15 (YYYY-MM-DD).", "Appointments");
            return;
        }
        try {
            currentDate = date;
            appointments.clear();
            appointments.addAll(db.listAppointments(session.pracId, date));
            appointmentModel.setRowCount(0);
            for (MaternaDb.Appointment row : appointments) {
                appointmentModel.addRow(new Object[] {
                        row.time, row.roleCode(), row.motherName, row.healthCard
                });
            }
            selected = null;
            notesModel.setRowCount(0);
            testsModel.setRowCount(0);
            setVisitEnabled(false);
            if (appointments.isEmpty()) {
                visitTitle.setText("No appointments");
                visitMeta.setText("Nothing is booked for " + date + ".");
            } else {
                visitTitle.setText("Select an appointment");
                visitMeta.setText(appointments.size() + " visit"
                        + (appointments.size() == 1 ? "" : "s") + " on " + date + ".");
                appointmentTable.setRowSelectionInterval(0, 0);
            }
        } catch (SQLException e) {
            alert(e.getMessage(), "Appointments");
        }
    }

    // Show notes/tests for the row highlighted in the table.
    private void onAppointmentSelected() {
        int row = appointmentTable.getSelectedRow();
        if (row < 0 || row >= appointments.size()) {
            return;
        }
        selected = appointments.get(row);
        visitTitle.setText(selected.motherName);
        visitMeta.setText(selected.time + "  ·  " + selected.roleLabel()
                + "  ·  " + selected.healthCard);
        setVisitEnabled(true);
        refreshVisitDetails();
    }

    // Fill the notes and tests tables for the selected mother.
    private void refreshVisitDetails() {
        if (session == null || selected == null) {
            return;
        }
        try {
            notesModel.setRowCount(0);
            for (MaternaDb.Note note : db.listNotes(session.pracId, currentDate, selected.healthCard)) {
                notesModel.addRow(new Object[] {note.date, note.time, note.observation});
            }
            testsModel.setRowCount(0);
            for (MaternaDb.LabTest test : db.listTests(session.pracId, currentDate, selected.healthCard)) {
                testsModel.addRow(new Object[] {test.date, test.type, test.result});
            }
        } catch (SQLException e) {
            alert(e.getMessage(), "Visit");
        }
    }

    private void addNote() {
        if (selected == null) {
            return;
        }
        String observation = noteArea.getText() == null ? "" : noteArea.getText().trim();
        if (observation.isEmpty()) {
            alert("Type an observation first.", "Notes");
            return;
        }
        try {
            db.addNote(selected.appointId, observation);
            noteArea.setText("");
            refreshVisitDetails();
        } catch (SQLException e) {
            alert(e.getMessage(), "Notes");
        }
    }

    private void prescribeTest() {
        if (selected == null) {
            return;
        }
        String type = text(testTypeField);
        String testId = text(testIdField);
        String techId = text(techIdField);
        if (type.isEmpty() || testId.isEmpty() || techId.isEmpty()) {
            alert("Enter test type, test ID, and technician ID.", "Tests");
            return;
        }
        try {
            db.addTest(selected.appointId, testId, techId, type);
            testTypeField.setText("");
            testIdField.setText("");
            refreshVisitDetails();
        } catch (SQLException e) {
            alert(e.getMessage(), "Tests");
        }
    }

    // Grey out note/test fields until a visit is selected.
    private void setVisitEnabled(boolean enabled) {
        addNoteButton.setEnabled(enabled);
        prescribeButton.setEnabled(enabled);
        noteArea.setEnabled(enabled);
        testTypeField.setEnabled(enabled);
        testIdField.setEnabled(enabled);
        techIdField.setEnabled(enabled);
    }

    private static String text(JTextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void alert(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    private static JScrollPane wrapTable(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(CARD);
        return scroll;
    }

    private static void styleTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(214, 232, 228));
        table.setSelectionForeground(HEADER);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(236, 242, 240));
        table.getTableHeader().setForeground(HEADER);
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer padded = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                    boolean focus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                if (cell instanceof JLabel) {
                    ((JLabel) cell).setBorder(new EmptyBorder(0, 8, 0, 8));
                }
                return cell;
            }
        };
        table.setDefaultRenderer(Object.class, padded);
    }

    private static void styleField(JTextField field) {
        field.setFont(BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(8, 10, 8, 10)));
        field.setPreferredSize(new Dimension(120, 36));
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }

    private static JButton ghostButton(String text) {
        JButton button = new JButton(text);
        button.setFont(SMALL);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 214, 214)),
                new EmptyBorder(6, 12, 6, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static void styleSecondary(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(HEADER);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }

    private static JPanel alignRight(Component child) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setOpaque(false);
        row.add(child);
        return row;
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private static GridBagConstraints gbc(GridBagConstraints proto, int y) {
        GridBagConstraints copy = (GridBagConstraints) proto.clone();
        copy.gridy = y;
        return copy;
    }

    private static void applyLookAndFeel() {
        try {
            // Nimbus is built into the JDK and looks cleaner than Metal.
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", PAGE);
            UIManager.put("nimbusBase", HEADER);
            UIManager.put("nimbusBlueGrey", new Color(90, 122, 124));
            UIManager.put("nimbusFocus", ACCENT);
        } catch (Exception ignored) {
            // platform look-and-feel is fine if Nimbus is unavailable
        }
    }
}
