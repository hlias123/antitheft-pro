const sqlite3 = require('sqlite3').verbose();

console.log('Opening database...');
const db = new sqlite3.Database('./database.db', (err) => {
    if (err) {
        console.error('Database connection error:', err);
        return;
    }
    console.log('Connected to database');
    
    db.get("SELECT COUNT(*) as count FROM verification_codes", (err, row) => {
        if (err) {
            console.error('Query error:', err);
        } else {
            console.log('Total verification codes:', row.count);
        }
        
        db.close((err) => {
            if (err) {
                console.error('Close error:', err);
            } else {
                console.log('Database closed');
            }
        });
    });
});