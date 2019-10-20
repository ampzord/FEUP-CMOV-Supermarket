
var express = require("express");
var app = express();

app.listen(3000, () => {
 console.log("Server running on port 3000");
});

app.get("/url", (req, res, next) => {
 res.json({ name: "Antonio", age: "18", email: "asd@gmail.com"});
});

