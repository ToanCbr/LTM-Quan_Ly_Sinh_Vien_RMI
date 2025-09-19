package student;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentManagerImpl extends UnicastRemoteObject implements StudentManager {
	private static final long serialVersionUID = 1L;
	private Map<String, Student> students;
	private final File storageFile = new File("students.csv");
	private final List<String> defaultModules = Arrays.asList("Lập Trình Mạng", "Kỹ Năng Mềm");

	// Quản lý user
	private Map<String, String> users;
	private final File userFile = new File("users.csv");

	protected StudentManagerImpl() throws RemoteException {
		super();
		students = new HashMap<>();
		users = new HashMap<>();
		loadFromFile();
		loadUsers();
	}

	// =================== USER LOGIN/REGISTER ===================
	private synchronized void loadUsers() {
		users.clear();
		if (!userFile.exists()) {
			// tạo user mặc định
			users.put("admin", "123");
			saveUsers();
			return;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty() || line.startsWith("Username,"))
					continue;
				String[] parts = line.split(",");
				if (parts.length == 2) {
					users.put(parts[0].trim(), parts[1].trim());
				}
			}
		} catch (IOException e) {
			System.err.println("Load users error: " + e.getMessage());
		}
	}

	private synchronized void saveUsers() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(userFile))) {
			bw.write("Username,Password");
			bw.newLine();
			for (Map.Entry<String, String> entry : users.entrySet()) {
				bw.write(entry.getKey() + "," + entry.getValue());
				bw.newLine();
			}
		} catch (IOException e) {
			System.err.println("Save users error: " + e.getMessage());
		}
	}

	@Override
	public synchronized boolean login(String username, String password) throws RemoteException {
		if (username == null || password == null)
			return false;
		return password.equals(users.get(username));
	}

	@Override
	public synchronized boolean register(String username, String password) throws RemoteException {
		if (username == null || password == null)
			return false;
		if (users.containsKey(username))
			return false; // user tồn tại
		users.put(username, password);
		saveUsers();
		return true;
	}

	// =================== STUDENT CRUD ===================
	private synchronized void loadFromFile() {
		students.clear();
		if (!storageFile.exists())
			return;

		try (BufferedReader br = new BufferedReader(new FileReader(storageFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty() || line.startsWith("ID,"))
					continue;

				String[] parts = line.split(",");
				String id = parts[0].trim();
				String name = parts[1].trim();
				int year = Integer.parseInt(parts[2].trim());
				String email = parts[3].trim();
				String className = parts[4].trim();

				Map<String, Student.SubjectScores> scoresMap = new HashMap<>();
				if (parts.length > 5 && parts[5].startsWith("Subjects:")) {
					String subjectsStr = parts[5].substring(9);
					String[] modulePairs = subjectsStr.split("\\|");
					for (String pair : modulePairs) {
						if (pair.isEmpty())
							continue;
						String[] modParts = pair.split(":");
						if (modParts.length == 2) {
							String moduleName = modParts[0].trim();
							Student.SubjectScores scores = Student.SubjectScores.fromString(modParts[1].trim());
							scoresMap.put(moduleName, scores);
						}
					}
				}
				// đảm bảo có modules mặc định
				for (String mod : defaultModules) {
					if (!scoresMap.containsKey(mod)) {
						scoresMap.put(mod, new Student.SubjectScores());
					}
				}
				Student s = new Student(id, name, year, email, className, scoresMap);
				students.put(id, s);
			}
		} catch (Exception e) {
			System.err.println("Load CSV error: " + e.getMessage());
		}
	}

	private synchronized void saveToFile() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(storageFile))) {
			bw.write("ID,Name,Year,Email,Class,Subjects");
			bw.newLine();
			for (Student s : students.values()) {
				StringBuilder subjects = new StringBuilder("Subjects:");
				boolean first = true;
				for (Map.Entry<String, Student.SubjectScores> entry : s.getSubjectScores().entrySet()) {
					if (!first)
						subjects.append("|");
					subjects.append(entry.getKey()).append(":").append(entry.getValue().toString());
					first = false;
				}
				bw.write(s.getId() + "," + s.getName() + "," + s.getYear() + "," + s.getEmail() + "," + s.getClassName()
						+ "," + subjects.toString());
				bw.newLine();
			}
		} catch (IOException e) {
			System.err.println("Save CSV error: " + e.getMessage());
		}
	}

	@Override
	public synchronized boolean addStudent(Student s) throws RemoteException {
		if (s == null || s.getId() == null)
			return false;
		if (students.containsKey(s.getId()))
			return false;
		students.put(s.getId(), s);
		saveToFile();
		return true;
	}

	@Override
	public synchronized boolean updateStudent(Student s) throws RemoteException {
		if (s == null || s.getId() == null)
			return false;
		if (!students.containsKey(s.getId()))
			return false;
		students.put(s.getId(), s);
		saveToFile();
		return true;
	}

	@Override
	public synchronized boolean deleteStudent(String id) throws RemoteException {
		if (id == null)
			return false;
		if (students.remove(id) != null) {
			saveToFile();
			return true;
		}
		return false;
	}

	@Override
	public synchronized Student getStudentById(String id) throws RemoteException {
		Student s = students.get(id);
		if (s != null) {
			return new Student(s.getId(), s.getName(), s.getYear(), s.getEmail(), s.getClassName(),
					s.getSubjectScores());
		}
		return null;
	}

	@Override
	public synchronized List<Student> getAllStudents() throws RemoteException {
		List<Student> result = new ArrayList<>();
		for (Student s : students.values()) {
			result.add(new Student(s.getId(), s.getName(), s.getYear(), s.getEmail(), s.getClassName(),
					s.getSubjectScores()));
		}
		return result;
	}

	@Override
	public synchronized List<String> getAllModules() throws RemoteException {
		return new ArrayList<>(defaultModules);
	}

	@Override
	public synchronized List<Student> getStudentsWithScoresForModule(String moduleName) throws RemoteException {
		List<Student> result = new ArrayList<>();
		for (Student s : students.values()) {
			Map<String, Student.SubjectScores> copyScores = new HashMap<>(s.getSubjectScores());
			Student copy = new Student(s.getId(), s.getName(), s.getYear(), s.getEmail(), s.getClassName(), copyScores);
			result.add(copy);
		}
		return result;
	}

	@Override
	public synchronized boolean updateScoresForModule(String moduleName, Map<String, Student.SubjectScores> updates)
			throws RemoteException {
		if (moduleName == null || updates == null)
			return false;
		boolean success = true;
		for (Map.Entry<String, Student.SubjectScores> entry : updates.entrySet()) {
			Student s = students.get(entry.getKey());
			if (s != null) {
				s.updateScoresForModule(moduleName, entry.getValue());
				success &= updateStudent(s);
			} else {
				success = false;
			}
		}
		return success;
	}

	// =================== INIT SAMPLE DATA ===================
	private synchronized void initSampleData() {
		if (!students.isEmpty())
			return; // nếu đã có dữ liệu thì không thêm

		try {
			Student sv1 = new Student("SV001", "Nguyễn Văn A", 2000, "ngva@example.com", "CNTT16-01");
			sv1.addScore("Lập Trình Mạng", new Student.SubjectScores(9, 8, 7));
			sv1.addScore("Kỹ Năng Mềm", new Student.SubjectScores(8, 9, 8));
			students.put(sv1.getId(), sv1);

			Student sv2 = new Student("SV002", "Trần Thị B", 2001, "ttb@example.com", "CNTT16-02");
			sv2.addScore("Lập Trình Mạng", new Student.SubjectScores(7, 8, 9));
			sv2.addScore("Kỹ Năng Mềm", new Student.SubjectScores(8, 7, 7.5));
			students.put(sv2.getId(), sv2);

			Student sv3 = new Student("SV003", "Lê Văn C", 2000, "lvc@example.com", "CNTT16-03");
			sv3.addScore("Lập Trình Mạng", new Student.SubjectScores(7, 7.5, 8));
			sv3.addScore("Kỹ Năng Mềm", new Student.SubjectScores(8, 8, 9));
			students.put(sv3.getId(), sv3);

			Student sv4 = new Student("SV004", "Phạm Thị D", 2002, "ptd@example.com", "CNTT16-01");
			sv4.addScore("Lập Trình Mạng", new Student.SubjectScores(8.5, 9, 7.5));
			sv4.addScore("Kỹ Năng Mềm", new Student.SubjectScores(9, 8, 8));
			students.put(sv4.getId(), sv4);

			Student sv5 = new Student("SV005", "Hoàng Văn E", 2001, "hve@example.com", "CNTT16-02");
			sv5.addScore("Lập Trình Mạng", new Student.SubjectScores(8, 8, 9));
			sv5.addScore("Kỹ Năng Mềm", new Student.SubjectScores(7, 9, 8));
			students.put(sv5.getId(), sv5);

			saveToFile();
			System.out.println("Đã khởi tạo dữ liệu mẫu thành công!");
		} catch (Exception e) {
			System.err.println("Lỗi khi khởi tạo dữ liệu mẫu: " + e.getMessage());
		}
	}

}
