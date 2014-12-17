/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.flink.api.java.functions;

import org.apache.flink.api.common.operators.SemanticProperties.InvalidSemanticAnnotationException;
import org.apache.flink.api.common.typeutils.CompositeType;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.junit.Assert;
import org.apache.flink.api.common.operators.DualInputSemanticProperties;
import org.apache.flink.api.common.operators.SingleInputSemanticProperties;
import org.apache.flink.api.common.operators.util.FieldSet;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.junit.Test;

public class SemanticPropUtilTest {
	
	private final TypeInformation<?> threeIntTupleType = new TupleTypeInfo<Tuple3<Integer, Integer, Integer>>(BasicTypeInfo.INT_TYPE_INFO,
			BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO);

	private final TypeInformation<?> fourIntTupleType = new TupleTypeInfo<Tuple4<Integer, Integer, Integer, Integer>>(BasicTypeInfo.INT_TYPE_INFO,
			BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO);

	private final TypeInformation<?> fiveIntTupleType = new TupleTypeInfo<Tuple5<Integer, Integer, Integer, Integer, Integer>>(BasicTypeInfo.INT_TYPE_INFO,
			BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO);

	private final TypeInformation<?> threeMixedTupleType = new TupleTypeInfo<Tuple3<Integer, Long, String>>(BasicTypeInfo.INT_TYPE_INFO,
			BasicTypeInfo.LONG_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO);

	private final TypeInformation<?> nestedTupleType = new TupleTypeInfo<Tuple3<Tuple3<Integer, Integer, Integer>, Integer, Integer>>(threeIntTupleType,
			BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO);

	private final TypeInformation<?> deepNestedTupleType = new TupleTypeInfo<Tuple3<Integer, Tuple3<Tuple3<Integer, Integer, Integer>, Integer, Integer>, Integer>>(
			BasicTypeInfo.INT_TYPE_INFO, nestedTupleType, BasicTypeInfo.INT_TYPE_INFO);

	private final TypeInformation<?> pojoType = TypeExtractor.getForClass(TestPojo.class);

	private final TypeInformation<?> pojo2Type = TypeExtractor.getForClass(TestPojo2.class);

	private final TypeInformation<?> nestedPojoType = TypeExtractor.getForClass(NestedTestPojo.class);

	private final TypeInformation<?> pojoInTupleType = new TupleTypeInfo<Tuple3<Integer, Integer, TestPojo>>(BasicTypeInfo.INT_TYPE_INFO,
			BasicTypeInfo.INT_TYPE_INFO, pojoType);

	private final TypeInformation<?> intType = BasicTypeInfo.INT_TYPE_INFO;

	// --------------------------------------------------------------------------------------------
	// Projection Operator Properties
	// --------------------------------------------------------------------------------------------

	@Test
	public void testSingleProjectionProperties() {

		int[] pMap = new int[] {3,0,4};
		SingleInputSemanticProperties sp = SemanticPropUtil.createProjectionPropertiesSingle(pMap, (CompositeType<?>) fiveIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(2));

		pMap = new int[] {2,2,1,1};
		sp = SemanticPropUtil.createProjectionPropertiesSingle(pMap, (CompositeType<?>) fiveIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 2);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 2);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));

		pMap = new int[] {2,0};
		sp = SemanticPropUtil.createProjectionPropertiesSingle(pMap, (CompositeType<?>) nestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(3));

		pMap = new int[] {2,0,1};
		sp = SemanticPropUtil.createProjectionPropertiesSingle(pMap, (CompositeType<?>) deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 6).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(6));

		pMap = new int[] {2, 1};
		sp = SemanticPropUtil.createProjectionPropertiesSingle(pMap, (CompositeType<?>) pojoInTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(4));

	}

	@Test
	public void testDualProjectionProperties() {

		int[] pMap = new int[]{4,2,0,1,3,4};
		boolean[] iMap = new boolean[]{true, true, false, true, false, false};
		DualInputSemanticProperties sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap,
				fiveIntTupleType, fiveIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 3).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 4).contains(5));

		pMap = new int[]{4,2,0,4,0,1};
		iMap = new boolean[]{true, true, false, true, false, false};
		sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap, fiveIntTupleType, fiveIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).size() == 2);
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).size() == 2);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 1).contains(5));

		pMap = new int[]{2,1,0,1};
		iMap = new boolean[]{false, false, true, true};
		sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap, nestedTupleType, threeIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(1, 2).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(5));

		pMap = new int[]{1,0,0};
		iMap = new boolean[]{false, false, true};
		sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap, nestedTupleType, deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(1, 1).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 3).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 4).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 5).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(6));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(7));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(8));

		pMap = new int[]{4,2,1,0};
		iMap = new boolean[]{true, false, true, false};
		sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap, fiveIntTupleType, pojoInTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 3).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 4).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 5).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(6));

		pMap = new int[]{2,3,-1,0};
		iMap = new boolean[]{true, true, false, true};
		sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap, fiveIntTupleType, intType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(3));

		pMap = new int[]{-1,-1};
		iMap = new boolean[]{false, true};
		sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap, intType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 4).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 5).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(6));

		pMap = new int[]{-1,-1};
		iMap = new boolean[]{true, false};
		sp = SemanticPropUtil.createProjectionPropertiesDual(pMap, iMap, intType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 2).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 3).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 4).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(1, 5).contains(6));

	}

	// --------------------------------------------------------------------------------------------
	// Forwarded Fields Annotation
	// --------------------------------------------------------------------------------------------

	@Test
	public void testForwardedNoArrrowIndividualStrings() {
		String[] forwardedFields = {"f2","f3","f0"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
	}

	@Test
	public void testForwardedNoArrrowOneString() {
		String[] forwardedFields = {"f2;f3;f0"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));

		forwardedFields[0] = "2;3;0";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));

		forwardedFields[0] = "2;3;0;";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
	}

	@Test
	public void testForwardedNoArrrowSpaces() {
		String[] forwardedFields = {"  f2  ;   f3  ;  f0   "};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
	}

	@Test
	public void testForwardedWithArrowIndividualStrings() {
		String[] forwardedFields = { "f0->f1", "f1->f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
	}

	@Test
	public void testForwardedWithArrowOneString() {
		String[] forwardedFields = { "f0->f0;f1->f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));

		forwardedFields[0] = "0->0;1->2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, fiveIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
	}

	@Test
	public void testForwardedWithArrowSpaces() {
		String[] forwardedFields = { "  f0 ->  f0    ;   f1  -> f2 " };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
	}
	
	@Test
	public void testForwardedMixedOneString() {
		String[] forwardedFields = {"f2;f3;f0->f4;f4->f0"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(0));
	}

	@Test
	public void testForwardedBasicType() {
		String[] forwardedFields = {"f1->*"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, intType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);

		forwardedFields[0] = "*->f2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, intType, threeIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(2));

		forwardedFields[0] = "*->*";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, intType, intType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
	}

	@Test
	public void testForwardedWildCard() {
		String[] forwardedFields = { "*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).size() == 0);

		forwardedFields[0] = "*";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, deepNestedTupleType, deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(4));

	}

	@Test
	public void testForwardedNestedTuples() {
		String[] forwardedFields = { "f0->f0.f0; f1->f0.f1; f2->f0.f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, nestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));

		forwardedFields[0] = "f0.f0->f1.f0.f2; f0.f1->f2; f2->f1.f2; f1->f0";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedTupleType, deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(6));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(0));

		forwardedFields[0] = "0.0->1.0.2; 0.1->2; 2->1.2; 1->0";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedTupleType, deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(6));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(0));

		forwardedFields[0] = "f1.f0.*->f0.*; f0->f2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, deepNestedTupleType, nestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(4));

		forwardedFields[0] = "1.0.*->0.*; 0->2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, deepNestedTupleType, nestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(4));

		forwardedFields[0] = "f1.f0->f0; f0->f2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, deepNestedTupleType, nestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(4));

		forwardedFields[0] = "1.0->0; 0->2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, deepNestedTupleType, nestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(4));

		forwardedFields[0] = "f1.f0.f1; f1.f1; f2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, deepNestedTupleType, deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 6).contains(6));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).size() == 0);

		forwardedFields[0] = "f1.f0.*; f1.f2";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, deepNestedTupleType, deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 6).size() == 0);

	}

	@Test
	public void testForwardedPojo() {

		String[] forwardedFields = { "int1->int2; int3->int1; string1 " };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, pojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));

		forwardedFields[0] = "f1->int1; f0->int3 ";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, pojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(0));

		forwardedFields[0] = "int1->f2; int2->f0; int3->f1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, threeIntTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(1));

		forwardedFields[0] = "*->pojo1.*";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(4));

		forwardedFields[0] = "*->pojo1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(4));

		forwardedFields[0] = "int1; string1; int2->pojo1.int3";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(5));

		forwardedFields[0] = "pojo1.*->f2.*; int1->f1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedPojoType, pojoInTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(5));

		forwardedFields[0] = "f2.*->*";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoInTupleType, pojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(3));

		forwardedFields[0] = "pojo1->f2; int1->f1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedPojoType, pojoInTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(5));

		forwardedFields[0] = "f2->*";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoInTupleType, pojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(3));

		forwardedFields[0] = "int2; string1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, pojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);

		forwardedFields[0] = "pojo1.int1; string1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedPojoType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).size() == 0);

		forwardedFields[0] = "pojo1.*; int1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedPojoType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).size() == 0);

		forwardedFields[0] = "pojo1; int1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedPojoType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).size() == 0);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testInvalidPojoField() {
		String[] forwardedFields = { "invalidField" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedNoArrowOneStringInvalidDelimiter() {
		String[] forwardedFields = {"f2,f3,f0"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedSameTargetTwice() {
		String[] forwardedFields = {"f0->f2; f1->f2"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, fiveIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedInvalidTargetFieldType1() {
		String[] forwardedFields = {"f0->f0","f1->f2"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, threeMixedTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedInvalidTargetFieldType2() {
		String[] forwardedFields = { "f2.*->*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoInTupleType, pojo2Type);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedInvalidTargetFieldType3() {
		String[] forwardedFields = { "*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoInTupleType, pojo2Type);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedInvalidTargetFieldType4() {
		String[] forwardedFields = { "int1; string1" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoInTupleType, pojo2Type);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedInvalidTargetFieldType5() {
		String[] forwardedFields = {"f0.*->*"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, nestedTupleType, fiveIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedWildCardInvalidTypes1() {
		String[] forwardedFields = { "*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, fiveIntTupleType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedWildCardInvalidTypes2() {
		String[] forwardedFields = { "*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, fiveIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedWildCardInvalidTypes3() {
		String[] forwardedFields = { "*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, pojoType, pojo2Type);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedForwardWildCard() {
		String[] forwardedFields = { "f1->*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedInvalidExpression() {
		String[] forwardedFields = { "f0" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, intType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedForwardMultiFields() {
		String[] forwardedFields = { "f1->f0,f1" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedInvalidString() {
		String[] forwardedFields = { "notValid" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, null, threeIntTupleType, threeIntTupleType);
	}
	
	// --------------------------------------------------------------------------------------------
	// Non-Forwarded Fields Annotation
	// --------------------------------------------------------------------------------------------

	@Test
	public void testNonForwardedIndividualStrings() {
		String[] nonForwardedFields = { "f1", "f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
	}

	@Test
	public void testNonForwardedSingleString() {
		String[] nonForwardedFields = { "f1;f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);

		nonForwardedFields[0] = "f1;f2;";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
	}

	@Test
	public void testNonForwardedSpaces() {
		String[] nonForwardedFields = { " f1 ;   f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
	}

	@Test
	public void testNonForwardedNone() {
		String[] nonForwardedFields = { "" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
	}

	@Test
	public void testNonForwardedNestedTuple() {
		String[] nonForwardedFields = {"f1.f0.*; f1.f2; f0"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, deepNestedTupleType, deepNestedTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 6).contains(6));

		nonForwardedFields[0] = "f1.f0; f1.f2; f0";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, deepNestedTupleType, deepNestedTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 6).contains(6));

		nonForwardedFields[0] = "f2; f1.f1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, deepNestedTupleType, deepNestedTupleType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(5));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 6).size() == 0);

	}

	@Test
	public void testNonForwardedPojo() {
		String[] nonForwardedFields = { "int1; string1" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, pojoType, pojoType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).size() == 0);

	}

	@Test
	public void testNonForwardedNestedPojo() {
		String[] nonForwardedFields = { "int1; pojo1.*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, nestedPojoType, nestedPojoType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).contains(5));

		nonForwardedFields[0] = "pojo1.int2; string1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, nestedPojoType, nestedPojoType);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(1));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 3).contains(3));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 4).contains(4));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 5).size() == 0);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedInvalidTypes1() {
		String[] nonForwardedFields = { "f1; f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, nestedPojoType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedInvalidTypes2() {
		String[] nonForwardedFields = { "f1; f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, nestedPojoType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedInvalidTypes3() {
		String[] nonForwardedFields = { "f1; f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, fiveIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedInvalidTypes4() {
		String[] nonForwardedFields = { "f1; f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, fiveIntTupleType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedInvalidTypes5() {
		String[] nonForwardedFields = { "int1" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, pojoType, pojo2Type);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedInvalidNesting() {
		String[] nonForwardedFields = { "f0.f4" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, nestedTupleType, nestedTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedInvalidString() {
		String[] nonForwardedFields = { "notValid" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, null, threeIntTupleType, threeIntTupleType);
	}
	
	
	// --------------------------------------------------------------------------------------------
	// Read Fields Annotation
	// --------------------------------------------------------------------------------------------

	@Test
	public void testReadFieldsIndividualStrings() {
		String[] readFields = { "f1", "f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, threeIntTupleType, threeIntTupleType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 2);
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(1));
	}
	
	@Test
	public void testReadFieldsOneString() {
		String[] readFields = { "f1;f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, threeIntTupleType, threeIntTupleType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 2);
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(1));

		readFields[0] = "f1;f2;";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, threeIntTupleType, threeIntTupleType);

		fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 2);
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(1));
	}

	@Test
	public void testReadFieldsSpaces() {
		String[] readFields = { "  f1  ; f2   " };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, threeIntTupleType, threeIntTupleType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 2);
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(1));
	}

	@Test
	public void testReadFieldsBasic() {
		String[] readFields = { "*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, intType, intType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 1);
		Assert.assertTrue(fs.contains(0));

		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, intType, fiveIntTupleType);

		fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 1);
		Assert.assertTrue(fs.contains(0));
	}

	@Test
	public void testReadFieldsNestedTuples() {
		String[] readFields = { "f0.f1; f0.f2; f2" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, nestedTupleType, intType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 3);
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(4));

		readFields[0] = "f0;f1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, nestedTupleType, intType);

		fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 4);
		Assert.assertTrue(fs.contains(0));
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(3));
	}

	@Test
	public void testReadFieldsNestedTupleWildCard() {
		String[] readFields = { "*" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, nestedTupleType, intType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 5);
		Assert.assertTrue(fs.contains(0));
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(3));
		Assert.assertTrue(fs.contains(4));

		readFields[0] = "f0.*;f1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, nestedTupleType, intType);

		fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 4);
		Assert.assertTrue(fs.contains(0));
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(3));

	}

	@Test
	public void testReadFieldsPojo() {
		String[] readFields = { "int2; string1" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, pojoType, threeIntTupleType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 2);
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(3));

		readFields[0] = "*";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, pojoType, intType);

		fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 4);
		Assert.assertTrue(fs.contains(0));
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(3));
	}

	@Test
	public void testReadFieldsNestedPojo() {
		String[] readFields = { "pojo1.int2; string1; pojo1.string1" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, nestedPojoType, intType);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 3);
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(4));
		Assert.assertTrue(fs.contains(5));

		readFields[0] = "pojo1.*";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, nestedPojoType, intType);

		fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 4);
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(3));
		Assert.assertTrue(fs.contains(4));

		readFields[0] = "pojo1";
		sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, nestedPojoType, intType);

		fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 4);
		Assert.assertTrue(fs.contains(1));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(3));
		Assert.assertTrue(fs.contains(4));
	}

	@Test
	public void testReadFieldsPojoInTuple() {
		String[] readFields = { "f0; f2.int1; f2.string1" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, pojoInTupleType, pojo2Type);

		FieldSet fs = sp.getReadFields(0);
		Assert.assertTrue(fs.size() == 3);
		Assert.assertTrue(fs.contains(0));
		Assert.assertTrue(fs.contains(2));
		Assert.assertTrue(fs.contains(5));
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testReadFieldsInvalidString() {
		String[] readFields = { "notValid" };
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, null, readFields, threeIntTupleType, threeIntTupleType);
	}
	
	// --------------------------------------------------------------------------------------------
	// Two Inputs
	// --------------------------------------------------------------------------------------------
	
	@Test
	public void testForwardedDual() {
		String[] forwardedFieldsFirst = { "f1->f2; f2->f3" };
		String[] forwardedFieldsSecond = { "f1->f1; f2->f0" };
		DualInputSemanticProperties dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, forwardedFieldsFirst, forwardedFieldsSecond, null,
				null, null, null, fourIntTupleType, fourIntTupleType, fourIntTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).contains(3));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 3).size() == 0);

		forwardedFieldsFirst[0] = "f1->f0;f3->f1";
		forwardedFieldsSecond[0] = "*->f2.*";
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, forwardedFieldsFirst, forwardedFieldsSecond, null,
				null, null, null, fourIntTupleType, pojoType, pojoInTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 3).contains(1));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 0).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).contains(3));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).contains(4));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 3).contains(5));

		forwardedFieldsFirst[0] = "f1.f0.f2->int1; f2->pojo1.int3";
		forwardedFieldsSecond[0] = "string1; int2->pojo1.int1; int1->pojo1.int2";
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, forwardedFieldsFirst, forwardedFieldsSecond, null,
				null, null, null, deepNestedTupleType, pojoType, nestedPojoType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 3).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 4).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 5).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 6).contains(3));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 0).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 3).contains(5));

		String[] forwardedFieldsFirst2 = { "f1.f0.f2->int1", "f2->pojo1.int3" };
		String[] forwardedFieldsSecond2 = { "string1", "int2->pojo1.int1", "int1->pojo1.int2" };
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, forwardedFieldsFirst2, forwardedFieldsSecond2, null,
				null, null, null, deepNestedTupleType, pojoType, nestedPojoType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 3).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 4).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 5).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 6).contains(3));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 0).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 3).contains(5));
	}


	@Test
	public void testNonForwardedDual() {
		String[] nonNorwardedFieldsFirst = { "f1;f2" };
		String[] nonNorwardedFieldsSecond = { "f0" };
		DualInputSemanticProperties dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				nonNorwardedFieldsFirst, nonNorwardedFieldsSecond, null, null, threeIntTupleType, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).contains(2));

		nonNorwardedFieldsFirst[0] = "f1";
		nonNorwardedFieldsSecond[0] = "";
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				nonNorwardedFieldsFirst, null, null, null, threeIntTupleType, fiveIntTupleType, threeIntTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).size() == 0);

		nonNorwardedFieldsFirst[0] = "";
		nonNorwardedFieldsSecond[0] = "f2;f0";
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				null, nonNorwardedFieldsSecond, null, null, fiveIntTupleType, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).size() == 0);

		String[] nonForwardedFields = {"f1", "f3"};
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				nonForwardedFields, null, null, null, fiveIntTupleType, threeIntTupleType, fiveIntTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 3).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 4).contains(4));

		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				null, nonForwardedFields, null, null, threeIntTupleType, fiveIntTupleType, fiveIntTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(1, 0).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 3).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 4).contains(4));
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedDualInvalidTypes1() {

		String[] nonNorwardedFieldsFirst = { "f1" };
		DualInputSemanticProperties dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				nonNorwardedFieldsFirst, null, null, null, fiveIntTupleType, threeIntTupleType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testNonForwardedDualInvalidTypes2() {

		String[] nonNorwardedFieldsSecond = { "f1" };
		DualInputSemanticProperties dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				null, nonNorwardedFieldsSecond, null, null, threeIntTupleType, pojoInTupleType, threeIntTupleType);
	}

	@Test
	public void testReadFieldsDual() {
		String[] readFieldsFirst = {"f1;f2"};
		String[] readFieldsSecond = {"f0"};
		DualInputSemanticProperties dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				null, null, readFieldsFirst, readFieldsSecond, threeIntTupleType, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(dsp.getReadFields(0).size() == 2);
		Assert.assertTrue(dsp.getReadFields(0).contains(1));
		Assert.assertTrue(dsp.getReadFields(0).contains(2));
		Assert.assertTrue(dsp.getReadFields(1).size() == 1);
		Assert.assertTrue(dsp.getReadFields(1).contains(0));

		readFieldsFirst[0] = "f0.*; f2";
		readFieldsSecond[0] = "int1; string1";
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				null, null, readFieldsFirst, readFieldsSecond, nestedTupleType, pojoType, threeIntTupleType);

		Assert.assertTrue(dsp.getReadFields(0).size() == 4);
		Assert.assertTrue(dsp.getReadFields(0).contains(0));
		Assert.assertTrue(dsp.getReadFields(0).contains(1));
		Assert.assertTrue(dsp.getReadFields(0).contains(2));
		Assert.assertTrue(dsp.getReadFields(0).contains(4));
		Assert.assertTrue(dsp.getReadFields(1).size() == 2);
		Assert.assertTrue(dsp.getReadFields(1).contains(0));
		Assert.assertTrue(dsp.getReadFields(1).contains(3));

		readFieldsFirst[0] = "pojo1.int2; string1";
		readFieldsSecond[0] = "f2.int2";
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				null, null, readFieldsFirst, readFieldsSecond, nestedPojoType, pojoInTupleType, threeIntTupleType);

		Assert.assertTrue(dsp.getReadFields(0).size() == 2);
		Assert.assertTrue(dsp.getReadFields(0).contains(2));
		Assert.assertTrue(dsp.getReadFields(0).contains(5));
		Assert.assertTrue(dsp.getReadFields(1).size() == 1);
		Assert.assertTrue(dsp.getReadFields(1).contains(3));

		String[] readFields = {"f0", "f2", "f4"};
		dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, null, null,
				null, null, readFields, readFields, fiveIntTupleType, fiveIntTupleType, threeIntTupleType);

		Assert.assertTrue(dsp.getReadFields(0).size() == 3);
		Assert.assertTrue(dsp.getReadFields(0).contains(0));
		Assert.assertTrue(dsp.getReadFields(0).contains(2));
		Assert.assertTrue(dsp.getReadFields(0).contains(4));
		Assert.assertTrue(dsp.getReadFields(1).size() == 3);
		Assert.assertTrue(dsp.getReadFields(1).contains(0));
		Assert.assertTrue(dsp.getReadFields(1).contains(2));
		Assert.assertTrue(dsp.getReadFields(1).contains(4));
	}

	// --------------------------------------------------------------------------------------------
	// Mixed Annotations
	// --------------------------------------------------------------------------------------------

	@Test
	public void testForwardedRead() {
		String[] forwardedFields = { "f0->f0;f1->f2" };
		String[] readFields = {"f0; f2"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, forwardedFields, null, readFields, threeIntTupleType, fiveIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(sp.getReadFields(0).size() == 2);
		Assert.assertTrue(sp.getReadFields(0).contains(0));
		Assert.assertTrue(sp.getReadFields(0).contains(2));
	}

	@Test
	public void testNonForwardedRead() {
		String[] nonForwardedFields = { "f1;f2" };
		String[] readFields = {"f0; f2"};
		SingleInputSemanticProperties sp = new SingleInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsSingleFromString(sp, null, nonForwardedFields, readFields, threeIntTupleType, threeIntTupleType);

		Assert.assertTrue(sp.getForwardingTargetFields(0, 0).contains(0));
		Assert.assertTrue(sp.getForwardingTargetFields(0, 1).size() == 0);
		Assert.assertTrue(sp.getForwardingTargetFields(0, 2).size() == 0);
		Assert.assertTrue(sp.getReadFields(0).size() == 2);
		Assert.assertTrue(sp.getReadFields(0).contains(0));
		Assert.assertTrue(sp.getReadFields(0).contains(2));
	}

	@Test
	public void testForwardedReadDual() {
		String[] forwardedFieldsFirst = { "f1->f2; f2->f3" };
		String[] forwardedFieldsSecond = { "f1->f1; f2->f0" };
		String[] readFieldsFirst = {"0;2"};
		String[] readFieldsSecond = {"1"};
		DualInputSemanticProperties dsp = new DualInputSemanticProperties();
		SemanticPropUtil.getSemanticPropsDualFromString(dsp, forwardedFieldsFirst, forwardedFieldsSecond, null,
				null, readFieldsFirst, readFieldsSecond, fourIntTupleType, fourIntTupleType, fourIntTupleType);

		Assert.assertTrue(dsp.getForwardingTargetFields(0, 1).contains(2));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 2).contains(3));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 1).contains(1));
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 2).contains(0));
		Assert.assertTrue(dsp.getForwardingTargetFields(0, 0).size() == 0);
		Assert.assertTrue(dsp.getForwardingTargetFields(1, 3).size() == 0);
		Assert.assertTrue(dsp.getReadFields(0).size() == 2);
		Assert.assertTrue(dsp.getReadFields(0).contains(0));
		Assert.assertTrue(dsp.getReadFields(0).contains(2));
		Assert.assertTrue(dsp.getReadFields(1).size() == 1);
		Assert.assertTrue(dsp.getReadFields(1).contains(1));

	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedNonForwardedCheck() {
		String[] forwarded = { "1" };
		String[] nonForwarded = { "1" };
		SemanticPropUtil.getSemanticPropsSingleFromString(new SingleInputSemanticProperties(), forwarded, nonForwarded, null, threeIntTupleType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedNonForwardedFirstCheck() {
		String[] forwarded = { "1" };
		String[] nonForwarded = { "1" };
		SemanticPropUtil.getSemanticPropsDualFromString(new DualInputSemanticProperties(), forwarded, null, nonForwarded, null, null, null,
				threeIntTupleType, threeIntTupleType, threeIntTupleType);
	}

	@Test(expected = InvalidSemanticAnnotationException.class)
	public void testForwardedNonForwardedSecondCheck() {
		String[] forwarded = { "1" };
		String[] nonForwarded = { "1" };
		SemanticPropUtil.getSemanticPropsDualFromString(new DualInputSemanticProperties(), null, forwarded, null, nonForwarded, null, null,
				threeIntTupleType, threeIntTupleType, threeIntTupleType);
	}

	// --------------------------------------------------------------------------------------------
	// Pojo Type Classes
	// --------------------------------------------------------------------------------------------

	public static class TestPojo {

		public int int1;
		public int int2;
		public int int3;
		public String string1;
	}

	public static class TestPojo2 {

		public int myInt1;
		public int myInt2;
		public int myInt3;
		public String myString1;
	}

	public static class NestedTestPojo{

		public int int1;
		public TestPojo pojo1;
		public String string1;

	}
}
