package controller.form;

import utils.Identifier;

import java.nio.charset.StandardCharsets;

public class ModifyPasswordForm {
    private String password;
    private String oldPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        if(oldPassword != null){
            Identifier identifier = new Identifier("SHA3-256");
            identifier.read(oldPassword.getBytes(StandardCharsets.UTF_8));
            this.oldPassword = identifier.getUniqueId();
        }else{
            this.oldPassword = null;
        }
    }
}
