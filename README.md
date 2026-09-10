MediaLab Documents

MediaLab Documents is a JavaFX desktop application developed for the Multimedia Technology course.
The application implements a simple document management system with different user roles, document categories, version control, document monitoring and persistent storage using JSON files.

Features
User Authentication

The application supports login functionality. Each user logs in using a username and password.

A default administrator account is available:

Username: medialab
Password: medialab_2025
User Roles

The system supports three user roles:

Simple User
Author
Administrator

Each role has different permissions.

Simple User

A simple user can:

View documents from the categories assigned to them.
Read only the latest version of each document.
Search documents by category, title or author.
Follow documents in order to be notified when a new version is available.
Remove followed documents from their monitoring list.
Author

An author has all the permissions of a simple user and can also:

Create new documents.
Edit existing documents.
Delete documents.
Access the latest version of a document and up to two previous versions.
Administrator

An administrator has all the permissions of an author and can also:

Add, edit and delete users.
Add, rename and delete document categories.
Assign document categories to users.
Delete all documents that belong to a removed category.
Document Management

Each document contains:

Title
Author name
Category
Creation date
Text content
Version number

When a document is created, its initial version number is 1.

When a document is edited, the previous version is not deleted. Instead, a new version is created automatically and the version number is increased by one.

Simple users can only see the latest version of a document, while authors and administrators can also access up to two previous versions.

Document Monitoring

Users can choose to follow documents.
If a followed document is edited and a new version is created, the user is notified the next time they log in to the application.

Users can also remove documents from their followed list.

Search Functionality

The application allows users to search documents based on:

Document category
Document title
Author name

The search results display the basic information of the matching documents, such as title, author, category, creation date and version number.

Data Storage

The application stores its data using JSON files.

The JSON files are stored inside a folder named:

medialab

When the application starts, it reads the JSON files and initializes the required objects in memory.

During execution, the application works with the data stored in memory.

Before the application terminates, the current state of the system is written back to the JSON files, so that the data is preserved between different executions.

Graphical User Interface

The graphical interface is implemented using JavaFX.

The main application window is titled:

MediaLab Documents

The GUI provides access to the available functions depending on the logged-in user's role.

The interface includes:

Login screen
Document search
Document viewing
Document creation and editing
Category management
User management
Followed document management
System information panel

The system information panel displays:

Total number of document categories
Total number of documents
Number of documents followed by the current user
Technologies Used
Java
JavaFX
JSON
Object-Oriented Programming
Javadoc
Project Structure

A typical project structure is:

src/
    

medialab/
    users.json
    categories.json
    documents.json

The exact class organization follows object-oriented design principles, separating the application logic, data models, GUI controllers and JSON storage handling.

How to Run
Open the project in a Java IDE, such as IntelliJ IDEA, Eclipse or NetBeans.
Make sure JavaFX is properly configured.
Run the main application class.
Log in using the default administrator account or another user created through the application.

Default administrator credentials:

Username: medialab
Password: medialab_2025
Notes

The application follows object-oriented design principles.
The data model is stored in JSON format and is restored when the application starts.
The graphical interface updates according to the actions and permissions of the current user.
