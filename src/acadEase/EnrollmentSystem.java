package acadEase;



import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import com.toedter.calendar.JDateChooser;

public class EnrollmentSystem extends JFrame {
    private CardLayout card = new CardLayout();
    private JPanel mainPanel;
    private JPanel storedEnrollPanel; 
    private List<Student> students;
    private File dataFile;
    private Student currentStudentForPayment;
    
 // Student ID counter tracking
    private Map<Integer, Map<Character, Integer>> yearCounters = new HashMap<>();
    private File counterFile = new File("id_counters.dat");

    // Instructor & Assignment Management
    private List<Instructor> instructors = new ArrayList<>();
    private File instructorFile = new File("instructors.dat");
    private Map<String, String> instructorAssignments = new HashMap<>();
    private File assignmentsFile = new File("assignments.dat");
    private Map<String, Map<String, String>> schedules = new HashMap<>();
    private File schedulesFile = new File("schedules.dat");
    
    private static int instructorIdCounter = 2000;
    private static int studentIdCounter = 1000;

    // TUITION FEE CONSTANTS PER PAYMENT PLAN (Grade 7 & 8)
    private static final Map<String, Map<String, Double>> TUITION_FEES = new HashMap<>();
    static {
        // Grade 7
        Map<String, Double> grade7Fees = new HashMap<>();
        grade7Fees.put("Full", 29000.0);
        grade7Fees.put("Semi", 30000.0);
        grade7Fees.put("Quarterly", 31200.0);
        grade7Fees.put("Monthly", 33000.0);
        TUITION_FEES.put("Grade 7", grade7Fees);
        
        // Grade 8
        Map<String, Double> grade8Fees = new HashMap<>();
        grade8Fees.put("Full", 29000.0);
        grade8Fees.put("Semi", 30000.0);
        grade8Fees.put("Quarterly", 31200.0);
        grade8Fees.put("Monthly", 33000.0);
        TUITION_FEES.put("Grade 8", grade8Fees);
        
        // Grade 9
        Map<String, Double> grade9Fees = new HashMap<>();
        grade9Fees.put("Full", 30000.0);
        grade9Fees.put("Semi", 31000.0);
        grade9Fees.put("Quarterly", 32400.0);
        grade9Fees.put("Monthly", 34000.0);
        TUITION_FEES.put("Grade 9", grade9Fees);
        
        // Grade 10
        Map<String, Double> grade10Fees = new HashMap<>();
        grade10Fees.put("Full", 31000.0);
        grade10Fees.put("Semi", 32000.0);
        grade10Fees.put("Quarterly", 33600.0);
        grade10Fees.put("Monthly", 35000.0);
        TUITION_FEES.put("Grade 10", grade10Fees);
    }

    private static final double MISC_NEW_TRANSFEREE = 2000;
    private static final double MISC_OLD = 1500;
    private static final double ESC_GRANT = 9000;

    // SECTION NAMES MAPPING
    private static final Map<String, String[]> SECTION_NAMES = new HashMap<>();
    static {
        SECTION_NAMES.put("Grade 7", new String[]{"HUMILITY", "COURAGE"});
        SECTION_NAMES.put("Grade 8", new String[]{"INTEGRITY", "RESILIENCE"});
        SECTION_NAMES.put("Grade 9", new String[]{"DETERMINATION", "GRATITUDE"});
        SECTION_NAMES.put("Grade 10", new String[]{"FORTITUDE", "HONESTY"});
    }
    // BANK ACCOUNT INFORMATION MAP
    private static final Map<String, String[]> BANK_ACCOUNTS = new HashMap<>();
    static {
        BANK_ACCOUNTS.put("PNB", new String[]{"ABC High School", "1234-5678-9012-3456"});
        BANK_ACCOUNTS.put("BDO", new String[]{"ABC High School", "2345-6789-0123-4567"});
        BANK_ACCOUNTS.put("Metrobank", new String[]{"ABC High School", "3456-7890-1234-5678"});
        BANK_ACCOUNTS.put("BPI", new String[]{"ABC High School", "4567-8901-2345-6789"});
        BANK_ACCOUNTS.put("Landbank", new String[]{"ABC High School", "5678-9012-3456-7890"});
        BANK_ACCOUNTS.put("Union Bank", new String[]{"ABC High School", "6789-0123-4567-8901"});
    }
    // SUBJECT CONFIGURATION (Updated with section names)
    private static final Map<String, Set<String>> SECTION_SUBJECTS = new HashMap<>();
    static {
        // Grade 7 - HUMILITY, COURAGE
        SECTION_SUBJECTS.put("Grade 7-HUMILITY", Set.of("Mathematics", "English", "Science", "Filipino", "Araling Panlipunan", "GMRC"));
        SECTION_SUBJECTS.put("Grade 7-COURAGE", Set.of("English", "Mathematics", "Filipino", "Science", "MAPEH", "GMRC"));
        // Grade 8 - INTEGRITY, RESILIENCE
        SECTION_SUBJECTS.put("Grade 8-INTEGRITY", Set.of("Mathematics", "English", "Science", "TLE", "Araling Panlipunan", "GMRC"));
        SECTION_SUBJECTS.put("Grade 8-RESILIENCE", Set.of("English", "Mathematics", "Araling Panlipunan", "Science", "Filipino", "GMRC"));
        // Grade 9 - DETERMINATION, GRATITUDE
        SECTION_SUBJECTS.put("Grade 9-DETERMINATION", Set.of("Science", "English", "Mathematics", "Araling Panlipunan", "TLE", "MAPEH"));
        SECTION_SUBJECTS.put("Grade 9-GRATITUDE", Set.of("English", "Science", "Araling Panlipunan", "Mathematics", "Music/Arts", "TLE"));
        // Grade 10 - FORTITUDE, HONESTY
        SECTION_SUBJECTS.put("Grade 10-FORTITUDE", Set.of("Science", "Mathematics", "English", "Araling Panlipunan", "TLE", "MAPEH"));
        SECTION_SUBJECTS.put("Grade 10-HONESTY", Set.of("English", "Science", "Mathematics", "Araling Panlipunan", "TLE", "MAPEH"));
    }

    // Admission Panel Components
    private DefaultTableModel adminTableModel;
    private int selectedStudentIndex = -1;
    private JPanel rightDetailPanel;
    private JTable adminTable;

    // Student Info Panel Components
    private DefaultTableModel studentInfoTableModel;
    private JTable studentInfoTable;
    private int selectedStudentInfoIndex = -1;
    private JPanel rightStudentInfoPanel;

    // Class & Section Panel Components
    private DefaultTableModel classSectionTableModel;
    private JTable classSectionTable;
    private int selectedClassStudentIndex = -1;
    private JPanel rightClassDetailPanel;
    private DefaultTableModel sectionStudentTableModel;
    private JTable sectionStudentTable;
    private int selectedSectionStudentIndex = -1;
    private JButton changeSectionBtn;

    public EnrollmentSystem() {
        this.mainPanel = new JPanel(card);
        this.students = new ArrayList<>();
        this.dataFile = new File("students.dat");
        setTitle("AcadEase Enrollment System");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadData();
        loadInstructors();
        loadAssignments();
        loadSchedules();
        loadCounters(); // ADDED

        mainPanel.add(mainMenuPanel(), "menu");
        mainPanel.add(enrollmentPanel(), "enroll");
        mainPanel.add(paymentPanel(), "payment");
        mainPanel.add(adminLoginPanel(), "login");
        mainPanel.add(adminDashboardPanel(), "dashboard");
        mainPanel.add(statusCheckerPanel(), "statusChecker");

        add(mainPanel);
        card.show(mainPanel, "menu");
        setVisible(true);
    }

    private JPanel mainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(211, 211, 211)); 

        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(panel.getBackground());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;

        JPanel verticalBox = new JPanel();
        verticalBox.setLayout(new BoxLayout(verticalBox, BoxLayout.Y_AXIS));
        verticalBox.setBackground(panel.getBackground());

        JLabel titleLabel = new JLabel("AcadEase");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(new Color(46, 109, 27));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("School Enrollment Access");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 28));
        subtitleLabel.setForeground(new Color(46, 109, 27));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        verticalBox.add(titleLabel);
        verticalBox.add(Box.createRigidArea(new Dimension(0, 10)));
        verticalBox.add(subtitleLabel);
        verticalBox.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(panel.getBackground());
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension buttonSize = new Dimension(350, 45);

        JButton enrollNewTransferee = new JButton("New/Transferee Student");
        styleEnrollmentButton(enrollNewTransferee, buttonSize); 

        JButton enrollOld = new JButton("Old Student");
        styleEnrollmentButton(enrollOld, buttonSize); 

        buttonsPanel.add(enrollNewTransferee);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonsPanel.add(enrollOld);

        verticalBox.add(buttonsPanel);
        verticalBox.add(Box.createRigidArea(new Dimension(0, 30)));

        JButton statusBtn = new JButton("Check enrollment status");
        statusBtn.setFont(new Font("Arial", Font.PLAIN, 15));
        statusBtn.setBackground(Color.WHITE);
        statusBtn.setForeground(new Color(46, 109, 27));
        statusBtn.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27), 1));
        statusBtn.setPreferredSize(buttonSize);
        statusBtn.setMaximumSize(buttonSize);
        statusBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusBtn.setFocusPainted(false);
        statusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        statusBtn.setMargin(new Insets(5, 10, 5, 10));
        verticalBox.add(statusBtn);

        JLabel adminLink = new JLabel("<html><i>Login as admin</i></html>");
        adminLink.setFont(new Font("Arial", Font.PLAIN, 13));
        adminLink.setForeground(Color.GRAY);
        adminLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        adminLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminLink.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        adminLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                adminLink.setText("<html><u><i>Login as admin</i></u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                adminLink.setText("<html><i>Login as admin</i></html>");
            }
        });

        verticalBox.add(adminLink);

        centerContainer.add(verticalBox, gbc);
        panel.add(centerContainer, BorderLayout.CENTER);

        enrollNewTransferee.addActionListener(e -> showEnrollmentForm("New"));
        enrollOld.addActionListener(e -> showEnrollmentForm("Old"));
        statusBtn.addActionListener(e -> showStatusChecker());
        adminLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                card.show(mainPanel, "login");
            }
        });

        return panel;
    }

    private void styleEnrollmentButton(JButton button, Dimension size) {
        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setBackground(new Color(176, 176, 184));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(5, 10, 5, 10));

        Color defaultBackground = button.getBackground();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(173, 207, 157)); 
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(defaultBackground); 
            }
        });
    }

    private String currentStudentType = "";

    private void showEnrollmentForm(String type) {
        currentStudentType = type;
        JPanel newEnrollPanel = enrollmentPanel();
        
        if (storedEnrollPanel != null) {
            mainPanel.remove(storedEnrollPanel);
        }
        storedEnrollPanel = enrollmentPanel(); 
        mainPanel.add(storedEnrollPanel, "enroll"); 
        card.show(mainPanel, "enroll");
    }
    
    private void addLabeledField(JPanel panel, GridBagConstraints gbc,
            String labelText, JComponent field,
            Map<String, JComponent> fieldMap, String fieldKey) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        
        fieldMap.put(fieldKey, field);
    }

    private JPanel enrollmentPanel() {
        JPanel enrollmentMainPanel = new JPanel(new BorderLayout(15, 15));
        enrollmentMainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        enrollmentMainPanel.setBackground(new Color(217, 217, 217));

        // Left panel - student's info (Made Scrollable)
        JPanel studentInfoPanelContent = new JPanel(new GridBagLayout()); // Content panel for scroll
        studentInfoPanelContent.setBackground(Color.white);
        studentInfoPanelContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.gridx = 0;
        int row = 0;

        JLabel studentInfoHeader = new JLabel("Student's Information");
        studentInfoHeader.setFont(new Font("Arial", Font.BOLD, 22));
        studentInfoHeader.setForeground(new Color(46, 109, 27));
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        studentInfoPanelContent.add(studentInfoHeader, gbc);
        gbc.gridwidth = 1;
        Map<String, JComponent> studentFields = new HashMap<>();

        // Grade level selection
        String[] years = {"---", "Grade 7", "Grade 8", "Grade 9", "Grade 10"};
        JComboBox<String> cbYear = new JComboBox<>(years);
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, cbYear, studentFields, "yearLevel", "Grade Level to enroll");

        // PAYMENT PLAN SELECTION (NEW REQUIREMENT)
        String[] paymentPlans = {"---", "Full", "Semi", "Quarterly", "Monthly"};
        JComboBox<String> cbPaymentPlan = new JComboBox<>(paymentPlans);
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, cbPaymentPlan, studentFields, "paymentPlan", "Payment Plan:");

        // Student ID (only for Old students)
        final JTextField[] tfStudentIdHolder = new JTextField[1];
        if ("Old".equals(currentStudentType)) {
            JTextField tfStudentIdLocal = new JTextField(20);
            tfStudentIdLocal.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
            tfStudentIdLocal.setMargin(new Insets(5, 5, 5, 5));
            gbc.gridy = row++;
            addLabeledFieldFullWidth(studentInfoPanelContent, gbc, tfStudentIdLocal, studentFields, "studentId", "Student ID No.:");
            tfStudentIdHolder[0] = tfStudentIdLocal;
        }

        // First Name
        JTextField tfFirst = new JTextField(20);
        tfFirst.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfFirst.setMargin(new Insets(5, 5, 5, 5));
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, tfFirst, studentFields, "firstName", "First Name:");

        // Middle Name
        JTextField tfMiddle = new JTextField(20);
        tfMiddle.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfMiddle.setMargin(new Insets(5, 5, 5, 5));
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, tfMiddle, studentFields, "middleName", "Middle Name:");

        // Last Name
        JTextField tfLast = new JTextField(20);
        tfLast.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfLast.setMargin(new Insets(5, 5, 5, 5));
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, tfLast, studentFields, "lastName", "Last Name:");

        // Age and Birthday labels
        JLabel lblAge = new JLabel("Age:");
        JLabel lblBirthday = new JLabel("Birthday:");
        lblAge.setFont(new Font("Arial", Font.PLAIN, 14));
        lblBirthday.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel labelsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        labelsPanel.setBackground(Color.WHITE);
        labelsPanel.add(lblAge);
        labelsPanel.add(lblBirthday);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        studentInfoPanelContent.add(labelsPanel, gbc);
        gbc.gridwidth = 1;

        // Age and Birthday inputs
        JPanel ageBirthdayPanel = new JPanel(new GridBagLayout());
        ageBirthdayPanel.setBackground(Color.WHITE);
        GridBagConstraints abGbc = new GridBagConstraints();
        abGbc.insets = new Insets(0, 0, 0, 5);
        abGbc.fill = GridBagConstraints.HORIZONTAL;
        abGbc.gridy = 0;
        JTextField tfAge = new JTextField(10);
        tfAge.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfAge.setMargin(new Insets(5, 5, 5, 5));
        abGbc.gridx = 0;
        abGbc.weightx = 0.3;
        ageBirthdayPanel.add(tfAge, abGbc);
        studentFields.put("age", tfAge);

        abGbc.gridx = 1;
        abGbc.weightx = 0.1;
        ageBirthdayPanel.add(Box.createRigidArea(new Dimension(10, 0)), abGbc);

        com.toedter.calendar.JDateChooser dateChooser = new com.toedter.calendar.JDateChooser();
        dateChooser.setDateFormatString("MM-dd-yyyy");
        dateChooser.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        dateChooser.getJCalendar().setPreferredSize(new Dimension(260, 260));
        abGbc.gridx = 2;
        abGbc.weightx = 0.6;
        ageBirthdayPanel.add(dateChooser, abGbc);
        studentFields.put("birthday", dateChooser);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        studentInfoPanelContent.add(ageBirthdayPanel, gbc);
        gbc.gridwidth = 1;

        // Address field
        JTextField tfAddress = new JTextField(20);
        tfAddress.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfAddress.setMargin(new Insets(5, 5, 5, 5));
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, tfAddress, studentFields, "address", "Address:");

        // Email
        JTextField tfEmail = new JTextField(20);
        tfEmail.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfEmail.setMargin(new Insets(5, 5, 5, 5));
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, tfEmail, studentFields, "email", "Email:");

        // Phone number
        JTextField tfContact = new JTextField(20);
        tfContact.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfContact.setMargin(new Insets(5, 5, 5, 5));
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, tfContact, studentFields, "contactNumber", "Phone Number:");

        // Marital status
        String[] maritalOptions = {"---", "Single", "Married", "Divorced", "Widowed"};
        JComboBox<String> cbMarital = new JComboBox<>(maritalOptions);
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, cbMarital, studentFields, "maritalStatus", "Marital Status");

        // Citizenship
        String[] citizenshipOptions = {"---", "Filipino", "American", "Chinese", "Japanese", "Korean", "Other"};
        JComboBox<String> cbCitizenship = new JComboBox<>(citizenshipOptions);
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanelContent, gbc, cbCitizenship, studentFields, "citizenship", "Citizenship");

        // Citizenship - others
        JTextField tfOtherCitizen = new JTextField(20);
        tfOtherCitizen.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfOtherCitizen.setMargin(new Insets(5, 5, 5, 5));
        tfOtherCitizen.setVisible(false);
        gbc.gridx = 1;
        gbc.gridy = row++;
        studentInfoPanelContent.add(tfOtherCitizen, gbc);
        studentFields.put("citizenshipOther", tfOtherCitizen);

        cbCitizenship.addActionListener(e -> {
            boolean isOther = "Other".equals(cbCitizenship.getSelectedItem());
            tfOtherCitizen.setVisible(isOther);
            studentInfoPanelContent.revalidate(); // Revalidate the content panel
            studentInfoPanelContent.repaint();
        });

        // Create the scrollable pane for the left panel content
        JScrollPane studentInfoScrollPane = new JScrollPane(studentInfoPanelContent);
        studentInfoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        studentInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        studentInfoScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Optional border
        // Ensure the scroll pane respects the preferred size set for the center panel
        studentInfoScrollPane.setPreferredSize(new Dimension(400, 600)); // Or desired initial size
        studentInfoScrollPane.setMaximumSize(new Dimension(400, Integer.MAX_VALUE)); // Limit width


        // Right panel - parent's info and requirements (Unchanged)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(400, 600));
        rightPanel.setBackground(new Color(238, 238, 238));

        // Parent's info panel
        JPanel parentInfoPanel = new JPanel(new GridBagLayout());
        parentInfoPanel.setBackground(Color.white);
        parentInfoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints pg = new GridBagConstraints();
        pg.insets = new Insets(8, 8, 8, 8);
        pg.fill = GridBagConstraints.HORIZONTAL;
        pg.gridx = 0;
        pg.gridy = 0;
        pg.gridwidth = 2;
        JLabel parentInfoHeader = new JLabel("Parent's Information");
        parentInfoHeader.setFont(new Font("Arial", Font.BOLD, 22));
        parentInfoHeader.setForeground(new Color(46, 109, 27));
        parentInfoPanel.add(parentInfoHeader, pg);
        pg.gridwidth = 1;
        pg.gridy++;
        Map<String, JComponent> parentFields = new HashMap<>();

        // Mother's Maiden Name
        JTextField tfMotherMaiden = new JTextField(20);
        tfMotherMaiden.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfMotherMaiden.setMargin(new Insets(5, 5, 5, 5));
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfMotherMaiden, parentFields, "motherMaiden", "Mother's Maiden Name:");
        pg.gridy++;

        // Mother's Contact & Email
        JPanel motherContactPanel = new JPanel(new GridLayout(1, 2, 10, 5));
        motherContactPanel.setBackground(Color.WHITE);
        JTextField tfMotherContact = new JTextField();
        tfMotherContact.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfMotherContact.setMargin(new Insets(5, 5, 5, 5));
        motherContactPanel.add(wrapWithCaption(tfMotherContact, "Contact Number"));
        parentFields.put("motherContact", tfMotherContact);
        JTextField tfMotherEmail = new JTextField();
        tfMotherEmail.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfMotherEmail.setMargin(new Insets(5, 5, 5, 5));
        motherContactPanel.add(wrapWithCaption(tfMotherEmail, "Email"));
        parentFields.put("motherEmail", tfMotherEmail);
        pg.gridx = 0;
        pg.gridwidth = 2;
        parentInfoPanel.add(motherContactPanel, pg);
        pg.gridwidth = 1;
        pg.gridy++;

        // Father's Name
        JTextField tfFatherName = new JTextField(20);
        tfFatherName.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfFatherName.setMargin(new Insets(5, 5, 5, 5));
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfFatherName, parentFields, "fatherName", "Father's Name:");
        pg.gridy++;

        // Father's Contact & Email
        JPanel fatherContactPanel = new JPanel(new GridLayout(1, 2, 10, 5));
        fatherContactPanel.setBackground(Color.WHITE);
        JTextField tfFatherContact = new JTextField();
        tfFatherContact.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfFatherContact.setMargin(new Insets(5, 5, 5, 5));
        fatherContactPanel.add(wrapWithCaption(tfFatherContact, "Contact Number"));
        parentFields.put("fatherContact", tfFatherContact);
        JTextField tfFatherEmail = new JTextField();
        tfFatherEmail.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfFatherEmail.setMargin(new Insets(5, 5, 5, 5));
        fatherContactPanel.add(wrapWithCaption(tfFatherEmail, "Email"));
        parentFields.put("fatherEmail", tfFatherEmail);
        pg.gridx = 0;
        pg.gridwidth = 2;
        parentInfoPanel.add(fatherContactPanel, pg);
        pg.gridwidth = 1;
        pg.gridy++;

        // Emergency contact's name
        JTextField tfEmergencyContactName = new JTextField(20);
        tfEmergencyContactName.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfEmergencyContactName.setMargin(new Insets(5, 5, 5, 5));
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfEmergencyContactName, parentFields, "emergencyContactName", "Emergency Contact's Name:");
        pg.gridy++;

        // Relation of emergency contact
        String[] relationOptions = {"---", "Mother", "Father", "Sister", "Brother", "Aunt", "Uncle", "Grandmother", "Grandfather"};
        JComboBox<String> cbEmergencyContactRelation = new JComboBox<>(relationOptions);
        addLabeledFieldFullWidth(parentInfoPanel, pg, cbEmergencyContactRelation, parentFields, "emergencyContactRelation", "Relation to Student:");
        pg.gridy++;

        // Emergency contact number
        JTextField tfEmergencyContact = new JTextField(20);
        tfEmergencyContact.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfEmergencyContact.setMargin(new Insets(5, 5, 5, 5));
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfEmergencyContact, parentFields, "emergencyContact", "Emergency Contact Number:");
        rightPanel.add(parentInfoPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Requirements panel
        JPanel requirementsPanel = new JPanel();
        requirementsPanel.setLayout(new BoxLayout(requirementsPanel, BoxLayout.Y_AXIS));
        requirementsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        requirementsPanel.setBackground(Color.WHITE);
        JLabel requirementsHeader = new JLabel("Requirements");
        requirementsHeader.setFont(new Font("Arial", Font.BOLD, 22));
        requirementsHeader.setForeground(new Color(46, 109, 27));
        requirementsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        requirementsPanel.add(requirementsHeader);
        requirementsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] birthCertPath = {""};
        String[] form137Path = {""};
        String[] form138Path = {""};
        String[] picturePath = {""};

        JButton btnBirthCert = new JButton("Upload PSA/Birth Certificate");
        JLabel lblBirthCert = new JLabel("No file selected");
        customizeRequirementButtonAndLabel(btnBirthCert, lblBirthCert);
        requirementsPanel.add(btnBirthCert);
        requirementsPanel.add(lblBirthCert);
        requirementsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton btnForm137 = new JButton("Upload Form 137");
        JLabel lblForm137 = new JLabel("No file selected");
        customizeRequirementButtonAndLabel(btnForm137, lblForm137);
        requirementsPanel.add(btnForm137);
        requirementsPanel.add(lblForm137);
        requirementsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton btnForm138 = new JButton("Upload Form 138 (Previous Grade Report)");
        JLabel lblForm138 = new JLabel("No file selected");
        customizeRequirementButtonAndLabel(btnForm138, lblForm138);
        btnForm138.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType));
        lblForm138.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType));
        requirementsPanel.add(btnForm138);
        requirementsPanel.add(lblForm138);
        requirementsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton btnPicture = new JButton("Upload 2x2 Picture");
        JLabel lblPicture = new JLabel("No file selected");
        customizeRequirementButtonAndLabel(btnPicture, lblPicture);
        btnPicture.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType));
        lblPicture.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType));
        requirementsPanel.add(btnPicture);
        requirementsPanel.add(lblPicture);

        JScrollPane requirementsScrollPane = new JScrollPane(requirementsPanel);
        requirementsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        requirementsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        requirementsScrollPane.setPreferredSize(new Dimension(400, 200));
        rightPanel.add(requirementsScrollPane);

        // Center panel layout (Now uses the scrollable left panel)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.insets = new Insets(5, 5, 5, 5);
        cGbc.fill = GridBagConstraints.BOTH;
        cGbc.gridx = 0;
        cGbc.gridy = 0;
        cGbc.weightx = 0.6; // Adjust weight as needed
        cGbc.weighty = 1;
        // Add the scrollable pane instead of the plain panel
        centerPanel.add(studentInfoScrollPane, cGbc);
        cGbc.gridx = 1;
        cGbc.weightx = 0.4; // Adjust weight as needed
        centerPanel.add(rightPanel, cGbc);

        enrollmentMainPanel.add(centerPanel, BorderLayout.CENTER);

        // Back to menu and enroll button
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonsPanel.setBackground(new Color(217, 217, 217));
        JButton backBtn = new JButton("Back to menu");
        backBtn.setPreferredSize(new Dimension(140, 40));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(Color.BLACK);
        backBtn.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27), 2));
        JButton enrollBtn = new JButton("Enroll");
        enrollBtn.setPreferredSize(new Dimension(140, 40));
        enrollBtn.setFont(new Font("Arial", Font.BOLD, 18));
        enrollBtn.setBackground(new Color(46, 109, 27));
        enrollBtn.setForeground(Color.WHITE);
        enrollBtn.setBorder(BorderFactory.createEmptyBorder());
        buttonsPanel.add(backBtn);
        buttonsPanel.add(enrollBtn);
        enrollmentMainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        addFileHandler(btnBirthCert, lblBirthCert, birthCertPath);
        addFileHandler(btnForm137, lblForm137, form137Path);
        addFileHandler(btnForm138, lblForm138, form138Path);
        addFileHandler(btnPicture, lblPicture, picturePath);

        backBtn.addActionListener(e -> card.show(EnrollmentSystem.this.mainPanel, "menu"));
        enrollBtn.addActionListener(e -> {
            try {
                if ("Old".equals(currentStudentType)) {
                    JTextField tfStudentId = tfStudentIdHolder[0];
                    if (tfStudentId == null || tfStudentId.getText().trim().isEmpty())
                        throw new Exception("Student ID No. is required.");
                    String idText = tfStudentId.getText().trim();
                    if (!idText.matches("\\d{4}-\\d{4}[A-Z]"))
                        throw new Exception("Invalid Student ID format. Format: YYYY-0000A");
                    boolean idExists = students.stream()
                            .anyMatch(s -> s.id != null && s.id.equals(idText));
                    if (!idExists) {
                        throw new Exception("Student ID not found in records. Please check and try again.");
                    }
                }
                validateAndEnrollS(studentFields, parentFields, birthCertPath[0], form137Path[0], form138Path[0],
                        picturePath[0], tfOtherCitizen.getText().trim(), currentStudentType);
                if (createdStudent != null) {
                    currentStudentForPayment = createdStudent;
                    card.show(mainPanel, "payment");
                    JPanel paymentPanelInstance = (JPanel) mainPanel.getComponent(2);
                    Runnable updateRunnable = (Runnable) paymentPanelInstance.getClientProperty("updatePaymentInfo");
                    if (updateRunnable != null) {
                        updateRunnable.run();
                    }
                } else {
                    throw new Exception("An error occurred during enrollment processing.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(enrollmentMainPanel, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return enrollmentMainPanel;
    }


    private void addLabeledFieldFullWidth(JPanel panel, GridBagConstraints gbc, JComponent field,
            Map<String, JComponent> fieldMap, String fieldKey, String labelText) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        field.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        if (field instanceof JComboBox) ((JComboBox<?>) field).setBackground(Color.WHITE);
        panel.add(field, gbc);
        if (fieldMap != null && fieldKey != null) {
            fieldMap.put(fieldKey, field);
        }
    }

    private JPanel wrapWithCaption(JTextField textField, String caption) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.white);
        panel.add(textField);
        JLabel capLabel = new JLabel("<html><i>" + caption + "</i></html>");
        capLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        capLabel.setForeground(new Color(46, 109, 27));
        panel.add(capLabel);
        return panel;
    }

    private void customizeRequirementButtonAndLabel(JButton btn, JLabel lbl) {
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 109, 27)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10) 
        ));
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));

        lbl.setFont(new Font("Arial", Font.ITALIC, 12));
        lbl.setForeground(new Color(46, 109, 27));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 10, 5, 10)); 
    }

    private void addFileHandler(JButton btn, JLabel lbl, String[] path) {
        btn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                path[0] = fc.getSelectedFile().getAbsolutePath();
                lbl.setText(fc.getSelectedFile().getName());
            }
        });
    }

    private Student createdStudent = null;

 // ... (other imports and class definition)

 // ... (other imports and class definition)

    private void validateAndEnrollS(Map<String, JComponent> studentFields, Map<String, JComponent> parentFields,
            String bcPath, String form137Path, String form138Path, String picturePath,
            String otherCitizenship, String currentStudentType) throws Exception {

        // STUDENT'S INFO GATHERING (No Validation)
        JTextField tfStudentId = (JTextField) studentFields.get("studentId");
        JTextField tfFirst = (JTextField) studentFields.get("firstName");
        JTextField tfMiddle = (JTextField) studentFields.get("middleName");
        JTextField tfLast = (JTextField) studentFields.get("lastName");
        JTextField tfAddress = (JTextField) studentFields.get("address");
        JTextField tfEmail = (JTextField) studentFields.get("email");
        JTextField tfContact = (JTextField) studentFields.get("contactNumber");
        JTextField tfAge = (JTextField) studentFields.get("age");
        com.toedter.calendar.JDateChooser tfBirthday = (com.toedter.calendar.JDateChooser) studentFields.get("birthday");

        // Gather student details (allowing empty values)
        String first = tfFirst.getText().trim();
        String middle = tfMiddle.getText().trim();
        String last = tfLast.getText().trim();
        String address = tfAddress.getText().trim();
        String email = tfEmail.getText().trim();
        String contact = tfContact.getText().trim();
        int age = 0; // Default to 0 if not entered or invalid
        try {
            age = Integer.parseInt(tfAge.getText().trim());
            if (age <= 0) age = 0; // Ensure positive age or default to 0
        } catch (NumberFormatException e) {
            // Ignore invalid age input, default to 0
        }
        java.util.Date selectedDate = tfBirthday.getDate();
        String birthday = selectedDate != null ? new java.text.SimpleDateFormat("MM-dd-yyyy").format(selectedDate) : "";

        // Marital status and Citizenship
        JComboBox<String> cbMarital = (JComboBox<String>) studentFields.get("maritalStatus");
        String maritalStatus = (String) cbMarital.getSelectedItem();
        if (maritalStatus == null || "---".equals(maritalStatus)) maritalStatus = "";

        JComboBox<String> cbCitizen = (JComboBox<String>) studentFields.get("citizenship");
        String citizen = (String) cbCitizen.getSelectedItem();
        if ("Other".equals(citizen) && otherCitizenship != null && !otherCitizenship.isEmpty()) {
            citizen = otherCitizenship;
        } else if (citizen == null || "---".equals(citizen)) {
            citizen = "";
        }

        // Year level and payment plan (These remain required and validated)
        JComboBox<String> cbYear = (JComboBox<String>) studentFields.get("yearLevel");
        if (cbYear == null || cbYear.getSelectedItem() == null || "---".equals(cbYear.getSelectedItem()))
            throw new Exception("Year Level must be selected.");
        String yearLevel = (String) cbYear.getSelectedItem();

        JComboBox<String> cbPaymentPlan = (JComboBox<String>) studentFields.get("paymentPlan");
        if (cbPaymentPlan == null || cbPaymentPlan.getSelectedItem() == null || "---".equals(cbPaymentPlan.getSelectedItem()))
            throw new Exception("Payment Plan must be selected.");
        String paymentPlan = (String) cbPaymentPlan.getSelectedItem();

        // SECTION ASSIGNMENT LOGIC (FIXED - 5 students per section)
        int section = -1;
        String[] sectionsInGrade = getSectionsForGrade(yearLevel);
        if (sectionsInGrade != null) {
            for (int i = 0; i < sectionsInGrade.length; i++) {
                int secNum = i + 1;
                if (!isSectionFull(yearLevel, secNum)) {
                    section = secNum;
                    break;
                }
            }
        }
        if (section == -1) {
            throw new Exception("All sections for " + yearLevel + " are full (5 students per section, 10 total). Enrollment blocked.");
        }

        // PARENT'S INFO GATHERING (No Validation)
        JTextField tfMotherMaiden = (JTextField) parentFields.get("motherMaiden");
        JTextField tfMotherContact = (JTextField) parentFields.get("motherContact");
        JTextField tfMotherEmail = (JTextField) parentFields.get("motherEmail");
        JTextField tfFatherName = (JTextField) parentFields.get("fatherName");
        JTextField tfFatherContact = (JTextField) parentFields.get("fatherContact");
        JTextField tfFatherEmail = (JTextField) parentFields.get("fatherEmail");
        JTextField tfEmergencyContactName = (JTextField) parentFields.get("emergencyContactName");
        JTextField tfEmergencyContact = (JTextField) parentFields.get("emergencyContact");

        String motherMaiden = tfMotherMaiden.getText().trim();
        String motherContact = tfMotherContact.getText().trim();
        String motherEmail = tfMotherEmail.getText().trim();
        String fatherName = tfFatherName.getText().trim();
        String fatherContact = tfFatherContact.getText().trim();
        String fatherEmail = tfFatherEmail.getText().trim();
        String emergencyContactName = tfEmergencyContactName.getText().trim();
        String emergencyContact = tfEmergencyContact.getText().trim();

        // Old Student ID Validation (Still Required if "Old" type)
        if ("Old".equals(currentStudentType)) {
            if (tfStudentId == null || tfStudentId.getText().trim().isEmpty())
                throw new Exception("Student ID No. is required for Old students.");
            String idText = tfStudentId.getText().trim();
            if (!idText.matches("\\d{4}-\\d{4}[A-Z]"))
                throw new Exception("Invalid Student ID format. Format: YYYY-0000A");
            boolean idExists = students.stream()
                    .anyMatch(s -> s.id != null && s.id.equals(idText));
            if (!idExists) {
                throw new Exception("Student ID not found in records. Please check and try again.");
            }
        }

        // REQUIREMENTS VALIDATION (REMOVED - No longer required)
        // The bcPath, form137Path, form138Path, and picturePath are now allowed to be empty/null.

        // ESC Grant eligibility (optional)
        boolean escEligible = false;
        if ("Grade 7".equals(yearLevel) && ("New".equals(currentStudentType) || "Transferee".equals(currentStudentType))) {
            int option = JOptionPane.showConfirmDialog(null,
                    "Are you eligible for ESC Grant (Grade 7 New/Transferee Student)?",
                    "ESC Grant", JOptionPane.YES_NO_OPTION);
            escEligible = (option == JOptionPane.YES_OPTION);
        }

        // Get section name and subjects
        String sectionName = getSectionName(yearLevel, section);
        String sectionKey = yearLevel + "-" + sectionName;
        Set<String> subjects = SECTION_SUBJECTS.getOrDefault(sectionKey, new HashSet<>());

        createdStudent = new Student(
                first.isEmpty() ? "N/A" : first,
                middle.isEmpty() ? "N/A" : middle,
                last.isEmpty() ? "N/A" : last,
                address.isEmpty() ? "N/A" : address,
                email.isEmpty() ? "N/A" : email,
                maritalStatus.isEmpty() ? "N/A" : maritalStatus,
                citizen.isEmpty() ? "N/A" : citizen,
                age,
                yearLevel, section,
                subjects,
                contact.isEmpty() ? "N/A" : contact,
                emergencyContactName.isEmpty() ? "N/A" : emergencyContactName,
                emergencyContact.isEmpty() ? "N/A" : emergencyContact,
                bcPath,
                ("Old".equals(currentStudentType) ? form137Path : (form137Path != null && !form137Path.isEmpty()) ? form137Path : form138Path),
                picturePath
        );
        createdStudent.studentType = currentStudentType;
        createdStudent.escEligible = escEligible;
        createdStudent.paymentPlan = paymentPlan;
     // At the end of validateAndEnrollS, after creating student:
        createdStudent.id = null; // Don't generate ID yet - wait for approval
        createdStudent.username = "pending_" + System.currentTimeMillis();
        createdStudent.password = "temp_" + System.currentTimeMillis();
        
        // CHANGED: Set initial status to "Pending" for admin review
        createdStudent.status = "Pending";  // Changed from "Approved"
        createdStudent.paymentStatus = "Pending Payment"; // Changed from "Paid in Full"
        
        createdStudent.computeFee();
    }
    
    
    
    
    private String[] getSectionsForGrade(String gradeLevel) {
        return SECTION_NAMES.get(gradeLevel);
    }

    private String getSectionName(String yearLevel, int sectionNumber) {
        String[] sections = SECTION_NAMES.get(yearLevel);
        if (sections != null && sectionNumber > 0 && sectionNumber <= sections.length) {
            return sections[sectionNumber - 1];
        }
        return "Section " + sectionNumber;
    }

    // FIXED: Section capacity is now 5 students per section
    private boolean isSectionFull(String year, int section) {
        final String finalYear = year;
        final int finalSection = section;
        return (int) students.stream()
            .filter(s -> finalYear.equals(s.yearLevel) && finalSection == s.section)
            .count() >= 5;
    }


    
    
    
    private JPanel paymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        
        // Header
        JLabel header = new JLabel("Payment Information", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        header.setForeground(new Color(46, 109, 27));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
        panel.add(header, BorderLayout.NORTH);
        
        // Main content with scroll
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // === STUDENT INFORMATION CARD ===
        JPanel studentInfoCard = createInfoCard();
        studentInfoCard.setMaximumSize(new Dimension(800, 150));
        studentInfoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel studentInfoTitle = new JLabel("Student Information");
        studentInfoTitle.setFont(new Font("Arial", Font.BOLD, 18));
        studentInfoTitle.setForeground(new Color(46, 109, 27));
        studentInfoTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel studentInfoContent = new JPanel(new GridLayout(3, 2, 10, 8));
        studentInfoContent.setBackground(Color.WHITE);
        studentInfoContent.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel nameVal = new JLabel();
        nameVal.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel gradeLabel = new JLabel("Grade & Section:");
        gradeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel gradeVal = new JLabel();
        gradeVal.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel planLabel = new JLabel("Payment Plan:");
        planLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel planVal = new JLabel();
        planVal.setFont(new Font("Arial", Font.BOLD, 14));
        
        studentInfoContent.add(nameLabel);
        studentInfoContent.add(nameVal);
        studentInfoContent.add(gradeLabel);
        studentInfoContent.add(gradeVal);
        studentInfoContent.add(planLabel);
        studentInfoContent.add(planVal);
        
        studentInfoCard.setLayout(new BorderLayout());
        studentInfoCard.add(studentInfoTitle, BorderLayout.NORTH);
        studentInfoCard.add(studentInfoContent, BorderLayout.CENTER);
        
        content.add(studentInfoCard);
        content.add(Box.createVerticalStrut(20));
        
        // === FEE BREAKDOWN CARD ===
        JPanel feeBreakdownCard = createInfoCard();
        feeBreakdownCard.setMaximumSize(new Dimension(800, 400));
        feeBreakdownCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel feeBreakdownTitle = new JLabel("Fee Breakdown");
        feeBreakdownTitle.setFont(new Font("Arial", Font.BOLD, 18));
        feeBreakdownTitle.setForeground(new Color(46, 109, 27));
        feeBreakdownTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel feeDetails = new JPanel();
        feeDetails.setLayout(new BoxLayout(feeDetails, BoxLayout.Y_AXIS));
        feeDetails.setBackground(Color.WHITE);
        feeDetails.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane feeDetailsScroll = new JScrollPane(feeDetails);
        feeDetailsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        feeDetailsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        feeDetailsScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        feeDetailsScroll.setPreferredSize(new Dimension(750, 300));
        
        feeBreakdownCard.setLayout(new BorderLayout());
        feeBreakdownCard.add(feeBreakdownTitle, BorderLayout.NORTH);
        feeBreakdownCard.add(feeDetailsScroll, BorderLayout.CENTER);
        
        content.add(feeBreakdownCard);
        content.add(Box.createVerticalStrut(20));
        
        // === TOTAL AMOUNT DUE ===
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setMaximumSize(new Dimension(800, 60));
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel totalFeeLabel = new JLabel("Total Amount Due (Downpayment): ");
        totalFeeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalFeeLabel.setForeground(new Color(46, 109, 27));
        
        JLabel totalFeeVal = new JLabel();
        totalFeeVal.setFont(new Font("Arial", Font.BOLD, 20));
        totalFeeVal.setForeground(new Color(0, 128, 0));
        
        totalPanel.add(totalFeeLabel);
        totalPanel.add(totalFeeVal);
        
        content.add(totalPanel);
        content.add(Box.createVerticalStrut(20));
        
        // === PAYMENT METHOD CARD ===
        JPanel payMethodCard = createInfoCard();
        payMethodCard.setMaximumSize(new Dimension(800, 200));
        payMethodCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel payMethodTitle = new JLabel("Payment Method");
        payMethodTitle.setFont(new Font("Arial", Font.BOLD, 18));
        payMethodTitle.setForeground(new Color(46, 109, 27));
        payMethodTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel payMethodContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        payMethodContent.setBackground(Color.WHITE);
        
        JRadioButton gcashBtn = new JRadioButton("GCash");
        gcashBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        gcashBtn.setBackground(Color.WHITE);
        gcashBtn.setSelected(true);
        
        JRadioButton bankBtn = new JRadioButton("Bank Transfer");
        bankBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        bankBtn.setBackground(Color.WHITE);
        
        ButtonGroup payGroup = new ButtonGroup();
        payGroup.add(gcashBtn);
        payGroup.add(bankBtn);
        
        payMethodContent.add(gcashBtn);
        payMethodContent.add(bankBtn);
        
        payMethodCard.setLayout(new BorderLayout());
        payMethodCard.add(payMethodTitle, BorderLayout.NORTH);
        payMethodCard.add(payMethodContent, BorderLayout.CENTER);
        
        content.add(payMethodCard);
        content.add(Box.createVerticalStrut(15));
        
        // === GCASH DETAILS PANEL ===
        JPanel gcashDetailsPanel = createInfoCard();
        gcashDetailsPanel.setMaximumSize(new Dimension(800, 250));
        gcashDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        gcashDetailsPanel.setBackground(new Color(250, 250, 255));
        
        JLabel gcashDetailsTitle = new JLabel("GCash Payment Details");
        gcashDetailsTitle.setFont(new Font("Arial", Font.BOLD, 16));
        gcashDetailsTitle.setForeground(new Color(46, 109, 27));
        gcashDetailsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel gcashDetailsContent = new JPanel(new GridBagLayout());
        gcashDetailsContent.setBackground(new Color(250, 250, 255));
        gcashDetailsContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // GCash Name
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel gcashNameLabel = new JLabel("GCash Name:");
        gcashNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gcashDetailsContent.add(gcashNameLabel, gbc);
        
        gbc.gridx = 1;
        JLabel gcashNameVal = new JLabel("ABC High School");
        gcashNameVal.setFont(new Font("Arial", Font.BOLD, 14));
        gcashDetailsContent.add(gcashNameVal, gbc);
        
        // GCash Number
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel gcashNumberLabel = new JLabel("GCash Number:");
        gcashNumberLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gcashDetailsContent.add(gcashNumberLabel, gbc);
        
        gbc.gridx = 1;
        JLabel gcashNumberVal = new JLabel("0917-123-4567");
        gcashNumberVal.setFont(new Font("Arial", Font.BOLD, 14));
        gcashDetailsContent.add(gcashNumberVal, gbc);
        
        // Reference Number
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel gcashRefLabel = new JLabel("Reference Number:");
        gcashRefLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gcashDetailsContent.add(gcashRefLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField gcashRefField = new JTextField(20);
        gcashRefField.setFont(new Font("Arial", Font.PLAIN, 14));
        gcashRefField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 109, 27), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gcashDetailsContent.add(gcashRefField, gbc);
        
        // Date/Time
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel gcashDateLabel = new JLabel("Transaction Date/Time:");
        gcashDateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gcashDetailsContent.add(gcashDateLabel, gbc);
        
        gbc.gridx = 1;
        JTextField gcashDateField = new JTextField(20);
        gcashDateField.setFont(new Font("Arial", Font.PLAIN, 14));
        gcashDateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 109, 27), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gcashDateField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));
        gcashDetailsContent.add(gcashDateField, gbc);
        
        gcashDetailsPanel.setLayout(new BorderLayout());
        gcashDetailsPanel.add(gcashDetailsTitle, BorderLayout.NORTH);
        gcashDetailsPanel.add(gcashDetailsContent, BorderLayout.CENTER);
        gcashDetailsPanel.setVisible(true);
        
        content.add(gcashDetailsPanel);
        content.add(Box.createVerticalStrut(15));
        
        // === BANK TRANSFER DETAILS PANEL ===
        JPanel bankDetailsPanel = createInfoCard();
        bankDetailsPanel.setMaximumSize(new Dimension(800, 250));
        bankDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bankDetailsPanel.setBackground(new Color(250, 255, 250));
        
        JLabel bankDetailsTitle = new JLabel("Bank Transfer Details");
        bankDetailsTitle.setFont(new Font("Arial", Font.BOLD, 16));
        bankDetailsTitle.setForeground(new Color(46, 109, 27));
        bankDetailsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel bankDetailsContent = new JPanel(new GridBagLayout());
        bankDetailsContent.setBackground(new Color(250, 255, 250));
        bankDetailsContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.insets = new Insets(5, 5, 5, 15);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        
        // Bank selection
        gbc2.gridx = 0; gbc2.gridy = 0;
        JLabel bankLabel = new JLabel("Select Bank:");
        bankLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        bankDetailsContent.add(bankLabel, gbc2);
        
        gbc2.gridx = 1; gbc2.weightx = 1.0;
        String[] banks = {"PNB", "BDO", "Metrobank", "BPI", "Landbank", "Union Bank"};
        JComboBox<String> bankCombo = new JComboBox<>(banks);
        bankCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        bankDetailsContent.add(bankCombo, gbc2);
        
        // Account Name
        gbc2.gridx = 0; gbc2.gridy = 1; gbc2.weightx = 0;
        JLabel accountNameLabel = new JLabel("Account Name:");
        accountNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        bankDetailsContent.add(accountNameLabel, gbc2);
        
        gbc2.gridx = 1;
        JLabel accountNameVal = new JLabel();
        accountNameVal.setFont(new Font("Arial", Font.BOLD, 14));
        bankDetailsContent.add(accountNameVal, gbc2);
        
        // Account Number
        gbc2.gridx = 0; gbc2.gridy = 2;
        JLabel accountNumberLabel = new JLabel("Account Number:");
        accountNumberLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        bankDetailsContent.add(accountNumberLabel, gbc2);
        
        gbc2.gridx = 1;
        JLabel accountNumberVal = new JLabel();
        accountNumberVal.setFont(new Font("Arial", Font.BOLD, 14));
        bankDetailsContent.add(accountNumberVal, gbc2);
        
        // Reference Number
        gbc2.gridx = 0; gbc2.gridy = 3;
        JLabel referenceLabel = new JLabel("Reference Number:");
        referenceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        bankDetailsContent.add(referenceLabel, gbc2);
        
        gbc2.gridx = 1;
        JTextField referenceField = new JTextField(20);
        referenceField.setFont(new Font("Arial", Font.PLAIN, 14));
        referenceField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 109, 27), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        bankDetailsContent.add(referenceField, gbc2);
        
        bankDetailsPanel.setLayout(new BorderLayout());
        bankDetailsPanel.add(bankDetailsTitle, BorderLayout.NORTH);
        bankDetailsPanel.add(bankDetailsContent, BorderLayout.CENTER);
        bankDetailsPanel.setVisible(false);
        
        content.add(bankDetailsPanel);
        
        // Action Listeners for Payment Method
        gcashBtn.addActionListener(e -> {
            gcashDetailsPanel.setVisible(true);
            bankDetailsPanel.setVisible(false);
            content.revalidate();
            content.repaint();
        });
        
        bankBtn.addActionListener(e -> {
            gcashDetailsPanel.setVisible(false);
            bankDetailsPanel.setVisible(true);
            content.revalidate();
            content.repaint();
        });
        
        // Bank selection listener
        bankCombo.addActionListener(e -> {
            String selectedBank = (String) bankCombo.getSelectedItem();
            if (selectedBank != null) {
                String[] details = BANK_ACCOUNTS.get(selectedBank);
                if (details != null) {
                    accountNameVal.setText(details[0]);
                    accountNumberVal.setText(details[1]);
                } else {
                    accountNameVal.setText("Details not available");
                    accountNumberVal.setText("N/A");
                }
            }
        });
        
        // Initialize bank details
        String initialBank = (String) bankCombo.getSelectedItem();
        if (initialBank != null) {
            String[] details = BANK_ACCOUNTS.get(initialBank);
            if (details != null) {
                accountNameVal.setText(details[0]);
                accountNumberVal.setText(details[1]);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // === BUTTON PANEL ===
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        btnPanel.setBackground(new Color(240, 240, 240));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)));
        
        JButton back = new JButton("Back to Enrollment");
        styleModernButton(back, false);
        
        JButton confirm = new JButton("Confirm Payment");
        styleModernButton(confirm, true);
        
        btnPanel.add(back);
        btnPanel.add(confirm);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // FIXED: Back button action
        back.addActionListener(e -> {
            card.show(mainPanel, "enroll");
        });
        
        // Confirm Button Action
        confirm.addActionListener(e -> {
            if (currentStudentForPayment != null) {
                if (gcashBtn.isSelected()) {
                    // GCash Payment
                    String gcashRef = gcashRefField.getText().trim();
                    if (gcashRef.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please enter a GCash reference number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    currentStudentForPayment.paymentMethod = "GCash";
                    currentStudentForPayment.bankReferenceNumber = gcashRef;
                    currentStudentForPayment.status = "Pending";
                    currentStudentForPayment.paymentStatus = "Payment Submitted";
                    
                    students.add(currentStudentForPayment);
                    saveData();
                    
                    JOptionPane.showMessageDialog(this,
                        "Enrollment application submitted successfully!\n\n" +
                        "Your application is now under review by the admin.\n" +
                        "Username: " + currentStudentForPayment.username + "\n" +
                        "Password: " + currentStudentForPayment.password + "\n\n" +
                        "Use these credentials to check your enrollment status.",
                        "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
                    currentStudentForPayment = null;
                    card.show(mainPanel, "menu");
                    
                } else if (bankBtn.isSelected()) {
                    // Bank Transfer
                    String refNumber = referenceField.getText().trim();
                    if (refNumber.length() != 13) {
                        JOptionPane.showMessageDialog(this, "Please enter a valid 13-character reference number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    currentStudentForPayment.paymentMethod = "Bank Transfer";
                    currentStudentForPayment.bankReferenceNumber = refNumber;
                    currentStudentForPayment.status = "Pending";
                    currentStudentForPayment.paymentStatus = "Payment Submitted";
                    
                    students.add(currentStudentForPayment);
                    saveData();
                    
                    JOptionPane.showMessageDialog(this,
                        "Enrollment application submitted successfully!\n\n" +
                        "Your application is now under review by the admin.\n" +
                        "Reference Number: " + refNumber + "\n" +
                        "Username: " + String.format("%-23s", currentStudentForPayment.username) + "│\n" +
                        "Password: " + String.format("%-23s", currentStudentForPayment.password) + "│\n" +
                        "Use these credentials to check your enrollment status.",
                        "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
                    currentStudentForPayment = null;
                    card.show(mainPanel, "menu");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No student data found for payment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // CRITICAL: Runnable to update the payment info panel when it's shown
        panel.putClientProperty("updatePaymentInfo", (Runnable) () -> {
            if (currentStudentForPayment != null) {
                nameVal.setText(currentStudentForPayment.getFullName());
                String sectionName = getSectionName(currentStudentForPayment.yearLevel, currentStudentForPayment.section);
                gradeVal.setText(currentStudentForPayment.yearLevel + " - " + sectionName);
                planVal.setText(currentStudentForPayment.paymentPlan != null ? currentStudentForPayment.paymentPlan : "Not specified");
                
                // Update Fee Breakdown
                feeDetails.removeAll();
                String gradeLevel = currentStudentForPayment.yearLevel;
                double tuitionFee = 0.0;
                double miscFee = 0.0;
                
                switch (gradeLevel) {
                    case "Grade 7":
                        addFeeLabel(feeDetails, "GRADE 7 TUITION BREAKDOWN:", true);
                        addFeeLabel(feeDetails, "Tuition (Academic Subjects) — ₱18,000", false);
                        tuitionFee = 18000.0;
                        addFeeLabel(feeDetails, " ", false);
                        addFeeLabel(feeDetails, "Miscellaneous Fees:", true);
                        addFeeLabel(feeDetails, "  Registration — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Library Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Athletics & Activities — ₱1,000", false);
                        addFeeLabel(feeDetails, "  Computer/Tech Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Medical & Dental — ₱1,000", false);
                        addFeeLabel(feeDetails, "  Campus Maintenance — ₱2,000", false);
                        addFeeLabel(feeDetails, "  Books & Modules — ₱2,500", false);
                        miscFee = 11000.0;
                        addFeeLabel(feeDetails, "TOTAL MISCELLANEOUS: ₱" + String.format("%.0f", miscFee), true);
                        break;
                    case "Grade 8":
                        addFeeLabel(feeDetails, "GRADE 8 TUITION BREAKDOWN:", true);
                        addFeeLabel(feeDetails, "Tuition (Academic Subjects) — ₱17,500", false);
                        tuitionFee = 17500.0;
                        addFeeLabel(feeDetails, " ", false);
                        addFeeLabel(feeDetails, "Miscellaneous Fees:", true);
                        addFeeLabel(feeDetails, "  Registration — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Library Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Athletics & Activities — ₱1,200", false);
                        addFeeLabel(feeDetails, "  Computer/Tech Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Medical & Dental — ₱1,000", false);
                        addFeeLabel(feeDetails, "  Campus Maintenance — ₱2,000", false);
                        addFeeLabel(feeDetails, "  Books & Modules — ₱2,800", false);
                        miscFee = 11500.0;
                        addFeeLabel(feeDetails, "TOTAL MISCELLANEOUS: ₱" + String.format("%.0f", miscFee), true);
                        break;
                    case "Grade 9":
                        addFeeLabel(feeDetails, "GRADE 9 TUITION BREAKDOWN:", true);
                        addFeeLabel(feeDetails, "Tuition (Academic Subjects + Additional Science Load) — ₱17,000", false);
                        tuitionFee = 17000.0;
                        addFeeLabel(feeDetails, " ", false);
                        addFeeLabel(feeDetails, "Miscellaneous Fees:", true);
                        addFeeLabel(feeDetails, "  Registration — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Library Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Athletics & Activities — ₱1,200", false);
                        addFeeLabel(feeDetails, "  Computer/Tech Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Medical & Dental — ₱1,000", false);
                        addFeeLabel(feeDetails, "  Science Laboratory Fee — ₱2,500", false);
                        addFeeLabel(feeDetails, "  Campus Maintenance — ₱2,000", false);
                        addFeeLabel(feeDetails, "  Books & Modules — ₱1,800", false);
                        miscFee = 13000.0;
                        addFeeLabel(feeDetails, "TOTAL MISCELLANEOUS: ₱" + String.format("%.0f", miscFee), true);
                        break;
                    case "Grade 10":
                        addFeeLabel(feeDetails, "GRADE 10 TUITION BREAKDOWN:", true);
                        addFeeLabel(feeDetails, "Tuition (Academic Subjects + Research/Completers Load) — ₱16,500", false);
                        tuitionFee = 16500.0;
                        addFeeLabel(feeDetails, " ", false);
                        addFeeLabel(feeDetails, "Miscellaneous Fees:", true);
                        addFeeLabel(feeDetails, "  Registration — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Library Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Athletics & Activities — ₱1,200", false);
                        addFeeLabel(feeDetails, "  Computer/Tech Fee — ₱1,500", false);
                        addFeeLabel(feeDetails, "  Medical & Dental — ₱1,000", false);
                        addFeeLabel(feeDetails, "  Science Laboratory Fee — ₱3,000", false);
                        addFeeLabel(feeDetails, "  Completion / Testing Fee — ₱2,000", false);
                        addFeeLabel(feeDetails, "  Campus Maintenance — ₱2,000", false);
                        addFeeLabel(feeDetails, "  Books & Modules — ₱1,800", false);
                        miscFee = 14500.0;
                        addFeeLabel(feeDetails, "TOTAL MISCELLANEOUS: ₱" + String.format("%.0f", miscFee), true);
                        break;
                    default:
                        addFeeLabel(feeDetails, "Fees for " + gradeLevel + " not specified.", false);
                }
                
                double totalTuition = tuitionFee + miscFee;
                addFeeLabel(feeDetails, "TUITION + MISC TOTAL: ₱" + String.format("%.0f", totalTuition), true);
                
                String plan = currentStudentForPayment.paymentPlan;
                double amountDue;
                if ("Full".equals(plan)) {
                    amountDue = totalTuition;
                } else {
                    amountDue = 3500.0;
                }
                totalFeeVal.setText(String.format("₱%.2f", amountDue));
                
                feeDetails.revalidate();
                feeDetails.repaint();
            }
        });
        
        return panel;
    }

    // Helper method to create info cards with modern styling
    private JPanel createInfoCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return card;
    }

    // Helper method to add fee labels with styling
    private void addFeeLabel(JPanel panel, String text, boolean isBold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, 13));
        label.setForeground(isBold ? new Color(46, 109, 27) : Color.BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        panel.add(label);
    }

    // Helper method to style modern buttons
    private void styleModernButton(JButton button, boolean isPrimary) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        if (isPrimary) {
            button.setBackground(new Color(46, 109, 27));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder());
            
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(35, 85, 20));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(46, 109, 27));
                }
            });
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(46, 109, 27));
            button.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27), 2));
            
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(240, 240, 240));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(Color.WHITE);
                }
            });
        }
    }

       

    private void showStatusChecker() {
        // Clear the input fields when showing the panel
        JPanel statusPanel = (JPanel) mainPanel.getComponent(getComponentIndex("statusChecker"));
        if (statusPanel != null) {
            clearStatusCheckerFields(statusPanel);
        }
        card.show(mainPanel, "statusChecker");
    }

    private void clearStatusCheckerFields(JPanel statusPanel) {
		// TODO Auto-generated method stub
		
	}

	// Add this helper method to find component index
    private int getComponentIndex(String name) {
        Component[] components = mainPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JPanel) {
                Object property = ((JPanel) components[i]).getClientProperty("panelName");
                if (name.equals(property)) {
                    return i;
                }
            }
        }
        return -1;
    }

    // Add this new method to create the status checker panel
    private JPanel statusCheckerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.putClientProperty("panelName", "statusChecker");
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 109, 27));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel headerLabel = new JLabel("Check Enrollment Status");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        
        JLabel subHeaderLabel = new JLabel("Enter your credentials to view your enrollment information");
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeaderLabel.setForeground(new Color(220, 220, 220));
        
        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.setBackground(new Color(46, 109, 27));
        headerTextPanel.add(headerLabel);
        headerTextPanel.add(Box.createVerticalStrut(5));
        headerTextPanel.add(subHeaderLabel);
        
        headerPanel.add(headerTextPanel, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Main Content
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Login Card
        JPanel loginCard = new JPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        loginCard.setPreferredSize(new Dimension(500, 400));
        loginCard.setMaximumSize(new Dimension(500, 400));
        
        JLabel loginTitle = new JLabel("ENROLLMENT STATUS");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loginTitle.setForeground(new Color(46, 109, 27));
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginCard.add(loginTitle);
        loginCard.add(Box.createVerticalStrut(30));
        
        // Username field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(80, 80, 80));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setPreferredSize(new Dimension(400, 45));
        usernameField.setMaximumSize(new Dimension(400, 45));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginCard.add(userLabel);
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(usernameField);
        loginCard.add(Box.createVerticalStrut(20));
        
        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(new Color(80, 80, 80));
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(400, 45));
        passwordField.setMaximumSize(new Dimension(400, 45));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginCard.add(passLabel);
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(passwordField);
        loginCard.add(Box.createVerticalStrut(25));
        
        // Error message label (hidden by default)
        JLabel errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        errorLabel.setForeground(new Color(200, 0, 0));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setVisible(false);
        
        loginCard.add(errorLabel);
        loginCard.add(Box.createVerticalStrut(5));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton backBtn = new JButton("Back to Menu");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        backBtn.setPreferredSize(new Dimension(190, 45));
        backBtn.setMinimumSize(new Dimension(190, 45));
        backBtn.setMaximumSize(new Dimension(190, 45));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(46, 109, 27));
        backBtn.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27), 2));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backBtn.setBackground(new Color(240, 240, 240));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backBtn.setBackground(Color.WHITE);
            }
        });
        
        JButton checkStatusBtn = new JButton("Check Status");
        checkStatusBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        checkStatusBtn.setPreferredSize(new Dimension(190, 45));
        checkStatusBtn.setMinimumSize(new Dimension(190, 45));
        checkStatusBtn.setMaximumSize(new Dimension(190, 45));
        checkStatusBtn.setBackground(new Color(46, 109, 27));
        checkStatusBtn.setForeground(Color.WHITE);
        checkStatusBtn.setBorder(BorderFactory.createEmptyBorder());
        checkStatusBtn.setFocusPainted(false);
        checkStatusBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        checkStatusBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                checkStatusBtn.setBackground(new Color(35, 85, 20));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                checkStatusBtn.setBackground(new Color(46, 109, 27));
            }
        });
        
        buttonsPanel.add(backBtn);
        buttonsPanel.add(Box.createHorizontalStrut(15));
        buttonsPanel.add(checkStatusBtn);
        
        // Add buttons panel to card with proper alignment
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setLayout(new BoxLayout(buttonWrapper, BoxLayout.X_AXIS));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonWrapper.setMaximumSize(new Dimension(400, 45));
        buttonWrapper.add(buttonsPanel);
        
        loginCard.add(buttonWrapper);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(50, 0, 50, 0);
        mainContent.add(loginCard, gbc);
        
        panel.add(mainContent, BorderLayout.CENTER);
        
        // Button Actions
        checkStatusBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("⚠ Please enter both username and password");
                errorLabel.setVisible(true);
                return;
            }
            
            errorLabel.setVisible(false);
            
            Student s = students.stream()
                .filter(st -> username.equals(st.username) && password.equals(st.password))
                .findFirst()
                .orElse(null);
            
            if (s != null) {
                showStatusResultPanel(s);
            } else {
                errorLabel.setText("✗ Invalid username or password");
                errorLabel.setVisible(true);
                passwordField.setText("");
            }
        });
        
        backBtn.addActionListener(e -> card.show(mainPanel, "menu"));
        
        // Enter key support
        passwordField.addActionListener(e -> checkStatusBtn.doClick());
        usernameField.addActionListener(e -> passwordField.requestFocus());
        
        return panel;
    }

    // Add this method to show the status result
    private void showStatusResultPanel(Student student) {
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(new Color(240, 240, 240));
        resultPanel.putClientProperty("panelName", "statusResult");
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(46, 109, 27));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel headerLabel = new JLabel("Enrollment Status");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        headerLabel.setForeground(Color.WHITE);
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        resultPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Main Content
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPanel statusCard = new JPanel();
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));
        statusCard.setBackground(Color.WHITE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(60, 50, 60, 50)
        ));
        statusCard.setPreferredSize(new Dimension(700, 400));
        statusCard.setMaximumSize(new Dimension(700, 400));
        
        // Determine status message and color
        String statusMessage = "";
        String statusDescription = "";
        Color statusColor = Color.BLACK;
        
        if ("Approved".equals(student.status)) {
            statusMessage = "You are officially enrolled!";
            statusDescription = "Your enrollment application has been approved. You will receive further instructions via email.";
            statusColor = new Color(0, 150, 0);
        } else if ("Pending".equals(student.status)) {
            statusMessage = "Application Under Review";
            statusDescription = "Your enrollment application and payment information are currently being reviewed by our admissions team. You will be notified once your application is processed. Please check back within 1-3 business days.";
            statusColor = new Color(200, 140, 0);
        } else if ("Declined".equals(student.status)) {
            statusMessage = "Application not accepted";
            statusDescription = "Unfortunately, your enrollment application was not approved at this time. Please contact the registrar's office for more information.";
            statusColor = new Color(200, 0, 0);
        } else if ("For Verification".equals(student.status)) {
            statusMessage = "Verification Required";
            statusDescription = "Please report to the school registrar's office for document verification and completion of your enrollment. Bring all required documents including your birth certificate, report cards, and valid ID.";
            statusColor = new Color(255, 140, 0);
        } else {
			statusMessage = "Status Unknown";
			statusDescription = "There was an issue retrieving your enrollment status. Please contact the registrar's office for assistance.";
			statusColor = new Color(150, 0, 0);
		}
        
        // Status Message
        JLabel messageLabel = new JLabel(statusMessage);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        messageLabel.setForeground(statusColor);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusCard.add(messageLabel);
        statusCard.add(Box.createVerticalStrut(20));
        
        // Divider
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(600, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusCard.add(separator);
        statusCard.add(Box.createVerticalStrut(30));
        
        // Status Description
        JLabel descLabel = new JLabel("<html><div style='text-align: center; width: 550px;'>" + 
            statusDescription + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descLabel.setForeground(new Color(80, 80, 80));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusCard.add(descLabel);
        statusCard.add(Box.createVerticalStrut(40));
        
        // Info message
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setMaximumSize(new Dimension(600, 40));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("For any questions or concerns, please contact the registrar's office.");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(new Color(120, 120, 120));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(Box.createHorizontalGlue());
        infoPanel.add(infoLabel);
        infoPanel.add(Box.createHorizontalGlue());
        
        statusCard.add(infoPanel);
        statusCard.add(Box.createVerticalStrut(30));
        
        // Back button - FULL WIDTH
        JButton backToMenuBtn = new JButton("Back to Main Menu");
        backToMenuBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        backToMenuBtn.setPreferredSize(new Dimension(600, 45));
        backToMenuBtn.setMinimumSize(new Dimension(600, 45));
        backToMenuBtn.setMaximumSize(new Dimension(600, 45));
        backToMenuBtn.setBackground(new Color(46, 109, 27));
        backToMenuBtn.setForeground(Color.WHITE);
        backToMenuBtn.setBorder(BorderFactory.createEmptyBorder());
        backToMenuBtn.setFocusPainted(false);
        backToMenuBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToMenuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        backToMenuBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backToMenuBtn.setBackground(new Color(35, 85, 20));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backToMenuBtn.setBackground(new Color(46, 109, 27));
            }
        });
        
        backToMenuBtn.addActionListener(e -> {
            mainPanel.remove(resultPanel);
            mainPanel.revalidate();
            mainPanel.repaint();
            card.show(mainPanel, "menu");
        });
        
        statusCard.add(backToMenuBtn);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 0, 30, 0);
        mainContent.add(statusCard, gbc);
        
        resultPanel.add(mainContent, BorderLayout.CENTER);
        
        // Remove old result panel if exists
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel && "statusResult".equals(((JPanel) comp).getClientProperty("panelName"))) {
                mainPanel.remove(comp);
                break;
            }
        }
        
        mainPanel.add(resultPanel, "statusResult");
        mainPanel.revalidate();
        mainPanel.repaint();
        card.show(mainPanel, "statusResult");
    }

    private JPanel adminLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(211, 211, 211)); // Consistent with main menu
        panel.setLayout(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JLabel titleLabel = new JLabel("Admin Access");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(46, 109, 27)); // Consistent green
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.gridx = 0;
        fgbc.insets = new Insets(10, 0, 10, 0);
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        Color fieldBackground = new Color(176, 214, 154);
        Color borderColor = new Color(46, 109, 27);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(Color.BLACK);
        fgbc.gridy = 0;
        fgbc.insets = new Insets(1, 0, 0, 0);
        fieldsPanel.add(usernameLabel, fgbc);

        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(fieldBackground);
        usernameField.setForeground(Color.BLACK);
        usernameField.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        usernameField.setPreferredSize(new Dimension(400, 40));
        fgbc.gridy = 1;
        fgbc.insets = new Insets(3, 0, 0, 0);
        fieldsPanel.add(usernameField, fgbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(Color.BLACK);
        fgbc.gridy = 2;
        fgbc.insets = new Insets(1, 0, 0, 0);
        fieldsPanel.add(passwordLabel, fgbc);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBackground(fieldBackground);
        passwordField.setForeground(Color.BLACK);
        passwordField.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        passwordField.setPreferredSize(new Dimension(400, 40));
        fgbc.gridy = 3;
        fieldsPanel.add(passwordField, fgbc);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); 
        buttonsPanel.setOpaque(false); 

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.PLAIN, 20));
        loginButton.setBackground(fieldBackground);
        loginButton.setForeground(Color.BLACK);
        loginButton.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        loginButton.setPreferredSize(new Dimension(180, 50)); 

        Color defaultBackground = fieldBackground;
        Color hoverBackground = new Color(173, 207, 157);
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(hoverBackground);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(defaultBackground);
            }
        });

        JButton backToMenuButton = new JButton("Back to Menu");
        backToMenuButton.setFont(new Font("Arial", Font.PLAIN, 20));
        backToMenuButton.setBackground(fieldBackground);
        backToMenuButton.setForeground(Color.BLACK);
        backToMenuButton.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        backToMenuButton.setPreferredSize(new Dimension(180, 50)); 

        backToMenuButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backToMenuButton.setBackground(hoverBackground);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backToMenuButton.setBackground(defaultBackground);
            }
        });
        backToMenuButton.addActionListener(e -> card.show(mainPanel, "menu")); 

        buttonsPanel.add(backToMenuButton);
        buttonsPanel.add(loginButton);      

        fgbc.insets = new Insets(40, 0, 0, 0);
        fgbc.gridy = 4;
        fieldsPanel.add(buttonsPanel, fgbc); 

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(fieldsPanel, gbc);

        loginButton.addActionListener(e -> {
            if ("admin".equals(usernameField.getText()) && "1234".equals(new String(passwordField.getPassword()))) {
                card.show(mainPanel, "dashboard");
                if (adminTableModel != null) refreshStudentTable(adminTableModel);
            } else {
                JOptionPane.showMessageDialog(panel, "Invalid credentials.");
            }
        });

        return panel;
    }
    
    
    private void migrateStudentIDs() {
        int currentYear = java.time.LocalDateTime.now().getYear();
        
        // Count how many students need migration
        int needsMigration = 0;
        for (Student s : students) {
            if (s.id != null && (s.id.startsWith("S") || !s.id.matches("\\d{4}-\\d{4}[A-Z]"))) {
                needsMigration++;
            }
        }
        
        if (needsMigration == 0) {
            JOptionPane.showMessageDialog(this, 
                "All student IDs are already in the new format (YYYY-NNNNX).",
                "Migration Complete", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Found " + needsMigration + " students with old ID format.\n\n" +
            "This will assign new IDs in format: " + currentYear + "-NNNNX\n" +
            "Old IDs will be lost. Continue?",
            "Migrate Student IDs", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        StringBuilder migrationLog = new StringBuilder();
        migrationLog.append("ID MIGRATION LOG\n");
        migrationLog.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        migrationLog.append("=".repeat(60)).append("\n\n");
        
        int migrated = 0;
        for (Student s : students) {
            // Check if student has old format ID
            if (s.id != null && (s.id.startsWith("S") || !s.id.matches("\\d{4}-\\d{4}[A-Z]"))) {
                String oldID = s.id;
                
                // Generate new ID
                String newID = generateID();
                s.id = newID;
                s.username = "std_" + newID;
                s.password = "pass_" + newID.replace("-", "");
                
                migrationLog.append("Student: ").append(s.getFullName()).append("\n");
                migrationLog.append("  Old ID: ").append(oldID).append("\n");
                migrationLog.append("  New ID: ").append(newID).append("\n");
                migrationLog.append("  Username: ").append(s.username).append("\n");
                migrationLog.append("  Password: ").append(s.password).append("\n\n");
                
                migrated++;
            }
        }
        
        // Save changes
        saveData();
        saveCounters();
        
        // Save migration log
        try (PrintWriter writer = new PrintWriter(new FileWriter("id_migration_log.txt"))) {
            writer.print(migrationLog.toString());
        } catch (IOException e) {
            System.err.println("Could not save migration log: " + e.getMessage());
        }
        
        // Refresh all tables
        refreshStudentTable(adminTableModel);
        refreshStudentInfoTable();
        refreshClassSectionTable();
        
        JOptionPane.showMessageDialog(this,
            "Migration Complete!\n\n" +
            "Migrated: " + migrated + " students\n" +
            "Log saved to: id_migration_log.txt\n\n" +
            "All tables have been refreshed.",
            "Migration Success", JOptionPane.INFORMATION_MESSAGE);
        
        System.out.println("ID Migration completed: " + migrated + " students");
    }
    
    
    

    private JPanel adminDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(211, 211, 211)); // Consistent background
        
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(46, 109, 27)); // Consistent green
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 14));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Admission", buildAdmissionPanel());
        tabs.addTab("Student Info", buildStudentInformationPanel());
        tabs.addTab("Class & Section", buildClassSectionPanel());
        tabs.addTab("Reports", buildReportsPanel());
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAdmissionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JPanel metrics = new JPanel(new GridLayout(1, 3, 10, 10));
        metrics.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        metrics.setBackground(new Color(211, 211, 211));
        
        metrics.add(wrapMetric(new JLabel("Total Registered: " + students.size())));
        metrics.add(wrapMetric(new JLabel("Pending: " + countByStatus("Pending"))));
        metrics.add(wrapMetric(new JLabel("Approved: " + countByStatus("Approved"))));
        panel.add(metrics, BorderLayout.NORTH);

        String[] cols = {"Name", "Email", "Year Level", "Section"};

        adminTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        adminTable = new JTable(adminTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(46, 109, 27));
                    c.setForeground(Color.WHITE);
                } else if (row < students.size()) {
                    Student s = students.get(row);
                    c.setForeground(Color.BLACK);
                    if ("Hold".equals(s.status)) {
                        c.setBackground(new Color(255, 255, 0, 100)); // Yellow
                    } else if ("Approved".equals(s.status)) {
                        c.setBackground(new Color(0, 255, 0, 100)); // Green
                    } else if ("For Verification".equals(s.status)) {
                        c.setBackground(new Color(255, 165, 0, 100)); // Orange - ADDED
                    } else if ("Pending".equals(s.status)) {
                        c.setBackground(new Color(200, 200, 255, 100)); // Light blue - ADDED
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        adminTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedStudentIndex = adminTable.getSelectedRow();
                updateRightPanel();
            }
        });

        JScrollPane tableScroll = new JScrollPane(adminTable);
        tableScroll.setPreferredSize(new Dimension(500, 400));
        JPanel main = new JPanel(new BorderLayout());
        main.add(tableScroll, BorderLayout.WEST);

        rightDetailPanel = new JPanel(new BorderLayout());
        rightDetailPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        rightDetailPanel.setPreferredSize(new Dimension(350, 400));
        updateRightPanel(); 
        main.add(rightDetailPanel, BorderLayout.CENTER);
        panel.add(main, BorderLayout.CENTER);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                refreshStudentTable(adminTableModel);
                updateRightPanel();
            }
        });

        JPanel btns = new JPanel();
        btns.setBackground(new Color(211, 211, 211));

        JButton refresh = new JButton("Refresh");
        styleAdminButton(refresh);

        JButton migrateIDs = new JButton("Migrate IDs");  // NEW
        styleAdminButton(migrateIDs);
        migrateIDs.setBackground(new Color(255, 200, 100)); // Orange to stand out

        JButton logout = new JButton("Logout");
        styleAdminButton(logout);

        btns.add(refresh);
        btns.add(migrateIDs);  // NEW
        btns.add(logout);
        panel.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> {
            refreshStudentTable(adminTableModel);
            updateRightPanel();
        });

        // NEW: Migration button action
        migrateIDs.addActionListener(e -> {
            migrateStudentIDs();
        });

        logout.addActionListener(e -> card.show(mainPanel, "menu"));

        refreshStudentTable(adminTableModel); 
        return panel;
    }

    private void styleAdminButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(176, 176, 184));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27), 2));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));
        
        Color defaultBg = button.getBackground();
        Color hoverBg = new Color(173, 207, 157);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(defaultBg);
            }
        });
    }

    private int countByStatus(String status) {
        return (int) students.stream().filter(s -> status.equals(s.status)).count();
    }

    private JPanel buildStudentInformationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Enrolled Students", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(46, 109, 27));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Last", "First", "MI", "Grade", "Section"};

        studentInfoTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        studentInfoTable = new JTable(studentInfoTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(46, 109, 27));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        studentInfoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentInfoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedStudentInfoIndex = studentInfoTable.getSelectedRow();
                updateStudentInfoRightPanel();
            }
        });

        JScrollPane scroll = new JScrollPane(studentInfoTable);
        scroll.setPreferredSize(new Dimension(500, 400));
        JPanel main = new JPanel(new BorderLayout());
        main.add(scroll, BorderLayout.WEST);

        rightStudentInfoPanel = new JPanel(new BorderLayout());
        rightStudentInfoPanel.setBorder(BorderFactory.createTitledBorder("Student Details & Editing"));
        rightStudentInfoPanel.setPreferredSize(new Dimension(900, 400)); // Increased width
        updateStudentInfoRightPanel();
        main.add(rightStudentInfoPanel, BorderLayout.CENTER);
        panel.add(main, BorderLayout.CENTER);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                refreshStudentInfoTable();
                updateStudentInfoRightPanel();
            }
        });

        JButton refresh = new JButton("Refresh");
        styleAdminButton(refresh);
        JPanel btns = new JPanel();
        btns.setBackground(new Color(211, 211, 211));
        btns.add(refresh);
        panel.add(btns, BorderLayout.SOUTH);
        
        refresh.addActionListener(e -> {
            refreshStudentInfoTable();
            updateStudentInfoRightPanel();
        });

        refreshStudentInfoTable();
        return panel;
    }

    private JPanel buildClassSectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        List<Student> accepted = getStudentsByStatus("Approved");
        
        // Header with metrics
        JPanel header = new JPanel(new GridLayout(1, 5, 8, 8));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.setBackground(new Color(211, 211, 211));
        
        JLabel totalLabel = new JLabel("Total Accepted: " + accepted.size());
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        header.add(wrapMetric(totalLabel));
        
        for (String grade : Arrays.asList("Grade 7", "Grade 8", "Grade 9", "Grade 10")) {
            int count = (int) accepted.stream().filter(s -> grade.equals(s.yearLevel)).count();
            JLabel metricLabel = new JLabel(grade.replace("Grade ", "") + ": " + count);
            metricLabel.setFont(new Font("Arial", Font.BOLD, 14));
            header.add(wrapMetric(metricLabel));
        }
        
        panel.add(header, BorderLayout.NORTH);

        // Left panel - Year Levels & Sections with 3 columns
        String[] cols1 = {"Year Level", "Section", "Students"};
        classSectionTableModel = new DefaultTableModel(cols1, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        classSectionTable = new JTable(classSectionTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(46, 109, 27));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        classSectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classSectionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedClassStudentIndex = classSectionTable.getSelectedRow();
                if (selectedClassStudentIndex >= 0) {
                    String grade = (String) classSectionTableModel.getValueAt(selectedClassStudentIndex, 0);
                    String sectionName = (String) classSectionTableModel.getValueAt(selectedClassStudentIndex, 1);
                    refreshSectionStudentTable(grade + " - " + sectionName);
                    updateClassRightPanel();
                }
            }
        });

        JScrollPane leftScroll = new JScrollPane(classSectionTable);
        leftScroll.setPreferredSize(new Dimension(300, 400));
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(leftScroll, BorderLayout.CENTER);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Year Levels & Sections"));

        // Center panel - Students in Selected Section
        String[] cols2 = {"Student Number", "Full Name", "Status", "Payment Status"};
        sectionStudentTableModel = new DefaultTableModel(cols2, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        sectionStudentTable = new JTable(sectionStudentTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(46, 109, 27));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        sectionStudentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane midScroll = new JScrollPane(sectionStudentTable);
        midScroll.setPreferredSize(new Dimension(400, 350));
        JPanel mid = new JPanel(new BorderLayout());
        mid.add(midScroll, BorderLayout.CENTER);
        
        changeSectionBtn = new JButton("Change Section");
        styleAdminButton(changeSectionBtn);
        changeSectionBtn.setEnabled(false);
        changeSectionBtn.addActionListener(e -> changeSectionDialog());
        
        sectionStudentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedSectionStudentIndex = sectionStudentTable.getSelectedRow();
                changeSectionBtn.setEnabled(selectedSectionStudentIndex >= 0);
            }
        });
        
        mid.add(changeSectionBtn, BorderLayout.SOUTH);
        mid.setBorder(BorderFactory.createTitledBorder("Students in Selected Section"));

        // Right panel - Section Details with Schedule
        rightClassDetailPanel = new JPanel(new BorderLayout());
        rightClassDetailPanel.setBorder(BorderFactory.createTitledBorder("Section Details"));
        rightClassDetailPanel.setPreferredSize(new Dimension(600, 400));
        updateClassRightPanel();

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mid, rightClassDetailPanel);
        rightSplit.setResizeWeight(0.4);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightSplit);
        mainSplit.setResizeWeight(0.2);
        panel.add(mainSplit, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        styleAdminButton(refresh);
        JButton logout = new JButton("Logout");
        styleAdminButton(logout);
        
        JPanel btns = new JPanel();
        btns.setBackground(new Color(211, 211, 211));
        btns.add(refresh);
        btns.add(logout);
        panel.add(btns, BorderLayout.SOUTH);
        
        refresh.addActionListener(e -> {
            refreshClassSectionTable();
            if (classSectionTable.getSelectedRow() >= 0) {
                String grade = (String) classSectionTableModel.getValueAt(classSectionTable.getSelectedRow(), 0);
                String sectionName = (String) classSectionTableModel.getValueAt(classSectionTable.getSelectedRow(), 1);
                refreshSectionStudentTable(grade + " - " + sectionName);
                updateClassRightPanel();
            }
        });
        logout.addActionListener(e -> card.show(mainPanel, "menu"));

        // IMPORTANT: Call this to populate the table initially
        refreshClassSectionTable();
        
        return panel;
    }

    private JTabbedPane buildReportsPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.PLAIN, 14));
        tabs.addTab("Section List", buildSectionListReport());
        tabs.addTab("Financial", buildFinancialReport());
        tabs.addTab("Statistics", buildStatisticalReport());
        return tabs;
    }

    private JPanel buildSectionListReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Section List Report", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(46, 109, 27));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Grade Level", "Section", "Students", "Capacity", "Status"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        refreshSectionListReport(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        styleAdminButton(refresh);
        JButton export = new JButton("Export");
        styleAdminButton(export);
        
        refresh.addActionListener(e -> refreshSectionListReport(model));
        export.addActionListener(e -> exportSectionListReport(model));
        
        JPanel btns = new JPanel();
        btns.setBackground(new Color(211, 211, 211));
        btns.add(refresh);
        btns.add(export);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshSectionListReport(DefaultTableModel model) {
        model.setRowCount(0);
        for (String grade : Arrays.asList("Grade 7", "Grade 8", "Grade 9", "Grade 10")) {
            String[] sections = SECTION_NAMES.get(grade);
            if (sections != null) {
                for (int i = 0; i < sections.length; i++) {
                    int secNum = i + 1;
                    String sectionName = sections[i];
                    final String finalGrade = grade;
                    final int finalSec = secNum;
                    int count = (int) students.stream()
                        .filter(s -> finalGrade.equals(s.yearLevel) && finalSec == s.section && "Approved".equals(s.status))
                        .count();
                    String status = count >= 5 ? "FULL" : "Available";
                    model.addRow(new Object[]{grade, sectionName, count, "5", status});
                }
            }
        }
    }

    private void exportSectionListReport(DefaultTableModel model) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("section_list_report.txt"))) {
            writer.println("ACADEASE SECTION LIST REPORT");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=".repeat(80));
            writer.println();
            writer.printf("%-15s %-20s %-15s %-15s %-15s%n", 
                "Grade Level", "Section Name", "Students", "Capacity", "Status");
            writer.println("-".repeat(80));
            for (int i = 0; i < model.getRowCount(); i++) {
                writer.printf("%-15s %-20s %-15s %-15s %-15s%n",
                    model.getValueAt(i, 0),
                    model.getValueAt(i, 1),
                    model.getValueAt(i, 2),
                    model.getValueAt(i, 3),
                    model.getValueAt(i, 4));
            }
            JOptionPane.showMessageDialog(this, "Section list report exported to section_list_report.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    private JPanel buildFinancialReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("Financial Report", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(new Color(46, 109, 27));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(headerLabel, BorderLayout.NORTH);

        Map<String, Object> financialData = calculateFinancialMetrics();
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        JPanel summaryPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Payment Summary"));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.add(new JLabel("Total Students:"));
        summaryPanel.add(new JLabel(financialData.get("totalStudents").toString()));
        summaryPanel.add(new JLabel("Paid in Full:"));
        summaryPanel.add(new JLabel(financialData.get("paidInFull").toString()));
        summaryPanel.add(new JLabel("Installment Plans:"));
        summaryPanel.add(new JLabel(financialData.get("installmentPlans").toString()));
        summaryPanel.add(new JLabel("Unpaid:"));
        summaryPanel.add(new JLabel(financialData.get("unpaid").toString()));
        summaryPanel.add(new JLabel("Total Revenue (Estimated):"));
        summaryPanel.add(new JLabel("₱" + String.format("%,.2f", (Double)financialData.get("totalRevenue"))));
        contentPanel.add(summaryPanel);

        JPanel methodPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        methodPanel.setBorder(BorderFactory.createTitledBorder("Payment Methods"));
        methodPanel.setBackground(Color.WHITE);
        methodPanel.add(new JLabel("Cash Payments:"));
        methodPanel.add(new JLabel(financialData.get("cashPayments").toString()));
        methodPanel.add(new JLabel("Bank Transfers:"));
        methodPanel.add(new JLabel(financialData.get("bankTransfers").toString()));
        contentPanel.add(methodPanel);

        // Payment Plan Distribution
        JPanel planPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        planPanel.setBorder(BorderFactory.createTitledBorder("Payment Plan Distribution"));
        planPanel.setBackground(Color.WHITE);
        @SuppressWarnings("unchecked")
        Map<String, Integer> planDist = (Map<String, Integer>) financialData.get("paymentPlanDistribution");
        for (Map.Entry<String, Integer> entry : planDist.entrySet()) {
            planPanel.add(new JLabel(entry.getKey() + " Payment:"));
            planPanel.add(new JLabel(entry.getValue().toString()));
        }
        contentPanel.add(planPanel);

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(211, 211, 211));
        
        JButton refreshBtn = new JButton("Refresh");
        styleAdminButton(refreshBtn);
        JButton exportBtn = new JButton("Export");
        styleAdminButton(exportBtn);
        
        refreshBtn.addActionListener(e -> {
            panel.removeAll();
            panel.add(buildFinancialReport());
            panel.revalidate();
            panel.repaint();
        });
        exportBtn.addActionListener(e -> exportFinancialReport(financialData));
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private Map<String, Object> calculateFinancialMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        int totalStudents = students.size();
        int paidInFull = 0;
        int installmentPlans = 0;
        int unpaid = 0;
        int cashPayments = 0;
        int bankTransfers = 0;
        double totalRevenue = 0.0;
        double escTotal = 0.0;
        
        Map<String, Integer> paymentPlanDist = new HashMap<>();
        paymentPlanDist.put("Full", 0);
        paymentPlanDist.put("Semi", 0);
        paymentPlanDist.put("Quarterly", 0);
        paymentPlanDist.put("Monthly", 0);

        for (Student s : students) {
            if ("Paid in Full".equals(s.paymentStatus)) {
                paidInFull++;
                totalRevenue += s.totalAmount;
            } else if ("Installment".equals(s.paymentStatus) || "Installment Pending".equals(s.paymentStatus)) {
                installmentPlans++;
                totalRevenue += s.totalAmount;
            } else {
                unpaid++;
            }
            if ("Cash".equals(s.paymentMethod)) {
                cashPayments++;
            } else if ("Bank Transfer".equals(s.paymentMethod)) {
                bankTransfers++;
            }
            if (s.escEligible) {
                escTotal += s.escDiscount;
            }
            if (s.paymentPlan != null) {
                paymentPlanDist.put(s.paymentPlan, paymentPlanDist.getOrDefault(s.paymentPlan, 0) + 1);
            }
        }

        metrics.put("totalStudents", totalStudents);
        metrics.put("paidInFull", paidInFull);
        metrics.put("installmentPlans", installmentPlans);
        metrics.put("unpaid", unpaid);
        metrics.put("cashPayments", cashPayments);
        metrics.put("bankTransfers", bankTransfers);
        metrics.put("totalRevenue", totalRevenue);
        metrics.put("escTotal", escTotal);
        metrics.put("paymentPlanDistribution", paymentPlanDist);
        return metrics;
    }

    private void exportFinancialReport(Map<String, Object> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("financial_report.txt"))) {
            writer.println("ACADEASE FINANCIAL REPORT");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=".repeat(60));
            writer.println();
            writer.println("PAYMENT SUMMARY:");
            writer.println("Total Students: " + data.get("totalStudents"));
            writer.println("Paid in Full: " + data.get("paidInFull"));
            writer.println("Installment Plans: " + data.get("installmentPlans"));
            writer.println("Unpaid: " + data.get("unpaid"));
            writer.println("Total Revenue (Estimated): ₱" + String.format("%,.2f", (Double)data.get("totalRevenue")));
            writer.println("ESC Grants Given: ₱" + String.format("%,.2f", (Double)data.get("escTotal")));
            writer.println();
            writer.println("PAYMENT METHODS:");
            writer.println("Cash Payments: " + data.get("cashPayments"));
            writer.println("Bank Transfers: " + data.get("bankTransfers"));
            writer.println();
            writer.println("PAYMENT PLAN DISTRIBUTION:");
            @SuppressWarnings("unchecked")
            Map<String, Integer> planDist = (Map<String, Integer>) data.get("paymentPlanDistribution");
            for (Map.Entry<String, Integer> entry : planDist.entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue());
            }
            JOptionPane.showMessageDialog(this, "Financial report exported to financial_report.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    private JPanel buildStatisticalReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JLabel headerLabel = new JLabel("Statistical Report", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(new Color(46, 109, 27));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(headerLabel, BorderLayout.NORTH);

        Map<String, Object> stats = calculateStatistics();
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        JPanel enrollmentPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        enrollmentPanel.setBorder(BorderFactory.createTitledBorder("Enrollment Statistics"));
        enrollmentPanel.setBackground(Color.WHITE);
        enrollmentPanel.add(new JLabel("Total Enrolled:"));
        enrollmentPanel.add(new JLabel(stats.get("totalEnrolled").toString()));
        enrollmentPanel.add(new JLabel("Pending Applications:"));
        enrollmentPanel.add(new JLabel(stats.get("pendingApplications").toString()));
        enrollmentPanel.add(new JLabel("Approved Students:"));
        enrollmentPanel.add(new JLabel(stats.get("approvedStudents").toString()));
        enrollmentPanel.add(new JLabel("ESC Grantees:"));
        enrollmentPanel.add(new JLabel(stats.get("escGrantees").toString()));
        enrollmentPanel.add(new JLabel("Average Age:"));
        enrollmentPanel.add(new JLabel(String.format("%.1f years", (Double)stats.get("averageAge"))));
        contentPanel.add(enrollmentPanel);

        @SuppressWarnings("unchecked")
        Map<String, Integer> gradeDistribution = (Map<String, Integer>) stats.get("gradeDistribution");
        JPanel gradePanel = new JPanel(new GridLayout(0, 2, 10, 5));
        gradePanel.setBorder(BorderFactory.createTitledBorder("Grade Level Distribution"));
        gradePanel.setBackground(Color.WHITE);
        for (Map.Entry<String, Integer> entry : gradeDistribution.entrySet()) {
            gradePanel.add(new JLabel(entry.getKey() + ":"));
            gradePanel.add(new JLabel(entry.getValue().toString()));
        }
        contentPanel.add(gradePanel);

        @SuppressWarnings("unchecked")
        Map<String, Integer> maritalStatus = (Map<String, Integer>) stats.get("maritalStatus");
        JPanel demoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        demoPanel.setBorder(BorderFactory.createTitledBorder("Demographics"));
        demoPanel.setBackground(Color.WHITE);
        for (Map.Entry<String, Integer> entry : maritalStatus.entrySet()) {
            demoPanel.add(new JLabel(entry.getKey() + ":"));
            demoPanel.add(new JLabel(entry.getValue().toString()));
        }
        contentPanel.add(demoPanel);

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(211, 211, 211));
        
        JButton refreshBtn = new JButton("Refresh");
        styleAdminButton(refreshBtn);
        JButton exportBtn = new JButton("Export");
        styleAdminButton(exportBtn);
        
        refreshBtn.addActionListener(e -> {
            panel.removeAll();
            panel.add(buildStatisticalReport());
            panel.revalidate();
            panel.repaint();
        });
        exportBtn.addActionListener(e -> exportStatisticalReport(stats));
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private Map<String, Object> calculateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        int totalEnrolled = students.size();
        int pendingApplications = 0;
        int approvedStudents = 0;
        int escGrantees = 0;
        double totalAge = 0.0;

        Map<String, Integer> gradeDistribution = new HashMap<>();
        Map<String, Integer> maritalStatus = new HashMap<>();

        for (Student s : students) {
            if ("Pending".equals(s.status)) pendingApplications++;
            else if ("Approved".equals(s.status)) approvedStudents++;
            if (s.escEligible) escGrantees++;
            totalAge += s.age;
            gradeDistribution.put(s.yearLevel, gradeDistribution.getOrDefault(s.yearLevel, 0) + 1);
            maritalStatus.put(s.maritalStatus, maritalStatus.getOrDefault(s.maritalStatus, 0) + 1);
        }

        double averageAge = totalEnrolled > 0 ? totalAge / totalEnrolled : 0.0;

        stats.put("totalEnrolled", totalEnrolled);
        stats.put("pendingApplications", pendingApplications);
        stats.put("approvedStudents", approvedStudents);
        stats.put("escGrantees", escGrantees);
        stats.put("averageAge", averageAge);
        stats.put("gradeDistribution", gradeDistribution);
        stats.put("maritalStatus", maritalStatus);
        return stats;
    }

    private void exportStatisticalReport(Map<String, Object> stats) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("statistical_report.txt"))) {
            writer.println("ACADEASE STATISTICAL REPORT");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=".repeat(60));
            writer.println();
            writer.println("ENROLLMENT STATISTICS:");
            writer.println("Total Enrolled: " + stats.get("totalEnrolled"));
            writer.println("Pending Applications: " + stats.get("pendingApplications"));
            writer.println("Approved Students: " + stats.get("approvedStudents"));
            writer.println("ESC Grantees: " + stats.get("escGrantees"));
            writer.println("Average Age: " + String.format("%.1f years", (Double)stats.get("averageAge")));
            writer.println();
            writer.println("GRADE LEVEL DISTRIBUTION:");
            @SuppressWarnings("unchecked")
            Map<String, Integer> gradeDist = (Map<String, Integer>) stats.get("gradeDistribution");
            for (Map.Entry<String, Integer> entry : gradeDist.entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue());
            }
            writer.println();
            writer.println("MARITAL STATUS DISTRIBUTION:");
            @SuppressWarnings("unchecked")
            Map<String, Integer> maritalStats = (Map<String, Integer>) stats.get("maritalStatus");
            for (Map.Entry<String, Integer> entry : maritalStats.entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue());
            }
            JOptionPane.showMessageDialog(this, "Statistical report exported to statistical_report.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    private JPanel wrapMetric(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 109, 27), 2),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        c.setFont(new Font("Arial", Font.BOLD, 13));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void refreshStudentTable(DefaultTableModel model) {
        model.setRowCount(0); // Clear the table
        for (Student s : students) {
            // FIXED: Include both Pending and For Verification students
            if ("Pending".equals(s.status) || "For Verification".equals(s.status)) {
                String sectionName = getSectionName(s.yearLevel, s.section);
                model.addRow(new Object[]{
                    s.getFullName(), s.emailAddress, s.yearLevel, sectionName
                });
            }
        }
    }

    private void updateRightPanel() {
        rightDetailPanel.removeAll();
        
        if (selectedStudentIndex >= 0 && selectedStudentIndex < adminTable.getRowCount()) {
            // Get the student info from the selected table row
            String selectedName = (String) adminTable.getValueAt(selectedStudentIndex, 0);
            String selectedEmail = (String) adminTable.getValueAt(selectedStudentIndex, 1);
            
            // Find the matching student in the students list
            Student s = students.stream()
                .filter(student -> selectedName.equals(student.getFullName()) && 
                                  selectedEmail.equals(student.emailAddress))
                .findFirst()
                .orElse(null);
            
            if (s == null) {
                rightDetailPanel.add(new JLabel("Student not found", JLabel.CENTER));
                rightDetailPanel.revalidate();
                rightDetailPanel.repaint();
                return;
            }
            
            // Now display the CORRECT student's details
            JPanel details = new JPanel(new GridLayout(0, 2, 5, 5));
            details.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            details.setBackground(Color.WHITE);
            
            String sectionName = getSectionName(s.yearLevel, s.section);
            addDetail(details, "Status", s.status);
            addDetail(details, "Name", s.getFullName());
            addDetail(details, "Age", String.valueOf(s.age));
            addDetail(details, "Year & Section", s.yearLevel + " - " + sectionName);
            addDetail(details, "Payment Plan", s.paymentPlan != null ? s.paymentPlan : "Not specified");
            addDetail(details, "Payment Method", s.paymentMethod != null ? s.paymentMethod : "Not specified");
            addDetail(details, "Reference #", s.bankReferenceNumber != null ? s.bankReferenceNumber : "N/A");
            addDetail(details, "Payment Status", s.paymentStatus != null ? s.paymentStatus : "Unpaid");
            addDetail(details, "Type", s.studentType);
            addDetail(details, "ESC Eligible", String.valueOf(s.escEligible));
            addDetail(details, "Total Fee", String.format("₱%.2f", s.totalAmount));
            rightDetailPanel.add(new JScrollPane(details), BorderLayout.CENTER);

            // Action buttons (Approve, Decline, For Verification)
            JPanel actions = new JPanel();
            actions.setBackground(new Color(211, 211, 211));
            
            JButton approve = new JButton("Approve");
            styleAdminButton(approve);
            JButton decline = new JButton("Decline");
            styleAdminButton(decline);
            JButton verify = new JButton("For Verification");
            styleAdminButton(verify);

            // Store reference to the correct student for button actions
            final Student selectedStudent = s;

            approve.addActionListener(e -> {
                // Generate ID when approving
                if (selectedStudent.id == null || selectedStudent.id.startsWith("S")) {
                    selectedStudent.id = generateID();
                }
                
                // CHANGED: Generate USER-FRIENDLY credentials for approved students
                String firstNameClean = selectedStudent.firstName != null && !selectedStudent.firstName.isEmpty() ? 
                    selectedStudent.firstName.toLowerCase().replaceAll("[^a-z]", "") : "user";
                String lastNameClean = selectedStudent.lastName != null && !selectedStudent.lastName.isEmpty() ? 
                    selectedStudent.lastName.toLowerCase().replaceAll("[^a-z]", "") : "student";
                
                // Username: firstname.lastname (e.g., ana.santos)
                selectedStudent.username = firstNameClean + "." + lastNameClean;
                
                // Check for duplicate usernames and add number if needed
                String baseUsername = selectedStudent.username;
                int counter = 1;
                boolean usernameExists = true;
                
                while (usernameExists) {
                    String finalUsername = selectedStudent.username;
                    usernameExists = students.stream()
                        .filter(st -> st != selectedStudent) // ✅ Changed to 'st'
                        .anyMatch(st -> finalUsername.equals(st.username)); // ✅ Use 'st' here too
                    
                    if (usernameExists) {
                        counter++;
                        selectedStudent.username = baseUsername + counter;
                    }
                }
                
                // Password: Lastname@StudentID (e.g., Santos@0006)
                String lastNameCapitalized = selectedStudent.lastName != null && !selectedStudent.lastName.isEmpty() ? 
                    selectedStudent.lastName.substring(0, 1).toUpperCase() + 
                    selectedStudent.lastName.substring(1).toLowerCase() : "Student";
                
                // Extract just the number part from ID (e.g., 2025-0006A -> 0006)
                String idNumber = selectedStudent.id.replaceAll("\\d{4}-(\\d{4})[A-Z]", "$1");
                selectedStudent.password = lastNameCapitalized + "@" + idNumber;
                
                selectedStudent.status = "Approved";
                
                String plan = selectedStudent.paymentPlan;
                if ("Full".equals(plan)) {
                    selectedStudent.paymentStatus = "Paid in Full";
                } else {
                    selectedStudent.paymentStatus = "Installment - Downpayment Paid";
                }
                
                saveData();
                refreshStudentTable(adminTableModel);
                refreshStudentInfoTable();
                adminTable.clearSelection();
                selectedStudentIndex = -1;
                updateRightPanel();
                
                JOptionPane.showMessageDialog(this, 
                    "╔════════════════════════════════════════╗\n" +
                    "         STUDENT APPROVED!              \n" +
                    "╚════════════════════════════════════════╝\n\n" +
                    
                    "Student Name: " + selectedStudent.getFullName() + "\n" +
                    "Student ID:   " + selectedStudent.id + "\n\n" +
                    
                    "┌────────────────────────────────────────┐\n" +
                    "│  PERMANENT LOGIN CREDENTIALS          │\n" +
                    "├────────────────────────────────────────┤\n" +
                    "│  Username: " + String.format("%-25s", selectedStudent.username) + "│\n" +
                    "│  Password: " + String.format("%-25s", selectedStudent.password) + "│\n" +
                    "└────────────────────────────────────────┘\n\n" +
                    
                    "Status:         " + selectedStudent.status + "\n" +
                    "Payment Status: " + selectedStudent.paymentStatus + "\n\n" +
                    
                    "✓ Student has been notified via email\n" +
                    "✓ Credentials are permanent and active",
                    "Approval Successful", JOptionPane.INFORMATION_MESSAGE);
            });
            
            decline.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(EnrollmentSystem.this,
                    "Are you sure you want to decline " + selectedStudent.getFullName() + "?",
                    "Confirm Decline", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    selectedStudent.status = "Declined";
                    saveData();
                    refreshStudentTable(adminTableModel);
                    adminTable.clearSelection();
                    selectedStudentIndex = -1;
                    updateRightPanel();
                    JOptionPane.showMessageDialog(EnrollmentSystem.this,
                        "Student declined successfully.",
                        "Student Declined", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            verify.addActionListener(e -> {
                selectedStudent.status = "For Verification";
                saveData();
                refreshStudentTable(adminTableModel);
                updateRightPanel();
                JOptionPane.showMessageDialog(this, "Student status set to 'For Verification'.");
            });
            
            actions.add(approve);
            actions.add(decline);
            actions.add(verify);
            rightDetailPanel.add(actions, BorderLayout.SOUTH);
        } else {
            rightDetailPanel.add(new JLabel("Select a student", JLabel.CENTER));
        }
        rightDetailPanel.revalidate();
        rightDetailPanel.repaint();
    }

    private String generateID() {
        int currentYear = java.time.LocalDateTime.now().getYear();
        
        // Initialize year if not present
        if (!yearCounters.containsKey(currentYear)) {
            yearCounters.put(currentYear, new HashMap<>());
            yearCounters.get(currentYear).put('A', 0);
            saveCounters(); // Save immediately after initialization
            System.out.println("Initialized year counter for " + currentYear + " starting at 0001A");
        }
        
        Map<Character, Integer> yearMap = yearCounters.get(currentYear);
        
        // Find current suffix (the one with the highest value)
        char currentSuffix = 'A';
        int maxCounter = 0;
        
        for (char suffix = 'A'; suffix <= 'Z'; suffix++) {
            if (yearMap.containsKey(suffix)) {
                int count = yearMap.get(suffix);
                if (count >= maxCounter) {
                    maxCounter = count;
                    currentSuffix = suffix;
                }
            }
        }
        
        // Get current counter for the active suffix
        int counter = yearMap.getOrDefault(currentSuffix, 0);
        
        // Increment counter
        counter++;
        
        // Check for rollover (9999 -> next suffix)
        if (counter > 9999) {
            counter = 1;
            currentSuffix++;
            
            if (currentSuffix > 'Z') {
                // Extremely rare case: more than 26 * 9999 students in one year
                JOptionPane.showMessageDialog(this, 
                    "Warning: Student ID suffix limit exceeded for year " + currentYear,
                    "ID Generation Warning", JOptionPane.WARNING_MESSAGE);
                currentSuffix = 'Z';
                counter = 9999; // Cap at maximum
            }
            
            yearMap.put(currentSuffix, counter);
            System.out.println("Rolled over to suffix " + currentSuffix + " for year " + currentYear);
        } else {
            yearMap.put(currentSuffix, counter);
        }
        
        // Save counters
        saveCounters();
        
        // Format: YYYY-NNNNX (e.g., 2025-0001A)
        String newID = String.format("%04d-%04d%c", currentYear, counter, currentSuffix);
        System.out.println("Generated new student ID: " + newID);
        
        return newID;
    }

	private void addDetail(JPanel panel, String label, String value) {
        JLabel lblKey = new JLabel(label + ":");
        lblKey.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblKey);
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(lblVal);
    }

    private void refreshStudentInfoTable() {
        if (studentInfoTableModel == null) return;
        studentInfoTableModel.setRowCount(0);
        for (Student s : getStudentsByStatus("Approved")) {
            String mi = (s.middleName != null && !s.middleName.isEmpty()) ?
                s.middleName.substring(0, 1).toUpperCase() + "." : "";
            String sectionName = getSectionName(s.yearLevel, s.section);
            studentInfoTableModel.addRow(new Object[]{
                s.id, s.lastName, s.firstName, mi, s.yearLevel, sectionName
            });
        }
    }

    private void updateStudentInfoRightPanel() {
        rightStudentInfoPanel.removeAll();
        if (selectedStudentInfoIndex >= 0) {
            List<Student> approved = getStudentsByStatus("Approved");
            if (selectedStudentInfoIndex < approved.size()) {
                Student s = approved.get(selectedStudentInfoIndex);
                
                // Create editable form
                JPanel formPanel = new JPanel(new GridBagLayout());
                formPanel.setBackground(Color.WHITE);
                formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.anchor = GridBagConstraints.WEST;
                
                // Store field references for editing
                Map<String, JComponent> editFields = new HashMap<>();
                
                int row = 0;
                String sectionName = getSectionName(s.yearLevel, s.section);
                
                // Student Number (non-editable)
                addEditableField(formPanel, gbc, row++, "Student Number:", s.id, editFields, "id", false);
                
                // Editable fields
                addEditableField(formPanel, gbc, row++, "First Name:", s.firstName, editFields, "firstName", true);
                addEditableField(formPanel, gbc, row++, "Middle Name:", s.middleName != null ? s.middleName : "", editFields, "middleName", true);
                addEditableField(formPanel, gbc, row++, "Last Name:", s.lastName, editFields, "lastName", true);
                addEditableField(formPanel, gbc, row++, "Age:", String.valueOf(s.age), editFields, "age", true);
                
                // Year Level dropdown
                String[] years = {"Grade 7", "Grade 8", "Grade 9", "Grade 10"};
                JComboBox<String> yearCombo = new JComboBox<>(years);
                yearCombo.setSelectedItem(s.yearLevel);
                addEditableComboField(formPanel, gbc, row++, "Year Level:", yearCombo, editFields, "yearLevel");
                
                // Section dropdown (dynamically populated based on year level)
                String[] sections = getSectionsForGrade(s.yearLevel);
                JComboBox<String> sectionCombo = new JComboBox<>(sections);
                sectionCombo.setSelectedItem(sectionName);
                addEditableComboField(formPanel, gbc, row++, "Section:", sectionCombo, editFields, "section");
                
                addEditableField(formPanel, gbc, row++, "Email:", s.emailAddress, editFields, "email", true);
                addEditableField(formPanel, gbc, row++, "Address:", s.address, editFields, "address", true);
                
                // Marital Status dropdown
                String[] maritalOptions = {"Single", "Married", "Divorced", "Widowed"};
                JComboBox<String> maritalCombo = new JComboBox<>(maritalOptions);
                maritalCombo.setSelectedItem(s.maritalStatus);
                addEditableComboField(formPanel, gbc, row++, "Marital Status:", maritalCombo, editFields, "maritalStatus");
                
                addEditableField(formPanel, gbc, row++, "Citizenship:", s.citizenship, editFields, "citizenship", true);
                addEditableField(formPanel, gbc, row++, "Contact Number:", s.contactNumber, editFields, "contactNumber", true);
                addEditableField(formPanel, gbc, row++, "Emergency Contact:", s.emergencyContactName, editFields, "emergencyContact", true);
                addEditableField(formPanel, gbc, row++, "Emergency Phone:", s.emergencyContactPhone, editFields, "emergencyPhone", true);
                
                JScrollPane scrollForm = new JScrollPane(formPanel);
                scrollForm.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                rightStudentInfoPanel.add(scrollForm, BorderLayout.CENTER);
                
                // Action buttons
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                btnPanel.setBackground(Color.WHITE);
                
                JButton viewDocs = new JButton("View Documents");
                JButton edit = new JButton("Edit");
                JButton saveChanges = new JButton("Save Changes");
                JButton hold = new JButton("Hold");
                JButton printReg = new JButton("Print Registration");
                JButton withdraw = new JButton("Withdraw Enrollment");
                
                styleAdminButton(viewDocs);
                styleAdminButton(edit);
                styleAdminButton(saveChanges);
                styleAdminButton(hold);
                styleAdminButton(printReg);
                styleAdminButton(withdraw);
                
                saveChanges.setEnabled(false);
                
                // Edit mode toggle
                final boolean[] editMode = {false};
                edit.addActionListener(e -> {
                    editMode[0] = !editMode[0];
                    enableFormEditing(editFields, editMode[0]);
                    saveChanges.setEnabled(editMode[0]);
                    edit.setText(editMode[0] ? "Cancel Edit" : "Edit");
                });
                
                // Save changes action
                saveChanges.addActionListener(e -> {
                    try {
                        s.firstName = ((JTextField)editFields.get("firstName")).getText().trim();
                        s.middleName = ((JTextField)editFields.get("middleName")).getText().trim();
                        s.lastName = ((JTextField)editFields.get("lastName")).getText().trim();
                        s.age = Integer.parseInt(((JTextField)editFields.get("age")).getText().trim());
                        s.yearLevel = (String)((JComboBox<?>)editFields.get("yearLevel")).getSelectedItem();
                        
                        String selectedSection = (String)((JComboBox<?>)editFields.get("section")).getSelectedItem();
                        String[] sectionsArray = getSectionsForGrade(s.yearLevel);
                        for (int i = 0; i < sectionsArray.length; i++) {
                            if (sectionsArray[i].equals(selectedSection)) {
                                s.section = i + 1;
                                break;
                            }
                        }
                        
                        s.emailAddress = ((JTextField)editFields.get("email")).getText().trim();
                        s.address = ((JTextField)editFields.get("address")).getText().trim();
                        s.maritalStatus = (String)((JComboBox<?>)editFields.get("maritalStatus")).getSelectedItem();
                        s.citizenship = ((JTextField)editFields.get("citizenship")).getText().trim();
                        s.contactNumber = ((JTextField)editFields.get("contactNumber")).getText().trim();
                        s.emergencyContactName = ((JTextField)editFields.get("emergencyContact")).getText().trim();
                        s.emergencyContactPhone = ((JTextField)editFields.get("emergencyPhone")).getText().trim();
                        
                        saveData();
                        refreshStudentInfoTable();
                        editMode[0] = false;
                        enableFormEditing(editFields, false);
                        saveChanges.setEnabled(false);
                        edit.setText("Edit");
                        
                        JOptionPane.showMessageDialog(EnrollmentSystem.this, 
                            "Student information updated successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(EnrollmentSystem.this, 
                            "Error saving changes: " + ex.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                // View Documents
                viewDocs.addActionListener(e -> {
                    StringBuilder docs = new StringBuilder();
                    docs.append("Documents for ").append(s.getFullName()).append(":\n\n");
                    docs.append("Birth Certificate: ").append(s.birthCertificatePath != null ? s.birthCertificatePath : "Not uploaded").append("\n");
                    docs.append("Report Card: ").append(s.reportCardPath != null ? s.reportCardPath : "Not uploaded").append("\n");
                    docs.append("Good Moral: ").append(s.goodMoralPath != null ? s.goodMoralPath : "Not uploaded").append("\n");
                    
                    JOptionPane.showMessageDialog(EnrollmentSystem.this, docs.toString(), 
                        "Student Documents", JOptionPane.INFORMATION_MESSAGE);
                });
                
                // Hold student
                hold.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(EnrollmentSystem.this,
                        "Place " + s.getFullName() + " on hold?",
                        "Confirm Hold", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        s.status = "Hold";
                        saveData();
                        JOptionPane.showMessageDialog(EnrollmentSystem.this, "Student placed on hold.");
                    }
                });
                
                // Print registration
                printReg.addActionListener(e -> {
                    printRegistration(s);
                });
                
                // Withdraw enrollment
                withdraw.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(EnrollmentSystem.this,
                        "Withdraw enrollment for " + s.getFullName() + "?",
                        "Confirm Withdrawal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        s.status = "Withdrawn";
                        saveData();
                        refreshStudentInfoTable();
                        JOptionPane.showMessageDialog(EnrollmentSystem.this, "Enrollment withdrawn.");
                    }
                });
                
                btnPanel.add(viewDocs);
                btnPanel.add(edit);
                btnPanel.add(saveChanges);
                btnPanel.add(hold);
                btnPanel.add(printReg);
                btnPanel.add(withdraw);
                
                rightStudentInfoPanel.add(btnPanel, BorderLayout.SOUTH);
            }
        } else {
            rightStudentInfoPanel.add(new JLabel("Select a student", JLabel.CENTER), BorderLayout.CENTER);
        }
        rightStudentInfoPanel.revalidate();
        rightStudentInfoPanel.repaint();
    }

    // Helper methods for the enhanced Student Info panel
    private void addEditableField(JPanel panel, GridBagConstraints gbc, int row, 
                                  String label, String value, Map<String, JComponent> fields, 
                                  String key, boolean editable) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setEditable(false);
        field.setBackground(Color.WHITE);
        panel.add(field, gbc);
        
        fields.put(key, field);
    }

    private void addEditableComboField(JPanel panel, GridBagConstraints gbc, int row,
                                       String label, JComboBox<?> combo, 
                                       Map<String, JComponent> fields, String key) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        combo.setEnabled(false);
        panel.add(combo, gbc);
        
        fields.put(key, combo);
    }

    private void enableFormEditing(Map<String, JComponent> fields, boolean enable) {
        for (Map.Entry<String, JComponent> entry : fields.entrySet()) {
            if (!entry.getKey().equals("id")) { // Don't allow editing ID
                if (entry.getValue() instanceof JTextField) {
                    ((JTextField)entry.getValue()).setEditable(enable);
                    entry.getValue().setBackground(enable ? new Color(255, 255, 200) : Color.WHITE);
                } else if (entry.getValue() instanceof JComboBox) {
                    ((JComboBox<?>)entry.getValue()).setEnabled(enable);
                }
            }
        }
    }

    private void printRegistration(Student s) {
        try {
            String filename = "registration_" + s.id + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            
            writer.println("=".repeat(60));
            writer.println("ACADEASE HIGH SCHOOL");
            writer.println("REGISTRATION FORM");
            writer.println("=".repeat(60));
            writer.println();
            writer.println("Student ID: " + s.id);
            writer.println("Name: " + s.getFullName());
            writer.println("Age: " + s.age);
            writer.println("Grade & Section: " + s.yearLevel + " - " + getSectionName(s.yearLevel, s.section));
            writer.println("Email: " + s.emailAddress);
            writer.println("Address: " + s.address);
            writer.println("Contact: " + s.contactNumber);
            writer.println("Emergency Contact: " + s.emergencyContactName + " (" + s.emergencyContactPhone + ")");
            writer.println();
            writer.println("Payment Plan: " + s.paymentPlan);
            writer.println("Total Fee: ₱" + String.format("%.2f", s.totalAmount));
            writer.println("Status: " + s.status);
            writer.println();
            writer.println("Printed on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=".repeat(60));
            
            writer.close();
            JOptionPane.showMessageDialog(this, "Registration printed to " + filename);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
        }
    }

    private List<Student> getStudentsByStatus(String status) {
        List<Student> list = new ArrayList<>();
        for (Student s : students) {
            if (status.equals(s.status)) list.add(s);
        }
        return list;
    }

    private void refreshClassSectionTable() {
        if (classSectionTableModel == null) return;
        classSectionTableModel.setRowCount(0);
        
        for (String grade : Arrays.asList("Grade 7", "Grade 8", "Grade 9", "Grade 10")) {
            String[] sections = SECTION_NAMES.get(grade);
            if (sections != null) {
                for (int i = 0; i < sections.length; i++) {
                    final String finalGrade = grade;
                    final int finalSec = i + 1;
                    String sectionName = sections[i];
                    int count = (int) students.stream()
                        .filter(s -> finalGrade.equals(s.yearLevel) && finalSec == s.section && "Approved".equals(s.status))
                        .count();
                    classSectionTableModel.addRow(new Object[]{grade, sectionName, count + " students"});
                }
            }
        }
    }

    private void refreshSectionStudentTable(String selected) {
        if (sectionStudentTableModel == null || selected == null) return;
        sectionStudentTableModel.setRowCount(0);
        
        String[] parts = selected.split(" - ");
        if (parts.length < 2) return;
        
        String grade = parts[0].trim();  // e.g., "Grade 7"
        String sectionName = parts[1].replace(" students", "").trim();  // e.g., "HUMILITY"
        
        // Find section number from section name
        String[] sectionsForGrade = SECTION_NAMES.get(grade);
        int sec = -1;
        if (sectionsForGrade != null) {
            for (int i = 0; i < sectionsForGrade.length; i++) {
                if (sectionsForGrade[i].equalsIgnoreCase(sectionName)) {
                    sec = i + 1;
                    break;
                }
            }
        }
        
        if (sec == -1) return;
        
        final int finalSec = sec;
        final String finalGrade = grade;
        
        // FIXED: Filter by grade, section, AND Approved status
        for (Student s : students) {
            if (finalGrade.equals(s.yearLevel) && 
                finalSec == s.section && 
                "Approved".equals(s.status)) {  // Make sure this filter is consistent
                sectionStudentTableModel.addRow(new Object[]{
                    s.id, s.getFullName(), s.status, s.paymentStatus
                });
            }
        }
    }
    private void changeSectionDialog() {
        if (selectedSectionStudentIndex < 0) return;

        // Get the selected student's ID from the "Students in Selected Section" table
        String studentId = (String) sectionStudentTableModel.getValueAt(selectedSectionStudentIndex, 0);

        // Find the corresponding Student object in the main students list
        Student targetStudent = students.stream()
            .filter(s -> studentId.equals(s.id))
            .findFirst()
            .orElse(null);

        if (targetStudent == null) {
            JOptionPane.showMessageDialog(this, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Determine the grade level of the student to be changed
        String grade = targetStudent.yearLevel; // Use the student's grade level directly

        // Retrieve the list of section *names* available for that grade level
        String[] sectionsForGrade = SECTION_NAMES.get(grade);

        if (sectionsForGrade == null) {
            JOptionPane.showMessageDialog(this, "Section configuration not found for grade: " + grade, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prepare the options for the input dialog - these are the section names for the specific grade
        String[] sectionOptions = sectionsForGrade; // Directly use the array from SECTION_NAMES

        // Show the input dialog, presenting only the sections for the student's grade level
        String selectedSectionName = (String) JOptionPane.showInputDialog(
            this,
            "Select new section for " + targetStudent.getFullName(), // Message
            "Change Section", // Title
            JOptionPane.PLAIN_MESSAGE, // messageType
            null, // icon
            sectionOptions, // selectionValues (This is the crucial part)
            sectionOptions[0] // initialSelectionValue
        );

        // Check if the user made a selection (didn't press Cancel)
        if (selectedSectionName != null) {
            // Find the corresponding section *number* based on the selected name
            int newSecNum = -1;
            for (int i = 0; i < sectionsForGrade.length; i++) {
                if (sectionsForGrade[i].equals(selectedSectionName)) {
                    newSecNum = i + 1; // Section numbers are 1-based
                    break;
                }
            }

            // Check if the selected section is full before changing
            if (newSecNum != -1 && !isSectionFull(grade, newSecNum)) {
                // Update the student's section number
                targetStudent.section = newSecNum;

                // Update the student's subjects based on the new section
                String newSectionKey = grade + "-" + selectedSectionName;
                targetStudent.subjects = new HashSet<>(SECTION_SUBJECTS.getOrDefault(newSectionKey, new HashSet<>()));

                // Save the changes to the data file
                saveData();

                // Refresh the UI to reflect the changes
                refreshClassSectionTable(); // Refresh the left table showing sections and counts
                String currentlySelectedSectionKey = classSectionTable.getSelectedRow() >= 0 ?
                    (String) classSectionTableModel.getValueAt(classSectionTable.getSelectedRow(), 0) : null;
                // Refresh the middle table showing students in the currently viewed section
                refreshSectionStudentTable(currentlySelectedSectionKey);

                JOptionPane.showMessageDialog(this, "Section changed successfully for " + targetStudent.getFullName() + " to " + selectedSectionName + "!");
            } else if (newSecNum == -1) {
                 // This case should ideally not happen if the dialog options are correct
                 JOptionPane.showMessageDialog(this, "An unexpected error occurred while identifying the selected section.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                 // Section is full
                 JOptionPane.showMessageDialog(this, "Selected section '" + selectedSectionName + "' is full (5 students max). Cannot move student.", "Section Full", JOptionPane.WARNING_MESSAGE);
            }
        }
        
    }


    private void updateClassRightPanel() {
        rightClassDetailPanel.removeAll();
        if (selectedClassStudentIndex >= 0 && selectedClassStudentIndex < classSectionTableModel.getRowCount()) {
            String grade = (String) classSectionTableModel.getValueAt(selectedClassStudentIndex, 0);
            String sectionName = (String) classSectionTableModel.getValueAt(selectedClassStudentIndex, 1);
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Section info panel with border
            JPanel infoPanel = new JPanel(new GridBagLayout());
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            infoPanel.setMaximumSize(new Dimension(600, 150));
            infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(8, 10, 8, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            String[] sectionsForGrade = SECTION_NAMES.get(grade);
            int secNum = -1;
            if (sectionsForGrade != null) {
                for (int i = 0; i < sectionsForGrade.length; i++) {
                    if (sectionsForGrade[i].equals(sectionName)) {
                        secNum = i + 1;
                        break;
                    }
                }
            }
            if (secNum != -1) {
                final int finalSec = secNum;
                final String finalGrade = grade;
                List<Student> studentsInSection = students.stream()
                    .filter(s -> finalGrade.equals(s.yearLevel) && finalSec == s.section && "Approved".equals(s.status))
                    .collect(java.util.stream.Collectors.toList());
                int currentCount = studentsInSection.size();
                int paidCount = (int) studentsInSection.stream()
                    .filter(s -> "Paid in Full".equals(s.paymentStatus))
                    .count();

                // Grade Level
                gbc.gridx = 0; gbc.gridy = 0;
                gbc.weightx = 0.3;
                JLabel gradeLevelLabel = new JLabel("Grade Level:");
                gradeLevelLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                infoPanel.add(gradeLevelLabel, gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.7;
                JLabel gradeLevelValue = new JLabel(grade);
                gradeLevelValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                infoPanel.add(gradeLevelValue, gbc);

                // Section
                gbc.gridx = 0; gbc.gridy = 1;
                gbc.weightx = 0.3;
                JLabel sectionLabel = new JLabel("Section:");
                sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                infoPanel.add(sectionLabel, gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.7;
                JLabel sectionValue = new JLabel(String.valueOf(secNum));
                sectionValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                infoPanel.add(sectionValue, gbc);

                // Student Count
                gbc.gridx = 0; gbc.gridy = 2;
                gbc.weightx = 0.3;
                JLabel countLabel = new JLabel("Student Count:");
                countLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                infoPanel.add(countLabel, gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.7;
                JLabel countValue = new JLabel(currentCount + " students");
                countValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                infoPanel.add(countValue, gbc);

                // Payment Status
                gbc.gridx = 0; gbc.gridy = 3;
                gbc.weightx = 0.3;
                JLabel paymentLabel = new JLabel("Payment Status:");
                paymentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                infoPanel.add(paymentLabel, gbc);
                gbc.gridx = 1;
                gbc.weightx = 0.7;
                String paymentStatusText = paidCount == currentCount ?
                    "All Paid (" + paidCount + "/" + currentCount + ")" :
                    (paidCount == 0 ? "All Unpaid (0/" + currentCount + ")" :
                    "Partial (" + paidCount + "/" + currentCount + ")");
                JLabel paymentValue = new JLabel(paymentStatusText);
                paymentValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                // Color code the payment status
                if (paidCount == 0) {
                    paymentValue.setForeground(Color.RED);
                    paymentValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
                } else if (paidCount == currentCount) {
                    paymentValue.setForeground(new Color(0, 150, 0));
                } else {
                    paymentValue.setForeground(new Color(200, 140, 0));
                }
                infoPanel.add(paymentValue, gbc);

                contentPanel.add(infoPanel);
                contentPanel.add(Box.createVerticalStrut(15));

                // Schedule table
                String[] days = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
                DefaultTableModel scheduleModel = new DefaultTableModel(days, 0) {
                    @Override public boolean isCellEditable(int r, int c) { return c > 0; } // Allow editing subjects for days
                };
                String[] timeSlots = {
                    "8:00-9:00", "9:00-10:00", "10:00-10:20", "10:20-11:20",
                    "11:20-12:20", "12:20-1:20", "1:20-2:20", "2:20-3:00", "3:00-4:00"
                };

                // Load schedule from predefined SECTION_SCHEDULES (NEW STRUCTURE)
                String scheduleKey = grade + "-" + sectionName;
                Map<String, String[]> sectionScheduleMap = SECTION_SCHEDULES.get(scheduleKey);

                // Populate the schedule table based on SECTION_SCHEDULES
                if (sectionScheduleMap != null) {
                    // Create a mapping from time slot to day-specific subject for easier lookup
                    Map<String, Map<String, String>> timeToDaySubject = new HashMap<>();
                    for (String day : days) {
                        if (!"Time".equals(day)) { // Skip the 'Time' header
                            String[] daySchedule = sectionScheduleMap.get(day);
                            if (daySchedule != null) {
                                for (String entry : daySchedule) {
                                    String[] parts = entry.split(" \\| ", 2); // Split on " | " and limit to 2 parts
                                    if (parts.length >= 2) {
                                        String timeSlot = parts[0].trim();
                                        String subject = parts[1].trim();
                                        timeToDaySubject.computeIfAbsent(timeSlot, k -> new HashMap<>()).put(day, subject);
                                    }
                                }
                            }
                        }
                    }

                    // Add rows to the table model
                    for (String time : timeSlots) {
                        Object[] row = new Object[6];
                        row[0] = time; // Time slot in the first column
                        for (int d = 1; d <= 5; d++) { // Loop through Monday to Friday
                            String day = days[d];
                            String subjectForTimeAndDay = timeToDaySubject.getOrDefault(time, new HashMap<>()).get(day);
                            row[d] = subjectForTimeAndDay != null ? subjectForTimeAndDay : ""; // Populate with subject or empty string
                        }
                        scheduleModel.addRow(row);
                    }
                } else {
                    // If no predefined schedule, leave the table empty or show a message
                    for (String time : timeSlots) {
                        Object[] row = new Object[6];
                        row[0] = time;
                        for (int d = 1; d <= 5; d++) {
                            row[d] = "";
                        }
                        scheduleModel.addRow(row);
                    }
                }


                JTable scheduleTable = new JTable(scheduleModel);
                scheduleTable.setRowHeight(35);
                scheduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                scheduleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                scheduleTable.getTableHeader().setBackground(new Color(240, 240, 240));
                scheduleTable.setGridColor(Color.GRAY);
                scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(80);
                scheduleTable.getColumnModel().getColumn(0).setMaxWidth(100);
                JScrollPane scheduleScroll = new JScrollPane(scheduleTable);
                scheduleScroll.setMaximumSize(new Dimension(600, 400));
                scheduleScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
                scheduleScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                contentPanel.add(scheduleScroll);

                contentPanel.add(Box.createVerticalStrut(15));

                // Schedule buttons
                JPanel scheduleBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                scheduleBtns.setBackground(Color.WHITE);
                scheduleBtns.setMaximumSize(new Dimension(600, 50));
                scheduleBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

                JButton saveSchedule = new JButton("Save Changes");
                saveSchedule.setFont(new Font("Segoe UI", Font.BOLD, 14));
                saveSchedule.setBackground(new Color(0, 123, 255));
                saveSchedule.setForeground(Color.WHITE);
                saveSchedule.setFocusPainted(false);
                saveSchedule.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                saveSchedule.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JButton clearSchedule = new JButton("Clear Schedule");
                clearSchedule.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                clearSchedule.setBackground(Color.WHITE);
                clearSchedule.setForeground(Color.BLACK);
                clearSchedule.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                clearSchedule.setFocusPainted(false);
                clearSchedule.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                clearSchedule.setPreferredSize(new Dimension(140, 40));

                JButton editSchedule = new JButton("Edit Schedule");
                editSchedule.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                editSchedule.setBackground(Color.WHITE);
                editSchedule.setForeground(Color.BLACK);
                editSchedule.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                editSchedule.setFocusPainted(false);
                editSchedule.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                editSchedule.setPreferredSize(new Dimension(140, 40));

                final boolean[] editMode = {false};
                scheduleTable.setEnabled(false); // Initially disabled

                editSchedule.addActionListener(e -> {
                    editMode[0] = !editMode[0];
                    scheduleTable.setEnabled(editMode[0]);
                    editSchedule.setText(editMode[0] ? "Cancel" : "Edit Schedule");
                    saveSchedule.setEnabled(editMode[0]);
                    if (editMode[0]) {
                        editSchedule.setBackground(new Color(220, 220, 220));
                    } else {
                        editSchedule.setBackground(Color.WHITE);
                    }
                });

                saveSchedule.addActionListener(e -> {
                    // When saving, update the SECTION_SCHEDULES map based on the table content
                    Map<String, String[]> updatedScheduleMap = new HashMap<>();
                    for (int d = 1; d <= 5; d++) { // Loop through Monday to Friday
                        String day = days[d];
                        List<String> dayScheduleList = new ArrayList<>();
                        for (int r = 0; r < scheduleModel.getRowCount(); r++) {
                            String time = (String) scheduleModel.getValueAt(r, 0);
                            String subject = (String) scheduleModel.getValueAt(r, d); // Get subject for this time and day
                            if (subject != null && !subject.trim().isEmpty()) {
                                dayScheduleList.add(time + " | " + subject);
                            }
                        }
                        if (!dayScheduleList.isEmpty()) {
                            updatedScheduleMap.put(day, dayScheduleList.toArray(new String[0]));
                        } else {
                            updatedScheduleMap.put(day, new String[0]); // Or remove day if empty?
                        }
                    }

                    SECTION_SCHEDULES.put(scheduleKey, updatedScheduleMap);

                    // Also update the main 'schedules' map if needed for persistence, but using the new format
                    Map<String, String> newScheduleMap = new HashMap<>();
                    for (int r = 0; r < scheduleModel.getRowCount(); r++) {
                         String time = (String) scheduleModel.getValueAt(r, 0);
                         for (int d = 1; d <= 5; d++) {
                             String day = days[d];
                             String subject = (String) scheduleModel.getValueAt(r, d);
                             if (subject != null && !subject.trim().isEmpty()) {
                                 String key = time + "_" + day;
                                 newScheduleMap.put(key, subject);
                             }
                         }
                    }
                    if (!newScheduleMap.isEmpty()) {
                        schedules.put(scheduleKey, newScheduleMap);
                    } else {
                        schedules.remove(scheduleKey);
                    }

                    saveSchedules(); // Persist the 'schedules' map
                    JOptionPane.showMessageDialog(EnrollmentSystem.this, "Schedule saved successfully!");
                    editMode[0] = false;
                    scheduleTable.setEnabled(false);
                    editSchedule.setText("Edit Schedule");
                    editSchedule.setBackground(Color.WHITE);
                    saveSchedule.setEnabled(false);
                });

                clearSchedule.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(EnrollmentSystem.this,
                        "Clear all schedule entries for this section?",
                        "Confirm Clear", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        SECTION_SCHEDULES.remove(scheduleKey); // Clear predefined schedule
                        schedules.remove(scheduleKey);        // Clear persisted schedule
                        saveSchedules();
                        updateClassRightPanel(); // Refresh the panel to show empty schedule
                        JOptionPane.showMessageDialog(EnrollmentSystem.this, "Schedule cleared!");
                    }
                });

                saveSchedule.setEnabled(false);
                scheduleBtns.add(saveSchedule);
                scheduleBtns.add(clearSchedule);
                scheduleBtns.add(editSchedule);
                contentPanel.add(scheduleBtns);
            }
            JScrollPane contentScroll = new JScrollPane(contentPanel);
            contentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            contentScroll.setBorder(BorderFactory.createEmptyBorder());
            rightClassDetailPanel.add(contentScroll, BorderLayout.CENTER);
        } else {
            JLabel emptyLabel = new JLabel("Select a section", JLabel.CENTER);
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(Color.GRAY);
            rightClassDetailPanel.add(emptyLabel, BorderLayout.CENTER);
        }
        rightClassDetailPanel.revalidate();
        rightClassDetailPanel.repaint();
    }
    // SCHEDULE DISPLAY - CORRECTED
    private static final Map<String, Map<String, String[]>> SECTION_SCHEDULES = new HashMap<>();
    static {
        // Grade 7 - HUMILITY
        Map<String, String[]> schedule7H = new HashMap<>();
        schedule7H.put("Monday", new String[]{
            "8:00-9:00 | Mathematics",
            "9:00-10:00 | English",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Science",
            "11:20-12:20 | Filipino",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | TLE",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule7H.put("Tuesday", schedule7H.get("Monday")); // Same as Monday
        schedule7H.put("Wednesday", new String[]{ // PE replaces TLE
            "8:00-9:00 | Mathematics",
            "9:00-10:00 | English",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Science",
            "11:20-12:20 | Filipino",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | PE",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule7H.put("Thursday", schedule7H.get("Monday")); // Same as Monday
        schedule7H.put("Friday", schedule7H.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 7-HUMILITY", schedule7H);

        // Grade 7 - COURAGE
        Map<String, String[]> schedule7C = new HashMap<>();
        schedule7C.put("Monday", new String[]{
            "8:00-9:00 | English",
            "9:00-10:00 | Mathematics",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Filipino",
            "11:20-12:20 | Science",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | MAPEH",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule7C.put("Tuesday", schedule7C.get("Monday")); // Same as Monday
        schedule7C.put("Wednesday", new String[]{ // PE replaces MAPEH
            "8:00-9:00 | English",
            "9:00-10:00 | Mathematics",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Filipino",
            "11:20-12:20 | Science",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | PE",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule7C.put("Thursday", schedule7C.get("Monday")); // Same as Monday
        schedule7C.put("Friday", schedule7C.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 7-COURAGE", schedule7C);

        // Grade 8 - INTEGRITY
        Map<String, String[]> schedule8I = new HashMap<>();
        schedule8I.put("Monday", new String[]{
            "8:00-9:00 | Mathematics",
            "9:00-10:00 | English",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Science",
            "11:20-12:20 | TLE",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Araling Panlipunan",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule8I.put("Tuesday", schedule8I.get("Monday")); // Same as Monday
        schedule8I.put("Wednesday", new String[]{ // Computer replaces TLE
            "8:00-9:00 | Mathematics",
            "9:00-10:00 | English",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Science",
            "11:20-12:20 | Computer",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Araling Panlipunan",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule8I.put("Thursday", schedule8I.get("Monday")); // Same as Monday
        schedule8I.put("Friday", schedule8I.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 8-INTEGRITY", schedule8I);

        // Grade 8 - RESILIENCE
        Map<String, String[]> schedule8R = new HashMap<>();
        schedule8R.put("Monday", new String[]{
            "8:00-9:00 | English",
            "9:00-10:00 | Mathematics",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Araling Panlipunan",
            "11:20-12:20 | Science",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Filipino",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule8R.put("Tuesday", schedule8R.get("Monday")); // Same as Monday
        schedule8R.put("Wednesday", new String[]{ // Music replaces GMRC
            "8:00-9:00 | English",
            "9:00-10:00 | Mathematics",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Araling Panlipunan",
            "11:20-12:20 | Science",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Filipino",
            "2:20-3:00 | Music",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule8R.put("Thursday", schedule8R.get("Monday")); // Same as Monday
        schedule8R.put("Friday", schedule8R.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 8-RESILIENCE", schedule8R);

        // Grade 9 - DETERMINATION
        Map<String, String[]> schedule9D = new HashMap<>();
        schedule9D.put("Monday", new String[]{
            "8:00-9:00 | Science",
            "9:00-10:00 | English",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Mathematics",
            "11:20-12:20 | Araling Panlipunan",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | TLE",
            "2:20-3:00 | MAPEH",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule9D.put("Tuesday", schedule9D.get("Monday")); // Same as Monday
        schedule9D.put("Wednesday", new String[]{ // Science Laboratory replaces Science
            "8:00-9:00 | Science Laboratory",
            "9:00-10:00 | English",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Mathematics",
            "11:20-12:20 | Araling Panlipunan",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | TLE",
            "2:20-3:00 | MAPEH",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule9D.put("Thursday", schedule9D.get("Monday")); // Same as Monday
        schedule9D.put("Friday", schedule9D.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 9-DETERMINATION", schedule9D);

        // Grade 9 - GRATITUDE
        Map<String, String[]> schedule9G = new HashMap<>();
        schedule9G.put("Monday", new String[]{
            "8:00-9:00 | English",
            "9:00-10:00 | Science",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Araling Panlipunan",
            "11:20-12:20 | Mathematics",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Music/Arts",
            "2:20-3:00 | TLE",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule9G.put("Tuesday", schedule9G.get("Monday")); // Same as Monday
        schedule9G.put("Wednesday", new String[]{ // Science Laboratory replaces Science
            "8:00-9:00 | English",
            "9:00-10:00 | Science Laboratory",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Araling Panlipunan",
            "11:20-12:20 | Mathematics",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Music/Arts",
            "2:20-3:00 | TLE",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule9G.put("Thursday", schedule9G.get("Monday")); // Same as Monday
        schedule9G.put("Friday", schedule9G.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 9-GRATITUDE", schedule9G);

        // Grade 10 - FORTITUDE
        Map<String, String[]> schedule10F = new HashMap<>();
        schedule10F.put("Monday", new String[]{
            "8:00-9:00 | Science",
            "9:00-10:00 | Mathematics",
            "10:00-10:20 | Recess",
            "10:20-11:20 | English",
            "11:20-12:20 | Araling Panlipunan",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | TLE",
            "2:20-3:00 | MAPEH",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule10F.put("Tuesday", schedule10F.get("Monday")); // Same as Monday
        schedule10F.put("Wednesday", new String[]{ // Research replaces TLE
            "8:00-9:00 | Science",
            "9:00-10:00 | Mathematics",
            "10:00-10:20 | Recess",
            "10:20-11:20 | English",
            "11:20-12:20 | Araling Panlipunan",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Research",
            "2:20-3:00 | MAPEH",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule10F.put("Thursday", schedule10F.get("Monday")); // Same as Monday
        schedule10F.put("Friday", schedule10F.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 10-FORTITUDE", schedule10F);

        // Grade 10 - HONESTY
        Map<String, String[]> schedule10H = new HashMap<>();
        schedule10H.put("Monday", new String[]{
            "8:00-9:00 | English",
            "9:00-10:00 | Science",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Mathematics",
            "11:20-12:20 | Araling Panlipunan",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | TLE",
            "2:20-3:00 | MAPEH",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule10H.put("Tuesday", schedule10H.get("Monday")); // Same as Monday
        schedule10H.put("Wednesday", new String[]{ // PE replaces MAPEH
            "8:00-9:00 | English",
            "9:00-10:00 | Science",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Mathematics",
            "11:20-12:20 | Araling Panlipunan",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | TLE",
            "2:20-3:00 | PE",
            "3:00-4:00 | Homework / Study Period"
        });
        schedule10H.put("Thursday", schedule10H.get("Monday")); // Same as Monday
        schedule10H.put("Friday", schedule10H.get("Monday"));   // Same as Monday
        SECTION_SCHEDULES.put("Grade 10-HONESTY", schedule10H);
    }


// PERSISTENCE METHODS
private void saveData() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
        oos.writeObject(students);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage());
    }
}

private void saveInstructors() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(instructorFile))) {
        oos.writeObject(instructors);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage());
    }
}

private void saveAssignments() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(assignmentsFile))) {
        oos.writeObject(instructorAssignments);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage());
    }
}

private void saveSchedules() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(schedulesFile))) {
        oos.writeObject(schedules);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Save error: " + e.getMessage());
    }
}


private void saveCounters() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(counterFile))) {
        oos.writeObject(yearCounters);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Counter save error: " + e.getMessage());
    }
}

private void loadCounters() {
    if (counterFile.exists()) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(counterFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                yearCounters = (Map<Integer, Map<Character, Integer>>) obj;
            }
        } catch (Exception e) {
            yearCounters = new HashMap<>();
        }
    } else {
        yearCounters = new HashMap<>();
    }
}


private void createSampleData() {
    this.students = new ArrayList<>();
    
    // Get current year dynamically
    int year = java.time.LocalDateTime.now().getYear();
    System.out.println("Creating sample data for year: " + year);
    
    // Initialize counters for current year
    if (!yearCounters.containsKey(year)) {
        yearCounters.put(year, new HashMap<>());
    }
    yearCounters.get(year).put('A', 5); // Start at 5 since we're creating 5 approved samples
    
    System.out.println("Initialized year " + year + " counter at position 5 (suffix A)");

    // Sample data using named sections
    Set<String> g7Sec1Subjects = SECTION_SUBJECTS.getOrDefault("Grade 7-HUMILITY", new HashSet<>());
    Set<String> g7Sec2Subjects = SECTION_SUBJECTS.getOrDefault("Grade 7-COURAGE", new HashSet<>());
    Set<String> g8Sec1Subjects = SECTION_SUBJECTS.getOrDefault("Grade 8-INTEGRITY", new HashSet<>());
    Set<String> g9Sec2Subjects = SECTION_SUBJECTS.getOrDefault("Grade 9-GRATITUDE", new HashSet<>());
    Set<String> g10Sec1Subjects = SECTION_SUBJECTS.getOrDefault("Grade 10-FORTITUDE", new HashSet<>());

    // Student 1: Approved with ID
    Student s1 = new Student(
        "Juan", "Dela Cruz", "Reyes",
        "123 Oak Street", "juan.dela@example.com",
        "Single", "Filipino", 12, "Grade 7", 1, g7Sec1Subjects,
        "09123456789", "Maria Dela Cruz", "09876543210",
        "birth_cert_s1.pdf", "form137_s1.pdf", "good_moral_s1.pdf"
    );
    s1.studentType = "New";
    s1.escEligible = true;
    s1.paymentPlan = "Full";
    s1.status = "Approved";
    s1.paymentStatus = "Paid in Full";
    s1.paymentMethod = "Cash";
 // Student 1: Approved with user-friendly credentials
    s1.id = String.format("%04d-0001A", year);
    s1.username = "juan.reyes";  // ← CHANGED
    s1.password = "Reyes@0001";  // ← CHANGED
    s1.computeFee();

 // Student 2: Pending (temporary credentials)
    Student s2 = new Student(
        "Ana", "Maganda", "Santos",
        "456 Pine Avenue", "ana.maganda@example.com",
        "Single", "Filipino", 13, "Grade 8", 1, g8Sec1Subjects,
        "09234567890", "Pedro Santos", "09765432109",
        "birth_cert_s2.pdf", "form137_s2.pdf", "good_moral_s2.pdf"
    );
    s2.studentType = "Transferee";
    s2.paymentPlan = "Semi";
    s2.status = "Pending";
    s2.paymentStatus = "Unpaid";
    s2.id = null;
    // Temporary system-generated credentials
    s2.username = "pending_" + System.currentTimeMillis();
    s2.password = "temp_" + System.currentTimeMillis();
    s2.computeFee();

    // Student 3: Approved with ID
    Student s3 = new Student(
        "Ramon", "Bautista", "Garcia",
        "789 Maple Drive", "ramon.bautista@example.com",
        "Single", "Filipino", 14, "Grade 9", 2, g9Sec2Subjects,
        "09345678901", "Lourdes Garcia", "09654321098",
        "birth_cert_s3.pdf", "form138_s3.pdf", ""
    );
    s3.studentType = "Old";
    s3.paymentPlan = "Quarterly";
    s3.status = "Approved";
    s3.paymentStatus = "Installment";
    s3.paymentMethod = "Bank Transfer";
    s3.id = String.format("%04d-0002A", year);
    s3.id = String.format("%04d-0002A", year);
    s3.username = "ramon.garcia";  // ← CHANGED
    s3.password = "Garcia@0002";   // ← CHANGED
    s3.computeFee();

    // Student 4: Approved with ID
    Student s4 = new Student(
        "Maria", "Clara", "Lopez",
        "321 Elm Blvd", "maria.lopez@example.com",
        "Single", "Filipino", 15, "Grade 10", 1, g10Sec1Subjects,
        "09456789012", "Carlos Lopez", "09543210987",
        "birth_cert_s4.pdf", "form137_s4.pdf", "good_moral_s4.pdf"
    );
    s4.studentType = "New";
    s4.escEligible = false;
    s4.paymentPlan = "Monthly";
    s4.status = "Approved";
    s4.paymentStatus = "Installment Pending";
    s4.id = String.format("%04d-0003A", year);
    s4.username = "maria.lopez";  // ← CHANGED
    s4.password = "Lopez@0003";   // ← CHANGED
    s4.computeFee();

    // Student 5: Approved with ID
    Student s5 = new Student(
        "Lito", "", "Santos",
        "654 Cedar Lane", "lito.santos@example.com",
        "Single", "Filipino", 12, "Grade 7", 2, g7Sec2Subjects,
        "09567890123", "Lolita Santos", "09432109876",
        "birth_cert_s5.pdf", "form138_s5.pdf", ""
    );
    s5.studentType = "Old";
    s5.paymentPlan = "Full";
    s5.status = "Approved";
    s5.paymentStatus = "Paid in Full";
    s5.paymentMethod = "Cash";
    s5.id = String.format("%04d-0004A", year);
    s5.username = "lito.santos";  // ← CHANGED
    s5.password = "Santos@0004";  // ← CHANGED
    s5.computeFee();

    // Student 6: For Verification status (has ID because payment was submitted)
    Student s6 = new Student(
        "Pedro", "Miguel", "Rizal",
        "888 Hero Avenue", "pedro.rizal@example.com",
        "Single", "Filipino", 13, "Grade 8", 1, g8Sec1Subjects,
        "09678901234", "Teresa Rizal", "09321098765",
        "birth_cert_s6.pdf", "form137_s6.pdf", "good_moral_s6.pdf"
    );
    s6.studentType = "New";
    s6.paymentPlan = "Full";
    s6.status = "For Verification";
    s6.paymentStatus = "Payment Submitted";
    s6.paymentMethod = "GCash";
    s6.bankReferenceNumber = "GCASH123456789";
    s6.id = String.format("%04d-0005A", year);
    s6.username = "pedro.rizal";  // ← CHANGED
    s6.password = "Rizal@0005";   // ← CHANGED
    s6.computeFee();

    students.add(s1);
    students.add(s2);
    students.add(s3);
    students.add(s4);
    students.add(s5);
    students.add(s6);
    
    saveData();
    saveCounters();

    System.out.println("=== SAMPLE DATA CREATION SUMMARY ===");
    System.out.println("Year: " + year);
    System.out.println("Total students created: " + students.size());
    System.out.println("Approved students: 5 (IDs assigned)");
    System.out.println("Pending students: 1 (no ID yet)");
    System.out.println("Counter position: 5 (suffix A)");
    System.out.println("Next ID will be: " + year + "-0006A");
    System.out.println("=====================================");
}
private void loadData() {
    if (dataFile.exists()) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                students = (List<Student>) obj;
                System.out.println("Data loaded successfully: " + students.size() + " students");
                for (Student s : students) {
                    // ✅ FIXED: Check if ID is not null before processing
                    if (s.id != null && !s.id.isEmpty()) {
                        String num = s.id.replaceAll("[^0-9]", "");
                        try {
                            int val = Integer.parseInt(num);
                            studentIdCounter = Math.max(studentIdCounter, val);
                        } catch (Exception ignored) {}
                    }
                    
                    // Set default values for missing fields
                    if (s.enrolledAt == 0L) {
                        s.enrolledAt = System.currentTimeMillis();
                    }
                    if (s.status == null || s.status.isEmpty()) {
                        s.status = "Pending";
                    }
                    
                    // ✅ ADDED: Initialize temporary credentials if missing
                    if (s.username == null || s.username.isEmpty()) {
                        s.username = "pending_" + System.currentTimeMillis();
                    }
                    if (s.password == null || s.password.isEmpty()) {
                        s.password = "temp_" + System.currentTimeMillis();
                    }
                }
            } else {
                createSampleData();
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            createSampleData();
        } catch (ClassCastException ex) {
            ex.printStackTrace();
            createSampleData();
        }
    } else {
        createSampleData();
    }
}







private void loadInstructors() {
    if (instructorFile.exists()) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(instructorFile))) {
            Object obj = ois.readObject();
            if (obj instanceof List) instructors = (List<Instructor>) obj;
            for (Instructor i : instructors) {
                try {
                    String num = i.instructorId.replaceAll("[^0-9]", "");
                    int val = Integer.parseInt(num);
                    instructorIdCounter = Math.max(instructorIdCounter, val);
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            instructors = new ArrayList<>();
        }
    } else {
        instructors = new ArrayList<>();
    }
}

private void loadAssignments() {
    if (assignmentsFile.exists()) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(assignmentsFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) instructorAssignments = (Map<String, String>) obj;
        } catch (Exception e) {
            instructorAssignments = new HashMap<>();
        }
    }
}

private void loadSchedules() {
    if (schedulesFile.exists()) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(schedulesFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) schedules = (Map<String, Map<String, String>>) obj;
        } catch (Exception e) {
            schedules = new HashMap<>();
        }
    }
}

// NESTED CLASSES
 
public static class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String id;
    protected String firstName, middleName, lastName;
    protected String address, emailAddress, maritalStatus, citizenship;
    protected int age;
    protected String yearLevel;
    protected int section;
    protected Set<String> subjects = new HashSet<>();
    protected long enrolledAt = System.currentTimeMillis();
    protected String status = "Pending";
    protected String studentType;
    protected boolean escEligible = false;
    protected String username, password;

    protected String contactNumber, emergencyContactName, emergencyContactPhone;
    protected String birthCertificatePath, reportCardPath, goodMoralPath;
    protected String paymentStatus = "Unpaid", paymentMethod, paymentPlan, installmentPlan, bankReferenceNumber;
    protected double tuitionFee, miscFees, escDiscount, totalAmount;

    public Student(String fn, String mn, String ln, String addr, String email,
            String marital, String citizen, int age, String yl, int sec,
            Collection<String> subs, String contact, String emergName,
            String emergPhone, String bc, String reportCard, String goodMoral) {
        this.firstName = fn;
        this.middleName = mn;
        this.lastName = ln;
        this.address = addr;
        this.emailAddress = email;
        this.maritalStatus = marital;
        this.citizenship = citizen;
        this.age = age;
        this.yearLevel = yl;
        this.section = sec;
        if (subs != null) subjects.addAll(subs);
        this.contactNumber = contact;
        this.emergencyContactName = emergName;
        this.emergencyContactPhone = emergPhone;
        this.birthCertificatePath = bc;
        this.reportCardPath = reportCard;
        this.goodMoralPath = goodMoral;
        
        // ID will be set when student is approved by admin
        this.id = null;
        
        // TEMPORARY credentials for pending status (system-generated)
        long timestamp = System.currentTimeMillis();
        this.username = "pending_" + timestamp;
        this.password = "temp_" + timestamp;
    }
   

    public void computeFee() {
        // Get tuition fee based on grade level and payment plan
        if (paymentPlan != null && TUITION_FEES.containsKey(yearLevel)) {
            Map<String, Double> gradeFees = TUITION_FEES.get(yearLevel);
            tuitionFee = gradeFees.getOrDefault(paymentPlan, 0.0);
        } else {
            // Default fallback
            tuitionFee = 0.0;
        }

        // Miscellaneous fees
        if ("New".equals(studentType) || "Transferee".equals(studentType)) {
            miscFees = MISC_NEW_TRANSFEREE;
        } else {
            miscFees = MISC_OLD;
        }

        // ESC Grant discount
        escDiscount = 0;
        if ("Grade 7".equals(yearLevel) && escEligible) {
            escDiscount = ESC_GRANT;
        }

        // Calculate total
        totalAmount = tuitionFee + miscFees - escDiscount;
        if (totalAmount < 0) totalAmount = 0;
    }

    public String getFullName() {
        return firstName + (middleName != null && !middleName.isEmpty() ?
            " " + middleName + " " : " ") + lastName;
    }

    // Getters
    public String getId() { return id; }
    public String getYearLevel() { return yearLevel; }
    public int getSection() { return section; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotalAmount() { return totalAmount; }
    public String getStudentType() { return studentType; }
    public boolean isEscEligible() { return escEligible; }
    public String getPaymentPlan() { return paymentPlan; }
}

public static class Instructor implements Serializable {
    private static final long serialVersionUID = 1L;
    String instructorId, firstName, lastName, gender, email, subject;
    
    public Instructor(String id, String fn, String ln, String g, String e, String subj) {
        this.instructorId = id;
        this.firstName = fn;
        this.lastName = ln;
        this.gender = g;
        this.email = e;
        this.subject = subj;
    }
    
    public String getFullName() { 
        return firstName + " " + lastName; 
    }
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(EnrollmentSystem::new);
	}
}
