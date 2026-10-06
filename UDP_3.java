import java.net.*;

public class De2Mask {
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

            // a. Gửi ;studentCode;qCode
            byte[] req = (";" + STUDENT_CODE + ";" + Q_CODE).getBytes("UTF-8");
            socket.send(new DatagramPacket(req, req.length, addr, PORT));

            // b. Nhận requestId;data
            byte[] buf = new byte[65507];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            socket.receive(recv);
            String response = new String(recv.getData(), 0, recv.getLength(), "UTF-8");
            System.out.println("Nhận: " + response);

            // bỏ ký tự '|' ở đầu nếu server có gửi kèm
            if (response.startsWith("|")) response = response.substring(1);

            int idx = response.indexOf(';');
            String requestId = response.substring(0, idx).trim();
            String data = response.substring(idx + 1);

            // c. Che thông tin nhạy cảm
            String answer = mask(data);

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

    static String mask(String s) {
        // token=xxxx -> token=[TOKEN] (làm trước để không lẫn với phone)
        s = s.replaceAll("(?i)token=[A-Za-z0-9_\\-]+", "token=[TOKEN]");
        // email
        s = s.replaceAll("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)+", "[EMAIL]");
        // số điện thoại: 9-15 chữ số, có thể có dấu + ở đầu
        s = s.replaceAll("(?<![\\w\\[])\\+?\\d{9,15}(?![\\w\\]])", "[PHONE]");
        return s;
    }
}

// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng
// 2208 theo cơ chế hai pha.

// Yêu cầu:

// a.  Gửi datagram đầu tiên chứa chuỗi theo định dạng ;studentCode;qCode.
//     Ví dụ: ;B15DCCN001;EE29C059.

// b.  Nhận phản hồi từ server theo định dạng |requestId;data. Ví dụ:
//     rBhDz2QJ;INFO user=minh email=minh@example.com phone=0309453907
//     token=bDZj8Olx9 action=refund.

// c.  Che email, số điện thoại và token theo quy tắc [EMAIL], [PHONE],
//     token=[TOKEN], giữ nguyên thứ tự các dòng log.

// d.  Gửi datagram nộp kết quả theo định dạng requestId;answer. Ví dụ:
//     rBhDz2QJ;INFO user=minh email=[EMAIL] phone=[PHONE] token=[TOKEN]
//     action=refund.

// e.  Đóng kết nối hoặc kết thúc client sau khi nộp kết quả.

// ============================================================