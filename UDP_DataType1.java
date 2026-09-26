import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class UDPClient {

    private static final String SERVER_IP = "36.50.135.242";
    private static final int SERVER_PORT = 2207;
    private static final String STUDENT_CODE = "B15DCCNxxx"; // TODO: thay bằng mã sinh viên thật
    private static final String Q_CODE = "73457A17";         // TODO: thay bằng mã câu hỏi thật

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

            // b. Nhận requestId;n;A1,A2,...,Am
            byte[] recvBuf = new byte[4096];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(recvPacket);
            String response = new String(recvPacket.getData(), 0, recvPacket.getLength());
            System.out.println("Received: " + response);

            String[] parts = response.split(";", 3);
            String requestId = parts[0];
            int n = Integer.parseInt(parts[1].trim());

            Set<Integer> present = new HashSet<>();
            if (parts.length > 2 && !parts[2].isEmpty()) {
                String[] numStrs = parts[2].split(",");
                for (String s : numStrs) {
                    if (!s.trim().isEmpty()) {
                        present.add(Integer.parseInt(s.trim()));
                    }
                }
            }

            // c. Tìm các giá trị còn thiếu từ 1..n và gửi requestId;B1,B2,...,Bm
            StringJoiner missingJoiner = new StringJoiner(",");
            for (int i = 1; i <= n; i++) {
                if (!present.contains(i)) {
                    missingJoiner.add(String.valueOf(i));
                }
            }

            String result = requestId + ";" + missingJoiner.toString();
            byte[] resultBuf = result.getBytes();
            DatagramPacket resultPacket = new DatagramPacket(resultBuf, resultBuf.length, serverAddress, SERVER_PORT);
            socket.send(resultPacket);
            System.out.println("Sent result: " + result);

            // Optional: đọc phản hồi kết quả từ server (nếu có)
            try {
                byte[] ackBuf = new byte[4096];
                DatagramPacket ackPacket = new DatagramPacket(ackBuf, ackBuf.length);
                socket.receive(ackPacket);
                String ack = new String(ackPacket.getData(), 0, ackPacket.getLength());
                System.out.println("Server ack: " + ack);
            } catch (IOException ignored) {
                // Server có thể không gửi phản hồi thêm; bỏ qua timeout ở bước này
            }

            // d. Đóng socket (tự động nhờ try-with-resources) và kết thúc chương trình
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Mã câu hỏi

// 5xQqf47z
// Exam Server

// 36.50.135.242
// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng 2207. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với server theo kịch bản:

// a. Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng ;studentCode;qCode.

// Ví dụ: ;B15DCCN001;73457A17

// b. Nhận thông điệp là một chuỗi từ server theo định dạng requestId;n;A1,A2,...An , với

// requestId là chuỗi ngẫu nhiên duy nhất
// n là một số ngẫu nhiên nhỏ hơn 100.
// A1, A2 ... Am (m <= n) là các giá trị ngẫu nhiên nhỏ hơn hoặc bằng n và có thể trùng nhau.
// Ví dụ: requestId;10;2,3,5,6,5

// c. Tìm kiếm các giá trị còn thiếu và gửi lên server theo định dạng requestId;B1,B2,...,Bm

// Ví dụ: requestId;1,4,7,8,9,10

// d. Đóng socket và kết thúc chương trình.