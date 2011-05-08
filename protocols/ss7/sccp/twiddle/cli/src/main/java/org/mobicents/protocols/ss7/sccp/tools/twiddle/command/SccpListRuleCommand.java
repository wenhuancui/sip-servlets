/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.ss7.sccp.tools.twiddle.command;

import java.io.PrintWriter;

import org.mobicents.protocols.ss7.sccp.impl.router.Rule;

/**
 * @author baranowb
 * 
 */
public class SccpListRuleCommand extends AbstractSccpCommand {

	private static final String METHOD = "getRules";
	private static final String NO_RULES = "No rules present.";
	/**
	 * @param name
	 * @param desc
	 */
	public SccpListRuleCommand() {
		super("route.list", "This command lists routing rules present in SCCP layer.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.console.twiddle.command.Command#displayHelp()
	 */
	public void displayHelp() {
		PrintWriter out = context.getWriter();

		out.println(desc);
		out.println();
		out.println("usage: " + name + " ");
		out.println();
		out.flush();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.console.twiddle.command.Command#execute(java.lang.String[])
	 */
	public void execute(String[] args) throws Exception {
		Rule[] rules = (Rule[]) super.context.getServer().invoke(super.createObjectName(), METHOD, new Object[] {}, new String[] {});
		// default impl of display;
		if (!context.isQuiet()) {
			if (rules == null || rules.length == 0) {

				PrintWriter out = context.getWriter();
				out.println(NO_RULES);
				out.flush();
			}else
			{
				StringBuffer sb = new StringBuffer();
				for(Rule r:rules)
				{
					sb.append("#            :").append(r.getNo()).append("\n");
					sb.append("Pattern      :").append(r.getPattern()).append("\n");
					sb.append("Translation  :").append(r.getTranslation()).append("\n");
					sb.append("MtpInfo      :").append(r.getTranslation()).append("\n\n");
				}
				PrintWriter out = context.getWriter();
				out.println(sb);
				out.flush();
			}
		}
	}

}
