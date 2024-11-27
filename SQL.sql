CREATE DATABASE metro_dw;
USE metro_dw;



SELECT * FROM metro_dw.`customers_data (1)`;
SELECT * FROM metro_dw.`products_data (1)`;

-- Start schema
-- Create 'product' table
CREATE TABLE product (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

-- Create 'customer' table
CREATE TABLE customer (
    customer_id INT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL
);

-- Create 'store' table
CREATE TABLE store (
    store_id INT PRIMARY KEY,
    store_name VARCHAR(255) NOT NULL
);

-- Create 'supplier' table
CREATE TABLE supplier (
    supplier_id INT PRIMARY KEY,
    supplier_name VARCHAR(255) NOT NULL
);

-- Create 'date' table
CREATE TABLE date (
    time_id INT PRIMARY KEY,
    t_date DATE NOT NULL,
    weekend BOOLEAN NOT NULL,
    half_of_year ENUM('First', 'Second') NOT NULL,
    month INT NOT NULL,
    quarter INT NOT NULL,
    year INT NOT NULL
);

-- Create 'sales' fact table
CREATE TABLE sales (
    transaction_id INT PRIMARY KEY,
    product_id INT NOT NULL,
    customer_id INT NOT NULL,
    supplier_id INT NOT NULL,
    store_id INT NOT NULL,
    time_id INT NOT NULL,
    quantity INT NOT NULL,
    total_sale DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id),
    FOREIGN KEY (time_id) REFERENCES date(time_id)
);

-- Query 1
SELECT 
    p.product_name,
    SUM(s.total_sale) AS total_revenue,
    EXTRACT(MONTH FROM d.t_date) AS month,
    CASE
        WHEN d.weekend = TRUE THEN 'Weekend'
        ELSE 'Weekday'
    END AS day_type
FROM sales s
JOIN product p ON s.product_id = p.product_id
JOIN date d ON s.time_id = d.time_id
WHERE EXTRACT(YEAR FROM d.t_date) = 2019 -- Specify the year
GROUP BY p.product_name, EXTRACT(MONTH FROM d.t_date), day_type
ORDER BY total_revenue DESC
LIMIT 5;


-- Query 2
SELECT 
    st.store_id,
    st.store_name,  -- Use st.store_name instead of s.store_name
    QUARTER(d.t_date) AS quarter,
    SUM(s.total_sale) AS total_revenue,
    LAG(SUM(s.total_sale), 1) OVER (PARTITION BY st.store_id ORDER BY QUARTER(d.t_date)) AS previous_quarter_revenue,
    (SUM(s.total_sale) - LAG(SUM(s.total_sale), 1) OVER (PARTITION BY st.store_id ORDER BY QUARTER(d.t_date))) / 
    LAG(SUM(s.total_sale), 1) OVER (PARTITION BY st.store_id ORDER BY QUARTER(d.t_date)) * 100 AS revenue_growth_rate
FROM sales s
JOIN store st ON s.store_id = st.store_id  -- Corrected this join
JOIN date d ON s.time_id = d.time_id
WHERE EXTRACT(YEAR FROM d.t_date) = 2019
GROUP BY st.store_id, QUARTER(d.t_date)  -- Corrected group by clause to reference st.store_id
ORDER BY st.store_id, QUARTER(d.t_date);


-- query 3
SELECT 
    st.store_name,
    sup.supplier_name,
    p.product_name,
    SUM(s.total_sale) AS total_sales_contribution
FROM sales s
JOIN product p ON s.product_id = p.product_id
JOIN store st ON s.store_id = st.store_id
JOIN supplier sup ON s.supplier_id = sup.supplier_id
GROUP BY st.store_name, sup.supplier_name, p.product_name
ORDER BY st.store_name, sup.supplier_name, p.product_name;


-- query 4
SELECT 
    p.product_name,
    SUM(s.total_sale) AS total_sales,
    CASE
        WHEN EXTRACT(MONTH FROM d.t_date) BETWEEN 3 AND 5 THEN 'Spring'
        WHEN EXTRACT(MONTH FROM d.t_date) BETWEEN 6 AND 8 THEN 'Summer'
        WHEN EXTRACT(MONTH FROM d.t_date) BETWEEN 9 AND 11 THEN 'Fall'
        WHEN EXTRACT(MONTH FROM d.t_date) = 12 OR EXTRACT(MONTH FROM d.t_date) BETWEEN 1 AND 2 THEN 'Winter'
        ELSE 'Unknown'
    END AS season
FROM sales s
JOIN product p ON s.product_id = p.product_id
JOIN date d ON s.time_id = d.time_id
GROUP BY p.product_name, season
ORDER BY season, total_sales DESC;


-- query 5
SELECT 
    st.store_name,  -- Corrected this to use 'st.store_name'
    sup.supplier_name, 
    EXTRACT(MONTH FROM d.t_date) AS month, 
    SUM(s.total_sale) AS total_sales, 
    LAG(SUM(s.total_sale), 1) OVER (PARTITION BY st.store_id, sup.supplier_id ORDER BY EXTRACT(MONTH FROM d.t_date)) AS previous_month_sales,
    (SUM(s.total_sale) - LAG(SUM(s.total_sale), 1) OVER (PARTITION BY st.store_id, sup.supplier_id ORDER BY EXTRACT(MONTH FROM d.t_date))) / 
    LAG(SUM(s.total_sale), 1) OVER (PARTITION BY st.store_id, sup.supplier_id ORDER BY EXTRACT(MONTH FROM d.t_date)) * 100 AS sales_volatility_percentage
FROM sales s
JOIN store st ON s.store_id = st.store_id  -- Using 'st.store_name' from the 'store' table
JOIN supplier sup ON s.supplier_id = sup.supplier_id
JOIN date d ON s.time_id = d.time_id
GROUP BY st.store_id, sup.supplier_id, EXTRACT(MONTH FROM d.t_date)  -- Corrected 's.store_id' to 'st.store_id'
ORDER BY st.store_name, sup.supplier_name, month;


-- query 6
SELECT 
    p1.product_name AS product_1,
    p2.product_name AS product_2,
    COUNT(*) AS purchase_count
FROM sales s1
JOIN sales s2 ON s1.transaction_id = s2.transaction_id  -- Self-join to find product pairs within the same transaction
JOIN product p1 ON s1.product_id = p1.product_id
JOIN product p2 ON s2.product_id = p2.product_id
WHERE s1.product_id != s2.product_id  -- Ensure the products are different (avoid pairing the same product with itself)
GROUP BY product_1, product_2
ORDER BY purchase_count DESC
LIMIT 5;

-- query 7
SELECT 
    st.store_name,
    sup.supplier_name,
    p.product_name,
    EXTRACT(YEAR FROM d.t_date) AS year,
    SUM(s.total_sale) AS total_sales
FROM sales s
JOIN store st ON s.store_id = st.store_id
JOIN supplier sup ON s.supplier_id = sup.supplier_id
JOIN product p ON s.product_id = p.product_id
JOIN date d ON s.time_id = d.time_id
GROUP BY st.store_name, sup.supplier_name, p.product_name, EXTRACT(YEAR FROM d.t_date) 
WITH ROLLUP
ORDER BY st.store_name, sup.supplier_name, p.product_name, year;


-- query 8
SELECT 
    p.product_name,
    SUM(CASE WHEN EXTRACT(MONTH FROM d.t_date) <= 6 THEN s.total_sale ELSE 0 END) AS H1_revenue,
    SUM(CASE WHEN EXTRACT(MONTH FROM d.t_date) > 6 THEN s.total_sale ELSE 0 END) AS H2_revenue,
    SUM(CASE WHEN EXTRACT(MONTH FROM d.t_date) <= 6 THEN s.quantity ELSE 0 END) AS H1_quantity,
    SUM(CASE WHEN EXTRACT(MONTH FROM d.t_date) > 6 THEN s.quantity ELSE 0 END) AS H2_quantity,
    SUM(s.total_sale) AS total_revenue,
    SUM(s.quantity) AS total_quantity
FROM sales s
JOIN product p ON s.product_id = p.product_id
JOIN date d ON s.time_id = d.time_id
GROUP BY p.product_name
ORDER BY p.product_name;


-- query 9
SELECT 
    subquery.product_name,
    subquery.t_date,
    subquery.daily_sales,
    subquery.avg_daily_sales,
    CASE
        WHEN subquery.daily_sales > 2 * subquery.avg_daily_sales THEN 'Outlier'
        ELSE 'Normal'
    END AS sales_status
FROM (
    SELECT 
        s.product_id,
        p.product_name,  -- Include product name in the subquery
        d.t_date,
        SUM(s.total_sale) AS daily_sales,
        AVG(SUM(s.total_sale)) OVER (PARTITION BY s.product_id) AS avg_daily_sales
    FROM sales s
    JOIN product p ON s.product_id = p.product_id
    JOIN date d ON s.time_id = d.time_id
    GROUP BY s.product_id, p.product_name, d.t_date
) AS subquery
WHERE subquery.daily_sales > 2 * subquery.avg_daily_sales
ORDER BY subquery.product_name, subquery.t_date;



-- query 10
CREATE VIEW STORE_QUARTERLY_SALES AS
SELECT 
    st.store_name,
    EXTRACT(QUARTER FROM d.t_date) AS quarter,
    SUM(s.total_sale) AS total_sales
FROM sales s
JOIN store st ON s.store_id = st.store_id
JOIN date d ON s.time_id = d.time_id
GROUP BY st.store_name, EXTRACT(QUARTER FROM d.t_date)
ORDER BY st.store_name, quarter;










