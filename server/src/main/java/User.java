public class User {
    private String sessionId;
    private String userCode;

    public User(String sessionId, String userCode){
        this.sessionId = sessionId;
        this.userCode = userCode;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getSessionId() {
        return sessionId;
    }

}

