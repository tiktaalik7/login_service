package ten;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class System {
    Database mDatabase = Database.getInstance();

    public boolean login(String id, String pw) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var value = MessageDigest.getInstance("SHA-256");
        var builder = new StringBuilder();

        if (check_IDPW(id, pw)) {
            if (database.sismember("members", id)) {
                value.update(pw.getBytes());
                for (var piece : value.digest()) {
                    builder.append(String.format("%02x", piece));
                }
                if (database.hget(id + "_info", "pw").equals(builder.toString())) {
                    return true;
                }
            }
        }

        return false;
    }

    public void logout() {

    }

    public boolean register(String id, String pw, String phone, String[] ans) {
        var database = mDatabase.getJedis();

        if (check_IDPW(id, pw) && !database.sismember("members", id)) {
            if ((check_Phone(phone) == 1) && check_Ans(ans)) {
                try {
                    Member.set(id, pw, phone, ans);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }

        return false;
    }

    public String find_ID(String phone) {
        var database = mDatabase.getJedis();
        String result = null;

        if (check_Phone(phone) == 0) {
            Set<String> temp = database.smembers("members");
            for (var member : temp) {
                if (database.hget(member + "_info", "phone").equals(phone)) {
                    result = member;
                }
            }
        }

        return result;
    }

    public String find_PW(String id, String phone, String[] ans) {
        var database = mDatabase.getJedis();
        int temp_PW = 0;
        var random = new Random();
        MessageDigest value = null;
        try {
            value = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        var builder = new StringBuilder();
        boolean flag = false;
        var map = new HashMap<String, String>();
        String result;

        if (check_IDPW(id, "password4test") && check_Phone(phone) == 0 && check_Ans(ans)) {
            var members = database.smembers("members");
            for (var member : members) {
                if (member.equals(id)) {
                    if (database.hget(id + "_info", "phone").equals(phone)) {
                        for (var piece : ans) {
                            builder.append(piece + "|");
                        }
                        value.update(builder.toString().getBytes());
                        builder.setLength(0);
                        for (var piece : value.digest()) {
                            builder.append(String.format("%02x", piece));
                        }
                        if (builder.toString().equals(database.hget(id + "_info", "ans"))) {
                            flag = true;
                        }
                    }
                }
            }
        }
        if (flag) {
            builder.setLength(0);
            temp_PW = random.nextInt(100000);
            builder.append(String.format("%05d", temp_PW));
            temp_PW = random.nextInt(100000);
            builder.append(String.format("%05d", temp_PW));
            result = builder.toString();
            value.update(builder.toString().getBytes());
            builder.setLength(0);
            for (var piece : value.digest()) {
                builder.append(String.format("%02x", piece));
            }
            map.put("pw", builder.toString());
            database.hmset(id + "_info", map);
            return result;
        }

        return null;
    }


    public int change_PW(String id, String newPW) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var value = MessageDigest.getInstance("SHA-256");
        var builder = new StringBuilder();
        var map = new HashMap<String, String>();

        if (check_IDPW(id, newPW)) {
            value.update(newPW.getBytes());
            for (var piece : value.digest()) {
                builder.append(String.format("%02x", piece));
            }
            newPW = builder.toString();
            if (!(database.hget(id + "_info", "pw").equals(newPW))) {
                map.put("pw", newPW);
                database.hmset(id + "_info", map);
                // change success
                return 1;
            }
            // same as old one
            return 0;
        }
        // wrong form
        return -1;
    }

    public boolean check_IDPW(String id, String pw) {
        if ((id.length() > 9) && (id.length() < 16)) {
            if ((pw.length() > 9) && (pw.length() < 16)) {
                return true;
            }
        }
        return false;
    }

    public int check_Phone(String phone) {
        var database = mDatabase.getJedis();
        var members = database.smembers("members");

        if (phone.length() == 11) {
            for (var piece : members) {
                if (phone.equals(database.hget(piece + "_info", "phone"))) {
                    // duplicated number
                    return 0;
                }
            }
            // unique number
            return 1;
        }
        // wrong form
        return -1;
    }

    public boolean check_Ans(String[] ans) {
        boolean lengthCheck = false;

        if ((ans[1].length() > 1) && (ans[1].length() < 31)) {
            if ((ans[3].length() > 1) && (ans[3].length() < 31)) {
                lengthCheck = true;
            }
        }
        if (lengthCheck) {
            for (var piece : ans) {
                if (piece.contains("|")) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
