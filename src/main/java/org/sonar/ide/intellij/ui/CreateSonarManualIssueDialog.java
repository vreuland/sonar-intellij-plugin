package org.sonar.ide.intellij.ui;

import com.intellij.CommonBundle;
import com.intellij.ide.actions.TemplateKindCombo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.repository.SonarIssueRepository;
import org.sonar.ide.intellij.repository.SonarRuleRepository;
import org.sonar.ide.intellij.repository.SonarUserRepository;
import org.sonar.ide.intellij.ui.utils.ComboBoxUtils;
import org.sonar.wsclient.issue.NewIssue;
import org.sonar.wsclient.services.Rule;
import org.sonar.wsclient.user.User;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Vincent Reuland
 */
public class CreateSonarManualIssueDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField projectField;
    private JTextField lineNumberField;
    private TemplateKindCombo severityComboBox;
    private com.intellij.openapi.ui.ComboBox ruleComboBox;
    private JTextField resourceField;
    private com.intellij.openapi.ui.ComboBox assigneeComboBox;
    private JTextArea descriptionTextArea;

    private final SonarRuleRepository ruleRepository = new SonarRuleRepository();
    private final SonarUserRepository userRepository = new SonarUserRepository();
    private final SonarIssueRepository issueRepository = new SonarIssueRepository();

    private static final String NO_LINE_NUMBER = "-";

    public CreateSonarManualIssueDialog(Project project, SonarModuleComponent sonarModuleComponent,
                                        String sonarResourceName, Integer lineNumber) {
        super(project);
        //getRootPane().setDefaultButton(buttonOK);
        setTitle("Create Manual Issue");
        projectField.setText(sonarModuleComponent.getState().projectKey);
        resourceField.setText(sonarResourceName);
        lineNumberField.setText(lineNumber != null ? String.valueOf(lineNumber) : NO_LINE_NUMBER);
        initRuleComboBox(sonarModuleComponent);
        initSeverityComboBox();
        initAssigneeComboBox(sonarModuleComponent);
        init();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return ruleComboBox;
    }

    @Override
    protected void doOKAction(){
        if (isDescriptionEmpty()) {
            Messages.showMessageDialog(descriptionTextArea, "Please enter a description", CommonBundle.getErrorTitle(),
                    Messages.getErrorIcon());
            return;
        }

        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public NewIssue getNewIssue() {
        NewIssue newIssue = NewIssue.create();
        newIssue.component(projectField.getText() + ':' + resourceField.getText());
        if (!NO_LINE_NUMBER.equals(lineNumberField.getText())) {
            newIssue.line(Integer.valueOf(lineNumberField.getText()));
        }
        newIssue.message(descriptionTextArea.getText());
        newIssue.rule(((Rule) ruleComboBox.getSelectedItem()).getKey());
        newIssue.severity(severityComboBox.getSelectedName());
        return newIssue;
    }

    @Nullable
    public User getIssueAssignee() {
        return ((AssigneeComboItem) assigneeComboBox.getSelectedItem()).user;
    }

    private boolean isDescriptionEmpty() {
        String issueDescription = descriptionTextArea.getText();
        if (issueDescription == null || issueDescription.trim().length() == 0) {
          return true;
        }
        return false;
    }

    private void initRuleComboBox(SonarModuleComponent sonarModuleComponent) {
        // Set a item listener to populate automatically description text area with rule default description
        ruleComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    descriptionTextArea.setText(((Rule) e.getItem()).getDescription());
                }
            }
        });

        // Adding entries
        for (Rule rule : ruleRepository.getAllManualRules(sonarModuleComponent)) {
            ruleComboBox.addItem(rule);
        }

        ComboBoxUtils.renderItemsAsSearchableStrings(ruleComboBox, new ComboBoxUtils.ItemToStringConverter<Rule>() {
            @Override
            public String convert(Rule item) {
                return item.getTitle();
            }
        });
    }

    private void initSeverityComboBox() {
        // Adding entries
        for (String severity : issueRepository.getAllIssueSeverities()) {
            severityComboBox.addItem(severity, IconLoader.getIcon("/icons/" + severity + ".png"), severity);
        }

        // Set default item
        severityComboBox.setSelectedName(issueRepository.getDefaultSeverity());
    }

    private void initAssigneeComboBox(SonarModuleComponent sonarModuleComponent) {
        // Adding entries
        assigneeComboBox.addItem(AssigneeComboItem.noAssignee());
        for (User user : userRepository.getAllActiveUsers(sonarModuleComponent)) {
            assigneeComboBox.addItem(new AssigneeComboItem(user));
        }

        ComboBoxUtils.renderItemsAsSearchableStrings(assigneeComboBox, new ComboBoxUtils.ItemToStringConverter<AssigneeComboItem>() {
            @Override
            public String convert(AssigneeComboItem assigneeItem) {
                return assigneeItem.isUnassigned() ? "-- Unassigned -- " :
                        assigneeItem.user.name() + " (" + assigneeItem.user.login() + ")";
            }
        });
    }

    /**
     * Represents an item of the assignee combo box, including the "Unassigned" item (no assignee)
     */
    private static class AssigneeComboItem
    {
        private User user;

        static AssigneeComboItem noAssignee() {
            return new AssigneeComboItem(null);
        }

        AssigneeComboItem(User user) {
            this.user = user;
        }

        boolean isUnassigned() {
            return user == null;
        }
    }

}
