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

package scouter.lang.step;

import java.io.IOException;

import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.lang.value.Value;

public class ThreadSubmitStep extends StepSingle {

	public long txid;
	public int hash;
	public int elapsed;
	public int cputime;
	public int error;
	

	public byte getStepType() {
		return StepEnum.THREAD_SUBMIT;
	}

	public void write(DataOutputX out) throws IOException {
		super.write(out);

		out.writeDecimal(txid);
		out.writeDecimal(hash);
		out.writeDecimal(elapsed);
		out.writeDecimal(cputime);
		out.writeDecimal(error);

	}

	public Step read(DataInputX in) throws IOException {
		super.read(in);

		this.txid = in.readDecimal();
		this.hash = (int) in.readDecimal();
		this.elapsed = (int) in.readDecimal();
		this.cputime = (int) in.readDecimal();
		this.error = (int) in.readDecimal();
		return this;
	}
}