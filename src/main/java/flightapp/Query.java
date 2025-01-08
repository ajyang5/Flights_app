package flightapp;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.*;
/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  //
  // Canned queries
  //
  private static final String FLIGHT_CAPACITY_SQL = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement flightCapacityStmt;

  private static final String RESERVATION_CLEAR_SQL = "DELETE FROM Reservation_ajyang5";
  private PreparedStatement clearReservationsStmt;

  private static final String USERS_CLEAR_SQL = "DELETE FROM Users_ajyang5";
  private PreparedStatement clearUsersStmt;


  private static final String USERS_CREATE_SQL = "INSERT INTO Users_ajyang5 VALUES (?, ?, ?)";
  private PreparedStatement createUserStmt;

  private static final String USERS_COUNTNAME_SQL = "SELECT Count(*) AS Count FROM Users_ajyang5 WHERE username = ?";
  private PreparedStatement countUserStmt;

  private static final String USERS_PASSWORD_SQL = "SELECT password FROM Users_ajyang5 WHERE username = ?";
  private PreparedStatement userPasswordStmt;

  private static final String DIRECT_FLIGHTS_SQL = "SELECT TOP ( ? ) fid, day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price "
                                                  + "FROM Flights WHERE canceled = 0 AND origin_city = ? AND dest_city = ? AND day_of_month =  ? "
                                                  + "ORDER BY actual_time ASC, fid ASC";
  private PreparedStatement directFlightsStmt;


  private static final String INDIRECT_FLIGHTS_SQL = "SELECT TOP ( ? ) " +
                                                  "F1.flight_num AS f1_flight_num, F1. day_of_month AS day_of_month," +  
                                                  "F1.carrier_id AS f1_carrier_id, F1.flight_num AS f1_flight_num," +  
                                                  "F1.origin_city AS f1_origin_city, F1.dest_city AS f1_dest_city," + 
                                                  "F1.actual_time AS f1_actual_time, F1.capacity AS f1_capacity," +  
                                                  "F1.price AS f1_price, F1.fid AS f1_fid," + 
                                                  "F2.flight_num AS f2_flight_num," + 
                                                  "F2.carrier_id AS f2_carrier_id, F2.flight_num AS f2_flight_num," +  
                                                  "F2.origin_city AS f2_origin_city, F2.dest_city AS f2_dest_city," +  
                                                  "F2.actual_time AS f2_actual_time, F2.capacity AS f2_capacity," +  
                                                  "F2.price AS f2_price, F2.fid AS f2_fid " + 
                                                  "FROM FLIGHTS F1, FLIGHTS F2 " + 
                                                  "WHERE F1.canceled = 0 AND F2.canceled = 0 AND F1.origin_city = ? " + 
                                                  "AND F1.dest_city = F2.origin_city " + 
                                                  "AND F2.dest_city = ? " + 
                                                  "AND F1.day_of_month = F2.day_of_month " + 
                                                  "AND F1.day_of_month = ? " + 
                                                  "ORDER BY (F1.actual_time + F2.actual_time) ASC, F1.fid ASC, F2.fid ASC";
  private PreparedStatement indirectFlightsStmt;

  private static final String RESERVATION_COUNTDATE_SQL = "SELECT Count(*) AS Count " +
                                                          "FROM Reservation_ajyang5 R, FLIGHTS F " + 
                                                          "WHERE R.fid1 = F.fid AND F.day_of_month = ? AND R.username = ?";
  private PreparedStatement countUserReservationDateStmt;

  private static final String RESERVATION_COUNTCAPACITY1_SQL = "SELECT Count(*) AS Count " +
                                                          "FROM Reservation_ajyang5 R " + 
                                                          "WHERE R.fid1 = ?";
  private PreparedStatement countReservationCapacity1Stmt;

  private static final String RESERVATION_COUNTCAPACITY2_SQL = "SELECT Count(*) AS Count " +
                                                          "FROM Reservation_ajyang5 R " + 
                                                          "WHERE R.fid2 = ?";
  private PreparedStatement countReservationCapacity2Stmt;

  private static final String RESERVATION_COUNT_SQL = "SELECT Count(*) AS Count " +
                                                          "FROM Reservation_ajyang5 R ";
  private PreparedStatement countReservationStmt;

  private static final String RESERVATION_ADD_SQL = "INSERT INTO Reservation_ajyang5 VALUES (?, 0, ?, ?, ?)";
  private PreparedStatement addReservationStmt;

  private static final String RESERVATION_SEARCH_SQL = "SELECT R.isPaid, R.username, (F1.price + COALESCE(F2.price, 0)) AS price " +
                                                        "FROM Reservation_ajyang5 AS R JOIN FLIGHTS AS F1 ON R.fid1 = F1.fid " +
                                                        "LEFT OUTER JOIN FLIGHTS AS F2 ON R.fid2 = F2.fid " + 
                                                        "WHERE R.rid = ? ";
  private PreparedStatement searchReservationStmt;

  private static final String USERS_BALANCE_SQL = "SELECT balance FROM Users_ajyang5 WHERE username = ?";
  private PreparedStatement userBalanceStmt;

  private static final String USERS_UPDATEBALANCE_SQL = "UPDATE Users_ajyang5 SET balance = ? WHERE username = ?";
  private PreparedStatement userUpdateBalanceStmt;

  private static final String RESERVATION_UPDATEPAID_SQL = "UPDATE Reservation_ajyang5 SET isPaid = 1 WHERE rid = ?";
  private PreparedStatement reservationUpdatePaidStmt;

  private static final String RESERVATION_INDIRECTFLIGHTS_SQL = "SELECT R.rid, R.isPaid, " + 
                                                  "F1.flight_num AS f1_flight_num, F1. day_of_month AS day_of_month, " +  
                                                  "F1.carrier_id AS f1_carrier_id, F1.flight_num AS f1_flight_num," +  
                                                  "F1.origin_city AS f1_origin_city, F1.dest_city AS f1_dest_city," + 
                                                  "F1.actual_time AS f1_actual_time, F1.capacity AS f1_capacity," +  
                                                  "F1.price AS f1_price, F1.fid AS f1_fid," + 
                                                  "F2.flight_num AS f2_flight_num," + 
                                                  "F2.carrier_id AS f2_carrier_id, F2.flight_num AS f2_flight_num," +  
                                                  "F2.origin_city AS f2_origin_city, F2.dest_city AS f2_dest_city," +  
                                                  "F2.actual_time AS f2_actual_time, F2.capacity AS f2_capacity," +  
                                                  "F2.price AS f2_price, F2.fid AS f2_fid " + 
                                                  "FROM Reservation_ajyang5 AS R JOIN FLIGHTS AS F1 ON R.fid1 = F1.fid " + 
                                                  "JOIN FLIGHTS AS F2 ON R.fid2 = F2.fid " +
                                                  "WHERE R.fid2 IS NOT NULL AND R.username = ?";
  private PreparedStatement reservationIndirectFlightsStmt;  

  private static final String RESERVATION_DIRECTFLIGHTS_SQL = "SELECT R.rid, R.isPaid, " + 
                                                  "F1.flight_num AS f1_flight_num, F1. day_of_month AS day_of_month, " +  
                                                  "F1.carrier_id AS f1_carrier_id, F1.flight_num AS f1_flight_num," +  
                                                  "F1.origin_city AS f1_origin_city, F1.dest_city AS f1_dest_city," + 
                                                  "F1.actual_time AS f1_actual_time, F1.capacity AS f1_capacity," +  
                                                  "F1.price AS f1_price, F1.fid AS f1_fid " + 
                                                  "FROM Reservation_ajyang5 AS R JOIN FLIGHTS AS F1 ON R.fid1 = F1.fid " + 
                                                  "WHERE R.fid2 IS NULL AND R.username = ?";
  private PreparedStatement reservationDirectFlightsStmt;  

  //
  // Instance variables
  //
  private boolean isLoggedin = false;
  private String username;
  private List<Itinerary> search;

  protected Query() throws SQLException, IOException {
    prepareStatements();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      clearReservationsStmt.clearParameters();
      clearReservationsStmt.executeUpdate();

      clearUsersStmt.clearParameters();
      clearUsersStmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    flightCapacityStmt = conn.prepareStatement(FLIGHT_CAPACITY_SQL);
    clearUsersStmt = conn.prepareStatement(USERS_CLEAR_SQL);
    clearReservationsStmt = conn.prepareStatement(RESERVATION_CLEAR_SQL);
    createUserStmt = conn.prepareStatement(USERS_CREATE_SQL);
    countUserStmt = conn.prepareStatement(USERS_COUNTNAME_SQL);
    userPasswordStmt = conn.prepareStatement(USERS_PASSWORD_SQL);    
    directFlightsStmt = conn.prepareStatement(DIRECT_FLIGHTS_SQL);
    indirectFlightsStmt = conn.prepareStatement(INDIRECT_FLIGHTS_SQL);

    countUserReservationDateStmt = conn.prepareStatement(RESERVATION_COUNTDATE_SQL);
    countReservationCapacity1Stmt = conn.prepareStatement(RESERVATION_COUNTCAPACITY1_SQL);
    countReservationCapacity2Stmt = conn.prepareStatement(RESERVATION_COUNTCAPACITY2_SQL);
    addReservationStmt = conn.prepareStatement(RESERVATION_ADD_SQL);
    searchReservationStmt = conn.prepareStatement(RESERVATION_SEARCH_SQL);
    userBalanceStmt = conn.prepareStatement(USERS_BALANCE_SQL);
    userUpdateBalanceStmt = conn.prepareStatement(USERS_UPDATEBALANCE_SQL);    
    reservationUpdatePaidStmt = conn.prepareStatement(RESERVATION_UPDATEPAID_SQL);  
    countReservationStmt = conn.prepareStatement(RESERVATION_COUNT_SQL);    
    reservationIndirectFlightsStmt = conn.prepareStatement(RESERVATION_INDIRECTFLIGHTS_SQL);
    reservationDirectFlightsStmt = conn.prepareStatement(RESERVATION_DIRECTFLIGHTS_SQL);
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_login(String username, String password) {
    // Check if the user has already logged in
    if (isLoggedin) {
      return "User already logged in\n";
    }
    
    try {
      conn.setAutoCommit(false);
      // Check if the user does not exist
      countUserStmt.clearParameters();
      countUserStmt.setString(1, username);
      ResultSet count = countUserStmt.executeQuery();
      if (count.next()) {
        int c = count.getInt("Count");
        if (c == 0) {
          conn.rollback();
          conn.setAutoCommit(true);
          return "Login failed\n";
        }
      }       
      count.close(); 

      // Check if password is correct
      userPasswordStmt.clearParameters();
      userPasswordStmt.setString(1, username);
      ResultSet pwd = userPasswordStmt.executeQuery();

      if (pwd.next()) {
        byte[] hash = pwd.getBytes("password");

        if (PasswordUtils.plaintextMatchesSaltedHash(password, hash)) {
          isLoggedin = true;
          this.username = username;
          conn.commit();
          conn.setAutoCommit(true);
          return "Logged in as " + username +"\n";
        }
      } 
      pwd.close();

      conn.rollback(); // if wrong password
      conn.setAutoCommit(true);
    } catch (SQLException e) {
      try {
        conn.rollback();
        conn.setAutoCommit(true);
      } catch (SQLException se) {
        se.printStackTrace();
      }
      if (isDeadlock(e)) {
        return transaction_login(username, password);
      }
    }

    return "Login failed\n";
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    // Check if initial amount is negative
    if (initAmount < 0) {
      return "Failed to create user\n";
    }

    try {
      conn.setAutoCommit(false);
      // Check if user has already existed.
      countUserStmt.clearParameters();
      countUserStmt.setString(1, username);
      ResultSet count = countUserStmt.executeQuery();

      if (count.next() && count.getInt("Count") > 0) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "Failed to create user\n";
      }
      count.close();

      // create new user
      byte[] storedPassword = PasswordUtils.saltAndHashPassword(password);

      createUserStmt.clearParameters();
      createUserStmt.setString(1, username);
      createUserStmt.setBytes(2, storedPassword);
      createUserStmt.setInt(3, initAmount);
      createUserStmt.executeUpdate();

      conn.commit();
      conn.setAutoCommit(true);
      return "Created user " + username + "\n";
    } catch (SQLException e) {
      try {
          conn.rollback();
          conn.setAutoCommit(true);
      } catch (SQLException se) {
          se.printStackTrace();
      }
      if (isDeadlock(e)) {
          return transaction_createCustomer(username, password, initAmount);
      }
    }  
    return "Failed to create user\n";
  }


  /* See QueryAbstract.java for javadoc */
  public String transaction_search(String originCity, String destinationCity, 
                                   boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries) {
    // WARNING: the below code is insecure (it's susceptible to SQL injection attacks) AND only
    // handles searches for direct flights.  We are providing it *only* as an example of how
    // to use JDBC; you are required to replace it with your own secure implementation.
    //
    StringBuffer sb = new StringBuffer();
    List<Itinerary> itineraries = new ArrayList<>();

    try {
      // fill in the directed flights
      directFlightsStmt.clearParameters();
      directFlightsStmt.setInt(1, numberOfItineraries);
      directFlightsStmt.setString(2, originCity);
      directFlightsStmt.setString(3, destinationCity);
      directFlightsStmt.setInt(4, dayOfMonth);
      ResultSet oneHopResults = directFlightsStmt.executeQuery();

      while (oneHopResults.next()) {
        Flight flight1 = new Flight(
            oneHopResults.getInt("fid"),
            oneHopResults.getInt("day_of_month"),
            oneHopResults.getString("carrier_id"),
            oneHopResults.getString("flight_num"),
            oneHopResults.getString("origin_city"),
            oneHopResults.getString("dest_city"),
            oneHopResults.getInt("actual_time"),
            oneHopResults.getInt("capacity"),
            oneHopResults.getInt("price")
        );

        Itinerary itinerary = new Itinerary(flight1);
        itineraries.add(itinerary);
      }
      oneHopResults.close();

      // fill in the indirected flights if there are spots left
      if (!directFlight && itineraries.size() < numberOfItineraries) {
        directFlightsStmt.clearParameters();
        indirectFlightsStmt.setInt(1, numberOfItineraries - itineraries.size());
        indirectFlightsStmt.setString(2, originCity);
        indirectFlightsStmt.setString(3, destinationCity);
        indirectFlightsStmt.setInt(4, dayOfMonth);
        ResultSet twoHopResults = indirectFlightsStmt.executeQuery();

        while (twoHopResults.next()) {
          Flight flight1 = new Flight(
              twoHopResults.getInt("f1_fid"),
              twoHopResults.getInt("day_of_month"),
              twoHopResults.getString("f1_carrier_id"),
              twoHopResults.getString("f1_flight_num"),
              twoHopResults.getString("f1_origin_city"),
              twoHopResults.getString("f1_dest_city"),
              twoHopResults.getInt("f1_actual_time"),
              twoHopResults.getInt("f1_capacity"),
              twoHopResults.getInt("f1_price")
          );

          Flight flight2 = new Flight(
              twoHopResults.getInt("f2_fid"),
              twoHopResults.getInt("day_of_month"),
              twoHopResults.getString("f2_carrier_id"),
              twoHopResults.getString("f2_flight_num"),
              twoHopResults.getString("f2_origin_city"),
              twoHopResults.getString("f2_dest_city"),
              twoHopResults.getInt("f2_actual_time"),
              twoHopResults.getInt("f2_capacity"),
              twoHopResults.getInt("f2_price")
          );

          Itinerary itinerary = new Itinerary(flight1, flight2);
          itineraries.add(itinerary);
        }
        twoHopResults.close();
      }

      // Sort the itinerarys in the array
      Collections.sort(itineraries);

      // If empty result
      if (itineraries.size() == 0) {
        this.search = null;
        return "No flights match your selection\n";
      }

      // Output the results
      int count = 0;
      for (Itinerary itinerary : itineraries) {
          sb.append("Itinerary " + count + ": " + (itinerary.isDirect == 1 ? "1 flight(s)" : "2 flight(s)") +
                    ", " + itinerary.totalDuration + " minutes\n");
          sb.append(itinerary.flight1 + "\n");
          if (itinerary.isDirect == 0) {
              sb.append(itinerary.flight2 + "\n");
          }
          count++;
      }
      if (isLoggedin) {
        this.search = itineraries;
      }
      return sb.toString();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return "Failed to search\n";    
  }

  
  /* See QueryAbstract.java for javadoc */
  public String transaction_book(int itineraryId) {
    if (!isLoggedin) {
      return "Cannot book reservations, not logged in\n";
    }

    if (search == null || search.size() < itineraryId + 1) {
      return "No such itinerary " + itineraryId + "\n";
    }

    Itinerary itinerary = search.get(itineraryId); 
    int rid = -1;

    try {
      conn.setAutoCommit(false);
      // check if same date
      countUserReservationDateStmt.clearParameters();
      countUserReservationDateStmt.setInt(1, itinerary.flight1.dayOfMonth);
      countUserReservationDateStmt.setString(2, this.username);
      ResultSet count = countUserReservationDateStmt.executeQuery();

      if (count.next()) {
        int c = count.getInt("Count");
        if (c > 0) {
          conn.rollback();
          conn.setAutoCommit(true); 
          return "You cannot book two flights in the same day\n";
        }
      }       
      count.close();  

      // check if exceed capacity-flight 1
      countReservationCapacity1Stmt.clearParameters();
      countReservationCapacity1Stmt.setInt(1, itinerary.flight1.fid);
      ResultSet count1 = countReservationCapacity1Stmt.executeQuery();

      if (count1.next()) {
        int c = count1.getInt("Count");
        if (c >= itinerary.flight1.capacity) {
          conn.rollback();
          conn.setAutoCommit(true); 
          return "Booking failed\n";
        }
      }       
      count1.close();

      // check flight 2 capacity
      if (itinerary.flight2 != null) {
        countReservationCapacity2Stmt.clearParameters();
        countReservationCapacity2Stmt.setInt(1, itinerary.flight2.fid);
        ResultSet count2 = countReservationCapacity2Stmt.executeQuery();

        if (count2.next()) {
          int c = count2.getInt("Count");
          if (c >= itinerary.flight2.capacity) {
            conn.rollback();
            conn.setAutoCommit(true); 
            return "Booking failed\n";
          }
        }       
        count2.close();
      }

      countReservationStmt.clearParameters();
      ResultSet count3 = countReservationStmt.executeQuery();
      if (count3.next()) {
        rid = count3.getInt("Count") + 1;
      }
      
      // add reservation
      addReservationStmt.clearParameters();
      addReservationStmt.setInt(1, rid);
      addReservationStmt.setString(2, this.username);
      addReservationStmt.setInt(3, itinerary.flight1.fid);
      if (itinerary.isDirect == 0) {
        addReservationStmt.setInt(4, itinerary.flight2.fid);
      } else {
        addReservationStmt.setNull(4, java.sql.Types.INTEGER);
      }       
      addReservationStmt.executeUpdate();
              
      conn.commit();
      conn.setAutoCommit(true); 
      return "Booked flight(s), reservation ID: " + rid + "\n";    
    } catch (SQLException e) {
      try {
          conn.rollback();
          conn.setAutoCommit(true);
      } catch (SQLException se) {
          se.printStackTrace();
      }
      if (isDeadlock(e)) {
          return transaction_book(itineraryId);
      }
    }  
    return "Booking failed\n";  
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_pay(int reservationId) {
    if (!isLoggedin) {
      return "Cannot pay, not logged in\n";
    }
    
    int totalPrice = -1;
    int balance = -1;
    try {
      conn.setAutoCommit(false);
      searchReservationStmt.clearParameters();
      searchReservationStmt.setInt(1, reservationId);
      ResultSet result = searchReservationStmt.executeQuery();


      if (result.next()) {
        String name = result.getString("username");
        int isPaid = result.getInt("isPaid");
        totalPrice = result.getInt("price");

        if (!name.equals(this.username) || isPaid == 1) {
          conn.rollback();
          conn.setAutoCommit(true); 
          return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
        }
      } else {
        conn.rollback();
        conn.setAutoCommit(true); 
        return "Cannot find unpaid reservation " + reservationId + " under user: " + this.username + "\n";
      }    
      result.close();

      userBalanceStmt.clearParameters();
      userBalanceStmt.setString(1, username);
      ResultSet result2 = userBalanceStmt.executeQuery();

      if (result2.next()) {
        balance = result2.getInt("balance");

        if (balance < totalPrice) {
          conn.rollback();
          conn.setAutoCommit(true); 
          return "User has only " + balance + " in account but itinerary costs " + totalPrice + "\n";
        }
      }
      result2.close();

      // update user balance
      userUpdateBalanceStmt.clearParameters();
      userUpdateBalanceStmt.setInt(1, balance - totalPrice);
      userUpdateBalanceStmt.setString(2, username);
      userUpdateBalanceStmt.executeUpdate();

      // update isPaid status
      reservationUpdatePaidStmt.clearParameters();
      reservationUpdatePaidStmt.setInt(1, reservationId);
      reservationUpdatePaidStmt.executeUpdate();

      conn.commit();
      conn.setAutoCommit(true); 
      return "Paid reservation: " + reservationId + " remaining balance: " + (balance - totalPrice) + "\n";
    } catch (SQLException e) {
      try {
          conn.rollback();
          conn.setAutoCommit(true);
      } catch (SQLException se) {
          se.printStackTrace();
      }
      if (isDeadlock(e)) {
          return transaction_pay(reservationId);
      }
    }  
    return "Failed to pay for reservation " + reservationId + "\n"; 
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_reservations() {
    if (!isLoggedin) {
      return "Cannot view reservations, not logged in\n";
    }

    StringBuffer sb = new StringBuffer();
    List<Reservation> reservations = new ArrayList<>();

    try {
      conn.setAutoCommit(false);
      // check if there is reservation
      countReservationStmt.clearParameters();
      ResultSet count = countReservationStmt.executeQuery();

      if (count.next()) {
        int c = count.getInt("Count");

        if (c == 0) {
          conn.rollback();
          conn.setAutoCommit(true);
          return "No reservations found\n";
        }
      }
      count.close();
      
      // Add direct lights
      reservationDirectFlightsStmt.clearParameters();
      reservationDirectFlightsStmt.setString(1, this.username);
      ResultSet results1 = reservationDirectFlightsStmt.executeQuery();

      while (results1.next()) {
        Flight flight1 = new Flight(
          results1.getInt("f1_fid"),
          results1.getInt("day_of_month"),
          results1.getString("f1_carrier_id"),
          results1.getString("f1_flight_num"),
          results1.getString("f1_origin_city"),
          results1.getString("f1_dest_city"),
          results1.getInt("f1_actual_time"),
          results1.getInt("f1_capacity"),
          results1.getInt("f1_price")
        );
        int rid = results1.getInt("rid");
        int isPaid = results1.getInt("isPaid");

        reservations.add(new Reservation(flight1, rid, isPaid));
      }
      results1.close();

      reservationIndirectFlightsStmt.clearParameters();
      reservationIndirectFlightsStmt.setString(1, this.username);
      ResultSet results2 = reservationIndirectFlightsStmt.executeQuery();

      while (results2.next()) {
        Flight flight1 = new Flight(
          results2.getInt("f1_fid"),
          results2.getInt("day_of_month"),
          results2.getString("f1_carrier_id"),
          results2.getString("f1_flight_num"),
          results2.getString("f1_origin_city"),
          results2.getString("f1_dest_city"),
          results2.getInt("f1_actual_time"),
          results2.getInt("f1_capacity"),
          results2.getInt("f1_price")
        );

        Flight flight2 = new Flight(
          results2.getInt("f2_fid"),
          results2.getInt("day_of_month"),
          results2.getString("f2_carrier_id"),
          results2.getString("f2_flight_num"),
          results2.getString("f2_origin_city"),
          results2.getString("f2_dest_city"),
          results2.getInt("f2_actual_time"),
          results2.getInt("f2_capacity"),
          results2.getInt("f2_price")
        );
        int rid = results2.getInt("rid");
        int isPaid = results2.getInt("isPaid");

        reservations.add(new Reservation(flight1, flight2, rid, isPaid));
      }
      results2.close();

      // Sort the itinerarys in the array
      Collections.sort(reservations);

      for (Reservation r : reservations) {
          sb.append("Reservation " + r.rid + " paid: " + (r.isPaid == 1 ? "true" : "false") + ":\n");
          sb.append(r.flight1 + "\n");
          if (r.flight2 != null) {
              sb.append(r.flight2 + "\n");
          }
      }
      conn.commit();
      conn.setAutoCommit(true);
      return sb.toString();
    } catch (SQLException e) {
      try {
          conn.rollback();
          conn.setAutoCommit(true);
      } catch (SQLException se) {
          se.printStackTrace();
      }
      if (isDeadlock(e)) {
          return transaction_reservations();
      }
    }  
    return "Failed to retrieve reservations\n";
  }

  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setInt(1, fid);

    ResultSet results = flightCapacityStmt.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }

  /**
   * Utility function to determine whether an error was caused by a deadlock
   */
  private static boolean isDeadlock(SQLException e) {
    return e.getErrorCode() == 1205;
  }

  /**
   * A class to store information about a single flight
   *
   * TODO(hctang): move this into QueryAbstract
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    Flight(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
           int cap, int pri) {
      fid = id;
      dayOfMonth = day;
      carrierId = carrier;
      flightNum = fnum;
      originCity = origin;
      destCity = dest;
      time = tm;
      capacity = cap;
      price = pri;
    }
    
    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
          + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
          + " Capacity: " + capacity + " Price: " + price;
    }
  }

  /**
   * A class to store information about an itinerary
  */
  class Itinerary implements Comparable<Itinerary> {
    Flight flight1;  
    Flight flight2;  
    int totalDuration;  
    int isDirect;
    String originCity;
    String destCity;

    public Itinerary(Flight flight1) {
        this.flight1 = flight1;
        this.totalDuration = flight1.time;
        this.isDirect = 1;
        this.originCity = flight1.originCity;
        this.destCity = flight1.destCity;
    }

    public Itinerary(Flight flight1, Flight flight2) {
        this.flight1 = flight1;
        this.flight2 = flight2;
        this.totalDuration = flight1.time + flight2.time;
        this.isDirect = 0;
        this.originCity = flight1.originCity;
        this.destCity = flight2.destCity;
    }

    public int compareTo(Itinerary other){
         // Compare by total actual time
        if (this.totalDuration != other.totalDuration) {
            return this.totalDuration - other.totalDuration;
        }
        
        // Tie-breaker: Compare by the first fid
        if (this.flight1.fid != other.flight1.fid) {
            return this.flight1.fid - other.flight1.fid;
        }
        
        // Additional tie-breaker for indirect itineraries: Compare by the second fid
        if (this.isDirect == 0 && this.isDirect == 0) {
          return this.flight2.fid - other.flight2.fid;
        }

        return 0;
    }
  }

  /**
   * A class to store information about an reservation
  */
  class Reservation implements Comparable<Reservation> {
    Flight flight1;  
    Flight flight2;  
    int rid;
    int isPaid;

    public Reservation(Flight flight1, int rid, int isPaid) {
      this.flight1 = flight1;
      this.rid = rid;  
      this.isPaid = isPaid;
    }

    public Reservation(Flight flight1, Flight flight2, int rid, int isPaid) {
      this.flight1 = flight1;
      this.flight2 = flight2;
      this.rid = rid;  
      this.isPaid = isPaid;
    }

    public int compareTo(Reservation other){
      return this.rid - other.rid;
    }
  }
}