#  Movie Ticket Booking Backend

A Spring Boot based backend application that allows users to view movies and shows, select seats, book tickets, cancel bookings, and generate barcodes using an external Barcode API.

---

##  Objective

This project is built with the following goals:

- View movies and shows  
- Select available seats  
- Book tickets  
- Cancel bookings  
- Generate barcode using external API after successful booking  

Barcode API Used:
```
https://quickchart.io/barcode?type=code128&text=YOUR_TEXT
```

---

## Tech Stack

- Java  
- Spring Boot  
- Spring Data JPA  
- Hibernate  
- MySQL  
- REST APIs  
- External Barcode API (QuickChart)

---

##  Entity Relationships

---

##  Features

✔ Register User  
✔ Create Movie  
✔ Create Theatre  
✔ Create Show  
✔ View Shows by Movie  
✔ View Available Seats  
✔ Book Seats  
✔ View Booking Details  
✔ Cancel Booking  
✔ Generate Barcode after Booking  

---

##  REST API Endpoints

> Add your actual endpoints below (Max 9)

### 1️⃣ User API
```
POST   /users
```

### 2️⃣ Movie API
```
POST   /movies
```

### 3️⃣ Theatre API
```
POST   /theatres
```

### 4️⃣ Show APIs
```
POST   /shows
GET    /shows/movie/{movieId}
```

### 5️⃣ Seat API
```
GET    /bookings/theatre/{id}/seats
```

### 6️⃣ Booking APIs
```
POST   /bookings
GET    /bookings/{bookingId}
DELETE    /bookings/{bookingId}
```
