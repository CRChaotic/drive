package controller;

import java.util.List;

public class ShareFileInfoForm {
    private String accessToken;
    private String expiryTime;
    private List<Integer> userFileIds;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

    public List<Integer> getUserFileIds() {
        return userFileIds;
    }

    public void setUserFileIds(List<Integer> userFileIds) {
        this.userFileIds = userFileIds;
    }
}
