const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// Routes
app.get('/', (req, res) => {
    res.redirect('/app.html');
});

app.get('/health', (req, res) => {
    res.json({ status: 'OK', service: 'Secure Guardian Pro' });
});

// Start server
app.listen(PORT, () => {
    console.log(`🚀 Secure Guardian Pro running on http://localhost:${PORT}`);
});