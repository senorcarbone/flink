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
package org.apache.flink.streaming.paper.experiments;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.policy.DeterministicPolicyGroup;
import org.apache.flink.streaming.api.windowing.policy.DeterministicTriggerPolicy;
import org.apache.flink.streaming.api.windowing.policy.TumblingPolicyGroup;
import org.apache.flink.streaming.api.windowing.windowbuffer.AggregationStats;
import org.apache.flink.streaming.paper.AggregationFramework;
import org.apache.flink.streaming.paper.PaperExperiment;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * This class reads an experiment setup generated by @link{DataGenerator} from a file and executes the experiments.
 * The results are written to a file.
 */
public class DEBSExpDriver {


	private static String RESULT_PATH = "test-result.txt";

	public static AggregationFramework.WindowAggregation<Tuple2<Long, Long>, Tuple4<Long, Long, Long, Integer>, Double>
			AvgAggregation = new AggregationFramework.WindowAggregation<>(
			new MapFunction<Tuple2<Long, Long>, Double>() {
				@Override
				public Double map(Tuple2<Long, Long> sumCount) throws Exception {
					return sumCount.f1 != 0 ? (double) sumCount.f0 / sumCount.f1 : 0d;
				}
			},
			new ReduceFunction<Tuple2<Long, Long>>() {
				private AggregationStats stats = AggregationStats.getInstance();

				@Override
				public Tuple2<Long, Long> reduce(Tuple2<Long, Long> t1, Tuple2<Long, Long> t2)
						throws Exception {
					stats.registerReduce();
					return new Tuple2<>(t1.f0 + t2.f0, t1.f1 + t1.f1);
				}
			}, new MapFunction<Tuple4<Long, Long, Long, Integer>, Tuple2<Long, Long>>() {
		@Override
		public Tuple2<Long, Long> map(Tuple4<Long, Long, Long, Integer> debs) throws Exception {
			return new Tuple2<>(debs.f2, 1l);
		}
	}, new Tuple2<>(0l, 0l));

	/**
	 * Main program: Runs all the test cases and writed the results to the specified output files.
	 *
	 * @param args not used,
	 * @throws Exception Any exception which may occurs at the runtime.
	 */
	public static void main(String[] args) throws Exception {

		AggregationStats stats = AggregationStats.getInstance();

		PrintWriter resultWriter = new PrintWriter(RESULT_PATH, "UTF-8");

		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(1);
		DataStream<Tuple4<Long, Long, Long, Integer>> sensorStream = env.readTextFile(args[0]).map(new DEBSDataFormatter());


		DeterministicPolicyGroup<Tuple4<Long, Long, Long, Integer>> togglePolicy =
				new TumblingPolicyGroup<>(new SensorTumblingWindow(5));
		List<DeterministicPolicyGroup<Tuple4<Long, Long, Long, Integer>>> detPolicies =
				new ArrayList<>();
		detPolicies.add(togglePolicy);

		AvgAggregation.applyOn(sensorStream,
				new Tuple3<>
						(detPolicies, new ArrayList<>(), new ArrayList<>()),
				AggregationFramework.AGGREGATION_STRATEGY.EAGER,
				AggregationFramework.DISCRETIZATION_TYPE.B2B)
				.map(new PaperExperiment.Prefix("SUM")).writeAsText("result-bla", FileSystem.WriteMode.OVERWRITE);

		JobExecutionResult result = env.execute("Scenario foo Case bla");

		finalizeExperiment(stats, resultWriter, result, 1, 1);

		sensorStream.print();
		env.execute();

		//close writer
		resultWriter.flush();
		resultWriter.close();
	}

	private static void finalizeExperiment(AggregationStats stats, PrintWriter resultWriter, JobExecutionResult result, int scenarioId, int caseId) {
		resultWriter.println(scenarioId + "\t" + caseId + "\t" + result.getNetRuntime() + "\t" + stats.getAggregateCount()
				+ "\t" + stats.getReduceCount() + "\t" + stats.getUpdateCount() + "\t" + stats.getMaxBufferSize() + "\t" + stats.getAverageBufferSize()
				+ "\t" + stats.getAverageUpdTime() + "\t" + stats.getTotalUpdateCount() + "\t" + stats.getAverageMergeTime() + "\t" + stats.getTotalMergeCount());
		stats.reset();
		resultWriter.flush();
	}

	protected static class SensorTumblingWindow implements DeterministicTriggerPolicy<Tuple4<Long, Long, Long, Integer>> {

		private int bitmask;
		private int currentState = Integer.MAX_VALUE; //initial state is -1 

		public 	SensorTumblingWindow(int sensorIndex) {
			this.bitmask = (int) Math.pow(2, sensorIndex);
		}

		@Override
		public double getNextTriggerPosition(double previousPosition) {
			//not used
			return 0;
		}

		@Override
		public boolean notifyTrigger(Tuple4<Long, Long, Long, Integer> datapoint) {
			int sensorVal = datapoint.f3 & bitmask;
			if (currentState == Integer.MAX_VALUE) {
				currentState = sensorVal;
				return false;
			}

			int tmp = currentState;
			this.currentState = sensorVal;

			boolean triggered = currentState != tmp;
			if(triggered){
				System.err.println("Triggered Window for sensor !!");
			}
			return triggered;
		}
	}

	public static class DEBSDataFormatter implements MapFunction<String, Tuple4<Long, Long, Long, Integer>> {

		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSX");

		@Override
		public Tuple4<Long, Long, Long, Integer> map(String line) throws Exception {
			String[] sensorVals = line.split("\t");
			//add measures into one
			long measure = Long.valueOf(sensorVals[2]) + Long.valueOf(sensorVals[3]) + Long.valueOf(sensorVals[4]);
			StringBuilder strBuilder = new StringBuilder();
			//create bin array of 21 sensor vals
			for (int i = 18; i < 27; i++) {
				strBuilder.append(sensorVals[i]);
			}
			for (int i = 30; i < 39; i++) {
				strBuilder.append(sensorVals[i]);
			}
			for (int i = 48; i < 51; i++) {
				strBuilder.append(sensorVals[i]);
			}
			return new Tuple4<>(dfm.parse(sensorVals[0])
					.getTime(), Long.valueOf(sensorVals[1]), measure, Integer.parseInt(strBuilder.toString(), 2));
		}
	}
}