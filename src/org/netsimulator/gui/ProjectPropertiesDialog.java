/*
NET-Simulator -- Network simulator.
Copyright (C) 2006 Maxim Tereshin <maxim-tereshin@yandex.ru>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
            
This program is distributed in the hope that it will be useful, but 
WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.
            
You should have received a copy of the GNU General Public License along 
with this program; if not, write to the Free Software Foundation, 
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/


package org.netsimulator.gui;

import java.util.ResourceBundle;
import java.util.Locale;

public class ProjectPropertiesDialog extends javax.swing.JDialog
{
    private static final ResourceBundle rsc = ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private boolean pressedOk = false;
    
    /** Creates new form DesktopPropertiesDialog */
    public ProjectPropertiesDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        
        setSize(450, 230);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        centerPanel = new javax.swing.JPanel();
        authorLabel = new javax.swing.JLabel();
        authorTextField = new javax.swing.JTextField();
        commentLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        commentTextArea = new javax.swing.JTextArea();
        southPanel = new javax.swing.JPanel();
        CancelButton = new javax.swing.JButton();
        OkButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(rsc.getString("Properties"));
        setAlwaysOnTop(true);
        setModal(true);
        centerPanel.setLayout(new java.awt.GridBagLayout());

        authorLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        authorLabel.setText(rsc.getString("Author:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        centerPanel.add(authorLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 2);
        centerPanel.add(authorTextField, gridBagConstraints);

        commentLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        commentLabel.setText(rsc.getString("Description:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        centerPanel.add(commentLabel, gridBagConstraints);

        jScrollPane1.setViewportView(commentTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        centerPanel.add(jScrollPane1, gridBagConstraints);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        CancelButton.setText(rsc.getString("Cancel"));
        CancelButton.setActionCommand("\u041e\u0442\u043c\u0435\u043d\u0438\u0442\u044c");
        CancelButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                CancelButtonActionPerformed(evt);
            }
        });

        southPanel.add(CancelButton);

        OkButton.setText(rsc.getString("Continue"));
        OkButton.setActionCommand("\u041f\u0440\u043e\u0434\u043e\u043b\u0436\u0438\u0442\u044c");
        OkButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                OkButtonActionPerformed(evt);
            }
        });

        southPanel.add(OkButton);

        getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OkButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_OkButtonActionPerformed
    {//GEN-HEADEREND:event_OkButtonActionPerformed
        setVisible(false);
        pressedOk = true;
    }//GEN-LAST:event_OkButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_CancelButtonActionPerformed
    {//GEN-HEADEREND:event_CancelButtonActionPerformed
        setVisible(false);
        pressedOk = false;
    }//GEN-LAST:event_CancelButtonActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new DesktopPropertiesDialog(new javax.swing.JFrame(), true).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton OkButton;
    private javax.swing.JLabel authorLabel;
    private javax.swing.JTextField authorTextField;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JTextArea commentTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel southPanel;
    // End of variables declaration//GEN-END:variables
    
    
    public void clear()
    {
        authorTextField.setText(null);
        commentTextArea.setText(null);
    }
    
    public void setAuthor(String author)
    {
        authorTextField.setText(author);
    }

    public void setComment(String comment)
    {
        commentTextArea.setText(comment);
    }

    public String getAuthor()
    {
        return authorTextField.getText();
    }
    
    public String getComment()
    {
        return commentTextArea.getText();
    }
    
    public boolean pressedOk()
    {
        return pressedOk;
    }
}
