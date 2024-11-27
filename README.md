Data Warehouse Project with Mesh Join and Star Schema
This project demonstrates the creation of a data warehouse using a star schema. It involves data integration, ETL (Extract, Transform, Load) operations, and querying for insights. The project leverages MySQL, Java (Eclipse IDE), and Python for various tasks.


Data Sources:

Transactions Data: Provided as a CSV file (transactions.csv).
Customers Data: Provided as a CSV file and imported into MySQL.
Products Data: Provided as a CSV file and imported into MySQL.
Mesh Join: A custom Java program processes transaction data by joining it with customers and products data to create an enriched data file.

Star Schema: A star schema for the data warehouse includes:

Dimension Tables: product, customer, supplier, store, and date.
Fact Table: sales.
Queries: Several advanced queries analyze sales performance, product trends, and more, as described in the project.


Prerequisites and Installation
Tools and Libraries Required:
MySQL Server: Install from MySQL Official Website.
Eclipse IDE: Install from Eclipse Downloads.
Java Development Kit (JDK): Ensure JDK is installed and configured.
Python (optional): For cleaning CSV files, install Python.


Setting Up the Project
1. Setting Up MySQL
Create a database metro_dw in MySQL.
Import the provided customer_data.csv and product_data.csv into MySQL


2. Connecting MySQL with Eclipse
Add the MySQL JDBC Driver to your project:
Download the MySQL Connector JAR file from MySQL Connector/J.
In Eclipse, right-click your project > Build Path > Add External JARs > Select the downloaded JAR file.
3. Accessing and Processing Data in Eclipse
Use the provided transactions.csv file directly in Java code.
Implement a Mesh Join using Java:
Reads transactions.csv.
Joins transaction data with customer and product tables from MySQL.
Outputs an enriched data file (enriched_transactions.csv).
4. Populating the Star Schema
The Star Schema consists of:
Dimension Tables: product, customer, supplier, store, date.
Fact Table: sales.
5. Querying the Data Warehouse
Execute analytical queries on the star schema using MySQL.




Key Steps in the Project
1. Mesh Join
The mesh join integrates transaction data with customers and products to create an enriched data file.
2. Data Population
Enriched data is inserted into the dimension and fact tables using a Java program.
3. Query Descriptions
Top Revenue-Generating Products:

Identify the top 5 products generating the highest revenue, categorized by weekdays and weekends.
Store Revenue Growth:

Analyze quarterly revenue growth for stores in 2017.
Supplier Contribution by Product:

Show sales contributions of suppliers broken down by product and store.
Seasonal Product Sales:

Analyze total sales by seasonal periods: Spring, Summer, Fall, and Winter.
Monthly Revenue Volatility:

Calculate month-to-month revenue volatility for each store and supplier pair.
Product Affinity Analysis:

Identify products frequently bought together across transactions.
Yearly Revenue Trends:

Aggregate yearly revenue by store, supplier, and product using ROLLUP.
H1 vs. H2 Product Sales:

Compare total revenue and sales volume for products in the first and second halves of the year.
Daily Sales Outliers:

Flag days with sales exceeding twice the average daily sales for each product.
Quarterly Sales View:

Create a view for quick retrieval of store-specific quarterly sales trends.




How to Run the Project
Set Up MySQL:

Create the metro_dw database.
Import customers_data.csv and products_data.csv into MySQL.
Run Mesh Join:

Execute the Java program to process transactions.csv and generate enriched_transactions.csv.
Populate Star Schema:

Use the provided Java program to insert data into the star schema tables.
Query the Data Warehouse:

Use the queries provided to analyze the data and generate insights.