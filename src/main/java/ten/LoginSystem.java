package ten;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class LoginSystem {
    Database mDatabase = Database.getInstance();
    private static final int EXISTED = 0;
    private static final int SUCCESS = 1;
    private static final int WRONG_FORM = -1;
    private static final int UNIQUE_NUMBER = 1;
    private static final int SEPERATOR_CONTAINED = 0;

    /*
     * 인자로 받은 아이디와 비밀번호로 로그인
     * 비밀번호는 SHA-256 해시값으로 변환해 비교
     */
    public boolean login(String id, String pw) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();
        var value = MessageDigest.getInstance("SHA-256");
        var builder = new StringBuilder();

        if (check_IDPW(id, pw)) {
            if (database.sismember("key_Members", id)) {
                value.update(pw.getBytes());
                for (var piece : value.digest()) {
                    builder.append(String.format("%02x", piece));
                }
                if (database.hget(id + "_info", "key_PW").equals(builder.toString())) {
                    return true;
                }
            }
        }

        return false;
    }
    
    /*
     * 로그아웃
     */
    public void logout() {

    }

    /*
     * 인자로 받은 아이디, 비밀번호, 전화번호, 본인확인질문으로 회원가입
     * 아이디, 비밀번호, 전화번호, 본인확인질문의 형식 검사
     * 아이디, 전화번호 중복 검사
     */
    public boolean register(String id, String pw, String phone, String[] ans) throws NoSuchAlgorithmException {
        var database = mDatabase.getJedis();

        if (check_IDPW(id, pw) && !database.sismember("key_Members", id)) {
            if ((check_Phone(phone) == UNIQUE_NUMBER) && check_Ans(ans) == SUCCESS) {
                Member member = new Member();
                member.set(id, pw, phone, ans);
                return true;
            }
        }

        return false;
    }

    /*
     * 인자로 받은 전화번호를 갖는 사용자를 찾아 아이디를 반환
     */
    public String find_ID(String phone) {
        var database = mDatabase.getJedis();
        String result = null;

        if (check_Phone(phone) == EXISTED) {
            Set<String> temp = database.smembers("key_Members");
            for (var member : temp) {
                if (database.hget(member + "_info", "key_Phone").equals(phone)) {
                    result = member;
                }
            }
        }

        return result;
    }

    /*
     * 인자로 받은 아이디, 전화번호, 본인확인질문으로 비밀번호 찾기
     * 아이디, 전화번호, 본인확인질문의 형식 검사
     * 인자로 받은 아이디에 해당하는 전화번호, 본인확인질문인지 비교
     * 본인확인질문은 SHA-256 해시값으로 변환해 비교
     * 임의의 정수 10자리로 구성된 임시 비밀번호를 생성해 반환
     * 임시비밀번호는 SHA-256 해시값으로 변환해 해당 아이디에 저장
     */
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

        if (check_IDPW(id, "password4test") 
        && check_Phone(phone) == EXISTED && check_Ans(ans) == SUCCESS) {
            var members = database.smembers("key_Members");
            for (var member : members) {
                if (member.equals(id)) {
                    if (database.hget(id + "_info", "key_Phone").equals(phone)) {
                        for (var piece : ans) {
                            builder.append(piece + "|");
                        }
                        value.update(builder.toString().getBytes());
                        builder.setLength(0);
                        for (var piece : value.digest()) {
                            builder.append(String.format("%02x", piece));
                        }
                        if (builder.toString().equals(database.hget(id + "_info", "key_Ans"))) {
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
            map.put("key_PW", builder.toString());
            database.hmset(id + "_info", map);
            return result;
        }

        return null;
    }

    /*
     * 인자로 받은 아이디, 새비밀번호로 비밀번호 변경
     * 아이디와 새비밀번호의 형식 검사
     * 새비밀번호와 기존비밀번호가 같은지 검사
     * 새비밀번호는 SHA-256 해시값으로 변환해 비교
     * 새비밀번호의 형식이 맞지 않은 경우 -1을 반환
     * 새비밀번호가 기존비밀번호와 동일한 경우 0을 반환
     * 새비밀번호가 모든 조건을 충족했을 경우 해당 아이디에 저장하고 1을 반환
     */
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
            if (!(database.hget(id + "_info", "key_PW").equals(newPW))) {
                map.put("key_PW", newPW);
                database.hmset(id + "_info", map);
                return SUCCESS;
            }
            return EXISTED;
        }
        return WRONG_FORM;
    }

    /*
     * 인자로 받은 아이디, 비밀번호의 형식을 검사
     */
    public boolean check_IDPW(String id, String pw) {
        if ((id.length() > 9) && (id.length() < 16)) {
            if ((pw.length() > 9) && (pw.length() < 16)) {
                return true;
            }
        }
        return false;
    }

    /*
     * 인자로 받은 전화번호의 형식, 중복을 검사
     * 전화번호의 형식이 맞지 않은 경우 -1을 반환
     * 전화번호가 중복된 경우 0을 반환
     * 전화번호가 모든 조건을 충족했을 경우 1을 반환
     */
    public int check_Phone(String phone) {
        var database = mDatabase.getJedis();
        var members = database.smembers("key_Members");

        if (phone.length() == 11) {
            for (var piece : members) {
                if (phone.equals(database.hget(piece + "_info", "key_Phone"))) {
                    return EXISTED;
                }
            }
            return UNIQUE_NUMBER;
        }
        return WRONG_FORM;
    }

    /*
     * 인자로 받은 본인봑인질문의 형식을 검사
     * 본인확인질문의 형식이 맞지 않은 경우 -1을 반환
     * 본인확인질문 중 '|'를 포함한 경우 0을 반환
     * 본인확인질문이 모든 조건을 충족했을 경우 1을 반환
     */
    public int check_Ans(String[] ans) {

        if ((ans[1].length() > 1) && (ans[1].length() < 31)) {
            if ((ans[3].length() > 1) && (ans[3].length() < 31)) {
                for (var piece : ans) {
                    if (piece.contains("|")) {
                        return SEPERATOR_CONTAINED;
                    }
                }
                return SUCCESS;
            }
        }
        return WRONG_FORM;
    }
}
