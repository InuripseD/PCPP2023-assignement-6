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

## 12.3
Implementation in `Server.java`.
If there are idle workers the task is sent to a worker—using a ComputeTask message.
If there are no idle workers, but the number of busy workers is less than maxWorkers, then spawn a
new worker and send the task.
If none of the above conditions hold, then the task must be placed in the list of pending tasks.


## 12.4
We added a signal-handler to the Server actor which handles `OnChildFailed` and in the actual handler method 
we get the reference to the failed worker, remove it from the set of busy workers, spawn a new and add that to the list of idle workers.