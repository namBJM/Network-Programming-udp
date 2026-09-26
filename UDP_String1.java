import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class UDPClient2208 {

    private static final String SERVER_IP = "36.50.135.242";
    private static final int SERVER_PORT = 2208;
    private static final String STUDENT_CODE = "B15DCCNxxx"; // TODO: thay bằng mã sinh viên thật
    private static final String Q_CODE = "bHrfoyWA";

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

            // b. Nhận requestId;data
            byte[] recvBuf = new byte[8192];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(recvPacket);
            String response = new String(recvPacket.getData(), 0, recvPacket.getLength());
            System.out.println("Received: " + response);

            int semiIdx = response.indexOf(';');
            String requestId = response.substring(0, semiIdx);
            String data = response.substring(semiIdx + 1).trim();

            // c. Tìm ký tự xuất hiện nhiều nhất và các vị trí (1-based) của nó
            // Dùng LinkedHashMap để giữ thứ tự xuất hiện lần đầu -> khi hòa tần suất,
            // ký tự xuất hiện trước trong chuỗi sẽ được chọn.
            Map<Character, List<Integer>> positionsMap = new LinkedHashMap<>();
            for (int i = 0; i < data.length(); i++) {
                char c = data.charAt(i);
                positionsMap.computeIfAbsent(c, k -> new ArrayList<>()).add(i + 1); // vị trí 1-based
            }

            char bestChar = '\0';
            int bestCount = -1;
            List<Integer> bestPositions = null;
            for (Map.Entry<Character, List<Integer>> entry : positionsMap.entrySet()) {
                int count = entry.getValue().size();
                if (count > bestCount) {
                    bestCount = count;
                    bestChar = entry.getKey();
                    bestPositions = entry.getValue();
                }
            }

            StringBuilder posBuilder = new StringBuilder();
            if (bestPositions != null) {
                for (int pos : bestPositions) {
                    posBuilder.append(pos).append(",");
                }
            }

            String result = requestId + ";" + bestChar + ":" + posBuilder;
            byte[] resultBuf = result.getBytes();
            DatagramPacket resultPacket = new DatagramPacket(resultBuf, resultBuf.length, serverAddress, SERVER_PORT);
            socket.send(resultPacket);
            System.out.println("Sent result: " + result);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Mã câu hỏi

// bHrfoyWA
// Exam Server

// 36.50.135.242
// Một chương trình server cho phép kết nối qua giao thức UDP tại cổng 2208. Yêu cầu là xây dựng một chương trình client tương tác với server kịch bản dưới đây:

// a. Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng ;studentCode;qCode.

// Ví dụ: ;B15DCCN001;EE29C059

// b. Nhận thông điệp từ server theo định dạng requestId; data

// requestId là một chuỗi ngẫu nhiên duy nhất
// data là chuỗi dữ liệu đầu vào cần xử lý
// Ví dụ: requestId;Qnc8d5x78aldSGWWmaAAjyg3

// c. Tìm kiếm ký tự xuất hiện nhiều nhất trong chuỗi và gửi lên server theo định dạng “requestId;ký tự xuất hiện nhiều nhất: các vị trí xuất hiện ký tự đó”

// Ví dụ: requestId;8:4,9,

// d. Đóng socket và kết thúc chương trình