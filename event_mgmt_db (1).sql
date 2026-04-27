-- ============================================
--  EVENT MANAGEMENT SYSTEM
--  Mini Project | Java (NetBeans) + MySQL
--  Domain: Event Management
-- ============================================

CREATE DATABASE IF NOT EXISTS event_mgmt_db;
USE event_mgmt_db;

-- -----------------------------------------------
-- TABLE: users (Admin / Organizer login)
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    full_name   VARCHAR(100),
    role        ENUM('admin','organizer') DEFAULT 'organizer',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------
-- TABLE: venues
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS venues (
    venue_id    INT AUTO_INCREMENT PRIMARY KEY,
    venue_name  VARCHAR(150) NOT NULL,
    location    VARCHAR(200),
    capacity    INT DEFAULT 100,
    contact     VARCHAR(50)
);

-- -----------------------------------------------
-- TABLE: events
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    event_id      INT AUTO_INCREMENT PRIMARY KEY,
    event_name    VARCHAR(150) NOT NULL,
    event_type    VARCHAR(100),
    event_date    DATE NOT NULL,
    event_time    TIME,
    venue_id      INT,
    organizer_id  INT,
    description   TEXT,
    budget        DECIMAL(10,2) DEFAULT 0,
    status        ENUM('Upcoming','Ongoing','Completed','Cancelled') DEFAULT 'Upcoming',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (venue_id)     REFERENCES venues(venue_id),
    FOREIGN KEY (organizer_id) REFERENCES users(user_id)
);

-- -----------------------------------------------
-- TABLE: participants
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS participants (
    participant_id  INT AUTO_INCREMENT PRIMARY KEY,
    event_id        INT NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(100),
    phone           VARCHAR(15),
    college         VARCHAR(150),
    registration_no VARCHAR(30) UNIQUE,
    status          ENUM('Registered','Attended','Absent','Cancelled') DEFAULT 'Registered',
    registered_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(event_id)
);

-- -----------------------------------------------
-- SAMPLE DATA
-- -----------------------------------------------

-- Users
INSERT INTO users (username, password, full_name, role) VALUES
('admin',   'admin123', 'Admin User',          'admin'),
('org1',    'org123',   'Prof. R. Sharma',      'organizer'),
('org2',    'org456',   'Prof. S. Patil',       'organizer');

-- Venues
INSERT INTO venues (venue_name, location, capacity, contact) VALUES
('Main Auditorium',     'Block A, Ground Floor',  500, '9876500001'),
('Seminar Hall 1',      'Block B, First Floor',   150, '9876500002'),
('Seminar Hall 2',      'Block B, Second Floor',  150, '9876500003'),
('Open Air Theatre',    'Campus Ground',         1000, '9876500004'),
('Conference Room',     'Admin Block',             50, '9876500005');

-- Events
INSERT INTO events (event_name, event_type, event_date, event_time, venue_id, organizer_id, description, budget, status) VALUES
('TechFest 2025',        'Technical',    '2025-02-15', '09:00:00', 1, 1, 'Annual technical festival with coding contests and project expo', 50000, 'Completed'),
('Cultural Night',       'Cultural',     '2025-03-10', '18:00:00', 4, 2, 'Annual cultural event with dance, music and drama performances', 30000, 'Completed'),
('Workshop on Java',     'Workshop',     '2025-04-05', '10:00:00', 2, 2, 'Hands-on Java programming workshop for SE and TE students', 10000, 'Completed'),
('Annual Sports Day',    'Sports',       '2025-05-20', '08:00:00', 4, 1, 'Annual inter-departmental sports competition', 25000, 'Upcoming'),
('Seminar on AI/ML',     'Seminar',      '2025-06-12', '11:00:00', 3, 2, 'Seminar on Artificial Intelligence and Machine Learning trends', 15000, 'Upcoming'),
('Hackathon 2025',       'Technical',    '2025-07-18', '09:00:00', 1, 1, '24-hour hackathon for innovative project development', 40000, 'Upcoming');

-- Participants
INSERT INTO participants (event_id, full_name, email, phone, college, registration_no, status) VALUES
(1,'Aadi Shah',       'aadi@email.com',     '9876543210','SNJB CoE','TF2025001','Attended'),
(1,'Rashmi Shah',     'rashmi@email.com',   '9876543211','SNJB CoE','TF2025002','Attended'),
(1,'Shrawani Patil',  'shrawani@email.com', '9876543212','SNJB CoE','TF2025003','Attended'),
(1,'Raj Mehta',       'raj@email.com',      '9876543213','PVPIT',   'TF2025004','Absent'),
(2,'Priya Desai',     'priya@email.com',    '9876543214','SNJB CoE','CN2025001','Attended'),
(2,'Arjun Kumar',     'arjun@email.com',    '9876543215','SNJB CoE','CN2025002','Attended'),
(3,'Sneha Joshi',     'sneha@email.com',    '9876543216','SNJB CoE','WJ2025001','Attended'),
(3,'Vivek Chaudhari', 'vivek@email.com',    '9876543217','SNJB CoE','WJ2025002','Attended'),
(4,'Aadi Shah',       'aadi@email.com',     '9876543210','SNJB CoE','SD2025001','Registered'),
(5,'Rashmi Shah',     'rashmi@email.com',   '9876543211','SNJB CoE','AI2025001','Registered'),
(6,'Shrawani Patil',  'shrawani@email.com', '9876543212','SNJB CoE','HK2025001','Registered');
