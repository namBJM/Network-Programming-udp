import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientTitleCase {

    private static final String SERVER_IP = "36.50.135.242";
    private static final int SERVER_PORT = 2208;
    private static final String STUDENT_CODE = "B15DCCNxxx"; // TODO: thay bằng mã sinh viên thật
    private static final String Q_CODE = "qXve5uzq";

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
            String data = response.substring(semiIdx + 1);

            // c. Chuẩn hóa: chữ cái đầu mỗi từ viết hoa, các ký tự còn lại viết thường
            String normalized = toTitleCase(data);

            String result = requestId + ";" + normalized;
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

    /**
     * Chuyển chuỗi thành dạng: ký tự đầu mỗi từ viết hoa, các ký tự còn lại viết thường.
     * Một "từ" được xác định là dãy ký tự không phải khoảng trắng, ngăn cách nhau bởi khoảng trắng
     * (dấu cách, tab, xuống dòng, ...).
     */
    private static String toTitleCase(String input) {
        StringBuilder sb = new StringBuilder(input.toLowerCase());
        boolean capitalizeNext = true;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.setCharAt(i, Character.toUpperCase(c));
                capitalizeNext = false;
            }
        }
        return sb.toString();
    }
}

// Mã câu hỏi

// qXve5uzq
// Exam Server

// 36.50.135.242
// Một chương trình server cho phép kết nối qua giao thức UDP tại cổng 2208. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với server theo kịch bản dưới đây:

// a. Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng ;studentCode;qCode.

// Ví dụ: ;B15DCCN001;5B35BCC1

// b. Nhận thông điệp từ server theo định dạng requestId;data

// requestId là một chuỗi ngẫu nhiên duy nhất
// data là chuỗi dữ liệu cần xử lý
// c. Xử lý chuẩn hóa chuỗi đã nhận thành theo nguyên tắc

// Ký tự đầu tiên của từng từ trong chuỗi là in hoa
// Các ký tự còn lại của chuỗi là in thường Gửi thông điệp chứa chuỗi đã được chuẩn hóa lên server theo định dạng requestId;data
// d. Đóng socket và kết thúc chương trình