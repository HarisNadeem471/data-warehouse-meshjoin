//import java.io.*;
//import java.sql.*;
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//public class MySQLConnectionTest {
//    private static final int CHUNK_SIZE = 50; // Number of transaction rows per chunk
//    private static final int PARTITION_SIZE = 100; // Number of customers (adjusted to 100, can be changed based on data)
//
//    public static void main(String[] args) {
//        // Database credentials
//        String dbUrl = "jdbc:mysql://localhost:3306/metro_dw";
//        String dbUser = "root";
//        String dbPassword = "12345";
//
//        // Paths for input and output files
//        String transactionsFile = "C:\\Users\\Muhammad\\Desktop\\Datawarehouse_metro\\MySQLConnectionTest\\src\\transactions.csv"; // Input file path
//        String enrichedOutputFile = "enriched_transactions.csv"; // Output file path
//
//        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
//            System.out.println("Connected to MySQL database!");
//
//            // Create output file writer
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter(enrichedOutputFile))) {
//                // Write header to the output file
//                writer.write("TRANSACTION_ID,PRODUCT_ID,PRODUCT_NAME,PRODUCT_PRICE,CUSTOMER_ID,CUSTOMER_NAME,GENDER,STORE_ID,STORE_NAME,SUPPLIER_ID,SUPPLIER_NAME,TIME_ID,TIME,DATE,WEEKEND,HALF_OF_YEAR,MONTH,QUARTER,YEAR,QUANTITY,TOTAL_SALES");
//                writer.newLine();
//
//                int transactionOffset = 0;
//                boolean moreTransactions = true;
//
//                // Process transactions in chunks
//                while (moreTransactions) {
//                    // Load the current chunk of transactions
//                    List<Map<String, String>> transactionChunk = loadTransactionChunk(transactionsFile, CHUNK_SIZE, transactionOffset);
//
//                    // If no transactions are left, stop the loop
//                    if (transactionChunk.isEmpty()) {
//                        moreTransactions = false;
//                        break;
//                    }
//
//                    // Load all customer and product data into memory (disk buffer)
//                    Map<Integer, String[]> customerPartition = loadCustomerPartition(conn, PARTITION_SIZE, 0); // Load all customers
//                    Map<Integer, String[]> productPartition = loadProductPartition(conn, PARTITION_SIZE, 0); // Load all products
//
//                    // Process each transaction in the chunk
//                    for (Map<String, String> transaction : transactionChunk) {
//                        int productId = Integer.parseInt(transaction.get("PRODUCT_ID"));
//                        int customerId = Integer.parseInt(transaction.get("CUSTOMER_ID"));
//                        int timeId = Integer.parseInt(transaction.get("time_id"));  // Extract time_id directly from the transaction
//
//                        String[] customerData = customerPartition.get(customerId);
//                        String[] productData = productPartition.get(productId);
//
//                        // Debugging logs to identify missing data
//                        if (customerData == null) {
//                            System.out.println("Missing customer data for CUSTOMER_ID: " + customerId);
//                        }
//                        if (productData == null) {
//                            System.out.println("Missing product data for PRODUCT_ID: " + productId);
//                        }
//
//                        // If both customer and product data exist, enrich the transaction
//                        if (customerData != null && productData != null) {
//                            // Enrich transaction data
//                            String priceString = productData[1]; // productPrice from product data
//                            double price = Double.parseDouble(priceString.replace("$", "").trim());
//
//                            int quantity = Integer.parseInt(transaction.get("QUANTITY"));
//                            double totalSales = price * quantity;
//
//                            // Extract time information from ORDER_DATE (in the format "yyyy-MM-dd HH:mm:ss")
//                            String orderDateStr = transaction.get("ORDER_DATE");
//                            String timeValue = extractTimeFromDate(orderDateStr);
//
//                            // Extract date information from ORDER_DATE
//                            LocalDate orderDate = parseDate(orderDateStr);
//
//                            // Calculate additional date-related information
//                            String date = orderDate.toString(); // Date in YYYY-MM-DD format
//                            boolean isWeekend = isWeekend(orderDate);
//                            int halfOfYear = getHalfOfYear(orderDate);
//                            int month = orderDate.getMonthValue(); // Month as number (1-12)
//                            int quarter = getQuarter(orderDate); // Quarter (1-4)
//                            int year = orderDate.getYear(); // Year
//
//                            // Write enriched transaction to the output file
//                            writer.write(transaction.get("ORDER_ID") + "," + // Transaction ID
//                                    productId + "," +
//                                    productData[0] + "," + // Product Name
//                                    String.format("%.2f", price) + "," +
//                                    customerId + "," +
//                                    customerData[0] + "," + // Customer Name
//                                    customerData[1] + "," + // Customer Gender
//                                    productData[4] + "," + // Store ID
//                                    productData[5] + "," + // Store Name
//                                    productData[2] + "," + // Supplier ID
//                                    productData[3] + "," + // Supplier Name
//                                    timeId + "," + // Time ID
//                                    timeValue + "," + // Actual Time (extracted from ORDER_DATE)
//                                    date + "," + // Date
//                                    isWeekend + "," + // Weekend (true/false)
//                                    halfOfYear + "," + // Half of Year (1 or 2)
//                                    month + "," + // Month
//                                    quarter + "," + // Quarter
//                                    year + "," + // Year
//                                    quantity + "," +
//                                    String.format("%.2f", totalSales));
//                            writer.newLine();
//                        }
//                    }
//
//                    // Increment the offset to move to the next chunk
//                    transactionOffset += CHUNK_SIZE;
//                }
//
//                System.out.println("MESHJOIN completed! Enriched data saved to " + enrichedOutputFile);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Extract time (HH:mm:ss) from the ORDER_DATE
//    private static String extractTimeFromDate(String dateStr) {
//        String time = dateStr.split(" ")[1]; // Extract the time part (HH:mm:ss)
//        return time;
//    }
//
//    // Parse the ORDER_DATE in yyyy-MM-dd HH:mm:ss format
//    private static LocalDate parseDate(String dateStr) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
//        return dateTime.toLocalDate(); // Extract the date part only
//    }
//
//    // Check if the date is a weekend (Saturday/Sunday)
//    private static boolean isWeekend(LocalDate date) {
//        DayOfWeek dayOfWeek = date.getDayOfWeek();
//        return (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
//    }
//
//    // Calculate the half of the year (1 = first half, 2 = second half)
//    private static int getHalfOfYear(LocalDate date) {
//        int month = date.getMonthValue();
//        return (month <= 6) ? 1 : 2;
//    }
//
//    // Calculate the quarter of the year (1-4)
//    private static int getQuarter(LocalDate date) {
//        int month = date.getMonthValue();
//        if (month <= 3) return 1;
//        else if (month <= 6) return 2;
//        else if (month <= 9) return 3;
//        else return 4;
//    }
//
//    // Load chunks of transaction data from CSV file
//    private static List<Map<String, String>> loadTransactionChunk(String filePath, int chunkSize, int offset) throws IOException {
//        List<Map<String, String>> chunk = new ArrayList<>();
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            int count = 0;
//            int currentRow = 0;
//
//            // Skip the header row
//            reader.readLine();
//
//            while ((line = reader.readLine()) != null) {
//                if (currentRow < offset) {
//                    currentRow++; // Skip rows until the offset
//                    continue;
//                }
//
//                if (count >= chunkSize) {
//                    break;
//                }
//
//                String[] fields = line.split(",");
//                Map<String, String> transaction = new HashMap<>();
//                transaction.put("ORDER_ID", fields[0]); // Transaction ID
//                transaction.put("ORDER_DATE", fields[1]);
//                transaction.put("PRODUCT_ID", fields[2]);
//                transaction.put("CUSTOMER_ID", fields[3]);
//                transaction.put("QUANTITY", fields[4]);
//                transaction.put("time_id", fields[5]); // Time ID
//
//                chunk.add(transaction);
//                count++;
//                currentRow++;
//            }
//        }
//        return chunk;
//    }
//
//    // Load customer data partition from MySQL (with OFFSET and LIMIT)
//    private static Map<Integer, String[]> loadCustomerPartition(Connection conn, int partitionSize, int offset) throws SQLException {
//        Map<Integer, String[]> customerPartition = new HashMap<>();
//        String query = "SELECT * FROM `customers_data (1)` LIMIT ? OFFSET ?";
//        try (PreparedStatement stmt = conn.prepareStatement(query)) {
//            stmt.setInt(1, partitionSize);  // Set LIMIT
//            stmt.setInt(2, offset);  // Set OFFSET
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                customerPartition.put(rs.getInt("CUSTOMER_ID"),
//                        new String[]{rs.getString("CUSTOMER_NAME"), rs.getString("GENDER")});
//            }
//        }
//        return customerPartition;
//    }
//
//    // Load product data partition from MySQL (with OFFSET and LIMIT)
//    private static Map<Integer, String[]> loadProductPartition(Connection conn, int partitionSize, int offset) throws SQLException {
//        Map<Integer, String[]> productPartition = new HashMap<>();
//        String query = "SELECT productID, productName, productPrice, supplierID, supplierName, storeID, storeName FROM `products_data (1)` LIMIT ? OFFSET ?";
//        try (PreparedStatement stmt = conn.prepareStatement(query)) {
//            stmt.setInt(1, partitionSize);  // Set LIMIT
//            stmt.setInt(2, offset);  // Set OFFSET
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                productPartition.put(rs.getInt("productID"),
//                        new String[]{
//                                rs.getString("productName"),
//                                rs.getString("productPrice"),
//                                rs.getString("supplierID"),
//                                rs.getString("supplierName"),
//                                rs.getString("storeID"),
//                                rs.getString("storeName")
//                        });
//            }
//        }
//        return productPartition;
//    }
//}
