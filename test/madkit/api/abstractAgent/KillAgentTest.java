/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIME_OUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;
import madkit.testing.util.agent.FaultyAgent;
import madkit.testing.util.agent.KillTargetAgent;
import madkit.testing.util.agent.NormalLife;
import madkit.testing.util.agent.RandomT;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class KillAgentTest  extends JunitMadKit{

	final Agent target = new Agent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		}

		protected void live() {pause(1000);}
	};

	final AbstractAgent timeOutAgent = new AbstractAgent(){
		protected void activate() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS,killAgent(target));
			}
		});
	}

	@Test
	public void returnSuccessAfterLaunchTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIME_OUT,launchAgent(timeOutAgent,1));
				assertEquals(SUCCESS,killAgent(timeOutAgent));
			}
		});
	}

	@Test
	public void selfKill(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(AGENT_CRASH,launchAgent(new SelfKillAgent(true),1));
				assertEquals(SUCCESS,launchAgent(new SelfKillAgent(false,true),1));
				assertEquals(SUCCESS,launchAgent(new SelfKillAgent(false,false,true),1));
				assertEquals(AGENT_CRASH,launchAgent(new SelfKillAgent(true,false,true),1));
			}
		});
	}

	@Test
	public void returnNOT_YET_LAUNCHEDAfterImmediateLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIME_OUT,launchAgent(timeOutAgent,0));
				ReturnCode r = killAgent(timeOutAgent);
				assertTrue(NOT_YET_LAUNCHED == r || SUCCESS == r);
				pause(2000);
				if (r == NOT_YET_LAUNCHED) {
					assertEquals(SUCCESS, killAgent(timeOutAgent));
				}				
			}
		});
	}

	@Test
	public void returnAlreadyKilled(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				AbstractAgent a = new FaultyAgent(true);
				assertEquals(AGENT_CRASH,launchAgent(a));
				assertEquals(ALREADY_KILLED,killAgent(a));
				a = new FaultyAgent(false,true);
				assertEquals(SUCCESS,launchAgent(a));
				pause(100);
				assertEquals(ALREADY_KILLED,killAgent(a));
			}
		});
	}

	@Test
	public void massKill(){
		addMadkitArgs("--"+LevelOption.agentLogLevel,"OFF");
		launchTest(new AbstractAgent(){
			protected void activate() {
				int number = 5000;
				ArrayList<AbstractAgent> list = new ArrayList<AbstractAgent>(number);
				for (int i = 0; i < number;i++) {
					TimeOutAgent t = new TimeOutAgent();
					list.add(t);
					assertEquals(SUCCESS,launchAgent(t));
				}
				for (AbstractAgent a : list) {
					ReturnCode r = killAgent(a);
					assertTrue(ALREADY_KILLED == r || SUCCESS == r);
				}
			}
		});
	}

	@Test
	public void returnTimeOut(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIME_OUT,launchAgent(timeOutAgent,1));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
				assertEquals(SUCCESS,killAgent(timeOutAgent));
				assertAgentIsTerminated(timeOutAgent);
			}
		});
	}

	@Test
	public void returnAleradyLaunch(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(TIME_OUT,launchAgent(timeOutAgent,1));
				assertEquals(ALREADY_LAUNCHED,launchAgent(timeOutAgent));
			}
		});
	}

	@Test
	public void randomLaunchAndKill() {
		launchTest(new AbstractAgent(){
			protected void activate() {
				if(logger != null){
					logger.info("******************* STARTING RANDOM LAUNCH & AGENT_KILL *******************\n");
				}
				Runnable r = new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < 20; i++) {
							Agent a = (Agent) launchAgent("madkit.testing.util.agent.NormalLife", (int)(Math.random()*3),Math.random()<.5 ?true : false);
							assertNotNull(a);
							pause((int)(Math.random()*25));
							ReturnCode r = killAgent(a,(int)(Math.random()*3));
							assertTrue(NOT_YET_LAUNCHED == r || ALREADY_KILLED == r || SUCCESS == r || TIME_OUT == r);
						}
					}
				};
				SwingUtilities.invokeLater(r);
				pause(400);
				try {
					SwingUtilities.invokeAndWait(r);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void cascadeKills() {//TODO more cases
		launchTest(new AbstractAgent(){
			protected void activate() {
				Agent a = new NormalLife(false,true);
				assertEquals(SUCCESS,launchAgent(a,1));
				assertNotNull(a);
				KillTargetAgent ka = new KillTargetAgent(a);
				assertEquals(SUCCESS,launchAgent(ka,1));
				pause(100);
				assertEquals(TIME_OUT,killAgent(ka, 0));
				pause(100);
				assertAgentIsTerminated(ka);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void immediateKillWithTimeOut() {
		launchTest(new AbstractAgent(){
			protected void activate() {
				Agent a = new NormalLife(false,true);
				assertEquals(SUCCESS,launchAgent(a));
				assertNotNull(a);
				assertEquals(SUCCESS, killAgent(a,1));
				ReturnCode res = killAgent(a,2);
				assertTrue(ALREADY_KILLED == res);
				pause(1500);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void immediateKill() {
		launchTest(new AbstractAgent(){
			protected void activate() {
				Agent a = new NormalLife(false,true);
				assertEquals(SUCCESS,launchAgent(a));
				pause(1000);
				assertEquals(SUCCESS, killAgent(a));
				pause(100);
				assertAgentIsTerminated(a);
				Agent b = (Agent) launchAgent("madkit.kernel.Agent",10);
				killAgent(b,0);
				pause(100);
				assertAgentIsTerminated(b);
			}
		});
	}
	
	


	@Test
	public void randomTesting() {
		RandomT.killingOn = false;
		launchTest(new AbstractAgent(){
			protected void activate() {
				ArrayList<AbstractAgent> agents = new ArrayList<AbstractAgent>();
				for (int i = 0; i < 50; i++) {
					agents.add(new RandomT());
				}
				RandomT.agents=agents;
				assertEquals(SUCCESS,launchAgent(agents.get(0),1));
				boolean notFinished = true;
				while(notFinished){
					if(logger != null){
						logger.info("waiting for the end of the test");
					}
					pause(3000);
					notFinished = false;
					for (AbstractAgent randomTest : agents) {
						try {
							if(randomTest.getState() != State.TERMINATED && randomTest.getState() != State.NOT_LAUNCHED){
								notFinished = true;
								if(logger != null){
									logger.info("Waiting termination of "+randomTest.getName()+" state is "+randomTest.getState());
								}
							}
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	RandomT.killingOn = true;
	launchTest(new AbstractAgent(){
		protected void activate() {
			ArrayList<AbstractAgent> agents = new ArrayList<AbstractAgent>();
			for (int i = 0; i < 50; i++) {
				agents.add(new RandomT());
			}
			RandomT.agents=agents;
			assertEquals(SUCCESS,launchAgent(agents.get(0),1));
			boolean notFinished = true;
			while(notFinished){
				if(logger != null){
					logger.info("waiting for the end of the test");
				}
				pause(3000);
				notFinished = false;
				for (AbstractAgent randomTest : agents) {
					try {
						if(randomTest.getState() != State.TERMINATED && randomTest.getState() != State.NOT_LAUNCHED){
							notFinished = true;
							if(logger != null){
								logger.info("Waiting termination of "+randomTest.getName()+" state is "+randomTest.getState());
							}
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		}
	});
}

}

class TimeOutAgent extends Agent{
	@Override
	protected void live() {
		while(true)
			pause(1000);
	}
	protected void end() {
	}
}

class SelfKillAgent extends DoItDuringLifeCycleAgent{

	public SelfKillAgent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SelfKillAgent(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		// TODO Auto-generated constructor stub
	}

	public SelfKillAgent(boolean inActivate, boolean inLive) {
		super(inActivate, inLive);
		// TODO Auto-generated constructor stub
	}

	public SelfKillAgent(boolean inActivate) {
		super(inActivate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doIt() {
		super.doIt();
		killAgent(this);
	}

}