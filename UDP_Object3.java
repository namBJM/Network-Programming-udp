import UDP.Book;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Bai1Book {
    static final String SERVER = "localhost"; // đổi thành IP/host server của đề
    static final int PORT = 2209;
    static final String STUDENT_CODE = "B23DCCN005";
    static final String Q_CODE = "eQKvAeId";

    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
            InetAddress addr = InetAddress.getByName(SERVER);

            // a. Gửi ;studentCode;qCode
            byte[] req = (";" + STUDENT_CODE + ";" + Q_CODE).getBytes("UTF-8");
            socket.send(new DatagramPacket(req, req.length, addr, PORT));

            // b. Nhận: 8 byte requestId + đối tượng Book
            byte[] buf = new byte[65507];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            socket.receive(recv);

            byte[] data = recv.getData();
            int len = recv.getLength();
            byte[] requestIdBytes = Arrays.copyOfRange(data, 0, 8);

            Book book;
            try (ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(data, 8, len - 8))) {
                book = (Book) ois.readObject();
            }
            System.out.println("Nhận: " + book);

            // c. Chuẩn hóa
            book.setTitle(normalizeTitle(book.getTitle()));
            book.setAuthor(normalizeAuthor(book.getAuthor()));
            book.setIsbn(normalizeIsbn(book.getIsbn()));
            book.setPublishDate(convertDate(book.getPublishDate()));
            System.out.println("Gửi: " + book);

            // d. Gửi 8 byte requestId + đối tượng đã sửa
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(requestIdBytes);
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(book);
            }
            byte[] out = bos.toByteArray();
            socket.send(new DatagramPacket(out, out.length, addr, PORT));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }

    // Viết hoa chữ cái đầu mỗi từ
    static String normalizeTitle(String s) {
        String[] words = s.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    // "HỌ, Tên": từ đầu tiên là họ (in hoa), các từ còn lại là tên (viết hoa chữ đầu mỗi từ)
    static String normalizeAuthor(String s) {
        String[] words = s.trim().toLowerCase().split("\\s+");
        if (words.length == 1) return words[0].toUpperCase();
        StringBuilder ten = new StringBuilder();
        for (int i = 1; i < words.length; i++) {
            if (i > 1) ten.append(" ");
            ten.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
        }
        return words[0].toUpperCase() + ", " + ten;
    }

    // 13 chữ số -> 978-3-16-148410-0 (nhóm 3-1-2-6-1)
    static String normalizeIsbn(String s) {
        String d = s.replaceAll("[^0-9Xx]", "");
        if (d.length() != 13) return s; // không đủ 13 ký tự thì giữ nguyên
        return d.substring(0, 3) + "-" + d.substring(3, 4) + "-" + d.substring(4, 6)
                + "-" + d.substring(6, 12) + "-" + d.substring(12);
    }

    // yyyy-mm-dd -> mm/yyyy
    static String convertDate(String s) {
        String[] p = s.trim().split("-");
        return p[1] + "/" + p[0];
    }
}

// ĐỀ 1 — UDP BOOK

// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng
// 2209. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với
// server theo kịch bản sau:

// Đối tượng trao đổi là thể hiện của lớp UDP.Book được mô tả:

// Tên đầy đủ lớp: UDP.Book

// Các thuộc tính: id (String), title (String), author (String), isbn
// (String), publishDate (String)

// Hàm khởi tạo:

// public Book(String id, String title, String author, String isbn, String
// publishDate)

// Trường dữ liệu: private static final long serialVersionUID = 20251107L

// Thực hiện:

// a.  Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo
//     định dạng ;studentCode;qCode.

// Ví dụ: ;B23DCCN005;eQKvAeId

// b.  Nhận thông điệp chứa: 08 byte đầu chứa chuỗi requestId, các byte còn
//     lại chứa một đối tượng là thể hiện của lớp Book từ server. Trong đó,
//     các thuộc tính id, title, author, isbn, và publishDate đã được thiết
//     lập sẵn.

// c.  Thực hiện:

// Chuẩn hóa title: viết hoa chữ cái đầu của mỗi từ.

// Chuẩn hóa author theo định dạng “HỌ, Tên”.

// Chuẩn hóa mã ISBN theo định dạng “978-3-16-148410-0”

// Chuyển đổi publishDate từ yyyy-mm-dd sang mm/yyyy.

// d.  Gửi lại đối tượng đã được chuẩn hóa về server với cấu trúc: 08 byte
//     đầu chứa chuỗi request và các byte còn lại chứa đối tượng Book đã
//     được sửa đổi.

// Đóng socket và kết thúc chương trình.

// ============================================================
