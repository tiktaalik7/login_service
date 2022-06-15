package ten;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Executor {
    Database mDatabase = Database.getInstance();

    /**
     * if it is registered member, returns true, otherwise returns false.
     * check id, pw.
     */
    private boolean login(String id, String pw) {
        var database = mDatabase.getJedis();

        if(check_IDPW(id, pw)) {
            if(database.sismember("members", id) && database.hget(id + "_info", "pw").equals(pw)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * TODO implement logout()
     */
    private void logout() {
        
    }

    /**
     * check duplicated id and form of id, pw, phone number, answers.
     * if it satisfies all steps, register the user by using parameters.
     */
    private boolean register(String id, String pw, String phone, String[] ans) {
        var database = mDatabase.getJedis();

        if(check_IDPW(id, pw) && !database.sismember("members", id)) {
            if(check_Phone(phone) && check_Ans(ans)) {
                // TODO change member instance
                Member member = new Member();
                try {
                    member.set(id, pw, phone, ans);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        }

        return false;
    }

    /**
     * find id and return id using phone number.
     */
    private String find_ID(String phone) {
        var database = mDatabase.getJedis();
        String result = null;

        if(check_Phone(phone)) {
            Set<String> temp = database.smembers("members");
            for(var member : temp) {
                if(database.hget(member + "_info", "phone").equals(phone)) {
                    result = member;
                }
            }
        }

        return result;
    }

    /**
     * identify user by using id, phone number, answers.
     * check form of id, phone number, answers.
     * hashing new password created ramdomly using 'SHA-256' algorithm.
     * if it doesn't matched, returns null,  otherwise returns new password.
     */
    private String find_PW(String id, String phone, String[] ans) {
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

        if(check_IDPW(id, "password") && check_Phone(phone) && check_Ans(ans)) {
            var members = database.smembers("members");
            for(var member : members) {
                if(member.equals(id)) {
                    if(database.hget(id + "_info", "phone").equals(phone)) {
                        for(var piece : ans) {
                            builder.append(piece + "|");
                        }
                        value.update(builder.toString().getBytes());
                        builder.setLength(0);
                        for(var piece : value.digest()) {
                            builder.append(String.format("%02x", piece));
                        }
                        if(builder.toString().equals(database.hget(id + "_info", "ans"))){
                            flag = true;
                        }
                    }
                }
            }
        }
        if(flag) {
            builder.setLength(0);
            temp_PW = random.nextInt(100000);
            builder.append(String.format("%05d", temp_PW));
            temp_PW = random.nextInt(100000);
            builder.append(String.format("%05d", temp_PW));
            result = builder.toString();
            value.update(builder.toString().getBytes());
            builder.setLength(0);
            for(var piece : value.digest()) {
                builder.append(String.format("%02x", piece));
            }
            map.put("pw", builder.toString());
            database.hmset(id + "_info", map);
            return result;
        }

        return null;
    }

    /**
     * TODO implement change_PW()
     */
    private boolean change_PW(String id, String newPW) {
        var database = mDatabase.getJedis();
        return (Boolean) null;

    }

    /**
     * check form of id, pw.
     */
    private boolean check_IDPW(String id, String pw) {
        if((id.length() > 9) && (id.length() < 16)){
            if((pw.length() > 9) && (pw.length() < 16)) {
                return true;
            }
        }
        return false;
    }

    /**
     * check form of phone number.
     * if it was phone number in used, it returns true, otherwise returns false.
     */
    private boolean check_Phone(String phone) {
        var database = mDatabase.getJedis();
        var members = database.smembers("members");
        
        if(phone.length() == 11) {
            for(var piece : members) {
                if(phone.equals(database.hget(piece + "_info", "phone"))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * check form of answers.
     */
    private boolean check_Ans(String[] ans) {
        boolean lengthCheck = false;

        if((ans[1].length() > 1) && (ans[1].length() < 31)){
            if((ans[3].length() > 1) && (ans[3].length() < 31)) {
                lengthCheck = true;
            }
        }
        if(lengthCheck) {
            for(var piece : ans) {
                if(piece.contains("|")) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
