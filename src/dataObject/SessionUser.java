package dataObject;

public class SessionUser {

    public final int empId;
    public final String fullName;
    public final String username;
    public final String role;
    public final String profilePic;

    public SessionUser(int empId, String fullName, String username,
            String role, String profilePic) {
        
        this.empId = empId;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
        this.profilePic = profilePic;
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
