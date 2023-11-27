# Exercise 12

## 12.1
We new a list of pending tasks because if all workers are busy we will need this list to store and access this task later.
Idle workers need a list too, so we can access them easily when a client ask for tasks.
We need a busy worker so we know which worker not to ask.