package ten;

public class Executant {
    Database mDatabase = Database.getInstance();

    private boolean login() {
        var database = mDatabase.getJedis();
        return (Boolean) null;
    }

    private void logout() {
        
    }

    private boolean register(String id, String pw, String phone, String[] ans) {
        var database = mDatabase.getJedis();
        return (Boolean) null;
    }

    private String find_ID(String phone) {
        var database = mDatabase.getJedis();
        return null;
    }

    private void find_PW(String id, String phone, String[] ans) {
        var database = mDatabase.getJedis();
    }

    private boolean change_PW(String id, String newPW) {
        var database = mDatabase.getJedis();
        return (Boolean) null;

    }

    private boolean check_IDPW() {
        var database = mDatabase.getJedis();
        return (Boolean) null;
    }

    private boolean check_Phone() {
        var database = mDatabase.getJedis();
        return (Boolean) null;
    }

    private boolean check_Answer() {
        return (Boolean) null;
    }
}
