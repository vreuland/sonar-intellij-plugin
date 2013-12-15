package org.sonar.ide.intellij.repository;

import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.NewIssue;
import org.sonar.wsclient.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vincent Reuland
 */
public class SonarIssueRepository {

    public List<String> getAllIssueSeverities() {
        return Arrays.asList("BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO");
    }

    public String getDefaultSeverity() {
        return "MAJOR";
    }

    public void save(SonarModuleComponent moduleComponent, NewIssue newIssue, User assignee) {
        Issue createdIssue = sendNewIssueToSonar(moduleComponent, newIssue);
        if (assignee != null) {
            assignIssue(moduleComponent, createdIssue, assignee);
        }
    }

    private Issue sendNewIssueToSonar(SonarModuleComponent moduleComponent, NewIssue newIssue) {
        return moduleComponent.getSonarClient().issueClient().create(newIssue);
    }

    private Issue assignIssue(SonarModuleComponent moduleComponent, Issue issue, User assignee) {
        return moduleComponent.getSonarClient().issueClient().assign(issue.key(), assignee.login());
    }

}
