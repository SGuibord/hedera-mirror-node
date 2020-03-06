package com.hedera.mirror.grpc.jmeter.handler;

/*-
 * ‌
 * Hedera Mirror Node
 * ​
 * Copyright (C) 2019 Hedera Hashgraph, LLC
 * ​
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
 * ‍
 */

import java.time.Instant;
import lombok.extern.log4j.Log4j2;

import com.hedera.mirror.grpc.converter.InstantToLongConverter;

@Log4j2
public class ConnectionHandler {

    private final InstantToLongConverter converter = new InstantToLongConverter();
    // private final DatabaseClient client;
    private final String host;
    private final int port;
    private final String dbName;
    private final String dbUser;
    private final String dbPassword;
    // private final PostgresqlConnectionFactory connectionFactory;

    public ConnectionHandler(String host, int port, String dbName, String dbUser, String dbPassword) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

//        connectionFactory = getConnectionFactory();
//        client = getClient();
    }
//
//    private PostgresqlConnectionFactory getConnectionFactory() {
//        if (connectionFactory != null) {
//            return connectionFactory;
//        }
//
//        log.trace("Initialize connectionFactory");
//        PostgresqlConnectionFactory connectionFactory = new PostgresqlConnectionFactory(
//                PostgresqlConnectionConfiguration.builder()
//                        .host(host)
//                        .port(port)
//                        .username(dbUser)
//                        .password(dbPassword)
//                        .database(dbName)
//                        .build());
//
//        return connectionFactory;
//    }
//
//    private DatabaseClient getClient() {
//        return DatabaseClient.create(connectionFactory);
//    }
//
//    private PostgresqlBatch getBatch() {
//        return connectionFactory.create().block().createBatch();
//    }

    public long createNextTopic() {
        long topicNum = getNextAvailableTopicID();

        createTopic(topicNum);
        return topicNum;
    }

    public void createTopic(long topicNum) {
        String entityInsertSql = "insert into t_entities"
                + " (entity_num, entity_realm, entity_shard, fk_entity_type_id)"
                + " values ($1, $2, $3, $4) on conflict do nothing";
//        client.execute(entityInsertSql)
//                .bind("$1", topicNum)
//                .bind("$2", 0)
//                .bind("$3", 0)
//                .bind("$4", 4)
//                .then()
//                .block();

        log.trace("Created new Topic {}", topicNum);
    }

    public boolean topicExists(long topicId) {
        boolean topicExists = false;

        String topicSelectSql = "select id from t_entities where entity_shard = $1 and entity_realm = $2 and " +
                "entity_num = $3";

//        topicExists = client.execute(topicSelectSql)
//                .bind("$1", 0)
//                .bind("$2", 0)
//                .bind("$3", topicId)
//                .map((row, metadata) -> {
//                    Long topicNum = row.get(0, Long.class);
//
//                    if (topicNum == null) {
//                        return false;
//                    }
//
//                    return true;
//                })
//                .first()
//                .defaultIfEmpty(false)
//                .block();

        return topicExists;
    }

    public void insertTopicMessage(int newTopicsMessageCount, long topicNum, Instant startTime, long seqStart) {
        if (newTopicsMessageCount == 0) {
            // no messages to create, abort and db logic
            return;
        }

        createTopic(topicNum);

        long nextSequenceNum = seqStart == -1 ? getNextAvailableSequenceNumber(topicNum) : seqStart;
        log.info("Inserting {} topic messages starting from sequence number {} and time {}", newTopicsMessageCount,
                nextSequenceNum, startTime);

//        PostgresqlBatch batch = getBatch();
//        String batchEntry = "insert into topic_message"
//                + " (consensus_timestamp, realm_num, topic_num, message, running_hash, sequence_number)"
//                + " values (%s, %s, %s, %s, %s, %s)";
//
//        for (int i = 0; i < newTopicsMessageCount; i++) {
//            long sequenceNum = nextSequenceNum + i;
//            Instant temp = startTime.plus(sequenceNum, ChronoUnit.NANOS);
//            Long consensusTimestamp = converter.convert(temp);
//
//            batch.add(String
//                    .format(batchEntry, consensusTimestamp, 0, topicNum, "'\\xdeadbeef'", "'\\xdeadbeef'",
//                            sequenceNum));
//
//            log.trace("Adding TopicMessage {}, Time: {}, count: {}, seq : {} to batch", topicNum,
//            consensusTimestamp, i,
//                    sequenceNum);
//        }
//
//        batch.execute()
//                .then()
//                .block();

        log.debug("Successfully inserted {} topic messages", newTopicsMessageCount);
    }

    public long getNextAvailableTopicID() {
        String nextTopicIdSql = "SELECT MAX(entity_num) FROM t_entities";

        long nextTopicId = 1;// + client.execute(nextTopicIdSql)
//                .map((row, metadata) -> {
//                    Long topicNum = row.get(0, Long.class);
//
//                    if (topicNum == null) {
//                        throw new IllegalStateException("Topic num query failed");
//                    }
//
//                    return topicNum;
//                }).first().block();

        log.trace("Next available topic ID number is {}", nextTopicId);
        return nextTopicId;
    }

    public long getNextAvailableSequenceNumber(long topicId) {
        String nextSeqSql = "SELECT MAX(sequence_number) FROM topic_message WHERE topic_num = $1";

        long nextSeqNum = 1;// + client.execute(nextSeqSql)
//                .bind("$1", topicId)
//                .map((row, metadata) -> {
//                    Long max = row.get(0, Long.class);
//
//                    if (max == null) {
//                        max = -1L;
//                        log.trace("Max sequence num query failed, setting max to -1 as likely no messages " +
//                                "for this topic exist");
//                    }
//
//                    return max;
//                }).first().block();

        log.trace("Next available topic ID sequence number is {}", nextSeqNum);
        return nextSeqNum;
    }

    public void clearTopicMessages(long topicId, long seqNumFrom) {
        if (topicId < 0 || seqNumFrom < 0) {
            log.warn("TopicId : {} or SeqNum : {} are outside of acceptable range. clearTopicMessages() will be " +
                    "skipped.", topicId, seqNumFrom);
            return;
        }

        String delTopicMsgsSql = "delete from topic_message where topic_num = $1 and sequence_number >= $2";

//        client.execute(delTopicMsgsSql)
//                .bind("$1", topicId)
//                .bind("$2", seqNumFrom)
//                .then().block();

        log.info("Cleared topic messages for topic ID {} after sequence {}", topicId, seqNumFrom);
    }
}
