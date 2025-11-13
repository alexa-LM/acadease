
//GUYSSSSS - yung sa sections sana is parang 1st come 1st serve. may 2 sections lang per grade level. then 5 students lang per section. 
//kumbaga, halimabawa napuno na yung g7humiity, yung next enrolle sa courage na mapupunta

//SECTIONS HIERARCHY
//GRADE 7 - HUMILITY - COURAGE
//GRADE 8 - INTEGRITY - RESILIENCE
//GRADE 9 - DETERMINATION - GRATITUDE
// GRADE 10 - FORTITUDE -HONESTY

//TUITION FEE
// grade 7	₱29,000	₱30,000	₱31,200	₱33,000
//grade 8	₱29,000	₱30,000	₱31,200	₱33,000
//grade 9	₱30,000	₱31,000	₱32,400	₱34,000
//grade 10	₱31,000	₱32,000	₱33,600	₱35,000

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

    // Instructor & Assignment Management
    private List<Instructor> instructors = new ArrayList<>();
    private File instructorFile = new File("instructors.dat");
    private Map<String, String> instructorAssignments = new HashMap<>();
    private File assignmentsFile = new File("assignments.dat");
    private Map<String, Map<String, String>> schedules = new HashMap<>();
    private File schedulesFile = new File("schedules.dat");
    

    private static int instructorIdCounter = 2000;
    private static int studentIdCounter = 1000;

    // Fee Constants
    private static final double TUITION_NEW_TRANSFEREE = 15000;
    private static final double TUITION_OLD = 13000;
    private static final double MISC_NEW_TRANSFEREE = 2000;
    private static final double MISC_OLD = 1500;
    private static final double ESC_GRANT = 9000;

    // SUBJECT CONFIGURATION
    private static final Map<String, Set<String>> SECTION_SUBJECTS = new HashMap<>();
    static {
        // Grade 7
        SECTION_SUBJECTS.put("Grade 7-1", Set.of("Mathematics", "English", "Science", "Filipino", "Araling Panlipunan", "GMRC"));
        SECTION_SUBJECTS.put("Grade 7-2", Set.of("English", "Mathematics", "Filipino", "Science", "MAPEH", "GMRC"));
        // Grade 8
        SECTION_SUBJECTS.put("Grade 8-1", Set.of("Mathematics", "English", "Science", "TLE", "Araling Panlipunan", "GMRC"));
        SECTION_SUBJECTS.put("Grade 8-2", Set.of("English", "Mathematics", "Araling Panlipunan", "Science", "Filipino", "GMRC"));
        // Grade 9
        SECTION_SUBJECTS.put("Grade 9-1", Set.of("Science", "English", "Mathematics", "Araling Panlipunan", "TLE", "MAPEH"));
        SECTION_SUBJECTS.put("Grade 9-2", Set.of("English", "Science", "Araling Panlipunan", "Mathematics", "Music/Arts", "TLE"));
        // Grade 10
        SECTION_SUBJECTS.put("Grade 10-1", Set.of("Science", "Mathematics", "English", "Araling Panlipunan", "TLE", "MAPEH"));
        SECTION_SUBJECTS.put("Grade 10-2", Set.of("English", "Science", "Mathematics", "Araling Panlipunan", "TLE", "MAPEH"));
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

        mainPanel.add(mainMenuPanel(), "menu");
        mainPanel.add(enrollmentPanel(), "enroll");
        mainPanel.add(paymentPanel(), "payment");
        mainPanel.add(adminLoginPanel(), "login");
        mainPanel.add(adminDashboardPanel(), "dashboard");

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

            // holds title, subtitle, buttons, status button + admin link
            JPanel verticalBox = new JPanel();
            verticalBox.setLayout(new BoxLayout(verticalBox, BoxLayout.Y_AXIS));
            verticalBox.setBackground(panel.getBackground());

            // acadease title
            JLabel titleLabel = new JLabel("AcadEase");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
            titleLabel.setForeground(new Color(46, 109, 27));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // School Enrollment Access subtitle
            JLabel subtitleLabel = new JLabel("School Enrollment Access");
            subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 28));
            subtitleLabel.setForeground(new Color(46, 109, 27));
            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            verticalBox.add(titleLabel);
            verticalBox.add(Box.createRigidArea(new Dimension(0, 10)));
            verticalBox.add(subtitleLabel);
            verticalBox.add(Box.createRigidArea(new Dimension(0, 30))); // spacing

            // Buttons Panel (for menu)
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

            // status check
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

            // link of admin login
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

            // action listeners
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
        
        Component componentToRemove = null;
        for (int i = 0; i < mainPanel.getComponentCount(); i++) {
            
            if (storedEnrollPanel != null) {
                 mainPanel.remove(storedEnrollPanel);
            }
            storedEnrollPanel = enrollmentPanel(); 
            mainPanel.add(storedEnrollPanel, "enroll"); 
            card.show(mainPanel, "enroll");
        }
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

        // left panel - student's info
        JPanel studentInfoPanel = new JPanel(new GridBagLayout());
        studentInfoPanel.setBackground(Color.white);
        studentInfoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.gridx = 0;
        int row = 0;

        JLabel studentInfoHeader = new JLabel("Student’s Information");
        studentInfoHeader.setFont(new Font("Arial", Font.BOLD, 22));
        studentInfoHeader.setForeground(new Color(46, 109, 27));
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        studentInfoPanel.add(studentInfoHeader, gbc);
        gbc.gridwidth = 1; // reset
        Map<String, JComponent> studentFields = new HashMap<>();

        // enrol grade level
        String[] years = {"---", "Grade 7", "Grade 8", "Grade 9", "Grade 10"};
        JComboBox<String> cbYear = new JComboBox<>(years);
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, cbYear, studentFields, "yearLevel", "Grade Level to enroll");

        // student id
        final JTextField[] tfStudentIdHolder = new JTextField[1]; 
        if ("Old".equals(currentStudentType)) {
            JTextField tfStudentIdLocal = new JTextField(20);
            tfStudentIdLocal.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
            tfStudentIdLocal.setMargin(new Insets(5, 5, 5, 5)); 
            gbc.gridy = row++;
            addLabeledFieldFullWidth(studentInfoPanel, gbc, tfStudentIdLocal, studentFields, "studentId", "Student ID No.:");
            tfStudentIdHolder[0] = tfStudentIdLocal; 
        }

        // First Name
        JTextField tfFirst = new JTextField(20);
        tfFirst.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfFirst.setMargin(new Insets(5, 5, 5, 5)); // Add padding inside the text field
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, tfFirst, studentFields, "firstName", "First Name:");
        
        // Middle Name
        JTextField tfMiddle = new JTextField(20);
        tfMiddle.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfMiddle.setMargin(new Insets(5, 5, 5, 5)); // Add padding
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, tfMiddle, studentFields, "middleName", "Middle Name:");

        // Last Name
        JTextField tfLast = new JTextField(20);
        tfLast.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfLast.setMargin(new Insets(5, 5, 5, 5)); // Add padding
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, tfLast, studentFields, "lastName", "Last Name:");

        // labels for age and birthday 
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
        studentInfoPanel.add(labelsPanel, gbc);
        gbc.gridwidth = 1;

        // Age and Birthday inputs
        JPanel ageBirthdayPanel = new JPanel(new GridBagLayout());
        ageBirthdayPanel.setBackground(Color.WHITE);
        GridBagConstraints abGbc = new GridBagConstraints();
        abGbc.insets = new Insets(0, 0, 0, 5);
        abGbc.fill = GridBagConstraints.HORIZONTAL;
        abGbc.gridy = 0;

        // Age field
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

        // birthday field - jdatechooser
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
        studentInfoPanel.add(ageBirthdayPanel, gbc);
        gbc.gridwidth = 1;

        studentFields.put("age", tfAge); 
        studentFields.put("birthday", dateChooser); 
        
        // address field
        JTextField tfAddress = new JTextField(20);
        tfAddress.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfAddress.setMargin(new Insets(5, 5, 5, 5)); 
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, tfAddress, studentFields, "address", "Address:");
        
        // Email
        JTextField tfEmail = new JTextField(20);
        tfEmail.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfEmail.setMargin(new Insets(5, 5, 5, 5)); 
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, tfEmail, studentFields, "email", "Email:");

        // phone number
        JTextField tfContact = new JTextField(20);
        tfContact.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfContact.setMargin(new Insets(5, 5, 5, 5)); 
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, tfContact, studentFields, "contactNumber", "Phone Number:");

        // marital status
        String[] maritalOptions = {"---", "Single", "Married", "Divorced", "Widowed"};
        JComboBox<String> cbMarital = new JComboBox<>(maritalOptions);
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, cbMarital, studentFields, "maritalStatus", "Marital Status");

        // citizenship
        String[] citizenshipOptions = {"---", "Filipino", "American", "Chinese", "Japanese", "Korean", "Other"};
        JComboBox<String> cbCitizenship = new JComboBox<>(citizenshipOptions);
        gbc.gridx = 0;
        gbc.gridy = row++;
        addLabeledFieldFullWidth(studentInfoPanel, gbc, cbCitizenship, studentFields, "citizenship", "Citizenship");

        // citizenship - others, will only appear if 'others' is selected
        JTextField tfOtherCitizen = new JTextField(20);
        tfOtherCitizen.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfOtherCitizen.setMargin(new Insets(5, 5, 5, 5)); 
        tfOtherCitizen.setVisible(false);
        gbc.gridx = 1;
        gbc.gridy = row++; 
        studentInfoPanel.add(tfOtherCitizen, gbc);
        studentFields.put("citizenshipOther", tfOtherCitizen);

        // Show/hide 'Other' textbox
        cbCitizenship.addActionListener(e -> {
            boolean isOther = "Other".equals(cbCitizenship.getSelectedItem());
            tfOtherCitizen.setVisible(isOther);
            studentInfoPanel.revalidate();
            studentInfoPanel.repaint();
        });

        // right panel - parent's info and requirements (separate panels)
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
        JLabel parentInfoHeader = new JLabel("Parent’s Information");
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
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfMotherMaiden, parentFields, "motherMaiden", "Mother’s Maiden Name:");
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
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfFatherName, parentFields, "fatherName", "Father’s Name:");
        pg.gridy++;

        // Father's Contact & Email side-by-side
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

        // emergencey contact's name
        JTextField tfEmergencyContactName = new JTextField(20);
        tfEmergencyContactName.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfEmergencyContactName.setMargin(new Insets(5, 5, 5, 5)); // Add padding
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfEmergencyContactName, parentFields, "emergencyContactName", "Emergency Contact's Name:");
        pg.gridy++;
        
     // relation of emergency contact
        String[] relationOptions = {"---", "Mother", "Father", "Sister", "Brother", "Aunt", "Uncle", "Grandmother", "Grandfather"};
        JComboBox<String> cbEmergencyContactRelation = new JComboBox<>(relationOptions);
        addLabeledFieldFullWidth(parentInfoPanel, pg, cbEmergencyContactRelation, parentFields, "emergencyContactRelation", "Relation to Student:");
        pg.gridy++;

        // emergency contact number
        JTextField tfEmergencyContact = new JTextField(20);
        tfEmergencyContact.setBorder(BorderFactory.createLineBorder(new Color(46, 109, 27)));
        tfEmergencyContact.setMargin(new Insets(5, 5, 5, 5)); 
        addLabeledFieldFullWidth(parentInfoPanel, pg, tfEmergencyContact, parentFields, "emergencyContact", "Emergency Contact Number:");
        rightPanel.add(parentInfoPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // requirements panel (if new/transferee: psa, f137, f138, and 2x2 pic   if old: psa and f137 only)
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

        //f137
        JButton btnForm138 = new JButton("Upload Form 138 (Previous Grade Report)");
        JLabel lblForm138 = new JLabel("No file selected");
        customizeRequirementButtonAndLabel(btnForm138, lblForm138);
        btnForm138.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType)); 
        lblForm138.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType));
        requirementsPanel.add(btnForm138);
        requirementsPanel.add(lblForm138);
        requirementsPanel.add(Box.createRigidArea(new Dimension(0, 15))); 

        // 2x2 pic
        JButton btnPicture = new JButton("Upload 2x2 Picture");
        JLabel lblPicture = new JLabel("No file selected");
        customizeRequirementButtonAndLabel(btnPicture, lblPicture);
        btnPicture.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType));
        lblPicture.setVisible("New".equals(currentStudentType) || "Transferee".equals(currentStudentType));
        requirementsPanel.add(btnPicture);
        requirementsPanel.add(lblPicture);

        // scrollbar for requirement panel only
        JScrollPane requirementsScrollPane = new JScrollPane(requirementsPanel);
        requirementsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        requirementsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
        requirementsScrollPane.setPreferredSize(new Dimension(400, 200));
        rightPanel.add(requirementsScrollPane);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cGbc = new GridBagConstraints();
        cGbc.insets = new Insets(5, 5, 5, 5);
        cGbc.fill = GridBagConstraints.BOTH;
        cGbc.gridx = 0;
        cGbc.gridy = 0;
        cGbc.weightx = 0.6;
        cGbc.weighty = 1;
        centerPanel.add(studentInfoPanel, cGbc);
        cGbc.gridx = 1;
        cGbc.weightx = 0.4;
        centerPanel.add(rightPanel, cGbc);
        enrollmentMainPanel.add(centerPanel, BorderLayout.CENTER); 

        // back to menu and enroll button
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
        
        // file handlers for the new buttons
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
                // Call validateAndEnrollS - this should now correctly populate createdStudent
                validateAndEnrollS(studentFields, parentFields, birthCertPath[0], form137Path[0], form138Path[0],
                        picturePath[0], tfOtherCitizen.getText().trim(), currentStudentType);

                // Check if createdStudent is populated correctly
                if (createdStudent != null) {
                    // Set the currentStudentForPayment for the payment panel
                    currentStudentForPayment = createdStudent;
                    // Navigate to the payment panel
                    card.show(mainPanel, "payment");
                    // Update the payment panel with the new student's info
                    JPanel paymentPanelInstance = (JPanel) mainPanel.getComponent(2); // Assuming "payment" panel is at index 2
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

    private void addRequirementRow(JPanel parentPanel, String buttonText, String[] path) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS)); 
        rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); 

        // create button
        JButton btn = new JButton(buttonText);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT); 
        btn.setPreferredSize(new Dimension(300, 40)); 
        btn.setMaximumSize(new Dimension(300, 40)); 
        btn.setMinimumSize(new Dimension(300, 40)); 
        btn.setBackground(Color.WHITE); 
        btn.setBorder(BorderFactory.createLineBorder(new Color(176, 176, 184), 1)); 

        // label for file name
        JLabel lbl = new JLabel("No file selected");
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT); 
        lbl.setForeground(new Color(46, 109, 27)); 
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));

        rowPanel.add(btn);
        rowPanel.add(lbl);

        parentPanel.add(rowPanel);

        addFileHandler(btn, lbl, path);
    }

    private JButton getButtonFromPanel(JPanel panel, int index) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel rowPanel = (JPanel) comp;
                for (Component child : rowPanel.getComponents()) {
                    if (child instanceof JButton && index-- == 0) {
                        return (JButton) child;
                    }
                }
            }
        }
        return null;
    }
    
    private JLabel getLabelFromPanel(JPanel panel, int index) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel rowPanel = (JPanel) comp;
                for (Component child : rowPanel.getComponents()) {
                    if (child instanceof JLabel && index-- == 0) {
                        return (JLabel) child;
                    }
                }
            }
        }
        return null;
    }
    
	private void addField(JPanel panel, String label, JComponent field) {
        panel.add(new JLabel(label));
        panel.add(field);
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

    private void updateDocumentFieldsForType(String type, JPanel panel, JButton btnForm, JLabel lblForm, JButton btnGoodMoral, JLabel lblGoodMoral, JButton btnForm138, JLabel lblForm138, String[] formPath, String[] form138Path) {
        boolean isNewOrTransferee = "New".equals(type) || "Transferee".equals(type);
        boolean isOld = "Old".equals(type);

        btnGoodMoral.setVisible(isNewOrTransferee);
        lblGoodMoral.setVisible(isNewOrTransferee);
        btnForm138.setVisible(isOld);
        lblForm138.setVisible(isOld);

        if (isOld) {
            btnForm.setText("Upload Form 138");
            lblForm.setText("No file selected (Previous Grade Report)");
           
            btnForm.setText("Upload Form 137");
            lblForm.setText("No file selected (Previous School Report)");
        }
    }
    private Student createdStudent = null; // class member to hold created student

 // Inside the validateAndEnrollS method definition
    private void validateAndEnrollS(Map<String, JComponent> studentFields, Map<String, JComponent> parentFields,
            String bcPath, String form137Path, String form138Path, String picturePath,
            String otherCitizenship, String currentStudentType) throws Exception {
        // STUDENT'S INFO VALIDATION
        JTextField tfStudentId = (JTextField) studentFields.get("studentId");
        JTextField tfFirst = (JTextField) studentFields.get("firstName");
        JTextField tfMiddle = (JTextField) studentFields.get("middleName");
        JTextField tfLast = (JTextField) studentFields.get("lastName");
        JTextField tfAddress = (JTextField) studentFields.get("address");
        JTextField tfEmail = (JTextField) studentFields.get("email");
        JTextField tfContact = (JTextField) studentFields.get("contactNumber");
        JTextField tfAge = (JTextField) studentFields.get("age");
        // format MM-DD-YYYY
        com.toedter.calendar.JDateChooser tfBirthday = (com.toedter.calendar.JDateChooser) studentFields.get("birthday");
        java.util.Date selectedDate = tfBirthday.getDate();
        if (selectedDate == null) {
            throw new Exception("Birthday is required.");
        }
        String birthday = new java.text.SimpleDateFormat("MM-dd-yyyy").format(selectedDate);
        if (!birthday.matches("\\d{2}-\\d{2}-\\d{4}")) throw new Exception("Birthday must be in MM-DD-YYYY format.");

        // name validations
        String first = tfFirst.getText().trim();
        if (first.isEmpty()) throw new Exception("First name is required.");
        if (!first.matches("[a-zA-Z .]+")) throw new Exception("First name can contain only alphabets, spaces, and periods.");
        String middle = tfMiddle.getText().trim();
        if (!middle.isEmpty() && !middle.matches("[a-zA-Z .]+"))
            throw new Exception("Middle name can contain only alphabets, spaces, and periods.");
        String last = tfLast.getText().trim();
        if (last.isEmpty()) throw new Exception("Last name is required.");
        if (!last.matches("[a-zA-Z .]+")) throw new Exception("Last name can contain only alphabets, spaces, and periods.");

        // age
        int age;
        try {
            age = Integer.parseInt(tfAge.getText().trim());
            if (age <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new Exception("Please enter a valid age.");
        }

        // address validation
        String address = tfAddress.getText().trim();
        if (address.isEmpty()) throw new Exception("Address is required.");

        // email validation
        String email = tfEmail.getText().trim();
        if (email.isEmpty() || !email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,6}$"))
            throw new Exception("Invalid email format.");

        // contact number validation
        String contact = tfContact.getText().trim();
        if (!contact.matches("\\d{11}")) throw new Exception("Contact Number must be exactly 11 digits.");

        // marital status selection
        JComboBox<String> cbMarital = (JComboBox<String>) studentFields.get("maritalStatus");
        String maritalStatus = (String) cbMarital.getSelectedItem();

        // citizenship - if Other is selected, specify required
        JComboBox<String> cbCitizen = (JComboBox<String>) studentFields.get("citizenship");
        String citizen = (String) cbCitizen.getSelectedItem();
        if ("Other".equals(citizen)) {
            if (otherCitizenship == null || otherCitizenship.isEmpty())
                throw new Exception("Please specify your citizenship.");
            citizen = otherCitizenship;
        }

        // year level selection
        JComboBox<String> cbYear = (JComboBox<String>) studentFields.get("yearLevel");
        if (cbYear == null || cbYear.getSelectedItem() == null)
            throw new Exception("Year Level must be selected.");
        String yearLevel = (String) cbYear.getSelectedItem();

        // --- FIRST-COME, FIRST-SERVED SECTION ASSIGNMENT LOGIC ---
        int section = -1; // Initialize to an invalid section number
        String[] sectionsInGrade = getSectionsForGrade(yearLevel); // Get the specific sections for the grade

        if (sectionsInGrade != null) {
            for (String sectionName : sectionsInGrade) {
                // Extract section number from the name (e.g., "Humility" -> 1, "Courage" -> 2)
                int secNum = getSectionNumberFromName(sectionName);
                if (secNum != -1 && !isSectionFull(yearLevel, secNum)) {
                    section = secNum;
                    break; // Assign to the first available section in the hierarchy
                }
            }
        }

        if (section == -1) { // If no section was found to be available
            throw new Exception("All sections for " + yearLevel + " are full (10 students max across both sections).");
        }
        // --- END FIRST-COME, FIRST-SERVED SECTION ASSIGNMENT LOGIC ---

        // PARENT'S INFO VALIDATION
        JTextField tfMotherMaiden = (JTextField) parentFields.get("motherMaiden");
        JTextField tfMotherContact = (JTextField) parentFields.get("motherContact");
        JTextField tfMotherEmail = (JTextField) parentFields.get("motherEmail");
        JTextField tfFatherName = (JTextField) parentFields.get("fatherName");
        JTextField tfFatherContact = (JTextField) parentFields.get("fatherContact");
        JTextField tfFatherEmail = (JTextField) parentFields.get("fatherEmail");
        JTextField tfEmergencyContactName = (JTextField) parentFields.get("emergencyContactName");
        JTextField tfEmergencyContact = (JTextField) parentFields.get("emergencyContact");

        if (tfMotherMaiden.getText().trim().isEmpty()) throw new Exception("Mother's Maiden Name is required.");
        if (!tfMotherMaiden.getText().trim().matches("[a-zA-Z .]+"))
            throw new Exception("Mother's Maiden Name may contain alphabets, spaces, and periods only.");
        if (!tfMotherContact.getText().trim().matches("\\d{11}"))
            throw new Exception("Mother's Contact Number must be 11 digits.");
        String motherEmailTxt = tfMotherEmail.getText().trim();
        if (!motherEmailTxt.isEmpty() && !motherEmailTxt.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,6}$"))
            throw new Exception("Invalid Mother's Email format.");
        if (tfFatherName.getText().trim().isEmpty()) throw new Exception("Father's Name is required.");
        if (!tfFatherName.getText().trim().matches("[a-zA-Z .]+"))
            throw new Exception("Father's Name may contain alphabets, spaces, and periods only.");
        if (!tfFatherContact.getText().trim().matches("\\d{11}"))
            throw new Exception("Father's Contact Number must be 11 digits.");
        String fatherEmailTxt = tfFatherEmail.getText().trim();
        if (!fatherEmailTxt.isEmpty() && !fatherEmailTxt.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,6}$"))
            throw new Exception("Invalid Father's Email format.");
        if (tfEmergencyContactName.getText().trim().isEmpty())
            throw new Exception("Emergency Contact's Name is required.");
        if (!tfEmergencyContact.getText().trim().matches("\\d{11}"))
            throw new Exception("Emergency Contact Number is required and must be 11 digits.");

        // REQUIREMENTS VALIDATION
        List<String> missingDocs = new ArrayList<>();
        if (bcPath == null || bcPath.isEmpty()) missingDocs.add("Birth Certificate");
        if ("New".equals(currentStudentType) || "Transferee".equals(currentStudentType)) {
            if ((form137Path == null || form137Path.isEmpty()) && (form138Path == null || form138Path.isEmpty())) {
                missingDocs.add("Either Form 137 (Report Card from Previous School) or Form 138 (Previous Grade Report) is required.");
            }
        } else {
            if (form137Path == null || form137Path.isEmpty()) missingDocs.add("Form 137 (Report Card from Previous School) is required.");
        }
        if (picturePath == null || picturePath.isEmpty()) missingDocs.add("2x2 Picture");
        if (!missingDocs.isEmpty()) {
            throw new Exception("Missing required documents: " + String.join(", ", missingDocs));
        }

        // ESC Grant eligibility query if Grade 7 New/Transferee
        boolean escEligible = false;
        if ("Grade 7".equals(yearLevel) && ("New".equals(currentStudentType) || "Transferee".equals(currentStudentType))) {
            int option = JOptionPane.showConfirmDialog(null,
                    "Are you eligible for ESC Grant (Grade 7 New/Transferee Student)?",
                    "ESC Grant", JOptionPane.YES_NO_OPTION);
            escEligible = (option == JOptionPane.YES_OPTION);
        }

        String sectionKey = yearLevel + "-" + section;
        Set<String> subjects = SECTION_SUBJECTS.getOrDefault(sectionKey, new HashSet<>());
        createdStudent = new Student(
                first, middle, last,
                address, email,
                maritalStatus, citizen,
                age, yearLevel, section, // Use the dynamically assigned section
                subjects,
                contact,
                tfEmergencyContactName.getText().trim(),
                tfEmergencyContact.getText().trim(),
                bcPath,
                ("Old".equals(currentStudentType) ? form137Path : (form137Path != null && !form137Path.isEmpty()) ? form137Path : form138Path), // Choose report card path based on type and availability
                ""
        );
        createdStudent.studentType = currentStudentType;
        createdStudent.escEligible = escEligible;
        createdStudent.computeFee();
    }

    // --- HELPER METHODS FOR SECTION ASSIGNMENT ---
    // Define the section hierarchy
    private static final Map<String, String[]> GRADE_SECTIONS_HIERARCHY = new HashMap<>();
    static {
        GRADE_SECTIONS_HIERARCHY.put("Grade 7", new String[]{"Humility", "Courage"});
        GRADE_SECTIONS_HIERARCHY.put("Grade 8", new String[]{"Integrity", "Resilience"});
        GRADE_SECTIONS_HIERARCHY.put("Grade 9", new String[]{"Determination", "Gratitude"});
        GRADE_SECTIONS_HIERARCHY.put("Grade 10", new String[]{"Fortitude", "Honesty"});
    }

    private String[] getSectionsForGrade(String gradeLevel) {
        return GRADE_SECTIONS_HIERARCHY.get(gradeLevel);
    }

    private int getSectionNumberFromName(String sectionName) {
        switch (sectionName) {
            case "Humility": return 1;
            case "Courage": return 2;
            case "Integrity": return 1;
            case "Resilience": return 2;
            case "Determination": return 1;
            case "Gratitude": return 2;
            case "Fortitude": return 1;
            case "Honesty": return 2;
            default: return -1;        }
    }
    private boolean isSectionFull(String year, int section) {
        final String finalYear = year;
        final int finalSection = section;
        return (int) students.stream()
            .filter(s -> finalYear.equals(s.yearLevel) && finalSection == s.section)
            .count() >= 5;
    }

    private JPanel paymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel header = new JLabel("Payment Information", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel studentInfo = new JLabel("Student Information:");
        studentInfo.setFont(new Font("Arial", Font.BOLD, 16));
        content.add(studentInfo, gbc);

        gbc.gridy++; gbc.gridwidth = 1;
        JLabel nameLabel = new JLabel("Name:");
        content.add(nameLabel, gbc);
        gbc.gridx = 1;
        JLabel nameVal = new JLabel();
        content.add(nameVal, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel gradeLabel = new JLabel("Grade & Section:");
        content.add(gradeLabel, gbc);
        gbc.gridx = 1;
        JLabel gradeVal = new JLabel();
        content.add(gradeVal, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel subLabel = new JLabel("Enrolled Subjects:");
        subLabel.setFont(new Font("Arial", Font.BOLD, 16));
        content.add(subLabel, gbc);

        gbc.gridy++;
        JPanel subPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        subPanel.setBackground(Color.WHITE);
        subPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane subScroll = new JScrollPane(subPanel);
        subScroll.setPreferredSize(new Dimension(500, 100));
        content.add(subScroll, gbc);

        gbc.gridy++; gbc.gridwidth = 1;
        JLabel feeLabel = new JLabel("Total Amount:");
        feeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        content.add(feeLabel, gbc);
        gbc.gridx = 1;
        JLabel feeVal = new JLabel();
        feeVal.setFont(new Font("Arial", Font.BOLD, 16));
        feeVal.setForeground(new Color(0, 128, 0));
        content.add(feeVal, gbc);

        // Payment options
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JLabel payMethodLabel = new JLabel("Payment Method:");
        payMethodLabel.setFont(new Font("Arial", Font.BOLD, 16));
        content.add(payMethodLabel, gbc);

        gbc.gridy++;
        JRadioButton cashBtn = new JRadioButton("Cash Payment");
        cashBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        cashBtn.setBackground(Color.WHITE);
        cashBtn.setSelected(true);
        content.add(cashBtn, gbc);
        gbc.gridx = 1;
        JRadioButton bankBtn = new JRadioButton("Bank Transfer");
        bankBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        bankBtn.setBackground(Color.WHITE);
        content.add(bankBtn, gbc);
        ButtonGroup payGroup = new ButtonGroup();
        payGroup.add(cashBtn); payGroup.add(bankBtn);

        JPanel bankDetails = new JPanel(new GridLayout(4, 1, 2, 2));
        bankDetails.setBackground(Color.WHITE);
        bankDetails.setBorder(BorderFactory.createTitledBorder("School Bank Details"));
        bankDetails.add(new JLabel("Bank: Philippine National Bank"));
        bankDetails.add(new JLabel("Account: ABC High School"));
        bankDetails.add(new JLabel("No.: 1234-5678-9012-3456"));
        bankDetails.add(new JLabel("Branch: Main Campus"));
        bankDetails.setVisible(false);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        content.add(bankDetails, gbc);

        panel.add(content, BorderLayout.CENTER);

        // Buttons -
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton confirm = new JButton("Confirm Payment");
        JButton back = new JButton("Back to Enrollment");
        btnPanel.add(back);
        btnPanel.add(confirm);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Listeners
        cashBtn.addActionListener(e -> bankDetails.setVisible(false));
        bankBtn.addActionListener(e -> bankDetails.setVisible(true));

        confirm.addActionListener(e -> {
            if (currentStudentForPayment != null) {
                currentStudentForPayment.paymentMethod = cashBtn.isSelected() ? "Cash" : "Bank Transfer";
               
                currentStudentForPayment.paymentStatus = cashBtn.isSelected() ? "Paid in Full" : "Installment Pending"; 
                
                currentStudentForPayment.status = "Approved"; 
                students.add(currentStudentForPayment); 
                saveData();
                JOptionPane.showMessageDialog(this,
                    "Enrollment and payment confirmed successfully!" +
                    "\nYour status is now: " + currentStudentForPayment.status + 
                    "\nUsername: " + currentStudentForPayment.username +
                    "\nPassword: " + currentStudentForPayment.password +
                    "\nUse these to check your status.",
                    "Enrollment & Payment Confirmed", JOptionPane.INFORMATION_MESSAGE);
                currentStudentForPayment = null; 
                card.show(mainPanel, "menu"); 
            } else {
                JOptionPane.showMessageDialog(this, "No student data found for payment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        back.addActionListener(e -> card.show(mainPanel, "enroll"));

        // Update method
        panel.putClientProperty("updatePaymentInfo", (Runnable) () -> {
            if (currentStudentForPayment != null) {
                nameVal.setText(currentStudentForPayment.getFullName());
                gradeVal.setText(currentStudentForPayment.yearLevel + " - Section " + currentStudentForPayment.section);
                subPanel.removeAll();
                for (String sub : currentStudentForPayment.subjects) {
                    subPanel.add(new JLabel("• " + sub));
                }
                subPanel.revalidate(); subPanel.repaint();
                feeVal.setText(String.format("₱%.2f", currentStudentForPayment.totalAmount));
            }
        });

        return panel;
    }

    private void showStatusChecker() {
        String user = JOptionPane.showInputDialog(this, "Enter your username:");
        String pass = JOptionPane.showInputDialog(this, "Enter your password:");
        if (user == null || pass == null) return;

        Student s = students.stream()
            .filter(st -> user.equals(st.username) && pass.equals(st.password))
            .findFirst()
            .orElse(null);

        if (s != null) {
            StringBuilder msg = new StringBuilder("Enrollment Status: " + s.status);
            if ("Approved".equals(s.status)) {
                msg.append("You are officially enrolled!");
                msg.append("Grade: ").append(s.yearLevel);
                msg.append("Section: ").append(s.section);
            } else if ("Pending".equals(s.status)) {
                msg.append("Awaiting admin review.");
            } else if ("Declined".equals(s.status)) {
                msg.append("Your application was not accepted.");
            } else if ("For Verification".equals(s.status)) {
                msg.append("⚠Please report to school for verification.");
            }
            JOptionPane.showMessageDialog(this, msg.toString(), "Status", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel adminLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0xD0D0D0));
        panel.setLayout(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        // Title Label
        JLabel titleLabel = new JLabel("Admin Access");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.BLACK);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.gridx = 0;
        fgbc.insets = new Insets(10, 0, 10, 0);
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        Color fieldBackground = new Color(176, 214, 154);
        Color borderColor = new Color(67, 99, 50);

        // Username Label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(Color.BLACK);
        fgbc.gridy = 0;
        fgbc.insets = new Insets(1, 0, 0, 0);
        fieldsPanel.add(usernameLabel, fgbc);

        // Username Field 
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(fieldBackground);
        usernameField.setForeground(Color.BLACK);
        usernameField.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        usernameField.setPreferredSize(new Dimension(400, 40));
        fgbc.gridy = 1;
        fgbc.insets = new Insets(3, 0, 0, 0);
        fieldsPanel.add(usernameField, fgbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(Color.BLACK);
        fgbc.gridy = 2;
        fgbc.insets = new Insets(1, 0, 0, 0);
        fieldsPanel.add(passwordLabel, fgbc);

        // Password Field
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBackground(fieldBackground);
        passwordField.setForeground(Color.BLACK);
        passwordField.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        passwordField.setPreferredSize(new Dimension(400, 40));
        fgbc.gridy = 3;
        fieldsPanel.add(passwordField, fgbc);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); 
        buttonsPanel.setOpaque(false); 

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.PLAIN, 20));
        loginButton.setBackground(fieldBackground);
        loginButton.setForeground(Color.BLACK);
        loginButton.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        loginButton.setPreferredSize(new Dimension(180, 50)); 

        Color defaultBackground = fieldBackground;
        Color hoverBackground = new Color(192, 192, 192);
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(hoverBackground);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(defaultBackground);
            }
        });

        // Back to Menu Button
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

    private JPanel adminDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        panel.add(title, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Admission", buildAdmissionPanel());
        tabs.addTab("Student Info", buildStudentInformationPanel());
        tabs.addTab("Class & Section", buildClassSectionPanel());
        tabs.addTab("Reports", buildReportsPanel());
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    // ADMISSION PANEL ($1, $2, $3)

    private JPanel buildAdmissionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel metrics = new JPanel(new GridLayout(1, 3, 10, 10));
        metrics.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        metrics.add(wrapMetric(new JLabel("Total Registered: " + students.size())));
        metrics.add(wrapMetric(new JLabel("Pending: " + countByStatus("Pending"))));
        metrics.add(wrapMetric(new JLabel("Approved: " + countByStatus("Approved"))));
        panel.add(metrics, BorderLayout.NORTH);

        String[] cols = {"Name", "Email", "Year Level", "Section"};

        // EnrollmentSystem$1
        adminTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // EnrollmentSystem$2
        adminTable = new JTable(adminTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(0, 120, 215));
                    c.setForeground(Color.WHITE);
                } else if (row < students.size()) {
                    Student s = students.get(row);
                    c.setForeground(Color.BLACK);
                    if ("Hold".equals(s.status)) {
                        c.setBackground(new Color(255, 255, 0, 100));
                    } else if ("Approved".equals(s.status)) {
                        c.setBackground(new Color(0, 255, 0, 100));
                    } else if ("For Verification".equals(s.status)) {
                        c.setBackground(new Color(255, 165, 0, 100));
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

        // EnrollmentSystem$3
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                refreshStudentTable(adminTableModel);
                updateRightPanel();
            }
        });

        JPanel btns = new JPanel();
        JButton refresh = new JButton("Refresh");
        JButton logout = new JButton("Logout");
        btns.add(refresh);
        btns.add(logout);
        panel.add(btns, BorderLayout.SOUTH);
        refresh.addActionListener(e -> {
            refreshStudentTable(adminTableModel);
            updateRightPanel();
        });
        logout.addActionListener(e -> card.show(mainPanel, "menu"));

        refreshStudentTable(adminTableModel); 
        return panel;
    }

    private int countByStatus(String status) {
        return (int) students.stream().filter(s -> status.equals(s.status)).count();
    }

    // STUDENT INFORMATION PANEL ($4, $5, $6)

    private JPanel buildStudentInformationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Enrolled Students", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Last", "First", "MI"};

        // EnrollmentSystem$4
        studentInfoTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // EnrollmentSystem$5
        studentInfoTable = new JTable(studentInfoTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(0, 120, 215));
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
        rightStudentInfoPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        rightStudentInfoPanel.setPreferredSize(new Dimension(350, 400));
        updateStudentInfoRightPanel(); 
        main.add(rightStudentInfoPanel, BorderLayout.CENTER);
        panel.add(main, BorderLayout.CENTER);

        // EnrollmentSystem$6
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                refreshStudentInfoTable();
                updateStudentInfoRightPanel();
            }
        });

        JButton refresh = new JButton("Refresh");
        JPanel btns = new JPanel();
        btns.add(refresh);
        panel.add(btns, BorderLayout.SOUTH);
        refresh.addActionListener(e -> {
            refreshStudentInfoTable();
            updateStudentInfoRightPanel();
        });

        refreshStudentInfoTable(); 
        return panel;
    }

    // CLASS & SECTION PANEL ($7, $8, $9, $10)

    private JPanel buildClassSectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        List<Student> accepted = getStudentsByStatus("Approved");
        JPanel header = new JPanel(new GridLayout(1, 5, 8, 8));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (String grade : Arrays.asList("Grade 7", "Grade 8", "Grade 9", "Grade 10")) {
            int count = (int) accepted.stream().filter(s -> grade.equals(s.yearLevel)).count();
            header.add(wrapMetric(new JLabel(grade + ": " + count)));
        }
        header.add(wrapMetric(new JLabel("Total: " + accepted.size())));
        panel.add(header, BorderLayout.NORTH);

        String[] cols1 = {"Class & Section", "Students"};

        // EnrollmentSystem$7
        classSectionTableModel = new DefaultTableModel(cols1, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // EnrollmentSystem$8
        classSectionTable = new JTable(classSectionTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return c;
            }
        };
        classSectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classSectionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedClassStudentIndex = classSectionTable.getSelectedRow();
                String sel = selectedClassStudentIndex >= 0 ?
                    (String) classSectionTableModel.getValueAt(selectedClassStudentIndex, 0) : null;
                refreshSectionStudentTable(sel);
                updateClassRightPanel();
            }
        });

        JScrollPane leftScroll = new JScrollPane(classSectionTable);
        leftScroll.setPreferredSize(new Dimension(250, 400));
        leftScroll.setBorder(BorderFactory.createTitledBorder("Classes"));

        String[] cols2 = {"ID", "Name", "Status", "Payment Status"};

        // EnrollmentSystem$9
        sectionStudentTableModel = new DefaultTableModel(cols2, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // EnrollmentSystem$10
        sectionStudentTable = new JTable(sectionStudentTableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return c;
            }
        };
        sectionStudentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionStudentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedSectionStudentIndex = sectionStudentTable.getSelectedRow();
                changeSectionBtn.setEnabled(selectedSectionStudentIndex >= 0);
            }
        });

        JScrollPane midScroll = new JScrollPane(sectionStudentTable);
        midScroll.setPreferredSize(new Dimension(400, 350));
        JPanel mid = new JPanel(new BorderLayout());
        mid.add(midScroll, BorderLayout.CENTER);
        changeSectionBtn = new JButton("Change Section");
        changeSectionBtn.setEnabled(false);
        changeSectionBtn.addActionListener(e -> changeSectionDialog());
        mid.add(changeSectionBtn, BorderLayout.SOUTH);
        mid.setBorder(BorderFactory.createTitledBorder("Students in Section"));

        rightClassDetailPanel = new JPanel(new BorderLayout());
        rightClassDetailPanel.setBorder(BorderFactory.createTitledBorder("Section Details"));
        rightClassDetailPanel.setPreferredSize(new Dimension(400, 400));
        updateClassRightPanel(); 

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mid, rightClassDetailPanel);
        rightSplit.setResizeWeight(0.6);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightSplit);
        mainSplit.setResizeWeight(0.25);
        panel.add(mainSplit, BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JButton logout = new JButton("Logout");
        JPanel btns = new JPanel();
        btns.add(refresh);
        btns.add(logout);
        panel.add(btns, BorderLayout.SOUTH);
        refresh.addActionListener(e -> {
            refreshClassSectionTable();
            String sel = classSectionTable.getSelectedRow() >= 0 ?
                (String) classSectionTableModel.getValueAt(classSectionTable.getSelectedRow(), 0) : null;
            refreshSectionStudentTable(sel);
            updateClassRightPanel();
        });
        logout.addActionListener(e -> card.show(mainPanel, "menu"));

        refreshClassSectionTable();
        return panel;
    }

    // REPORTS PANEL ($11)

    private JTabbedPane buildReportsPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Section List", buildSectionListReport());
        tabs.addTab("Financial", buildFinancialReport());
        tabs.addTab("Statistics", buildStatisticalReport());
        return tabs;
    }

    private JPanel buildSectionListReport() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Section List Report", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Grade Level", "Section", "Students", "Instructor", "Has Schedule"};

        // EnrollmentSystem$11
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        refreshSectionListReport(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JButton export = new JButton("Export");
        refresh.addActionListener(e -> refreshSectionListReport(model));
        export.addActionListener(e -> exportSectionListReport(model));
        JPanel btns = new JPanel();
        btns.add(refresh);
        btns.add(export);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshSectionListReport(DefaultTableModel model) {
        model.setRowCount(0);
        Set<String> sections = new HashSet<>();
        for (Student s : students) {
            sections.add(s.yearLevel + "#Section " + s.section);
        }
        for (String key : sections) {
            String[] parts = key.split("#");
            String grade = parts[0];
            String sec = parts[1];
            int count = (int) students.stream().filter(s -> (s.yearLevel + "#Section " + s.section).equals(key)).count();
            String inst = instructorAssignments.getOrDefault(key, "Not Assigned");
            if (!"Not Assigned".equals(inst)) {
                for (Instructor i : instructors) {
                    if (i.instructorId.equals(inst)) {
                        inst = i.firstName + " " + i.lastName;
                        break;
                    }
                }
            }
            boolean hasSched = schedules.containsKey(key) && !schedules.get(key).isEmpty();
            model.addRow(new Object[]{grade, sec, count, inst, hasSched ? "Yes" : "No"});
        }
    }

    private void exportSectionListReport(DefaultTableModel model) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("section_list_report.txt"))) {
            writer.println("ACADEASE SECTION LIST REPORT");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=".repeat(60));
            writer.println();
            writer.printf("%-15s %-15s %-15s %-25s %-15s%n", 
                "Grade Level", "Section", "Total Students", "Instructor", "Has Schedule");
            writer.println("-".repeat(85));
            for (int i = 0; i < model.getRowCount(); i++) {
                writer.printf("%-15s %-15s %-15s %-25s %-15s%n",
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
        JLabel headerLabel = new JLabel("Financial Report", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(headerLabel, BorderLayout.NORTH);

        Map<String, Object> financialData = calculateFinancialMetrics();
        JPanel contentPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel summaryPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Payment Summary"));
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
        methodPanel.add(new JLabel("Cash Payments:"));
        methodPanel.add(new JLabel(financialData.get("cashPayments").toString()));
        methodPanel.add(new JLabel("Bank Transfers:"));
        methodPanel.add(new JLabel(financialData.get("bankTransfers").toString()));
        contentPanel.add(methodPanel);

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export Financial Report");
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

        for (Student s : students) {
            if ("Paid in Full".equals(s.paymentStatus)) {
                paidInFull++;
                totalRevenue += s.totalAmount;
            } else if ("Installment".equals(s.paymentStatus)) {
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
        }

        metrics.put("totalStudents", totalStudents);
        metrics.put("paidInFull", paidInFull);
        metrics.put("installmentPlans", installmentPlans);
        metrics.put("unpaid", unpaid);
        metrics.put("cashPayments", cashPayments);
        metrics.put("bankTransfers", bankTransfers);
        metrics.put("totalRevenue", totalRevenue);
        metrics.put("escTotal", escTotal);
        return metrics;
    }

    private void exportFinancialReport(Map<String, Object> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("financial_report.txt"))) {
            writer.println("ACADEASE FINANCIAL REPORT");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("=" + "=".repeat(50));
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
            JOptionPane.showMessageDialog(this, "Financial report exported to financial_report.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    private JPanel buildStatisticalReport() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel headerLabel = new JLabel("Statistical Report", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(headerLabel, BorderLayout.NORTH);

        Map<String, Object> stats = calculateStatistics();
        JPanel contentPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel enrollmentPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        enrollmentPanel.setBorder(BorderFactory.createTitledBorder("Enrollment Statistics"));
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
        for (Map.Entry<String, Integer> entry : gradeDistribution.entrySet()) {
            gradePanel.add(new JLabel(entry.getKey() + ":"));
            gradePanel.add(new JLabel(entry.getValue().toString()));
        }
        contentPanel.add(gradePanel);

        @SuppressWarnings("unchecked")
        Map<String, Integer> maritalStatus = (Map<String, Integer>) stats.get("maritalStatus");
        JPanel demoPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        demoPanel.setBorder(BorderFactory.createTitledBorder("Demographics"));
        for (Map.Entry<String, Integer> entry : maritalStatus.entrySet()) {
            demoPanel.add(new JLabel(entry.getKey() + ":"));
            demoPanel.add(new JLabel(entry.getValue().toString()));
        }
        contentPanel.add(demoPanel);

        panel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export Statistics");
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
            writer.println("=" + "=".repeat(50));
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

    // UTILITIES

    private JPanel wrapMetric(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void refreshStudentTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Student s : students) {
            if (!"Approved".equals(s.status)) {
                model.addRow(new Object[]{
                    s.getFullName(), s.emailAddress, s.yearLevel, s.section
                });
            }
        }
    }

    private void updateRightPanel() {
        rightDetailPanel.removeAll();
        if (selectedStudentIndex >= 0 && selectedStudentIndex < students.size()) {
            Student s = students.get(selectedStudentIndex);
            JPanel details = new JPanel(new GridLayout(0, 2, 5, 5));
            details.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            addDetail(details, "Status", s.status);
            addDetail(details, "Name", s.getFullName());
            addDetail(details, "Age", String.valueOf(s.age));
            addDetail(details, "Year & Section", s.yearLevel + " - Section " + s.section);
            addDetail(details, "Type", s.studentType);
            addDetail(details, "ESC Eligible", String.valueOf(s.escEligible));
            addDetail(details, "Total Fee", String.format("₱%.2f", s.totalAmount));
            rightDetailPanel.add(new JScrollPane(details), BorderLayout.CENTER);

            JPanel actions = new JPanel();
            JButton approve = new JButton("Approve");
            JButton decline = new JButton("Decline");
            JButton verify = new JButton("For Verification");

            approve.addActionListener(e -> {
                s.status = "Approved";
                s.paymentStatus = "Paid in Full";
                saveData();
                updateRightPanel();
                JOptionPane.showMessageDialog(this, "Student approved.");
            });
            decline.addActionListener(e -> {
                s.status = "Declined";
                saveData();
                updateRightPanel();
                JOptionPane.showMessageDialog(this, "Student declined.");
            });
            verify.addActionListener(e -> {
                s.status = "For Verification";
                saveData();
                updateRightPanel();
                JOptionPane.showMessageDialog(this, "Student marked for verification.");
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

    private void addDetail(JPanel panel, String label, String value) {
        panel.add(new JLabel(label + ":"));
        panel.add(new JLabel(value));
    }

    private void refreshStudentInfoTable() {
        if (studentInfoTableModel == null) return;
        studentInfoTableModel.setRowCount(0);
        for (Student s : getStudentsByStatus("Approved")) {
            String mi = (s.middleName != null && !s.middleName.isEmpty()) ?
                s.middleName.substring(0, 1).toUpperCase() + "." : "";
            studentInfoTableModel.addRow(new Object[]{
                s.id, s.lastName, s.firstName, mi
            });
        }
    }

    private void updateStudentInfoRightPanel() {
        rightStudentInfoPanel.removeAll();
        if (selectedStudentInfoIndex >= 0) {
            List<Student> approved = getStudentsByStatus("Approved");
            if (selectedStudentInfoIndex < approved.size()) {
                Student s = approved.get(selectedStudentInfoIndex);
                rightStudentInfoPanel.add(new JLabel("Details for: " + s.getFullName()));
            }
        }
        rightStudentInfoPanel.revalidate();
        rightStudentInfoPanel.repaint();
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
            for (int sec = 1; sec <= 2; sec++) {
                final String finalGrade = grade;
                final int finalSec = sec;
                int count = (int) students.stream()
                    .filter(s -> finalGrade.equals(s.yearLevel) && finalSec == s.section && "Approved".equals(s.status))
                    .count();
                classSectionTableModel.addRow(new Object[]{grade + " - Section " + sec, count + " students"});
            }
        }
    }

    private void refreshSectionStudentTable(String selected) {
        if (sectionStudentTableModel == null || selected == null) return;
        sectionStudentTableModel.setRowCount(0);
        String[] parts = selected.split(" - Section ");
        if (parts.length != 2) return;
        String grade = parts[0];
        int sec;
        try { sec = Integer.parseInt(parts[1]); } catch (Exception e) { return; }
        final String finalGrade = grade;
        final int finalSec = sec;
        for (Student s : students) {
            if (finalGrade.equals(s.yearLevel) && finalSec == s.section && "Approved".equals(s.status)) {
                sectionStudentTableModel.addRow(new Object[]{
                    s.id, s.getFullName(), s.status, s.paymentStatus
                });
            }
        }
    }

    private void changeSectionDialog() {
        JOptionPane.showMessageDialog(this, "Change section dialog ready.");
    }

    // SCHEDULE DISPLAY ($12)
    private void updateClassRightPanel() {
        rightClassDetailPanel.removeAll();
        if (selectedClassStudentIndex >= 0) {
            String sectionInfo = (String) classSectionTableModel.getValueAt(selectedClassStudentIndex, 0);
            rightClassDetailPanel.add(new JLabel("Managing: " + sectionInfo));

            // Display official schedule based on sectionInfo
            String[] parts = sectionInfo.split(" - Section ");
            if (parts.length == 2) {
                String grade = parts[0];
                String secNum = parts[1];
                String key = grade + "-" + secNum;

                // retrieving schedule for a section
                String[] scheduleData = SECTION_SCHEDULES.get(key);
                if (scheduleData != null) {
                    JPanel schedulePanel = new JPanel(new GridLayout(0, 1, 2, 2));
                    schedulePanel.setBorder(BorderFactory.createTitledBorder("Official Daily Schedule"));

                    for (String entry : scheduleData) {
                        JLabel timeLabel = new JLabel(entry);
                        timeLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        schedulePanel.add(timeLabel);
                    }

                    rightClassDetailPanel.add(new JScrollPane(schedulePanel), BorderLayout.CENTER);
                } else {
                    rightClassDetailPanel.add(new JLabel("No schedule found for this section."), BorderLayout.CENTER);
                }
            } else {
                rightClassDetailPanel.add(new JLabel("Invalid section format."), BorderLayout.CENTER);
            }

            JButton editScheduleBtn = new JButton("Edit Schedule (Disabled)");
            editScheduleBtn.setEnabled(false);
            rightClassDetailPanel.add(editScheduleBtn, BorderLayout.SOUTH);
        } else {
            rightClassDetailPanel.add(new JLabel("Select a section", JLabel.CENTER));
        }
        rightClassDetailPanel.revalidate();
        rightClassDetailPanel.repaint();
    }

    private static final Map<String, String[]> SECTION_SCHEDULES = new HashMap<>();
    static {
        SECTION_SCHEDULES.put("Grade 7-1", new String[]{
            "8:00-9:00 | Mathematics",
            "9:00-10:00 | English",
            "10:00-10:20 | Recess",
            "10:20-11:20 | Science",
            "11:20-12:20 | Filipino",
            "12:20-1:20 | Lunch",
            "1:20-2:20 | Araling Panlipunan",
            "2:20-3:00 | GMRC",
            "3:00-4:00 | Homework / Study Period"
        });
        SECTION_SCHEDULES.put("Grade 7-2", new String[]{
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
        SECTION_SCHEDULES.put("Grade 8-1", new String[]{
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
        SECTION_SCHEDULES.put("Grade 8-2", new String[]{
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
        SECTION_SCHEDULES.put("Grade 9-1", new String[]{
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
        SECTION_SCHEDULES.put("Grade 9-2", new String[]{
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
        SECTION_SCHEDULES.put("Grade 10-1", new String[]{
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
        SECTION_SCHEDULES.put("Grade 10-2", new String[]{
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
    }

    // PERSISTENCE
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
    private void createSampleData() {
        this.students = new ArrayList<>();

        // sample data - 7-Humility
        Set<String> g7Sec1Subjects = SECTION_SUBJECTS.getOrDefault("Grade 7-1", new HashSet<>());
        // sample data - 7-courage
        Set<String> g7Sec2Subjects = SECTION_SUBJECTS.getOrDefault("Grade 7-2", new HashSet<>());
        // sample data - 8-integrity
        Set<String> g8Sec1Subjects = SECTION_SUBJECTS.getOrDefault("Grade 8-1", new HashSet<>());
        // sample data - 9-gratitude
        Set<String> g9Sec2Subjects = SECTION_SUBJECTS.getOrDefault("Grade 9-2", new HashSet<>());
        // sample data - 10-fortitude
        Set<String> g10Sec1Subjects = SECTION_SUBJECTS.getOrDefault("Grade 10-1", new HashSet<>());

        // Student 1: New, Grade 7, Section 1, ESC Eligible
        Student s1 = new Student(
            "Juan", "Dela Cruz", "Reyes",
            "123 Oak Street", "juan.dela@example.com",
            "Single", "Filipino", 12, "Grade 7", 1, g7Sec1Subjects,
            "09123456789", "Maria Dela Cruz", "09876543210",
            "birth_cert_s1.pdf", "form137_s1.pdf", "good_moral_s1.pdf"
        );
        s1.studentType = "New";
        s1.escEligible = true; 
        s1.status = "Approved";
        s1.paymentStatus = "Paid in Full";
        s1.computeFee(); 

        // Student 2: Transferee, Grade 8, Section 1
        Student s2 = new Student(
            "Ana", "Maganda", "Santos",
            "456 Pine Avenue", "ana.maganda@example.com",
            "Single", "Filipino", 13, "Grade 8", 1, g8Sec1Subjects,
            "09234567890", "Pedro Santos", "09765432109",
            "birth_cert_s2.pdf", "form137_s2.pdf", "good_moral_s2.pdf"
        );
        s2.studentType = "Transferee";
        s2.status = "Pending";
        s2.paymentStatus = "Unpaid";
        s2.computeFee();

        // Student 3: Old, Grade 9, Section 2
        Student s3 = new Student(
            "Ramon", "Bautista", "Garcia",
            "789 Maple Drive", "ramon.bautista@example.com",
            "Single", "Filipino", 14, "Grade 9", 2, g9Sec2Subjects,
            "09345678901", "Lourdes Garcia", "09654321098",
            "birth_cert_s3.pdf", "form138_s3.pdf", "" 
        );
        s3.studentType = "Old";
        s3.status = "Declined";
        s3.paymentStatus = "Unpaid";
        s3.computeFee();

        // Student 4: New, Grade 10, Section 1
        Student s4 = new Student(
            "Maria", "Clara", "Lopez",
            "321 Elm Blvd", "maria.lopez@example.com",
            "Single", "Filipino", 15, "Grade 10", 1, g10Sec1Subjects,
            "09456789012", "Carlos Lopez", "09543210987",
            "birth_cert_s4.pdf", "form137_s4.pdf", "good_moral_s4.pdf"
        );
        s4.studentType = "New";
        s4.escEligible = false; 
        s4.status = "Approved";
        s4.paymentStatus = "Installment";
        s4.installmentPlan = "Monthly";
        s4.computeFee();

        // Student 5: Old, Grade 7, Section 2 (Example of Old in Grade 7)
        Student s5 = new Student(
            "Lito", "", "Santos",
            "654 Cedar Lane", "lito.santos@example.com",
            "Single", "Filipino", 12, "Grade 7", 2, g7Sec2Subjects,
            "09567890123", "Lolita Santos", "09432109876",
            "birth_cert_s5.pdf", "form138_s5.pdf", "" 
        );
        s5.studentType = "Old";
        s5.status = "Approved";
        s5.paymentStatus = "Paid in Full";
        s5.computeFee();

        students.add(s1);
        students.add(s2);
        students.add(s3);
        students.add(s4);
        students.add(s5);
        saveData();

        for (Student s : students) {
            try {
                String num = s.id.replaceAll("[^0-9]", "");
                if (!num.isEmpty()) {
                    int val = Integer.parseInt(num);
                    studentIdCounter = Math.max(studentIdCounter, val);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        System.out.println("Sample data created successfully with " + students.size() + " students.");
    }
    
    private void loadData() {
        if (dataFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
                Object obj = ois.readObject();
                if (obj instanceof List) {
                    students = (List<Student>) obj;
                    System.out.println("Data loaded successfully: " + students.size() + " students");
                    for (Student s : students) {
                        String num = s.id.replaceAll("[^0-9]", "");
                        try {
                            int val = Integer.parseInt(num);
                            studentIdCounter = Math.max(studentIdCounter, val);
                        } catch (Exception ignored) {}
                        if (s.enrolledAt == 0L) {
                            s.enrolledAt = System.currentTimeMillis();
                        }
                        if (s.status == null || s.status.isEmpty()) {
                            s.status = "Pending";
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
                // Update counter
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
        protected String paymentStatus = "Unpaid", paymentMethod, installmentPlan, bankReferenceNumber;
        protected double tuitionFee, miscFees, escDiscount, totalAmount;

        public Student(String fn, String mn, String ln, String addr, String email,
                       String marital, String citizen, int age, String yl, int sec,
                       Collection<String> subs, String contact, String emergName, // Add emergName parameter
                       String emergPhone, String bc, String reportCard, String goodMoral) { // Updated parameter names
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
            this.id = generateID(yl);
            this.username = "std_" + this.id;
            this.password = "pass_" + this.id;
        }
        private String generateID(String grade) {
            return "S" + System.currentTimeMillis() % 100000 + grade.substring(6);
        }

        public void computeFee() {
            if ("New".equals(studentType) || "Transferee".equals(studentType)) {
                tuitionFee = TUITION_NEW_TRANSFEREE;
                miscFees = MISC_NEW_TRANSFEREE;
            } else { 
                tuitionFee = TUITION_OLD;
                miscFees = MISC_OLD;
            }
            escDiscount = 0;
            if ("Grade 7".equals(yearLevel) && escEligible) {
                escDiscount = ESC_GRANT;
            }
            totalAmount = tuitionFee + miscFees - escDiscount;
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
        public String getFullName() { return firstName + " " + lastName; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnrollmentSystem::new);
    }
}