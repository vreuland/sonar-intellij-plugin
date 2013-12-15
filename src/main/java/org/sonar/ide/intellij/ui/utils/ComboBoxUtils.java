package org.sonar.ide.intellij.ui.utils;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ComboboxSpeedSearch;
import com.intellij.ui.ListCellRendererWrapper;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.sonar.wsclient.services.Rule;

import javax.swing.*;

/**
 * @author Vincent Reuland
 */
public final class ComboBoxUtils {


    /**
     * Add:
     * <ul>
     *     <li>a renderer that displays combo box items as {@link String}</li>
     *     <li>a search feature (same as the one present in others IntelliJ dialogs)</li>
     * </ul>
     *  to the given <code>comboBox</code>
     * <p>
     *     <u>Note:</u> Both features rely on the way the combo box items are represented (as {@link String}),
     *     and this method insures that the same item's string representation is used for both the
     *     rendering of the item and the search feature. Indeed, we always want the search criterion to match the
     *     way items are rendered in the combo box.
     * </p>
     * @param comboBox
     * @param converter
     */
    public static void renderItemsAsSearchableStrings(ComboBox comboBox, final ItemToStringConverter converter) {
        // Add the renderer
        comboBox.setRenderer(new ListCellRendererWrapper() {
            @Override
            public void customize(JList jList, Object item, int i, boolean b, boolean b2) {
                setText(converter.convert(item));
            }
        });

        // add search feature
        new ComboboxSpeedSearch(comboBox) {
            @Override
            protected String getElementText(Object element) {
                if (element != null) {
                    return converter.convert(element);
                }
                return null;
            }
        };
    }

    public interface ItemToStringConverter<T> {
        String convert(T item);
    }
}
