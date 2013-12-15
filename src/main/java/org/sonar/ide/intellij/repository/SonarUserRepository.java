package org.sonar.ide.intellij.repository;

import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.wsclient.user.User;
import org.sonar.wsclient.user.UserQuery;

import java.util.List;

/**
 * @author Vincent Reuland
 */
public class SonarUserRepository {

    public List<User> getAllActiveUsers(SonarModuleComponent sonarModuleComponent) {
        return sonarModuleComponent.getSonarClient().userClient().find(UserQuery.create());
    }
}
