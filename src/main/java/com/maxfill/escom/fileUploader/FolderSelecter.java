package com.maxfill.escom.fileUploader;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.maxfill.escom.fileUploader.folders.Folder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;

public class FolderSelecter extends JFrame implements TreeSelectionListener{
    private JTextField jSelectedFolder;
    private JTree treeFolders;
    private JButton btnCancel;
    private JButton btnSelect;
    private JPanel contentPane;
    private JTextPane jErrMsg;
    private JPanel JErrParent;
    private JButton btnContinue;

    private final Main main;
    private DefaultMutableTreeNode selected;

    public FolderSelecter(Main main) {
        this.main = main;
        $$$setupUI$$$();
        createUIComponents();

        setContentPane(contentPane);
        getRootPane().setDefaultButton(btnSelect);

        btnContinue.setVisible(main.getNeedSelectFolder());
        btnSelect.setEnabled(false);

        btnSelect.addActionListener(e -> onSelect());
        btnCancel.addActionListener(e -> onCancel());
        btnContinue.addActionListener(e -> onContinue());

        if(StringUtils.isNotBlank(main.getFolderName())) {
            jSelectedFolder.setText(main.getFolderName());
            JErrParent.setVisible(false);
            btnContinue.setEnabled(true);
        } else {
            JErrParent.setVisible(true);
            btnContinue.setEnabled(false);
            jErrMsg.setText("You need to specify the folder!");
        }
    }

    private void createUIComponents() {
        btnSelect = new JButton();
        btnSelect.setText("Select");
        selected = new DefaultMutableTreeNode(new Folder(null, 0, "Архив", true));
        treeFolders = new JTree(selected);
        treeFolders.addTreeSelectionListener(this);
        treeFolders.setCellRenderer(new FoldersCellRenderer());

    }

    /**
     * Закрытие окна. Дальнейшее поведение зависит от слушателя данного события
     */
    private void onContinue(){
       dispose();
       dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Сохранение выбранной папки
     */
    private void onSelect() {
        Folder folder = (Folder) selected.getUserObject();
        main.setFolderName(Utils.getPath(folder));
        main.setFolderId(String.valueOf(folder.getId()));
        main.saveProperties();
        if (!main.getNeedSelectFolder()) {
            dispose();
        }
    }

    /**
     * Закрытие окна без сохранения выбранной папки
     */
    private void onCancel() {
        System.exit(0);
    }

    @Override
    public void valueChanged(TreeSelectionEvent arg0) {
        selected = (DefaultMutableTreeNode) arg0.getNewLeadSelectionPath().getLastPathComponent();
        if(selected == null) return;
        Folder folder = (Folder) selected.getUserObject();
        if(folder.isChildsLoaded()) return;
        try {
            List <Folder> folders = main.loadFolders(folder);
            folders.stream().forEach(f -> selected.add(new DefaultMutableTreeNode(f)));
            treeFolders.expandPath(arg0.getNewLeadSelectionPath());
            folder.setChildsLoaded(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        panel1.add(btnCancel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(339, 11), null, 0, false));
        btnSelect = new JButton();
        btnSelect.setText("Select");
        panel1.add(btnSelect, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panel2.add(treeFolders, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 150), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Selected folder:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jSelectedFolder = new JTextField();
        panel3.add(jSelectedFolder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        JErrParent = new JPanel();
        JErrParent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(JErrParent, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        jErrMsg = new JTextPane();
        jErrMsg.setForeground(new Color(-64251));
        jErrMsg.setText("ErrMsg");
        JErrParent.add(jErrMsg, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(150, 50), null, 0, false));
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public class FoldersCellRenderer extends DefaultTreeCellRenderer{
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

            Folder folder = (Folder) ((DefaultMutableTreeNode) value).getUserObject();

            if(folder.getReadOnly()) {
                btnSelect.setEnabled(false);
                setForeground(new Color(139, 141, 141));
            } else {
                btnSelect.setEnabled(true);
            }

            return this;
        }
    }
}
