#!/bin/bash

echo "VRP WhatsNew Data Migration Script"
echo "=================================="

# Check if MariaDB is running
if ! brew services list | grep mariadb | grep started > /dev/null; then
    echo "❌ MariaDB is not running. Starting MariaDB..."
    brew services start mariadb
    sleep 3
fi

# Test database connection
if ! mysql -u storm -pstormpass -e "USE refweb_portal;" 2>/dev/null; then
    echo "❌ Cannot connect to database. Please check your credentials."
    exit 1
fi

echo "✅ Connected to database successfully"

# Show current data count
echo -e "\n📊 Current data in vrp_whatsnew table:"
mysql -u storm -pstormpass refweb_portal -e "SELECT COUNT(*) as 'Current Records' FROM vrp_whatsnew;"

# Ask user for confirmation
echo -e "\n🔄 This will migrate data from migration_data.csv to the database."
echo "The migration will add 6 historical records from 2012."
read -p "Do you want to proceed? (y/N): " confirm

if [[ $confirm =~ ^[Yy]$ ]]; then
    echo -e "\n🚀 Starting migration..."
    
    # Execute the migration SQL
    if mysql -u storm -pstormpass refweb_portal < migrate_csv_data.sql; then
        echo "✅ Migration completed successfully!"
        
        # Show new data count
        echo -e "\n📊 Data after migration:"
        mysql -u storm -pstormpass refweb_portal -e "SELECT COUNT(*) as 'Total Records' FROM vrp_whatsnew;"
        
        # Show migrated records
        echo -e "\n📋 Migrated records:"
        mysql -u storm -pstormpass refweb_portal -e "SELECT ID, TITLE, VALIDFROM, VALIDTO, PRIORITY, ENABLED FROM vrp_whatsnew ORDER BY ID;"
        
    else
        echo "❌ Migration failed! Please check the error messages above."
        exit 1
    fi
else
    echo "❌ Migration cancelled by user."
    exit 0
fi

echo -e "\n🎉 Migration process completed!"
echo "You can now view the migrated data in the application at http://localhost:8080/whatsnew/list" 