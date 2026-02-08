-- Active: 1747013265788@@127.0.0.1@3306@librarydatabase
-- Active: 1746799051317@@127.0.0.1@3306@mysql

--Tested by Fahim Tanvir and Khalid Issa(Second run)
-- Initial Data. Feel free to use these to test things around at the start.(Fahim)
INSERT INTO Books (ISBN, Author, Title, NumOfCopies, Binding, Description, Language) VALUES
('978-1234567890', 'J.K. Rowling', 'Harry Potter and the Sorcerer''s Stone', 5, 'Hardcover', 'A young wizard''s journey begins.', 'English');

INSERT INTO Member (SSN, MemberName, Address, Role) VALUES
(101, 'Alice Johnson', '123 Maple St', 'Professor'),
(102, 'Bob Smith', '456 Oak Ave', 'Student');

INSERT INTO Card (CardNumber, CardName, Photo, ExpirationDate, SSN) VALUES
(201, 'Alice Johnson', NULL, '2025-12-31', 101),
(202, 'Bob Smith', NULL, '2024-11-30', 102);

INSERT INTO Librarian (EMPLID, Position, LibrarianName) VALUES
(301, 'Head Librarian', 'Emily Brown'),
(302, 'Assistant Librarian', 'Michael Lee');

INSERT INTO CheckDescription (CDEMPLID, CDLibrarianName, CDMemberName, BookDescription, CDISBN, Position) VALUES
(301, 'Emily Brown', 'Alice Johnson', 'A young wizard''s journey begins.', '978-1234567890', 'Head Librarian');
 
 
 
 --I used this to see if overdo book detection works(Khalid)
INSERT INTO CheckOut (COMemberName, COMemberNumber, COBookTitle, COISBN, CheckoutDate, ReturnDeadline) VALUES
('Alice Johnson', 201, 'Harry Potter and the Sorcerer''s Stone', '978-1234567890', '2025-05-01', '2025-05-15');

-- This will be used for presentation. Will demonstrated the 5 book limit.(Khalid)
INSERT INTO Member (SSN, MemberName, Address, Role) VALUES
(103, 'Khalid', '789 Pine St, Othertown, USA', 'Student');
INSERT INTO Card (CardNumber, CardName, Photo, ExpirationDate, SSN) VALUES
(203, 'Khalid', NULL, '2027-05-11', 103); 
