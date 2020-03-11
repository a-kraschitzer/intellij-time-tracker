// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package net.kraschitzer.intellij.plugin.time_tracker;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;

/**
 * Creates an action group to contain menu actions. See plugin.xml declarations.
 *
 * @author Anna Bulenkova
 * @author jhake
 */
public class CustomDefaultActionGroup extends DefaultActionGroup {

  /**
   * Given CustomDefaultActionGroup is derived from ActionGroup, in this context
   * update() determines whether the action group itself should be enabled or disabled.
   * Requires an editor to be active in order to enable the group functionality.
   *
   * @param event Event received when the associated group-id menu is chosen.
   * @see com.intellij.openapi.actionSystem.AnAction#update(AnActionEvent)
   */
  @Override
  public void update(AnActionEvent event) {
    // Enable/disable depending on whether user is editing
    Editor editor = event.getData(CommonDataKeys.EDITOR);
    event.getPresentation().setEnabled(editor != null);
    // Take this opportunity to set an icon for the menu entry.
  }
}
