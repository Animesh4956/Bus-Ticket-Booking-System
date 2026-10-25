package com.example.demo.service;

import com.example.demo.model.Booking;
import com.example.demo.model.Bus;
import com.example.demo.model.Passenger;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfTicketService {

    public byte[] generateTicketPdf(Booking booking) throws DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(30, 58, 138));
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(30, 58, 138));
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(71, 85, 105));
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(22, 101, 52));

        // Header Table
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{3f, 2f});

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.addElement(new Paragraph("BUS TICKET CONFIRMATION", titleFont));
        titleCell.addElement(new Paragraph("Online Booking E-Ticket / Boarding Pass", subTitleFont));
        headerTable.addCell(titleCell);

        PdfPCell pnrCell = new PdfPCell();
        pnrCell.setBorder(Rectangle.NO_BORDER);
        pnrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph pnrPara = new Paragraph("PNR / Ticket Ref:\n" + booking.getBookingReference(), 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(220, 38, 38)));
        pnrPara.setAlignment(Element.ALIGN_RIGHT);
        pnrCell.addElement(pnrPara);

        Paragraph statusPara = new Paragraph("Status: " + booking.getStatus(), statusFont);
        statusPara.setAlignment(Element.ALIGN_RIGHT);
        pnrCell.addElement(statusPara);

        headerTable.addCell(pnrCell);
        document.add(headerTable);

        // Divider
        LineSeparator sep = new LineSeparator(1, 100, new Color(203, 213, 225), Element.ALIGN_CENTER, -2);
        document.add(new Paragraph(" "));
        document.add(sep);
        document.add(new Paragraph(" "));

        Bus bus = booking.getBus();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        // Journey Summary Box
        PdfPTable routeTable = new PdfPTable(3);
        routeTable.setWidthPercentage(100);
        routeTable.setWidths(new float[]{2f, 1f, 2f});

        PdfPCell fromCell = new PdfPCell();
        fromCell.setBackgroundColor(new Color(241, 245, 249));
        fromCell.setPadding(10);
        fromCell.setBorder(Rectangle.NO_BORDER);
        fromCell.addElement(new Paragraph("FROM", labelFont));
        fromCell.addElement(new Paragraph(bus.getSource().toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK)));
        fromCell.addElement(new Paragraph("Departure: " + bus.getDepartureTime().format(timeFmt), valFont));
        fromCell.addElement(new Paragraph("Date: " + bus.getJourneyDate().format(dateFmt), subTitleFont));
        routeTable.addCell(fromCell);

        PdfPCell arrowCell = new PdfPCell();
        arrowCell.setBackgroundColor(new Color(241, 245, 249));
        arrowCell.setPadding(10);
        arrowCell.setBorder(Rectangle.NO_BORDER);
        arrowCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        arrowCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph arrowPara = new Paragraph("➔", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(59, 130, 246)));
        arrowPara.setAlignment(Element.ALIGN_CENTER);
        arrowCell.addElement(arrowPara);
        routeTable.addCell(arrowCell);

        PdfPCell toCell = new PdfPCell();
        toCell.setBackgroundColor(new Color(241, 245, 249));
        toCell.setPadding(10);
        toCell.setBorder(Rectangle.NO_BORDER);
        toCell.addElement(new Paragraph("TO", labelFont));
        toCell.addElement(new Paragraph(bus.getDestination().toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK)));
        toCell.addElement(new Paragraph("Arrival: " + bus.getArrivalTime().format(timeFmt), valFont));
        toCell.addElement(new Paragraph("Date: " + bus.getJourneyDate().format(dateFmt), subTitleFont));
        routeTable.addCell(toCell);

        document.add(routeTable);
        document.add(new Paragraph(" "));

        // Bus & Reservation Details Section
        Paragraph secHeader = new Paragraph("TRIP & RESERVATION DETAILS", sectionHeaderFont);
        secHeader.setSpacingAfter(6);
        document.add(secHeader);

        PdfPTable busInfoTable = new PdfPTable(4);
        busInfoTable.setWidthPercentage(100);
        busInfoTable.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f});

        addTableRow(busInfoTable, "Bus Operator", bus.getBusName(), labelFont, valFont);
        addTableRow(busInfoTable, "Bus Number", bus.getBusNumber(), labelFont, valFont);
        addTableRow(busInfoTable, "Bus Type", bus.getBusType(), labelFont, valFont);
        addTableRow(busInfoTable, "Payment Method", (booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "UPI") + " (Verified)", labelFont, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(22, 101, 52)));

        addTableRow(busInfoTable, "Booked By Account", booking.getUser().getUsername() + " (" + booking.getUser().getFullName() + ")", labelFont, valFont);
        addTableRow(busInfoTable, "Total Seats Reserved", String.valueOf(booking.getSeatCount()), labelFont, valFont);
        addTableRow(busInfoTable, "Fare Breakdown", "₹ " + bus.getFare() + " x " + booking.getSeatCount() + " seat(s)", labelFont, valFont);
        addTableRow(busInfoTable, "Total Fare Paid", "₹ " + String.format("%.2f", booking.getTotalFare()), labelFont, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(22, 101, 52)));

        document.add(busInfoTable);
        document.add(new Paragraph(" "));

        // Individual Passengers List Table
        Paragraph passHeader = new Paragraph("PASSENGER MANIFEST (" + booking.getSeatCount() + " SEATS)", sectionHeaderFont);
        passHeader.setSpacingAfter(6);
        document.add(passHeader);

        PdfPTable passTable = new PdfPTable(4);
        passTable.setWidthPercentage(100);
        passTable.setWidths(new float[]{1f, 2.5f, 1.2f, 1.3f});

        // Header cells
        addTableHeaderCell(passTable, "SEAT #", labelFont);
        addTableHeaderCell(passTable, "PASSENGER NAME", labelFont);
        addTableHeaderCell(passTable, "AGE", labelFont);
        addTableHeaderCell(passTable, "GENDER", labelFont);

        if (booking.getPassengers() != null && !booking.getPassengers().isEmpty()) {
            for (Passenger p : booking.getPassengers()) {
                addTableCell(passTable, p.getSeatNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
                addTableCell(passTable, p.getName(), valFont);
                addTableCell(passTable, p.getAge() != null ? p.getAge() + " yrs" : "N/A", valFont);
                addTableCell(passTable, p.getGender() != null ? p.getGender() : "N/A", valFont);
            }
        } else {
            addTableCell(passTable, booking.getSeatNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
            addTableCell(passTable, booking.getPassengerName(), valFont);
            addTableCell(passTable, booking.getPassengerAge() != null ? booking.getPassengerAge() + " yrs" : "N/A", valFont);
            addTableCell(passTable, booking.getPassengerGender() != null ? booking.getPassengerGender() : "N/A", valFont);
        }

        document.add(passTable);
        document.add(new Paragraph(" "));

        // Digital Barcode & Boarding Pass Verification Banner
        try {
            PdfContentByte cb = writer.getDirectContent();
            Barcode128 code128 = new Barcode128();
            code128.setCode(booking.getBookingReference());
            code128.setCodeType(Barcode128.CODE128);
            Image barcodeImage = code128.createImageWithBarcode(cb, null, null);
            barcodeImage.scaleToFit(140, 45);

            String verificationUrl = "http://localhost:8080/ticket/verify/" + booking.getBookingReference();

            PdfPTable qrTable = new PdfPTable(2);
            qrTable.setWidthPercentage(100);
            qrTable.setWidths(new float[]{1.5f, 3.5f});

            PdfPCell barCell = new PdfPCell(barcodeImage);
            barCell.setBorder(Rectangle.BOX);
            barCell.setBorderColor(new Color(226, 232, 240));
            barCell.setPadding(8);
            barCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            barCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            qrTable.addCell(barCell);

            PdfPCell qrDescCell = new PdfPCell();
            qrDescCell.setBorder(Rectangle.BOX);
            qrDescCell.setBorderColor(new Color(226, 232, 240));
            qrDescCell.setPadding(8);
            qrDescCell.setBackgroundColor(new Color(248, 250, 252));
            qrDescCell.addElement(new Paragraph("OFFICIAL DIGITAL BOARDING PASS VERIFICATION", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138))));
            qrDescCell.addElement(new Paragraph("Present this digital barcode at terminal check-in gate for instant boarding pass validation.", subTitleFont));
            qrDescCell.addElement(new Paragraph("Verification Link: " + verificationUrl, FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(59, 130, 246))));
            qrTable.addCell(qrDescCell);

            document.add(qrTable);
        } catch (Exception ignored) {
        }

        document.add(new Paragraph(" "));

        // Instructions
        Paragraph instrHeader = new Paragraph("IMPORTANT TRAVEL INSTRUCTIONS", sectionHeaderFont);
        instrHeader.setSpacingAfter(4);
        document.add(instrHeader);

        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        list.add(new ListItem("Please present this e-ticket along with a valid Government ID for each passenger.", subTitleFont));
        list.add(new ListItem("Report to the boarding point at least 15 minutes before the scheduled departure time.", subTitleFont));
        list.add(new ListItem("Smoking and consumption of alcohol are strictly prohibited inside the bus.", subTitleFont));
        document.add(list);

        document.add(new Paragraph(" "));
        document.add(sep);

        Paragraph footer = new Paragraph("Generated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")) + " | BusGo Ticket Booking System", subTitleFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value, valFont));
        table.addCell(cell);
    }

    private void addTableHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setPadding(6);
        cell.setBorderColor(new Color(203, 213, 225));
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPadding(6);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }
}
