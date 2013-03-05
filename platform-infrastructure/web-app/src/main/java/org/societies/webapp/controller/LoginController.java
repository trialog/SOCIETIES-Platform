package org.societies.webapp.controller;

import org.societies.api.identity.IIdentity;
import org.societies.webapp.service.OpenfireLoginService;
import org.societies.webapp.service.UserService;
import org.springframework.stereotype.Controller;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

@Controller
@ManagedBean(name = "loginController") //required to access data from XHTML files
@SessionScoped // indicates the lifetime of this object
public class LoginController extends BasePageController {

    //    @Autowired
    @ManagedProperty(value = "#{userService}")
    private UserService userService;
    //    @Autowired
    @ManagedProperty(value = "#{openfireLoginService}")
    private OpenfireLoginService openfireLoginService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        log.trace("setUserService() has been called with " + userService);
        this.userService = userService;
    }

    public OpenfireLoginService getOpenfireLoginService() {
        return openfireLoginService;
    }

    public void setOpenfireLoginService(OpenfireLoginService openfireLoginService) {
        log.trace("setOpenfireLoginService() has been called with " + openfireLoginService);
        this.openfireLoginService = openfireLoginService;
    }

    //    private boolean loggedIn = false;
//    private String username = "";
//    private String userID = "";
    private String loginDialogUsername;
    private String loginDialogPassword;

    public LoginController() {
        log.trace("LoginController() ctor");
    }

    public String loginButtonAction() {
        // NB actions should return a String object - it is used by faces-config.xml

        // NB: use loginDialogUsername and loginDialogPassword - these are populated from the front end
        // "username" field should be populated by this method, then the loginDialogUsername and loginDialogPassword cleared

        if (isLoggedIn()) {
            // log out before logging in again
            logoutAction();
        }

        String result = openfireLoginService.doLogin(loginDialogUsername, loginDialogPassword);
        if (result == null) {
            String summary = "Login failed";
            String detail = "Incorrect username or password";
            addGlobalMessage(summary, detail, FacesMessage.SEVERITY_WARN);

            return "false";
        }

        userService.setUserLoggedIn(true);
        userService.loadUserDetailsFromCommMgr();

//        setLoggedIn(true);
//        username = userService.getUsername();
//        userID = userService.getUserID();

//        setUsername("paddy");
//        setUserID("ermahgerd");

        String summary = "User " + getUsername() + " logged in";
        String detail = "User successfully logged in";
        addGlobalMessage(summary, detail, FacesMessage.SEVERITY_INFO);

        // clean up
        setLoginDialogUsername("");
        setLoginDialogPassword("");

        return "true";
    }

    public String logoutAction() {
        log.trace("logoutAction()");

        if (!isLoggedIn())
            return "false";

        String summary = "User " + getUsername() + " logged out";
        String detail = "User logged out";

//        setLoggedIn(false);
//        setUsername("");
//        setUserID("");
        userService.setUserLoggedIn(false);

        addGlobalMessage(summary, detail, FacesMessage.SEVERITY_INFO);

        return "true";
    }

    public boolean isLoggedIn() {
//        log.trace("isLoggedIn()=" + loggedIn);
//        return loggedIn;
        if (userService == null) {
            log.error("userService is null - cannot determine login state");
            return false;
        }

        return userService.isUserLoggedIn();
    }

//    public void setLoggedIn(boolean loggedIn) {
//        this.loggedIn = loggedIn;
//    }

    public String getUsername() {
//        return username;
        return userService.getUsername();
    }

//    public void setUsername(String username) {
//        this.username = username;
//    }

    public String getUserID() {
//        return userID;
        return userService.getUserID();
    }

//    public void setUserID(String userID) {
//        this.userID = userID;
//    }

    public IIdentity getIdentity() {
//        return new IIdentity() {
//            @Override
//            public String getIdentifier() {
//                return "id";
//            }
//
//            @Override
//            public String getDomain() {
//                return "domain";
//            }
//
//            @Override
//            public IdentityType getType() {
//                return IdentityType.CIS;
//            }
//
//            @Override
//            public String getJid() {
//                return "jid";
//            }
//
//            @Override
//            public String getBareJid() {
//                return "bare jid";
//            }
//        };
        return userService.getIdentity();
    }

    public void setLoginDialogUsername(String username) {
        this.loginDialogUsername = username;
    }

    public String getLoginDialogUsername() {
        return loginDialogUsername;
    }

    public void setLoginDialogPassword(String password) {
        this.loginDialogPassword = password;
    }

    public String getLoginDialogPassword() {
        return loginDialogPassword;
    }
}
