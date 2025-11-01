const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const dbPath = path.join(__dirname, 'database.db');
console.log('Database path:', dbPath);

const db = new sqlite3.Database(dbPath, sqlite3.OPEN_READWRITE, (err) => {
    if (err) {
        console.error('❌ Cannot open database:', err.message);
        return;
    }
    console.log('✅ Connected to SQLite database');
    
    // Check if verification_codes table exists
    db.get("SELECT name FROM sqlite_master WHERE type='table' AND name='verification_codes'", (err, row) => {
        if (err) {
            console.error('❌ Error checking table:', err);
            return;
        }
        
        if (!row) {
            console.log('❌ verification_codes table does not exist!');
            db.close();
            return;
        }
        
        console.log('✅ verification_codes table exists');
        
        // Get all verification codes
        db.all("SELECT * FROM verification_codes ORDER BY created_at DESC LIMIT 10", (err, rows) => {
            if (err) {
                console.error('❌ Error fetching codes:', err);
            } else {
                console.log(`📊 Found ${rows.length} verification codes:`);
                rows.forEach((row, index) => {
                    console.log(`${index + 1}. Code: ${row.code}, User: ${row.user_id}, Expires: ${row.expires_at}, Used: ${row.used}`);
                });
            }
            
            // Check specific user
            const userId = '9cad0f85-5815-412d-89a0-0a32f7be9e36';
            db.all("SELECT * FROM verification_codes WHERE user_id = ? ORDER BY created_at DESC", [userId], (err, userCodes) => {
                if (err) {
                    console.error('❌ Error fetching user codes:', err);
                } else {
                    console.log(`\n🔍 Codes for user ${userId}:`);
                    userCodes.forEach((code, index) => {
                        console.log(`${index + 1}. Code: ${code.code}, Expires: ${code.expires_at}, Used: ${code.used}, Created: ${code.created_at}`);
                    });
                }
                
                db.close((err) => {
                    if (err) {
                        console.error('❌ Error closing database:', err);
                    } else {
                        console.log('✅ Database connection closed');
                    }
                });
            });
        });
    });
});