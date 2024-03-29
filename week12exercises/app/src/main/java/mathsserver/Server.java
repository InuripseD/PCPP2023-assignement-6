package mathsserver;

// Hint: The imports below may give you hints for solving the exercise.
//       But feel free to change them.

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.ChildFailed;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.*;

import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.IntStream;

import mathsserver.Task;
import mathsserver.Task.BinaryOperation;

public class Server extends AbstractBehavior<Server.ServerCommand> {
    /* --- Messages ------------------------------------- */
    public interface ServerCommand { }

    
    public static final class ComputeTasks implements ServerCommand {
		public final List<Task> tasks;
		public final ActorRef<Client.ClientCommand> client;

		public ComputeTasks(List<Task> tasks,
							ActorRef<Client.ClientCommand> client) {
			this.tasks  = tasks;
			this.client = client;
		}
    }

    public static final class WorkDone implements ServerCommand {
		ActorRef<Worker.WorkerCommand> worker;

		public WorkDone(ActorRef<Worker.WorkerCommand> worker) {
			this.worker = worker;
		}
    }
    
    /* --- State ---------------------------------------- */
	private ArrayList<Worker.ComputeTask> pendingTasks;
	private ArrayList<ActorRef<Worker.WorkerCommand>> idleWorkers;
	private HashSet<ActorRef<Worker.WorkerCommand>> busyWorkers;
	private int minWorkers;
	private int maxWorkers;
	private static int workersNumbers;
    

    /* --- Constructor ---------------------------------- */
    private Server(ActorContext<ServerCommand> context,
				   int minWorkers, int maxWorkers) {
    	super(context);
		// To be implemented
		workersNumbers = 0;
		this.minWorkers = minWorkers;
		this.maxWorkers = maxWorkers;
		this.pendingTasks = new ArrayList<>();
		this.busyWorkers = new HashSet<>();
		this.idleWorkers = new ArrayList<>();
		for(int i = 0; i < minWorkers; i++) {
			ActorRef<Worker.WorkerCommand> worker = getContext().spawn(Worker.create(this.getContext().getSelf()), "Worker"+workersNumbers); 
			getContext().watch(worker); //12.4
			idleWorkers.add(worker);
			workersNumbers++;
		}
		

	
    }


    /* --- Actor initial state -------------------------- */
    public static Behavior<ServerCommand> create(int minWorkers, int maxWorkers) {
    	return Behaviors.setup(context -> new Server(context, minWorkers, maxWorkers));
    }
    

    /* --- Message handling ----------------------------- */
    @Override
    public Receive<ServerCommand> createReceive() {
    	return newReceiveBuilder()
    	    .onMessage(ComputeTasks.class, this::onComputeTasks)
    	    .onMessage(WorkDone.class, this::onWorkDone)
			.onSignal(ChildFailed.class, this::onChildFailed)
			// To be extended
    	    .build();
    }


    /* --- Handlers ------------------------------------- */
    public Behavior<ServerCommand> onComputeTasks(ComputeTasks msg) {
		for(Task task: msg.tasks){
			if (idleWorkers.isEmpty()) {
				if (busyWorkers.size() < maxWorkers) {
					ActorRef<Worker.WorkerCommand> worker = getContext().spawn(Worker.create(this.getContext().getSelf()), "Worker"+workersNumbers); 
					getContext().watch(worker); // 12.4
					workersNumbers++;
					worker.tell(new Worker.ComputeTask(task, msg.client));
					busyWorkers.add(worker);
				}else{
					pendingTasks.add(new Worker.ComputeTask(task, msg.client));
				}
			}else{
				ActorRef<Worker.WorkerCommand> worker = idleWorkers.get(0);
				worker.tell(new Worker.ComputeTask(task, msg.client));
				busyWorkers.add(worker);
			}
		}
    	return this;
    }

    public Behavior<ServerCommand> onWorkDone(WorkDone msg) {
		if (!pendingTasks.isEmpty()){
			msg.worker.tell(pendingTasks.get(0));
		} else {
			var ref = msg.worker;
			busyWorkers.remove(ref);
			idleWorkers.add(ref);
		}
		return this;	
    }   
	
	public Behavior<ServerCommand> onChildFailed(ChildFailed childFailed) {
		// get the worker and remove it from busyworkers
		var ref = childFailed.getRef();
		busyWorkers.remove(ref);

		// spawn a new worker and add to idle
		ActorRef<Worker.WorkerCommand> worker = getContext().spawn(Worker.create(this.getContext().getSelf()), "Worker"+workersNumbers); 
		workersNumbers++;
		idleWorkers.add(worker);


		return this;
	}
}
