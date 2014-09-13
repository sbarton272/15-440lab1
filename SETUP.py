#!/usr/bin/python

from subprocess import call

# Compiles the code
call(['javac', '-cp', './src', 'src/Lab1.java'])
call(['javac', '-cp', './src', 'src/worker/Worker.java'])

# To Run Lab
# java -cp . Lab1

# To Run Worker, note port number must not be reserved
# java -cp . worker/Worker 8080
