package org.motechproject.security.osgi.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.motechproject.security.domain.MotechSecurityConfiguration;
import org.motechproject.security.domain.MotechURLSecurityRule;

public final class SecurityTestConfigBuilder {

    private SecurityTestConfigBuilder() { 
        //static class 
    }

    public static MotechSecurityConfiguration buildConfig(String testOption, String configOption, String configOption2) {
        List<MotechURLSecurityRule> newRules = new ArrayList<MotechURLSecurityRule>();
        List<String> supportedSchemes = new ArrayList<String>();
        Set<String> methodsRequired = new HashSet<String>();
        List<String> permissionAccess = new ArrayList<String>();
        List<String> userAccess = new ArrayList<String>();

        MotechURLSecurityRule rule1 = new MotechURLSecurityRule();
        MotechURLSecurityRule rule2 = new MotechURLSecurityRule();

        rule1.setPattern("/**/web-api/**");
        rule1.setOrigin("test");
        rule1.setProtocol("HTTP");
        rule1.setRest(true);
        rule1.setVersion("1");

        rule2.setPattern("/**");
        rule2.setOrigin("test");
        rule2.setProtocol("HTTP");
        rule2.setRest(true);
        rule2.setVersion("1");

        newRules.add(rule1);
        newRules.add(rule2);

        switch (testOption) {
            case "addUserAccess" : 
                userAccess.add(configOption);
                rule1.setUserAccess(userAccess);
                supportedSchemes.add("USERNAME_PASSWORD");
                supportedSchemes.add("BASIC");
                methodsRequired.add("ANY");
                break;
            case "addPermissionAccess" : 
                permissionAccess.add(configOption);
                rule1.setPermissionAccess(permissionAccess);
                supportedSchemes.add("USERNAME_PASSWORD");
                supportedSchemes.add("BASIC");
                methodsRequired.add("ANY");
                break;
            case "methodSpecific" : 
                supportedSchemes.add("USERNAME_PASSWORD");
                supportedSchemes.add("BASIC");
                methodsRequired.add(configOption);
                permissionAccess.add(configOption2);
                rule1.setPermissionAccess(permissionAccess);
                break;
            case "loginAccess" :         
                supportedSchemes.add("USERNAME_PASSWORD");
                supportedSchemes.add("OATH");
                methodsRequired.add("ANY");
                rule1.setRest(false);
                break;
            case "noSecurity" :
                newRules.remove(rule1);
                supportedSchemes.add("NO_SECURITY");
                methodsRequired.add("ANY");
                break;
            default : break;
        }

        rule1.setMethodsRequired(methodsRequired);
        rule1.setSupportedSchemes(supportedSchemes);

        rule2.setMethodsRequired(methodsRequired);
        rule2.setSupportedSchemes(supportedSchemes);

        return new MotechSecurityConfiguration(newRules);
    }
}