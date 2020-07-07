// Import all env vars from .env file
require('dotenv').config()

const API_KEY = process.env.API_KEY

console.log(API_KEY) // => Hello
