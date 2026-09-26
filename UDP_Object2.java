import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import UDP.Customer;

public class UDPClientCustomer {

    private static final String SERVER_IP = "36.50.135.242";
    private static final int SERVER_PORT = 2209;
    private static final String STUDENT_CODE = "B15DCCNxxx"; // TODO: thay bằng mã sinh viên thật
    private static final String Q_CODE = "0cuTGSBN";
    private static final int REQUEST_ID_LEN = 8;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            // Gửi ;studentCode;qCode
            String request = ";" + STUDENT_CODE + ";" + Q_CODE;
            byte[] sendBuf = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);
            System.out.println("Sent: " + request);

            // Nhận: 8 byte đầu là requestId, phần còn lại là đối tượng Customer đã serialize
            byte[] recvBuf = new byte[16384];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(recvPacket);
            int len = recvPacket.getLength();
            byte[] data = Arrays.copyOf(recvPacket.getData(), len);

            byte[] requestIdBytes = Arrays.copyOfRange(data, 0, REQUEST_ID_LEN);
            byte[] objectBytes = Arrays.copyOfRange(data, REQUEST_ID_LEN, len);

            Customer customer;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(objectBytes))) {
                customer = (Customer) ois.readObject();
            }
            System.out.println("Received requestId=" + new String(requestIdBytes) + ", customer=" + customer);

            // Xử lý theo yêu cầu, dựa trên tên và ngày sinh gốc
            String rawName = customer.getName();
            String rawDob = customer.getDayOfBirth();

            String formattedName = formatName(rawName);
            String userName = buildUserName(rawName);
            String formattedDob = formatDayOfBirth(rawDob);

            customer.setName(formattedName);
            customer.setUserName(userName);
            customer.setDayOfBirth(formattedDob);
            System.out.println("Processed customer=" + customer);

            // Serialize lại đối tượng Customer đã sửa đổi
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(customer);
            }
            byte[] newObjectBytes = baos.toByteArray();

            // Ghép 8 byte requestId (giữ nguyên) + object đã sửa đổi
            byte[] resultData = new byte[REQUEST_ID_LEN + newObjectBytes.length];
            System.arraycopy(requestIdBytes, 0, resultData, 0, REQUEST_ID_LEN);
            System.arraycopy(newObjectBytes, 0, resultData, REQUEST_ID_LEN, newObjectBytes.length);

            DatagramPacket resultPacket = new DatagramPacket(resultData, resultData.length, serverAddress, SERVER_PORT);
            socket.send(resultPacket);
            System.out.println("Sent processed customer object to server.");

            // Optional: đọc phản hồi/ack từ server nếu có
            try {
                byte[] ackBuf = new byte[4096];
                DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
                socket.receive(ackPacket);
                String ack = new String(ackPacket.getData(), 0, ackPacket.getLength());
                System.out.println("Server ack: " + ack);
            } catch (IOException ignored) {
                // Server có thể không gửi thêm phản hồi; bỏ qua timeout ở bước này
            }

            // Đóng socket (tự động nhờ try-with-resources) và kết thúc chương trình
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * "nguyen van hai duong" -> "DUONG, Nguyen Van Hai"
     * Từ cuối cùng viết hoa toàn bộ, đặt trước dấu phẩy; các từ còn lại giữ
     * nguyên thứ tự, mỗi từ viết hoa chữ cái đầu, phần còn lại viết thường.
     */
    private static String formatName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return rawName;
        }
        String[] words = rawName.trim().toLowerCase().split("\\s+");
        String lastWord = words[words.length - 1];

        StringBuilder rest = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            if (i > 0) {
                rest.append(" ");
            }
            String w = words[i];
            rest.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }

        return lastWord.toUpperCase() + ", " + rest;
    }

    /**
     * "nguyen van hai duong" -> "nvhduong"
     * Chữ cái đầu của các từ đứng trước, ghép với toàn bộ từ cuối cùng, tất cả viết thường.
     */
    private static String buildUserName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return "";
        }
        String[] words = rawName.trim().toLowerCase().split("\\s+");
        String lastWord = words[words.length - 1];

        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            if (!words[i].isEmpty()) {
                initials.append(words[i].charAt(0));
            }
        }
        return initials + lastWord;
    }

    /**
     * mm-dd-yyyy -> dd/mm/yyyy
     * "10-11-2012" -> "11/10/2012"
     */
    private static String formatDayOfBirth(String rawDob) {
        if (rawDob == null || rawDob.trim().isEmpty()) {
            return rawDob;
        }
        String[] parts = rawDob.trim().split("-");
        String mm = parts[0];
        String dd = parts[1];
        String yyyy = parts[2];
        return dd + "/" + mm + "/" + yyyy;
    }
}

// Mã câu hỏi

// 0cuTGSBN
// Exam Server

// 36.50.135.242
// Thông tin khách hàng được yêu cầu thay đổi định dạng lại cho phù hợp với khu vực, cụ thể:

// a. Tên khách hàng cần được chuẩn hóa theo định dạng mới.

// Ví dụ: nguyen van hai duong -> DUONG, Nguyen Van Hai

// b. Ngày sinh của khách hàng đang ở dạng mm-dd-yyyy, cần được chuyển thành định dạng dd/mm/yyyy.

// Ví dụ: 10-11-2012 -> 11/10/2012

// c. Tài khoản khách hàng được tạo từ các chữ cái in thường được sinh tự động từ họ tên khách hàng.

// Ví dụ: nguyen van hai duong -> nvhduong

// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng 2209. Yêu cầu là xây dựng một chương trình client giao tiếp với server theo mô tả sau:

// a. Đối tượng trao đổi là thể hiện của lớp UDP.Customer được mô tả như sau

// Tên đầy đủ của lớp: UDP.Customer
// Các thuộc tính: id String, code String, name String, , dayOfBirth String, userName String
// Một Hàm khởi tạo với đầy đủ các thuộc tính được liệt kê ở trên
// Trường dữ liệu: private static final long serialVersionUID = 20151107;
// b. Client giao tiếp với server theo các bước

// Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng ;studentCode;qCode.
// Ví dụ: ;B15DCCN001;EE29C059

// Nhận thông điệp chứa: 08 byte đầu chứa chuỗi requestId, các byte còn lại chứa một đối tượng là thể hiện của lớp Customer từ server. Trong đó, các thuộc tính id, code, name,dayOfBirth đã được thiết lập sẵn.
// Yêu cầu thay đổi thông tin các thuộc tính như yêu cầu ở trên và gửi lại đối tượng khách hàng đã được sửa đổi lên server với cấu trúc: 08 byte đầu chứa chuỗi requestId và các byte còn lại chứa đối tượng Customer đã được sửa đổi.
// Đóng socket và kết thúc chương trình.