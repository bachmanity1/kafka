/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.processor.internals.assignment;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.apache.kafka.common.utils.Utils.mkEntry;
import static org.apache.kafka.common.utils.Utils.mkMap;
import static org.apache.kafka.common.utils.Utils.mkSet;
import static org.apache.kafka.common.utils.Utils.mkSortedSet;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_0_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_0_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_0_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_0_NAME;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_1_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_1_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_1_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.CHANGELOG_TP_1_NAME;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.EMPTY_CLIENT_TAGS;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.NODE_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.NODE_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.NODE_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.NODE_3;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.NO_RACK_NODE;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.PI_0_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.PI_0_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.PI_0_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.PI_1_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.PI_1_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.PI_1_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.RACK_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.RACK_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.RACK_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.RACK_3;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.RACK_4;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.REPLICA_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.REPLICA_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.REPLICA_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.REPLICA_3;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.SUBTOPOLOGY_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TASK_0_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TASK_0_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TASK_0_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TASK_1_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TASK_1_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TASK_1_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TP_0_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TP_0_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TP_0_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TP_0_NAME;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TP_1_0;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TP_1_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.TP_1_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.UUID_1;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.UUID_2;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.UUID_3;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.UUID_4;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.UUID_5;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.UUID_6;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.UUID_7;
import static org.apache.kafka.streams.processor.internals.assignment.AssignmentTestUtils.uuidForInt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.utils.MockTime;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.StreamsConfig.InternalConfig;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.processor.internals.InternalTopicManager;
import org.apache.kafka.streams.processor.internals.TopologyMetadata.Subtopology;
import org.apache.kafka.streams.processor.internals.assignment.AssignorConfiguration.AssignmentConfigs;
import org.apache.kafka.test.MockClientSupplier;
import org.apache.kafka.test.MockInternalTopicManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public class RackAwareTaskAssignorTest {
    private static final String USER_END_POINT = "localhost:8080";
    private static final String APPLICATION_ID = "stream-partition-assignor-test";
    private static final String TOPIC_PREFIX = "topic";
    private static final String CHANGELOG_TOPIC_PREFIX = "changelog-topic";
    private static final String RACK_PREFIX = "rack";

    private final MockTime time = new MockTime();
    private final StreamsConfig streamsConfig = new StreamsConfig(configProps());
    private final MockClientSupplier mockClientSupplier = new MockClientSupplier();

    private int trafficCost;
    private int nonOverlapCost;

    @Parameter
    public boolean stateful;

    @Parameterized.Parameters(name = "stateful={0}")
    public static Collection<Object[]> getParamStoreType() {
        return asList(new Object[][] {
            {true},
            {false}
        });
    }

    private final MockInternalTopicManager mockInternalTopicManager = new MockInternalTopicManager(
            time,
            streamsConfig,
            mockClientSupplier.restoreConsumer,
            false
    );

    @Before
    public void setUp() {
        if (stateful) {
            trafficCost = 10;
            nonOverlapCost = 1;
        } else {
            trafficCost = 1;
            nonOverlapCost = 0;
        }
    }

    private Map<String, Object> configProps() {
        return configProps(0);
    }

    private Map<String, Object> configProps(final int standbyNum) {
        final Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        configurationMap.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, USER_END_POINT);
        configurationMap.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, standbyNum);

        final ReferenceContainer referenceContainer = new ReferenceContainer();
        /*
        referenceContainer.mainConsumer = consumer;
        referenceContainer.adminClient = adminClient;
        referenceContainer.taskManager = taskManager;
        referenceContainer.streamsMetadataState = streamsMetadataState;
        referenceContainer.time = time;
        */
        configurationMap.put(InternalConfig.REFERENCE_CONTAINER_PARTITION_ASSIGNOR, referenceContainer);
        return configurationMap;
    }

    @Test
    public void shouldDisableActiveWhenMissingClusterInfo() {
        final RackAwareTaskAssignor assignor = spy(new RackAwareTaskAssignor(
            getClusterForTopic0(),
            getTaskTopicPartitionMapForTask0(true),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        ));

        // False since partitionWithoutInfo10 is missing in cluster metadata
        assertFalse(assignor.canEnableRackAwareAssignor());
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(false));
        verify(assignor, never()).populateTopicsToDescribe(anySet(), eq(true));
        assertFalse(assignor.populateTopicsToDescribe(new HashSet<>(), false));
    }

    @Test
    public void shouldDisableActiveWhenRackMissingInNode() {
        final RackAwareTaskAssignor assignor = spy(new RackAwareTaskAssignor(
            getClusterWithPartitionMissingRack(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        ));

        // False since nodeMissingRack has one node which doesn't have rack
        assertFalse(assignor.canEnableRackAwareAssignor());
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(false));
        verify(assignor, never()).populateTopicsToDescribe(anySet(), eq(true));
        assertFalse(assignor.populateTopicsToDescribe(new HashSet<>(), false));
    }

    @Test
    public void shouldReturnInvalidClientRackWhenRackMissingInClientConsumer() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(true),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );
        // False since process1 doesn't have rackId
        assertFalse(assignor.validClientRack());
    }

    @Test
    public void shouldReturnFalseWhenRackMissingInProcess() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessWithNoConsumerRacks(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );
        // False since process1 doesn't have rackId
        assertFalse(assignor.validClientRack());
    }

    @Test
    public void shouldPopulateRacksForProcess() {
        // Throws since process1 doesn't have rackId
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );
        final Map<UUID, String> racksForProcess = assignor.racksForProcess();
        assertEquals(mkMap(mkEntry(UUID_1, RACK_1)), racksForProcess);
    }

    @Test
    public void shouldReturnInvalidClientRackWhenRackDiffersInSameProcess() {
        final Map<UUID, Map<String, Optional<String>>> processRacks = new HashMap<>();

        // Different consumers in same process have different rack ID. This shouldn't happen.
        // If happens, there's a bug somewhere
        processRacks.computeIfAbsent(UUID_1, k -> new HashMap<>()).put("consumer1", Optional.of("rack1"));
        processRacks.computeIfAbsent(UUID_1, k -> new HashMap<>()).put("consumer2", Optional.of("rack2"));

        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            processRacks,
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        assertFalse(assignor.validClientRack());
    }

    @Test
    public void shouldEnableRackAwareAssignorWithoutDescribingTopics() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        // partitionWithoutInfo00 has rackInfo in cluster metadata
        assertTrue(assignor.canEnableRackAwareAssignor());
    }

    @Test
    public void shouldEnableRackAwareAssignorWithCacheResult() {
        final RackAwareTaskAssignor assignor = spy(new RackAwareTaskAssignor(
            getClusterForTopic0(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        ));

        // partitionWithoutInfo00 has rackInfo in cluster metadata
        assertTrue(assignor.canEnableRackAwareAssignor());
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(false));

        // Should use cache result
        Mockito.reset(assignor);
        assertTrue(assignor.canEnableRackAwareAssignor());
        verify(assignor, never()).populateTopicsToDescribe(anySet(), eq(false));
    }

    @Test
    public void shouldEnableRackAwareAssignorWithDescribingTopics() {
        final MockInternalTopicManager spyTopicManager = spy(mockInternalTopicManager);
        doReturn(
            Collections.singletonMap(
                TP_0_NAME,
                Collections.singletonList(
                    new TopicPartitionInfo(0, NODE_0, Arrays.asList(REPLICA_1), Collections.emptyList())
                )
            )
        ).when(spyTopicManager).getTopicPartitionInfo(Collections.singleton(TP_0_NAME));

        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterWithNoNode(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            spyTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        assertTrue(assignor.canEnableRackAwareAssignor());
    }

    @Test
    public void shouldEnableRackAwareAssignorWithStandbyDescribingTopics() {
        final MockInternalTopicManager spyTopicManager = spy(mockInternalTopicManager);
        doReturn(
            Collections.singletonMap(
                TP_0_NAME,
                Collections.singletonList(
                    new TopicPartitionInfo(0, NODE_0, Arrays.asList(REPLICA_1), Collections.emptyList())
                )
            )
        ).when(spyTopicManager).getTopicPartitionInfo(Collections.singleton(TP_0_NAME));

        doReturn(
            Collections.singletonMap(
                CHANGELOG_TP_0_NAME,
                Collections.singletonList(
                    new TopicPartitionInfo(0, NODE_0, Arrays.asList(REPLICA_1), Collections.emptyList())
                )
            )
        ).when(spyTopicManager).getTopicPartitionInfo(Collections.singleton(CHANGELOG_TP_0_NAME));

        final StreamsConfig streamsConfig1 = new StreamsConfig(configProps(1));
        final RackAwareTaskAssignor assignor = spy(new RackAwareTaskAssignor(
            getClusterWithNoNode(),
            getTaskTopicPartitionMapForTask0(),
            getTaskChangeLogTopicPartitionMapForTask0(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            spyTopicManager,
            new AssignorConfiguration(streamsConfig1.originals()).assignmentConfigs()
        ));

        assertTrue(assignor.canEnableRackAwareAssignor());
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(false));
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(true));

        final Map<TopicPartition, Set<String>> racksForPartition = assignor.racksForPartition();
        final Map<TopicPartition, Set<String>> expected = mkMap(
            mkEntry(TP_0_0, mkSet(RACK_1, RACK_2)),
            mkEntry(CHANGELOG_TP_0_0, mkSet(RACK_1, RACK_2))
        );
        assertEquals(expected, racksForPartition);
    }

    @Test
    public void shouldDisableRackAwareAssignorWithStandbyDescribingTopicsFailure() {
        final MockInternalTopicManager spyTopicManager = spy(mockInternalTopicManager);
        doReturn(
            Collections.singletonMap(
                TP_0_NAME,
                Collections.singletonList(
                    new TopicPartitionInfo(0, NODE_0, Arrays.asList(REPLICA_1), Collections.emptyList())
                )
            )
        ).when(spyTopicManager).getTopicPartitionInfo(Collections.singleton(TP_0_NAME));

        doThrow(new TimeoutException("Timeout describing topic")).when(spyTopicManager).getTopicPartitionInfo(Collections.singleton(
            CHANGELOG_TP_0_NAME));

        final StreamsConfig streamsConfig1 = new StreamsConfig(configProps(1));
        final RackAwareTaskAssignor assignor = spy(new RackAwareTaskAssignor(
            getClusterWithNoNode(),
            getTaskTopicPartitionMapForTask0(),
            getTaskChangeLogTopicPartitionMapForTask0(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            spyTopicManager,
            new AssignorConfiguration(streamsConfig1.originals()).assignmentConfigs()
        ));

        assertFalse(assignor.canEnableRackAwareAssignor());
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(false));
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(true));
    }

    @Test
    public void shouldDisableRackAwareAssignorWithDescribingTopicsFailure() {
        final MockInternalTopicManager spyTopicManager = spy(mockInternalTopicManager);
        doThrow(new TimeoutException("Timeout describing topic")).when(spyTopicManager).getTopicPartitionInfo(Collections.singleton(
            TP_0_NAME));

        final RackAwareTaskAssignor assignor = spy(new RackAwareTaskAssignor(
            getClusterWithNoNode(),
            getTaskTopicPartitionMapForTask0(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForProcess0(),
            spyTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        ));

        assertFalse(assignor.canEnableRackAwareAssignor());
        verify(assignor, times(1)).populateTopicsToDescribe(anySet(), eq(false));
        verify(assignor, never()).populateTopicsToDescribe(anySet(), eq(true));
        assertTrue(assignor.populateTopicsToDescribe(new HashSet<>(), false));
    }

    @Test
    public void shouldOptimizeEmptyActiveTasks() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState1.assignActiveTasks(mkSet(TASK_0_1, TASK_1_1));

        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_1, clientState1)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet();

        assertTrue(assignor.canEnableRackAwareAssignor());
        final long originalCost = assignor.activeTasksCost(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(0, originalCost);

        final long cost = assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(0, cost);

        assertEquals(mkSet(TASK_0_1, TASK_1_1), clientState1.activeTasks());
    }

    @Test
    public void shouldOptimizeActiveTasks() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState3 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState1.assignActiveTasks(mkSet(TASK_0_1, TASK_1_1));
        clientState2.assignActive(TASK_1_0);
        clientState3.assignActive(TASK_0_0);

        // task_0_0 has same rack as UUID_1
        // task_0_1 has same rack as UUID_2 and UUID_3
        // task_1_0 has same rack as UUID_1 and UUID_3
        // task_1_1 has same rack as UUID_2
        // Optimal assignment is UUID_1: {0_0, 1_0}, UUID_2: {1_1}, UUID_3: {0_1} which result in no cross rack traffic
        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_1, clientState1),
            mkEntry(UUID_2, clientState2),
            mkEntry(UUID_3, clientState3)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet(TASK_0_0, TASK_0_1, TASK_1_0, TASK_1_1);

        assertTrue(assignor.canEnableRackAwareAssignor());
        int expected = stateful ? 40 : 4;
        final long originalCost = assignor.activeTasksCost(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(expected, originalCost);

        expected = stateful ? 4 : 0;
        final long cost = assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(expected, cost);

        assertEquals(mkSet(TASK_0_0, TASK_1_0), clientState1.activeTasks());
        assertEquals(mkSet(TASK_1_1), clientState2.activeTasks());
        assertEquals(mkSet(TASK_0_1), clientState3.activeTasks());
    }

    @Test
    public void shouldOptimizeRandomActive() {
        final int nodeSize = 30;
        final int tpSize = 40;
        final int clientSize = 30;
        final SortedMap<TaskId, Set<TopicPartition>> taskTopicPartitionMap = getTaskTopicPartitionMap(tpSize, false);
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getRandomCluster(nodeSize, tpSize),
            taskTopicPartitionMap,
            mkMap(),
            getTopologyGroupTaskMap(),
            getRandomProcessRacks(clientSize, nodeSize),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final SortedMap<UUID, ClientState> clientStateMap = getRandomClientState(clientSize, tpSize, 1);
        final SortedSet<TaskId> taskIds = (SortedSet<TaskId>) taskTopicPartitionMap.keySet();

        final Map<UUID, Integer> clientTaskCount = clientTaskCount(clientStateMap, ClientState::activeTaskCount);

        assertTrue(assignor.canEnableRackAwareAssignor());
        final long originalCost = assignor.activeTasksCost(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        final long cost = assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertThat(cost, lessThanOrEqualTo(originalCost));

        for (final Entry<UUID, ClientState> entry : clientStateMap.entrySet()) {
            assertEquals((int) clientTaskCount.get(entry.getKey()), entry.getValue().activeTasks().size());
        }
    }

    @Test
    public void shouldMaintainOriginalAssignment() {
        final int nodeSize = 20;
        final int tpSize = 40;
        final int clientSize = 30;
        final SortedMap<TaskId, Set<TopicPartition>> taskTopicPartitionMap = getTaskTopicPartitionMap(tpSize, false);
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getRandomCluster(nodeSize, tpSize),
            taskTopicPartitionMap,
            mkMap(),
            getTopologyGroupTaskMap(),
            getRandomProcessRacks(clientSize, nodeSize),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final SortedMap<UUID, ClientState> clientStateMap = getRandomClientState(clientSize, tpSize, 1);
        final SortedSet<TaskId> taskIds = (SortedSet<TaskId>) taskTopicPartitionMap.keySet();

        final Map<TaskId, UUID> taskClientMap = new HashMap<>();
        for (final Entry<UUID, ClientState> entry : clientStateMap.entrySet()) {
            entry.getValue().activeTasks().forEach(t -> taskClientMap.put(t, entry.getKey()));
        }
        assertEquals(taskIds.size(), taskClientMap.size());

        // Because trafficCost is 0, original assignment should be maintained
        assertTrue(assignor.canEnableRackAwareAssignor());
        final long originalCost = assignor.activeTasksCost(taskIds, clientStateMap, 0, 1);
        assertEquals(0, originalCost);

        final long cost = assignor.optimizeActiveTasks(taskIds, clientStateMap, 0, 1);
        assertEquals(0, cost);

        // Make sure assignment doesn't change
        for (final Entry<TaskId, UUID> entry : taskClientMap.entrySet()) {
            final ClientState clientState = clientStateMap.get(entry.getValue());
            assertTrue(clientState.hasAssignedTask(entry.getKey()));
        }
    }

    @Test
    public void shouldOptimizeActiveTasksWithMoreClients() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState3 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState2.assignActive(TASK_1_0);
        clientState3.assignActive(TASK_0_0);

        // task_0_0 has same rack as UUID_1 and UUID_2
        // task_1_0 has same rack as UUID_1 and UUID_3
        // Optimal assignment is UUID_1: {}, UUID_2: {0_0}, UUID_3: {1_0} which result in no cross rack traffic
        // and keeps UUID_1 empty since it was originally empty
        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_1, clientState1),
            mkEntry(UUID_2, clientState2),
            mkEntry(UUID_3, clientState3)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet(TASK_0_0, TASK_1_0);

        assertTrue(assignor.canEnableRackAwareAssignor());
        final long originalCost = assignor.activeTasksCost(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        int expected = stateful ? 20 : 2;
        assertEquals(expected, originalCost);

        expected = stateful ? 2 : 0;
        final long cost = assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(expected, cost);

        // UUID_1 remains empty
        assertEquals(mkSet(), clientState1.activeTasks());
        assertEquals(mkSet(TASK_0_0), clientState2.activeTasks());
        assertEquals(mkSet(TASK_1_0), clientState3.activeTasks());
    }

    @Test
    public void shouldOptimizeActiveTasksWithMoreClientsWithMoreThanOneTask() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState3 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState2.assignActiveTasks(mkSet(TASK_0_1, TASK_1_0));
        clientState3.assignActive(TASK_0_0);

        // task_0_0 has same rack as UUID_1 and UUID_2
        // task_0_1 has same rack as UUID_2 and UUID_3
        // task_1_0 has same rack as UUID_1 and UUID_3
        // Optimal assignment is UUID_1: {}, UUID_2: {0_0, 0_1}, UUID_3: {1_0} which result in no cross rack traffic
        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_1, clientState1),
            mkEntry(UUID_2, clientState2),
            mkEntry(UUID_3, clientState3)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet(TASK_0_0, TASK_0_1, TASK_1_0);

        assertTrue(assignor.canEnableRackAwareAssignor());
        final long originalCost = assignor.activeTasksCost(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        int expected = stateful ? 20 : 2;
        assertEquals(expected, originalCost);

        expected = stateful ? 2 : 0;
        final long cost = assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(expected, cost);

        // Because original assignment is not balanced (3 tasks but client 0 has no task), we maintain it
        assertEquals(mkSet(), clientState1.activeTasks());
        assertEquals(mkSet(TASK_0_0, TASK_0_1), clientState2.activeTasks());
        assertEquals(mkSet(TASK_1_0), clientState3.activeTasks());
    }

    @Test
    public void shouldBalanceAssignmentWithMoreCost() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState1.assignActiveTasks(mkSet(TASK_0_0, TASK_1_1));
        clientState2.assignActive(TASK_0_1);

        // task_0_0 has same rack as UUID_2
        // task_0_1 has same rack as UUID_2
        // task_1_1 has same rack as UUID_2
        // UUID_5 is not in same rack as any task
        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_2, clientState1),
            mkEntry(UUID_5, clientState2)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet(TASK_0_0, TASK_0_1, TASK_1_1);

        assertTrue(assignor.canEnableRackAwareAssignor());
        final int expectedCost = stateful ? 10 : 1;
        final long originalCost = assignor.activeTasksCost(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(expectedCost, originalCost);

        final long cost = assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost);
        assertEquals(expectedCost, cost);

        // Even though assigning all tasks to UUID_2 will result in min cost, but it's not balanced
        // assignment. That's why TASK_0_1 is still assigned to UUID_5
        assertEquals(mkSet(TASK_0_0, TASK_1_1), clientState1.activeTasks());
        assertEquals(mkSet(TASK_0_1), clientState2.activeTasks());
    }

    @Test
    public void shouldThrowIfMissingCallcanEnableRackAwareAssignor() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState1.assignActiveTasks(mkSet(TASK_0_0, TASK_1_1));
        clientState2.assignActive(TASK_0_1);

        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_2, clientState1),
            mkEntry(UUID_5, clientState2)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet(TASK_0_0, TASK_0_1, TASK_1_1);
        final Exception exception = assertThrows(IllegalStateException.class,
            () -> assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost));
        Assertions.assertEquals("TopicPartition topic0-0 has no rack information. "
            + "Maybe forgot to call canEnableRackAwareAssignor first", exception.getMessage());
    }

    @Test
    public void shouldThrowIfTaskInMultipleClients() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState1.assignActiveTasks(mkSet(TASK_0_0, TASK_1_1));
        clientState2.assignActiveTasks(mkSet(TASK_0_1, TASK_1_1));

        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_2, clientState1),
            mkEntry(UUID_5, clientState2)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet(TASK_0_0, TASK_0_1, TASK_1_1);
        assertTrue(assignor.canEnableRackAwareAssignor());
        final Exception exception = assertThrows(IllegalArgumentException.class,
            () -> assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost));
        Assertions.assertEquals(
            "Task 1_1 assigned to multiple clients 00000000-0000-0000-0000-000000000005, "
                + "00000000-0000-0000-0000-000000000002", exception.getMessage());
    }

    @Test
    public void shouldThrowIfTaskMissingInClients() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManager,
            new AssignorConfiguration(streamsConfig.originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1);

        clientState1.assignActiveTasks(mkSet(TASK_0_0, TASK_1_1));
        clientState2.assignActive(TASK_0_1);

        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_2, clientState1),
            mkEntry(UUID_5, clientState2)
        ));
        final SortedSet<TaskId> taskIds = mkSortedSet(TASK_0_0, TASK_0_1, TASK_1_0, TASK_1_1);
        assertTrue(assignor.canEnableRackAwareAssignor());
        final Exception exception = assertThrows(IllegalArgumentException.class,
            () -> assignor.optimizeActiveTasks(taskIds, clientStateMap, trafficCost, nonOverlapCost));
        Assertions.assertEquals(
            "Task 1_0 not assigned to any client", exception.getMessage());
    }

    @Test
    public void shouldNotCrashForEmptyStandby() {
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            mkMap(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManagerForChangelog(),
            new AssignorConfiguration(new StreamsConfig(configProps(1)).originals()).assignmentConfigs()
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_2);
        final ClientState clientState3 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_3);

        clientState1.assignActiveTasks(mkSet(TASK_0_1, TASK_1_1));
        clientState2.assignActive(TASK_1_0);
        clientState3.assignActive(TASK_0_0);

        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_1, clientState1),
            mkEntry(UUID_2, clientState2),
            mkEntry(UUID_3, clientState3)
        ));

        final long originalCost = assignor.standByTasksCost(new TreeSet<>(), clientStateMap, 10, 1);
        assertEquals(0, originalCost);

        final long cost = assignor.optimizeStandbyTasks(clientStateMap, 10, 1,
            (source, destination, task, clientStates) -> true);
        assertEquals(0, cost);
    }

    @Test
    public void shouldOptimizeStandbyTasksWhenTasksAllMovable() {
        final int replicaCount = 2;
        final AssignmentConfigs assignorConfiguration = new AssignorConfiguration(new StreamsConfig(configProps(replicaCount)).originals()).assignmentConfigs();
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            getTaskChangelogMapForAllTasks(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManagerForChangelog(),
            assignorConfiguration
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_2);
        final ClientState clientState3 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_3);
        final ClientState clientState4 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_4);
        final ClientState clientState5 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_6);
        final ClientState clientState6 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_7);

        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_1, clientState1),
            mkEntry(UUID_2, clientState2),
            mkEntry(UUID_3, clientState3),
            mkEntry(UUID_4, clientState4),
            mkEntry(UUID_6, clientState5),
            mkEntry(UUID_7, clientState6)
        ));

        clientState1.assignActive(TASK_0_0);
        clientState2.assignActive(TASK_0_1);
        clientState3.assignActive(TASK_1_0);
        clientState4.assignActive(TASK_1_1);
        clientState5.assignActive(TASK_0_2);
        clientState6.assignActive(TASK_1_2);

        clientState1.assignStandbyTasks(mkSet(TASK_0_1, TASK_1_1)); // Cost 10
        clientState2.assignStandbyTasks(mkSet(TASK_0_0, TASK_1_0)); // Cost 10
        clientState3.assignStandbyTasks(mkSet(TASK_0_0, TASK_0_2)); // Cost 20
        clientState4.assignStandbyTasks(mkSet(TASK_0_1, TASK_1_2)); // Cost 10
        clientState5.assignStandbyTasks(mkSet(TASK_1_0, TASK_1_2)); // Cost 10
        clientState6.assignStandbyTasks(mkSet(TASK_0_2, TASK_1_1)); // Cost 10

        final SortedSet<TaskId> taskIds = new TreeSet<>(mkSet(TASK_0_0, TASK_0_1, TASK_0_2, TASK_1_0, TASK_1_1, TASK_1_2));
        final Map<UUID, Integer> standbyTaskCount = clientTaskCount(clientStateMap, ClientState::standbyTaskCount);

        assertTrue(assignor.canEnableRackAwareAssignor());
        verifyStandbySatisfyRackReplica(taskIds, assignor.racksForProcess(), clientStateMap, replicaCount, false, null);

        final long originalCost = assignor.standByTasksCost(taskIds, clientStateMap, 10, 1);
        assertEquals(60, originalCost);

        // Task can be moved anywhere so cost can be reduced to 30 compared to in shouldOptimizeStandbyTasksWithMovingConstraint it
        // can only be reduced to 50 since there are moving constraints
        final long cost = assignor.optimizeStandbyTasks(clientStateMap, 10, 1,
            (source, destination, task, clients) -> true);
        assertEquals(30, cost);
        // Don't validate tasks in different racks after moving
        verifyStandbySatisfyRackReplica(taskIds, assignor.racksForProcess(), clientStateMap, replicaCount, true, standbyTaskCount);
    }

    @Test
    public void shouldOptimizeStandbyTasksWithMovingConstraint() {
        final int replicaCount = 2;
        final AssignmentConfigs assignorConfiguration = new AssignorConfiguration(new StreamsConfig(configProps(replicaCount)).originals()).assignmentConfigs();
        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getClusterForTopic0And1(),
            getTaskTopicPartitionMapForAllTasks(),
            getTaskChangelogMapForAllTasks(),
            getTopologyGroupTaskMap(),
            getProcessRacksForAllProcess(),
            mockInternalTopicManagerForChangelog(),
            assignorConfiguration
        );

        final ClientState clientState1 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_1);
        final ClientState clientState2 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_2);
        final ClientState clientState3 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_3);
        final ClientState clientState4 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_4);
        final ClientState clientState5 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_6);
        final ClientState clientState6 = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, 1, UUID_7);

        final SortedMap<UUID, ClientState> clientStateMap = new TreeMap<>(mkMap(
            mkEntry(UUID_1, clientState1),
            mkEntry(UUID_2, clientState2),
            mkEntry(UUID_3, clientState3),
            mkEntry(UUID_4, clientState4),
            mkEntry(UUID_6, clientState5),
            mkEntry(UUID_7, clientState6)
        ));

        clientState1.assignActive(TASK_0_0);
        clientState2.assignActive(TASK_0_1);
        clientState3.assignActive(TASK_1_0);
        clientState4.assignActive(TASK_1_1);
        clientState5.assignActive(TASK_0_2);
        clientState6.assignActive(TASK_1_2);

        clientState1.assignStandbyTasks(mkSet(TASK_0_1, TASK_1_1)); // Cost 10
        clientState2.assignStandbyTasks(mkSet(TASK_0_0, TASK_1_0)); // Cost 10
        clientState3.assignStandbyTasks(mkSet(TASK_0_0, TASK_0_2)); // Cost 20
        clientState4.assignStandbyTasks(mkSet(TASK_0_1, TASK_1_2)); // Cost 10
        clientState5.assignStandbyTasks(mkSet(TASK_1_0, TASK_1_2)); // Cost 10
        clientState6.assignStandbyTasks(mkSet(TASK_0_2, TASK_1_1)); // Cost 10

        final SortedSet<TaskId> taskIds = new TreeSet<>(mkSet(TASK_0_0, TASK_0_1, TASK_0_2, TASK_1_0, TASK_1_1, TASK_1_2));
        final Map<UUID, Integer> standbyTaskCount = clientTaskCount(clientStateMap, ClientState::standbyTaskCount);

        assertTrue(assignor.canEnableRackAwareAssignor());
        verifyStandbySatisfyRackReplica(taskIds, assignor.racksForProcess(), clientStateMap, replicaCount, false, null);

        final long originalCost = assignor.standByTasksCost(taskIds, clientStateMap, 10, 1);
        assertEquals(60, originalCost);

        final StandbyTaskAssignor standbyTaskAssignor = StandbyTaskAssignorFactory.create(assignorConfiguration, assignor);
        assertInstanceOf(ClientTagAwareStandbyTaskAssignor.class, standbyTaskAssignor);
        final long cost = assignor.optimizeStandbyTasks(clientStateMap, 10, 1,
            standbyTaskAssignor::isAllowedTaskMovement);
        assertEquals(50, cost);
        // Validate tasks in different racks after moving
        verifyStandbySatisfyRackReplica(taskIds, assignor.racksForProcess(), clientStateMap, replicaCount, false, standbyTaskCount);
    }

    @Test
    public void shouldOptimizeRandomStandby() {
        final int nodeSize = 50;
        final int tpSize = 60;
        final int clientSize = 50;
        final int replicaCount = 3;
        final int maxCapacity = 3;
        final SortedMap<TaskId, Set<TopicPartition>> taskTopicPartitionMap = getTaskTopicPartitionMap(
            tpSize, false);
        final AssignmentConfigs assignorConfiguration = new AssignorConfiguration(
            new StreamsConfig(configProps(replicaCount)).originals()).assignmentConfigs();

        final RackAwareTaskAssignor assignor = new RackAwareTaskAssignor(
            getRandomCluster(nodeSize, tpSize),
            taskTopicPartitionMap,
            getTaskTopicPartitionMap(tpSize, true),
            getTopologyGroupTaskMap(),
            getRandomProcessRacks(clientSize, nodeSize),
            mockInternalTopicManagerForRandomChangelog(nodeSize, tpSize),
            assignorConfiguration
        );

        final SortedMap<UUID, ClientState> clientStateMap = getRandomClientState(clientSize,
            tpSize, maxCapacity);
        final SortedSet<TaskId> taskIds = (SortedSet<TaskId>) taskTopicPartitionMap.keySet();

        final StandbyTaskAssignor standbyTaskAssignor = StandbyTaskAssignorFactory.create(
            assignorConfiguration, assignor);
        assertInstanceOf(ClientTagAwareStandbyTaskAssignor.class, standbyTaskAssignor);
        // Get a standby assignment
        standbyTaskAssignor.assign(clientStateMap, taskIds, taskIds, assignorConfiguration);
        final Map<UUID, Integer> standbyTaskCount = clientTaskCount(clientStateMap,
            ClientState::standbyTaskCount);

        assertTrue(assignor.canEnableRackAwareAssignor());
        verifyStandbySatisfyRackReplica(taskIds, assignor.racksForProcess(), clientStateMap,
            replicaCount, false, null);

        final long originalCost = assignor.standByTasksCost(taskIds, clientStateMap, 10, 1);
        assertThat(originalCost, greaterThanOrEqualTo(0L));

        final long cost = assignor.optimizeStandbyTasks(clientStateMap, 10, 1,
            standbyTaskAssignor::isAllowedTaskMovement);
        assertThat(cost, lessThanOrEqualTo(originalCost));
        // Validate tasks in different racks after moving
        verifyStandbySatisfyRackReplica(taskIds, assignor.racksForProcess(), clientStateMap,
            replicaCount, false, standbyTaskCount);
    }

    private List<Node> getRandomNodes(final int nodeSize) {
        final List<Node> nodeList = new ArrayList<>(nodeSize);
        for (int i = 0; i < nodeSize; i++) {
            nodeList.add(new Node(i, "node" + i, 1, RACK_PREFIX + i));
        }
        Collections.shuffle(nodeList);
        return nodeList;
    }

    private Cluster getRandomCluster(final int nodeSize, final int tpSize) {
        final List<Node> nodeList = getRandomNodes(nodeSize);
        final Set<PartitionInfo> partitionInfoSet = new HashSet<>();
        for (int i = 0; i < tpSize; i++) {
            final Node firstNode = nodeList.get(i % nodeSize);
            final Node secondNode = nodeList.get((i + 1) % nodeSize);
            final Node[] replica = new Node[] {firstNode, secondNode};
            partitionInfoSet.add(new PartitionInfo(TOPIC_PREFIX + i, 0, firstNode, replica, replica));
        }

        return new Cluster(
            "cluster",
            new HashSet<>(nodeList),
            partitionInfoSet,
            Collections.emptySet(),
            Collections.emptySet()
        );
    }

    private Map<UUID, Map<String, Optional<String>>> getRandomProcessRacks(final int clientSize, final int nodeSize) {
        final List<String> racks = new ArrayList<>(nodeSize);
        for (int i = 0; i < nodeSize; i++) {
            racks.add(RACK_PREFIX + i);
        }
        Collections.shuffle(racks);
        final Map<UUID, Map<String, Optional<String>>> processRacks = new HashMap<>();
        for (int i = 0; i < clientSize; i++) {
            final String rack = racks.get(i % nodeSize);
            processRacks.put(uuidForInt(i), mkMap(mkEntry("1", Optional.of(rack))));
        }
        return processRacks;
    }

    private SortedMap<TaskId, Set<TopicPartition>> getTaskTopicPartitionMap(final int tpSize, final boolean changelog) {
        final SortedMap<TaskId, Set<TopicPartition>> taskTopicPartitionMap = new TreeMap<>();
        final String topicName = changelog ? CHANGELOG_TOPIC_PREFIX : TOPIC_PREFIX;
        for (int i = 0; i < tpSize; i++) {
            taskTopicPartitionMap.put(new TaskId(i, 0), mkSet(
                new TopicPartition(topicName + i, 0),
                new TopicPartition(topicName + (i + 1) % tpSize, 0)
            ));
        }
        return taskTopicPartitionMap;
    }

    private InternalTopicManager mockInternalTopicManagerForRandomChangelog(final int nodeSize, final int tpSize) {
        final Set<String> changelogNames = new HashSet<>();
        final List<Node> nodeList = getRandomNodes(nodeSize);
        final Map<String, List<TopicPartitionInfo>> topicPartitionInfo = new HashMap<>();
        for (int i = 0; i < tpSize; i++) {
            final String topicName = CHANGELOG_TOPIC_PREFIX + i;
            changelogNames.add(topicName);

            final Node firstNode = nodeList.get(i % nodeSize);
            final Node secondNode = nodeList.get((i + 1) % nodeSize);
            final TopicPartitionInfo info = new TopicPartitionInfo(0, firstNode, Arrays.asList(firstNode, secondNode), Collections.emptyList());

            topicPartitionInfo.computeIfAbsent(topicName, tp -> new ArrayList<>()).add(info);
        }

        final MockInternalTopicManager spyTopicManager = spy(mockInternalTopicManager);
        doReturn(topicPartitionInfo).when(spyTopicManager).getTopicPartitionInfo(changelogNames);
        return spyTopicManager;
    }

    private SortedMap<UUID, ClientState> getRandomClientState(final int clientSize, final int tpSize, final int maxCapacity) {
        final SortedMap<UUID, ClientState> clientStates = new TreeMap<>();
        final List<TaskId> taskIds = new ArrayList<>(tpSize);
        for (int i = 0; i < tpSize; i++) {
            taskIds.add(new TaskId(i, 0));
        }
        Collections.shuffle(taskIds);
        final Random random = new Random();
        for (int i = 0; i < clientSize; i++) {
            final int capacity = random.nextInt(maxCapacity) + 1;
            final ClientState clientState = new ClientState(emptySet(), emptySet(), emptyMap(), EMPTY_CLIENT_TAGS, capacity, uuidForInt(i));
            clientStates.put(uuidForInt(i), clientState);
        }
        Iterator<Entry<UUID, ClientState>> iterator = clientStates.entrySet().iterator();
        for (final TaskId taskId : taskIds) {
            if (iterator.hasNext()) {
                iterator.next().getValue().assignActive(taskId);
            } else {
                iterator = clientStates.entrySet().iterator();
                iterator.next().getValue().assignActive(taskId);
            }
        }
        return clientStates;
    }

    private Cluster getClusterForTopic0And1() {
        return new Cluster(
            "cluster",
            mkSet(NODE_0, NODE_1, NODE_2, NODE_3),
            mkSet(PI_0_0, PI_0_1, PI_0_2, PI_1_0, PI_1_1, PI_1_2),
            Collections.emptySet(),
            Collections.emptySet()
        );
    }

    private Cluster getClusterForTopic0() {
        return new Cluster(
            "cluster",
            mkSet(NODE_0, NODE_1, NODE_2),
            mkSet(PI_0_0, PI_0_1),
            Collections.emptySet(),
            Collections.emptySet()
        );
    }

    private Cluster getClusterWithPartitionMissingRack() {
        final Node[] nodeMissingRack = new Node[]{NODE_0, NO_RACK_NODE};
        final PartitionInfo partitionInfoMissingNode = new PartitionInfo(TP_0_NAME, 0, NODE_0, nodeMissingRack, nodeMissingRack);
        return new Cluster(
            "cluster",
            mkSet(NODE_0, NODE_1, NODE_2),
            mkSet(partitionInfoMissingNode, PI_0_1),
            Collections.emptySet(),
            Collections.emptySet()
        );
    }

    private Cluster getClusterWithNoNode() {
        final PartitionInfo noNodeInfo = new PartitionInfo(TP_0_NAME, 0, null, new Node[0], new Node[0]);

        return new Cluster(
            "cluster",
            mkSet(NODE_0, NODE_1, NODE_2, Node.noNode()), // mockClientSupplier.setCluster requires noNode
            Collections.singleton(noNodeInfo),
            Collections.emptySet(),
            Collections.emptySet()
        );
    }

    private Map<UUID, Map<String, Optional<String>>> getProcessRacksForAllProcess() {
        return mkMap(
            mkEntry(UUID_1, mkMap(mkEntry("1", Optional.of(RACK_0)))),
            mkEntry(UUID_2, mkMap(mkEntry("1", Optional.of(RACK_1)))),
            mkEntry(UUID_3, mkMap(mkEntry("1", Optional.of(RACK_2)))),
            mkEntry(UUID_4, mkMap(mkEntry("1", Optional.of(RACK_3)))),
            mkEntry(UUID_5, mkMap(mkEntry("1", Optional.of(RACK_4)))),
            mkEntry(UUID_6, mkMap(mkEntry("1", Optional.of(RACK_0)))),
            mkEntry(UUID_7, mkMap(mkEntry("1", Optional.of(RACK_1))))
        );
    }

    private Map<UUID, Map<String, Optional<String>>> getProcessRacksForProcess0() {
        return getProcessRacksForProcess0(false);
    }

    private Map<UUID, Map<String, Optional<String>>> getProcessRacksForProcess0(final boolean missingRack) {
        final Map<UUID, Map<String, Optional<String>>> processRacks = new HashMap<>();
        final Optional<String> rack = missingRack ? Optional.empty() : Optional.of("rack1");
        processRacks.put(UUID_1, Collections.singletonMap("consumer1", rack));
        return processRacks;
    }

    private Map<UUID, Map<String, Optional<String>>> getProcessWithNoConsumerRacks() {
        return mkMap(
            mkEntry(UUID_1, mkMap())
        );
    }

    private Map<TaskId, Set<TopicPartition>> getTaskTopicPartitionMapForTask0() {
        return getTaskTopicPartitionMapForTask0(false);
    }

    private Map<TaskId, Set<TopicPartition>> getTaskChangeLogTopicPartitionMapForTask0() {
        return mkMap(
            mkEntry(TASK_0_0, mkSet(CHANGELOG_TP_0_0))
        );
    }

    private Map<TaskId, Set<TopicPartition>> getTaskTopicPartitionMapForTask0(final boolean extraTopic) {
        final Set<TopicPartition> topicPartitions = new HashSet<>();
        topicPartitions.add(TP_0_0);
        if (extraTopic) {
            topicPartitions.add(TP_1_0);
        }
        return Collections.singletonMap(TASK_0_0, topicPartitions);
    }

    private Map<TaskId, Set<TopicPartition>> getTaskTopicPartitionMapForAllTasks() {
        return mkMap(
            mkEntry(TASK_0_0, mkSet(TP_0_0)),
            mkEntry(TASK_0_1, mkSet(TP_0_1)),
            mkEntry(TASK_0_2, mkSet(TP_0_2)),
            mkEntry(TASK_1_0, mkSet(TP_1_0)),
            mkEntry(TASK_1_1, mkSet(TP_1_1)),
            mkEntry(TASK_1_2, mkSet(TP_1_2))
        );
    }

    private Map<TaskId, Set<TopicPartition>> getTaskChangelogMapForAllTasks() {
        return mkMap(
            mkEntry(TASK_0_0, mkSet(CHANGELOG_TP_0_0)),
            mkEntry(TASK_0_1, mkSet(CHANGELOG_TP_0_1)),
            mkEntry(TASK_0_2, mkSet(CHANGELOG_TP_0_2)),
            mkEntry(TASK_1_0, mkSet(CHANGELOG_TP_1_0)),
            mkEntry(TASK_1_1, mkSet(CHANGELOG_TP_1_1)),
            mkEntry(TASK_1_2, mkSet(CHANGELOG_TP_1_2))
        );
    }

    private InternalTopicManager mockInternalTopicManagerForChangelog() {
        final MockInternalTopicManager spyTopicManager = spy(mockInternalTopicManager);
        doReturn(
            mkMap(
                mkEntry(
                    CHANGELOG_TP_0_NAME, Arrays.asList(
                        new TopicPartitionInfo(0, NODE_0, Arrays.asList(REPLICA_0), Collections.emptyList()),
                        new TopicPartitionInfo(1, NODE_1, Arrays.asList(REPLICA_1), Collections.emptyList()),
                        new TopicPartitionInfo(2, NODE_1, Arrays.asList(REPLICA_1), Collections.emptyList())
                    )
                ),
                mkEntry(
                    CHANGELOG_TP_1_NAME, Arrays.asList(
                        new TopicPartitionInfo(0, NODE_2, Arrays.asList(REPLICA_2), Collections.emptyList()),
                        new TopicPartitionInfo(1, NODE_3, Arrays.asList(REPLICA_3), Collections.emptyList()),
                        new TopicPartitionInfo(2, NODE_0, Arrays.asList(REPLICA_0), Collections.emptyList())
                    )
                )
            )
        ).when(spyTopicManager).getTopicPartitionInfo(mkSet(CHANGELOG_TP_0_NAME, CHANGELOG_TP_1_NAME));
        return spyTopicManager;
    }

    private Map<Subtopology, Set<TaskId>> getTopologyGroupTaskMap() {
        return Collections.singletonMap(SUBTOPOLOGY_0, Collections.singleton(new TaskId(1, 1)));
    }

    private void verifyStandbySatisfyRackReplica(final Set<TaskId> taskIds,
                                                 final Map<UUID, String> racksForProcess,
                                                 final Map<UUID, ClientState> clientStateMap,
                                                 final int replica,
                                                 final boolean relaxRackCheck,
                                                 final Map<UUID, Integer> standbyTaskCount) {
        if (standbyTaskCount != null) {
            for (final Entry<UUID, ClientState> entry : clientStateMap.entrySet()) {
                final int expected = standbyTaskCount.get(entry.getKey());
                final int actual = entry.getValue().standbyTaskCount();
                assertEquals("StandbyTaskCount for " + entry.getKey() + " doesn't match", expected, actual);
            }
        }
        for (final TaskId taskId : taskIds) {
            int activeCount = 0;
            int standbyCount = 0;
            final Map<String, UUID> racks = new HashMap<>();
            for (final Map.Entry<UUID, ClientState> entry : clientStateMap.entrySet()) {
                final UUID processId = entry.getKey();
                final ClientState clientState = entry.getValue();

                if (!relaxRackCheck && clientState.hasAssignedTask(taskId)) {
                    final String rack = racksForProcess.get(processId);
                    assertThat("Task " + taskId + " appears in both " + processId + " and " + racks.get(rack), racks.keySet(), not(hasItems(rack)));
                    racks.put(rack, processId);
                }

                boolean hasActive = false;
                if (clientState.hasActiveTask(taskId)) {
                    activeCount++;
                    hasActive = true;
                }

                boolean hasStandby = false;
                if (clientState.hasStandbyTask(taskId)) {
                    standbyCount++;
                    hasStandby = true;
                }

                assertFalse(clientState + " has both active and standby task " + taskId, hasActive && hasStandby);
            }

            assertEquals("Task " + taskId + " should have 1 active task", 1, activeCount);
            assertEquals("Task " + taskId + " has wrong replica count", replica, standbyCount);
        }
    }

    private Map<UUID, Integer> clientTaskCount(final Map<UUID, ClientState> clientStateMap,
                                               final Function<ClientState, Integer> taskFunc) {
        return clientStateMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, v -> taskFunc.apply(v.getValue())));
    }
}
