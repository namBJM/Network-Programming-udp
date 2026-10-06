import java.net.*;
import java.util.*;

public class Bai1UDP {
    static final String SERVER = "localhost"; // đổi thành IP/host server của đề
    static final int PORT = 2207;
    static final String STUDENT_CODE = "B21DCCN795";
    static final String Q_CODE = "jyLrhZ6UM";

    public static void main(String[] args) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
            InetAddress addr = InetAddress.getByName(SERVER);

            // a. Gửi ;studentCode;qCode
            String request = ";" + STUDENT_CODE + ";" + Q_CODE;
            byte[] sendData = request.getBytes("UTF-8");
            socket.send(new DatagramPacket(sendData, sendData.length, addr, PORT));

            // b. Nhận requestId;n;k;z1,z2,...,zn
            byte[] buf = new byte[65507];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            socket.receive(recv);
            String response = new String(recv.getData(), 0, recv.getLength(), "UTF-8").trim();
            System.out.println("Nhận: " + response);

            String[] parts = response.split(";");
            String requestId = parts[0];
            int n = Integer.parseInt(parts[1].trim());
            int k = Integer.parseInt(parts[2].trim());
            String[] nums = parts[3].split(",");
            int[] arr = new int[nums.length];
            for (int i = 0; i < nums.length; i++) {
                arr[i] = Integer.parseInt(nums[i].trim());
            }

            // c. Tìm max mỗi cửa sổ kích thước k
            List<Integer> result = slidingWindowMax(arr, k);

            StringBuilder sb = new StringBuilder(requestId).append(";");
            for (int i = 0; i < result.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(result.get(i));
            }

            // Gửi kết quả
            byte[] out = sb.toString().getBytes("UTF-8");
            socket.send(new DatagramPacket(out, out.length, addr, PORT));
            System.out.println("Gửi: " + sb);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // d. Đóng socket
            if (socket != null && !socket.isClosed()) socket.close();
        }
    }

    static List<Integer> slidingWindowMax(int[] a, int k) {
        List<Integer> res = new ArrayList<>();
        Deque<Integer> dq = new ArrayDeque<>(); // lưu chỉ số, giá trị giảm dần
        for (int i = 0; i < a.length; i++) {
            // bỏ phần tử đã ra khỏi cửa sổ
            while (!dq.isEmpty() && dq.peekFirst() <= i - k) dq.pollFirst();
            // bỏ các phần tử nhỏ hơn a[i]
            while (!dq.isEmpty() && a[dq.peekLast()] <= a[i]) dq.pollLast();
            dq.offerLast(i);
            if (i >= k - 1) res.add(a[dq.peekFirst()]);
        }
        return res;
    }
}

// Một chương trình server cho phép giao tiếp qua giao thức UDP tại cổng 2207. Yêu cầu là xây dựng một chương trình client trao đổi thông tin với server theo kịch bản:

// a. Gửi thông điệp là một chuỗi chứa mã sinh viên và mã câu hỏi theo định dạng ;studentCode;qCode.

// Ví dụ: ;B21DCCN795;jyLrhZ6UM

// b. Nhận thông điệp là một chuỗi từ server theo định dạng requestId;n;k;z1,z2,...,zn trong đó:

// - requestId là chuỗi ngẫu nhiên duy nhất.
// - n là số phần tử của mảng.
// - k là kích thước cửa sổ (k < n).
// - z1 đến zn là n phần tử là số nguyên của mảng.

// c. Thực hiện tìm giá trị lớn nhất trong mỗi cửa sổ với kích thước k trên mảng số nguyên nhận được, và gửi thông điệp lên server theo định dạng requestId;max1,max2,...,maxm trong đó max1 đến maxm là các giá trị lớn nhất tương ứng trong mỗi cửa sổ.

// Ví dụ: requestId;5;3;1,5,2,3,4

// Kết quả: requestId;5,5,4

// d. Đóng socket và kết thúc chương trình.