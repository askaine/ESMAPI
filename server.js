const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');
const cors = require('cors');
const axios = require('axios');  // Required to fetch data from GitHub

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(bodyParser.json());
app.use(cors());

// GitHub URL for the raw playerData.json file
const playerDataUrl = 'https://raw.githubusercontent.com/askaine/ESMAPI/main/playerData.json';

// Fetch playerData.json from GitHub and send it in the response
app.get('/playerData', async (req, res) => {
    try {
        const response = await axios.get(playerDataUrl);
        res.json(response.data);
    } catch (error) {
        res.status(500).json({ message: 'Error fetching player data from GitHub' });
    }
});
const path = require('path');

app.post('/playerData/Add', async (req, res) => {
    const playerData = req.body;
    const filePath = path.join(__dirname, 'playerData.json');

    try {
        let existingData = [];

        try {
            const data = await fs.readFile(filePath, 'utf8');
            existingData = JSON.parse(data);
        } catch (err) {
            if (err.code !== 'ENOENT') {
                throw new Error('Error reading player data file');
            }
        }

        existingData.push(playerData);
        await fs.writeFile(filePath, JSON.stringify(existingData, null, 2), 'utf8');
        res.status(200).json({ message: 'Player data successfully stored' });
    } catch (err) {
        console.error(err);
        res.status(500).json({ message: 'Error saving player data' });
    }
});

// Get player data


// Default route
app.get('/', (req, res) => {
    res.send('Welcome to the ESM API!');
});

// Start the server
app.listen(port, () => {
    console.log(`API server running on http://localhost:${port}`);
});