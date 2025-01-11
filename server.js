const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs').promises;
const path = require('path');
const cors = require('cors');
const axios = require('axios');  // Required to fetch data from GitHub

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(express.json());
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
app.post('/playerData/Add', (req, res) => {
    const playerData = req.body; // The player data sent from the mod

    // Path to your playerData.json file
    const filePath = path.join(__dirname, 'playerData.json');

    // Read the current playerData.json, or initialize an empty array if it doesn't exist
    fs.readFile(filePath, (err, data) => {
        if (err && err.code !== 'ENOENT') {
            console.error('Error reading player data file:', err);
            return res.status(500).json({ message: 'Error reading player data file' });
        }

        let existingData = [];
        if (!err) {
            existingData = JSON.parse(data);
        }

        // Add the new player data
        existingData.push(playerData);

        // Save the updated player data back to playerData.json
        fs.writeFile(filePath, JSON.stringify(existingData, null, 2), (err) => {
            if (err) {
                console.error('Error writing player data file:', err);
                return res.status(500).json({ message: 'Error saving player data' });
            }
            console.log('Player data saved successfully');
            res.status(200).json({ message: 'Player data successfully stored' });
        });
    });
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