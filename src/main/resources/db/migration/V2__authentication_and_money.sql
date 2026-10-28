ALTER TABLE users ADD COLUMN password_hash VARCHAR(100);
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE shows MODIFY price_per_seat DECIMAL(12,2) NOT NULL;
ALTER TABLE booking MODIFY total_amount DECIMAL(12,2) NOT NULL;
ALTER TABLE booking MODIFY status VARCHAR(20) NOT NULL;
ALTER TABLE users ADD CONSTRAINT ck_user_role CHECK (role IN ('USER', 'ADMIN'));
ALTER TABLE booking ADD CONSTRAINT ck_booking_status CHECK (status IN ('CONFIRMED', 'CANCELLED'));
ALTER TABLE booking ADD CONSTRAINT ck_booking_amount CHECK (total_amount >= 0);
ALTER TABLE shows ADD CONSTRAINT ck_show_price CHECK (price_per_seat > 0);
