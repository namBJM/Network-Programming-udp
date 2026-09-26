import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import UDP.Student;

public class UDPClientStudent {

    private static final String SERVER_IP = "36.50.135.242";
    private static final int SERVER_PORT = 2209;
    private static final String STUDENT_CODE = "B15DCCNxxx"; // TODO: thay bằng mã sinh viên thật
    private static final String Q_CODE = "zr5Iu2QE";
    private static final int REQUEST_ID_LEN = 8;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            // a. Gửi ;studentCode;qCode
            String request = ";" + STUDENT_CODE + ";" + Q_CODE;
            byte[] sendBuf = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);
            System.out.println("Sent: " + request);

            // b. Nhận: 8 byte đầu là requestId, phần còn lại là đối tượng Student đã serialize
            byte[] recvBuf = new byte[16384];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(recvPacket);
            int len = recvPacket.getLength();
            byte[] data = Arrays.copyOf(recvPacket.getData(), len);

            byte[] requestIdBytes = Arrays.copyOfRange(data, 0, REQUEST_ID_LEN);
            byte[] objectBytes = Arrays.copyOfRange(data, REQUEST_ID_LEN, len);

            Student student;
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(objectBytes))) {
                student = (Student) ois.readObject();
            }
            System.out.println("Received requestId=" + new String(requestIdBytes) + ", student=" + student);

            // c. Chuẩn hóa tên: chữ cái đầu tiên in hoa, các chữ cái còn lại in thường
            String rawName = student.getName();
            String normalizedName = normalizeName(rawName);
            String email = buildEmail(rawName);

            student.setName(normalizedName);
            student.setEmail(email);
            System.out.println("Processed student=" + student);

            // Serialize lại đối tượng Student đã sửa đổi
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(student);
            }
            byte[] newObjectBytes = baos.toByteArray();

            // Ghép 8 byte requestId (giữ nguyên) + object đã sửa đổi
            byte[] resultData = new byte[REQUEST_ID_LEN + newObjectBytes.length];
            System.arraycopy(requestIdBytes, 0, resultData, 0, REQUEST_ID_LEN);
            System.arraycopy(newObjectBytes, 0, resultData, REQUEST_ID_LEN, newObjectBytes.length);

            DatagramPacket resultPacket = new DatagramPacket(resultData, resultData.length, serverAddress, SERVER_PORT);
            socket.send(resultPacket);
            System.out.println("Sent processed student object to server.");

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

            // d. Đóng socket (tự động nhờ try-with-resources) và kết thúc chương trình
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chữ cái đầu tiên của toàn bộ chuỗi viết hoa, các ký tự còn lại viết thường.
     */
    private static String normalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String lower = name.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    /**
     * Tạo email dạng ptit.edu.vn: lấy tên (từ cuối cùng) ghép với chữ cái đầu
     * của họ và tên đệm (các từ phía trước), theo đúng thứ tự xuất hiện.
     * Ví dụ: "nguyen van tuan nam" -> "namnvt@ptit.edu.vn"
     */
    private static String buildEmail(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "@ptit.edu.vn";
        }
        String[] words = name.trim().toLowerCase().split("\\s+");
        String firstName = words[words.length - 1];
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            if (!words[i].isEmpty()) {
                initials.append(words[i].charAt(0));
            }
        }
        return firstName + initials + "@ptit.edu.vn";
    }
}

// Mã câu hỏi

// zr5Iu2QE
// Exam Server

// 36.50.135.242
// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng 2209. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với server theo kịch bản sau: Đối tượng trao đổi là thể hiện của lớp UDP.Student được mô tả:

// Tên đầy đủ lớp: UDP.Student
// Các thuộc tính: id String,code String, name String, email String
// 02 Hàm khởi tạo:
// public Student(String id, String code, String name, String email)
// public Student(String code)
// Trường dữ liệu: private static final long serialVersionUID = 20171107
// Thực hiện:

// a. Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng ;studentCode;qCode.

// Ví dụ: ;B15DCCN001;EE29C059

// b. Nhận thông điệp chứa: 08 byte đầu chứa chuỗi requestId, các byte còn lại chứa một đối tượng là thể hiện của lớp Student từ server. Trong đó, các thông tin được thiết lập gồm id và name.

// c. Yêu cầu:

// Chuẩn hóa tên theo quy tắc: Chữ cái đầu tiên in hoa, các chữ cái còn lại in thường và gán lại thuộc tính name của đối tượng
// Tạo email ptit.edu.vn từ tên người dùng bằng cách lấy tên và các chữ cái bắt đầu của họ và tên đệm.
// Ví dụ: nguyen van tuan nam -> namnvt@ptit.edu.vn. Gán giá trị này cho thuộc tính email của đối tượng nhận được

// Gửi thông điệp chứa đối tượng xử lý ở bước c lên Server với cấu trúc: 08 byte đầu chứa chuỗi requestId và các byte còn lại chứa đối tượng Student đã được sửa đổi.
// d. Đóng socket và kết thúc chương trình.