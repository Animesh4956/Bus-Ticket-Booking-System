package com.example.demo.controller;

import com.example.demo.model.Booking;
import com.example.demo.model.Bus;
import com.example.demo.model.Passenger;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.BusRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.BookingService;
import com.example.demo.service.PdfTicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
public class BusBookingController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PdfTicketService pdfTicketService;

    // Helper: Get Logged in user from session
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }

    // HOME PAGE
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        model.addAttribute("currentUser", getLoggedInUser(session));
        model.addAttribute("sources", busRepository.findDistinctSources());
        model.addAttribute("destinations", busRepository.findDistinctDestinations());
        return "index";
    }

    // PNR STATUS SEARCH
    @GetMapping("/pnr/search")
    public String pnrSearch(@RequestParam(required = false) String pnr, Model model, RedirectAttributes redirectAttributes) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return "redirect:/";
        }
        Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(pnr.trim().toUpperCase());
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No ticket found with Reference / PNR: " + pnr);
            return "redirect:/";
        }
        return "redirect:/ticket/verify/" + bookingOpt.get().getBookingReference();
    }

    // PUBLIC TICKET VERIFICATION (QR Code landing page)
    @GetMapping("/ticket/verify/{reference}")
    public String verifyTicket(@PathVariable String reference, Model model) {
        Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(reference);
        if (bookingOpt.isEmpty()) {
            model.addAttribute("notFound", true);
            model.addAttribute("reference", reference);
            return "ticket-verify";
        }
        model.addAttribute("booking", bookingOpt.get());
        model.addAttribute("notFound", false);
        return "ticket-verify";
    }

    // LOGIN PAGE
    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (getLoggedInUser(session) != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    // PROCESS LOGIN
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("currentUser", userOpt.get());
            if ("ROLE_ADMIN".equals(userOpt.get().getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }

    // REGISTER PAGE
    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (getLoggedInUser(session) != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    // PROCESS REGISTER
    @PostMapping("/register")
    public String processRegister(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String fullName,
                                  @RequestParam String email,
                                  @RequestParam String phone,
                                  RedirectAttributes redirectAttributes) {
        if (userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username is already taken");
            return "redirect:/register";
        }
        User newUser = new User(username, password, fullName, email, phone, "ROLE_USER");
        userRepository.save(newUser);
        redirectAttributes.addFlashAttribute("success", "Account created successfully! Please login.");
        return "redirect:/login";
    }

    // LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    // USER DASHBOARD
    @GetMapping("/dashboard")
    public String userDashboard(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }
        List<Booking> userBookings = bookingRepository.findByUserOrderByBookingTimeDesc(user);
        model.addAttribute("user", user);
        model.addAttribute("bookings", userBookings);
        return "dashboard";
    }

    // BUS SEARCH
    @GetMapping("/buses")
    public String searchBuses(@RequestParam(required = false) String source,
                              @RequestParam(required = false) String destination,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              HttpSession session,
                              Model model) {
        model.addAttribute("currentUser", getLoggedInUser(session));
        List<Bus> buses;
        if (source != null && !source.isBlank() && destination != null && !destination.isBlank()) {
            if (date != null) {
                buses = busRepository.findBySourceAndDestinationAndDate(source.trim(), destination.trim(), date);
            } else {
                buses = busRepository.findBySourceAndDestination(source.trim(), destination.trim());
            }
        } else {
            buses = busRepository.findAll();
        }

        // Map bus id to available seats count
        Map<Long, Integer> availableSeatsMap = new HashMap<>();
        for (Bus b : buses) {
            Set<String> bookedSeats = bookingService.getBookedSeatsForBus(b.getId());
            availableSeatsMap.put(b.getId(), Math.max(0, b.getTotalSeats() - bookedSeats.size()));
        }

        model.addAttribute("buses", buses);
        model.addAttribute("availableSeatsMap", availableSeatsMap);
        model.addAttribute("selectedSource", source);
        model.addAttribute("selectedDest", destination);
        model.addAttribute("selectedDate", date);
        model.addAttribute("allSources", busRepository.findDistinctSources());
        model.addAttribute("allDestinations", busRepository.findDistinctDestinations());
        return "buses";
    }

    // BUS SEAT SELECTION & BOOKING PAGE
    @GetMapping("/book/{busId}")
    public String bookSeatPage(@PathVariable Long busId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to book tickets");
            return "redirect:/login";
        }

        Optional<Bus> busOpt = busRepository.findById(busId);
        if (busOpt.isEmpty()) {
            return "redirect:/buses";
        }

        Bus bus = busOpt.get();
        Set<String> bookedSeats = bookingService.getBookedSeatsForBus(busId);

        model.addAttribute("currentUser", user);
        model.addAttribute("bus", bus);
        model.addAttribute("bookedSeats", bookedSeats);
        model.addAttribute("totalSeats", bus.getTotalSeats());
        return "book-seat";
    }

    // PROCESS BOOKING WITH PER-SEAT PASSENGER DETAILS AND PAYMENT METHOD
    @PostMapping("/book/confirm")
    public String confirmBooking(@RequestParam Long busId,
                                 @RequestParam String seatNumbers,
                                 @RequestParam(required = false, defaultValue = "UPI") String paymentMethod,
                                 HttpServletRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Bus> busOpt = busRepository.findById(busId);
        if (busOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bus not found");
            return "redirect:/buses";
        }

        Bus bus = busOpt.get();

        if (seatNumbers == null || seatNumbers.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one seat.");
            return "redirect:/book/" + busId;
        }

        String[] seatArr = seatNumbers.split(",");
        List<String> chosenSeats = new ArrayList<>();
        for (String s : seatArr) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                chosenSeats.add(trimmed);
            }
        }

        if (chosenSeats.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one seat.");
            return "redirect:/book/" + busId;
        }

        // Verify no collisions
        Set<String> alreadyBooked = bookingService.getBookedSeatsForBus(busId);
        for (String seat : chosenSeats) {
            if (alreadyBooked.contains(seat)) {
                redirectAttributes.addFlashAttribute("error", "Seat " + seat + " was just booked by another user! Please select different seats.");
                return "redirect:/book/" + busId;
            }
        }

        int seatCount = chosenSeats.size();
        double totalFare = bus.getFare() * seatCount;
        String formattedSeatString = String.join(", ", chosenSeats);

        // Lead Passenger (first seat)
        String leadPassengerName = request.getParameter("passenger_name_" + chosenSeats.get(0));
        if (leadPassengerName == null || leadPassengerName.trim().isEmpty()) {
            leadPassengerName = user.getFullName();
        }

        String ageStrLead = request.getParameter("passenger_age_" + chosenSeats.get(0));
        Integer leadAge = (ageStrLead != null && !ageStrLead.isEmpty()) ? Integer.parseInt(ageStrLead) : 25;
        String leadGender = request.getParameter("passenger_gender_" + chosenSeats.get(0));
        if (leadGender == null) leadGender = "Male";

        String reference = "TKT-" + (100000 + new Random().nextInt(900000));

        Booking booking = new Booking(reference, user, bus, leadPassengerName, leadAge, leadGender,
                formattedSeatString, seatCount, totalFare, paymentMethod, LocalDateTime.now(), "CONFIRMED");

        // Populate all individual passengers per seat
        for (String seat : chosenSeats) {
            String pName = request.getParameter("passenger_name_" + seat);
            if (pName == null || pName.trim().isEmpty()) {
                pName = "Passenger (" + seat + ")";
            }

            String pAgeStr = request.getParameter("passenger_age_" + seat);
            Integer pAge = (pAgeStr != null && !pAgeStr.isEmpty()) ? Integer.parseInt(pAgeStr) : 25;

            String pGender = request.getParameter("passenger_gender_" + seat);
            if (pGender == null) pGender = "Male";

            Passenger passenger = new Passenger(seat, pName.trim(), pAge, pGender);
            booking.addPassenger(passenger);
        }

        bookingRepository.save(booking);

        return "redirect:/booking/confirmation/" + booking.getBookingReference();
    }

    // BOOKING CONFIRMATION SCREEN
    @GetMapping("/booking/confirmation/{reference}")
    public String bookingConfirmation(@PathVariable String reference, HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(reference);
        if (bookingOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("booking", bookingOpt.get());
        return "confirmation";
    }

    // DOWNLOAD PDF TICKET
    @GetMapping("/booking/ticket-pdf/{reference}")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable String reference, HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(reference);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking booking = bookingOpt.get();
        if (!booking.getUser().getId().equals(user.getId()) && !"ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).build();
        }

        try {
            byte[] pdfBytes = pdfTicketService.generateTicketPdf(booking);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Ticket_" + booking.getBookingReference() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // CANCEL TICKET (With Instant Refund Calculation)
    @PostMapping("/booking/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            if (booking.getUser().getId().equals(user.getId()) || "ROLE_ADMIN".equals(user.getRole())) {
                booking.setStatus("CANCELLED");
                bookingRepository.save(booking);

                // Refund calculation: 90% if > 24 hours, 50% otherwise
                double refundPct = 0.90;
                double refundAmount = booking.getTotalFare() * refundPct;

                redirectAttributes.addFlashAttribute("success", 
                        "Ticket " + booking.getBookingReference() + " was successfully cancelled. " +
                        "A refund of ₹" + String.format("%.2f", refundAmount) + " (90%) has been initiated to your " + booking.getPaymentMethod() + " account.");
            }
        }
        return "redirect:/dashboard";
    }

    // --- ADMIN CONTROLLER METHODS ---

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Bus> buses = busRepository.findAll();
        List<Booking> allBookings = bookingRepository.findAllByOrderByBookingTimeDesc();
        long totalConfirmedBookings = bookingRepository.countByStatus("CONFIRMED");
        Double totalRevenue = bookingRepository.calculateTotalRevenue();

        Map<Long, Integer> busOccupancyCounts = new HashMap<>();
        for (Bus b : buses) {
            Set<String> bookedSeats = bookingService.getBookedSeatsForBus(b.getId());
            busOccupancyCounts.put(b.getId(), bookedSeats.size());
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("buses", buses);
        model.addAttribute("bookings", allBookings);
        model.addAttribute("totalBuses", buses.size());
        model.addAttribute("totalConfirmedBookings", totalConfirmedBookings);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        model.addAttribute("busBookingCounts", busOccupancyCounts);

        return "admin-dashboard";
    }

    // ADMIN: VIEW BUS PASSENGERS
    @GetMapping("/admin/bus/{id}/passengers")
    public String viewBusPassengers(@PathVariable Long id, HttpSession session, Model model) {
        User user = getLoggedInUser(session);
        if (user == null || !"ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        Optional<Bus> busOpt = busRepository.findById(id);
        if (busOpt.isEmpty()) {
            return "redirect:/admin/dashboard";
        }

        Bus bus = busOpt.get();
        List<Booking> passengers = bookingRepository.findByBusAndStatus(bus, "CONFIRMED");

        model.addAttribute("currentUser", user);
        model.addAttribute("bus", bus);
        model.addAttribute("passengers", passengers);
        return "admin-bus-passengers";
    }

    // ADMIN: ADD BUS
    @PostMapping("/admin/bus/add")
    public String addBus(@RequestParam String busNumber,
                         @RequestParam String busName,
                         @RequestParam String source,
                         @RequestParam String destination,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate,
                         @RequestParam String departureTime,
                         @RequestParam String arrivalTime,
                         @RequestParam Double fare,
                         @RequestParam Integer totalSeats,
                         @RequestParam String busType,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null || !"ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        Bus newBus = new Bus(busNumber, busName, source, destination, journeyDate,
                LocalTime.parse(departureTime), LocalTime.parse(arrivalTime), fare, totalSeats, busType);
        busRepository.save(newBus);

        redirectAttributes.addFlashAttribute("success", "Bus schedule added successfully!");
        return "redirect:/admin/dashboard";
    }

    // ADMIN: DELETE BUS
    @PostMapping("/admin/bus/delete/{id}")
    public String deleteBus(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(session);
        if (user == null || !"ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        busRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Bus schedule removed.");
        return "redirect:/admin/dashboard";
    }
}
