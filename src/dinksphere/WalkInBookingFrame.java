/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package dinksphere;

/**
 *
 * @author juliepearledoria
 */
public class WalkInBookingFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(WalkInBookingFrame.class.getName());

    /**
     * Creates new form WalkInBookingFrame
     */
    
public WalkInBookingFrame() {
    initComponents();

    setSize(1200, 780);
    setResizable(false);
    setLocationRelativeTo(null);

    calendarWalkIn.setMinSelectableDate(new java.util.Date());

    cmbCourt.removeAllItems();
    cmbCourt.addItem("Court Athena");
    cmbCourt.addItem("Court Apollo");
    cmbCourt.addItem("Court Atlas");
    cmbCourt.addItem("Court Aurora");

    calendarWalkIn.getDayChooser()
        .addPropertyChangeListener("day", evt -> {
            if ("day".equals(evt.getPropertyName())) {
                java.util.Calendar selectedCalendar =
                    calendarWalkIn.getCalendar();

                if (selectedCalendar != null) {
                    resetWalkInSelection();

java.util.Date date = selectedCalendar.getTime();
lblSummaryDate.setText(
    "DATE: " + new java.text.SimpleDateFormat("MMMM dd, yyyy")
        .format(date)
);
loadTimeSlots(date);
                }
            }
        });

    cmbCourt.addActionListener(e -> {
        resetWalkInSelection();

java.util.Calendar selectedCalendar = calendarWalkIn.getCalendar();

if (selectedCalendar != null) {
    java.util.Date date = selectedCalendar.getTime();

    lblSummaryCourt.setText(
        "COURT: " + cmbCourt.getSelectedItem()
    );

    loadTimeSlots(date);
}
    });
}

private String selectedStartTime = null;
private String selectedEndTime = null;
private int selectedSlotCount = 0;

private final java.util.Set<String> bookedTimes =
        new java.util.HashSet<>();

private final java.util.Set<String> selectedTimes =
        new java.util.HashSet<>();

private void loadTimeSlots(java.util.Date selectedDate) {
    timeSlotPanel.removeAll();
    timeSlotPanel.setLayout(
        new java.awt.GridLayout(0, 1, 8, 8)
    );

    String[] startTimes = {
        "08:00:00", "09:00:00", "10:00:00",
        "11:00:00", "12:00:00", "13:00:00",
        "14:00:00", "15:00:00", "16:00:00",
        "17:00:00"
    };

    String[] endTimes = {
        "09:00:00", "10:00:00", "11:00:00",
        "12:00:00", "13:00:00", "14:00:00",
        "15:00:00", "16:00:00", "17:00:00",
        "18:00:00"
    };

    bookedTimes.clear();

    String courtName = (String) cmbCourt.getSelectedItem();
    if (courtName == null) {
        timeSlotPanel.revalidate();
        timeSlotPanel.repaint();
        return;
    }

    java.time.LocalDate localDate = selectedDate.toInstant()
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate();

    java.sql.Date sqlDate = java.sql.Date.valueOf(localDate);

    String sql = "SELECT start_time, end_time FROM tbl_bookings "
        + "WHERE court_name = ? AND booking_date = ? "
        + "AND status IN ('Pending', 'Confirmed')";

    try (java.sql.Connection conn = DBConnection.connect()) {
        if (conn == null) {
            return;
        }

        try (java.sql.PreparedStatement pst =
                    conn.prepareStatement(sql)) {
            pst.setString(1, courtName);
            pst.setDate(2, sqlDate);

            try (java.sql.ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    java.time.LocalTime bookedStart =
                        rs.getTime("start_time").toLocalTime();
                    java.time.LocalTime bookedEnd =
                        rs.getTime("end_time").toLocalTime();

                    for (int i = 0; i < startTimes.length; i++) {
                        java.time.LocalTime slotStart =
                            java.time.LocalTime.parse(startTimes[i]);
                        java.time.LocalTime slotEnd =
                            java.time.LocalTime.parse(endTimes[i]);

                        if (slotStart.isBefore(bookedEnd)
                                && slotEnd.isAfter(bookedStart)) {
                            bookedTimes.add(
                                startTimes[i] + "-" + endTimes[i]
                            );
                        }
                    }
                }
            }
        }
    } catch (java.sql.SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Database error: " + e.getMessage()
        );
        return;
    }

    for (int i = 0; i < startTimes.length; i++) {
        final String start = startTimes[i];
        final String end = endTimes[i];
        final int index = i;

        javax.swing.JButton button = new javax.swing.JButton();

        boolean isBooked = bookedTimes.contains(start + "-" + end);

        button.setText(
            formatTime(start) + " - " + formatTime(end)
            + (isBooked ? " (BOOKED)" : " (AVAILABLE)")
        );
        button.setEnabled(!isBooked);

        if (selectedTimes.contains(start + "-" + end)) {
            button.setText(
                formatTime(start) + " - " + formatTime(end)
                + " (SELECTED)"
            );
        }

        button.addActionListener(e -> {
            if (selectedStartTime == null) {
                selectedStartTime = start;
                selectedEndTime = end;
                selectedSlotCount = 1;
                selectedTimes.clear();
                selectedTimes.add(start + "-" + end);
            } else {
                int firstIndex = -1;
                int lastIndex = -1;

                for (int j = 0; j < startTimes.length; j++) {
                    if (startTimes[j].equals(selectedStartTime)) {
                        firstIndex = j;
                    }
                    if (startTimes[j].equals(selectedEndTime)) {
                        lastIndex = j;
                    }
                }

                if (index == firstIndex - 1) {
                    selectedStartTime = start;
                    selectedSlotCount++;
                    selectedTimes.add(start + "-" + end);
                } else if (index == lastIndex) {
                    selectedEndTime = end;
                    selectedSlotCount++;
                    selectedTimes.add(start + "-" + end);
                } else {
                    selectedStartTime = start;
                    selectedEndTime = end;
                    selectedSlotCount = 1;
                    selectedTimes.clear();
                    selectedTimes.add(start + "-" + end);
                }
            }

            updateWalkInSummary(selectedDate);
            loadTimeSlots(selectedDate);
        });

        timeSlotPanel.add(button);
    }

    timeSlotPanel.revalidate();
    timeSlotPanel.repaint();
}

private String formatTime(String time) {
    java.time.LocalTime value = java.time.LocalTime.parse(time);
    return value.format(
        java.time.format.DateTimeFormatter.ofPattern("h:mm a")
    );
}


private int getRatePerHour() {
    String courtName = (String) cmbCourt.getSelectedItem();

    if (courtName == null) {
        return 0;
    }

    switch (courtName) {
        case "Court Athena":
        case "Court Apollo":
            return 500;

        case "Court Atlas":
        case "Court Aurora":
            return 300;

        default:
            return 0;
    }
}

private void updateWalkInSummary(java.util.Date selectedDate) {
    String courtName = (String) cmbCourt.getSelectedItem();

    lblSummaryCourt.setText("COURT: " + courtName);

    lblSummaryDate.setText(
        "DATE: " + new java.text.SimpleDateFormat("MMMM dd, yyyy")
            .format(selectedDate)
    );

    if (selectedStartTime != null && selectedEndTime != null) {
        lblSummaryTime.setText(
            "TIME: " + formatTime(selectedStartTime)
            + " - " + formatTime(selectedEndTime)
        );

        lblSummaryDuration.setText(
            "DURATION: " + selectedSlotCount + " hour(s)"
        );

        int ratePerHour = getRatePerHour();
        int totalAmount = selectedSlotCount * ratePerHour;

        lblSummaryPrice.setText("TOTAL: ₱" + totalAmount);
    }
}

private void resetWalkInSelection() {
    selectedStartTime = null;
    selectedEndTime = null;
    selectedSlotCount = 0;
    selectedTimes.clear();

    lblSummaryTime.setText("TIME: -");
    lblSummaryDuration.setText("DURATION: -");
    lblSummaryPrice.setText("TOTAL: -");
}




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jButton15 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblCustomerName = new javax.swing.JLabel();
        txtCustomerName = new javax.swing.JTextField();
        lblCourt = new javax.swing.JLabel();
        cmbCourt = new javax.swing.JComboBox<>();
        calendarWalkIn = new com.toedter.calendar.JCalendar();
        timeSlotPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblSummaryCourt = new javax.swing.JLabel();
        lblSummaryDate = new javax.swing.JLabel();
        lblSummaryTime = new javax.swing.JLabel();
        lblSummaryDuration = new javax.swing.JLabel();
        lblSummaryPrice = new javax.swing.JLabel();
        btnConfirm = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 102, 102));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setPreferredSize(new java.awt.Dimension(220, 780));

        jButton7.setBackground(new java.awt.Color(255, 51, 51));
        jButton7.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton7.setText("LOGOUT");
        jButton7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.addActionListener(this::jButton7ActionPerformed);

        jButton9.setBackground(new java.awt.Color(204, 255, 255));
        jButton9.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton9.setText("DASHBOARD");
        jButton9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton9.addActionListener(this::jButton9ActionPerformed);

        jButton10.setBackground(new java.awt.Color(204, 255, 255));
        jButton10.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton10.setText("COURTS");
        jButton10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton10.addActionListener(this::jButton10ActionPerformed);

        jButton11.setBackground(new java.awt.Color(204, 255, 255));
        jButton11.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton11.setText("BOOKINGS");
        jButton11.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton11.addActionListener(this::jButton11ActionPerformed);

        jButton12.setBackground(new java.awt.Color(204, 255, 255));
        jButton12.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton12.setText("USERS");
        jButton12.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton12.addActionListener(this::jButton12ActionPerformed);

        jButton13.setBackground(new java.awt.Color(204, 255, 255));
        jButton13.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton13.setText("  SCHEDULES");
        jButton13.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton13.addActionListener(this::jButton13ActionPerformed);

        jButton14.setBackground(new java.awt.Color(204, 255, 255));
        jButton14.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        jButton14.setText("REPORTS");
        jButton14.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton14.addActionListener(this::jButton14ActionPerformed);

        jLabel8.setBackground(new java.awt.Color(9, 223, 149));
        jLabel8.setFont(new java.awt.Font("Phosphate", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(9, 223, 149));
        jLabel8.setText("DINKSPHERE");

        jLabel28.setFont(new java.awt.Font("Helvetica Neue", 0, 12)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("Play ◾ Connect ◾ Belong");

        jButton15.setBackground(new java.awt.Color(204, 255, 255));
        jButton15.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jButton15.setText("PENDINGS");
        jButton15.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton15.addActionListener(this::jButton15ActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel8))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addGap(28, 28, 28)
                                    .addComponent(jLabel28))
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 19, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63))
        );

        jPanel3.setBackground(new java.awt.Color(204, 255, 255));

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 36)); // NOI18N
        jLabel1.setText("WALK-IN BOOKING");

        jLabel2.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        jLabel2.setText("Create a booking for walk-in customers and record cash payment.");

        lblCustomerName.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblCustomerName.setText("Customer Name:");

        lblCourt.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblCourt.setText("Select Court:");

        cmbCourt.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Court Athena", "Court Apollo", "Court Atlas", "Court Aurora" }));
        cmbCourt.addActionListener(this::cmbCourtActionPerformed);

        calendarWalkIn.setPreferredSize(new java.awt.Dimension(350, 280));

        timeSlotPanel.setBackground(new java.awt.Color(204, 255, 255));

        javax.swing.GroupLayout timeSlotPanelLayout = new javax.swing.GroupLayout(timeSlotPanel);
        timeSlotPanel.setLayout(timeSlotPanelLayout);
        timeSlotPanelLayout.setHorizontalGroup(
            timeSlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 229, Short.MAX_VALUE)
        );
        timeSlotPanelLayout.setVerticalGroup(
            timeSlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jLabel3.setText("BOOKING SUMMARY:");

        lblSummaryCourt.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblSummaryCourt.setText("COURT:  ");

        lblSummaryDate.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblSummaryDate.setText("DATE: ");

        lblSummaryTime.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblSummaryTime.setText("TIME:");

        lblSummaryDuration.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblSummaryDuration.setText("DURATION:");

        lblSummaryPrice.setFont(new java.awt.Font("Helvetica Neue", 1, 14)); // NOI18N
        lblSummaryPrice.setText("TOTAL: ₱");

        btnConfirm.setBackground(new java.awt.Color(9, 223, 149));
        btnConfirm.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        btnConfirm.setText("CONFIRM BOOKING");
        btnConfirm.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        btnConfirm.addActionListener(this::btnConfirmActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(233, 233, 233))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(197, 197, 197))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(calendarWalkIn, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblSummaryDate)
                                    .addComponent(lblSummaryTime)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(lblCustomerName)
                                            .addGap(49, 49, 49)
                                            .addComponent(txtCustomerName))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(lblCourt)
                                            .addGap(75, 75, 75)
                                            .addComponent(cmbCourt, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(39, 39, 39)
                                .addComponent(timeSlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblSummaryCourt)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(lblSummaryDuration)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(144, 144, 144))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(151, 151, 151)
                        .addComponent(lblSummaryPrice)))
                .addContainerGap(117, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel1)
                .addGap(6, 6, 6)
                .addComponent(jLabel2)
                .addGap(36, 36, 36)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(lblCustomerName))
                            .addComponent(txtCustomerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(lblCourt))
                            .addComponent(cmbCourt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addComponent(calendarWalkIn, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timeSlotPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(lblSummaryCourt)
                .addGap(18, 18, 18)
                .addComponent(lblSummaryDate, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblSummaryTime)
                .addGap(19, 19, 19)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSummaryDuration))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSummaryPrice)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(111, 111, 111)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(68, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Confirm Logout",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            Session.userId = 0;
            Session.username = null;
            Session.fullname = null;

            new LoginFrame().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        this.dispose();
        new AdminDashboard().setVisible(true);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        new CourtsManagementFrame().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        new BookingsManagementFrame().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        new UsersManagementFrame().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        new ScheduleFrame().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed

        new ReportsFrame().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        new PendingPaymentsFrame().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton15ActionPerformed

    private void btnConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmActionPerformed
       
    String customerName = txtCustomerName.getText().trim();
    
    if (customerName.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Please enter the customer's name."
        );
        txtCustomerName.requestFocus();
        return;
    }

    java.util.Date selectedDate = calendarWalkIn.getDate();

    if (selectedDate == null) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Please select a booking date."
        );
        return;
    }

    java.time.LocalDate bookingDate = selectedDate.toInstant()
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate();

    if (bookingDate.isBefore(java.time.LocalDate.now())) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Past dates cannot be booked."
        );
        return;
    }

    String courtName = (String) cmbCourt.getSelectedItem();

    if (courtName == null || selectedStartTime == null
            || selectedEndTime == null || selectedSlotCount < 1) {
        javax.swing.JOptionPane.showMessageDialog(
            this, "Please select an available time slot."
        );
        return;
    }

    int ratePerHour = getRatePerHour();
    int totalAmount = selectedSlotCount * ratePerHour;

    int confirm = javax.swing.JOptionPane.showConfirmDialog(
        this,
        "Customer: " + customerName
        + "\nCourt: " + courtName
        + "\nDate: " + bookingDate
        + "\nTime: " + formatTime(selectedStartTime)
        + " - " + formatTime(selectedEndTime)
        + "\nTotal: ₱" + totalAmount
        + "\n\nConfirm that cash has been received?",
        "Confirm Cash Payment",
        javax.swing.JOptionPane.YES_NO_OPTION
    );

    if (confirm != javax.swing.JOptionPane.YES_OPTION) {
        return;
    }

    String checkSql =
        "SELECT COUNT(*) FROM tbl_bookings "
        + "WHERE court_name = ? AND booking_date = ? "
        + "AND status IN ('Pending', 'Confirmed') "
        + "AND start_time < ? AND end_time > ?";

    String insertSql =
        "INSERT INTO tbl_bookings "
        + "(user_id, customer_name, court_name, booking_date, "
        + "start_time, end_time, status, payment_method, payment_status) "
        + "VALUES (NULL, ?, ?, ?, ?, ?, 'Confirmed', 'Cash', 'Paid')";

    try (java.sql.Connection conn = DBConnection.connect()) {
        if (conn == null) {
            javax.swing.JOptionPane.showMessageDialog(
                this, "Could not connect to the database."
            );
            return;
        }

        conn.setAutoCommit(false);

        try {
            // Lock overlapping records while checking availability.
            String lockSql =
                "SELECT id FROM tbl_bookings "
                + "WHERE court_name = ? AND booking_date = ? "
                + "AND status IN ('Pending', 'Confirmed') "
                + "AND start_time < ? AND end_time > ? FOR UPDATE";

            try (java.sql.PreparedStatement check =
                    conn.prepareStatement(lockSql)) {
                check.setString(1, courtName);
                check.setDate(2, java.sql.Date.valueOf(bookingDate));
                check.setTime(3, java.sql.Time.valueOf(selectedEndTime));
                check.setTime(4, java.sql.Time.valueOf(selectedStartTime));

                try (java.sql.ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        javax.swing.JOptionPane.showMessageDialog(
                            this,
                            "This time slot is no longer available. Please select another."
                        );
                        resetWalkInSelection();
                        loadTimeSlots(selectedDate);
                        return;
                    }
                }
            }

            try (java.sql.PreparedStatement pst =
                    conn.prepareStatement(insertSql)) {
                pst.setString(1, customerName);
                pst.setString(2, courtName);
                pst.setDate(3, java.sql.Date.valueOf(bookingDate));
                pst.setTime(4, java.sql.Time.valueOf(selectedStartTime));
                pst.setTime(5, java.sql.Time.valueOf(selectedEndTime));

                pst.executeUpdate();
            }

            conn.commit();

            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Walk-in booking saved successfully!\n"
                + "Customer: " + customerName
                + "\nTotal paid: ₱" + totalAmount,
                "Booking Successful",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
            );

            resetWalkInSelection();
            txtCustomerName.setText("");
            loadTimeSlots(selectedDate);

        } catch (java.sql.SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }

    } catch (java.sql.SQLException e) {
        javax.swing.JOptionPane.showMessageDialog(
            this,
            "Could not save walk-in booking: " + e.getMessage(),
            "Database Error",
            javax.swing.JOptionPane.ERROR_MESSAGE
        );
    }

    }//GEN-LAST:event_btnConfirmActionPerformed

    private void cmbCourtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCourtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCourtActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new WalkInBookingFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConfirm;
    private com.toedter.calendar.JCalendar calendarWalkIn;
    private javax.swing.JComboBox<String> cmbCourt;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblCourt;
    private javax.swing.JLabel lblCustomerName;
    private javax.swing.JLabel lblSummaryCourt;
    private javax.swing.JLabel lblSummaryDate;
    private javax.swing.JLabel lblSummaryDuration;
    private javax.swing.JLabel lblSummaryPrice;
    private javax.swing.JLabel lblSummaryTime;
    private javax.swing.JPanel timeSlotPanel;
    private javax.swing.JTextField txtCustomerName;
    // End of variables declaration//GEN-END:variables
}
