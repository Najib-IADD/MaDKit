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

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_ARG;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.Madkit.Roles.GROUP_CANDIDATE_ROLE;
import static madkit.kernel.Madkit.Roles.GROUP_MANAGER_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Message;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class sendMessageWithAATest  extends JunitMadKit{

	final AbstractAgent target = new AbstractAgent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		}
	};

	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNotNull(aa);
				
				//Without role
				assertEquals(SUCCESS, sendMessage(aa, new Message()));
				Message m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());

				//With role
				assertEquals(SUCCESS, sendMessageWithRole(aa, new Message(),ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());
			}});
	}

	@Test
	public void returnSuccessOnCandidateRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));

				//Without role
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, GROUP_MANAGER_ROLE);
				assertNotNull(aa);
				assertEquals(SUCCESS, sendMessage(aa, new Message()));
				Message m = target.nextMessage();
				assertNotNull(m);
				assertEquals(GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				assertEquals(GROUP_CANDIDATE_ROLE, m.getSender().getRole());

				//With role
				aa = getAgentWithRole(COMMUNITY, GROUP, GROUP_MANAGER_ROLE);
				assertNotNull(aa);
				assertEquals(SUCCESS, sendMessageWithRole(aa, new Message(), GROUP_CANDIDATE_ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				assertEquals(GROUP_CANDIDATE_ROLE, m.getSender().getRole());
			}});
	}

	@Test
	public void returnInvalidAA(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(INVALID_AA, sendMessage(aa, new Message()));

				//With role
				assertEquals(INVALID_AA, sendMessageWithRole(aa, new Message(), ROLE));
			}});
	}

	@Test
	public void returnNotInGroup(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(aa, new Message(), ROLE));
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(INVALID_AA, sendMessage(aa, new Message()));
				assertEquals(NOT_ROLE, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));

				//With role
			}});
	}
	
	@Test
	public void returnRoleNotHandled(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(aa, new Message(),aa()));

			}});
	}
	
	

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(INVALID_ARG, sendMessage(null, null));
				assertEquals(INVALID_ARG, sendMessage(aa, null));
				assertEquals(INVALID_ARG, sendMessage(null, new Message()));
			}
		});
	}

}
