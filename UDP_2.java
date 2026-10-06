import java.net.*;

public class Bai2UDP {
    static final String SERVER = "localhost"; // đổi thành IP/host server của đề
    static final int PORT = 2208;
    static final String STUDENT_CODE = "B15DCCN001";
    static final String Q_CODE = "EE29C059";

    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
            InetAddress addr = InetAddress.getByName(SERVER);

            // a. Gửi datagram đầu tiên ;studentCode;qCode
            String request = ";" + STUDENT_CODE + ";" + Q_CODE;
            byte[] sendData = request.getBytes("UTF-8");
            socket.send(new DatagramPacket(sendData, sendData.length, addr, PORT));

            // b. Nhận requestId;data
            byte[] buf = new byte[65507];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            socket.receive(recv);
            String response = new String(recv.getData(), 0, recv.getLength(), "UTF-8").trim();
            System.out.println("Nhận: " + response);

            int idx = response.indexOf(';');
            String requestId = response.substring(0, idx);
            String data = response.substring(idx + 1);

            // c. Chuẩn hóa chuỗi
            String answer = normalize(data);

            // d. Gửi requestId;answer
            String result = requestId + ";" + answer;
            byte[] out = result.getBytes("UTF-8");
            socket.send(new DatagramPacket(out, out.length, addr, PORT));
            System.out.println("Gửi: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // e. Đóng socket
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }

    static String normalize(String s) {
        s = s.toLowerCase();
        s = s.replaceAll("\\p{Punct}", "");      // loại bỏ dấu câu
        s = s.trim().replaceAll("\\s+", " ");    // gom khoảng trắng
        return s.replace(' ', '-');              // thay khoảng trắng bằng '-'
    }
}

// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng 2208 theo cơ chế hai pha.

// Yêu cầu

// a. Gửi datagram đầu tiên chứa chuỗi theo định dạng ;studentCode;qCode. Ví dụ: ;B15DCCN001;EE29C059.

// b. Nhận phản hồi từ server theo định dạng requestId;data. Ví dụ: 7BcrZ6QQA;REFUND ticket Payment Payment customer- Payment customer ticket REFUND customer shipping shipping Payment...

// c. Chuyển chuỗi về chữ thường, loại bỏ dấu câu, gom nhiều khoảng trắng thành một khoảng trắng và thay khoảng trắng bằng dấu gạch ngang.

// d. Gửi datagram nộp kết quả theo định dạng requestId;answer. Ví dụ: 7BcrZ6QQA;refund-ticket-payment-payment-customer-payment-customer-ticket-refund-customer-shipping-shipping-payment

// e. Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.