package RMI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private int year;
    private String email;
    private String className;
    private Map<String, SubjectScores> subjectScores;  

    public static class SubjectScores implements Serializable {
        private static final long serialVersionUID = 1L;
        private double attendance;  
        private double test1;       
        private double exam;        

        public SubjectScores() {
            this(0,0,0);
        }

        public SubjectScores(double attendance, double test1, double exam) {
            this.attendance = attendance;
            this.test1 = test1;
            this.exam = exam;
        }

        public double getAttendance() { return attendance; }
        public void setAttendance(double attendance) { this.attendance = attendance; }
        public double getTest1() { return test1; }
        public void setTest1(double test1) { this.test1 = test1; }
        public double getExam() { return exam; }
        public void setExam(double exam) { this.exam = exam; }

        @Override
        public String toString() {
            return attendance + "," + test1 + "," + exam;
        }

        public static SubjectScores fromString(String str) {
            String[] parts = str.split(",");
            if(parts.length == 3) {
                try {
                    return new SubjectScores(
                        Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1]),
                        Double.parseDouble(parts[2])
                    );
                } catch (NumberFormatException e) {}
            }
            return new SubjectScores();
        }
    }

    public Student() {
        this("", "", 2000, "", "");
    }

    public Student(String id, String name, int year, String email, String className) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.email = email;
        this.className = className;
        this.subjectScores = new HashMap<>();
        // khởi tạo mặc định để Client không lỗi
        this.subjectScores.put("Lập Trình Mạng", new SubjectScores());
        this.subjectScores.put("Kỹ Năng Mềm", new SubjectScores());
    }

    public Student(String id, String name, int year, String email, String className, Map<String, SubjectScores> subjectScores) {
        this(id, name, year, email, className);
        if(subjectScores != null) {
            this.subjectScores.putAll(subjectScores);
        }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Map<String, SubjectScores> getSubjectScores() { return subjectScores; }
    public void setSubjectScores(Map<String, SubjectScores> subjectScores) { this.subjectScores = subjectScores; }

    public SubjectScores getScoresForModule(String moduleName) {
        return subjectScores.getOrDefault(moduleName, new SubjectScores());
    }

    public void updateScoresForModule(String moduleName, SubjectScores scores) {
        subjectScores.put(moduleName, scores);
    }

    @Override
    public String toString() {
        return id + " - " + name + " - " + year + " - " + email + " - Class: " + className + " - Scores: " + subjectScores;
    }
}
