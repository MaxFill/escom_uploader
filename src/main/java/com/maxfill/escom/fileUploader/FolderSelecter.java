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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class FolderSelecter extends JFrame implements TreeSelectionListener{
    private static final long serialVersionUID = 295318601675513431L;
    
    private JTextField jSelectedFolder;
    private JTree treeFolders;
    private JButton btnCancel;
    private JButton btnSelect;
    private JPanel contentPane;
    private JTextPane jErrMsg;
    private JPanel JErrParent;
    private JButton btnContinue;

    private final Callback callback;
    private final Main main;

    private DefaultMutableTreeNode selected;

    public FolderSelecter(Main main, Callback callback) {
        this.main = main;
        this.callback = callback;

        $$$setupUI$$$();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        setContentPane(contentPane);
        getRootPane().setDefaultButton(btnSelect);

        btnContinue.setVisible(main.getNeedSelectFolder());
        btnSelect.setEnabled(true);

        btnCancel.addActionListener(e -> onCancel());
        btnContinue.addActionListener(e -> onContinue());
        btnSelect.addActionListener(e -> onSelect());

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
        btnSelect.setToolTipText("To select a highlighted folder");
        selected = new DefaultMutableTreeNode(new Folder(null, 0, "Архив", true));
        treeFolders = new JTree(selected);
        treeFolders.addTreeSelectionListener(this);
        treeFolders.setCellRenderer(new FoldersCellRenderer());
    }

    /**
     * Закрытие окна для продолжения работы программы
     */
    private void onContinue() {
        closeAndBack();
    }

    /**
     * Сохранение выбранной папки
     */
    private void onSelect() {
        Folder folder = (Folder) selected.getUserObject();
        String folderName = Utils.getPath(folder);
        main.setFolderName(folderName);
        main.setFolderId(String.valueOf(folder.getId()));
        main.saveProperties();
        jSelectedFolder.setText(folderName);
        btnContinue.setEnabled(true);
        if(!main.getNeedSelectFolder()) {
            closeAndBack();
        }
    }

    private void closeAndBack() {
        dispose();
        callback.goToBack();
    }

    /**
     * Закрытие окна без сохранения выбранной папки
     */
    private void onCancel() {
        System.out.println("The user pressed the <Cancel> button.");
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
            treeFolders.expandPath(arg0.getPath());
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
        contentPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 5, new Insets(4, 4, 4, 4), -1, -1));
        contentPane.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        btnCancel.setToolTipText("Exit from program without saving changes");
        panel1.add(btnCancel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, new Dimension(339, 11), null, 0, false));
        btnSelect = new JButton();
        btnSelect.setText("Select");
        btnSelect.setToolTipText("To select a highlighted folder");
        panel1.add(btnSelect, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnContinue = new JButton();
        btnContinue.setText("Continue");
        btnContinue.setToolTipText("Continue work with selected folder");
        panel1.add(btnContinue, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        panel2.add(treeFolders, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 150), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(4, 4, 4, 4), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label1 = new JLabel();
        label1.setText("Selected folder:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jSelectedFolder = new JTextField();
        panel3.add(jSelectedFolder, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        JErrParent = new JPanel();
        JErrParent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(JErrParent, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        JErrParent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        jErrMsg = new JTextPane();
        jErrMsg.setForeground(new Color(-64251));
        jErrMsg.setText("ErrMsg");
        JErrParent.add(jErrMsg, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(100, 50), null, 0, false));
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
