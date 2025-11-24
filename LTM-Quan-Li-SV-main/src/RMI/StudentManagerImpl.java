package RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class StudentManagerImpl extends UnicastRemoteObject implements StudentManager {

    private final Map<String, Student> students = new LinkedHashMap<>(); // key=id
    private final Set<String> modules = new LinkedHashSet<>();

    protected StudentManagerImpl() throws RemoteException {
        super();
        // dữ liệu mẫu
        modules.add("Toán cao cấp");
        modules.add("Lập trình Mobile");
        modules.add("Cơ sở dữ liệu");
        modules.add("Mạng máy tính");

        // thêm vài sinh viên mẫu
        Student s1 = new Student("SV001","Nguyễn Văn A",2000,"a@email.com","CNTT1");
        Student s2 = new Student("SV002","Trần Thị B",2001,"b@email.com","CNTT2");
        students.put(s1.getId(), s1);
        students.put(s2.getId(), s2);
    }

    @Override
    public boolean addStudent(Student s) throws RemoteException {
        if(students.containsKey(s.getId())) return false;
        students.put(s.getId(), s);
        return true;
    }

    @Override
    public boolean updateStudent(Student s) throws RemoteException {
        if(!students.containsKey(s.getId())) return false;
        students.put(s.getId(), s);
        return true;
    }

    @Override
    public boolean deleteStudent(String id) throws RemoteException {
        return students.remove(id) != null;
    }

    @Override
    public Student getStudentById(String id) throws RemoteException {
        return students.get(id);
    }

    @Override
    public List<Student> getAllStudents() throws RemoteException {
        return new ArrayList<>(students.values());
    }

    @Override
    public List<String> getAllModules() throws RemoteException {
        return new ArrayList<>(modules);
    }

    @Override
    public List<Student> getStudentsWithScoresForModule(String moduleName) throws RemoteException {
        List<Student> result = new ArrayList<>();
        for(Student s : students.values()) {
            // Nếu sinh viên chưa có điểm môn này, khởi tạo default
            if(!s.getSubjectScores().containsKey(moduleName)){
                s.getSubjectScores().put(moduleName,new Student.SubjectScores(0.0,0.0,0.0));
            }
            result.add(s);
        }
        return result;
    }

    @Override
    public boolean updateScoresForModule(String moduleName, Map<String, Student.SubjectScores> updates) throws RemoteException {
        boolean updated = false;
        for(Map.Entry<String, Student.SubjectScores> en : updates.entrySet()){
            String sid = en.getKey();
            Student.SubjectScores sc = en.getValue();
            Student s = students.get(sid);
            if(s != null){
                s.getSubjectScores().put(moduleName, sc);
                updated = true;
            }
        }
        return updated;
    }
}
