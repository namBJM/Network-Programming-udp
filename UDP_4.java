import java.net.*;
import java.util.*;

public class De3Count {
    static final String SERVER = "localhost"; // đổi thành IP/host server của đề
    static final int PORT = 2208;
    static final String STUDENT_CODE = "B15DCCN001";
    static final String Q_CODE = "9F8C2D3A";

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
            String response = new String(recv.getData(), 0, recv.getLength(), "UTF-8").trim();
            System.out.println("Nhận: " + response);

            int idx = response.indexOf(';');
            String requestId = response.substring(0, idx);
            String data = response.substring(idx + 1).trim();

            // c. Đếm ký tự
            String processed = countChars(data);

            String result = requestId + ";" + processed;
            byte[] out = result.getBytes("UTF-8");
            socket.send(new DatagramPacket(out, out.length, addr, PORT));
            System.out.println("Gửi: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // d. Đóng socket
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }

    // Tổng số lần xuất hiện của mỗi ký tự, theo thứ tự xuất hiện đầu tiên
    static String countChars(String s) {
        Map<Character, Integer> map = new LinkedHashMap<>();
        for (char c : s.toCharArray()) {
            map.merge(c, 1, Integer::sum);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, Integer> e : map.entrySet()) {
            sb.append(e.getValue()).append(e.getKey());
        }
        return sb.toString();
    }

    // Cách hiểu thay thế: đếm các ký tự liên tiếp (run-length), aaazbbbccdaa -> 3a1z3b2c1d2a
    static String runLength(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int j = i;
            while (j < s.length() && s.charAt(j) == s.charAt(i)) j++;
            sb.append(j - i).append(s.charAt(i));
            i = j;
        }
        return sb.toString();
    }
}

// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng
// 2208. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với
// server theo kịch bản:

// a.  Gửi một thông điệp chứa mã sinh viên và mã câu hỏi theo định dạng
//     ;studentCode;qCode.

// Ví dụ: ;B15DCCN001;9F8C2D3A

// b.  Nhận một thông điệp từ server theo định dạng requestId;data, với:

// -   requestId là chuỗi ngẫu nhiên duy nhất.
// -   data là một chuỗi ký tự liên tiếp cần xử lý.

// Ví dụ: requestId;aaabbccddaa

// c.  Xử lý chuỗi bằng cách duyệt lần lượt và đếm số lần xuất hiện của
//     từng ký tự. Gửi kết quả về server theo định dạng:

// requestId;processedData

// Ví dụ: Với chuỗi aaazbbbccdaa, kết quả sẽ là: requestId;5a2z3b2c1d

// d.  Đóng socket và kết thúc chương trình.