package server.Gui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel {
    private final Set<String> allUserNames = new HashSet<>();
    private String newMessage;
    private String lastJoinedUser;
    private String lastLeavingUser;

    public Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }

    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }

    public String getLastJoinedUser() {
        return lastJoinedUser;
    }

    public String getLastLeavingUser() {
        return lastLeavingUser;
    }

    public void setLastJoinedUser(String lastJoinedUser) {
        this.lastJoinedUser = lastJoinedUser;
    }

    public void setLastLeavingUser(String lastLeavingUser) {
        this.lastLeavingUser = lastLeavingUser;
    }
}
