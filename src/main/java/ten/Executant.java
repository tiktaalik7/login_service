package ten;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Set;

public class Executant {
    Database mDatabase = Database.getInstance();

    private boolean login(String id, String pw) {
        var database = mDatabase.getJedis();
        boolean result = false;

        if(check_IDPW(id, pw)) {
            if(database.sismember("members", id) && database.hget(id + "_info", "pw").equals(pw)) {
                result = true;
            }
        }
        
        return result;
    }

    private void logout() {
        
    }

    private boolean register(String id, String pw, String phone, String[] ans) {
        var database = mDatabase.getJedis();
        boolean result = false;

        if(check_IDPW(id, pw) && !database.sismember("members", id)) {
            if(check_Phone(phone) && check_Answer(ans)) {
                // TODO change member instance
                Member member = new Member();
                member.set(id, pw, phone, ans);
                result = true;
            }
        }

        return result;
    }

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

    private String find_PW(String id, String phone, String[] ans) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        String sentence = null;
        String result = null;
        int temp_PW = 0;
        var random = new Random();
        var value = MessageDigest.getInstance("SHA-256");
        var builder = new StringBuilder();

        if(check_IDPW(id, "password") && check_Phone(phone) && check_Answer(ans)) {
            Set<String> temp = database.smembers("members");
            for(var member : temp) {
                if(member.equals(id)) {
                    if(database.hget(id + "_info", "phone").equals(phone)) {
                        for(var add : ans) {
                            sentence += add + "|";
                        }
                        value.update(sentence.getBytes());
                        for(var piece : value.digest()) {
                            builder.append(String.format("%02x", piece));
                        }
                        sentence = builder.toString();
                        if(sentence.equals(database.hget(id + "_info", "ans"))){
                            temp_PW = random.nextInt(100000);
                            result = String.format("%05d", temp_PW);
                            temp_PW = random.nextInt(100000);
                            result += String.format("%05d", temp_PW);
                            return result;
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean change_PW(String id, String newPW) {
        var database = mDatabase.getJedis();
        return (Boolean) null;

    }

    private boolean check_IDPW(String id, String pw) {
        var database = mDatabase.getJedis();
        return (Boolean) null;
    }

    private boolean check_Phone(String phone) {
        var database = mDatabase.getJedis();
        return (Boolean) null;
    }

    private boolean check_Answer(String[] ans) {
        return (Boolean) null;
    }
}
