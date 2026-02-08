-- 1. Setup the Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'librarydb')
BEGIN
    CREATE DATABASE librarydb;
END
GO

USE librarydb;
GO

-- 2. Create Books Table
IF OBJECT_ID('dbo.Books', 'U') IS NOT NULL DROP TABLE dbo.Books;
CREATE TABLE Books (
    ISBN NVARCHAR(20) PRIMARY KEY,
    Author NVARCHAR(100),
    Title NVARCHAR(150),
    NumOfCopies INT,
    Binding NVARCHAR(50),
    Description NVARCHAR(MAX), -- Replaced TEXT with NVARCHAR(MAX)
    Language NVARCHAR(50)
);

-- 3. Create Member Table
IF OBJECT_ID('dbo.Member', 'U') IS NOT NULL DROP TABLE dbo.Member;
CREATE TABLE Member (
    SSN INT PRIMARY KEY,
    MemberName NVARCHAR(100),
    Address NVARCHAR(200),
    Role NVARCHAR(50)
);

-- 4. Create Card Table
IF OBJECT_ID('dbo.Card', 'U') IS NOT NULL DROP TABLE dbo.Card;
CREATE TABLE Card (
    CardNumber INT PRIMARY KEY,
    CardName NVARCHAR(100),
    Photo VARBINARY(MAX), -- Replaced BLOB with VARBINARY(MAX)
    ExpirationDate DATE,
    SSN INT,
    FOREIGN KEY (SSN) REFERENCES Member(SSN) ON DELETE CASCADE
);

-- 5. Create Librarian Table
IF OBJECT_ID('dbo.Librarian', 'U') IS NOT NULL DROP TABLE dbo.Librarian;
CREATE TABLE Librarian (
    EMPLID INT PRIMARY KEY,
    Position NVARCHAR(100),
    LibrarianName NVARCHAR(100)
);

-- 6. Create CheckDescription Table
IF OBJECT_ID('dbo.CheckDescription', 'U') IS NOT NULL DROP TABLE dbo.CheckDescription;
CREATE TABLE CheckDescription (
    CDEMPLID INT,
    CDLibrarianName NVARCHAR(100),
    CDMemberName NVARCHAR(100),
    BookDescription NVARCHAR(MAX), -- Replaced TEXT
    CDISBN NVARCHAR(20),
    Position NVARCHAR(100),
    PRIMARY KEY (CDEMPLID, CDISBN),
    FOREIGN KEY (CDEMPLID) REFERENCES Librarian(EMPLID),
    FOREIGN KEY (CDISBN) REFERENCES Books(ISBN)
);

-- 7. Create CheckOut Table
IF OBJECT_ID('dbo.CheckOut', 'U') IS NOT NULL DROP TABLE dbo.CheckOut;
CREATE TABLE CheckOut (
    COMemberName NVARCHAR(100),
    COMemberNumber INT,
    COBookTitle NVARCHAR(150),
    COISBN NVARCHAR(20),
    CheckoutDate DATE,
    ReturnDeadline DATE,
    PRIMARY KEY (COMemberNumber, COISBN),
    FOREIGN KEY (COISBN) REFERENCES Books(ISBN),
    FOREIGN KEY (COMemberNumber) REFERENCES Card(CardNumber)
);
GO