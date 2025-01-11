const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');
const cors = require('cors');

const app = express();
const port = 3000;

// Middleware
app.use(bodyParser.json());
app.use(cors());

// In-memory data store (or file storage)
const dataFile = './playerData.json';

// Ensure the file exists
if (!fs.existsSync(dataFile)) {
    fs.writeFileSync(dataFile, JSON.stringify({}));
}

// Load data
const loadData = () => JSON.parse(fs.readFileSync(dataFile, 'utf-8'));
const saveData = (data) => fs.writeFileSync(dataFile, JSON.stringify(data, null, 2));

// Get player data
app.get('/player/:uuid', (req, res) => {
    const uuid = req.params.uuid;
    const data = loadData();
    res.json(data[uuid] || { message: 'Player not found' });
});

// Save or update player data
app.post('/player/:uuid', (req, res) => {
    const uuid = req.params.uuid;
    const newData = req.body;
    const data = loadData();

    data[uuid] = newData;
    saveData(data);

    res.json({ message: 'Data saved successfully', data: newData });
});

// Start the server
app.listen(port, () => {
    console.log(`API server running on http://localhost:${port}`);
});