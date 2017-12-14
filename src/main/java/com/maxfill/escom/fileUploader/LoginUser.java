package com.maxfill.escom.fileUploader;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginUser extends JFrame{
    private static final long serialVersionUID = 5505727943500445334L;
    private static final Logger LOGGER = Logger.getLogger(LoginUser.class.getName());

    private JPanel contentPane;
    private JButton btnLogin;
    private JTextField jUserName;
    private JPasswordField jPassword;
    private JTextField jServer;
    private JButton btnCancel;
    private JTextPane jErrMsg;

    private final Main main;

    public LoginUser(Main main) {
        this.main = main;

        $$$setupUI$$$();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(btnLogin);

        this.jErrMsg.setVisible(false);
        this.jServer.setText(main.getServerURL());
        showErrMsg(main.getErrMsg());

        btnLogin.addActionListener(e -> onOK());
        btnCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        try {
            // TODO проверить адрес сервера, а если не корректный то поругаться!
            // TODO проверить заполнение полей login и password
            String serverURL = jServer.getText().trim();
            String userLogin = jUserName.getText().trim();
            char[] userPassword = jPassword.getPassword();
            // подключиться к серверу для login и получения token
            if(main.loginToServer(serverURL, userLogin, userPassword)) {
                try {
                    main.uploadFile();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    this.jErrMsg.setText(ex.getMessage());
                }
                System.exit(0);
            } else {
                showErrMsg("Login failed! User name or password incorrect.");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            showErrMsg(ex.getMessage());
        }
    }

    private void onCancel() {
        System.exit(0);
    }

    private void showErrMsg(String msg) {
        if(StringUtils.isBlank(msg)) return;
        this.jErrMsg.setVisible(true);
        if(msg.length() > 100) {
            this.jErrMsg.setText(msg.substring(0, 100));
        } else {
            this.jErrMsg.setText(msg);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:grow"));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        CellConstraints cc = new CellConstraints();
        panel1.add(panel2, cc.xy(3, 1));
        btnLogin = new JButton();
        btnLogin.setText("Login");
        panel2.add(btnLogin, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancel = new JButton();
        btnCancel.setText("Cancel");
        panel2.add(btnCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("User name:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        jUserName = new JTextField();
        panel3.add(jUserName, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Password:");
        panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPassword = new JPasswordField();
        panel3.add(jPassword, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Server URL:");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jServer = new JTextField();
        panel3.add(jServer, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        jErrMsg = new JTextPane();
        jErrMsg.setForeground(new Color(-64251));
        jErrMsg.setText("");
        panel3.add(jErrMsg, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
    }

    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
