package ten;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class Member {
    static Database mDatabase = Database.getInstance();
    private String id;
    private String pw;
    private String phone;
    private String[] answer;

    /*
     * 인자로 받은 아이디에 해당하는 비밀번호를 반환
     */
    public String get_PW(String id) {
        var database = mDatabase.getJedis();

        return database.hget(id + "_info", "key_PW");
    }

    /*
     * 인자로 받은 비밀번호를 SHA-256 해시값으로 변환해 해당 아이디에 저장
     */
    public void set_PW(String id, String pw) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var value = MessageDigest.getInstance("SHA-256");
        var builder = new StringBuilder();
        var map = new HashMap<String, String>();

        value.update(pw.getBytes());
        for (var piece : value.digest()) {
            builder.append(String.format("%02x", piece));
        }
        pw = builder.toString();
        map.put("key_PW", pw);
        database.hmset(id + "_info", map);

        return;
    }

    /*
     * 인자로 받은 아이디에 해당하는 전화번호를 반환
     */
    public String get_Phone(String id) {
        var database = mDatabase.getJedis();

        return database.hget(id + "_info", "key_Phone");
    }

    /*
     * 인자로 받은 전화번호를 해당 아이디에 저장
     */
    public void set_Phone(String id, String phone) {
        var database = mDatabase.getJedis();
        var map = new HashMap<String, String>();

        map.put("key_Phone", phone);
        database.hmset(id + "_info", map);

        return;
    }

    /*
     * 인자로 받은 아이디에 해당하는 본인확인질문을 반환
     */
    public String get_Answer(String id) {
        var database = mDatabase.getJedis();

        return database.hget(id + "_info", "key_Ans");
    }

    /*
     * 인자로 받은 본인확인질문을 '|'로 구분해 SHA-256 해시값으로 변환 후 해당 아이디에 저장
     */
    public void set_Answer(String id, String[] ans) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var builder = new StringBuilder();
        var value = MessageDigest.getInstance("SHA-256");
        var map = new HashMap<String, String>();

        for (var piece : ans) {
            builder.append(piece + "|");
        }
        value.update(builder.toString().getBytes());
        builder.setLength(0);
        for (var piece : value.digest()) {
            builder.append(String.format("%02x", piece));
        }
        map.put("key_Ans", builder.toString());
        database.hmset(id + "_info", map);

        return;
    }

    /*
     * 인자로 받은 아이디, 비밀번호, 전화번호, 본인확인질문을 저장
     */
    public void set(String id, String pw, String phone, String[] ans) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var map = new HashMap<String, String>();

        map.put("key_ID", id);
        map.put("key_Phone", phone);
        database.hmset(id + "_info", map);
        set_Answer(id, ans);
        set_PW(id, pw);
        database.sadd("key_Members", id);

        return;
    }
}
