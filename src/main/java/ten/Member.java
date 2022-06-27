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

    public String get_PW(String id) {
        var database = mDatabase.getJedis();

        return database.hget(id + "_info", "pw");
    }

    public static void set_PW(String id, String pw) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var value = MessageDigest.getInstance("SHA-256");
        var builder = new StringBuilder();
        var map = new HashMap<String, String>();

        value.update(pw.getBytes());
        for (var piece : value.digest()) {
            builder.append(String.format("%02x", piece));
        }
        pw = builder.toString();
        map.put("pw", pw);
        database.hmset(id + "_info", map);

        return;
    }

    public String get_Phone(String id) {
        var database = mDatabase.getJedis();

        return database.hget(id + "_info", "phone");
    }

    public void set_Phone(String id, String phone) {
        var database = mDatabase.getJedis();
        var map = new HashMap<String, String>();

        map.put("phone", phone);
        database.hmset(id + "_info", map);

        return;
    }

    public String get_Answer(String id) {
        var database = mDatabase.getJedis();

        return database.hget(id + "_info", "ans");
    }

    public static void set_Answer(String id, String[] ans) throws NoSuchAlgorithmException {
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
        map.put("ans", builder.toString());
        database.hmset(id + "_info", map);

        return;
    }

    public static void set(String id, String pw, String phone, String[] ans) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var map = new HashMap<String, String>();

        map.put("id", id);
        map.put("phone", phone);
        database.hmset(id + "_info", map);
        set_Answer(id, ans);
        set_PW(id, pw);
        database.sadd("members", id);

        return;
    }
}
