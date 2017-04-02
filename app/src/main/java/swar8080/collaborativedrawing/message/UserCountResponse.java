package swar8080.collaborativedrawing.message;

/**
 *
 */

public class UserCountResponse {

    private int userCount;

    public UserCountResponse(int userCount) {
        this.userCount = userCount;
    }

    public int getUserCount() {
        return userCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        UserCountResponse other = (UserCountResponse)obj;
        return this.userCount == other.userCount;
    }
}
