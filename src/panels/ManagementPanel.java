package panels;

import dbquery.LeaveQuery;
import dataObject.LeaveRequestEntity;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;

public class ManagementPanel extends javax.swing.JPanel {

    public ManagementPanel() {
        initComponents();
//        displayPendingRequests();
    }

//    public void displayPendingRequests() {
//        DefaultTableModel model = new DefaultTableModel();
//        for (String col : new String[]{"ID", "Emp ID", "Name", "Type", "Start", "End", "Status"}) {
//            model.addColumn(col);
//        }
//
//        for (LeaveRequestEntity r : LeaveQuery.getPendingRequests()) {
//            model.addRow(new Object[]{
//                r.requestId,
//                r.empId,
//                r.fullName,
//                r.leaveType,
//                r.startDate,
//                r.endDate,
//                r.status
//            });
//        }
//        tblPendingRequests.setModel(model);
//    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        managementPnl = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblPendingRequests = new javax.swing.JTable();
        btnApprove = new javax.swing.JButton();
        btnDisapprove = new javax.swing.JButton();
        btnRefreshMgmt = new javax.swing.JButton();

        managementPnl.setBackground(new java.awt.Color(255, 255, 255));

        tblPendingRequests.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tblPendingRequests);

        btnApprove.setText("Approve");
        btnApprove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApproveActionPerformed(evt);
            }
        });

        btnDisapprove.setText("Disapprove");
        btnDisapprove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisapproveActionPerformed(evt);
            }
        });

        btnRefreshMgmt.setText("Refresh");
        btnRefreshMgmt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshMgmtActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout managementPnlLayout = new javax.swing.GroupLayout(managementPnl);
        managementPnl.setLayout(managementPnlLayout);
        managementPnlLayout.setHorizontalGroup(
            managementPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(managementPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(managementPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 758, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(managementPnlLayout.createSequentialGroup()
                        .addComponent(btnRefreshMgmt, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(168, 168, 168)
                        .addComponent(btnDisapprove)
                        .addGap(18, 18, 18)
                        .addComponent(btnApprove, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(259, Short.MAX_VALUE))
        );
        managementPnlLayout.setVerticalGroup(
            managementPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(managementPnlLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(managementPnlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefreshMgmt, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDisapprove, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnApprove, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1023, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(managementPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 724, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(managementPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnApproveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApproveActionPerformed
        int row = tblPendingRequests.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a request first!");
            return;
        }

        String requestId = tblPendingRequests.getValueAt(row, 0).toString();
        String empName = tblPendingRequests.getValueAt(row, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve leave for " + empName + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && LeaveQuery.updateStatus(requestId, "Approved")) {
            JOptionPane.showMessageDialog(this, "Leave for " + empName + " approved!");
//            displayPendingRequests();
        }
    }//GEN-LAST:event_btnApproveActionPerformed

    private void btnDisapproveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisapproveActionPerformed
        int row = tblPendingRequests.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a request first!");
            return;
        }

        String requestId = tblPendingRequests.getValueAt(row, 0).toString();
        if (LeaveQuery.updateStatus(requestId, "Disapproved")) {
            JOptionPane.showMessageDialog(this, "Request #" + requestId + " disapproved.");
//            displayPendingRequests();
        }
    }//GEN-LAST:event_btnDisapproveActionPerformed

    private void btnRefreshMgmtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshMgmtActionPerformed
//        displayPendingRequests();
    }//GEN-LAST:event_btnRefreshMgmtActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApprove;
    private javax.swing.JButton btnDisapprove;
    private javax.swing.JButton btnRefreshMgmt;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel managementPnl;
    private javax.swing.JTable tblPendingRequests;
    // End of variables declaration//GEN-END:variables
}
