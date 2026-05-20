package com.jsp.book.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jsp.book.entity.Movie;
import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Seat;
import com.jsp.book.entity.Show;
import com.jsp.book.entity.ShowSeat;
import com.jsp.book.entity.Theater;
import com.jsp.book.repository.MovieRepository;
import com.jsp.book.repository.ScreenRepository;
import com.jsp.book.repository.SeatRepository;
import com.jsp.book.repository.ShowRepository;
import com.jsp.book.repository.TheaterRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private static final String DEMO_THEATER_NAME = "Test Theater";
	private static final String DEMO_THEATER_ADDRESS = "123 Test Ave";
	private static final String DEMO_SCREEN_NAME = "Screen 1";

	private final TheaterRepository theaterRepository;
	private final ScreenRepository screenRepository;
	private final SeatRepository seatRepository;
	private final MovieRepository movieRepository;
	private final ShowRepository showRepository;

	@Override
	public void run(String... args) throws Exception {
		List<Movie> movies = movieRepository.findAll();
		if (movies.isEmpty()) {
			return;
		}

		Screen screen = getOrCreateDemoScreen();
		List<Seat> seats = getOrCreateSeats(screen);
		LocalDate activeCutoff = LocalDate.now().minusDays(1);
		int createdShows = 0;

		for (Movie movie : movies) {
			boolean hasActiveShow = !showRepository.findByMovieAndShowDateAfter(movie, activeCutoff).isEmpty();
			if (!hasActiveShow) {
				createDemoShow(movie, screen, seats);
				createdShows++;
			}
		}

		if (createdShows > 0) {
			System.out.println("Generated " + createdShows + " active demo shows. Booking pages are ready.");
		}
	}

	private Screen getOrCreateDemoScreen() {
		Theater theater = theaterRepository.findAll().stream()
				.filter(item -> DEMO_THEATER_NAME.equals(item.getName())
						&& DEMO_THEATER_ADDRESS.equals(item.getAddress()))
				.findFirst()
				.orElseGet(this::createDemoTheater);

		return screenRepository.findByTheater(theater).stream()
				.filter(screen -> DEMO_SCREEN_NAME.equals(screen.getName()))
				.findFirst()
				.orElseGet(() -> createDemoScreen(theater));
	}

	private Theater createDemoTheater() {
		Theater theater = new Theater();
		theater.setName(DEMO_THEATER_NAME);
		theater.setAddress(DEMO_THEATER_ADDRESS);
		theater.setLocationLink("https://maps.google.com");
		theater.setImageLocation("/theater-placeholder.svg");
		theater.setScreenCount(1);
		return theaterRepository.save(theater);
	}

	private Screen createDemoScreen(Theater theater) {
		Screen screen = new Screen();
		screen.setName(DEMO_SCREEN_NAME);
		screen.setType("IMAX 3D");
		screen.setTheater(theater);
		return screenRepository.save(screen);
	}

	private List<Seat> getOrCreateSeats(Screen screen) {
		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);
		if (!seats.isEmpty()) {
			return seats;
		}

		char[] rows = { 'A', 'B', 'C', 'D', 'E' };
		for (char rowName : rows) {
			String category = (rowName == 'E') ? "VIP" : (rowName == 'C' || rowName == 'D') ? "Premium" : "Standard";
			for (int i = 1; i <= 10; i++) {
				Seat seat = new Seat();
				seat.setScreen(screen);
				seat.setSeatRow(String.valueOf(rowName));
				seat.setSeatColumn(i);
				seat.setSeatNumber(rowName + String.valueOf(i));
				seat.setCategory(category.toUpperCase());
				seats.add(seat);
			}
		}

		return seatRepository.saveAll(seats);
	}

	private void createDemoShow(Movie movie, Screen screen, List<Seat> seats) {
		Show show = new Show();
		show.setMovie(movie);
		show.setScreen(screen);
		show.setShowDate(getBookableDate(movie));
		show.setStartTime(LocalTime.of(18, 0));
		show.setEndTime(show.getStartTime().plusHours(movie.getDuration().getHour())
				.plusMinutes(movie.getDuration().getMinute() + 30));
		show.setTicketPrice(250.0);
		show.setSeats(createShowSeats(seats));

		showRepository.save(show);
	}

	private LocalDate getBookableDate(Movie movie) {
		LocalDate today = LocalDate.now();
		return movie.getReleaseDate().isAfter(today) ? movie.getReleaseDate() : today;
	}

	private List<ShowSeat> createShowSeats(List<Seat> seats) {
		List<ShowSeat> showSeats = new ArrayList<>();
		for (Seat seat : seats) {
			ShowSeat showSeat = new ShowSeat();
			showSeat.setSeat(seat);
			showSeat.setBooked(false);
			showSeats.add(showSeat);
		}
		return showSeats;
	}
}
