package RMI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.io.*;

public class ClientGUI extends JFrame {
    // Các field chính
    private StudentManager manager;
    private JTable studentTable;
    private DefaultTableModel studentModel;
    private JTable moduleTable;
    private DefaultTableModel moduleModel;
    private JTable scoreTable;
    private DefaultTableModel scoreModel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceModel;
    private JComboBox<String> moduleComboBox;
    private final Set<String> localModules = new LinkedHashSet<>();
    private JLabel footerLabel;
    private JPanel contentPanel;
    private JPanel studentBottom; // To manage the bottom panel for students

    // Authentication
    private static final Properties credentials = new Properties();
    private static final File CREDENTIALS_FILE = new File("users.properties");
    private static String currentUser = null;

    // Colors (theme: orange, light-blue, white)
    // Theme: header orange, sidebar navy, functional buttons light-blue, soft accents
    private static final Color LIGHT_BLUE = new Color(225, 243, 250); // very pale blue (background accents)
    private static final Color BG = LIGHT_BLUE;
    private static final Color HEADER_BG = new Color(255, 159, 28); // vivid but warm orange for header
    private static final Color SIDEBAR_BG = new Color(16, 43, 60); // deep blue-green for sidebar background
    private static final Color SIDEBAR_BASE = new Color(20, 55, 75); // slightly lighter for menu items base
    private static final Color CARD = Color.WHITE; // card/foreground background
    private static final Color FUNC_BTN = new Color(100, 170, 235); // pleasant medium blue for functional buttons
    private static final Color ADD_COLOR = FUNC_BTN; // primary action (blue)
    private static final Color EDIT_COLOR = FUNC_BTN; // edit actions (blue)
    private static final Color VIEW_COLOR = FUNC_BTN; // view action (blue)
    private static final Color MODULE_ADD = FUNC_BTN;
    private static final Color PRIMARY = FUNC_BTN; // keep PRIMARY mapped to functional color for buttons
    private static final Color DELETE_COLOR = FUNC_BTN; // delete actions also blue per request
    private static final Color TEXT = new Color(33, 37, 41); // dark text for readability
    private static final Color TABLE_HEADER_BG = new Color(46, 139, 87); // green (xanh lá) for table headers
    private static final Color ALT_ROW = new Color(250, 250, 252); // near-white alternate rows

    // Small utility to produce a subtly lighter version of a color
    private static Color light(Color c, double factor) {
        int r = (int) Math.min(255, c.getRed() * factor);
        int g = (int) Math.min(255, c.getGreen() * factor);
        int b = (int) Math.min(255, c.getBlue() * factor);
        return new Color(r, g, b);
    }

    // Small utility to produce a slightly darker / dimmed version of a color
    private static Color darken(Color c, double factor) {
        int r = (int) Math.max(0, c.getRed() * factor);
        int g = (int) Math.max(0, c.getGreen() * factor);
        int b = (int) Math.max(0, c.getBlue() * factor);
        return new Color(r, g, b);
    }
    
    
    // Rounded border for nicer inputs and buttons
    private static class RoundedBorder implements Border {
        private final int radius;
        private final Color color;
        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius, radius/2, radius);
        }

        @Override
        public boolean isBorderOpaque() { return false; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    // Helpers to style input components consistently
    private static void styleTextField(JTextField tf) {
        tf.setBackground(Color.WHITE);
        tf.setOpaque(true);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // reduce vertical insets to avoid clipping of descenders and increase preferred height
        tf.setBorder(new CompoundBorder(new RoundedBorder(8, new Color(200, 200, 200)), new EmptyBorder(6, 12, 6, 12)));
        tf.setMargin(new Insets(6, 12, 6, 12));
        tf.setPreferredSize(new Dimension(380, 40));
    }

    private static void stylePasswordField(JPasswordField pf) {
        pf.setBackground(Color.WHITE);
        pf.setOpaque(true);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // reduce vertical insets and increase height to avoid character clipping
        pf.setBorder(new CompoundBorder(new RoundedBorder(8, new Color(200, 200, 200)), new EmptyBorder(6, 12, 6, 12)));
        pf.setMargin(new Insets(6, 12, 6, 12));
        pf.setPreferredSize(new Dimension(380, 40));
    }

    private static void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(Color.WHITE);
        cb.setOpaque(true);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(new CompoundBorder(new RoundedBorder(8, new Color(200, 200, 200)), new EmptyBorder(2, 6, 2, 6)));
        cb.setPreferredSize(new Dimension(220, 28));
    }
    // Constants
    private static final int ATTENDANCE_THRESHOLD = 5; 
    private static final double GPA_WEIGHT_TEST1 = 0.3;  
    private static final double GPA_WEIGHT_EXAM = 0.7;   
    private static final double ATTENDANCE_PENALTY = 0.5; 

    // Định nghĩa interface StudentManager
    interface StudentManager {
        List<Student> getAllStudents();
        void addStudent(Student s);
        void updateStudent(Student s);
        void deleteStudent(String id);
        Student getStudentById(String id);
        List<Student> searchStudents(String keyword);
        List<String> getAllModules();
        void addModule(String module);
        void deleteModule(String module);
        List<Score> getScoresByModule(String module);
        Map<String, Integer> getAttendanceByStudent(String studentId);
        List<Score> getAllScoresForStudent(String studentId);
        void updateScore(String studentId, String fullName, String module, int attendance, int test1, int exam);
    }

    // Định nghĩa class Student
    static class Student {
        private String id, fullName, clazz, hometown;
        private int birthYear;
        private double gpa;
        private String note;

        public Student(String id, String fullName, String clazz, int birthYear, String hometown) {
            this.id = id;
            this.fullName = fullName;
            this.clazz = clazz;
            this.birthYear = birthYear;
            this.hometown = hometown;
            this.gpa = 0.0;
            this.note = "";
        }

        public String getId() { return id; }
        public String getFullName() { return fullName; }
        public String getClazz() { return clazz; }
        public int getBirthYear() { return birthYear; }
        public String getHometown() { return hometown; }
        public double getGpa() { return gpa; }
        public String getNote() { return note; }

        public void setFullName(String fullName) { this.fullName = fullName; }
        public void setClazz(String clazz) { this.clazz = clazz; }
        public void setBirthYear(int birthYear) { this.birthYear = birthYear; }
        public void setHometown(String hometown) { this.hometown = hometown; }
        public void setGpa(double gpa) { this.gpa = gpa; }
        public void setNote(String note) { this.note = note; }
    }

    // Định nghĩa class Score
    static class Score {
        private String studentId, fullName, module, semester, academicYear;
        private int attendance, test1, exam;

        public Score(String studentId, String fullName, String module, String semester, String academicYear, int attendance, int test1, int exam) {
            this.studentId = studentId;
            this.fullName = fullName;
            this.module = module;
            this.semester = semester;
            this.academicYear = academicYear;
            this.attendance = attendance;
            this.test1 = test1;
            this.exam = exam;
        }

        public String getStudentId() { return studentId; }
        public String getFullName() { return fullName; }
        public String getModule() { return module; }
        public String getSemester() { return semester; }
        public String getAcademicYear() { return academicYear; }
        public int getAttendance() { return attendance; }
        public int getTest1() { return test1; }
        public int getExam() { return exam; }

        public double calculateModuleGrade() {
            return (test1 * GPA_WEIGHT_TEST1 + exam * GPA_WEIGHT_EXAM) - (attendance * ATTENDANCE_PENALTY);
        }
    }

    // Mock StudentManager
    static class MockStudentManager implements StudentManager {
        private List<Student> students = new ArrayList<>();
        private List<String> modules = new ArrayList<>();
        private Map<String, Integer> moduleCredits = new HashMap<>();
        private List<Score> scores = new ArrayList<>();
        private Map<String, Integer> attendanceMap = new HashMap<>();

        public MockStudentManager() {
            Student s1 = new Student("SV001", "Nguyễn Văn A", "CTK43", 2000, "Hà Nội");
            s1.setGpa(7.5);
            s1.setNote("Tốt");
            students.add(s1);

            Student s2 = new Student("SV002", "Trần Thị B", "CTK44", 2001, "TP.HCM");
            s2.setGpa(8.2);
            s2.setNote("Cảnh báo chuyên cần");
            students.add(s2);

            // Additional mock students for testing
            Student s3 = new Student("SV003", "Lê Văn C", "CTK43", 1999, "Đà Nẵng");
            s3.setGpa(6.8);
            s3.setNote("Khá");
            students.add(s3);

            Student s4 = new Student("SV004", "Phan Thị D", "CTK45", 2002, "Hải Phòng");
            s4.setGpa(9.0);
            s4.setNote("Xuất sắc");
            students.add(s4);

            Student s5 = new Student("SV005", "Ngô Văn E", "CTK44", 2000, "Cần Thơ");
            s5.setGpa(7.2);
            s5.setNote("");
            students.add(s5);

            Student s6 = new Student("SV006", "Hoàng Thị F", "CTK45", 2001, "Thanh Hóa");
            s6.setGpa(5.9);
            s6.setNote("Cần cải thiện chuyên cần");
            students.add(s6);

            Student s7 = new Student("SV007", "Đặng Văn G", "CTK43", 1998, "Quảng Ninh");
            s7.setGpa(8.5);
            s7.setNote("");
            students.add(s7);

            Student s8 = new Student("SV008", "Vũ Thị H", "CTK44", 2000, "Bình Dương");
            s8.setGpa(6.4);
            s8.setNote("");
            students.add(s8);

            Student s9 = new Student("SV009", "Trịnh Văn I", "CTK45", 1997, "Nghệ An");
            s9.setGpa(7.8);
            s9.setNote("");
            students.add(s9);

            Student s10 = new Student("SV010", "Bùi Thị K", "CTK43", 2002, "Phú Thọ");
            s10.setGpa(8.1);
            s10.setNote("");
            students.add(s10);

            modules.add("Mạng máy tính");
            moduleCredits.put("Lập Trình Mạng", 3);
            modules.add("Lập trình Mobile");
            moduleCredits.put("Cơ sở dữ liệu", 4);
            modules.add("Cơ sở dữ liệu");
            moduleCredits.put("Lập Trình C++", 3);
            modules.add("Toán cao cấp");
            moduleCredits.put("Mạng máy tính", 4);

            scores.add(new Score("SV001", "Nguyễn Văn A", "Toán cao cấp", "HK1", "2025-2026", 2, 8, 7));
            scores.add(new Score("SV001", "Phạm Văn D", "Lập trình Mobile", "HK2", "2025-2026", 1, 9, 8));
            scores.add(new Score("SV002", "Trần Thị B", "Toán cao cấp", "HK1", "2025-2026", 6, 7, 6));
            scores.add(new Score("SV002", "Trần Thu Huyền", "Cơ sở dữ liệu", "HK2", "2025-2026", 3, 8, 9));

            // Scores for additional mock students
            scores.add(new Score("SV003", "Lê Văn C", "Toán cao cấp", "HK1", "2025-2026", 1, 6, 5));
            scores.add(new Score("SV003", "Lê Văn C", "Lập trình Mobile", "HK2", "2025-2026", 0, 7, 6));

            scores.add(new Score("SV004", "Phan Thị D", "Cơ sở dữ liệu", "HK1", "2025-2026", 0, 9, 9));
            scores.add(new Score("SV004", "Phan Thị D", "Mạng máy tính", "HK2", "2025-2026", 2, 8, 9));

            scores.add(new Score("SV005", "Ngô Văn E", "Toán cao cấp", "HK1", "2025-2026", 3, 7, 6));
            scores.add(new Score("SV005", "Ngô Văn E", "Cơ sở dữ liệu", "HK2", "2025-2026", 1, 6, 7));

            scores.add(new Score("SV006", "Hoàng Thị F", "Lập trình Mobile", "HK1", "2025-2026", 5, 5, 4));
            scores.add(new Score("SV006", "Hoàng Thị F", "Mạng máy tính", "HK2", "2025-2026", 6, 6, 5));

            scores.add(new Score("SV007", "Đặng Văn G", "Toán cao cấp", "HK1", "2025-2026", 0, 8, 8));
            scores.add(new Score("SV007", "Đặng Văn G", "Lập trình Mobile", "HK2", "2025-2026", 1, 9, 9));

            scores.add(new Score("SV008", "Vũ Thị H", "Cơ sở dữ liệu", "HK1", "2025-2026", 2, 6, 6));
            scores.add(new Score("SV008", "Vũ Thị H", "Mạng máy tính", "HK2", "2025-2026", 0, 7, 6));

            scores.add(new Score("SV009", "Trịnh Văn I", "Toán cao cấp", "HK1", "2025-2026", 1, 8, 7));
            scores.add(new Score("SV009", "Trịnh Văn I", "Cơ sở dữ liệu", "HK2", "2025-2026", 2, 7, 8));

            scores.add(new Score("SV010", "Bùi Thị K", "Lập trình Mobile", "HK1", "2025-2026", 0, 9, 8));
            scores.add(new Score("SV010", "Bùi Thị K", "Mạng máy tính", "HK2", "2025-2026", 1, 8, 8));

            // Attendance records for all mock students
            attendanceMap.put("SV001", 2);
            attendanceMap.put("SV002", 6);
            attendanceMap.put("SV003", 1);
            attendanceMap.put("SV004", 0);
            attendanceMap.put("SV005", 3);
            attendanceMap.put("SV006", 5);
            attendanceMap.put("SV007", 0);
            attendanceMap.put("SV008", 2);
            attendanceMap.put("SV009", 1);
            attendanceMap.put("SV010", 0);
        }

        public void setModuleCredits(String module, int credits) {
            moduleCredits.put(module, credits);
        }

        public int getModuleCredits(String module) {
            return moduleCredits.getOrDefault(module, 0);
        }

        @Override
        public List<Student> getAllStudents() { 
            for (Student s : students) {
                s.setGpa(calculateGpa(s.getId()));
                s.setNote(generateNote(s.getId()));
            }
            return new ArrayList<>(students); 
        }

        @Override
        public void addStudent(Student s) { 
            s.setGpa(calculateGpa(s.getId()));
            s.setNote(generateNote(s.getId()));
            students.add(s); 
        }

        @Override
        public void updateStudent(Student s) {
            students.removeIf(st -> st.getId().equals(s.getId()));
            s.setGpa(calculateGpa(s.getId()));
            s.setNote(generateNote(s.getId()));
            students.add(s);
        }

        @Override
        public void deleteStudent(String id) { 
            students.removeIf(st -> st.getId().equals(id));
            scores.removeIf(sc -> sc.getStudentId().equals(id));
            attendanceMap.remove(id);
        }

        @Override
        public Student getStudentById(String id) {
            return students.stream().filter(st -> st.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Student> searchStudents(String keyword) {
            return students.stream()
                    .filter(st -> st.getId().contains(keyword) || st.getFullName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        public List<Student> searchStudents(String idKeyword, String nameKeyword) {
            return students.stream()
                    .filter(st -> (idKeyword.isEmpty() || st.getId().toLowerCase().contains(idKeyword.toLowerCase())) &&
                                  (nameKeyword.isEmpty() || st.getFullName().toLowerCase().contains(nameKeyword.toLowerCase())))
                    .collect(Collectors.toList());
        }

        @Override
        public List<String> getAllModules() { return new ArrayList<>(modules); }

        @Override
        public void addModule(String module) { 
            if (!modules.contains(module)) {
                modules.add(module);
                moduleCredits.put(module, 3);
            }
        }

        @Override
        public void deleteModule(String module) { 
            modules.remove(module);
            moduleCredits.remove(module);
            scores.removeIf(sc -> sc.getModule().equals(module));
        }

        @Override
        public List<Score> getScoresByModule(String module) {
            return scores.stream().filter(sc -> sc.getModule().equals(module)).collect(Collectors.toList());
        }

        @Override
        public Map<String, Integer> getAttendanceByStudent(String studentId) {
            Map<String, Integer> map = new HashMap<>();
            map.put(studentId, attendanceMap.getOrDefault(studentId, 0));
            return map;
        }

        @Override
        public List<Score> getAllScoresForStudent(String studentId) {
            return scores.stream().filter(sc -> sc.getStudentId().equals(studentId)).collect(Collectors.toList());
        }

        @Override
        public void updateScore(String studentId, String fullName, String module, int attendance, int test1, int exam) {
            scores.removeIf(sc -> sc.getStudentId().equals(studentId) && sc.getModule().equals(module));
            scores.add(new Score(studentId, fullName, module, "HK1", "2025-2026", attendance, test1, exam));
            Student s = students.stream().filter(st -> st.getId().equals(studentId)).findFirst().orElse(null);
            if (s != null) {
                s.setGpa(calculateGpa(studentId));
                s.setNote(generateNote(studentId));
            }
        }

        private double calculateGpa(String studentId) {
            List<Score> studentScores = getAllScoresForStudent(studentId);
            if (studentScores.isEmpty()) return 0.0;
            double total = 0.0;
            for (Score sc : studentScores) {
                total += sc.calculateModuleGrade();
            }
            return total / studentScores.size();
        }

        private String generateNote(String studentId) {
            int att = attendanceMap.getOrDefault(studentId, 0);
            if (att > ATTENDANCE_THRESHOLD) {
                return "Cảnh báo chuyên cần (nghỉ " + att + " ngày)";
            }
            return "Tốt - Chuyên cần cao";
        }
    }

    public ClientGUI() {
        setTitle("QUẢN LÍ SINH VIÊN");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("TextField.foreground", TEXT);
            UIManager.put("Label.foreground", TEXT);
            UIManager.put("Table.foreground", TEXT);
            UIManager.put("TableHeader.foreground", Color.WHITE);
        } catch (Exception e) {
            System.err.println("Không load Nimbus: " + e.getMessage());
        }

        manager = new MockStudentManager();

        localModules.addAll(Arrays.asList("Toán cao cấp", "Lập trình Mobile", "Cơ sở dữ liệu", "Mạng máy tính"));

        add(createSidebar(), BorderLayout.WEST);
        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadStudents();
        loadModules();
        loadModuleScores();

        new javax.swing.Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
            footerLabel.setText(" Hệ thống sẵn sàng | " + time);
        }).start();

        System.out.println("ClientGUI khởi tạo thành công - Ngày: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
    }

    // ---------------- Authentication helpers ----------------
    private static void loadCredentials() {
        if (CREDENTIALS_FILE.exists()) {
            try (FileInputStream in = new FileInputStream(CREDENTIALS_FILE)) {
                credentials.load(in);
            } catch (IOException e) {
                System.err.println("Không thể đọc file credentials: " + e.getMessage());
            }
        }
    }

    private static void saveCredentials() {
        try (FileOutputStream out = new FileOutputStream(CREDENTIALS_FILE)) {
            credentials.store(out, "User credentials (not secure - plain text)");
        } catch (IOException e) {
            System.err.println("Không thể lưu credentials: " + e.getMessage());
        }
    }

    private static boolean showAuthDialog() {
        loadCredentials();
        final boolean[] ok = {false};
        JDialog dialog = new JDialog((Frame) null, "Đăng nhập", true);
        // increase size so form + message + buttons fit without clipping
        dialog.setSize(520, 360);
        dialog.setLocationRelativeTo(null);
        dialog.getContentPane().setBackground(CARD);

        JPanel main = new JPanel();
        main.setBackground(CARD);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Styled header matching requested sample (left-aligned, large)
        JLabel header = new JLabel("<html><div style='text-align:center'>" +
            "<span style='font-size:36px;font-weight:800;color:rgb(" + PRIMARY.getRed() + "," + PRIMARY.getGreen() + "," + PRIMARY.getBlue() + ")'>QUẢN LÝ</span><br>" +
            "<span style='font-size:20px;color:rgb(" + HEADER_BG.getRed() + "," + HEADER_BG.getGreen() + "," + HEADER_BG.getBlue() + ")'>SINH VIÊN</span>" +
            "</div></html>");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(new EmptyBorder(8, 0, 16, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUser = new JLabel("Tài khoản:");
        lblUser.setForeground(TEXT);
        lblUser.setHorizontalAlignment(SwingConstants.LEFT);
        JTextField tfUser = new JTextField();
        tfUser.setBackground(new Color(250, 250, 250));
        tfUser.setColumns(20);
        styleTextField(tfUser);
        tfUser.setPreferredSize(new Dimension(380, 34));

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setForeground(TEXT);
        lblPass.setHorizontalAlignment(SwingConstants.LEFT);
        JPasswordField pf = new JPasswordField();
        pf.setBackground(new Color(250, 250, 250));
        pf.setColumns(20);
        stylePasswordField(pf);
        pf.setPreferredSize(new Dimension(380, 34));

        // row 0: label (left) + field (expands)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; form.add(lblUser, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST; form.add(tfUser, gbc);
        // row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; form.add(lblPass, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST; form.add(pf, gbc);
        // reset weightx
        gbc.weightx = 0;

        JLabel msg = new JLabel(" ");
        msg.setForeground(Color.RED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 6));
        bottom.setBackground(CARD);
        JButton btnRegister = coloredButton("Đăng ký", FUNC_BTN);
        JButton btnLogin = coloredButton("Đăng nhập", PRIMARY);
        JButton btnExit = coloredButton("Thoát", DELETE_COLOR);
        btnRegister.setPreferredSize(new Dimension(140, 36));
        btnLogin.setPreferredSize(new Dimension(140, 36));
        btnExit.setPreferredSize(new Dimension(140, 36));
        bottom.add(btnRegister);
        bottom.add(btnLogin);
        bottom.add(btnExit);

        btnLogin.addActionListener(e -> {
            String u = tfUser.getText().trim();
            String ptxt = new String(pf.getPassword());
            if (u.isEmpty() || ptxt.isEmpty()) {
                msg.setText("Vui lòng điền tài khoản và mật khẩu");
                return;
            }
            String stored = credentials.getProperty(u);
            if (stored != null && stored.equals(ptxt)) {
                currentUser = u;
                ok[0] = true;
                dialog.dispose();
            } else {
                msg.setText("Tên đăng nhập hoặc mật khẩu không đúng");
            }
        });

        btnRegister.addActionListener(e -> showRegisterDialog(dialog));

        btnExit.addActionListener(e -> {
            ok[0] = false;
            dialog.dispose();
        });

        main.add(header);
        main.add(form);
        main.add(Box.createVerticalStrut(8));
        main.add(msg);
        main.add(Box.createVerticalStrut(10));
        main.add(bottom);

        dialog.add(main);
        dialog.setResizable(false);
        dialog.setVisible(true);
        return ok[0];
    }

    private static void showRegisterDialog(Component parent) {
        JDialog d = new JDialog((Frame) null, "Đăng ký", true);
        // increase height so action buttons + padding fit inside the dialog
        d.setSize(460, 380);
        d.setLocationRelativeTo(parent);
        d.getContentPane().setBackground(CARD);

        JPanel main = new JPanel();
        main.setBackground(CARD);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Tạo tài khoản mới");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(6, 0, 8, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfUser = new JTextField();
        tfUser.setColumns(20);
        styleTextField(tfUser);
        JPasswordField pf1 = new JPasswordField();
        pf1.setColumns(20);
        stylePasswordField(pf1);
        JPasswordField pf2 = new JPasswordField();
        pf2.setColumns(20);
        stylePasswordField(pf2);
        JLabel lblUser = new JLabel("Tài khoản:"); lblUser.setForeground(TEXT);
        JLabel lblPass = new JLabel("Mật khẩu:"); lblPass.setForeground(TEXT);
        JLabel lblPass2 = new JLabel("Xác nhận mật khẩu:"); lblPass2.setForeground(TEXT);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; form.add(lblUser, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; form.add(tfUser, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; form.add(lblPass, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; form.add(pf1, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; form.add(lblPass2, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; form.add(pf2, gbc);
        gbc.weightx = 0;

        JLabel msg = new JLabel(" ");
        msg.setForeground(Color.RED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setBackground(CARD);
        // add internal padding so buttons have breathing room above the dialog edge
        bottom.setBorder(new EmptyBorder(0, 0, 12, 0));
        JButton btnOk = coloredButton("Tạo tài khoản", PRIMARY);
        JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
        btnOk.setPreferredSize(new Dimension(140, 36));
        btnCancel.setPreferredSize(new Dimension(140, 36));
        bottom.add(btnCancel); bottom.add(btnOk);

        btnOk.addActionListener(e -> {
            String u = tfUser.getText().trim();
            String a = new String(pf1.getPassword());
            String b = new String(pf2.getPassword());
            if (u.isEmpty() || a.isEmpty() || b.isEmpty()) {
                msg.setText("Vui lòng điền đầy đủ thông tin");
                return;
            }
            if (!a.equals(b)) {
                msg.setText("Mật khẩu xác nhận không khớp");
                return;
            }
            if (credentials.containsKey(u)) {
                msg.setText("Tài khoản đã tồn tại");
                return;
            }
            credentials.setProperty(u, a);
            saveCredentials();
            JOptionPane.showMessageDialog(d, "Đăng ký thành công! Bạn có thể đăng nhập ngay.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });

        btnCancel.addActionListener(e -> d.dispose());

        main.add(title);
        main.add(form);
        main.add(Box.createVerticalStrut(18));
        main.add(msg);
        main.add(Box.createVerticalStrut(18));
        main.add(bottom);
        // add extra spacing below the action buttons so they don't sit flush with the dialog edge
        main.add(Box.createVerticalStrut(12));

        d.add(main);
        d.setResizable(false);
        d.setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(12,12,12,12));

        // Create a slightly lighter header area for the logo/title
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(light(SIDEBAR_BG, 1.08));
        logoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel logo = new JLabel("<html><span style='color:#ffffff;font-weight:700;font-size:18px'>QUẢN LÝ</span><br>"
            + "<span style='color:rgb(" + HEADER_BG.getRed() + "," + HEADER_BG.getGreen() + "," + HEADER_BG.getBlue() + ") ;font-weight:600;font-size:18px'>SINH VIÊN</span></html>");
        logo.setBorder(new EmptyBorder(6,6,6,6));
        logo.setOpaque(false);
        logoPanel.add(logo, BorderLayout.CENTER);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.add(createSideMenuItem("👤 Sinh viên", "students"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createSideMenuItem("📚 Học phần", "modules"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createSideMenuItem("📝 Điểm", "scores"));
        menu.add(Box.createVerticalStrut(8));
        menu.add(createSideMenuItem("🗓 Chuyên cần", "attendance"));
        menu.add(Box.createVerticalGlue());
        sidebar.add(menu, BorderLayout.CENTER);

        return sidebar;
    }

    private JLabel createSideMenuItem(String text, String viewKey) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(SIDEBAR_BASE);
        lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Make menu items a consistent width/height for balanced layout
        Dimension itemSize = new Dimension(180, 44);
        lbl.setPreferredSize(itemSize);
        lbl.setMaximumSize(itemSize);
        lbl.setMinimumSize(itemSize);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchView(viewKey);
                Component[] components = ((JPanel) lbl.getParent()).getComponents();
                for (Component c : components) {
                    if (c instanceof JLabel) {
                        // reset other items to the base sidebar tone
                        ((JLabel) c).setBackground(SIDEBAR_BASE);
                        ((JLabel) c).setForeground(Color.WHITE);
                    }
                }
                // dim the selected item to a slightly darker (mờ) appearance
                lbl.setBackground(darken(SIDEBAR_BASE, 0.88));
                lbl.setForeground(Color.WHITE);
            }
        });
        return lbl;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(1100, 48));
        header.setBackground(HEADER_BG);
        JLabel lbl = new JLabel("HỆ THỐNG QUẢN LÝ SINH VIÊN");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(new EmptyBorder(0, 12, 0, 0));
        header.add(lbl, BorderLayout.WEST);
        return header;
    }

    private JPanel createContent() {
        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(buildStudentsPanel(), "students");
        contentPanel.add(buildModulesPanel(), "modules");
        contentPanel.add(buildScoresPanel(), "scores");
        contentPanel.add(buildAttendancePanel(), "attendance");
        return contentPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setPreferredSize(new Dimension(1100, 36));
        // make footer a darker shade of the header orange for consistency
        footer.setBackground(HEADER_BG.darker());
        footerLabel = new JLabel(" Hệ thống sẵn sàng");
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        footer.add(footerLabel, BorderLayout.WEST);
        return footer;
    }

    private void switchView(String key) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, key);
        switch (key) {
            case "students":
                loadStudents();
                break;
            case "modules":
                loadModules();
                break;
            case "scores":
                loadModuleScores();
                break;
            case "attendance":
                loadAttendance();
                break;
            default:
                break;
        }
    }

    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBackground(CARD);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setOpaque(false);
        JButton btnAdd = coloredButton("Thêm", ADD_COLOR);
        JButton btnSearch = coloredButton("Tìm kiếm", PRIMARY);
        top.add(btnAdd);
        top.add(btnSearch);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {"STT", "Mã SV", "Họ và tên", "Lớp", "Năm sinh", "Quê quán", "Điểm TB", "Ghi chú"};
        studentModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make "STT" column non-editable
            }
        };
        studentTable = new JTable(studentModel);
        styleTable(studentTable);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Set width for "Thứ tự" column

        studentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(255, 245, 235)); // soft orange highlight
                    c.setForeground(TEXT);
                } else {
                    Color base = (row % 2 == 0) ? CARD : ALT_ROW;
                    c.setBackground(base);
                    c.setForeground(TEXT);
                    if (column == 1 || column == 2) {
                        c.setBackground(new Color(220, 240, 248)); // subtle pale blue for id/name columns
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new MatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));
                return c;
            }
        });

        JScrollPane sc = new JScrollPane(studentTable);
        sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
        panel.add(sc, BorderLayout.CENTER);

        studentBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        studentBottom.setOpaque(false);
        JButton btnEdit = coloredButton("Sửa", EDIT_COLOR);
        JButton btnDelete = coloredButton("Xóa", DELETE_COLOR);
        JButton btnView = coloredButton("Xem chi tiết", VIEW_COLOR);
        studentBottom.add(btnEdit);
        studentBottom.add(btnDelete);
        studentBottom.add(btnView);
        panel.add(studentBottom, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> showAddDialog());
        btnSearch.addActionListener(e -> showSearchDialog());
        btnEdit.addActionListener(e -> {
            int r = studentTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showEditStudentDialog((String) studentModel.getValueAt(r, 1));
        });
        btnDelete.addActionListener(e -> {
            int r = studentTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            deleteStudent((String) studentModel.getValueAt(r, 1), (String) studentModel.getValueAt(r, 2));
        });
        btnView.addActionListener(e -> {
            int r = studentTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            showDetailDialog((String) studentModel.getValueAt(r, 1));
        });

        return panel;
    }

    private void loadStudents() {
        try {
            studentModel.setRowCount(0);
            List<Student> list = manager.getAllStudents();
            for (int i = 0; i < list.size(); i++) {
                Student s = list.get(i);
                studentModel.addRow(new Object[]{
                    i + 1, // Serial number
                    s.getId(),
                    s.getFullName(),
                    s.getClazz(),
                    s.getBirthYear(),
                    s.getHometown(),
                    String.format("%.2f", s.getGpa()),
                    s.getNote()
                });
            }
            // Reset the bottom panel to remove the Back button if it exists
            studentBottom.removeAll();
            JButton btnEdit = coloredButton("Sửa", EDIT_COLOR);
            JButton btnDelete = coloredButton("Xóa", DELETE_COLOR);
            JButton btnView = coloredButton("Xem chi tiết", VIEW_COLOR);
            studentBottom.add(btnEdit);
            studentBottom.add(btnDelete);
            studentBottom.add(btnView);
            btnEdit.addActionListener(e -> {
                int r = studentTable.getSelectedRow();
                if (r == -1) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showEditStudentDialog((String) studentModel.getValueAt(r, 1));
            });
            btnDelete.addActionListener(e -> {
                int r = studentTable.getSelectedRow();
                if (r == -1) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                deleteStudent((String) studentModel.getValueAt(r, 1), (String) studentModel.getValueAt(r, 2));
            });
            btnView.addActionListener(e -> {
                int r = studentTable.getSelectedRow();
                if (r == -1) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showDetailDialog((String) studentModel.getValueAt(r, 1));
            });
            revalidate();
            repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu sinh viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(this, "Thêm sinh viên mới", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(CARD);
        dialog.setSize(400, 300);

        String[] hometowns = {
            "An Giang", "Bà Rịa - Vũng Tàu", "Bạc Liêu", "Bắc Giang", "Bắc Kạn", "Bắc Ninh",
            "Bến Tre", "Bình Dương", "Bình Định", "Bình Phước", "Bình Thuận",
            "Cà Mau", "Cao Bằng", "Cần Thơ", "Đà Nẵng", "Đắk Lắk", "Đắk Nông",
            "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam",
            "Hà Nội", "Hà Tĩnh", "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình",
            "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng",
            "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình",
            "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi",
            "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình",
            "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "TP. Hồ Chí Minh",
            "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
        };
        JComboBox<String> cbHometown = new JComboBox<>(hometowns);
        cbHometown.setToolTipText("Chọn quê quán của sinh viên");

        JComboBox<Integer> cbYear = new JComboBox<>();
        for (int y = 1950; y <= 2010; y++) {
            cbYear.addItem(y);
        }
        cbYear.setSelectedItem(2000);
        cbYear.setToolTipText("Chọn năm sinh");

        JTextField txtClass = new JTextField();
        txtClass.setToolTipText("Nhập mã lớp, ví dụ: CTK43");
        styleTextField(txtClass);

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY), "Thông tin sinh viên", TitledBorder.LEFT, TitledBorder.TOP));
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(10, 10, 10, 10)));

        JTextField txtId = new JTextField();
        txtId.setToolTipText("Nhập mã sinh viên, ví dụ: SV001");
        JTextField txtName = new JTextField();
        txtName.setToolTipText("Nhập họ và tên sinh viên");

        JLabel lblId = new JLabel("Mã SV: *");
        JLabel lblName = new JLabel("Họ và tên: *");
        JLabel lblClass = new JLabel("Lớp:");
        JLabel lblYear = new JLabel("Năm sinh:");
        JLabel lblHometown = new JLabel("Quê quán:");

        p.add(lblId); p.add(txtId);
        p.add(lblName); p.add(txtName);
        p.add(lblClass); p.add(txtClass);
        p.add(lblYear); p.add(cbYear);
        p.add(lblHometown); p.add(cbHometown);

        JLabel validationLabel = new JLabel("");
        validationLabel.setForeground(Color.RED);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(CARD);
        inputPanel.add(p, BorderLayout.CENTER);
        inputPanel.add(validationLabel, BorderLayout.SOUTH);

        JButton btnOk = coloredButton("Lưu", ADD_COLOR);
        btnOk.setToolTipText("Lưu thông tin sinh viên");
        JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
        btnCancel.setToolTipText("Hủy và đóng cửa sổ");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(CARD);
        bottom.add(btnOk);
        bottom.add(btnCancel);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);

        btnOk.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String clazz = txtClass.getText().trim();
            if (id.isEmpty() || name.isEmpty()) {
                validationLabel.setText("Mã SV và Họ tên không được rỗng!");
                return;
            }
            if (manager.getStudentById(id) != null) {
                validationLabel.setText("Mã SV đã tồn tại!");
                return;
            }
            try {
                int birthYear = (Integer) cbYear.getSelectedItem();
                Student s = new Student(id, name, clazz, birthYear, (String) cbHometown.getSelectedItem());
                manager.addStudent(s);
                loadStudents();
                dialog.dispose();
                JOptionPane.showMessageDialog(dialog, "Thêm sinh viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                validationLabel.setText("Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void showEditStudentDialog(String id) {
        try {
            Student s = manager.getStudentById(id);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JDialog dialog = new JDialog(this, "Sửa thông tin sinh viên", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.getContentPane().setBackground(CARD);
            dialog.setSize(400, 300);

            String[] hometowns = {
                "An Giang", "Bà Rịa - Vũng Tàu", "Bạc Liêu", "Bắc Giang", "Bắc Kạn", "Bắc Ninh",
                "Bến Tre", "Bình Dương", "Bình Định", "Bình Phước", "Bình Thuận",
                "Cà Mau", "Cao Bằng", "Cần Thơ", "Đà Nẵng", "Đắk Lắk", "Đắk Nông",
                "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam",
                "Hà Nội", "Hà Tĩnh", "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình",
                "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng",
                "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An", "Ninh Bình",
                "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi",
                "Quảng Ninh", "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình",
                "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "TP. Hồ Chí Minh",
                "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
            };
            JComboBox<String> cbHometown = new JComboBox<>(hometowns);
            cbHometown.setSelectedItem(s.getHometown());
            cbHometown.setToolTipText("Chọn quê quán của sinh viên");

            JComboBox<Integer> cbYear = new JComboBox<>();
            for (int y = 1950; y <= 2010; y++) {
                cbYear.addItem(y);
            }
            cbYear.setSelectedItem(s.getBirthYear());
            cbYear.setToolTipText("Chọn năm sinh");

            JTextField txtClass = new JTextField(s.getClazz());
            txtClass.setToolTipText("Nhập mã lớp, ví dụ: CTK43");

            JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
            p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY), "Thông tin sinh viên", TitledBorder.LEFT, TitledBorder.TOP));
            p.setBackground(CARD);
            p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(10, 10, 10, 10)));

            JTextField txtId = new JTextField(s.getId());
            txtId.setEditable(false);
            txtId.setToolTipText("Mã sinh viên không thể chỉnh sửa");
            JTextField txtName = new JTextField(s.getFullName());
            txtName.setToolTipText("Nhập họ và tên sinh viên");

            JLabel lblId = new JLabel("Mã SV:");
            JLabel lblName = new JLabel("Họ và tên: *");
            JLabel lblClass = new JLabel("Lớp:");
            JLabel lblYear = new JLabel("Năm sinh:");
            JLabel lblHometown = new JLabel("Quê quán:");

            p.add(lblId); p.add(txtId);
            p.add(lblName); p.add(txtName);
            p.add(lblClass); p.add(txtClass);
            p.add(lblYear); p.add(cbYear);
            p.add(lblHometown); p.add(cbHometown);

            JLabel validationLabel = new JLabel("");
            validationLabel.setForeground(Color.RED);
            validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.setBackground(CARD);
            inputPanel.add(p, BorderLayout.CENTER);
            inputPanel.add(validationLabel, BorderLayout.SOUTH);

            JButton btnOk = coloredButton("Cập nhật", EDIT_COLOR);
            btnOk.setToolTipText("Lưu thông tin cập nhật");
            JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
            btnCancel.setToolTipText("Hủy và đóng cửa sổ");
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            bottom.setBackground(CARD);
            bottom.add(btnOk);
            bottom.add(btnCancel);

            dialog.add(inputPanel, BorderLayout.CENTER);
            dialog.add(bottom, BorderLayout.SOUTH);
            dialog.setLocationRelativeTo(this);

            btnOk.addActionListener(e -> {
                String name = txtName.getText().trim();
                String clazz = txtClass.getText().trim();
                if (name.isEmpty()) {
                    validationLabel.setText("Họ tên không được rỗng!");
                    return;
                }
                try {
                    int birthYear = (Integer) cbYear.getSelectedItem();
                    s.setFullName(name);
                    s.setClazz(clazz);
                    s.setBirthYear(birthYear);
                    s.setHometown((String) cbHometown.getSelectedItem());
                    manager.updateStudent(s);
                    loadStudents();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    validationLabel.setText("Lỗi: " + ex.getMessage());
                }
            });

            btnCancel.addActionListener(e -> dialog.dispose());

            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetailDialog(String id) {
        try {
            Student s = manager.getStudentById(id);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(this, "Chi tiết sinh viên: " + s.getFullName(), true);
            dialog.setSize(600, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));

            JPanel infoPanel = new JPanel(new GridLayout(6, 2, 5, 5));
            infoPanel.setBorder(BorderFactory.createTitledBorder("Thông tin cơ bản"));
            infoPanel.setBackground(CARD);
            infoPanel.add(new JLabel("Mã SV:"));
            infoPanel.add(new JLabel(s.getId()));
            infoPanel.add(new JLabel("Họ tên:"));
            infoPanel.add(new JLabel(s.getFullName()));
            infoPanel.add(new JLabel("Lớp:"));
            infoPanel.add(new JLabel(s.getClazz()));
            infoPanel.add(new JLabel("Năm sinh:"));
            infoPanel.add(new JLabel(String.valueOf(s.getBirthYear())));
            infoPanel.add(new JLabel("Quê quán:"));
            infoPanel.add(new JLabel(s.getHometown()));
            infoPanel.add(new JLabel("Điểm TB:"));
            infoPanel.add(new JLabel(String.format("%.2f", s.getGpa())));
            infoPanel.add(new JLabel("Ghi chú:"));
            infoPanel.add(new JLabel(s.getNote()));

            String[] scoreColumns = {"STT", "Học phần", "Chuyên cần", "KT1", "Thi", "Điểm môn"};
            DefaultTableModel scoreModel = new DefaultTableModel(scoreColumns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0; // Make "Thứ tự" column non-editable
                }
            };
            JTable detailScoreTable = new JTable(scoreModel);
            styleTable(detailScoreTable);
            detailScoreTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Set width for "Thứ tự" column

            List<Score> studentScores = manager.getAllScoresForStudent(id);
            for (int i = 0; i < studentScores.size(); i++) {
                Score sc = studentScores.get(i);
                scoreModel.addRow(new Object[]{
                    i + 1, // Serial number
                    sc.getModule(),
                    sc.getAttendance(),
                    sc.getTest1(),
                    sc.getExam(),
                    String.format("%.2f", sc.calculateModuleGrade())
                });
            }

            JScrollPane scoreScroll = new JScrollPane(detailScoreTable);
            scoreScroll.setBorder(BorderFactory.createTitledBorder("Điểm từng học phần"));

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(CARD);
            mainPanel.add(infoPanel, BorderLayout.NORTH);
            mainPanel.add(scoreScroll, BorderLayout.CENTER);

            JButton closeBtn = new JButton("Đóng");
            closeBtn.setBackground(DELETE_COLOR);
            closeBtn.setForeground(Color.WHITE);
            closeBtn.addActionListener(e -> dialog.dispose());

            dialog.add(mainPanel, BorderLayout.CENTER);
            dialog.add(closeBtn, BorderLayout.SOUTH);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi hiển thị chi tiết: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent(String id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa sinh viên '" + name + "' (ID: " + id + ")?", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.deleteStudent(id);
                loadStudents();
                JOptionPane.showMessageDialog(this, "Xóa sinh viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSearchDialog() {
        JDialog dialog = new JDialog(this, "Tìm kiếm sinh viên", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(CARD);
        dialog.setSize(400, 200);

        JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY), "Tìm kiếm", TitledBorder.LEFT, TitledBorder.TOP));
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(10, 10, 10, 10)));

        JTextField txtIdKeyword = new JTextField();
        txtIdKeyword.setToolTipText("Nhập mã sinh viên hoặc một phần mã");
        JTextField txtNameKeyword = new JTextField();
        txtNameKeyword.setToolTipText("Nhập tên sinh viên hoặc một phần tên");

        JLabel lblIdKeyword = new JLabel("Mã SV:");
        JLabel lblNameKeyword = new JLabel("Họ và tên:");

        p.add(lblIdKeyword); p.add(txtIdKeyword);
        p.add(lblNameKeyword); p.add(txtNameKeyword);

        JLabel validationLabel = new JLabel("");
        validationLabel.setForeground(Color.RED);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(CARD);
        inputPanel.add(p, BorderLayout.CENTER);
        inputPanel.add(validationLabel, BorderLayout.SOUTH);

        JButton btnSearch = coloredButton("Tìm", PRIMARY);
        btnSearch.setToolTipText("Tìm kiếm sinh viên");
        JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
        btnCancel.setToolTipText("Hủy và đóng cửa sổ");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(CARD);
        bottom.add(btnSearch);
        bottom.add(btnCancel);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);

        btnSearch.addActionListener(e -> {
            String idKeyword = txtIdKeyword.getText().trim();
            String nameKeyword = txtNameKeyword.getText().trim();
            if (idKeyword.isEmpty() && nameKeyword.isEmpty()) {
                validationLabel.setText("Vui lòng nhập ít nhất một tiêu chí tìm kiếm!");
                return;
            }
            try {
                studentModel.setRowCount(0);
                List<Student> results = ((MockStudentManager) manager).searchStudents(idKeyword, nameKeyword);
                if (results.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Không tìm thấy kết quả!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    return;
                }
                for (int i = 0; i < results.size(); i++) {
                    Student s = results.get(i);
                    studentModel.addRow(new Object[]{
                        i + 1, // Serial number
                        s.getId(), s.getFullName(), s.getClazz(), s.getBirthYear(), s.getHometown(),
                        String.format("%.2f", s.getGpa()), s.getNote()
                    });
                }
                // Add back button to the studentBottom panel
                JButton btnBack = coloredButton("⬅ Back", PRIMARY);
                studentBottom.removeAll();
                JButton btnEdit = coloredButton("✏️ Sửa", EDIT_COLOR);
                JButton btnDelete = coloredButton("🗑️ Xóa", DELETE_COLOR);
                JButton btnView = coloredButton("👁 Xem chi tiết", VIEW_COLOR);
                studentBottom.add(btnEdit);
                studentBottom.add(btnDelete);
                studentBottom.add(btnView);
                studentBottom.add(btnBack);
                btnEdit.addActionListener(e1 -> {
                    int r = studentTable.getSelectedRow();
                    if (r == -1) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    showEditStudentDialog((String) studentModel.getValueAt(r, 1));
                });
                btnDelete.addActionListener(e1 -> {
                    int r = studentTable.getSelectedRow();
                    if (r == -1) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    deleteStudent((String) studentModel.getValueAt(r, 1), (String) studentModel.getValueAt(r, 2));
                });
                btnView.addActionListener(e1 -> {
                    int r = studentTable.getSelectedRow();
                    if (r == -1) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    showDetailDialog((String) studentModel.getValueAt(r, 1));
                });
                btnBack.addActionListener(e1 -> loadStudents());
                revalidate();
                repaint();
                dialog.dispose();
            } catch (Exception ex) {
                validationLabel.setText("Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private JPanel buildModulesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBackground(CARD);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setOpaque(false);
        JButton btnAdd = coloredButton("Thêm học phần", MODULE_ADD);
        JButton btnDelete = coloredButton("Xóa học phần", DELETE_COLOR);
        JButton btnRefresh = coloredButton("Làm mới", PRIMARY);
        top.add(btnAdd);
        top.add(btnDelete);
        top.add(btnRefresh);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {"STT", "Tên học phần", "Số tín chỉ"};
        moduleModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make "Thứ tự" column non-editable
            }
        };
        moduleTable = new JTable(moduleModel);
        styleTable(moduleTable);
        moduleTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Set width for "Thứ tự" column
        JScrollPane sc = new JScrollPane(moduleTable);
        sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
        panel.add(sc, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addModuleDialog());
        btnDelete.addActionListener(e -> deleteModule());
        btnRefresh.addActionListener(e -> loadModules());

        return panel;
    }

    private void loadModules() {
        try {
            moduleModel.setRowCount(0);
            moduleComboBox.removeAllItems();
            List<String> list = manager.getAllModules();
            if (list.isEmpty()) {
                list.addAll(localModules);
            }
            for (int i = 0; i < list.size(); i++) {
                String m = list.get(i);
                int credits = ((MockStudentManager) manager).getModuleCredits(m);
                moduleModel.addRow(new Object[]{i + 1, m, credits});
                moduleComboBox.addItem(m);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải học phần: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addModuleDialog() {
        JDialog dialog = new JDialog(this, "Thêm học phần mới", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(CARD);
        dialog.setSize(350, 200);

        JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY), "Thông tin học phần", TitledBorder.LEFT, TitledBorder.TOP));
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(10, 10, 10, 10)));

        JTextField txtName = new JTextField();
        txtName.setToolTipText("Nhập tên học phần, ví dụ: Toán cao cấp");
        JSpinner spCredits = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spCredits.setToolTipText("Chọn số tín chỉ từ 1 đến 10");

        JLabel lblName = new JLabel("Tên học phần: *");
        JLabel lblCredits = new JLabel("Số tín chỉ: *");

        p.add(lblName); p.add(txtName);
        p.add(lblCredits); p.add(spCredits);

        JLabel validationLabel = new JLabel("");
        validationLabel.setForeground(Color.RED);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(CARD);
        inputPanel.add(p, BorderLayout.CENTER);
        inputPanel.add(validationLabel, BorderLayout.SOUTH);

        JButton btnOk = coloredButton("Lưu", ADD_COLOR);
        btnOk.setToolTipText("Lưu thông tin học phần");
        JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
        btnCancel.setToolTipText("Hủy và đóng cửa sổ");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(CARD);
        bottom.add(btnOk);
        bottom.add(btnCancel);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);

        btnOk.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                validationLabel.setText("Tên học phần không được rỗng!");
                return;
            }
            if (manager.getAllModules().contains(name)) {
                validationLabel.setText("Học phần đã tồn tại!");
                return;
            }
            try {
                int credits = (Integer) spCredits.getValue();
                manager.addModule(name);
                ((MockStudentManager) manager).setModuleCredits(name, credits);
                loadModules();
                dialog.dispose();
                JOptionPane.showMessageDialog(dialog, "Thêm học phần thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                validationLabel.setText("Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void deleteModule() {
        int r = moduleTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học phần!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = (String) moduleModel.getValueAt(r, 1);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xóa học phần '" + name + "'? (Sẽ xóa tất cả điểm liên quan)", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                manager.deleteModule(name);
                loadModules();
                JOptionPane.showMessageDialog(this, "Xóa học phần thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel buildScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBackground(CARD);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setOpaque(false);
        top.add(new JLabel("Học phần: "));
        moduleComboBox = new JComboBox<>();
        moduleComboBox.setPreferredSize(new Dimension(260, 28));
        moduleComboBox.setBorder(new LineBorder(PRIMARY, 1, true));
        moduleComboBox.setToolTipText("Chọn học phần để xem điểm");
        top.add(moduleComboBox);
        JButton btnRefresh = coloredButton("Làm mới", PRIMARY);
        btnRefresh.setToolTipText("Làm mới danh sách điểm");
        JButton btnEditScore = coloredButton("Sửa điểm", EDIT_COLOR);
        btnEditScore.setToolTipText("Sửa điểm cho sinh viên được chọn");
        top.add(btnRefresh);
        top.add(btnEditScore);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {"STT", "Học kỳ", "Năm học", "Mã SV", "Họ và tên", "Học phần", "Điểm chuyên cần", "Điểm KT1", "Điểm Thi", "Điểm tổng"};
        scoreModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make "Thứ tự" column non-editable
            }
        };
        scoreTable = new JTable(scoreModel);
        styleTable(scoreTable);
        scoreTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Set width for "Thứ tự" column
        JScrollPane sc = new JScrollPane(scoreTable);
        sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
        panel.add(sc, BorderLayout.CENTER);

        moduleComboBox.addActionListener(e -> loadModuleScores());
        btnRefresh.addActionListener(e -> loadModuleScores());
        btnEditScore.addActionListener(e -> showEditScoreDialog());

        return panel;
    }

    private void loadModuleScores() {
        try {
            scoreModel.setRowCount(0);
            String module = (String) moduleComboBox.getSelectedItem();
            if (module == null || module.isEmpty()) return;
            List<Student> students = manager.getAllStudents();
            List<Score> scores = manager.getScoresByModule(module);
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                Score score = scores.stream()
                        .filter(sc -> sc.getStudentId().equals(s.getId()) && sc.getModule().equals(module))
                        .findFirst()
                        .orElse(new Score(s.getId(), s.getFullName(), module, "HK1", "2025-2026", 0, 0, 0));
                scoreModel.addRow(new Object[]{
                    i + 1, // Serial number
                    score.getSemester(),
                    score.getAcademicYear(),
                    score.getStudentId(),
                    score.getFullName(),
                    score.getModule(),
                    score.getAttendance(),
                    score.getTest1(),
                    score.getExam(),
                    String.format("%.2f", score.calculateModuleGrade())
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải điểm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditScoreDialog() {
        int r = scoreTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) scoreModel.getValueAt(r, 3);
        String name = (String) scoreModel.getValueAt(r, 4);
        String module = (String) moduleComboBox.getSelectedItem();
        if (module == null || module.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn học phần!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Sửa điểm cho " + name, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(CARD);
        dialog.setSize(350, 220);

        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY), "Thông tin điểm", TitledBorder.LEFT, TitledBorder.TOP));
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(10, 10, 10, 10)));

        JTextField txtAttendance = new JTextField("0");
        txtAttendance.setToolTipText("Nhập số buổi nghỉ");
        JTextField txtTest1 = new JTextField("0");
        txtTest1.setToolTipText("Nhập điểm kiểm tra 1 (0-10)");
        JTextField txtExam = new JTextField("0");
        txtExam.setToolTipText("Nhập điểm thi (0-10)");

        JLabel lblAttendance = new JLabel("Điểm chuyên cần:");
        JLabel lblTest1 = new JLabel("Điểm KT1 (0-10):");
        JLabel lblExam = new JLabel("Điểm Thi (0-10):");

        p.add(lblAttendance); p.add(txtAttendance);
        p.add(lblTest1); p.add(txtTest1);
        p.add(lblExam); p.add(txtExam);

        JLabel validationLabel = new JLabel("");
        validationLabel.setForeground(Color.RED);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(CARD);
        inputPanel.add(p, BorderLayout.CENTER);
        inputPanel.add(validationLabel, BorderLayout.SOUTH);

        JButton btnOk = coloredButton("Lưu", ADD_COLOR);
        btnOk.setToolTipText("Lưu điểm đã cập nhật");
        JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
        btnCancel.setToolTipText("Hủy và đóng cửa sổ");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(CARD);
        bottom.add(btnOk);
        bottom.add(btnCancel);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);

        Score existingScore = manager.getScoresByModule(module).stream()
                .filter(sc -> sc.getStudentId().equals(id))
                .findFirst()
                .orElse(null);
        if (existingScore != null) {
            txtAttendance.setText(String.valueOf(existingScore.getAttendance()));
            txtTest1.setText(String.valueOf(existingScore.getTest1()));
            txtExam.setText(String.valueOf(existingScore.getExam()));
        }

        btnOk.addActionListener(e -> {
            try {
                int attendance = Integer.parseInt(txtAttendance.getText().trim());
                int test1 = Integer.parseInt(txtTest1.getText().trim());
                int exam = Integer.parseInt(txtExam.getText().trim());
                if (attendance < 0) {
                    validationLabel.setText("Điểm chuyên cần phải >= 0!");
                    return;
                }
                if (test1 < 0 || test1 > 10 || exam < 0 || exam > 10) {
                    validationLabel.setText("Điểm KT1 và Thi phải từ 0-10!");
                    return;
                }
                manager.updateScore(id, name, module, attendance, test1, exam);
                loadModuleScores();
                loadStudents();
                dialog.dispose();
                JOptionPane.showMessageDialog(dialog, "Cập nhật điểm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                validationLabel.setText("Vui lòng nhập số hợp lệ!");
            } catch (Exception ex) {
                validationLabel.setText("Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBackground(CARD);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        top.setOpaque(false);
        JButton btnRefresh = coloredButton("Làm mới", PRIMARY);
        btnRefresh.setToolTipText("Làm mới danh sách chuyên cần");
        JButton btnEditAttendance = coloredButton("Sửa chuyên cần", EDIT_COLOR);
        btnEditAttendance.setToolTipText("Sửa thông tin chuyên cần");
        top.add(btnRefresh);
        top.add(btnEditAttendance);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {"STT", "Họ và tên", "Học kỳ", "Năm học", "Tên môn", "Số tiết nghỉ", "Phần trăm nghỉ"};
        attendanceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make "STT" column non-editable
            }
        };
        attendanceTable = new JTable(attendanceModel);
        attendanceTable.setRowSelectionAllowed(true);
        styleTable(attendanceTable);
        attendanceTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Set width for "STT" column
        attendanceTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Set width for "Họ và tên" column
        JScrollPane sc = new JScrollPane(attendanceTable);
        sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
        panel.add(sc, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadAttendance());
        btnEditAttendance.addActionListener(e -> editAttendance());

        return panel;
    }

    private void loadAttendance() {
        try {
            attendanceModel.setRowCount(0);
            List<Student> list = manager.getAllStudents();
            int rowIndex = 1;
            for (Student s : list) {
                int daysMissed = manager.getAttendanceByStudent(s.getId()).getOrDefault(s.getId(), 0);
                List<Score> studentScores = manager.getAllScoresForStudent(s.getId());
                for (Score sc : studentScores) {
                    double attendancePercentage = (daysMissed / 10.0) * 100; // Giả sử tổng số tiết là 10
                    attendanceModel.addRow(new Object[]{
                        rowIndex++, // STT
                        s.getFullName(), // Họ và tên
                        sc.getSemester(),
                        sc.getAcademicYear(),
                        sc.getModule(),
                        daysMissed,
                        String.format("%.2f%%", attendancePercentage)
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải chuyên cần: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editAttendance() {
        int r = attendanceTable.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String studentName = (String) attendanceModel.getValueAt(r, 1); // Get "Họ và tên" from table
        Student student = manager.getAllStudents().stream()
                .filter(s -> s.getFullName().equals(studentName))
                .findFirst()
                .orElse(null);
        if (student == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String studentId = student.getId();

        JDialog dialog = new JDialog(this, "Sửa chuyên cần cho " + studentName, true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(CARD);
        dialog.setSize(350, 150);

        JPanel p = new JPanel(new GridLayout(1, 2, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY), "Thông tin chuyên cần", TitledBorder.LEFT, TitledBorder.TOP));
        p.setBackground(CARD);
        p.setBorder(new CompoundBorder(p.getBorder(), new EmptyBorder(10, 10, 10, 10)));

        JTextField txtDaysMissed = new JTextField(String.valueOf(manager.getAttendanceByStudent(studentId).getOrDefault(studentId, 0)));
        txtDaysMissed.setToolTipText("Nhập số buổi nghỉ");

        JLabel lblDaysMissed = new JLabel("Số tiết nghỉ:");
        p.add(lblDaysMissed); p.add(txtDaysMissed);

        JLabel validationLabel = new JLabel("");
        validationLabel.setForeground(Color.RED);
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(CARD);
        inputPanel.add(p, BorderLayout.CENTER);
        inputPanel.add(validationLabel, BorderLayout.SOUTH);

        JButton btnOk = coloredButton("Lưu", ADD_COLOR);
        btnOk.setToolTipText("Lưu thông tin chuyên cần");
        JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
        btnCancel.setToolTipText("Hủy và đóng cửa sổ");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(CARD);
        bottom.add(btnOk);
        bottom.add(btnCancel);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);

        btnOk.addActionListener(e -> {
            try {
                int days = Integer.parseInt(txtDaysMissed.getText().trim());
                if (days < 0) {
                    validationLabel.setText("Số tiết phải >= 0!");
                    return;
                }
                ((MockStudentManager) manager).attendanceMap.put(studentId, days);
                loadAttendance();
                loadStudents();
                dialog.dispose();
                JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                validationLabel.setText("Vui lòng nhập số hợp lệ!");
            } catch (Exception ex) {
                validationLabel.setText("Lỗi: " + ex.getMessage());
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private static JButton coloredButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return b;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT);

        // show grid lines and use a light divider between cells
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 34));
        // Custom header renderer: draw a vertical gradient using TABLE_HEADER_BG
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(Color.WHITE);
                // bottom + right separator line to match the attached style
                setBorder(new MatteBorder(0, 0, 1, 1, new Color(255, 255, 255, 120)));
                setOpaque(false); // we'll paint background ourselves
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Color top = TABLE_HEADER_BG.brighter();
                Color bottom = TABLE_HEADER_BG.darker();
                GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bottom);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                        if (isSelected) {
                            c.setBackground(new Color(255, 245, 235));
                            c.setForeground(TEXT);
                        } else {
                            c.setBackground((row % 2 == 0) ? CARD : ALT_ROW);
                            c.setForeground(TEXT);
                        }
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new MatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));
                return c;
            }
        });

        t.setGridColor(new Color(220, 220, 220));
        t.setSelectionBackground(new Color(200, 230, 255));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            boolean ok = showAuthDialog();
            if (!ok) {
                System.out.println("Người dùng không đăng nhập. Thoát.");
                System.exit(0);
            }
            new ClientGUI().setVisible(true);
        });
    }
}
