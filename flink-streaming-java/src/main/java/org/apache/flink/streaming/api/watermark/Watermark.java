/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.api.watermark;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.streaming.runtime.streamrecord.StreamElement;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * A Watermark tells operators that no elements with a timestamp older or equal
 * to the watermark timestamp should arrive at the operator. Watermarks are emitted at the
 * sources and propagate through the operators of the topology. Operators must themselves emit
 * watermarks to downstream operators using
 * {@link org.apache.flink.streaming.api.operators.Output#emitWatermark(Watermark)}. Operators that
 * do not internally buffer elements can always forward the watermark that they receive. Operators
 * that buffer elements, such as window operators, must forward a watermark after emission of
 * elements that is triggered by the arriving watermark.
 *
 * <p>In some cases a watermark is only a heuristic and operators should be able to deal with
 * late elements. They can either discard those or update the result and emit updates/retractions
 * to downstream operations.
 *
 * <p>When a source closes it will emit a final watermark with timestamp {@code Long.MAX_VALUE}.
 * When an operator receives this it will know that no more input will be arriving in the future.
 */
@PublicEvolving
public final class Watermark extends StreamElement implements Comparable<Watermark>{

	/** The watermark that signifies end-of-event-time. */
	public static final Watermark MAX_WATERMARK = new Watermark(Long.MAX_VALUE);

	// ------------------------------------------------------------------------
	
	/** The timestamp of the watermark in milliseconds*/
	private long timestamp;
	private final List<Long> context;
	private boolean iterationDone = false;
	private boolean iterationOnly = false;

	/**
	 * Creates a new watermark with the given timestamp in milliseconds.
	 */
	public Watermark(long timestamp) {
		this.timestamp = timestamp;
		this.context = new LinkedList<>();
	}

	/**
	 * Creates a new watermark from an existing one.
	 */
	public Watermark(Watermark watermark) {
		this.timestamp = watermark.getTimestamp();
		this.context = new LinkedList<>(watermark.getContext());
	}

	/**
	 * Creates a new watermark with the given timestamp in milliseconds and a context.
	 */
	public Watermark(List<Long> context, long timestamp) {
		this.timestamp = timestamp;
		this.context = new LinkedList(context);
	}

	/**
	 * Creates a new watermark with the given timestamp in milliseconds and a context.
	 */
	public Watermark(List<Long> context, long timestamp, boolean iterationDone) {
		this.timestamp = timestamp;
		this.context = new LinkedList(context);
		this.iterationDone = iterationDone;
	}

	/**
	 * Creates a new watermark with the given timestamp in milliseconds and a context.
	 */
	public Watermark(List<Long> context, long timestamp, boolean iterationDone, boolean iterationOnly) {
		this.timestamp = timestamp;
		this.context = new LinkedList(context);
		this.iterationDone = iterationDone;
		this.iterationOnly = iterationOnly;
	}

	/**
	 * Returns the timestamp associated with this {@link Watermark} in milliseconds.
	 */
	public long getTimestamp() {
		return timestamp;
	}
	public List<Long> getFullTimestamp() {
		List<Long> fullTimestamp = new LinkedList<>(context);
		fullTimestamp.add(timestamp);
		return fullTimestamp;
	}
	public List<Long> getContext() {return context; }

	public void addNestedTimestamp(long timestamp) {
		this.context.add(this.timestamp);
		this.timestamp = timestamp;
	}
	public void removeNestedTimestamp() {
		if(this.context.size() > 0) {
			this.timestamp = this.context.remove(this.context.size()-1);
		}
	}

	public void forwardTimestamp() {
		timestamp = timestamp + 1;
	}

	public boolean iterationDone() { return iterationDone; }
	public void setIterationDone(boolean iterationDone) {
		this.iterationDone = iterationDone;
	}

	public boolean iterationOnly() { return iterationOnly; }

	// ------------------------------------------------------------------------

	@Override
	public boolean equals(Object o) {
		return this == o ||
				o != null && o.getClass() == Watermark.class
					&& ((Watermark) o).timestamp == this.timestamp
					&& ((Watermark) o).context.equals(this.context);
	}

	@Override
	public int hashCode() {
		return (int) timestamp ^ context.hashCode();
	}

	@Override
	public String toString() {
		return "Watermark @ [" + StringUtils.join(context, ", ") + ", " + timestamp + "]";
	}

	@Override
	public int compareTo(Watermark mark) {
		return new Long(timestamp).compareTo(mark.getTimestamp());
	}
}
