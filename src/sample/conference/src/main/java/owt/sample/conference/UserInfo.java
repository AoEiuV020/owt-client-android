package owt.sample.conference;

@SuppressWarnings("unused")
public class UserInfo {
    private String participantId;
    private String streamId;
    private String username;
    private String avatarUrl;

    @Override
    public String toString() {
        return "UserInfo{" +
                "participantId='" + participantId + '\'' +
                ", streamId='" + streamId + '\'' +
                ", username='" + username + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
