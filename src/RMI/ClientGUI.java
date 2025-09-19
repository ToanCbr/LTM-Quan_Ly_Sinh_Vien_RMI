package RMI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

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

	// Colors
	private static final Color BG = new Color(173, 216, 230);
	private static final Color CARD = new Color(135, 206, 235);
	private static final Color PRIMARY = new Color(30, 144, 255);
	private static final Color ADD_COLOR = new Color(0, 100, 0);
	private static final Color EDIT_COLOR = new Color(255, 215, 0);
	private static final Color DELETE_COLOR = new Color(139, 0, 0);
	private static final Color VIEW_COLOR = new Color(75, 0, 130);
	private static final Color MODULE_ADD = new Color(218, 165, 32);
	private static final Color TEXT = Color.BLACK;
	private static final Color ALT_ROW = new Color(176, 224, 230);
	// Constants
	private static final int ATTENDANCE_THRESHOLD = 5;
	private static final double GPA_WEIGHT_TEST1 = 0.3;
	private static final double GPA_WEIGHT_EXAM = 0.7;
	private static final double ATTENDANCE_PENALTY = 0.5;

	// Định nghĩa interface StudentManager - Không thay đổi
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

		// Thêm method cho attendance và scores chi tiết
		Map<String, Integer> getAttendanceByStudent(String studentId);

		List<Score> getAllScoresForStudent(String studentId);
	}

	// Định nghĩa class Student - Thêm field cho ghi chú và gpa tạm thời
	static class Student {
		private String id, fullName, clazz, hometown;
		private int birthYear;
		private double gpa; // Thêm GPA
		private String note; // Thêm ghi chú

		public Student(String id, String fullName, String clazz, int birthYear, String hometown) {
			this.id = id;
			this.fullName = fullName;
			this.clazz = clazz;
			this.birthYear = birthYear;
			this.hometown = hometown;
			this.gpa = 0.0; // Default
			this.note = ""; // Default
		}

		// Getters
		public String getId() {
			return id;
		}

		public String getFullName() {
			return fullName;
		}

		public String getClazz() {
			return clazz;
		}

		public int getBirthYear() {
			return birthYear;
		}

		public String getHometown() {
			return hometown;
		}

		public double getGpa() {
			return gpa;
		}

		public String getNote() {
			return note;
		}

		// Setters
		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}

		public void setBirthYear(int birthYear) {
			this.birthYear = birthYear;
		}

		public void setHometown(String hometown) {
			this.hometown = hometown;
		}

		public void setGpa(double gpa) {
			this.gpa = gpa;
		}

		public void setNote(String note) {
			this.note = note;
		}
	}

	// Định nghĩa class Score - Không thay đổi
	static class Score {
		private String studentId, fullName, module;
		private int attendance, test1, exam;

		public Score(String studentId, String fullName, String module, int attendance, int test1, int exam) {
			this.studentId = studentId;
			this.fullName = fullName;
			this.module = module;
			this.attendance = attendance;
			this.test1 = test1;
			this.exam = exam;
		}

		// Getters
		public String getStudentId() {
			return studentId;
		}

		public String getFullName() {
			return fullName;
		}

		public String getModule() {
			return module;
		}

		public int getAttendance() {
			return attendance;
		}

		public int getTest1() {
			return test1;
		}

		public int getExam() {
			return exam;
		}

		// Tính điểm môn
		public double calculateModuleGrade() {
			return (test1 * GPA_WEIGHT_TEST1 + exam * GPA_WEIGHT_EXAM) - (attendance * ATTENDANCE_PENALTY);
		}
	}

	// Mock StudentManager - Mở rộng với data chi tiết hơn
	static class MockStudentManager implements StudentManager {
		private List<Student> students = new ArrayList<>();
		private List<String> modules = new ArrayList<>();
		private Map<String, Integer> moduleCredits = new HashMap<>();
		private List<Score> scores = new ArrayList<>();
		private Map<String, Integer> attendanceMap = new HashMap<>(); // Thêm attendance

		public MockStudentManager() {
			// Khởi tạo students với GPA và note
			Student s1 = new Student("SV001", "Nguyễn Văn A", "CTK43", 2000, "Hà Nội");
			s1.setGpa(7.5);
			s1.setNote("Tốt");
			students.add(s1);

			Student s2 = new Student("SV002", "Trần Thị B", "CTK44", 2001, "TP.HCM");
			s2.setGpa(8.2);
			s2.setNote("Cảnh báo chuyên cần");
			students.add(s2);

			// Modules
			modules.add("Toán cao cấp");
			moduleCredits.put("Toán cao cấp", 3);
			modules.add("Lập trình Mobile");
			moduleCredits.put("Lập trình Mobile", 4);
			modules.add("Cơ sở dữ liệu");
			moduleCredits.put("Cơ sở dữ liệu", 3);
			modules.add("Mạng máy tính");
			moduleCredits.put("Mạng máy tính", 4);

			// Scores chi tiết
			scores.add(new Score("SV001", "Nguyễn Văn A", "Toán cao cấp", 2, 8, 7));
			scores.add(new Score("SV001", "Nguyễn Văn A", "Lập trình Mobile", 1, 9, 8));
			scores.add(new Score("SV002", "Trần Thị B", "Toán cao cấp", 6, 7, 6)); // Attendance cao -> cảnh báo
			scores.add(new Score("SV002", "Trần Thị B", "Cơ sở dữ liệu", 3, 8, 9));

			// Attendance
			attendanceMap.put("SV001", 2);
			attendanceMap.put("SV002", 6);
		}

		public void setModuleCredits(String module, int credits) {
			moduleCredits.put(module, credits);
		}

		public int getModuleCredits(String module) {
			return moduleCredits.getOrDefault(module, 0);
		}

		@Override
		public List<Student> getAllStudents() {
			// Tính GPA và note động
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
					.filter(st -> st.getId().contains(keyword)
							|| st.getFullName().toLowerCase().contains(keyword.toLowerCase()))
					.collect(Collectors.toList());
		}

		@Override
		public List<String> getAllModules() {
			return new ArrayList<>(modules);
		}

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

		// Method mới: Attendance
		@Override
		public Map<String, Integer> getAttendanceByStudent(String studentId) {
			Map<String, Integer> map = new HashMap<>();
			map.put(studentId, attendanceMap.getOrDefault(studentId, 0));
			return map;
		}

		// Method mới: Scores cho student
		@Override
		public List<Score> getAllScoresForStudent(String studentId) {
			return scores.stream().filter(sc -> sc.getStudentId().equals(studentId)).collect(Collectors.toList());
		}

		// Tính GPA cho student
		private double calculateGpa(String studentId) {
			List<Score> studentScores = getAllScoresForStudent(studentId);
			if (studentScores.isEmpty())
				return 0.0;
			double total = 0.0;
			for (Score sc : studentScores) {
				total += sc.calculateModuleGrade();
			}
			return total / studentScores.size();
		}

		// Tạo ghi chú dựa trên attendance
		private String generateNote(String studentId) {
			int att = attendanceMap.getOrDefault(studentId, 0);
			if (att > ATTENDANCE_THRESHOLD) {
				return "Cảnh báo chuyên cần (nghỉ " + att + " ngày)";
			}
			return "Tốt - Chuyên cần cao";
		}
	}

	// Constructor - Mở rộng với load dynamic combos
	public ClientGUI() {
		// Cài đặt window
		setTitle("QUẢN LÍ SINH VIÊN");
		setSize(1100, 700);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setBackground(BG);
		setLayout(new BorderLayout());

		// Look and Feel
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			// Tăng contrast cho dễ đọc
			UIManager.put("TextField.foreground", TEXT);
			UIManager.put("Label.foreground", TEXT);
			UIManager.put("Table.foreground", TEXT);
			UIManager.put("TableHeader.foreground", Color.WHITE);
		} catch (Exception e) {
			System.err.println("Không load Nimbus: " + e.getMessage());
		}

		// Khởi tạo manager
		manager = new MockStudentManager();

		// Local modules
		localModules.addAll(Arrays.asList("Toán cao cấp", "Lập trình Mobile", "Cơ sở dữ liệu", "Mạng máy tính"));

		// Thêm components
		add(createSidebar(), BorderLayout.WEST);
		add(createHeader(), BorderLayout.NORTH);
		add(createContent(), BorderLayout.CENTER);
		add(createFooter(), BorderLayout.SOUTH);

		// Load initial data
		loadStudents();
		loadModules();
		loadModuleScores();

		// Timer cho footer
		new javax.swing.Timer(1000, e -> {
			String time = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(new Date());
			footerLabel.setText(" Hệ thống sẵn sàng | " + time);
		}).start();

		// Log khởi tạo
		System.out.println(
				"ClientGUI khởi tạo thành công - Ngày: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
	}

	// Tạo sidebar - Không thay đổi nhiều
	private JPanel createSidebar() {
		JPanel sidebar = new JPanel(new BorderLayout());
		sidebar.setPreferredSize(new Dimension(220, 0));
		sidebar.setBackground(new Color(18, 33, 56));
		sidebar.setBorder(new EmptyBorder(12, 12, 12, 12));

		// Logo
		JLabel logo = new JLabel("<html><span style='color:#ffffff;font-weight:700;font-size:18px'>QUẢN LÝ</span><br>"
				+ "<span style='color:#9fb6ff;font-weight:600;font-size:18px'>SINH VIÊN</span></html>");
		logo.setBorder(new EmptyBorder(6, 6, 12, 6));
		sidebar.add(logo, BorderLayout.NORTH);

		// Menu panel
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

	// Method riêng cho side menu item
	private JLabel createSideMenuItem(String text, String viewKey) {
		JLabel lbl = new JLabel(text);
		lbl.setForeground(Color.WHITE);
		lbl.setOpaque(true);
		lbl.setBackground(new Color(23, 34, 50));
		lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
		lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				switchView(viewKey);
				// Reset background cho tất cả items
				Component[] components = ((JPanel) lbl.getParent()).getComponents();
				for (Component c : components) {
					if (c instanceof JLabel) {
						((JLabel) c).setBackground(new Color(23, 34, 50));
					}
				}
				lbl.setBackground(new Color(36, 58, 92));
			}
		});
		return lbl;
	}

	// Tạo header
	private JPanel createHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setPreferredSize(new Dimension(1100, 48));
		header.setBackground(CARD); // Sử dụng CARD xám nhạt
		JLabel lbl = new JLabel("HỆ THỐNG QUẢN LÝ SINH VIÊN");
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
		lbl.setForeground(TEXT); // Trắng nổi bật
		lbl.setBorder(new EmptyBorder(0, 12, 0, 0));
		header.add(lbl, BorderLayout.WEST);

		// Thêm nút kết nối nếu cần (tạm comment)
		// JButton connectBtn = new JButton("Kết nối");
		// header.add(connectBtn, BorderLayout.EAST);

		return header;
	}

	// Tạo content với CardLayout
	private JPanel createContent() {
		contentPanel = new JPanel(new CardLayout());
		contentPanel.add(buildStudentsPanel(), "students");
		contentPanel.add(buildModulesPanel(), "modules");
		contentPanel.add(buildScoresPanel(), "scores");
		contentPanel.add(buildAttendancePanel(), "attendance");
		return contentPanel;
	}

	// Tạo footer
	private JPanel createFooter() {
		JPanel footer = new JPanel(new BorderLayout());
		footer.setPreferredSize(new Dimension(1100, 36));
		footer.setBackground(new Color(34, 42, 56));
		footerLabel = new JLabel(" Hệ thống sẵn sàng");
		footerLabel.setForeground(Color.WHITE);
		footerLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
		footer.add(footerLabel, BorderLayout.WEST);
		return footer;
	}

	// Switch view
	private void switchView(String key) {
		CardLayout cl = (CardLayout) contentPanel.getLayout();
		cl.show(contentPanel, key);
		// Load data tương ứng
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

	// =================== STUDENTS PANEL ===================
	private JPanel buildStudentsPanel() {
		// Panel chính
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setOpaque(false);
		panel.setBackground(CARD);

		// Top buttons
		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		top.setOpaque(false);
		JButton btnAdd = coloredButton("➕ Thêm", ADD_COLOR);
		JButton btnSearch = coloredButton("🔍 Tìm kiếm", PRIMARY);
		top.add(btnAdd);
		top.add(btnSearch);
		panel.add(top, BorderLayout.NORTH);

		// Bảng với cột mới
		String[] columns = { "Mã SV", "Họ và tên", "Lớp", "Năm sinh", "Quê quán", "Điểm TB", "Ghi chú" };
		studentModel = new DefaultTableModel(columns, 0);
		studentTable = new JTable(studentModel);
		styleTable(studentTable); // Style với alt row dễ đọc

		// Custom renderer cho cột mã SV và họ tên (màu nền khác)
		studentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (isSelected) {
					c.setBackground(new Color(200, 230, 255));
					c.setForeground(Color.BLACK);
				} else {
					Color base = (row % 2 == 0) ? CARD : ALT_ROW;
					c.setBackground(base);
					c.setForeground(TEXT);
					if (column == 0 || column == 1) {
						c.setBackground(new Color(135, 206, 250));
					}
				}
				setBorder(new EmptyBorder(0, 0, 0, 0));
				return c;
			}
		});

		JScrollPane sc = new JScrollPane(studentTable);
		sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
		panel.add(sc, BorderLayout.CENTER);

		// Bottom buttons
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		bottom.setOpaque(false);
		JButton btnEdit = coloredButton("✏️ Sửa", EDIT_COLOR);
		JButton btnDelete = coloredButton("🗑️ Xóa", DELETE_COLOR);
		JButton btnView = coloredButton("👁 Xem chi tiết", VIEW_COLOR);
		bottom.add(btnEdit);
		bottom.add(btnDelete);
		bottom.add(btnView);
		panel.add(bottom, BorderLayout.SOUTH);

		// Action listeners
		btnAdd.addActionListener(e -> showAddDialog());
		btnSearch.addActionListener(e -> searchStudents());
		btnEdit.addActionListener(e -> {
			int r = studentTable.getSelectedRow();
			if (r == -1) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			showEditStudentDialog((String) studentModel.getValueAt(r, 0));
		});
		btnDelete.addActionListener(e -> {
			int r = studentTable.getSelectedRow();
			if (r == -1) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			deleteStudent((String) studentModel.getValueAt(r, 0), (String) studentModel.getValueAt(r, 1));
		});
		btnView.addActionListener(e -> {
			int r = studentTable.getSelectedRow();
			if (r == -1) {
				JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			showDetailDialog((String) studentModel.getValueAt(r, 0));
		});

		return panel;
	}

	// Load students với cột mới
	private void loadStudents() {
		try {
			studentModel.setRowCount(0);
			List<Student> list = manager.getAllStudents();
			for (Student s : list) {
				studentModel.addRow(new Object[] { s.getId(), s.getFullName(), s.getClazz(), s.getBirthYear(),
						s.getHometown(), String.format("%.2f", s.getGpa()), // Định dạng 2 chữ số
						s.getNote() });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu sinh viên: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	// Dialog thêm sinh viên - Lớp tự điền (JTextField), Năm sinh JComboBox, Quê
	// quán JComboBox
	private void showAddDialog() {
		JDialog dialog = new JDialog(this, "Thêm sinh viên mới", true);
		dialog.setLayout(new BorderLayout(6, 6));

		// Quê quán combo
		String[] hometowns = { "Hà Nội", "TP.HCM", "Đà Nẵng", "Cần Thơ", "Khác" };
		JComboBox<String> cbHometown = new JComboBox<>(hometowns);

		// Năm sinh combo
		JComboBox<Integer> cbYear = new JComboBox<>();
		for (int y = 1950; y <= 2010; y++) {
			cbYear.addItem(y);
		}
		cbYear.setSelectedItem(2000);

		// Lớp tự điền
		JTextField txtClass = new JTextField(); // Tự điền lớp

		JPanel p = new JPanel(new GridLayout(6, 2, 6, 6));
		JTextField txtId = new JTextField();
		JTextField txtName = new JTextField();
		p.add(new JLabel("Mã SV: *"));
		p.add(txtId);
		p.add(new JLabel("Họ và tên: *"));
		p.add(txtName);
		p.add(new JLabel("Lớp:"));
		p.add(txtClass); // Tự điền
		p.add(new JLabel("Năm sinh:"));
		p.add(cbYear); // Combo
		p.add(new JLabel("Quê quán:"));
		p.add(cbHometown); // Combo

		// Validation label
		JLabel validationLabel = new JLabel("");
		validationLabel.setForeground(Color.RED);
		p.add(new JLabel(""));
		p.add(validationLabel);

		JButton btnOk = coloredButton("Lưu", ADD_COLOR);
		JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
		JPanel bottom = new JPanel();
		bottom.add(btnOk);
		bottom.add(btnCancel);

		dialog.add(p, BorderLayout.CENTER);
		dialog.add(bottom, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(this);

		// Action cho OK
		btnOk.addActionListener(e -> {
			String id = txtId.getText().trim();
			String name = txtName.getText().trim();
			String clazz = txtClass.getText().trim(); // Lớp tự điền
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
				JOptionPane.showMessageDialog(dialog, "Thêm sinh viên thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				validationLabel.setText("Lỗi: " + ex.getMessage());
			}
		});

		btnCancel.addActionListener(e -> dialog.dispose());

		dialog.setVisible(true);
	}

	// Dialog sửa - Tương tự add nhưng load data, Lớp tự điền
	private void showEditStudentDialog(String id) {
		try {
			Student s = manager.getStudentById(id);
			if (s == null) {
				JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JDialog dialog = new JDialog(this, "Sửa thông tin sinh viên", true);
			dialog.setLayout(new BorderLayout(6, 6));

			// Quê quán combo
			String[] hometowns = { "Hà Nội", "TP.HCM", "Đà Nẵng", "Cần Thơ", "Khác" };
			JComboBox<String> cbHometown = new JComboBox<>(hometowns);
			cbHometown.setSelectedItem(s.getHometown());

			// Năm sinh combo
			JComboBox<Integer> cbYear = new JComboBox<>();
			for (int y = 1950; y <= 2010; y++) {
				cbYear.addItem(y);
			}
			cbYear.setSelectedItem(s.getBirthYear());

			// Lớp tự điền
			JTextField txtClass = new JTextField(s.getClazz());

			JPanel p = new JPanel(new GridLayout(6, 2, 6, 6));
			JTextField txtId = new JTextField(s.getId());
			txtId.setEditable(false); // Không sửa ID
			JTextField txtName = new JTextField(s.getFullName());
			p.add(new JLabel("Mã SV:"));
			p.add(txtId);
			p.add(new JLabel("Họ và tên: *"));
			p.add(txtName);
			p.add(new JLabel("Lớp:"));
			p.add(txtClass); // Tự điền
			p.add(new JLabel("Năm sinh:"));
			p.add(cbYear); // Combo
			p.add(new JLabel("Quê quán:"));
			p.add(cbHometown); // Combo

			JLabel validationLabel = new JLabel("");
			validationLabel.setForeground(Color.RED);
			p.add(new JLabel(""));
			p.add(validationLabel);

			JButton btnOk = coloredButton("Cập nhật", EDIT_COLOR);
			JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
			JPanel bottom = new JPanel();
			bottom.add(btnOk);
			bottom.add(btnCancel);

			dialog.add(p, BorderLayout.CENTER);
			dialog.add(bottom, BorderLayout.SOUTH);
			dialog.pack();
			dialog.setLocationRelativeTo(this);

			btnOk.addActionListener(e -> {
				String name = txtName.getText().trim();
				String clazz = txtClass.getText().trim(); // Lớp tự điền
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
					JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!", "Thành công",
							JOptionPane.INFORMATION_MESSAGE);
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

	// Dialog xem chi tiết - Với bảng điểm từng học phần
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

			// Info panel
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

			// Scores table
			String[] scoreColumns = { "Học phần", "Chuyên cần", "KT1", "Thi", "Điểm môn" };
			DefaultTableModel scoreModel = new DefaultTableModel(scoreColumns, 0);
			JTable detailScoreTable = new JTable(scoreModel);
			styleTable(detailScoreTable); // Style dễ đọc

			List<Score> studentScores = manager.getAllScoresForStudent(id);
			for (Score sc : studentScores) {
				scoreModel.addRow(new Object[] { sc.getModule(), sc.getAttendance(), sc.getTest1(), sc.getExam(),
						String.format("%.2f", sc.calculateModuleGrade()) });
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
			JOptionPane.showMessageDialog(this, "Lỗi hiển thị chi tiết: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Xóa sinh viên
	private void deleteStudent(String id, String name) {
		int confirm = JOptionPane.showConfirmDialog(this,
				"Bạn có chắc muốn xóa sinh viên '" + name + "' (ID: " + id + ")?", "Xác nhận xóa",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				manager.deleteStudent(id);
				loadStudents();
				JOptionPane.showMessageDialog(this, "Xóa sinh viên thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// Tìm kiếm
	private void searchStudents() {
		String keyword = JOptionPane.showInputDialog(this, "Nhập mã hoặc tên sinh viên để tìm:");
		if (keyword == null || keyword.trim().isEmpty())
			return;
		try {
			studentModel.setRowCount(0);
			List<Student> results = manager.searchStudents(keyword);
			if (results.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả!", "Thông báo",
						JOptionPane.INFORMATION_MESSAGE);
			}
			for (Student s : results) {
				studentModel.addRow(new Object[] { s.getId(), s.getFullName(), s.getClazz(), s.getBirthYear(),
						s.getHometown(), String.format("%.2f", s.getGpa()), s.getNote() });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	// =================== MODULES PANEL ===================
	private JPanel buildModulesPanel() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setOpaque(false);
		panel.setBackground(CARD); // Nền card

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		top.setOpaque(false);
		JButton btnAdd = coloredButton("➕ Thêm học phần", MODULE_ADD);
		JButton btnDelete = coloredButton("🗑️ Xóa học phần", DELETE_COLOR);
		JButton btnRefresh = coloredButton("🔄 Làm mới", PRIMARY);
		top.add(btnAdd);
		top.add(btnDelete);
		top.add(btnRefresh);
		panel.add(top, BorderLayout.NORTH);

		String[] columns = { "Tên học phần", "Số tín chỉ" };
		moduleModel = new DefaultTableModel(columns, 0);
		moduleTable = new JTable(moduleModel);
		styleTable(moduleTable);
		JScrollPane sc = new JScrollPane(moduleTable);
		sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
		panel.add(sc, BorderLayout.CENTER);

		// Actions
		btnAdd.addActionListener(e -> addModuleDialog());
		btnDelete.addActionListener(e -> deleteModule());
		btnRefresh.addActionListener(e -> loadModules());

		return panel;
	}

	// Load modules
	private void loadModules() {
		try {
			moduleModel.setRowCount(0);
			moduleComboBox.removeAllItems();
			List<String> list = manager.getAllModules();
			if (list.isEmpty()) {
				list.addAll(localModules);
			}
			for (String m : list) {
				int credits = ((MockStudentManager) manager).getModuleCredits(m);
				moduleModel.addRow(new Object[] { m, credits });
				moduleComboBox.addItem(m);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi tải học phần: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Thêm module
	private void addModuleDialog() {
		JDialog dialog = new JDialog(this, "Thêm học phần mới", true);
		dialog.setLayout(new BorderLayout(6, 6));

		JPanel p = new JPanel(new GridLayout(3, 2, 6, 6));
		JTextField txtName = new JTextField();
		JSpinner spCredits = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
		p.add(new JLabel("Tên học phần: *"));
		p.add(txtName);
		p.add(new JLabel("Số tín chỉ: *"));
		p.add(spCredits);

		// Validation label
		JLabel validationLabel = new JLabel("");
		validationLabel.setForeground(Color.RED);
		p.add(new JLabel(""));
		p.add(validationLabel);

		JButton btnOk = coloredButton("Lưu", ADD_COLOR);
		JButton btnCancel = coloredButton("Hủy", DELETE_COLOR);
		JPanel bottom = new JPanel();
		bottom.add(btnOk);
		bottom.add(btnCancel);

		dialog.add(p, BorderLayout.CENTER);
		dialog.add(bottom, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(this);

		// Action cho OK
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
				JOptionPane.showMessageDialog(dialog, "Thêm học phần thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				validationLabel.setText("Lỗi: " + ex.getMessage());
			}
		});

		btnCancel.addActionListener(e -> dialog.dispose());

		dialog.setVisible(true);
	}

	// Xóa module
	private void deleteModule() {
		int r = moduleTable.getSelectedRow();
		if (r == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn học phần!", "Thông báo", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String name = (String) moduleModel.getValueAt(r, 0);
		int confirm = JOptionPane.showConfirmDialog(this, "Xóa học phần '" + name + "'? (Sẽ xóa tất cả điểm liên quan)",
				"Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				manager.deleteModule(name);
				loadModules();
				JOptionPane.showMessageDialog(this, "Xóa học phần thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// =================== SCORES PANEL ===================
	private JPanel buildScoresPanel() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setOpaque(false);
		panel.setBackground(CARD); // Nền card

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		top.setOpaque(false);
		top.add(new JLabel("Học phần: "));
		moduleComboBox = new JComboBox<>();
		moduleComboBox.setPreferredSize(new Dimension(260, 28));
		moduleComboBox.setBorder(new LineBorder(PRIMARY, 1, true));
		top.add(moduleComboBox);
		JButton btnRefresh = coloredButton("🔄 Làm mới", PRIMARY);
		top.add(btnRefresh);
		panel.add(top, BorderLayout.NORTH);

		String[] columns = { "Mã SV", "Họ và tên", "Học phần", "Chuyên cần", "KT1", "Thi" };
		scoreModel = new DefaultTableModel(columns, 0);
		scoreTable = new JTable(scoreModel);
		styleTable(scoreTable);
		JScrollPane sc = new JScrollPane(scoreTable);
		sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
		panel.add(sc, BorderLayout.CENTER);

		// Actions
		moduleComboBox.addActionListener(e -> loadModuleScores());
		btnRefresh.addActionListener(e -> loadModuleScores());

		return panel;
	}

	// Load scores theo module
	private void loadModuleScores() {
		try {
			scoreModel.setRowCount(0);
			String module = (String) moduleComboBox.getSelectedItem();
			if (module == null || module.isEmpty())
				return;
			List<Score> list = manager.getScoresByModule(module);
			for (Score s : list) {
				scoreModel.addRow(new Object[] { s.getStudentId(), s.getFullName(), s.getModule(), s.getAttendance(),
						s.getTest1(), s.getExam() });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi tải điểm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
	}

	// =================== ATTENDANCE PANEL ===================
	private JPanel buildAttendancePanel() {
		JPanel panel = new JPanel(new BorderLayout(8, 8));
		panel.setOpaque(false);
		panel.setBackground(CARD); // Nền card

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		top.setOpaque(false);
		JButton btnRefresh = coloredButton("🔄 Làm mới", PRIMARY);
		JButton btnEditAttendance = coloredButton("✏️ Sửa chuyên cần", EDIT_COLOR); // Thêm nút sửa
		top.add(btnRefresh);
		top.add(btnEditAttendance);
		panel.add(top, BorderLayout.NORTH);

		// Thêm cột Số tín chỉ
		String[] columns = { "Mã SV", "Họ và tên", "Số ngày nghỉ", "Số tín chỉ" };
		attendanceModel = new DefaultTableModel(columns, 0);
		attendanceTable = new JTable(attendanceModel);
		attendanceTable.setRowSelectionAllowed(true);
		styleTable(attendanceTable);
		JScrollPane sc = new JScrollPane(attendanceTable);
		sc.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220)), new EmptyBorder(8, 8, 8, 8)));
		panel.add(sc, BorderLayout.CENTER);

		// Actions
		btnRefresh.addActionListener(e -> loadAttendance());
		btnEditAttendance.addActionListener(e -> editAttendance());

		return panel;
	}

	// Load attendance với cột tín chỉ
	private void loadAttendance() {
		try {
			attendanceModel.setRowCount(0);
			List<Student> list = manager.getAllStudents();
			for (Student s : list) {
				int daysMissed = manager.getAttendanceByStudent(s.getId()).getOrDefault(s.getId(), 0);
				List<Score> studentScores = manager.getAllScoresForStudent(s.getId());
				int totalCredits = 0;
				for (Score sc : studentScores) {
					totalCredits += ((MockStudentManager) manager).getModuleCredits(sc.getModule());
				}
				attendanceModel.addRow(new Object[] { s.getId(), s.getFullName(), daysMissed, totalCredits });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Lỗi tải chuyên cần: " + e.getMessage(), "Lỗi",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Sửa attendance (mới thêm)
	private void editAttendance() {
		int r = attendanceTable.getSelectedRow();
		if (r == -1) {
			JOptionPane.showMessageDialog(this, "Vui lòng chọn sinh viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String id = (String) attendanceModel.getValueAt(r, 0);
		String current = JOptionPane.showInputDialog(this, "Số ngày nghỉ cho " + id + ":", "Sửa chuyên cần",
				JOptionPane.QUESTION_MESSAGE);
		if (current != null) {
			try {
				int days = Integer.parseInt(current.trim());
				if (days < 0) {
					JOptionPane.showMessageDialog(this, "Số ngày phải >= 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// Cập nhật map (giả lập)
				((MockStudentManager) manager).attendanceMap.put(id, days);
				loadAttendance();
				loadStudents(); // Refresh note và gpa
				JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Số ngày không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// =================== STYLING METHODS ===================
	private JButton coloredButton(String text, Color bg) {
		JButton b = new JButton(text);
		b.setBackground(bg);
		b.setForeground(Color.WHITE); // Trắng dễ đọc
		b.setFocusPainted(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.setBorder(new EmptyBorder(8, 12, 8, 12));
		b.setFont(new Font("Segoe UI", Font.BOLD, 13));
		return b;
	}

	// Style table với alt row dễ đọc hơn, text trắng nổi trên nền trầm
	private void styleTable(JTable t) {
		t.setRowHeight(28);
		t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		t.setForeground(TEXT); // Text trắng

		JTableHeader h = t.getTableHeader();
		h.setFont(new Font("Segoe UI", Font.BOLD, 13));
		h.setBackground(PRIMARY);
		h.setForeground(Color.BLACK);
		h.setPreferredSize(new Dimension(h.getPreferredSize().width, 34));

		t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {
				Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
				if (isSelected) {
					c.setBackground(new Color(200, 230, 255)); // Selected sáng
					c.setForeground(Color.BLACK); // Text đen khi select
				} else {
					c.setBackground((row % 2 == 0) ? CARD : ALT_ROW); // Alt row xám đậm
					c.setForeground(TEXT); // Trắng nổi
				}
				setBorder(new EmptyBorder(0, 0, 0, 0));
				return c;
			}
		});

		t.setGridColor(new Color(100, 100, 100)); // Grid xám đậm
		t.setSelectionBackground(new Color(200, 230, 255));
	}

	// =================== MAIN METHOD ===================
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			new ClientGUI().setVisible(true);
		});
	}
}