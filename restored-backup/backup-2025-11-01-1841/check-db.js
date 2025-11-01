const sqlite3 = require('sqlite3').verbose();
const db = new sqlite3.Database('./database.db');

console.log('Checking database tables...');

db.all("SELECT name FROM sqlite_master WHERE type='table'", (err, rows) => {
    if (err) {
        console.error('Error:', err);
    } else {
        console.log('Tables in database:');
        rows.forEach(row => console.log('- ' + row.name));
        
        // Check verification_codes table specifically
        db.all("SELECT * FROM verification_codes WHERE user_id = '790ae526-7a13-4af4-9feb-749b4d88c81e' ORDER BY created_at DESC", (err, codes) => {
            if (err) {
                console.error('Error checking verification_codes:', err);
            } else {
                console.log('\nVerification codes for user:');
                codes.forEach(code => {
                    console.log({
                        code: code.code,
                        expires_at: code.expires_at,
                        used: code.used,
                        created_at: code.created_at
                    });
                });
            }
            db.close();
        });
    }
});