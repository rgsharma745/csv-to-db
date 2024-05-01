# csv-to-db

## Overview
`csv-to-db` is a Java-based utility that automates the process of reading CSV files from a specified directory, creating corresponding tables in a PostgreSQL database, and uploading the data into these tables. This project streamlines the data migration process and ensures that data stored in CSV format can be easily transferred to a structured database system.

## Features
- **Automated CSV Reading**: Batch reads multiple CSV files from a designated folder.
- **Dynamic Table Creation**: Generates a new table for each CSV file with appropriate data types.
- **Data Upload**: Efficiently uploads CSV data into the PostgreSQL database tables.
- **Error Handling**: Provides robust error handling during the read and upload processes.

## Prerequisites
- Java JDK 22
- PostgreSQL Database
- Maven (for building and managing the project)

## Installation
1. Clone the repository:
   git clone https://github.com/rgsharma745/csv-to-db.git
2. Navigate to the project directory: cd csv-to-db
3. Build the project with Maven:
4. mvn clean install


## Usage
1. Update the `application.properties` file with your PostgreSQL database credentials and the path to the CSV directory.
2. Run the application: java -jar target/csv-to-db-1.0-SNAPSHOT.jar


## Configuration
The `application.properties` file contains the following configurations:
- `csv.folder.path`: The path to the folder containing the CSV files.
- `spring.datasource.url`: The JDBC URL of the PostgreSQL database.
- `spring.datasource.jdbcUrl`: The JDBC URL of the PostgreSQL database.
- `spring.datasource.username`: The username for the database.
- `spring.datasource.password`: The password for the database.

## Contributing
Contributions are welcome! Please feel free to submit pull requests or create issues for bugs and feature requests.

## License
This project is open source and available under the MIT License.

## Acknowledgments
- Thanks to all the contributors who have helped with the development of this project.
- Special thanks to the PostgreSQL community for their robust database management system.
