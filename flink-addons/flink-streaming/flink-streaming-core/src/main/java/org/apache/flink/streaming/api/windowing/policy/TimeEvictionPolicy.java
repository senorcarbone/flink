/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.api.windowing.policy;

import java.util.LinkedList;

import org.apache.flink.streaming.api.invokable.util.TimeStamp;

public class TimeEvictionPolicy<DATA> implements EvictionPolicy<DATA> {

	/**
	 * auto generated version id
	 */
	private static final long serialVersionUID = -1457476766124518220L;

	private long granularity;
	private TimeStamp<DATA> timestamp;
	private LinkedList<DATA> buffer=new LinkedList<DATA>();

	public TimeEvictionPolicy(long granularity, TimeStamp<DATA> timestamp) {
		this.timestamp = timestamp;
		this.granularity = granularity;
	}

	@Override
	public int notifyEviction(DATA datapoint, boolean triggered, int bufferSize) {
		// check for deleted tuples (deletes by other policies)
		while (bufferSize > this.buffer.size()) {
			this.buffer.removeFirst();
		}

		// delete and count expired tuples
		int counter = 0;
		for (DATA d : buffer) {
			if (timestamp.getTimestamp(d) < timestamp.getTimestamp(datapoint) - granularity) {
				buffer.removeFirst();
			}
		}

		// return result
		return counter;
	}

}
