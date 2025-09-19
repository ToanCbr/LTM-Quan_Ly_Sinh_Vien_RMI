package student;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface StudentManager extends Remote {
	// Quản lý sinh viên
	boolean addStudent(Student s) throws RemoteException;

	boolean updateStudent(Student s) throws RemoteException;

	boolean deleteStudent(String id) throws RemoteException;

	Student getStudentById(String id) throws RemoteException;

	List<Student> getAllStudents() throws RemoteException;

	// Quản lý học phần
	List<String> getAllModules() throws RemoteException;

	List<Student> getStudentsWithScoresForModule(String moduleName) throws RemoteException;

	boolean updateScoresForModule(String moduleName, Map<String, Student.SubjectScores> updates) throws RemoteException;

	// Login / Register
	boolean login(String username, String password) throws RemoteException;

	boolean register(String username, String password) throws RemoteException;
}
