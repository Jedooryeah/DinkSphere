/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package dinksphere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;




/**
 *
 * @author juliepearledoria
 */
public class CourtAuroraBookingFrame extends javax.swing.JFrame {
    
    private String selectedStartTime = null;
    private String selectedEndTime = null;
    private int selectedSlotCount = 0;
    private java.util.Date selectedDate = null;
    
    private java.util.Set<String> bookedTimes = new java.util.HashSet<>();
    
    private java.util.Set<String> selectedTimes =
        new java.util.HashSet<>();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CourtAuroraBookingFrame.class.getName());

    /**
     * Creates new form CourtAthenaBookingFrame
     */
    

public CourtAuroraBookingFrame() {
    initComponents();

    setSize(1200, 780);
    setResizable(false);
    setLocationRelativeTo(null);

    calendarAurora.setMinSelectableDate(
        new java.util.Date()
    );

    selectedDate = calendarAurora.getDate();
    loadTimeSlots(selectedDate);

    calendarAurora.getDayChooser()
        .addPropertyChangeListener("day", evt -> {
            if ("day".equals(evt.getPropertyName())) {
                Calendar selectedCalendar =
                    calendarAurora.getCalendar();

                if (selectedCalendar != null) {
                    selectedDate = selectedCalendar.getTime();
                    loadTimeSlots(selectedDate);
                }
            }
        });


    calendarAurora.getDayChooser()
        .addPropertyChangeListener("day", evt -> {

    if ("day".equals(evt.getPropertyName())) {

        Calendar selectedCalendar =
                calendarAurora.getCalendar();

        if (selectedCalendar != null) {

            selectedDate = selectedCalendar.getTime();

            loadTimeSlots(selectedDate);
        }
    }
});
}

private void loadTimeSlots(java.util.Date selectedDate) {

    timeSlotPanel.removeAll();

    timeSlotPanel.setLayout(
            new java.awt.GridLayout(0, 1, 8, 8)
    );

    String[] startTimes = {
        "08:00:00",
        "09:00:00",
        "10:00:00",
        "11:00:00",
        "12:00:00",
        "13:00:00",
        "14:00:00",
        "15:00:00",
        "16:00:00",
        "17:00:00"
    };

    String[] endTimes = {
        "09:00:00",
        "10:00:00",
        "11:00:00",
        "12:00:00",
        "13:00:00",
        "14:00:00",
        "15:00:00",
        "16:00:00",
        "17:00:00",
        "18:00:00"
    };

    
bookedTimes.clear();

java.time.LocalDate localDate =
        selectedDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

java.sql.Date sqlDate =
        java.sql.Date.valueOf(localDate);

String sql = "SELECT start_time, end_time FROM tbl_bookings "
        + "WHERE court_name = ? "
        + "AND booking_date = ? "
        + "AND status IN ('Pending', 'Confirmed')";

Connection conn = null;

try {
    conn = DBConnection.connect();

    if (conn == null) {
        javax.swing.JOptionPane.showMessageDialog(
                this,
                "Database connection failed."
        );
        return;
    }

    try (PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, "Court Aurora");
        pst.setDate(2, sqlDate);

        try (ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                java.time.LocalTime bookedStart =
                        rs.getTime("start_time").toLocalTime();

                java.time.LocalTime bookedEnd =
                        rs.getTime("end_time").toLocalTime();

                for (int j = 0; j < startTimes.length; j++) {
                    java.time.LocalTime slotStart =
                            java.time.LocalTime.parse(startTimes[j]);

                    java.time.LocalTime slotEnd =
                            java.time.LocalTime.parse(endTimes[j]);

                    if (slotStart.isBefore(bookedEnd)
                            && slotEnd.isAfter(bookedStart)) {
                        bookedTimes.add(
                                startTimes[j] + "-" + endTimes[j]
                        );
                    }
                }
            }
        }
    }

} catch (java.sql.SQLException e) {
    javax.swing.JOptionPane.showMessageDialog(
            this,
            "Database error: " + e.getMessage(),
            "Error",
            javax.swing.JOptionPane.ERROR_MESSAGE
    );
    e.printStackTrace();
    return;

} finally {
    if (conn != null) {
        try {
            conn.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}


    for (int i = 0; i < startTimes.length; i++) {

        String start = startTimes[i];
        String end = endTimes[i];

        javax.swing.JButton button =
                new javax.swing.JButton();

        if (bookedTimes.contains(start + "-" + end)) {

            button.setText(
                    formatTime(start)
                    + " - "
                    + formatTime(end)
                    + "  (BOOKED)"
            );

            button.setEnabled(false);

        } else {

            button.setText(
                    formatTime(start)
                    + " - "
                    + formatTime(end)
                    + "  (AVAILABLE)"
            );

    button.addActionListener(e -> {

    // First selection
    // First selection
    if (selectedStartTime == null) {

        selectedTimes.clear();

        selectedStartTime = start;
        selectedEndTime = end;
        selectedSlotCount = 1;

        selectedTimes.add(start + "-" + end);

    } else {

        int currentStartIndex = -1;
        int currentEndIndex = -1;
        int clickedIndex = -1;

        for (int j = 0; j < startTimes.length; j++) {

        if (startTimes[j].equals(selectedStartTime)) {
            currentStartIndex = j;
        }

        if (startTimes[j].equals(selectedEndTime)) {
            currentEndIndex = j;
        }

    if (startTimes[j].equals(start)) {
        clickedIndex = j;
    }
}

        // Select one slot immediately BEFORE the current selection
        // Select one slot immediately BEFORE the current selection
        if (clickedIndex == currentStartIndex - 1) {

            selectedStartTime = start;
            selectedSlotCount++;

            selectedTimes.add(start + "-" + end);
        }

        // Select one slot immediately AFTER the current selection
        else if (clickedIndex == currentEndIndex) {

            selectedEndTime = end;
            selectedSlotCount++;

            selectedTimes.add(start + "-" + end);
    }

        // Clicked inside the current selection
        else if (clickedIndex >= currentStartIndex
                && clickedIndex < currentEndIndex) {

            return;

        }

        // Otherwise start a new selection
        // Otherwise start a new selection
        else {

            selectedTimes.clear();

            selectedStartTime = start;
            selectedEndTime = end;
            selectedSlotCount = 1;

            selectedTimes.add(start + "-" + end);
}
    }

    // Update summary
    java.text.SimpleDateFormat dateFormat =
            new java.text.SimpleDateFormat("MMMM dd, yyyy");

    String selectedDateText =
            dateFormat.format(selectedDate);

    String selectedTime =
            formatTime(selectedStartTime)
            + " - "
            + formatTime(selectedEndTime);

    int totalPrice =
            selectedSlotCount * 300;

    lblSummaryCourt.setText(
            "Court: Court Aurora"
    );

    lblSummaryDate.setText(
            "Date: " + selectedDateText
    );

    lblSummaryTime.setText(
            "Time: " + selectedTime
    );

    lblSummaryPrice.setText(
            "Total: ₱" + totalPrice
    );
    
    loadTimeSlots(selectedDate);
});
        }

        String slotKey = start + "-" + end;

if (selectedTimes.contains(slotKey)) {

    button.setText(
            formatTime(start)
            + " - "
            + formatTime(end)
            + "  (SELECTED)"
    );

    button.setFont(
            button.getFont().deriveFont(
                    java.awt.Font.BOLD
            )
    );
}

timeSlotPanel.add(button);;
    }

    timeSlotPanel.revalidate();
    timeSlotPanel.repaint();
}

private String formatTime(String time) {

    try {

        SimpleDateFormat input =
                new SimpleDateFormat("HH:mm:ss");

        SimpleDateFormat output =
                new SimpleDateFormat("h:mm a");

        return output.format(input.parse(time));

    } catch (java.text.ParseException e) {

        return time;
    }
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
        calendarAurora = new com.toedter.calendar.JCalendar();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        timeSlotPanel = new javax.swing.JPanel();
        bookingSummaryPanel = new javax.swing.JPanel();
        lblBookingSummary = new javax.swing.JLabel();
        lblSummaryCourt = new javax.swing.JLabel();
        lblSummaryDate = new javax.swing.JLabel();
        lblSummaryTime = new javax.swing.JLabel();
        lblSummaryPrice = new javax.swing.JLabel();
        btnConfirmBooking = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 153, 153));

        jLabel1.setFont(new java.awt.Font("Phosphate", 1, 36)); // NOI18N
        jLabel1.setText("BOOK COURT Aurora");

        jLabel2.setFont(new java.awt.Font("PT Mono", 0, 30)); // NOI18N
        jLabel2.setText("₱300 / hour");

        timeSlotPanel.setOpaque(false);

        javax.swing.GroupLayout timeSlotPanelLayout = new javax.swing.GroupLayout(timeSlotPanel);
        timeSlotPanel.setLayout(timeSlotPanelLayout);
        timeSlotPanelLayout.setHorizontalGroup(
            timeSlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 360, Short.MAX_VALUE)
        );
        timeSlotPanelLayout.setVerticalGroup(
            timeSlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        bookingSummaryPanel.setBackground(new java.awt.Color(204, 204, 204));
        bookingSummaryPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        bookingSummaryPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblBookingSummary.setFont(new java.awt.Font("ITF Devanagari", 1, 18)); // NOI18N
        lblBookingSummary.setText("BOOKING SUMMARY:");
        bookingSummaryPanel.add(lblBookingSummary, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 7, -1, -1));

        lblSummaryCourt.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lblSummaryCourt.setText("Court: Court Aurora");
        bookingSummaryPanel.add(lblSummaryCourt, new org.netbeans.lib.awtextra.AbsoluteConstraints(71, 53, -1, -1));

        lblSummaryDate.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lblSummaryDate.setText("Date: - ");
        bookingSummaryPanel.add(lblSummaryDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(71, 79, -1, -1));

        lblSummaryTime.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lblSummaryTime.setText("Time: -");
        bookingSummaryPanel.add(lblSummaryTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(71, 105, -1, -1));

        lblSummaryPrice.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        lblSummaryPrice.setText("Total: - ");
        bookingSummaryPanel.add(lblSummaryPrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(71, 143, -1, -1));

        btnConfirmBooking.setBackground(new java.awt.Color(9, 223, 149));
        btnConfirmBooking.setText("Confirm Booking");
        btnConfirmBooking.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        btnConfirmBooking.addActionListener(this::btnConfirmBookingActionPerformed);
        bookingSummaryPanel.add(btnConfirmBooking, new org.netbeans.lib.awtextra.AbsoluteConstraints(326, 90, 126, 35));

        jButton1.setBackground(new java.awt.Color(9, 223, 149));
        jButton1.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        jButton1.setText("Back");
        jButton1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        jLabel17.setBackground(new java.awt.Color(0, 153, 153));
        jLabel17.setFont(new java.awt.Font("Phosphate", 0, 36)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(9, 223, 149));
        jLabel17.setText("DINKSPHERE");

        jLabel28.setFont(new java.awt.Font("Helvetica Neue", 0, 12)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("Play ◾ Connect ◾ Belong");

        jButton2.setBackground(new java.awt.Color(204, 255, 255));
        jButton2.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jButton2.setText("HOME");
        jButton2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setBackground(new java.awt.Color(204, 255, 255));
        jButton3.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jButton3.setText("COURTS");
        jButton3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setBackground(new java.awt.Color(204, 255, 255));
        jButton4.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jButton4.setText("MY SCHEDULE");
        jButton4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setBackground(new java.awt.Color(204, 255, 255));
        jButton5.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jButton5.setText("ABOUT US");
        jButton5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton6.setBackground(new java.awt.Color(255, 51, 51));
        jButton6.setFont(new java.awt.Font("Helvetica Neue", 1, 13)); // NOI18N
        jButton6.setText("LOGOUT");
        jButton6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white, java.awt.Color.white));
        jButton6.addActionListener(this::jButton6ActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel17))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 15, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addGap(108, 108, 108)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 125, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(355, 355, 355)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(calendarAurora, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bookingSummaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 527, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(timeSlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(435, 435, 435)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(305, 305, 305)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addComponent(jLabel2)
                .addGap(25, 25, 25)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(calendarAurora, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(39, 39, 39)
                        .addComponent(bookingSummaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timeSlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnConfirmBookingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmBookingActionPerformed
    
if (calendarAurora.getDate() == null) {
    javax.swing.JOptionPane.showMessageDialog(
        this,
        "Please select a booking date first.",
        "Date Required",
        javax.swing.JOptionPane.WARNING_MESSAGE
    );
    return;
}

if (selectedStartTime == null || selectedEndTime == null) {
    javax.swing.JOptionPane.showMessageDialog(
        this,
        "Please select an available time slot first.",
        "Time Required",
        javax.swing.JOptionPane.WARNING_MESSAGE
    );
    return;
}

        
        java.util.Date selectedDate = calendarAurora.getDate();

if (selectedDate == null) {
    javax.swing.JOptionPane.showMessageDialog(
        this,
        "Please select a booking date."
    );
    return;
}

java.time.LocalDate bookingDate = selectedDate.toInstant()
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate();

if (bookingDate.isBefore(java.time.LocalDate.now())) {
    javax.swing.JOptionPane.showMessageDialog(
        this,
        "You cannot book a past date. Please select today or a future date."
    );
    return;
}    
    
        
        new PaymentFrame(
            selectedDate,
            selectedStartTime,
            selectedEndTime,
            selectedSlotCount,
            "Court Aurora",
                300
        ).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnConfirmBookingActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    BookCourtFrame bookCourt = new BookCourtFrame();
    bookCourt.setVisible(true);
    this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    HomeFrame home = new HomeFrame();
    home.setVisible(true);
    this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
    CourtselectionFrame courtSelection = new CourtselectionFrame();
    courtSelection.setVisible(true);
    this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    MyBookingsFrame myBookings = new MyBookingsFrame();
    myBookings.setVisible(true);
    this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
    AboutusFrame about = new AboutusFrame();
    about.setVisible(true);
    this.dispose();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
    
int confirm = javax.swing.JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to log out?",
        "Confirm Logout",
        javax.swing.JOptionPane.YES_NO_OPTION,
        javax.swing.JOptionPane.QUESTION_MESSAGE
);

if (confirm == javax.swing.JOptionPane.YES_OPTION) {
    Session.userId = 0;
    Session.username = null;
    Session.fullname = null;

    new LoginFrame().setVisible(true);
    this.dispose();
}
    }//GEN-LAST:event_jButton6ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new CourtAuroraBookingFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bookingSummaryPanel;
    private javax.swing.JButton btnConfirmBooking;
    private com.toedter.calendar.JCalendar calendarAurora;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblBookingSummary;
    private javax.swing.JLabel lblSummaryCourt;
    private javax.swing.JLabel lblSummaryDate;
    private javax.swing.JLabel lblSummaryPrice;
    private javax.swing.JLabel lblSummaryTime;
    private javax.swing.JPanel timeSlotPanel;
    // End of variables declaration//GEN-END:variables
}
