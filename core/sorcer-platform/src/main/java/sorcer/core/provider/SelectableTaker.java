/*
 * Copyright 2018 the original author or authors.
 * Copyright 2018 SorcerSoft.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.core.provider;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.signature.ServiceSignature;
import sorcer.river.TX;
import sorcer.service.space.SpaceAccessor;
import sorcer.co.operator.Tokens;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/*
 * This space taker first reads and takes only tasks with indicated OS
 * matching OS of the platform the SpaceOSTaker is running.
 */
public class SelectableTaker extends SpaceTaker {

	ExertionEnvelop ee;

	public SelectableTaker() {
	}

	public SelectableTaker(SpaceTakerData data, ExecutorService pool) {
		this();
		this.data = data;
		this.pool = pool;
		this.transactionLeaseTimeout = getTransactionLeaseTime();
		this.spaceTimeout = getTimeOut();
	}

	public void run() {
		String threadId = doThreadMonitorTaker(null);
		Transaction.Created txnCreated = null;

		while (keepGoing) {
			Object envelopNoCast;
			try {
				// read a satisfactory task to be executed
				while (ee == null) {
					space = SpaceAccessor.getSpace(data.spaceName);
					if (space == null) {
						Thread.sleep(spaceTimeout / 2);
						continue;
					}
					// select the space entry that matching provider's OS and application constraints
					ee = selectSpaceEntry(data, space);
					if (ee == null) {
						Thread.sleep(spaceTimeout);
					}
				}

				if (data.noQueue) {
					if (((ThreadPoolExecutor) pool).getActiveCount() != ((ThreadPoolExecutor) pool).getCorePoolSize()) {
						Transaction tx = null;
						if (isTransactional) {
							txnCreated = TX.createTransaction(transactionLeaseTimeout);
							if (txnCreated == null) {
								logger.warn("SpaceTaker did not getValue TRANSACTION thread: {}", threadId);
								Thread.sleep(spaceTimeout / 6);
								continue;
							}
							tx = txnCreated.transaction;
						}
						envelopNoCast = space.take(data.entry, tx, spaceTimeout);
						ee = (ExertionEnvelop) envelopNoCast;
					} else {
                        /* Sleep for whats basically a clock tick to avoid thrashing */
						Thread.sleep(50);
						continue;
					}
				} else {
					if (isTransactional) {
						txnCreated = TX.createTransaction(transactionLeaseTimeout);
						if (txnCreated == null) {
//							doLog("\t***warning: space taker did not getValue TRANSACTION.",
//								threadId, null);
							Thread.sleep(spaceTimeout / 6);
							continue;
						}
						ee = (ExertionEnvelop) space.take(data.entry,
							txnCreated.transaction, spaceTimeout);
					} else {
						ee = (ExertionEnvelop) space.take(data.entry, null,
							spaceTimeout);
					}
				}

				// after 'take' timeout abort transaction and sleep for a while
				// before 'taking' the next exertion
				if (ee == null) {
					if (txnCreated != null) {
						TX.abortTransaction(txnCreated);
						try {
							Thread.sleep(spaceTimeout / 2);
						} catch (InterruptedException ie) {
							keepGoing = false;
							break;
						}
					}

					txnCreated = null;
					continue;
				}
				pool.execute(new SpaceWorker(ee, txnCreated, data.provider, remoteLogging));
			} catch (Exception ex) {
				logger.warn("Problem with SelectableTaker", ex);
			}
			ee = null;
		}
		// remove thread monitor
		doThreadMonitorTaker(threadId);
	}

	ExertionEnvelop selectSpaceEntry(SpaceTakerData data, JavaSpace05 space) throws
		TransactionException, UnusableEntryException, RemoteException, InterruptedException {
		ExertionEnvelop envelop = (ExertionEnvelop) space.read(data.entry, null, SPACE_TIMEOUT);
		logger.debug("########### {} selectable taker read envelop: {}", data.provider.getProviderName(), envelop);
		if (envelop != null) {
			List<?> matchTokens = ((ServiceSignature) envelop.exertion.getProcessSignature()).getOperation().getMatchTokens();
			logger.debug("########### {} selectable taker read matchTokens: {}", data.provider.getProviderName(), matchTokens);
			if (matchTokens instanceof Tokens) {
				Tokens tokens = (Tokens)matchTokens;
				switch (tokens.getType()) {
					case "LIST":
						boolean noMatch = false;
						for (Object list : tokens) {
							if (list instanceof Tokens) {
								Tokens tokenList = (Tokens) list;
								if (tokenList.getType().equals("OS")) {
									if (tokenList.contains(data.osName)) {
										logger.debug("########### {} Signature OS Names {} match provider OS: {}",
													 data.provider.getProviderName(),
													 list,
													 data.osName);
									} else {
										logger.debug("########### {} Signature OS Names {} no match provider OS: {}",
													 data.provider.getProviderName(),
													 list,
													 data.osName);
										noMatch = true;
										break;
									}
								} else if (tokenList.getType().equals("APP")) {
									if (data.appNames != null && data.appNames.containsAll((Tokens) list)) {
										logger.debug("########### {} Signature appNames {} match provider apps: {}",
													 data.provider.getProviderName(),
													 list,
													 data.appNames);
									} else {
										noMatch = true;
										break;
									}
								}
							} else {
								logger.warn("Unknown Tokens type: " + list.getClass().getName());
								noMatch = true;
							}
						}
						if (noMatch) {
							envelop = null;
						}
						break;
					case "OS":
						if (!tokens.contains(data.osName)) {
							envelop = null;
						}
						break;
					case "APP":
						if (!data.appNames.containsAll(matchTokens)) {
							envelop = null;
						}
						break;
				}
			}
		}
		return envelop;
	}

}