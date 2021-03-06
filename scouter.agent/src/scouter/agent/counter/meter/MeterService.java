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

package scouter.agent.counter.meter;

import scouter.lang.ref.INT;
import scouter.lang.ref.LONG;
import scouter.util.MeteringUtil;
import scouter.util.MeteringUtil.Handler;

public class MeterService {
	private static MeterService inst = new MeterService();

	public static MeterService getInstance() {
		return inst;
	}

	static class Bucket {
		int count;
		int error;
		long time;
	}

	private MeteringUtil<Bucket> meter = new MeteringUtil<Bucket>() {
		protected Bucket create() {
			return new Bucket();
		};

		protected void clear(Bucket o) {
			o.count = 0;
			o.error = 0;
			o.time = 0L;
		}
	};

	public synchronized void add(long elapsed, boolean err) {
		Bucket b = meter.getCurrentBucket();
		b.count++;
		b.time += elapsed;
		if (err)
			b.error++;
	}

	public float getTPS(int period) {
		final INT sum = new INT();
		period = meter.search(period, new Handler<MeterService.Bucket>() {
			public void process(Bucket b) {
				sum.value += b.count;
			}
		});
		return (float) ((double) sum.value / period);
	}

	public int getElapsedTime(int period) {
		final LONG sum = new LONG();
		final INT cnt = new INT();
		meter.search(period, new Handler<MeterService.Bucket>() {
			public void process(Bucket b) {
				sum.value += b.time;
				cnt.value += b.count;

			}
		});
		return (int) ((cnt.value == 0) ? 0 : sum.value / cnt.value);
	}

	public float getError(int period) {

		final INT cnt = new INT();
		final INT err = new INT();
		meter.search(period, new Handler<MeterService.Bucket>() {
			public void process(Bucket b) {
				cnt.value += b.count;
				err.value += b.error;
			}
		});
		return (float) ((cnt.value == 0) ? 0 : (((double) err.value / cnt.value) * 100.0));
	}

	public int getServiceCount(int period) {

		final INT sum = new INT();
		meter.search(period, new Handler<MeterService.Bucket>() {
			public void process(Bucket b) {
				sum.value += b.count;
			}
		});
		return sum.value;
	}

	public int getServiceError(int period) {
		final INT sum = new INT();
		meter.search(period, new Handler<MeterService.Bucket>() {
			public void process(Bucket b) {
				sum.value += b.error;
			}
		});
		return sum.value;
	}
}