import UDP.Student;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Bai4Student {
    static final String SERVER = "localhost"; // đổi thành IP/host server của đề
    static final int PORT = 2209;
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

            // b. Nhận: 8 byte requestId + đối tượng Student
            byte[] buf = new byte[65507];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            socket.receive(recv);

            byte[] data = recv.getData();
            int len = recv.getLength();
            byte[] requestIdBytes = Arrays.copyOfRange(data, 0, 8);

            Student st;
            try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(data, 8, len - 8))) {
                st = (Student) ois.readObject();
            }
            System.out.println("Nhận: " + st);

            // c. Chuẩn hóa tên và tạo email
            String name = normalizeName(st.getName());
            st.setName(name);
            st.setEmail(buildEmail(name));
            System.out.println("Gửi: " + st);

            // Gửi 8 byte requestId + đối tượng đã sửa
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(requestIdBytes);
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(st);
            }
            byte[] out = bos.toByteArray();
            socket.send(new DatagramPacket(out, out.length, addr, PORT));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // d. Đóng socket
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }

    // Mỗi từ: chữ đầu in hoa, còn lại in thường
    static String normalizeName(String s) {
        String[] words = s.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    // "Nguyen Van Tuan Nam" -> namnvt@ptit.edu.vn
    static String buildEmail(String name) {
        String[] w = name.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder(w[w.length - 1]); // tên
        for (int i = 0; i < w.length - 1; i++) {
            sb.append(w[i].charAt(0));                          // chữ cái đầu của họ, tên đệm
        }
        return sb + "@ptit.edu.vn";
    }
}

// ĐỀ 4 — UDP STUDENT

// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng
// 2209. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với
// server theo kịch bản sau: Đối tượng trao đổi là thể hiện của lớp
// UDP.Student được mô tả:

// -   Tên đầy đủ lớp: UDP.Student
// -   Các thuộc tính: id String, code String, name String, email String
// -   02 Hàm khởi tạo:
//     -   public Student(String id, String code, String name, String
//         email)
//     -   public Student(String code)
// -   Trường dữ liệu: private static final long serialVersionUID =
//     20171107

// Thực hiện:

// a.  Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo
//     định dạng ;studentCode;qCode.

// Ví dụ: ;B15DCCN001;EE29C059

// b.  Nhận thông điệp chứa: 08 byte đầu chứa chuỗi requestId, các byte còn
//     lại chứa một đối tượng là thể hiện của lớp Student từ server. Trong
//     đó, các thông tin được thiết lập gồm id và name.

// c.  Yêu cầu:

// -   Chuẩn hóa tên theo quy tắc: Chữ cái đầu tiên in hoa, các chữ cái còn
//     lại in thường và gán lại thuộc tính name của đối tượng.

// -   Tạo email ptit.edu.vn từ tên người dùng bằng cách lấy tên và các chữ
//     cái bắt đầu của họ và tên đệm.

// Ví dụ: nguyen van tuan nam -> namnvt@ptit.edu.vn. Gán giá trị này cho
// thuộc tính email của đối tượng nhận được.

// -   Gửi thông điệp chứa đối tượng xử lý ở bước c lên Server với cấu
//     trúc: 08 byte đầu chứa chuỗi requestId và các byte còn lại chứa đối
//     tượng Student đã được sửa đổi.

// d.  Đóng socket và kết thúc chương trình.
