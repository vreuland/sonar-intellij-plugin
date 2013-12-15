package org.sonar.ide.intellij.repository;

import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Rule;
import org.sonar.wsclient.services.RuleQuery;
import org.sonar.wsclient.user.User;

import java.util.List;

/**
 * Created by Vincent on 14/12/13.
 */
public class SonarRuleRepository {

    public List<Rule> getAllManualRules(SonarModuleComponent sonarModuleComponent) {
        Sonar sonar = sonarModuleComponent.getSonar();
        RuleQuery manualRulesQuery = new RuleQuery("");
        manualRulesQuery.setActive(true);
        manualRulesQuery.setRepositories("manual");
        return sonar.findAll(manualRulesQuery);
    }

}
