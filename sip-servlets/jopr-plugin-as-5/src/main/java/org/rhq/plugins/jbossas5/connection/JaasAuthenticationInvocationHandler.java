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

package org.rhq.plugins.jbossas5.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.security.auth.login.LoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.Configuration;

import org.rhq.plugins.jbossas5.connection.jaas.JBossCallbackHandler;
import org.rhq.plugins.jbossas5.connection.jaas.JBossConfiguration;

/**
 * @author Ian Springer
 */
public class JaasAuthenticationInvocationHandler implements InvocationHandler {    
    private Object target;
    private LoginContext loginContext;

    public JaasAuthenticationInvocationHandler(Object target, String username, String password) {
        this.target = target;                
        JBossCallbackHandler jaasCallbackHandler = new JBossCallbackHandler(username, password);
        Configuration jaasConfig = new JBossConfiguration();
        try {
            this.loginContext = new LoginContext(JBossConfiguration.JBOSS_ENTRY_NAME, null, jaasCallbackHandler,
                    jaasConfig);
        }
        catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        this.loginContext.login();
        Object returnValue = method.invoke(this.target, args);
        this.loginContext.logout();
        return returnValue;
    }
}
