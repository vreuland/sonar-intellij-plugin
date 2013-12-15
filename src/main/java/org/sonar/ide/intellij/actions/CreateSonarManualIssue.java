package org.sonar.ide.intellij.actions;

import com.intellij.CommonBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.sonar.ide.intellij.component.SonarModuleComponent;
import org.sonar.ide.intellij.repository.SonarIssueRepository;
import org.sonar.ide.intellij.ui.CreateSonarManualIssueDialog;
import org.sonar.ide.intellij.utils.SonarResourceKeyUtils;
import org.sonar.ide.intellij.worker.ResourceLookupWorker;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.NewIssue;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.user.User;

/**
 * @author Vincent Reuland
 */
public class CreateSonarManualIssue extends DumbAwareAction {

    private static final Logger log = Logger.getInstance(CreateSonarManualIssue.class);
    private final SonarIssueRepository issueRepository = new SonarIssueRepository();

    @Override
    public void actionPerformed(final AnActionEvent e) {

        final Project project = e.getData(PlatformDataKeys.PROJECT);
        final VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        if (project == null || file == null || project.isDefault()) {
            return;
        }

        ResourceLookupWorker resourceLookupWorker = new ResourceLookupWorker(project, file, new ResourceLookupWorker.ResourceLoadCallback() {
            @Override
            public void resourceLoaded(@Nullable Resource resource) {

                if(resource == null) {
                    Messages.showMessageDialog(project, "Please make sure the selected " +
                            "resource is in the scope of sonar scan and that is has already been scanned by Sonar",
                            "Resource is unknown by Sonar",
                            Messages.getErrorIcon());
                    return;
                }

                SonarModuleComponent sonarModuleComponent = SonarResourceKeyUtils.getSonarModuleComponent(project, file);
                CreateSonarManualIssueDialog dialog = new CreateSonarManualIssueDialog(project, sonarModuleComponent,
                        resource.getLongName(), getLineHeightFromContext(e));
                dialog.show();

                if (dialog.isOK()) {
                    issueRepository.save(sonarModuleComponent, dialog.getNewIssue(), dialog.getIssueAssignee());
                }
            }
        });
        resourceLookupWorker.execute();
    }

    /**
     * @return the line number the cursor is on if the given action is performed in an open editor.
     * Return <code>null</code> if the action is performed outside of an editor.
     */
    private Integer getLineHeightFromContext(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        return editor != null ? editor.getCaretModel().getLogicalPosition().line + 1: null;
    }




}
