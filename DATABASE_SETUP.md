# Database Setup Guide

## MS SQL Server Local Setup

Since we removed MS SQL Server from Docker containers, you need to set up MS SQL Server locally on your machine.

### Option 1: SQL Server Express (Recommended)

1. **Download SQL Server Express:**
   - Go to: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
   - Download SQL Server Express (free)

2. **Install SQL Server Express:**
   - Run the installer
   - Choose "Basic" installation
   - Note the instance name (usually `SQLEXPRESS`)

3. **Enable TCP/IP (if needed):**
   - Open SQL Server Configuration Manager
   - Go to SQL Server Network Configuration
   - Enable TCP/IP protocol
   - Restart SQL Server service

4. **Create Database:**
   ```sql
   CREATE DATABASE OnlineLibrary;
   ```

5. **Create Login:**
   ```sql
   CREATE LOGIN sa WITH PASSWORD = 'YourPassword123';
   ALTER SERVER ROLE sysadmin ADD MEMBER sa;
   ```

### Option 2: SQL Server LocalDB

1. **Install SQL Server LocalDB:**
   - Download from Microsoft website
   - Or install via Visual Studio installer

2. **Connect using:**
   ```
   Server: (localdb)\MSSQLLocalDB
   Database: OnlineLibrary
   ```

3. **Update application.properties:**
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost\\MSSQLLocalDB:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true
   ```

### Option 3: Docker Desktop (Alternative)

If you want to run SQL Server in Docker but separately:

```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=YourPassword123" \
   -p 1433:1433 --name sqlserver \
   -d mcr.microsoft.com/mssql/server:2022-latest
```

### Current Configuration

The application supports environment variables for database configuration:

#### Environment Variables:
- **DB_URL**: Database connection URL
- **DB_USERNAME**: Database username  
- **DB_PASSWORD**: Database password

#### Default Values (if environment variables not set):
- **URL:** jdbc:sqlserver://localhost:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true
- **Username:** sa
- **Password:** YourPassword123

#### Setting Environment Variables:

**Windows (Command Prompt):**
```cmd
set DB_URL=jdbc:sqlserver://localhost:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true
set DB_USERNAME=sa
set DB_PASSWORD=YourSecurePassword123
mvn spring-boot:run
```

**Windows (PowerShell):**
```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YourSecurePassword123"
mvn spring-boot:run
```

**Linux/macOS:**
```bash
export DB_URL="jdbc:sqlserver://localhost:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true"
export DB_USERNAME="sa"
export DB_PASSWORD="YourSecurePassword123"
mvn spring-boot:run
```

**Docker Environment:**
```bash
# Option 1: Export environment variables before docker-compose
export DB_URL="jdbc:sqlserver://host.docker.internal:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true"
export DB_USERNAME="sa"
export DB_PASSWORD="YourSecurePassword123"
docker-compose up

# Option 2: Create .env file (recommended)
# Copy .env.docker to .env and modify values
cp .env.docker .env
# Edit .env file with your database credentials
docker-compose up

# Option 3: Inline environment variables
DB_URL="jdbc:sqlserver://host.docker.internal:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true" \
DB_USERNAME="sa" \
DB_PASSWORD="YourSecurePassword123" \
docker-compose up
```

**Docker Compose with .env file:**
```bash
# 1. Create .env file in project root
echo "DB_URL=jdbc:sqlserver://host.docker.internal:1433;databaseName=OnlineLibrary;encrypt=false;trustServerCertificate=true" > .env
echo "DB_USERNAME=sa" >> .env
echo "DB_PASSWORD=YourSecurePassword123" >> .env

# 2. Run docker-compose (will automatically load .env)
docker-compose up
```

### Docker Application Connection

When running the application in Docker, it will connect to your local SQL Server using `host.docker.internal:1433`.

### Verify Connection

1. Start your local SQL Server
2. Create the `OnlineLibrary` database
3. Run the application:
   ```bash
   # Local development
   mvn spring-boot:run
   
   # Docker
   docker-compose up
   ```

### Troubleshooting

1. **Connection refused:**
   - Check if SQL Server is running
   - Verify TCP/IP is enabled
   - Check firewall settings

2. **Authentication failed:**
   - Verify username/password
   - Check if mixed authentication is enabled

3. **Database not found:**
   - Create the database manually
   - Or set `spring.jpa.hibernate.ddl-auto=create` temporarily
