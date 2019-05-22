package morlivm.memory;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

public class LoadedSectionsQueue
{
	public static class Section
	{
		private String discription;
		private Callable<Void> code;
		
		public Section(String discription, Callable<Void> code) {
			this.discription = discription;
			this.code = code;
		}
		
		public void execute() {
			try { code.call(); }
			catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not load the section \"" + discription + "\"");
			}
		}
		
		public String getDiscription() { return discription; }
	}
	
	private Queue<Section> queue;
	
	public LoadedSectionsQueue() {
		queue = new LinkedList<Section>();
	}
	
	public void enqueue(String discription, Callable<Void> code) {
		queue.add(new Section(discription, code));
	}
	
	public Section dequeue() { return queue.remove(); }
	public Section peek() { return queue.peek(); }
	public boolean isEmpty() { return queue.isEmpty(); }
}