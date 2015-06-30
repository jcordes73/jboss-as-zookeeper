/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat, Inc., and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
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

package org.jboss.as.zookeeper;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.jboss.as.network.SocketBinding;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="jcordes@redhat.com">Jochen Cordes</a>
 */
public class ZooKeeperService implements Service<ZooKeeperServer> {

	private final Logger log = Logger.getLogger(ZooKeeperService.class);

	public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("zookeeper");
	
	private final InjectedValue<ExecutorService> executor = new InjectedValue<ExecutorService>();
	private final InjectedValue<SocketBinding> binding = new InjectedValue<SocketBinding>();
	    
	private long tickTime = 2000;
	private String dataDir = "${jboss.server.data.dir}/zookeeper";

	private ZooKeeperServer zooKeeperServer = null;

	public ZooKeeperService(long tickTime, String dataDir) {
		this.tickTime = tickTime;
		this.dataDir = dataDir;
	}

	@Override
	public ZooKeeperServer getValue() throws IllegalStateException,
			IllegalArgumentException {
		return zooKeeperServer;
	}

	@Override
	public synchronized void start(final StartContext context) throws StartException {
		File dir = new File(dataDir).getAbsoluteFile();

		try {
			zooKeeperServer = new ZooKeeperServer(dir, dir, (int) tickTime);
		
			ServerCnxnFactory serverCnxnFactory = ServerCnxnFactory.createFactory(binding.getValue().getSocketAddress(), 5);
			zooKeeperServer.setServerCnxnFactory(serverCnxnFactory);
			
			context.asynchronous();
			executor.getValue().execute(new ZooKeeperServerRunner(zooKeeperServer, context));
		} catch (IOException e) {
			context.failed(new StartException(e));
		}
	}

	@Override
	public synchronized void stop(final StopContext context) {
		context.asynchronous();
		executor.getValue().execute(new ZooKeeperServerDestroyer(zooKeeperServer, context));
	}

	public void setTickTime(long tickTime) {
		this.tickTime = tickTime;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}
	
	public InjectedValue<SocketBinding> getBinding() {
		return binding;
	}
	
	public InjectedValue<ExecutorService> getExecutor() {
        return executor;
    }
	
	class ZooKeeperServerRunner implements Runnable {

		private ZooKeeperServer zooKeeperServer;
		private StartContext startContext;
		
		public ZooKeeperServerRunner(ZooKeeperServer zookeeperServer, StartContext startContext){
			this.zooKeeperServer = zookeeperServer;
			this.startContext = startContext;
		}
		
		@Override
		public void run() {
			try {
				zooKeeperServer.startup();
				zooKeeperServer.getServerCnxnFactory().start();
				startContext.complete();
				log.info("Zookeeper started at " + binding.getValue().getSocketAddress());
			} catch (Throwable e) {
				startContext.failed(new StartException(e));
				log.error("Zookeeper failed to start at " + binding.getValue().getSocketAddress());
			}
			
		}
	}
	
	class ZooKeeperServerDestroyer implements Runnable {

		private ZooKeeperServer zooKeeperServer;
		private StopContext stopContext;
		
		public ZooKeeperServerDestroyer(ZooKeeperServer zookeeperServer, StopContext stopContext){
			this.zooKeeperServer = zookeeperServer;
			this.stopContext = stopContext;
		}
		
		@Override
		public void run() {
			try {
			  zooKeeperServer.getServerCnxnFactory().closeSession(tickTime);
			  zooKeeperServer.getServerCnxnFactory().closeAll();
			  zooKeeperServer.getServerCnxnFactory().shutdown();
			  zooKeeperServer.shutdown();
			} finally {
			  stopContext.complete();
			}
			
			log.info("Zookeeper stopped");
		}
	}
}
