/*
 *  Copyright 2015 LG CNS.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */

package scouter.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import scouter.lang.pack.MapPack;
import scouter.lang.value.DecimalValue;
import scouter.lang.value.ListValue;
import scouter.lang.value.TextValue;

public class ThreadUtil {
	public static void sleep(long tm) {
		try {
			Thread.sleep(tm);
		} catch (InterruptedException e) {
		}
	}

	public static void qWait() {
		sleep(1);
	}

	public static void wait(Object o) {
		try {
			o.wait();
		} catch (InterruptedException e) {
		}
	}

	public static void wait(Object o, long time) {
		try {
			o.wait(time);
		} catch (InterruptedException e) {
		}
	}

	public static String getName(Thread t) {
		return getName(t.getClass());
	}
	public static String getName(Class clazz) {
		String name = clazz.getName();
		if(name.startsWith("scouter.agent.")==false)
			return name;
		return "Scouter-"+name.substring(name.lastIndexOf('.')+1);
	}

	public static MapPack getThreadDetail(long thread_id) {

		MapPack m = new MapPack();
		if (thread_id == 0)
			return m;
		ThreadMXBean tmb = ManagementFactory.getThreadMXBean();
		ThreadInfo f = tmb.getThreadInfo(thread_id, 500);
		if (f == null)
			return m;

		m.put("Thread Id", new DecimalValue(f.getThreadId()));
		m.put("Thread Cpu Time", new DecimalValue(tmb.getThreadCpuTime(thread_id) / 1000000));
		m.put("Thread User Time", new DecimalValue(tmb.getThreadUserTime(thread_id) / 1000000));

		m.put("Blocked Count", new DecimalValue(f.getBlockedCount()));
		m.put("Blocked Time", new DecimalValue(f.getBlockedTime()));
		m.put("Waited Count", new DecimalValue(f.getWaitedCount()));
		m.put("Waited Time", new DecimalValue(f.getWaitedTime()));
		m.put("Lock Owner Id", new DecimalValue(f.getLockOwnerId()));
		m.put("Lock Name", new TextValue(f.getLockName()));
		m.put("Lock Owner Name", new TextValue(f.getLockOwnerName()));
		m.put("Thread Name", new TextValue(f.getThreadName()));
		m.put("Stack Trace", new TextValue(getStackTrace(f.getStackTrace()).toString()));
		m.put("State", new TextValue(f.getThreadState().toString()));

		return m;
	}

	public static String getThreadStack(long id){
		ThreadMXBean tmb = ManagementFactory.getThreadMXBean();
		ThreadInfo f = tmb.getThreadInfo(id, 500);
		if (f == null)
			return null;
		return getStackTrace(f.getStackTrace()).toString();
	}
	public static String getThreadStack(){
		return getStackTrace(Thread.currentThread().getStackTrace()).toString();
	}
	public static String getStackTrace(StackTraceElement[] se) {
		String CRLF = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		if (se != null) {
			for (int i = 0; i < se.length; i++) {
				if (se[i] != null)
					sb.append(se[i].toString() + CRLF);
			}
		}
		return sb.toString();
	}

	public static MapPack getThreadList() {
		ThreadMXBean tmb = ManagementFactory.getThreadMXBean();

		long[] thread = tmb.getAllThreadIds();
		MapPack pack = new MapPack();
		ListValue id = pack.newList("id");
		ListValue name = pack.newList("name");
		ListValue stat = pack.newList("stat");
		ListValue cpu = pack.newList("cpu");

		for (int i = 0; i < thread.length; i++) {
			ThreadInfo fo = tmb.getThreadInfo(thread[i]);
			id.add(fo.getThreadId());
			name.add(fo.getThreadName());
			stat.add(fo.getThreadState().toString());
			cpu.add(tmb.getThreadCpuTime(thread[i]) / 1000000);
		}

		return pack;
	}

	public static ExecutorService createExecutor(final String name, int count, int keepAlive, final boolean isDaemon) {
		return createExecutor(name, count, count, keepAlive, isDaemon);
	}

	public static ExecutorService createExecutor(final String name, int coresize, int maxsize, int keepAlive,
			final boolean isDaemon) {
		ThreadPoolExecutor exe = new ThreadPoolExecutor(coresize, maxsize, keepAlive, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
					private int threadNum = 1;

					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, name + (threadNum++));
						t.setDaemon(isDaemon);
						return t;
					}

				});

//		if (keepAlive > 0) {
//			// FIXME JDK 1.7 ?
//			if (SystemUtils.IS_JAVA_1_5 == false) {
//				try {
//					exe.allowCoreThreadTimeOut(true);
//				} catch(Throwable t) { }
//			}
//		}

		return exe;
	}

	public static String getStackTrace(Throwable t) {
        String CRLF = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        sb.append(t.toString() + CRLF);
        StackTraceElement[] se = t.getStackTrace();
        if (se != null) {
            for (int i = 0; i < se.length; i++) {
                if (se[i] != null) {
                    sb.append("\t" + se[i].toString());
                    if (i != se.length -1) {
                    	sb.append(CRLF);
                    }          
                }
            }
        }
        
        return sb.toString();    	
    }
	public static void getStackTrace(StringBuffer sb , Throwable t, int max) {
		if(t==null)
			return ;
		if(max<=0){
			max=10000;
		}
        String CRLF = System.getProperty("line.separator");
        sb.append(t);
        StackTraceElement[] se = t.getStackTrace();
        if (se != null && se.length>0) {
            for (int i = 0; i < se.length && i <max; i++) {
                if (se[i] != null) {
                    sb.append(CRLF);
                	sb.append("\t" + se[i]);          
                }
            }
            if(max < se.length){
            	sb.append(CRLF+"\t...more lines " +(se.length-max) ); 
            }
        }else{
        	sb.append(CRLF+"\tno stack info ");
        }
        
      
    }
}