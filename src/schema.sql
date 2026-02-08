-- Active: 1747013265788@@127.0.0.1@3306@librarydatabase
--Tested by Jessica Chen, Abul Hassan, Jason Lopez(first and initial time)
CREATE DATABASE librarydb;

USE librarydb;

CREATE TABLE Books (
    ISBN VARCHAR(20) PRIMARY KEY,
    Author VARCHAR(100),
    Title VARCHAR(150),
    NumOfCopies INT,
    Binding VARCHAR(50),
    Description TEXT,
    Language VARCHAR(50)
);

CREATE TABLE Member (
    SSN INT PRIMARY KEY,
    MemberName VARCHAR(100),
    Address VARCHAR(200),
    Role VARCHAR(50)
);

CREATE TABLE Card (
    CardNumber INT PRIMARY KEY,
    CardName VARCHAR(100),
    Photo BLOB,
    ExpirationDate DATE,
    SSN INT,
    FOREIGN KEY (SSN) REFERENCES Member(SSN)
);

CREATE TABLE Librarian (
    EMPLID INT PRIMARY KEY,
    Position VARCHAR(100),
    LibrarianName VARCHAR(100)
);

CREATE TABLE CheckDescription (
    CDEMPLID INT,
    CDLibrarianName VARCHAR(100),
    CDMemberName VARCHAR(100),
    BookDescription TEXT,
    CDISBN VARCHAR(20),
    Position VARCHAR(100),
    PRIMARY KEY (CDEMPLID, CDISBN),
    FOREIGN KEY (CDEMPLID) REFERENCES Librarian(EMPLID),
    FOREIGN KEY (CDISBN) REFERENCES Books(ISBN)
);

CREATE TABLE CheckOut (
    COMemberName VARCHAR(100),
    COMemberNumber INT,
    COBookTitle VARCHAR(150),
    COISBN VARCHAR(20),
    CheckoutDate DATE,
    ReturnDeadline DATE,
    PRIMARY KEY (COMemberNumber, COISBN),
    FOREIGN KEY (COISBN) REFERENCES Books(ISBN),
    FOREIGN KEY (COMemberNumber) REFERENCES Card(CardNumber)
);
