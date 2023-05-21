import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TicketBookingSystem {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/movie_ticket_booking_system";
    private static final String DATABASE_USERNAME = "root";
    private static final String DATABASE_PASSWORD = "password";

    private Connection connection;
    private Scanner scanner;
    private UserInterface userInterface;

    public TicketBookingSystem() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
            scanner = new Scanner(System.in);
            userInterface = new UserInterface(scanner);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void bookTicket() {
        // Get the movie and theater from the user.
        Movie movie = userInterface.getMovie();
        Theater theater = userInterface.getTheater();

        // Get the list of available seats.
        List<Seat> seats = getAvailableSeats(movie, theater);

        // Allow the user to select the seats they want to book.
        List<Seat> selectedSeats = userInterface.selectSeats(seats);

        // Calculate the total cost of the tickets.
        int totalCost = calculateTicketCost(movie, selectedSeats);

        // Allow the user to pay for the tickets.
        userInterface.payForTickets(totalCost);

        // Send the user a confirmation email with their ticket information.
        userInterface.sendConfirmationEmail(movie, theater, selectedSeats);
    }

    private List<Seat> getAvailableSeats(Movie movie, Theater theater) {
        List<Seat> seats = new ArrayList<>();

        try {
            String sql = "SELECT seat_id, row, column FROM seats WHERE movie_id = ? AND theater_id = ? AND status = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, movie.getId());
            statement.setInt(2, theater.getId());
            statement.setString(3, "AVAILABLE");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                seats.add(new Seat(
                        resultSet.getInt("seat_id"),
                        resultSet.getString("row"),
                        resultSet.getString("column")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seats;
    }

    private int calculateTicketCost(Movie movie, List<Seat> selectedSeats) {
        int totalCost = 0;

        for (Seat seat : selectedSeats) {
            totalCost += movie.getTicketPrice();
        }

        return totalCost;
    }

    public static void main(String[] args) throws IOException {
        TicketBookingSystem ticketBookingSystem = new TicketBookingSystem();
        ticketBookingSystem.bookTicket();
    }
}

class Movie {

    private int id;
    private String name;
    private String genre;
    private String releaseDate;
    private int ticketPrice;

    public Movie(int id, String name, String genre, String releaseDate, int ticketPrice) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.ticketPrice = ticketPrice;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenre() {
        return genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

 
