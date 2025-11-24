package RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class ServerMain {
    public static void main(String[] args) {
        try {
            int port = 1099;

            // Tạo RMI registry
            try {
                LocateRegistry.createRegistry(port);
                System.out.println("RMI registry đã tạo tại cổng " + port);
            } catch (Exception e) {
                System.out.println("RMI registry có thể đã chạy sẵn tại cổng " + port);
            }

            // Tạo và export đối tượng StudentManagerImpl
            StudentManagerImpl impl = new StudentManagerImpl(); // phải extends UnicastRemoteObject

            // Đăng ký đối tượng với registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("StudentManager", impl);

            System.out.println("Đối tượng StudentManager đã được đăng ký. Server sẵn sàng hoạt động.");
        } catch (RemoteException e) {
            System.err.println("Lỗi RemoteException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi máy chủ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
