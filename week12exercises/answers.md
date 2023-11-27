# Exercise 12

## 12.1
We new a list of pending tasks because if all workers are busy we will need this list to store and access this task later.
Idle workers need a list too, so we can access them easily when a client ask for tasks.
We need a busy worker so we know which worker not to ask.


## 12.2
We added two private fields:
- `int minWorkers`
- `int maxWorkers`
this is all that is needed for new state.
In the constructor we use a for-loop to spawn `minWorkers`
new workers and add them to the list of idle workers. We also initialize the other state: `busyWorkers` and `pendingTasks`.