package com.example.sessiondemo;

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ClientAuthInitialize implements AuthInitialize {
    public static final String USER_NAME = "security-username";
    public static final String PASSWORD = "security-password";
    private static String userName;
    private static String password;


    public static void setVCapServices(Map credentials) {
        System.setProperty("gemfire.security-client-auth-init", "com.example.sessiondemo.ClientAuthInitialize.create");
        userName = (String) ((Map) ((List) credentials.get("users")).get(0)).get("username");
        password = (String) ((Map) ((List) credentials.get("users")).get(0)).get("password");
    }

    public static AuthInitialize create() {
        return new ClientAuthInitialize();
    }

    @Override
    public void close() {
    }

    @Override
    public Properties getCredentials(Properties arg0, DistributedMember arg1,
                                     boolean arg2) throws AuthenticationFailedException {
        Properties props = new Properties();
        props.put(USER_NAME, userName);
        props.put(PASSWORD, password);
        return props;
    }

    @Override
    public void init(LogWriter arg0, LogWriter arg1)
            throws AuthenticationFailedException {
    }
}
