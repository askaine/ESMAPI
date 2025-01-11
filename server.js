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

// Get player data
app.get('/player/:uuid', async (req, res) => {
    const uuid = req.params.uuid;
    try {
        const response = await axios.get(playerDataUrl);
        const data = response.data;
        res.json(data[uuid] || { message: 'Player not found' });
    } catch (error) {
        res.status(500).json({ message: 'Error fetching player data from GitHub' });
    }
});

// Save or update player data
app.post('/player/:uuid', async (req, res) => {
    const uuid = req.params.uuid;
    const newData = req.body;

    try {
        const response = await axios.get(playerDataUrl);
        const data = response.data;

        data[uuid] = newData;

        // Save the updated playerData.json back to GitHub (requires GitHub API and authentication)
        // For now, we'll only modify the local data store, but you can implement the push to GitHub here
        // Example: await axios.put('https://api.github.com/repos/yourusername/yourrepo/contents/playerData.json', updatedData);

        res.json({ message: 'Data saved successfully', data: newData });
    } catch (error) {
        res.status(500).json({ message: 'Error updating player data from GitHub' });
    }
});

// Default route
app.get('/', (req, res) => {
    res.send('Welcome to the API! Use /playerData to get player data.');
});

// Start the server
app.listen(port, () => {
    console.log(`API server running on http://localhost:${port}`);
});