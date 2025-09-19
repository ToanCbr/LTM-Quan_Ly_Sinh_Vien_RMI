package student;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
	public static void main(String[] args) {
		try {
			// Cổng RMI registry
			int port = 1099;

			// Tạo RMI registry (nếu đã chạy sẵn thì sẽ bỏ qua lỗi)
			try {
				LocateRegistry.createRegistry(port);
				System.out.println("RMI Registry đã tạo tại cổng " + port);
			} catch (Exception e) {
				System.out.println("RMI Registry có thể đã chạy trước đó: " + e.getMessage());
			}

			// Tạo instance của StudentManagerImpl
			StudentManagerImpl studentManager = new StudentManagerImpl();

			// Đăng ký RMI service
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind("StudentManager", studentManager);

			System.out.println("Server RMI đã sẵn sàng. Service: StudentManager");
		} catch (Exception e) {
			System.err.println("Lỗi khi khởi chạy server: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
