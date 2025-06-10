#!/bin/bash

echo "🗄️  Setting up MariaDB database for Visakh Refinery Portal"
echo "================================================="

# Test if we can connect using storm user
if mysql -u storm -e "SELECT 1;" &> /dev/null; then
    echo "✅ Connected to MariaDB successfully"
    
    echo "📁 Creating database..."
    mysql -u storm -e "CREATE DATABASE IF NOT EXISTS refweb_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    
    echo "👤 Creating user..."
    mysql -u storm -e "CREATE USER IF NOT EXISTS 'portal_user'@'localhost' IDENTIFIED BY 'portal_password';"
    
    echo "🔑 Granting privileges..."
    mysql -u storm -e "GRANT ALL PRIVILEGES ON refweb_portal.* TO 'portal_user'@'localhost';"
    mysql -u storm -e "FLUSH PRIVILEGES;"
    
    echo "✅ Database setup completed!"
    echo ""
    echo "📊 Databases:"
    mysql -u storm -e "SHOW DATABASES;" | grep refweb_portal
    
    echo ""
    echo "🧪 Testing connection with new user..."
    if mysql -u portal_user -pportal_password -e "USE refweb_portal; SELECT 'Connection successful!' as test;" 2>/dev/null; then
        echo "✅ portal_user can connect successfully!"
    else
        echo "❌ Failed to connect with portal_user"
    fi
    
else
    echo "❌ Cannot connect to MariaDB"
    echo "Try these troubleshooting steps:"
    echo "1. Make sure MariaDB is running: brew services start mariadb"
    echo "2. Try connecting manually: mysql -u storm"
    echo "3. If that fails, try: mysql -u root"
    echo "4. Reset password if needed: brew services stop mariadb && mariadb-install-db"
fi 