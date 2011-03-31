/**
 * 
 */
package madkit.classreloading;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import madkit.classreloading.anotherPackage.Fake;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Scheduler;

public class TestAgent extends Agent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public TestAgent() {
		Activator<AbstractAgent> a = new Activator<AbstractAgent>("test", "r", "r"){
			public void adding(AbstractAgent theAgent) {
				ActionListener al = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TestAgent.this.activate();
					}
				};
				al.actionPerformed(null);
			};
		};
		a.execute();
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setLogLevel(Level.ALL);
		super.activate();
		if(logger != null){
			logger.info("\n\ndS zz d\n\n");
			FakeObject o = new FakeObject();
			logger.info("fake is "+o);
			logger.info("fake2 is "+(new Fake().toString()));
			pause(4000);
			try {
				reloadAgentClass("madkit.classreloading.anotherPackage.Fake");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("fake3 is "+(new Fake().toString()));
		}
	}
	
	/**
	 * 
	 */
	@Override
	protected void live() {
			if(logger != null)
				logger.info("b");
			pause(1000);
	}
	
	public static void main(String[] argss) {
		String[] args = {"--agentLogLevel","INFO","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",TestAgent.class.getName()+",true"};
		Madkit.main(args);
	}
	
}

class FakeObject{
	@Override
	public String toString() {
		return "dddddddddddddddddddddddd";
	}
}