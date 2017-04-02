package swar8080.collaborativedrawing.connection;

/**
 *
 */

public class AvailableSession {

    private String hostId;
    private String sessionName;
    private Integer playerCount;


    public AvailableSession(String hostId, String sessionName, Integer playerCount) {
        this.hostId = hostId;
        this.sessionName = sessionName;
        this.playerCount = playerCount;
    }

    public AvailableSession(String hostId, String sessionName){
        this(hostId, sessionName, null);
    }

    public String getHostId(){
        return hostId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public boolean hasPlayerCount(){
        return playerCount != null;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer count){ playerCount = count; }
}
