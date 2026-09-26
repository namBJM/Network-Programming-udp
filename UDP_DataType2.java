import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientMaxMin {

    private static final String SERVER_IP = "36.50.135.242";
    private static final int SERVER_PORT = 2207;
    private static final String STUDENT_CODE = "B15DCCNxxx"; // TODO: thay bằng mã sinh viên thật
    private static final String Q_CODE = "gaiPrygl";

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

            // b. Nhận requestId;a1,a2,...,a50
            byte[] recvBuf = new byte[4096];
            DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(recvPacket);
            String response = new String(recvPacket.getData(), 0, recvPacket.getLength());
            System.out.println("Received: " + response);

            int semiIdx = response.indexOf(';');
            String requestId = response.substring(0, semiIdx);
            String numsPart = response.substring(semiIdx + 1).trim();

            String[] numStrs = numsPart.split(",");
            long max = Long.MIN_VALUE;
            long min = Long.MAX_VALUE;
            for (String s : numStrs) {
                s = s.trim();
                if (s.isEmpty()) continue;
                long value = Long.parseLong(s);
                if (value > max) max = value;
                if (value < min) min = value;
            }

            // c. Gửi requestId;max,min
            String result = requestId + ";" + max + "," + min;
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

// gaiPrygl
// Exam Server

// 36.50.135.242
// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng 2207. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với server theo kịch bản:

// a. Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng ;studentCode;qCode.

// Ví dụ: ;B15DCCN001;DC73CA2E

// b. Nhận thông điệp là một chuỗi từ server theo định dạng requestId;a1,a2,...,a50

// requestId là chuỗi ngẫu nhiên duy nhất
// a1 -> a50 là 50 số nguyên ngẫu nhiên
// c. Thực hiện tìm giá trị lớn nhất và giá trị nhỏ nhất thông điệp trong a1 -> a50 và gửi thông điệp lên lên server theo định dạng requestId;max,min

// d. Đóng socket và kết thúc chương trình