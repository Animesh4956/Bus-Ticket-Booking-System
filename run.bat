@echo off
title Bus Ticket Booking System
echo ========================================================
echo     BUS TICKET BOOKING SYSTEM - STARTING APPLICATION
echo ========================================================
echo.
echo Requirements: Java 21 is installed.
echo Running local server at: http://localhost:8080
echo.

if exist "target\busbooking-0.0.1-SNAPSHOT.jar" (
    java -jar "target\busbooking-0.0.1-SNAPSHOT.jar"
) else (
    echo Building project first with Maven...
    call mvn clean package -DskipTests
    java -jar "target\busbooking-0.0.1-SNAPSHOT.jar"
)

pause
