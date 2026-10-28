CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    CONSTRAINT uk_user_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE movie (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    duration_minutes INT NOT NULL,
    language VARCHAR(255),
    rating VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE theatre (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    location VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE seat (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(255),
    type VARCHAR(255),
    is_available BIT NOT NULL,
    theatre_id BIGINT,
    CONSTRAINT fk_seat_theatre FOREIGN KEY (theatre_id) REFERENCES theatre(id)
) ENGINE=InnoDB;

CREATE TABLE shows (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    show_time DATETIME(6),
    price_per_seat DOUBLE,
    movie_id BIGINT,
    theatre_id BIGINT,
    CONSTRAINT fk_show_movie FOREIGN KEY (movie_id) REFERENCES movie(id),
    CONSTRAINT fk_show_theatre FOREIGN KEY (theatre_id) REFERENCES theatre(id)
) ENGINE=InnoDB;

CREATE TABLE show_seat (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    show_id BIGINT,
    seat_id BIGINT,
    available BIT NOT NULL,
    CONSTRAINT uk_show_seat UNIQUE (show_id, seat_id),
    CONSTRAINT fk_show_seat_show FOREIGN KEY (show_id) REFERENCES shows(id),
    CONSTRAINT fk_show_seat_seat FOREIGN KEY (seat_id) REFERENCES seat(id)
) ENGINE=InnoDB;

CREATE TABLE booking (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    booking_time DATETIME(6),
    total_amount DOUBLE,
    status VARCHAR(255),
    user_id BIGINT,
    show_id BIGINT,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_booking_show FOREIGN KEY (show_id) REFERENCES shows(id)
) ENGINE=InnoDB;

CREATE TABLE ticket (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    barcode_image LONGBLOB,
    issued_at DATETIME(6),
    booking_id BIGINT,
    seat_id BIGINT,
    CONSTRAINT fk_ticket_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_id) REFERENCES seat(id)
) ENGINE=InnoDB;
