const Joi = require('joi');
const express = require("express");
const app = express();

app.use(express.json());

const courses = [
    {   id: 1, name: "course1"  },
    {   id: 2, name: "course2"  },  
    {   id: 3, name: "course3"  },
]

app.get("/", (req, res) => {
    res.send('Hello World');
});

app.get("/url", (req, res) => {
 res.json({ name: "Antonio", age: "18", email: "asd@gmail.com"});
});

app.get("/api/courses", (req,res) => {
    res.send(courses);
});

app.get("/api/courses/:id", (req, res) => {
    const course = courses.find(c=> c.id === parseInt(req.params.id));
    if (!course)
        return res.status(404).send('The course with the given ID was not found');
    res.send(course);
});

app.post("/api/courses", (req,res) => {
    const result = validateCourse(req.body);
    //console.log(result);

    if (result.error) {
        //400 bad request
        return res.status(400).send(result.error.details[0].message);
    }


    const course = { //in DB id is automatic
        id: courses.length + 1,
        name: req.body.name
    }
    courses.push(course);
    res.send(course);
});

app.put("/api/courses/:id", (req,res) => {
    //look up the course
    //if not existing return 404
    const course = courses.find(c => c.id === parseInt(req.params.id));
    if (!course)
        return res.status(404).send('The course with the given ID was not found');

    // validate
    //if invalid return 400 bad request
    const result = validateCourse(req.body);
    if (result.error) {
        //400 bad request
        return res.status(400).send(result.error.details[0].message);
    }

    //update course
    //return the updated course
    course.name = req.body.name;
    res.send(course);
});

app.delete("/api/courses/:id", (req,res) => {
    //look up the course
    //not existing, return 404
    const course = courses.find(c => c.id === parseInt(req.params.id));
    if (!course)
        return res.status(404).send('The course with the given ID was not found');

    //delete
    const index = courses.indexOf(course);
    courses.splice(index, 1); 

    //return the same course
    res.send(course);
});


const port = process.env.PORT || 3000;
app.listen(port, () => {
    console.log(`Listening on port ${port}...`);
});

function validateCourse(course) {
    const schema = {
        name: Joi.string().min(3).required()
    };

    return Joi.validate(course, schema);
}